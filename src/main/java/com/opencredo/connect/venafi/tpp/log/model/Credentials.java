package com.opencredo.connect.venafi.tpp.log.model;

public class Credentials {
//These are uppercase currently to allow GSON to auto convert them to JSON without needing annotations.
    private String Username;
    private String Password;

    public Credentials(String username, String password) {
        Username = username;
        Password = password;
    }
}
