package com.opencredo.connect.venafi.tpp.log;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.opencredo.connect.venafi.tpp.log.model.EventLog;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.opencredo.connect.venafi.tpp.log.TppLogSourceConnector.*;
import static com.opencredo.connect.venafi.tpp.log.api.TppLog.FROM_TIME;
import static com.opencredo.connect.venafi.tpp.log.api.TppLog.OFFSET;
import static com.opencredo.connect.venafi.tpp.log.model.EventLog.*;
import static org.junit.jupiter.api.Assertions.*;

public class EventLogSourceTaskTest {

    public static final String TODAY = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    public WireMockServer wireMockServer = new WireMockServer(
            new WireMockConfiguration()
                    .dynamicPort()
                    .extensions(
                            new ResponseTemplateTransformer(false)
                    ));

    @BeforeEach
    private void setup() {
        wireMockServer.start();
    }

    @AfterEach
    private void shutdown() {
        wireMockServer.shutdown();
    }

    @Test
    public void as_a_client_I_want_a_token() {


        given_the_mock_will_respond_to_auth();
        TppLogSourceTask task = given_a_task_is_setup();

        String token = when_a_token_is_got(task);
        assertNotNull(token);
        assertNotEquals("", token);
    }

    @Test
    public void as_a_client_I_want_some_logs() {


        given_the_mock_will_respond_to_auth();
        given_the_mock_will_respond_to_log();
        TppLogSourceTask task = given_a_task_is_setup();

        String token = when_a_token_is_got(task);
        List<EventLog> logs = when_the_logs_are_got(task, token);
        assertNotNull(logs);
        assertNotEquals(Collections.emptyList(), logs);
        assertEquals(5, logs.size());
    }

    @Test
    public void as_a_client_I_want_to_paginate_logs() {


        given_the_mock_will_respond_to_auth();
        given_the_mock_will_respond_to_log();
        TppLogSourceTask task = given_a_task_is_setup();

        List<SourceRecord> logs = task.poll();
        assertNotNull(logs);
        assertEquals(5, logs.size());
        //get next page
        List<SourceRecord> page_2_of_logs = task.poll();
        assertNotNull(page_2_of_logs);
        assertEquals(5, page_2_of_logs.size());
        //get next Page
        List<SourceRecord> page_3_of_logs = task.poll();
        assertNotNull(page_3_of_logs);
        assertEquals(4, page_3_of_logs.size());

        List<SourceRecord> page_4_of_logs = task.poll();
        assertNotNull(page_4_of_logs);
        assertEquals(2, page_4_of_logs.size());
    }

    @Test
    public void as_a_connector_I_want_to_pass_an_object_as_a_struct() {
        Struct struct = new Struct(TppLogSchema())
                .put(CLIENT_TIMESTAMP, new Date())
                .put(COMPONENT, COMPONENT)
                .put(COMPONENT_ID, 123)
                .put(COMPONENT_SUBSYSTEM, COMPONENT_SUBSYSTEM)
                .put(EVENT_ID, EVENT_ID)
                .put(GROUPING, 123)
                .put(ID, 123L)
                .put(NAME, NAME)
                .put(SERVER_TIMESTAMP, new Date())
                .put(SEVERITY, SEVERITY)
                .put(SOURCE_IP, SOURCE_IP);
        SourceRecord record = new SourceRecord(Collections.emptyMap(), Collections.emptyMap(), "", TppLogSchema(), struct);
        System.out.println(record);
    }

    public String when_a_token_is_got(TppLogSourceTask task) {
        return task.getToken();
    }

    public List<EventLog> when_the_logs_are_got(TppLogSourceTask task, String token) {
        return task.getTppLogs(token, TODAY, 0);
    }

    public TppLogSourceTask given_a_task_is_setup() {
        TppLogSourceTask task = new TppLogSourceTask();
        Map<String, String> config = new HashMap<>();
        config.put(BASE_URL_CONFIG, wireMockServer.baseUrl());
        config.put(TOPIC_CONFIG, "temp");
        config.put(BATCH_SIZE, "1000");
        config.put(POLL_INTERVAL, "0");
        task.start(config);
        return task;
    }

