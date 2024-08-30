package com.myproject.eventtrackingservice;

import com.myproject.eventtrackingservice.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.myproject.eventtrackingservice.service.EventServiceImpl.SUCCESS_MESSAGE;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/event")
    public ResponseEntity<Map<Integer, String>> processEventsPayload(@RequestBody String payload) {

        Map<Integer, String> eventProcessingResults = eventService.validateAndProcessEvents(payload);

        boolean hasErrors = eventProcessingResults.values().stream()
                .anyMatch(status -> !status.equals(SUCCESS_MESSAGE));

        boolean hasSuccess = eventProcessingResults.values().stream()
                .anyMatch(status -> status.equals(SUCCESS_MESSAGE));

        HttpStatus status;

        if (hasErrors && hasSuccess) {
            status = HttpStatus.MULTI_STATUS;
        } else if (hasErrors) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.ACCEPTED;
        }

        return new ResponseEntity<>(eventProcessingResults, status);
    }

    @GetMapping("/stats")
    public ResponseEntity<String> getStats() {
        return ResponseEntity.ok(eventService.getStats());
    }
}
