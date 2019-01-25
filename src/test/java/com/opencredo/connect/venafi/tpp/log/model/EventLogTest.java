package com.opencredo.connect.venafi.tpp.log.model;


import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

class EventLogTest {

    @Test
    void as_a_SourceRecord_I_want_to_convert_a_basic_EventLog_to_a_Struct() {
        EventLog eventLog = given_a_minimal_EventLog_with_only_required_fields_set();
        Struct convertedEventLog = when_the_EventLog_is_converted_to_a_struct(eventLog);
        then_the_struct_contains_all_required_fields(convertedEventLog);
    }

    @Test
    void as_a_SourceRecord_I_want_to_convert_a_complete_EventLog_to_a_Struct() {
        EventLog completeEventLog = given_a_complete_EventLog_with_all_fields_set();
        Struct convertedEventLog = when_the_EventLog_is_converted_to_a_struct(completeEventLog);
        then_all_fields_were_converted(convertedEventLog);
    }

    // Expected Values
    // required fields
    private static final String TEXT_1_FIELD = "Text1";
    private static final String EXPECTED_COMPONENT = "component";
    private static final Long EXPECTED_ID = 1L;
    private static final String EXPECTED_NAME = "name";
    private static final Integer EXPECTED_GROUPING = 1;
    private static final ZonedDateTime EXPECTED_CLIENT_TIMESTAMP = ZonedDateTime.now();
    private static final ZonedDateTime EXPECTED_SERVER_TIMESTAMP = ZonedDateTime.now();
    private static final String EXPECTED_SEVERITY = "severity";
    private static final String EXPECTED_SOURCE_IP = "127.0.0.1";
    // optional fields
    private static final Integer EXPECTED_COMPONENT_ID = 1;
    private static final String EXPECTED_COMPONENT_SUBSYSTEM = "component subsystem";
    private static final String EXPECTED_EVENT_ID = "1";
    private static final String EXPECTED_DATA = "data";
    private static final String EXPECTED_TEXT_1 = "text 1";
    private static final String EXPECTED_TEXT_2 = "text 2";
    private static final Integer EXPECTED_VALUE_1 = 1;
    private static final Integer EXPECTED_VALUE_2 = 2;

    // Field names
    // required fields
    private static final String ID_FIELD = "Id";
    private static final String NAME_FIELD = "Name";
    private static final String GROUPING_FIELD = "Grouping";
    private static final String CLIENT_TIMESTAMP_FIELD = "ClientTimestamp";
    private static final String SERVER_TIMESTAMP_FIELD = "ServerTimestamp";
    private static final String SEVERITY_FIELD = "Severity";
    private static final String SOURCE_IP_FIELD = "SourceIP";
    // optional fields
    private static final String COMPONENT_FIELD = "Component";
    private static final String COMPONENT_ID_FIELD = "ComponentId";
    private static final String COMPONENT_SUBSYSTEM_FIELD = "ComponentSubsystem";
    private static final String EVENT_ID_FIELD = "EventId";
    private static final String DATA_FIELD = "Data";
    private static final String TEXT_2_FIELD = "Text2";
    private static final String VALUE_1_FIELD = "Value1";
    private static final String VALUE_2_FIELD = "Value2";

    // GIVEN
    private EventLog given_a_minimal_EventLog_with_only_required_fields_set() {
        return given_an_EventLogBuilder_with_all_required_fields_set()
                .build();
    }

    private EventLogBuilder given_an_EventLogBuilder_with_all_required_fields_set() {
        return new EventLogBuilder()
                .id(EXPECTED_ID)
                .name(EXPECTED_NAME)
                .grouping(EXPECTED_GROUPING)
                .clientTimestamp(EXPECTED_CLIENT_TIMESTAMP)
                .serverTimestamp(EXPECTED_SERVER_TIMESTAMP)
                .severity(EXPECTED_SEVERITY)
                .sourceIP(EXPECTED_SOURCE_IP);
    }

