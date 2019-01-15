package com.opencredo.connect.venafi.tpp.log.api;

import com.opencredo.connect.venafi.tpp.log.model.LogResponse;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

import java.util.HashMap;
import java.util.Map;

public interface TppLog {

    String FROM_TIME = "FromTime";
    String LIMIT = "Limit";

    @RequestLine("GET /Log")
    @Headers({"Content-Type: application/json", "X-Venafi-Api-Key: {token}"})
    LogResponse getLogs(@Param("token") String token, @QueryMap Map<String, Object> queryMap);

    default LogResponse getLogs(String token, String fromTime, String limit) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(FROM_TIME, fromTime);
        queryParams.put(LIMIT, limit);
        return getLogs(token, queryParams);
    }
}
