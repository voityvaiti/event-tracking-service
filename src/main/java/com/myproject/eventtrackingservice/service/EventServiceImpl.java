package com.myproject.eventtrackingservice.service;

import com.myproject.eventtrackingservice.EventDto;
import jakarta.validation.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    public static final String SUCCESS_MESSAGE = "Successfully processed";

    private static final String DEFAULT_STATS = "0,0.0000000000,0.0000000000,0,0.000";

    private static final int MINUTE_IN_MILLIS = 60 * 1000;


    private final Deque<EventDto> eventDeque = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();

    private int eventCount = 0;
    private double sumX = 0.0;
    private long sumY = 0;

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    @Override
    public String getStats() {

        cleanExpiredEvents();

        if (eventCount == 0) {
            return DEFAULT_STATS;
        }

        double avgX = sumX / eventCount;
        double avgY = (double) sumY / eventCount;

        return String.format(Locale.US, "%d,%.10f,%.10f,%d,%.3f", eventCount, sumX, avgX, sumY, avgY);
    }


    @Override
    public Map<Integer, String> validateAndProcessEvents(String payload) {

        Map<Integer, String> eventProcessingResults = new HashMap<>();

        String[] lines = payload.split("\n");

        for (int i = 0; i < lines.length; i++) {

            String result = processLine(lines[i]);
            eventProcessingResults.put(i + 1, result);
        }

        return eventProcessingResults;
    }

    private void cleanExpiredEvents() {

        long past60Seconds = System.currentTimeMillis() - MINUTE_IN_MILLIS;

        lock.lock();
        try {

            while (!eventDeque.isEmpty() && eventDeque.peekFirst().getTimestamp() < past60Seconds) {
                EventDto expiredEvent = eventDeque.pollFirst();
                eventCount--;
                sumX -= expiredEvent.getX();
                sumY -= expiredEvent.getY();
            }

        } finally {
            lock.unlock();
        }

    }

    private String processLine(String line) {

        return parseEvent(line)
                .map(event -> {

                    if (event.getTimestamp() < System.currentTimeMillis() - MINUTE_IN_MILLIS) {
                        return "Event timestamp is too old and cannot be processed.";
                    }

                    return validateEvent(event)
                            .map(this::formatValidationErrors)
                            .orElseGet(() -> {
                                addEvent(event);
                                return SUCCESS_MESSAGE;
                            });
                })
                .orElse("Invalid event data format.");
    }

    private Optional<EventDto> parseEvent(String line) {

        String[] parts = line.trim().split(",");

        if (parts.length != 3) return Optional.empty();

        try {
            long timestamp = Long.parseLong(parts[0]);
            double x = Double.parseDouble(parts[1]);
            int y = Integer.parseInt(parts[2]);

            EventDto event = new EventDto();
            event.setTimestamp(timestamp);
            event.setX(x);
            event.setY(y);

            return Optional.of(event);

        } catch (NumberFormatException e) {

            return Optional.empty();
        }
    }

    private Optional<Map<String, String>> validateEvent(EventDto event) {

        Set<ConstraintViolation<EventDto>> violations = validator.validate(event);

        if (violations.isEmpty()) {
            return Optional.empty();
        }

        Map<String, String> validationErrors = violations.stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> "Value " + violation.getInvalidValue() + " is invalid: " + violation.getMessage()
                ));

        return Optional.of(validationErrors);
    }


    private void addEvent(EventDto event) {

        lock.lock();

        try {
            ListIterator<EventDto> iterator = ((LinkedList<EventDto>) eventDeque).listIterator(eventDeque.size());

            while (iterator.hasPrevious()) {

                EventDto current = iterator.previous();
                if (current.getTimestamp() <= event.getTimestamp()) {
                    iterator.next();
                    iterator.add(event);
                    break;
                }
            }

            if (!iterator.hasPrevious()) {
                eventDeque.addFirst(event);
            }

            eventCount++;
            sumX += event.getX();
            sumY += event.getY();

        } finally {
            lock.unlock();
        }
    }


    private String formatValidationErrors(Map<String, String> errors) {
        return errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));
    }

}
