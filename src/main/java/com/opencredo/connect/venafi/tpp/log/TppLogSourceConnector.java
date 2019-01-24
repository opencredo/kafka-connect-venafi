package com.opencredo.connect.venafi.tpp.log;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.opencredo.connect.venafi.tpp.log.TppLogSourceConfig.CONFIG_DEF;


public class TppLogSourceConnector extends SourceConnector {
    private static final Logger log = LoggerFactory.getLogger(TppLogSourceConnector.class);
    private Map<String, String> configProperties;

    @Override
    public void start(Map<String, String> props) {

        log.info("Starting up TPP Log Source connector");
        try {
            configProperties = setupSourcePropertiesWithDefaultsIfMissing(props);
        } catch (ConfigException e) {
            throw new ConnectException("Couldn't start TppLogSourceConnector due to configuration error", e);
        }
    }

    private Map<String, String> setupSourcePropertiesWithDefaultsIfMissing(Map<String, String> props) throws ConfigException {
        return new TppLogSourceConfig(props).returnPropertiesWithDefaultsValuesIfMissing();
    }

    @Override
    public Class<? extends Task> taskClass() {
        return TppLogSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        if (maxTasks != 1) {
            log.info("Ignoring maxTasks as there is can only be one.");
        }
        List<Map<String, String>> configs = new ArrayList<>(maxTasks);
        Map<String, String> taskConfig = new HashMap<>();
        taskConfig.putAll(configProperties);
        configs.add(taskConfig);
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
        return VersionUtil.getVersion();
    }
}
