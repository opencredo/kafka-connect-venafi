package com.opencredo.connect.venafi.tpp.log.Deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;


//TODO test DESERIALIZERS
public class ZonedDateTimeDeserializer implements JsonDeserializer<ZonedDateTime> {

    private static final Logger log = LoggerFactory.getLogger(ZonedDateTimeDeserializer.class);

    private static ZonedDateTime getParsedDate(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString).atZone(ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            //swallow exception for now
            log.error("Failed to parse to LocalDateTime format", e);
        }

        try {
            return ZonedDateTime.parse(dateTimeString);
        } catch (DateTimeParseException e) {
            //swallow exception for now
            log.error("Failed to parse to ZonedDateTime format", e);
        }

        try {
            return ZonedDateTime.parse(dateTimeString + "Z");
        } catch (DateTimeParseException up) {
            log.error("Failed to parse to ZonedDateTime format with added Z.", up);
            throw up;
        }
    }

    @Override
    public ZonedDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonNull()) {
            return null;
        }
        String json = jsonElement.getAsString();

        return getParsedDate(json);
    }
}
