package com.opencredo.connect.venafi.tpp.log.api;

import com.opencredo.connect.venafi.tpp.log.model.Credentials;
import com.opencredo.connect.venafi.tpp.log.model.TppRefreshToken;
import com.opencredo.connect.venafi.tpp.log.model.TppToken;
import feign.Headers;
import feign.RequestLine;

public interface TppPlatformAuthorization {

    @RequestLine("POST /vedauth/authorize/")
    @Headers("Content-Type: application/json")
    TppToken getToken(Credentials credentials);

    @RequestLine("POST /vedauth/authorize/token")
    @Headers("Content-Type: application/json")
    TppToken refreshToken(TppRefreshToken refreshToken);
}
