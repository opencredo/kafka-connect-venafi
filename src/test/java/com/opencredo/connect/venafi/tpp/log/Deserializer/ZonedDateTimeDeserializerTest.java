package com.opencredo.connect.venafi.tpp.log.Deserializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.opencredo.connect.venafi.tpp.log.Deserializer.DotNetDateDeserializerTest.ZONED_DATE_TIME_1945_05_04_Z;
import static org.junit.jupiter.api.Assertions.*;

class ZonedDateTimeDeserializerTest {
    public static final ZonedDateTimeDeserializer DESERIALIZER = new ZonedDateTimeDeserializer();
    public static final String LOCAL_DATE_TIME = "1984-05-04T00:00:00.0000000";
    public static final String ZONED_DATE_TIME = LOCAL_DATE_TIME + "Z";
    public static final String GIBBERISH = "sadkjalskjdalksjd";

    @Test
    void json_null() {
        JsonElement jsonElem = given_a_json_null();
        Executable dateTimeExecutable = when_the_date_is_deserialized_which_may_throw_exception(jsonElem);
        assertThrows(JsonParseException.class, dateTimeExecutable);
    }

    @Test
    void empty_string() {
        JsonElement jsonElem = given_an_empty_string_json_elem();
        Executable dateTimeExecutable = when_the_date_is_deserialized_which_may_throw_exception(jsonElem);
        assertThrows(JsonParseException.class, dateTimeExecutable);
    }

    @Test
    void invalid_string() {
        JsonElement jsonElem = given_json_of_(GIBBERISH);
        Executable dateTimeExecutable = when_the_date_is_deserialized_which_may_throw_exception(jsonElem);
        assertThrows(JsonParseException.class, dateTimeExecutable);
    }

    @Test
    void localDateTime() {
        JsonElement jsonElem = given_json_of_(LOCAL_DATE_TIME);
        ZonedDateTime dateTime = when_the_date_is_deserialized(jsonElem);
        assertNotNull(dateTime);
        assertTrue(dateTime.isEqual(ZONED_DATE_TIME_1945_05_04_Z));
        assertEquals(dateTime.getOffset(), ZoneOffset.ofHours(0));
    }

    @Test
    void zonedDateTime() {
        JsonElement jsonElem = given_json_of_(ZONED_DATE_TIME);
        ZonedDateTime dateTime = when_the_date_is_deserialized(jsonElem);
        assertNotNull(dateTime);
        assertTrue(dateTime.isEqual(ZONED_DATE_TIME_1945_05_04_Z));
        assertEquals(dateTime.getOffset(), ZoneOffset.ofHours(0));
    }

    private JsonElement given_an_empty_string_json_elem() {
        return new JsonPrimitive("");
    }

    private JsonElement given_a_json_null() {
        return JsonNull.INSTANCE;
    }

    private JsonElement given_json_of_(String milliseconds) {
        return new JsonPrimitive(milliseconds);
    }

    private ZonedDateTime when_the_date_is_deserialized(JsonElement jsonElem) {
        return DESERIALIZER.deserialize(jsonElem, null, null);
    }

    private Executable when_the_date_is_deserialized_which_may_throw_exception(JsonElement jsonElem) {
        return () -> DESERIALIZER.deserialize(jsonElem, null, null);
    }
}