package com.opencredo.connect.venafi.tpp.log.Deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;


//TODO test DESERIALIZERS
public class ZonedDateTimeDeserializer implements JsonDeserializer<ZonedDateTime> {

    private static final Logger log = LoggerFactory.getLogger(ZonedDateTimeDeserializer.class);

    private static ZonedDateTime getParsedDate(String dateTimeString) {
        try {
            return LocalDateTime.parse(dateTimeString).atZone(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            //swallow exception for now
            log.debug("Failed to parse to LocalDateTime format", e);
        }

        try {
            return ZonedDateTime.parse(dateTimeString);
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse to ZonedDateTime format", e);
            throw new JsonParseException("Unable to deserialize [" + dateTimeString + "] to a ZoneDateTime.", e);
        }
    }

    @Override
    public ZonedDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonNull() || jsonElement.getAsString().isEmpty()) {
            throw new JsonParseException("Unable to deserialize [" + jsonElement + "] to a ZoneDateTime.");
        }
        String json = jsonElement.getAsString();
        return getParsedDate(json);
    }
}
