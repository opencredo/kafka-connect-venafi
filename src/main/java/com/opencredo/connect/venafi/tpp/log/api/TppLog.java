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
    String ORDER = "Order";
    String ORDERING = "ServerTimestamp";
    String OFFSET = "OFFSET";

    @RequestLine("GET /Log")
    @Headers({"Content-Type: application/json", "X-Venafi-Api-Key: {token}"})
    LogResponse getLogs(@Param("token") String token, @QueryMap Map<String, Object> queryMap);

    //If we in the future need to send a query Param with +
    // be aware TPP server decodes plus as space so we'd have to actively encode + as %2B
    default LogResponse getLogs(String token, String fromTime, String limit, int offset) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(FROM_TIME, fromTime);
        queryParams.put(LIMIT, limit);
        queryParams.put(OFFSET,offset);
        queryParams.put(ORDER, ORDERING);
        return getLogs(token, queryParams);
    }
}
