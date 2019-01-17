package com.opencredo.connect.venafi.tpp.log.api.client;

import com.google.gson.GsonBuilder;
import com.opencredo.connect.venafi.tpp.log.Deserializer.ZonedDateTimeDeserializer;
import com.opencredo.connect.venafi.tpp.log.api.TppLog;
import com.opencredo.connect.venafi.tpp.log.model.LogResponse;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.slf4j.Slf4jLogger;

import java.time.ZonedDateTime;

public class LogsClient {

    public static LogResponse getLogs(String token, String date, String baseUrl, String batchSize, int offset) {
        return Feign.builder()
                    .logger(new Slf4jLogger())
                    .decoder(logDecoder())
                    .target(TppLog.class, baseUrl)
                    .getLogs(token, date, batchSize, offset );
    }

    private static GsonDecoder logDecoder() {
        return new GsonDecoder(
                new GsonBuilder()
                        .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeDeserializer())
                        .create());
    }
}
