package com.opencredo.connect.venafi.tpp.log.model;

public class TppRefreshToken {

    private String refresh_token;
    private String client_id;

    public TppRefreshToken(String refresh_token, String client_id) {
        this.refresh_token = refresh_token;
        this.client_id = client_id;
    }
}
