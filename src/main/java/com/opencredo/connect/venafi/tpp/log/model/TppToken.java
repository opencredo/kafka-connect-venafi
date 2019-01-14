package com.opencredo.connect.venafi.tpp.log.model;

import java.time.ZonedDateTime;

public class TppToken {
    private final String APIKey;
    private final ZonedDateTime validUntil;

    public TppToken(String apiKey, ZonedDateTime validUntil) {
        APIKey = apiKey;
        this.validUntil = validUntil;
    }

    public String getAPIKey() {
        return APIKey;
    }

    public ZonedDateTime getValidUntil() {
        return validUntil;
    }
}
