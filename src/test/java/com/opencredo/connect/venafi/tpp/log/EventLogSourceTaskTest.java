package com.opencredo.connect.venafi.tpp.log;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.opencredo.connect.venafi.tpp.log.TppLogSourceConnector.*;
import static com.opencredo.connect.venafi.tpp.log.TppLogSourceTask.LAST_API_OFFSET;
import static com.opencredo.connect.venafi.tpp.log.TppLogSourceTask.LAST_READ;
import static com.opencredo.connect.venafi.tpp.log.api.TppLog.FROM_TIME;
import static com.opencredo.connect.venafi.tpp.log.api.TppLog.OFFSET;
import static com.opencredo.connect.venafi.tpp.log.model.EventLog.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EventLogSourceTaskTest {

    private final ZonedDateTime TODAY = ZonedDateTime.now();

    ZonedDateTime getTodayPlus(int seconds) {
        return TODAY.plusSeconds(seconds);
    }

    String getStringOfTodayPlus(int seconds) {
        return getTodayPlus(seconds).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }


    private WireMockServer wireMockServer = new WireMockServer(
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
    public void as_a_task_I_want_a_valid_context() {
        SourceTaskContext mockSourceTaskContext = given_a_mock_source_context_with(getTodayPlus(2), 1);

        given_the_mock_will_respond_to_auth();
        given_the_mock_will_respond_to_log_for_offsetsStorage();

        TppLogSourceTask task = given_a_task_is_setup_with(mockSourceTaskContext);


        List<SourceRecord> sourceRecords1 = when_the_task_is_polled(task);
        then_the_logs_are_of_size(sourceRecords1, 3);

        List<SourceRecord> sourceRecords2 = when_the_task_is_polled(task);
        then_the_logs_are_of_size(sourceRecords2, 1);
    }

    @Test
    public void as_a_client_I_want_some_logs() {


        given_the_mock_will_respond_to_auth();
        given_the_mock_will_respond_to_log();
        TppLogSourceTask task = given_a_task_is_setup();

        List<SourceRecord> logs = when_the_task_is_polled(task);
        then_the_logs_are_of_size(logs, 2);
    }

    @Test
    public void as_a_client_I_want_to_paginate_logs() {


        given_the_mock_will_respond_to_auth();
        given_the_mock_will_respond_to_log_for_windowing();
        TppLogSourceTask task = given_a_task_is_setup();

        List<SourceRecord> page_1_of_logs = when_the_task_is_polled(task);
        then_the_logs_are_of_size(page_1_of_logs, 5);
        //get next page
        List<SourceRecord> page_2_of_logs = when_the_task_is_polled(task);
        then_the_logs_are_of_size(page_2_of_logs, 5);
        then_the_number_of_logs_with_timestamp_is(5, page_2_of_logs, getTodayPlus(5));
        //get next Page
        List<SourceRecord> page_3_of_logs = when_the_task_is_polled(task);
        then_the_logs_are_of_size(page_3_of_logs, 4);
        then_the_number_of_logs_with_timestamp_is(2, page_3_of_logs, getTodayPlus(6));

        List<SourceRecord> page_4_of_logs = when_the_task_is_polled(task);
        then_the_logs_are_of_size(page_4_of_logs, 2);
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


    private List<SourceRecord> when_the_task_is_polled(TppLogSourceTask task) {
        return task.poll();
    }

    private SourceTaskContext given_a_mock_source_context_with(ZonedDateTime lastReadDate, Integer lastApiOffset) {
        SourceTaskContext mockSourceTaskContext = Mockito.mock(SourceTaskContext.class);
        OffsetStorageReader mockOffsetStorageReader = Mockito.mock(OffsetStorageReader.class);
        Map<String, Object> config = new HashMap<>(2);
        config.put(LAST_READ, lastReadDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        config.put(LAST_API_OFFSET, lastApiOffset);
        Mockito.when(mockOffsetStorageReader.offset(Mockito.anyMap())).thenReturn(config);
        Mockito.when(mockSourceTaskContext.offsetStorageReader()).thenReturn(mockOffsetStorageReader);
        return mockSourceTaskContext;
    }

    private void then_the_number_of_logs_with_timestamp_is(int count, List<SourceRecord> page_of_logs, ZonedDateTime todayPlus) {
        assertEquals(count, getCountOfLogsWithLastRead(todayPlus, page_of_logs));
    }

    private void then_the_logs_are_of_size(List<SourceRecord> page_3_of_logs, int i) {
        assertNotNull(page_3_of_logs);
        assertEquals(i, page_3_of_logs.size());
    }

    private long getCountOfLogsWithLastRead(ZonedDateTime date, List<SourceRecord> page_2_of_logs) {
        return page_2_of_logs.stream().filter(sourceRecord -> date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).equals(sourceRecord.sourceOffset().get(LAST_READ))).count();
    }

    private String when_a_token_is_got(TppLogSourceTask task) {
        return task.getToken();
    }

    private TppLogSourceTask given_a_task_is_setup_with(SourceTaskContext context) {
        TppLogSourceTask task = new TppLogSourceTask();
        Map<String, String> config = getTaskConfig();
        if (context != null) {
            task.initialize(context);
        }
        task.start(config);
        return task;
    }

    private TppLogSourceTask given_a_task_is_setup() {
        return given_a_task_is_setup_with(null);
    }

    private Map<String, String> getTaskConfig() {
        Map<String, String> config = new HashMap<>();
        config.put(BASE_URL_CONFIG, wireMockServer.baseUrl());
        config.put(TOPIC_CONFIG, "temp");
        config.put(BATCH_SIZE, "1000");
        config.put(POLL_INTERVAL, "0");
        return config;
    }

    private void given_the_mock_will_respond_to_auth() {
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

    private void given_the_mock_will_respond_to_log() {


        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))//.withQueryParam(FROM_TIME, equalTo(TODAY_PLUS_SIX.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(getTodayPlus(1)) + "," +
                        createLogEventBody(getTodayPlus(2)) +
                        "    ]\n" +
                        "}")
                ));

    }

    private void given_the_mock_will_respond_to_log_for_windowing() {


        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))//.withQueryParam(FROM_TIME, equalTo(TODAY_PLUS_SIX.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(getTodayPlus(1)) + "," +
                        createLogEventBody(getTodayPlus(2)) + "," +
                        createLogEventBody(getTodayPlus(3)) + "," +
                        createLogEventBody(getTodayPlus(4)) + "," +
                        createLogEventBody(getTodayPlus(5)) +
                        "    ]\n" +
                        "}")
                ));
        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))
                .withQueryParam(FROM_TIME, equalTo(getStringOfTodayPlus(5)))
                .withQueryParam(OFFSET, equalTo(String.valueOf(1)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(getTodayPlus(5)) + "," +
                        createLogEventBody(getTodayPlus(5)) + "," +
                        createLogEventBody(getTodayPlus(5)) + "," +
                        createLogEventBody(getTodayPlus(5)) + "," +
                        createLogEventBody(getTodayPlus(5)) +
                        "    ]\n" +
                        "}")
                ));
        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))
                .withQueryParam(FROM_TIME, equalTo(getStringOfTodayPlus(5)))
                .withQueryParam(OFFSET, equalTo(String.valueOf(6)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(getTodayPlus(5)) + "," +
                        createLogEventBody(getTodayPlus(6)) + "," +
                        createLogEventBody(getTodayPlus(6)) + "," +
                        createLogEventBody(getTodayPlus(7)) +
                        "    ]\n" +
                        "}")
                ));
        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))
                .withQueryParam(FROM_TIME, equalTo(getStringOfTodayPlus(7)))
                .withQueryParam(OFFSET, equalTo(String.valueOf(1)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(getTodayPlus(8)) + "," +
                        createLogEventBody(getTodayPlus(9)) +
                        "    ]\n" +
                        "}")
                ));


    }

    private void given_the_mock_will_respond_to_log_for_offsetsStorage() {

        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))
                .withQueryParam(FROM_TIME, equalTo(getStringOfTodayPlus(2)))
                .withQueryParam(OFFSET, equalTo(String.valueOf(1)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(getTodayPlus(8)) + "," +
                        createLogEventBody(getTodayPlus(9)) + "," +
                        createLogEventBody(getTodayPlus(9)) +
                        "    ]\n" +
                        "}")
                ));
        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))
                .withQueryParam(FROM_TIME, equalTo(getStringOfTodayPlus(9)))
                .withQueryParam(OFFSET, equalTo(String.valueOf(2)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(getTodayPlus(9)) +
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