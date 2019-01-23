package com.opencredo.connect.venafi.tpp.log.model;

import java.time.ZonedDateTime;

public class TppToken {
    private String APIKey;
    private ZonedDateTime validUntil;

    public String getAPIKey() {
        return APIKey;
    }

    public ZonedDateTime getValidUntil() {
        return validUntil;
    }
}
