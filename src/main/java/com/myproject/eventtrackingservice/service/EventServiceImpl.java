package com.myproject.eventtrackingservice.service;

import com.myproject.eventtrackingservice.EventDto;
import jakarta.validation.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    public static final String SUCCESS_MESSAGE = "Successfully processed";

    private static final String DEFAULT_STATS = "0,0.0000000000,0.0000000000,0,0.000";

    private static final int MINUTE_IN_MILLIS = 60000;


    private final List<EventDto> events = Collections.synchronizedList(new ArrayList<>());

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    @Override
    public String getStats() {

        long currentTime = System.currentTimeMillis();
        long past60Seconds = currentTime - MINUTE_IN_MILLIS;

        List<EventDto> recentEvents = events.stream()
                .filter(event -> event.getTimestamp() >= past60Seconds)
                .toList();

        if (recentEvents.isEmpty()) {
            return DEFAULT_STATS;
        }

        int total = recentEvents.size();
        double sumX = recentEvents.stream().mapToDouble(EventDto::getX).sum();
        double avgX = sumX / total;
        long sumY = recentEvents.stream().mapToLong(EventDto::getY).sum();
        double avgY = (double) sumY / total;

        return String.format(Locale.US, "%d,%.10f,%.10f,%d,%.3f", total, sumX, avgX, sumY, avgY);
    }


    @Override
    public Map<Integer, String> validateAndProcessEvents(String payload) throws IllegalArgumentException {

        Map<Integer, String> eventsErrors = new HashMap<>();

        String[] lines = payload.split("\n");

        for (int i = 0; i < lines.length; i++) {

            String result = processLine(lines[i]);
            eventsErrors.put(i + 1, result);
        }

        return eventsErrors;
    }

    private String processLine(String line) {

        return parseEvent(line)
                .map(event -> validateEvent(event)

                        .map(this::formatValidationErrors)
                        .orElseGet(() -> {

                            synchronized (events) {
                                events.add(event);
                            }

                            return SUCCESS_MESSAGE;
                        })
                )
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

    private String formatValidationErrors(Map<String, String> errors) {
        return errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));
    }

}
