package com.myproject.eventtrackingservice.service;

import java.util.Map;

public interface EventService {

    Map<Integer, String> validateAndProcessEvents(String payload);

    String getStats();

}
