package com.opencredo.connect.venafi.tpp.log;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TppLogSourceConnector extends SourceConnector {
    public static final String BASE_URL_CONFIG = "BASE_URL";
    public static final String TOPIC_CONFIG = "TOPIC";
    public static final String BATCH_SIZE = "BATCH_SIZE";
    public static final String POLL_INTERVAL = "POLL_INTERVAL";
    private static final Logger log = LoggerFactory.getLogger(TppLogSourceConnector.class);
    private static ConfigDef CONFIG_DEF = new ConfigDef()
            .define(BASE_URL_CONFIG, ConfigDef.Type.STRING, "https://the-vedsdk-URL-that-have.com:443/vedsdk", ConfigDef.Importance.HIGH, "Url to TPP box with /VEDSDK")
            .define(TOPIC_CONFIG, ConfigDef.Type.STRING, "TPP-LOGS", ConfigDef.Importance.HIGH, "TOPIC to publish data to.")
            .define(BATCH_SIZE, ConfigDef.Type.INT, 10, ConfigDef.Importance.LOW, "Window of data to pull from log api.")
            .define(POLL_INTERVAL, ConfigDef.Type.INT, 1000, ConfigDef.Importance.LOW, "Poll interval in milliseconds.");
    private String baseUrl;
    private String topic;
    private String batchSize;
    private String pollInterval;

    @Override
    public void start(Map<String, String> props) {
        log.info("Started up connector");
        baseUrl = props.get(BASE_URL_CONFIG);
        topic = props.get(TOPIC_CONFIG);
        batchSize = props.get(BATCH_SIZE);
        pollInterval = props.get(POLL_INTERVAL);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return TppLogSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();
        Map<String, String> config = new HashMap<>();
        config.put(BASE_URL_CONFIG, baseUrl);
        config.put(TOPIC_CONFIG, topic);
        config.put(BATCH_SIZE, batchSize);
        config.put(POLL_INTERVAL, pollInterval);
        configs.add(config);
        return configs;
    }

    @Override
    public void stop() {

    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public String version() {
        return "1.0";
    }
}