    public void given_the_mock_will_respond_to_auth() {
        wireMockServer.stubFor(post(urlPathEqualTo("/authorize/"))
                .withRequestBody(equalToJson("{\n" +
                        "\t\"Username\":\"rufus\",\n" +
                        "\t\"Password\":\"qxaag{q,h=g$9~!e\"\n" +
                        "}")).withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"APIKey\": \"" + UUID.randomUUID() + "\",\n" +
                        "    \"ValidUntil\": \"/Date(" + LocalDateTime.now().plusMinutes(3).toEpochSecond(ZoneOffset.UTC) + ")/\"\n" +
                        "}")
                ));
    }

    public void given_the_mock_will_respond_to_log() {
        ZonedDateTime d1 = ZonedDateTime.now().plusSeconds(1);
        ZonedDateTime d2 = d1.plusSeconds(1);
        ZonedDateTime d3 = d2.plusSeconds(1);
        ZonedDateTime d4 = d3.plusSeconds(1);
        ZonedDateTime d5 = d4.plusSeconds(1);
        ZonedDateTime d6 = d5.plusSeconds(1);
        ZonedDateTime d7 = d6.plusSeconds(1);
        ZonedDateTime d8 = d7.plusSeconds(1);
        ZonedDateTime d9 = d8.plusSeconds(1);


//        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))
//                .willReturn(aResponse().withBody("{\n" +
//                        "    \"LogEvents\": [\n" +
//                        "        {\n" +
//                        "            \"ClientTimestamp\": \"{{now}}\",\n" +
//                        "            \"Component\": \"\\\\VED\\\\Policy\\\\certificates\\\\_Discovered\\\\TrustNet\\\\defaultwebsite.lab.venafi.com - 83\",\n" +
//                        "            \"ComponentId\": 123185,\n" +
//                        "            \"ComponentSubsystem\": \"Config\",\n" +
//                        "            \"Data\": null,\n" +
//                        "            \"Grouping\": 0,\n" +
//                        "            \"Id\": 1835016,\n" +
//                        "            \"Name\": \"Certificate Revocation - CRL Failure\",\n" +
//                        "            \"ServerTimestamp\": \"{{now}}\",\n" +
//                        "            \"Severity\": \"Info\",\n" +
//                        "            \"SourceIP\": \"[::1]\",\n" +
//                        "            \"Text1\": \"CN=traininglab-Root-CA, DC=traininglab, DC=local\",\n" +
//                        "            \"Text2\": \"ldap:///CN=traininglab-Root-CA(1),CN=server1,CN=CDP,CN=Public%20Key%20Services,CN=Services,CN=Configuration,DC=traininglab,DC=local?certificateRevocationList?base?objectClass=cRLDistributionPoint\",\n" +
//                        "            \"Value1\": 0,\n" +
//                        "            \"Value2\": 0\n" +
//                        "        }\n" +
//                        "    ]\n" +
//                        "}").withTransformers("response-template")
//                ));
        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))//.withQueryParam(FROM_TIME, equalTo(d5.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(d1) + "," +
                        createLogEventBody(d2) + "," +
                        createLogEventBody(d3) + "," +
                        createLogEventBody(d4) + "," +
                        createLogEventBody(d5) +
                        "    ]\n" +
                        "}")
                ));
        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og")).withQueryParam(FROM_TIME, equalTo(d5.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .withQueryParam(OFFSET, equalTo(String.valueOf(1)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(d5) + "," +
                        createLogEventBody(d5) + "," +
                        createLogEventBody(d5) + "," +
                        createLogEventBody(d5) + "," +
                        createLogEventBody(d5) +
                        "    ]\n" +
                        "}")
                ));
        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og")).withQueryParam(FROM_TIME, equalTo(d5.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .withQueryParam(OFFSET, equalTo(String.valueOf(6)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(d5) + "," +
                        createLogEventBody(d6) + "," +
                        createLogEventBody(d6) + "," +
                        createLogEventBody(d7) +
                        "    ]\n" +
                        "}")
                ));

        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og")).withQueryParam(FROM_TIME, equalTo(d7.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .withQueryParam(OFFSET, equalTo(String.valueOf(1)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(d8) + "," +
                        createLogEventBody(d9) +
                        "    ]\n" +
                        "}")
                ));
    }

    private String createLogEventBody(ZonedDateTime dateTime) {
        return "        {\n" +
                "            \"ClientTimestamp\": \"" + dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "\",\n" +
                "            \"Component\": \"\\\\VED\\\\Policy\\\\certificates\\\\_Discovered\\\\TrustNet\\\\defaultwebsite.lab.venafi.com - 83\",\n" +
                "            \"ComponentId\": 123185,\n" +
                "            \"ComponentSubsystem\": \"Config\",\n" +
                "            \"Data\": null,\n" +
                "            \"Grouping\": 0,\n" +
                "            \"Id\": 1835016,\n" +
                "            \"Name\": \"Certificate Revocation - CRL Failure\",\n" +
                "            \"ServerTimestamp\": \"" + dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "\",\n" +
                "            \"Severity\": \"Info\",\n" +
                "            \"SourceIP\": \"[::1]\",\n" +
                "            \"Text1\": \"CN=traininglab-Root-CA, DC=traininglab, DC=local\",\n" +
                "            \"Text2\": \"ldap:///CN=traininglab-Root-CA(1),CN=server1,CN=CDP,CN=Public%20Key%20Services,CN=Services,CN=Configuration,DC=traininglab,DC=local?certificateRevocationList?base?objectClass=cRLDistributionPoint\",\n" +
                "            \"Value1\": 0,\n" +
                "            \"Value2\": 0\n" +
                "        }\n";
    }

}