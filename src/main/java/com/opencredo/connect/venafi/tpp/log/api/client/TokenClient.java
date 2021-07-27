package com.opencredo.connect.venafi.tpp.log.api.client;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.opencredo.connect.venafi.tpp.log.Deserializer.EpochSecondsDeserializer;
import com.opencredo.connect.venafi.tpp.log.api.TppPlatformAuthorization;
import com.opencredo.connect.venafi.tpp.log.model.Credentials;
import com.opencredo.connect.venafi.tpp.log.model.TppRefreshToken;
import com.opencredo.connect.venafi.tpp.log.model.TppToken;
import feign.Feign;
import feign.FeignException;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

public class TokenClient {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TokenClient.class);

    private String tokenValue;
    private ZonedDateTime tokenExpiry = ZonedDateTime.now();
    private ZonedDateTime tokenRefreshUntil = ZonedDateTime.now();
    private Credentials credentials;
    private TppRefreshToken tppRefreshToken;
    private String clientId;

    public TokenClient(String username, String password, String scope, String clientId) {
        this.credentials = new Credentials(username, password, scope, clientId);
        this.clientId = clientId;
    }

    private static GsonDecoder customDecoder() {
        return new GsonDecoder(
                new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .registerTypeAdapter(ZonedDateTime.class, new EpochSecondsDeserializer())
                        .create());
    }

    public String getToken(String baseUrl) {
        try {
            TppToken token;
            TppPlatformAuthorization tppAuth = tppAuth(baseUrl);

            if (isTokenValid()) {
                return tokenValue;
            } else if (isTokenInvalid()) {
                log.info("Getting new authorization token");
                 token = tppAuth.getToken(credentials);
            } else if (isTokenExpired()) {
                log.info("Refreshing authorization token");
                token = tppAuth.refreshToken(tppRefreshToken);
            } else {
                token = tppAuth.getToken(credentials);
            }

            tokenValue = token.getAccessToken();
            tokenExpiry = token.getExpires();
            tokenRefreshUntil = token.getRefresh_until();
            tppRefreshToken = new TppRefreshToken(token.getRefresh_token(), clientId);
        } catch (FeignException e) {
            log.error("Caught following exception, ignoring to ensure connector doesn't fail", e);
            tokenValue = "";
            tokenExpiry = ZonedDateTime.now();
            tokenRefreshUntil = ZonedDateTime.now();
            tppRefreshToken = null;
            return "";
        }
        return tokenValue;
    }

    /**
     * If it is the first time we are calling /authorize endpoint at connector startup
     *
     */
    private boolean isTokenInvalid() {
        return tokenValue == null && tppRefreshToken == null;
    }

    private boolean isTokenValid() {
        return tokenValue != null && tokenExpiry.isAfter(ZonedDateTime.now().minusSeconds(10L));
    }

    /**
     * If token is expired but the refresh_token is still valid
     */
    private boolean isTokenExpired() {
        return tokenExpiry.isBefore(ZonedDateTime.now().minusSeconds(10L)) &&
                tokenRefreshUntil.isAfter(ZonedDateTime.now().plusSeconds(10L));
    }

    private TppPlatformAuthorization tppAuth(String baseUrl) {
        return Feign.builder()
                .encoder(new GsonEncoder())
                .logger(new Slf4jLogger())
                .decoder(customDecoder())
                .retryer(Retryer.NEVER_RETRY)
                .target(TppPlatformAuthorization.class, baseUrl);
    }
}
