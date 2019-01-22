package com.opencredo.connect.venafi.tpp.log.api;

import com.opencredo.connect.venafi.tpp.log.model.Credentials;
import com.opencredo.connect.venafi.tpp.log.model.TppToken;
import feign.Headers;
import feign.RequestLine;

public interface TppPlatformAuthorization {


    @RequestLine("POST /authorize")
    @Headers("Content-Type: application/json")
    TppToken getToken(Credentials credentials);


}