    private EventLog given_a_complete_EventLog_with_all_fields_set() {
        return given_an_EventLogBuilder_with_all_required_fields_set()
                .component(EXPECTED_COMPONENT)
                .componentId(EXPECTED_COMPONENT_ID)
                .componentSubsystem(EXPECTED_COMPONENT_SUBSYSTEM)
                .eventId(EXPECTED_EVENT_ID)
                .data(EXPECTED_DATA)
                .text1(EXPECTED_TEXT_1)
                .text2(EXPECTED_TEXT_2)
                .value1(EXPECTED_VALUE_1)
                .value2(EXPECTED_VALUE_2)
                .build();
    }

    // WHEN
    private Struct when_the_EventLog_is_converted_to_a_struct(EventLog eventLog) {
        return eventLog.toStruct();
    }

    // THEN
    private void then_the_struct_contains_all_required_fields(Struct minimumStruct) {
        minimumStruct.validate(); // ensures all required fields are set (manually or via default)

        // these are the required fields - ensure they are all set, and that they can be converted to the right data type by Kafka
        assertEquals(EXPECTED_ID, minimumStruct.getInt64(ID_FIELD));
        assertEquals(EXPECTED_NAME, minimumStruct.getString(NAME_FIELD));
        assertEquals(EXPECTED_GROUPING, minimumStruct.getInt32(GROUPING_FIELD));
        assertEquals(Date.from(EXPECTED_CLIENT_TIMESTAMP.toInstant()), minimumStruct.get(CLIENT_TIMESTAMP_FIELD));
        assertEquals(Date.from(EXPECTED_SERVER_TIMESTAMP.toInstant()), minimumStruct.get(SERVER_TIMESTAMP_FIELD));
        assertEquals(EXPECTED_SEVERITY, minimumStruct.getString(SEVERITY_FIELD));
        assertEquals(EXPECTED_SOURCE_IP, minimumStruct.getString(SOURCE_IP_FIELD));

        // we currently have no defaults defined, so all fields must also have been set manually
        assertEquals(EXPECTED_ID, minimumStruct.getWithoutDefault(ID_FIELD));
        assertEquals(EXPECTED_NAME, minimumStruct.getWithoutDefault(NAME_FIELD));
        assertEquals(EXPECTED_GROUPING, minimumStruct.getWithoutDefault(GROUPING_FIELD));
        assertEquals(Date.from(EXPECTED_CLIENT_TIMESTAMP.toInstant()), minimumStruct.getWithoutDefault(CLIENT_TIMESTAMP_FIELD));
        assertEquals(Date.from(EXPECTED_SERVER_TIMESTAMP.toInstant()), minimumStruct.getWithoutDefault(SERVER_TIMESTAMP_FIELD));
        assertEquals(EXPECTED_SEVERITY, minimumStruct.getWithoutDefault(SEVERITY_FIELD));
        assertEquals(EXPECTED_SOURCE_IP, minimumStruct.getWithoutDefault(SOURCE_IP_FIELD));
    }

