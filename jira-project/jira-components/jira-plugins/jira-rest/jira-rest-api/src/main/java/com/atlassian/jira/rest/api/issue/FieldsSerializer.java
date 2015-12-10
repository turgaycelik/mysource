package com.atlassian.jira.rest.api.issue;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Serialises IssueFields, with the contents of the {@code fields} map at the same level as the other fields.
 */
public class FieldsSerializer extends JsonSerializer<IssueFields>
{
    @Override
    public void serialize(final IssueFields issueFields, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        if (issueFields != null)
        {
            jgen.writeStartObject();
            serializeSystemFields(issueFields, jgen, provider);
            serializeCustomFields(issueFields, jgen, provider);
            jgen.writeEndObject();
        }
    }

    /**
     * Serialises the system fields.
     */
    protected void serializeSystemFields(final IssueFields issueFields, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        for (Field field : issueFields.getClass().getFields())
        {
            // only serialise public or annotated fields
            JsonProperty jsonAnnotation = field.getAnnotation(JsonProperty.class);
            if (isPublic(field) || jsonAnnotation != null)
            {
                try
                {
                    Object fieldValue = field.get(issueFields);
                    if (fieldValue != null)
                    {
                        String overrideName = jsonAnnotation != null ? jsonAnnotation.value() : null;
                        String jsonName = !isEmpty(overrideName) ? overrideName : field.getName();

                        provider.defaultSerializeField(jsonName, fieldValue, jgen);
                    }
                }
                catch (IllegalAccessException e)
                {
                    throw new JsonGenerationException("Error reading field '" + field.getName() + "'", e);
                }
            }
        }
    }

    private void serializeCustomFields(IssueFields issueFields, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        if (issueFields.fields != null)
        {
            for (Map.Entry<String, Object> customField : issueFields.fields.entrySet())
            {
                Object value = customField.getValue();
                if (value instanceof String)
                {
                    jgen.writeStringField(customField.getKey(), (String) customField.getValue());
                }
                else if (value instanceof Array)
                {
                    jgen.writeArrayFieldStart(customField.getKey());
                    for (String string : (String[]) value)
                    {
                        jgen.writeString(string);
                    }
                    jgen.writeEndArray();
                }
                else
                {
                    jgen.writeObjectField(customField.getKey(), customField.getValue());
                }
            }
        }
    }

    protected static boolean isPublic(Field field)
    {
        return (field.getModifiers() & Modifier.PUBLIC) != 0;
    }
}
