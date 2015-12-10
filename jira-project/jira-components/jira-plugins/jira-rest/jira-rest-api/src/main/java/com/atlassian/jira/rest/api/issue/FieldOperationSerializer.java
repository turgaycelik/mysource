package com.atlassian.jira.rest.api.issue;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * @since v5.0
 */
public class FieldOperationSerializer extends JsonSerializer<FieldOperation>
{
    @Override
    public void serialize(FieldOperation value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        if (value != null)
        {
            jgen.writeStartObject();
            if (value.getOperation() != null)
            {
                provider.defaultSerializeField(value.getOperation(), value.getValue(), jgen);
            }
            jgen.writeEndObject();
        }
    }
}
