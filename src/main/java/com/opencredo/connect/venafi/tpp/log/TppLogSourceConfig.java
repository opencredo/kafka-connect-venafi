package com.opencredo.connect.venafi.tpp.log;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.HashMap;
import java.util.Map;

public class TppLogSourceConfig extends AbstractConfig {
    public static final String BASE_URL_CONFIG = "venafi.base.url";
    private static final String BASE_URL_DEFAULT = "https://localhost:443/vedsdk";
    private static final String BASE_URL_DOC = "Url to TPP api with /VEDSDK";

    public static final String USERNAME_CONFIG = "venafi.username";
    private static final String USERNAME_DEFAULT = "placeholder_username";
    private static final String USERNAME_DOC = "The username to use with the /VEDSDK api.";

    public static final String PASSWORD_CONFIG = "venafi.password";
    private static final String PASSWORD_DEFAULT = "placeholder_password";
    private static final String PASSWORD_DOC = "The password to use with the /VEDSDK api.";

    public static final String TOPIC_CONFIG = "venafi.topic";
    private static final String TOPIC_DEFAULT = "TPP-LOGS";
    private static final String TOPIC_DOC = "TOPIC to publish TPP log data to.";

    public static final String BATCH_SIZE = "venafi.batch.size";
    private static final int BATCH_SIZE_DEFAULT = 100;
    private static final String BATCH_SIZE_DOC = "Window of data to pull from log api.";

    public static final String POLL_INTERVAL = "venafi.poll.interval";
    private static final int POLL_INTERVAL_DEFAULT = 1000;
    private static final String POLL_INTERVAL_DOC = "Poll interval in milliseconds.";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(BASE_URL_CONFIG, ConfigDef.Type.STRING, BASE_URL_DEFAULT, ConfigDef.Importance.HIGH, BASE_URL_DOC)
            .define(TOPIC_CONFIG, ConfigDef.Type.STRING, TOPIC_DEFAULT, ConfigDef.Importance.HIGH, TOPIC_DOC)
            .define(BATCH_SIZE, ConfigDef.Type.INT, BATCH_SIZE_DEFAULT, ConfigDef.Importance.LOW, BATCH_SIZE_DOC)
            .define(POLL_INTERVAL, ConfigDef.Type.INT, POLL_INTERVAL_DEFAULT, ConfigDef.Importance.LOW, POLL_INTERVAL_DOC)
            .define(USERNAME_CONFIG, ConfigDef.Type.STRING, USERNAME_DEFAULT, ConfigDef.Importance.HIGH, USERNAME_DOC)
            .define(PASSWORD_CONFIG, ConfigDef.Type.STRING, PASSWORD_DEFAULT, ConfigDef.Importance.HIGH, PASSWORD_DOC);

    public TppLogSourceConfig(Map<String, ?> props) {
        super(CONFIG_DEF, props);
    }

    private TppLogSourceConfig(ConfigDef definition, Map<?, ?> originals, boolean doLog) {
        super(definition, originals, doLog);
    }

    private TppLogSourceConfig(ConfigDef definition, Map<?, ?> originals) {
        super(definition, originals);
    }

    Map<String, String> returnPropertiesWithDefaultsValuesIfMissing() {
        Map<String, ?> uncastProperties = this.values();
        Map<String, String> config = new HashMap<>(uncastProperties.size());
        uncastProperties.forEach((key, valueToBeCast) -> config.put(key, valueToBeCast.toString()));

        return config;
    }
}
