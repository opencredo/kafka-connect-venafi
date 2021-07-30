package com.opencredo.connect.venafi.tpp.log.Deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class EpochSecondsDeserializer implements JsonDeserializer<ZonedDateTime> {
    private static final Logger log = LoggerFactory.getLogger(EpochSecondsDeserializer.class);

    private ZonedDateTime getParsedDate(String dateTimeString) {
        ZonedDateTime zonedDateTime;
        try {
            Instant instant = Instant.ofEpochSecond(Long.parseLong(dateTimeString));
            zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC.normalized());
        } catch (DateTimeParseException e) {
            log.debug("Failed to parse to ZonedDateTime format", e);
            throw new JsonParseException("Unable to deserialize [" + dateTimeString + "] to a ZoneDateTime.", e);
        }
        return zonedDateTime;
    }

    @Override
    public ZonedDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonNull() || jsonElement.getAsString().isEmpty() || !jsonElement.getAsString().chars().allMatch(Character::isDigit)) {
            throw new JsonParseException("Unable to deserialize [" + jsonElement + "] to a ZoneDateTime.");
        }
        String json = jsonElement.getAsString();
        return getParsedDate(json);
    }
}
