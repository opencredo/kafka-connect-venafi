package com.opencredo.connect.venafi.tpp.log.model;

import java.time.ZonedDateTime;

public class TppToken {
    private String access_token;
    private ZonedDateTime expires;
    private String refresh_token;
    private ZonedDateTime refresh_until;

    public String getAccessToken() {
        return access_token;
    }

    public ZonedDateTime getExpires() {
        return expires;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public ZonedDateTime getRefresh_until() {
        return refresh_until;
    }
}