    private void then_all_fields_were_converted(Struct struct) {
        then_the_struct_contains_all_required_fields(struct);

        assertEquals(EXPECTED_COMPONENT, struct.getString(COMPONENT_FIELD));
        assertEquals(EXPECTED_COMPONENT_ID, struct.getInt32(COMPONENT_ID_FIELD));
        assertEquals(EXPECTED_COMPONENT_SUBSYSTEM, struct.getString(COMPONENT_SUBSYSTEM_FIELD));
        assertEquals(EXPECTED_EVENT_ID, struct.getString(EVENT_ID_FIELD));
        assertEquals(EXPECTED_DATA, struct.getString(DATA_FIELD));
        assertEquals(EXPECTED_TEXT_1, struct.getString(TEXT_1_FIELD));
        assertEquals(EXPECTED_TEXT_2, struct.getString(TEXT_2_FIELD));
        assertEquals(EXPECTED_VALUE_1, struct.getInt32(VALUE_1_FIELD));
        assertEquals(EXPECTED_VALUE_2, struct.getInt32(VALUE_2_FIELD));

        // we currently have no defaults defined, so all fields must also have been set manually
        assertEquals(EXPECTED_COMPONENT, struct.getWithoutDefault(COMPONENT_FIELD));
        assertEquals(EXPECTED_COMPONENT_ID, struct.getWithoutDefault(COMPONENT_ID_FIELD));
        assertEquals(EXPECTED_COMPONENT_SUBSYSTEM, struct.getWithoutDefault(COMPONENT_SUBSYSTEM_FIELD));
        assertEquals(EXPECTED_EVENT_ID, struct.getWithoutDefault(EVENT_ID_FIELD));
        assertEquals(EXPECTED_DATA, struct.getWithoutDefault(DATA_FIELD));
        assertEquals(EXPECTED_TEXT_1, struct.getWithoutDefault(TEXT_1_FIELD));
        assertEquals(EXPECTED_TEXT_2, struct.getWithoutDefault(TEXT_2_FIELD));
        assertEquals(EXPECTED_VALUE_1, struct.getWithoutDefault(VALUE_1_FIELD));
        assertEquals(EXPECTED_VALUE_2, struct.getWithoutDefault(VALUE_2_FIELD));

        // ensure we didn't forget to test any non-static field
        Class<EventLog> testee = EventLog.class;
        Field[] fields = testee.getDeclaredFields();
        Arrays.stream(fields)
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(Field::getName)
                .forEach(instanceVariable ->
                        Assertions.assertNotNull(
                                struct.getWithoutDefault(instanceVariable),
                                () -> String.format("No value found for instance variable %s", instanceVariable)));

    }

    // Helper
    private static class EventLogBuilder {
        private Long id;
        private String name;
        private Integer grouping;
        private ZonedDateTime clientTimestamp;
        private ZonedDateTime serverTimestamp;
        private String severity;
        private String sourceIP;

        private String component;
        private Integer componentId;
        private String componentSubsystem;
        private String eventId;
        private String data;
        private String text1;
        private String text2;
        private Integer value1;
        private Integer value2;

        EventLogBuilder clientTimestamp(ZonedDateTime clientTimestamp) {
            this.clientTimestamp = clientTimestamp;
            return this;
        }

        EventLogBuilder component(String component) {
            this.component = component;
            return this;
        }

        EventLogBuilder componentId(Integer componentId) {
            this.componentId = componentId;
            return this;
        }

        EventLogBuilder componentSubsystem(String componentSubsystem) {
            this.componentSubsystem = componentSubsystem;
            return this;
        }

        EventLogBuilder grouping(Integer grouping) {
            this.grouping = grouping;
            return this;
        }

        EventLogBuilder id(Long id) {
            this.id = id;
            return this;
        }

        EventLogBuilder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        EventLogBuilder name(String name) {
            this.name = name;
            return this;
        }

        EventLogBuilder data(String data) {
            this.data = data;
            return this;
        }

        EventLogBuilder serverTimestamp(ZonedDateTime serverTimestamp) {
            this.serverTimestamp = serverTimestamp;
            return this;
        }

        EventLogBuilder severity(String severity) {
            this.severity = severity;
            return this;
        }

        EventLogBuilder sourceIP(String sourceIP) {
            this.sourceIP = sourceIP;
            return this;
        }

        EventLogBuilder text1(String text1) {
            this.text1 = text1;
            return this;
        }

        EventLogBuilder text2(String text2) {
            this.text2 = text2;
            return this;
        }

        EventLogBuilder value1(Integer value1) {
            this.value1 = value1;
            return this;
        }

        EventLogBuilder value2(Integer value2) {
            this.value2 = value2;
            return this;
        }

        EventLog build() {
            return new EventLog(
                    clientTimestamp,
                    component,
                    componentId,
                    componentSubsystem,
                    grouping,
                    id,
                    eventId,
                    name,
                    data,
                    serverTimestamp,
                    severity,
                    sourceIP,
                    text1,
                    text2,
                    value1,
                    value2);
        }
    }
}