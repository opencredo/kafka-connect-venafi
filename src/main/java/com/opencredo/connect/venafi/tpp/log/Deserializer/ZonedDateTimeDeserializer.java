package com.opencredo.connect.venafi.tpp.log.Deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class ZonedDateTimeDeserializer implements JsonDeserializer<ZonedDateTime> {

    @Override
    public ZonedDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonNull()) {
            return null;
        }
        String json = jsonElement.getAsString();

        return getParsedDate(json);
    }

    private static ZonedDateTime getParsedDate(String dateTimeString) {
        ZonedDateTime dateTime = null;
        try {
            dateTime = ZonedDateTime.parse(dateTimeString);
        } catch (DateTimeParseException e) {
            //swallow exception for now
            System.out.println(e.getMessage() + "/\n" + e.getStackTrace());
        }
        if (dateTime == null) {
            try {
                dateTime = LocalDateTime.parse(dateTimeString).atZone(ZoneId.systemDefault());
            } catch (DateTimeParseException e) {
                //swallow exception for now
                System.out.println(e.getMessage() + "/\n" + e.getStackTrace());
            }
        }

        try {
            dateTime = ZonedDateTime.parse(dateTimeString + "Z");
        } catch (DateTimeParseException e) {
            //swallow exception for now
            System.out.println(e.getMessage() + "/\n" + e.getStackTrace());
        }


        return dateTime;
    }
}
