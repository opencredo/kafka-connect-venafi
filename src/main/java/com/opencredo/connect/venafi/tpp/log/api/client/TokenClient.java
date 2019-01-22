package com.opencredo.connect.venafi.tpp.log.api.client;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.opencredo.connect.venafi.tpp.log.Deserializer.DotNetDateDeserializer;
import com.opencredo.connect.venafi.tpp.log.api.TppPlatformAuthorization;
import com.opencredo.connect.venafi.tpp.log.model.Credentials;
import com.opencredo.connect.venafi.tpp.log.model.TppToken;
import feign.Feign;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;

import java.time.ZonedDateTime;

public class TokenClient {

    private String tokenValue;
    private ZonedDateTime tokenExpiry = ZonedDateTime.now();

    private static GsonDecoder customDecoder() {
        return new GsonDecoder(
                new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                        .registerTypeAdapter(ZonedDateTime.class, new DotNetDateDeserializer())
                        .create());
    }

    public String getToken(String baseUrl) {
        if (isTokenInvalid()) {
            Credentials credentials = new Credentials("rufus", "qxaag{q,h=g$9~!e");
            TppToken token = Feign
                    .builder()
                    .encoder(new GsonEncoder())
                    .logger(new Slf4jLogger())
                    .decoder(customDecoder())
                    .retryer(Retryer.NEVER_RETRY)
                    .target(TppPlatformAuthorization.class, baseUrl)
                    .getToken(credentials);

            tokenValue = token.getAPIKey();
            tokenExpiry = token.getValidUntil();
        }
        return tokenValue;

    }

    private boolean isTokenInvalid() {
        return tokenValue == null || tokenExpiry.isBefore(ZonedDateTime.now().minusSeconds(10L));
    }
}
