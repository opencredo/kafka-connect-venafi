package com.opencredo.connect.venafi.tpp.log.model;


import java.util.List;

public class LogResponse {
    private List<EventLog> LogEvents;

    public LogResponse(List<EventLog> logEvents) {
        LogEvents = logEvents;
    }

    public List<EventLog> getLogEvents() {
        return LogEvents;
    }

    public void setLogEvents(List<EventLog> logEvents) {
        LogEvents = logEvents;
    }
}
