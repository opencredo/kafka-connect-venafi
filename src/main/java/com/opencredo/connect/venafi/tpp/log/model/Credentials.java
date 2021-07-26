package com.opencredo.connect.venafi.tpp.log.model;

public class Credentials {
    //client_id to allow GSON to auto convert them to JSON without needing annotations.

    private String username;
    private String password;
    private String client_id;
    private String scope;

    public Credentials(String username, String password, String scope, String client_id) {
        this.username = username;
        this.password = password;
        this.client_id = client_id;
        this.scope = scope;
    }
}
