package com.opencredo.connect.venafi.tpp.log.Deserializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.opencredo.connect.venafi.tpp.log.TppLogSourceTask.DEFAULT_FROM_TIME;
import static org.junit.jupiter.api.Assertions.*;

class DotNetDateDeserializerTest {

    public static final String MILLISECOND_SINCE_EPOCH_UNTIL_1945_MAY_04_Z = "452476800000";
    public static final String SLASH_DATE = "/DATE(";
    public static final String CLOSE_BRACKET_SLASH = ")/";
    public static final String NEGATIVE_OFFSET = "-0100";
    public static final String POSITIVE_OFFSET = "+0100";
    public static final ZonedDateTime ZONED_DATE_TIME_1945_05_04_Z = ZonedDateTime.parse(DEFAULT_FROM_TIME);
    public static final DotNetDateDeserializer DESERIALIZER = new DotNetDateDeserializer();

    @Test
    void deserialize_empty_string() {
        JsonElement jsonElem = given_an_empty_string_json_elem();
        Executable dateTimeExecutable = when_the_date_is_deserialized_which_may_throw_exception(jsonElem);
        assertThrows(JsonParseException.class, dateTimeExecutable);
    }

    @Test
    void deserialize_json_null() {
        JsonElement jsonElem = given_a_json_null();
        Executable dateTimeExecutable = when_the_date_is_deserialized_which_may_throw_exception(jsonElem);
        assertThrows(JsonParseException.class, dateTimeExecutable);
    }

    @Test
    void deserialize_invalid_null() {
        JsonElement jsonElem = given_json_of_milliseconds("");
        Executable dateTimeExecutable = when_the_date_is_deserialized_which_may_throw_exception(jsonElem);
        assertThrows(JsonParseException.class, dateTimeExecutable);
    }

    @Test
    void deserialize_normal_format() {
        JsonElement jsonElem = given_json_of_milliseconds(MILLISECOND_SINCE_EPOCH_UNTIL_1945_MAY_04_Z);
        ZonedDateTime dateTime = when_the_date_is_deserialized(jsonElem);
        assertNotNull(dateTime);
        assertTrue(dateTime.isEqual(ZONED_DATE_TIME_1945_05_04_Z));
        assertEquals(dateTime.getOffset(), ZoneOffset.ofHours(0));
    }

    @Test
    void deserialize_positive_timezone() {
        JsonElement jsonElem = given_json_of_milliseconds_with_offset(MILLISECOND_SINCE_EPOCH_UNTIL_1945_MAY_04_Z, POSITIVE_OFFSET);
        ZonedDateTime dateTime = when_the_date_is_deserialized(jsonElem);
        assertNotNull(dateTime);
        assertTrue(dateTime.isEqual(ZONED_DATE_TIME_1945_05_04_Z));
        assertEquals(dateTime.getOffset(), ZoneOffset.ofHours(1));
    }

    @Test
    void deserialize_negative_timezone() {
        JsonElement jsonElem = given_json_of_milliseconds_with_offset(MILLISECOND_SINCE_EPOCH_UNTIL_1945_MAY_04_Z, NEGATIVE_OFFSET);
        ZonedDateTime dateTime = when_the_date_is_deserialized(jsonElem);
        assertNotNull(dateTime);
        assertTrue(dateTime.isEqual(ZONED_DATE_TIME_1945_05_04_Z));
        assertEquals(dateTime.getOffset(), ZoneOffset.ofHours(-1));
    }

    private JsonElement given_an_empty_string_json_elem() {
        return new JsonPrimitive("");
    }

    private JsonElement given_a_json_null() {
        return JsonNull.INSTANCE;
    }

    private JsonElement given_json_of_milliseconds(String milliseconds) {
        return new JsonPrimitive(SLASH_DATE + milliseconds + CLOSE_BRACKET_SLASH);
    }

    private JsonElement given_json_of_milliseconds_with_offset(String milliseconds, String offset) {
        return new JsonPrimitive(SLASH_DATE + milliseconds + offset + CLOSE_BRACKET_SLASH);
    }

    private ZonedDateTime when_the_date_is_deserialized(JsonElement jsonElem) {
        return DESERIALIZER.deserialize(jsonElem, null, null);
    }

    private Executable when_the_date_is_deserialized_which_may_throw_exception(JsonElement jsonElem) {
        return () -> DESERIALIZER.deserialize(jsonElem, null, null);
    }
}