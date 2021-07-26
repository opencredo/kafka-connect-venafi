package com.opencredo.connect.venafi.tpp.log.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegrationTest {

    public static final String LOG_API_REGEX_PATH = "/vedsdk/[Ll]og/?";
    public static final String AUTHORIZE_API_REGEX_PATH = "/vedauth/[Aa]uthorize/?";
    public static final String AUTHORIZE_REFRESH_API_REGEX_PATH = "/vedauth/[Aa]uthorize/token?";

    private static final String DEFAULT_TOPIC = "VENAFI-LOGS";

    private final WireMockServer wireMockServer = new WireMockServer(
            new WireMockConfiguration().port(8090)
                    .extensions(new ResponseTemplateTransformer(false), new MockEventLogResponseTransformer(false))
    );

    @ClassRule
    public static final DockerComposeContainer<?> compose =
            new DockerComposeContainer<>(
                    new File("integration/docker-compose.yml")).withLocalCompose(true)
                    .withExposedService("kafka", 9092);

    @BeforeEach
    private void setup() {
        wireMockServer.start();
        compose.start();
    }

    @AfterEach
    private void shutdown() {
        wireMockServer.shutdown();
        compose.stop();
    }

    @Test
    void ensureAMessagePlacedOnTheStubEndpointIsReceivedByKafka() {
        given_the_mock_will_respond_to_auth();
        and_given_the_mock_will_respond_to_auth_refresh();
        then_the_tasks_can_be_polled();

        Consumer<String,String> consumer = createConsumer("localhost:9092");

        List<String> receivedLogs = new ArrayList<>();
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String,String> record : records) {
                receivedLogs.add(record.value());
            }

            if (receivedLogs.size() == 3200) {
                break;
            }

            consumer.commitAsync();
        }

        assertEquals(3200, receivedLogs.size());
    }

    private void given_the_mock_will_respond_to_auth() {
        wireMockServer.stubFor(post(urlPathMatching(AUTHORIZE_API_REGEX_PATH))
                .withRequestBody(equalToJson("{\n" +
                        "\t\"username\":\"tppadmin\",\n" +
                        "\t\"password\":\"Password123!\",\n" +
                        "\t\"client_id\":\"kafka-connect-logs-test\",\n" +
                        "\t\"scope\":\"any\"\n" +
                        "}")).withHeader("Content-Type", containing("application/json"))
                .willReturn(okJson("{\n" +
                        "    \"token\": \"{{randomValue length=15 type='ALPHANUMERIC'}}\",\n" +
                        "    \"expires\": \"/Date(" + LocalDateTime.now().plusMinutes(3).toEpochSecond(ZoneOffset.UTC) + "000)/\",\n" +
                        "    \"refresh_token\": \"{{randomValue length=15 type='ALPHANUMERIC'}}\",\n" +
                        "    \"refresh_until\": \"/Date(" + LocalDateTime.now().plusMinutes(6).toEpochSecond(ZoneOffset.UTC) + "000)/\"\n" +
                        "}").withTransformers("response-template")
                ));
    }

    private void and_given_the_mock_will_respond_to_auth_refresh() {
        wireMockServer.stubFor(post(urlPathMatching(AUTHORIZE_REFRESH_API_REGEX_PATH))
                .withRequestBody(equalToJson("{\n" +
                        "\t'refresh_token':'${json-unit.any-string}',\n" +
                        "\t'client_id':'venafi-kafka-connect-logs-test'\n" +
                        "}")).withHeader("Content-Type", containing("application/json"))
                .willReturn(okJson("{\n" +
                        "    \"token\": \"{{randomValue length=24 type='ALPHANUMERIC'}}\",\n" +
                        "    \"expires\": \"/Date(" + LocalDateTime.now().plusMinutes(3).toEpochSecond(ZoneOffset.UTC) + "000)/\",\n" +
                        "    \"refresh_token\": \"{{randomValue length=24 type='ALPHANUMERIC'}}\",\n" +
                        "    \"refresh_until\": \"/Date(" + LocalDateTime.now().plusMinutes(6).toEpochSecond(ZoneOffset.UTC) + "000)/\"\n" +
                        "}").withTransformers("response-template")
                ));
    }

    private void then_the_tasks_can_be_polled() {
        wireMockServer.stubFor(get(urlPathMatching(LOG_API_REGEX_PATH))
                .willReturn(aResponse()
                        .withTransformers("event-log-response-transformer")
                ));
    }

    private static Consumer<String, String> createConsumer(String bootstrapServer) {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, DEFAULT_TOPIC+"-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create the consumer using props.
        final Consumer<String, String> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList(DEFAULT_TOPIC));
        return consumer;
    }
}
