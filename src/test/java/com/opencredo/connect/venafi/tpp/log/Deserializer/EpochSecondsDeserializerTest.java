package com.opencredo.connect.venafi.tpp.log.Deserializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class EpochSecondsDeserializerTest {

    public static final EpochSecondsDeserializer DESERIALIZER = new EpochSecondsDeserializer();
    public static final long EPOCH_SECONDS_25_OCT_2021 = 1635153150;
    public static final ZonedDateTime ZONED_DATE_TIME_25_OCT_21 = ZonedDateTime.parse("2021-10-25T09:12:30Z");
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
    void epoch_seconds() {
        JsonElement jsonElem = given_json_of_(EPOCH_SECONDS_25_OCT_2021);
        ZonedDateTime dateTime = when_the_date_is_deserialized(jsonElem);
        assertNotNull(dateTime);
        System.out.println(dateTime);
        assertTrue(dateTime.isEqual(ZONED_DATE_TIME_25_OCT_21));
        assertEquals(dateTime.getOffset(), ZoneOffset.ofHours(0));
    }

    private JsonElement given_an_empty_string_json_elem() {
        return new JsonPrimitive("");
    }

    private JsonElement given_a_json_null() {
        return JsonNull.INSTANCE;
    }

    private JsonElement given_json_of_(long milliseconds) {
        return new JsonPrimitive(milliseconds);
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
