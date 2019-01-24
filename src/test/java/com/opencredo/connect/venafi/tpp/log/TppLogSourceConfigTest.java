package com.opencredo.connect.venafi.tpp.log;

import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.opencredo.connect.venafi.tpp.log.TppLogSourceConfig.*;
import static org.junit.jupiter.api.Assertions.*;

class TppLogSourceConfigTest {

    public static final String EMPTY_STRING = "";
    public static final int TOO_LARGE_BATCH_SIZE = MAX_BATCH_SIZE + 1;
    public static final int TOO_SMALL_BATCH_SIZE = MIN_BATCH_SIZE - 1;

    @Test
    void as_a_Config_I_should_be_able_to_generate_a_guide() {
        assertNotNull(CONFIG_DEF.toEnrichedRst());
        assertNotEquals("", CONFIG_DEF.toEnrichedRst().trim());
    }

    @Test
    void as_a_config_I_should_throw_a_config_exception_if_nothing_is_set() {
        Map<String, Object> props = Collections.emptyMap();
        Executable executingConfig = given_an_config_with(props);
        then_I_expect_a_config_exception_when_I_run_this(executingConfig);
    }

    @Test
    void as_a_config_I_should_throw_a_config_exception_if_minimum_properties_are_set() {
        Map<String, Object> props = given_a_minimum_list_of_properties();
        Executable executingConfig = given_an_config_with(props);
        then_I_expect_no_config_exception_when_run_this(executingConfig);
    }

    private Map<String, Object> given_a_minimum_list_of_properties() {
        Map<String, Object> props = new HashMap<>();
        props.put(BASE_URL_CONFIG, "https://localhost:443");
        props.put(USERNAME_CONFIG, "placeholder_username");
        props.put(PASSWORD_CONFIG, "placeholder_password");
        return props;
    }

    @Test
    void as_a_config_I_should_throw_a_config_exception_if_base_URL_is_not_set() {
        Map<String, Object> props = given_a_minimum_list_of_properties();
        given_this_is_removed_from(BASE_URL_CONFIG, props);
        Executable executingConfig = given_an_config_with(props);
        ConfigException exception = then_I_expect_a_config_exception_when_I_run_this(executingConfig);
        assertEquals(noDefaultValueFor(BASE_URL_CONFIG), exception.getMessage());
    }

    @Test
    void as_a_config_I_should_throw_a_config_exception_if_password_is_not_set() {
        Map<String, Object> props = given_a_minimum_list_of_properties();
        given_this_is_removed_from(PASSWORD_CONFIG, props);
        Executable executingConfig = given_an_config_with(props);
        ConfigException exception = then_I_expect_a_config_exception_when_I_run_this(executingConfig);
        assertEquals(noDefaultValueFor(PASSWORD_CONFIG), exception.getMessage());
    }

    @Test
    void as_a_config_I_should_throw_a_config_exception_if_username_is_not_set() {
        Map<String, Object> props = given_a_minimum_list_of_properties();
        given_this_is_removed_from(USERNAME_CONFIG, props);
        Executable executingConfig = given_an_config_with(props);
        ConfigException exception = then_I_expect_a_config_exception_when_I_run_this(executingConfig);
        assertEquals(noDefaultValueFor(USERNAME_CONFIG), exception.getMessage());
    }


    @Test
    void as_a_config_I_should_throw_a_config_exception_if_base_URL_is_invalid() {
        Map<String, Object> props = given_a_minimum_list_of_properties();
        given_this_is_overriden_with(BASE_URL_CONFIG, EMPTY_STRING, props);
        Executable executingConfig = given_an_config_with(props);
        ConfigException exception = then_I_expect_a_config_exception_when_I_run_this(executingConfig);
        assertEquals(invalidConfigForNonEmptyString(BASE_URL_CONFIG, EMPTY_STRING), exception.getMessage());
    }

    @Test
    void as_a_config_I_should_throw_a_config_exception_if_topic_is_invalid() {
        Map<String, Object> props = given_a_minimum_list_of_properties();
        given_this_is_overriden_with(TOPIC_CONFIG, EMPTY_STRING, props);
        Executable executingConfig = given_an_config_with(props);
        ConfigException exception = then_I_expect_a_config_exception_when_I_run_this(executingConfig);
        assertEquals(invalidConfigForNonEmptyString(TOPIC_CONFIG, EMPTY_STRING), exception.getMessage());
    }

    @Test
    void as_a_config_I_should_throw_a_config_exception_if_batch_size_is_too_small() {
        Map<String, Object> props = given_a_minimum_list_of_properties();
        given_this_is_overriden_with(BATCH_SIZE, TOO_SMALL_BATCH_SIZE, props);
        Executable executingConfig = given_an_config_with(props);
        ConfigException exception = then_I_expect_a_config_exception_when_I_run_this(executingConfig);
        assertEquals(invalidConfigForTooSmallValue(BATCH_SIZE, TOO_SMALL_BATCH_SIZE, MIN_BATCH_SIZE), exception.getMessage());
    }

    @Test
    void as_a_config_I_should_throw_a_config_exception_if_batch_size_is_too_big() {
        Map<String, Object> props = given_a_minimum_list_of_properties();
        given_this_is_overriden_with(BATCH_SIZE, TOO_LARGE_BATCH_SIZE, props);
        Executable executingConfig = given_an_config_with(props);
        ConfigException exception = then_I_expect_a_config_exception_when_I_run_this(executingConfig);
        assertEquals(invalidConfigForTooLargeValue(BATCH_SIZE, TOO_LARGE_BATCH_SIZE, MAX_BATCH_SIZE), exception.getMessage());
    }

    private String noDefaultValueFor(String key) {
        return "Missing required configuration \"" + key + "\" which has no default value.";
    }

    private String invalidConfigForTooSmallValue(String key, int value, int min) {
        return invalidValueForConfig(key, Integer.toString(value)) + ": Value must be at least " + min;
    }

    private String invalidConfigForTooLargeValue(String key, int value, int max) {
        return invalidValueForConfig(key, Integer.toString(value)) + ": Value must be no more than " + max;
    }

    private String invalidConfigForNonEmptyString(String key, String value) {
        return invalidValueForConfig(key, value) + ": String may not be empty";
    }

    private String invalidValueForConfig(String key, String value) {
        return "Invalid value " + value + " for configuration " + key;
    }

    private void given_this_is_removed_from(String key, Map<String, Object> props) {
        props.remove(key);
    }

    private void given_this_is_overriden_with(String key, Object value, Map<String, Object> props) {
        props.put(key, value);
    }

    private ConfigException then_I_expect_a_config_exception_when_I_run_this(Executable executingConfig) {
        return assertThrows(ConfigException.class, executingConfig);

    }


    private void then_I_expect_no_config_exception_when_run_this(Executable executingConfig) {
        assertDoesNotThrow(executingConfig);

    }

    private Executable given_an_config_with(Map<String, Object> props) {
        return () -> new TppLogSourceConfig(props);
    }
}
