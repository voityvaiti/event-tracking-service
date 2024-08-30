package com.myproject.eventtrackingservice;

import static org.mockito.Mockito.*;

import com.myproject.eventtrackingservice.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessEventsPayloadWithSuccess() throws Exception {

        long validTimestamp = System.currentTimeMillis() - 3 * 1000;

        String payload = validTimestamp + ",0.0899538547,1282509067\n" + validTimestamp + ",0.0876221433,1194727708";
        Map<Integer, String> processingResults = new HashMap<>();
        processingResults.put(1, "Successfully processed");
        processingResults.put(2, "Successfully processed");

        when(eventService.validateAndProcessEvents(payload)).thenReturn(processingResults);

        mockMvc.perform(MockMvcRequestBuilders.post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(MockMvcResultMatchers.status().isAccepted())
                .andExpect(MockMvcResultMatchers.jsonPath("$['1']").value("Successfully processed"))
                .andExpect(MockMvcResultMatchers.jsonPath("$['2']").value("Successfully processed"));
    }

    @Test
    void testProcessEventsPayloadWithErrors() throws Exception {

        long validTimestamp = System.currentTimeMillis() - 3 * 1000;

        String payload = "invalid data\n" + validTimestamp + ",1.04422968,45";
        Map<Integer, String> processingResults = new HashMap<>();
        processingResults.put(1, "Invalid event data format.");
        processingResults.put(2, "Validation error.");

        when(eventService.validateAndProcessEvents(payload)).thenReturn(processingResults);

        mockMvc.perform(MockMvcRequestBuilders.post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$['1']").value("Invalid event data format."))
                .andExpect(MockMvcResultMatchers.jsonPath("$['2']").value("Validation error."));
    }

    @Test
    void testProcessEventsPayloadWithMixedResults() throws Exception {

        long validTimestamp = System.currentTimeMillis() - 3 * 1000;

        String payload = validTimestamp + ",12.5,50\ninvalid data";
        Map<Integer, String> processingResults = new HashMap<>();
        processingResults.put(1, "Successfully processed");
        processingResults.put(2, "Invalid event data format.");

        when(eventService.validateAndProcessEvents(payload)).thenReturn(processingResults);

        mockMvc.perform(MockMvcRequestBuilders.post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(MockMvcResultMatchers.status().isMultiStatus())
                .andExpect(MockMvcResultMatchers.jsonPath("$['1']").value("Successfully processed"))
                .andExpect(MockMvcResultMatchers.jsonPath("$['2']").value("Invalid event data format."));
    }

    @Test
    void testGetStats() throws Exception {

        String stats = "2,0.1775759980,0.0887879990,2477236775,1238618387.500";
        when(eventService.getStats()).thenReturn(stats);

        mockMvc.perform(MockMvcRequestBuilders.get("/stats"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(stats));
    }
}
