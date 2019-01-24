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
import static com.opencredo.connect.venafi.tpp.log.TppLogSourceConfig.*;
import static com.opencredo.connect.venafi.tpp.log.TppLogSourceTask.*;
import static com.opencredo.connect.venafi.tpp.log.api.TppLog.FROM_TIME;
import static com.opencredo.connect.venafi.tpp.log.api.TppLog.OFFSET;
import static com.opencredo.connect.venafi.tpp.log.model.EventLog.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EventLogSourceTaskTest {

    public static final String LOG_API_REGEX_PATH = "/[Ll]og/?";
    public static final String AUTHORIZE_API_REGEX_PATH = "/[Aa]uthorize/?";
    private static final ZonedDateTime TODAY = ZonedDateTime.now();
    private WireMockServer wireMockServer = new WireMockServer(
            new WireMockConfiguration().dynamicPort()
                    .extensions(new ResponseTemplateTransformer(false))
    );

    static ZonedDateTime getTodayPlus(int seconds) {
        return TODAY.plusSeconds(seconds);
    }

    static String createLogEventBody(ZonedDateTime dateTime) {
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

    String getStringOfTodayPlus(int seconds) {
        return getTodayPlus(seconds).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @BeforeEach
    private void setup() {
        wireMockServer.start();
    }

    @AfterEach
    private void shutdown() {
        wireMockServer.shutdown();
    }

    @Test
    public void as_a_task_I_should_return_a_version() {
        TppLogSourceTask task = given_a_task_is_setup();
        assertEquals("1.0.0", task.version());
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
    public void as_a_client_I_want_an_empty_token_if_I_get_an_exception_from_auth() {

        given_the_mock_will_respond_to_auth_bad_request();
        TppLogSourceTask task = given_a_task_is_setup();

        String token = when_a_token_is_got(task);
        assertNotNull(token);
        assertEquals("", token);
    }

    @Test
    public void as_a_client_I_want_to_not_make_a_logs_call_if_I_get_an_exception_from_auth() {

        given_the_mock_will_respond_to_auth_bad_request();
        TppLogSourceTask task = given_a_task_is_setup();

        List<SourceRecord> logs = when_the_task_is_polled(task);
        then_the_logs_are_of_size(logs, 0);
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(AUTHORIZE_API_REGEX_PATH)));
        wireMockServer.verify(0, getRequestedFor(urlPathMatching(LOG_API_REGEX_PATH)));
    }

    @Test
    public void as_a_client_I_want_a_token_to_only_generate_once_while_before_token_expiry() {


        given_the_mock_will_respond_to_auth();
        TppLogSourceTask task = given_a_task_is_setup();

        String token = when_a_token_is_got(task);
        assertTrue(isNotNullOrBlank(token));
        String token2 = when_a_token_is_got(task);
        assertTrue(isNotNullOrBlank(token2));
        assertEquals(token, token2);
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(AUTHORIZE_API_REGEX_PATH)));
    }

    @Test
    public void as_a_client_I_want_a_token_to_only_regenerate_while_away_token_expiry() {
        given_the_mock_will_respond_to_auth_with_expired_token();
        TppLogSourceTask task = given_a_task_is_setup();

        String invalidToken = when_a_token_is_got(task);
        assertTrue(isNotNullOrBlank(invalidToken));
        String invalidToken2 = when_a_token_is_got(task);
        assertTrue(isNotNullOrBlank(invalidToken2));
        wireMockServer.verify(2, postRequestedFor(urlPathMatching(AUTHORIZE_API_REGEX_PATH)));
        assertNotEquals(invalidToken, invalidToken2);

    }

    @Test
    public void as_a_task_I_want_a_valid_context() {
        SourceTaskContext mockSourceTaskContext = given_a_mock_source_context_with(getTodayPlus(2), 1L);

        given_the_mock_will_respond_to_auth();
        given_the_mock_will_respond_to_log_for_offsetsStorage();

        TppLogSourceTask task = given_a_task_is_setup_with(mockSourceTaskContext);


        List<SourceRecord> sourceRecords1 = when_the_task_is_polled(task);
        then_the_logs_are_of_size(sourceRecords1, 3);

        List<SourceRecord> sourceRecords2 = when_the_task_is_polled(task);
        then_the_logs_are_of_size(sourceRecords2, 1);
    }

    @Test
    public void as_a_task_I_want_to_handle_an_empty_context() {
        SourceTaskContext mockSourceTaskContext = given_a_mock_source_context_with(Collections.emptyMap());

        given_the_mock_will_respond_to_auth();
        given_the_mock_will_respond_to_log_for_empty_offsetsStorage();

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
    public void as_a_client_I_want_some_logs_and_handle_token_expiry() {

        given_the_mock_will_respond_to_auth();
//        given_the_mock_will_respond_to_log();
        given_the_mock_will_respond_to_log_as_expired_token();
        TppLogSourceTask task = given_a_task_is_setup();

        List<SourceRecord> logs = when_the_task_is_polled(task);
        then_the_logs_are_of_size(logs, 0);
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

    private SourceTaskContext given_a_mock_source_context_with(ZonedDateTime lastReadDate, Long lastApiOffset) {
        Map<String, Object> config = new HashMap<>(2);
        config.put(LAST_READ, lastReadDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        config.put(LAST_API_OFFSET, lastApiOffset);

        return given_a_mock_source_context_with(config);
    }

    private SourceTaskContext given_a_mock_source_context_with(Map<String, Object> config) {
        SourceTaskContext mockSourceTaskContext = Mockito.mock(SourceTaskContext.class);
        OffsetStorageReader mockOffsetStorageReader = Mockito.mock(OffsetStorageReader.class);
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
        config.put(POLL_INTERVAL, "0");
        config.put(USERNAME_CONFIG, "placeholder_username");
        config.put(PASSWORD_CONFIG, "placeholder_password");
        return new TppLogSourceConfig(config).returnPropertiesWithDefaultsValuesIfMissing();
    }

    private void given_the_mock_will_respond_to_auth() {
        wireMockServer.stubFor(post(urlPathMatching(AUTHORIZE_API_REGEX_PATH))
                .withRequestBody(equalToJson("{\n" +
                        "\t\"Username\":\"placeholder_username\",\n" +
                        "\t\"Password\":\"placeholder_password\"\n" +
                        "}")).withHeader("Content-Type", containing("application/json"))
                .willReturn(okJson("{\n" +
                        "    \"APIKey\": \"{{randomValue type='UUID'}}\",\n" +
                        "    \"ValidUntil\": \"/Date(" + LocalDateTime.now().plusMinutes(3).toEpochSecond(ZoneOffset.UTC) + "000)/\"\n" +
                        "}").withTransformers("response-template")
                ));
    }

    private void given_the_mock_will_respond_to_auth_with_expired_token() {
        wireMockServer.stubFor(post(urlPathMatching(AUTHORIZE_API_REGEX_PATH))
                .withRequestBody(equalToJson("{\n" +
                        "\t\"Username\":\"placeholder_username\",\n" +
                        "\t\"Password\":\"placeholder_password\"\n" +
                        "}")).withHeader("Content-Type", containing("application/json"))
                .willReturn(okJson("{\n" +
                        "    \"APIKey\": \"{{randomValue type='UUID'}}\",\n" +
                        "    \"ValidUntil\": \"/Date(" + LocalDateTime.now().minusMinutes(3).toEpochSecond(ZoneOffset.UTC) + "000)/\"\n" +
                        "}").withTransformers("response-template")
                ));
    }

    private void given_the_mock_will_respond_to_auth_bad_request() {
        wireMockServer.stubFor(post(urlPathMatching(AUTHORIZE_API_REGEX_PATH))
                .withRequestBody(equalToJson("{\n" +
                        "\t\"Username\":\"placeholder_username\",\n" +
                        "\t\"Password\":\"placeholder_password\"\n" +
                        "}")).withHeader("Content-Type", containing("application/json"))
                .willReturn(badRequest()
                ));
    }

    private void given_the_mock_will_respond_to_log() {
        wireMockServer.stubFor(get(urlPathMatching(LOG_API_REGEX_PATH))
                .willReturn(okJson("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(getTodayPlus(1)) + "," +
                        createLogEventBody(getTodayPlus(2)) +
                        "    ]\n" +
                        "}")
                ));
    }

    private void given_the_mock_will_respond_to_log_as_expired_token() {
        wireMockServer.stubFor(get(urlPathMatching(LOG_API_REGEX_PATH))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody("{\n" +
                                "    \"Error\": \"API key '5dfb7fa4-d300-44b5-b192-df8d6e8303df' is not valid. Try /authorize or /authorize/integrated\"\n" +
                                "}"))
        );
    }

    private void given_the_mock_will_respond_to_log_for_windowing() {


        wireMockServer.stubFor(get(urlPathMatching(LOG_API_REGEX_PATH))
                .willReturn(okJson("{\n" +
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
                .willReturn(okJson("{\n" +
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
                .willReturn(okJson("{\n" +
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
                .willReturn(okJson("{\n" +
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

    private void given_the_mock_will_respond_to_log_for_empty_offsetsStorage() {

        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))
                .withQueryParam(FROM_TIME, equalTo(DEFAULT_FROM_TIME))
                .withQueryParam(OFFSET, equalTo(String.valueOf(0)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(getTodayPlus(8)) + "," +
                        createLogEventBody(getTodayPlus(8)) + "," +
                        createLogEventBody(getTodayPlus(100)) +
                        "    ]\n" +
                        "}")
                ));
        wireMockServer.stubFor(get(urlPathMatching("/[Ll]og"))
                .withQueryParam(FROM_TIME, equalTo(getStringOfTodayPlus(100)))
                .withQueryParam(OFFSET, equalTo(String.valueOf(1)))
                .willReturn(aResponse().withBody("{\n" +
                        "    \"LogEvents\": [\n" +
                        createLogEventBody(getTodayPlus(100)) +
                        "    ]\n" +
                        "}")
                ));
    }

}