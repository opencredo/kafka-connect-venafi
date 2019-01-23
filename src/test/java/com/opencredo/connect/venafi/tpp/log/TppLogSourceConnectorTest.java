package com.opencredo.connect.venafi.tpp.log;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.opencredo.connect.venafi.tpp.log.EventLogSourceTaskTest.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TppLogSourceConnectorTest {

    public static final int ONE_MAX_TASK = 1;
    public static final int FIRST_VALUE_IN_LIST = 0;
    private WireMockServer wireMockServer = new WireMockServer(
            new WireMockConfiguration().dynamicPort()
                    .extensions(new ResponseTemplateTransformer(false))
    );

    @BeforeEach
    private void setup() {
        wireMockServer.start();
    }

    @AfterEach
    private void shutdown() {
        wireMockServer.shutdown();
    }

    @Test
    void as_a_connector_I_should_be_able_to_start_up_with_empty_properties() throws IllegalAccessException, InstantiationException, InterruptedException {
        TppLogSourceConnector connector = given_a_connector();

        when_the_connector_is_started_with_no_properties(connector);
        SourceTask sourceTask = then_I_should_be_able_to_get_a_source_task_from_the_connector(connector);
        Map<String, String> taskProperties = then_I_can_get_the_task_properties(connector);

        when_the_task_is_started(sourceTask, taskProperties);
        List<SourceRecord> records = then_the_task_can_be_polled(sourceTask);

        assertEquals(0, records.size());
    }

    @Test
    void as_a_connector_I_should_be_able_to_start_up_with_some_properties() throws IllegalAccessException, InstantiationException, InterruptedException {
        given_the_mock_will_respond_to_auth();
        given_the_mock_will_respond_to_log();

        TppLogSourceConnector connector = given_a_connector();

        when_the_connector_is_started_with_no_properties(connector);
        SourceTask sourceTask = then_I_should_be_able_to_get_a_source_task_from_the_connector(connector);
        Map<String, String> taskProperties = then_I_can_get_the_task_properties(connector);

        when_the_task_is_started(sourceTask, taskProperties);
        List<SourceRecord> records = then_the_task_can_be_polled(sourceTask);
        assertEquals(0, records.size());
    }

    private List<SourceRecord> then_the_task_can_be_polled(SourceTask sourceTask) throws InterruptedException {
        return sourceTask.poll();
    }

    private void when_the_task_is_started(SourceTask sourceTask, Map<String, String> taskProperties) {
        sourceTask.start(taskProperties);
    }

    private Map<String, String> then_I_can_get_the_task_properties(TppLogSourceConnector connector) {
        return connector.taskConfigs(ONE_MAX_TASK).get(FIRST_VALUE_IN_LIST);
    }

    private SourceTask then_I_should_be_able_to_get_a_source_task_from_the_connector(TppLogSourceConnector connector) throws InstantiationException, IllegalAccessException {
        Task task = connector.taskClass().newInstance();
        assertTrue(task instanceof SourceTask);
        return (SourceTask) task;
    }

    private void when_the_connector_is_started_with_no_properties(TppLogSourceConnector connector) {
        when_the_connector_is_started_with_properties(connector, new HashMap<>());
    }

    private void when_the_connector_is_started_with_properties(TppLogSourceConnector connector,Map<String,String> props){
        connector.start(props);
    }
    private TppLogSourceConnector given_a_connector() {
        return new TppLogSourceConnector();
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

}