package com.myproject.eventtrackingservice.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.Locale;
import java.util.Map;

class EventServiceImplClientTest {

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventService = new EventServiceImpl();
    }

    @Test
    void testGetStatsWithNoEvents() {

        String stats = eventService.getStats();
        assertEquals("0,0.0000000000,0.0000000000,0,0.000", stats);
    }

    @Test
    void testValidateAndProcessEventsWithValidPayload() {

        long validTimestamp = System.currentTimeMillis() - 3 * 1000;

        String payload = validTimestamp + ",0.0899538547,1282509067\n" + validTimestamp + ",0.0876221433,1194727708";
        Map<Integer, String> results = eventService.validateAndProcessEvents(payload);

        assertEquals(2, results.size());
        assertEquals("Successfully processed", results.get(1));
        assertEquals("Successfully processed", results.get(2));

        String stats = eventService.getStats();


        int eventCount = 2;
        double sumX = 0.0899538547 + 0.0876221433;
        double avgX = sumX / eventCount;
        long sumY = 1282509067L + 1194727708L;
        double avgY = (double) sumY / eventCount;

        String expectedSumX = String.format(Locale.US, "%.10f", sumX);
        String expectedAvgX = String.format(Locale.US, "%.10f", avgX);
        String expectedSumY = String.format(Locale.US, "%d", sumY);
        String expectedAvgY = String.format(Locale.US, "%.3f", avgY);

        assertTrue(stats.contains(eventCount + ","));
        assertTrue(stats.contains(expectedSumX + ","));
        assertTrue(stats.contains(expectedAvgX + ","));
        assertTrue(stats.contains(expectedSumY + ","));
        assertTrue(stats.contains(expectedAvgY));
    }


    @Test
    void testValidateAndProcessEventsWithInvalidPayload() {

        long validTimestamp = System.currentTimeMillis() - 4 * 1000;

        String payload = "invalid data\n" + validTimestamp + ",0.0302456915,1112127673";
        Map<Integer, String> results = eventService.validateAndProcessEvents(payload);

        assertEquals(2, results.size());
        assertEquals("Invalid event data format.", results.get(1));
        assertEquals("Successfully processed", results.get(2));
    }

    @Test
    void testEventTimestampTooOld() {

        long oldTimestamp = System.currentTimeMillis() - 2 * 60 * 1000;
        String payload = oldTimestamp + ",0.0231608748,1282509067";
        Map<Integer, String> results = eventService.validateAndProcessEvents(payload);

        assertEquals(1, results.size());
        assertEquals("Event timestamp is too old and cannot be processed.", results.get(1));
    }

    @Test
    void testCleanExpiredEvents() throws InterruptedException {

        long past55Seconds = System.currentTimeMillis() - 5 * 60 * 1000;

        eventService.validateAndProcessEvents(past55Seconds + ",0.0360791311,1282509067");

        Thread.sleep(7 * 1000);

        eventService.getStats();
        String stats = eventService.getStats();
        assertEquals("0,0.0000000000,0.0000000000,0,0.000", stats);
    }

}