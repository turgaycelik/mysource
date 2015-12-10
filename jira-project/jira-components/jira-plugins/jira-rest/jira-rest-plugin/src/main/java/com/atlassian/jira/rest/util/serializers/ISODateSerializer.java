package com.atlassian.jira.rest.util.serializers;

import java.io.IOException;

import com.atlassian.jira.rest.Dates;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Serializes {@link org.joda.time.DateTime} in ISO format. Unlike {@link org.codehaus.jackson.map.ext.JodaSerializers}
 * doesn't use {@link org.codehaus.jackson.map.SerializationConfig.Feature#WRITE_DATES_AS_TIMESTAMPS}.
 */
public class ISODateSerializer extends JsonSerializer<DateTime>
{

    @Override
    public void serialize(final DateTime value, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        final DateTime utcDateTime = value.toDateTime(DateTimeZone.UTC);
        final String isoDate = Dates.asISODateTimeString(utcDateTime);

        jgen.writeString(isoDate);
    }
}
