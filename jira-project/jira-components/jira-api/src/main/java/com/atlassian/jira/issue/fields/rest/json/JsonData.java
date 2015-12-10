package com.atlassian.jira.issue.fields.rest.json;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.util.ErrorCollection;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @since 5.0
 */
@PublicApi
public class JsonData
{
    private final Object data;

    public JsonData(Object data)
    {
        this.data = data;
    }

    public Object getData()
    {
        return data;
    }

    public boolean isNull()
    {
        return data == null;
    }

    public String asString()
    {
        return isString() ? (String) data : null;
    }
    public String asString(String fieldName, ErrorCollection errors)
    {
        if (isString())
        {
            return (String) data;
        }
        else
        {
            errors.addError(fieldName, "expected a string", ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }
    }

    public boolean isString()
    {
        return data instanceof String;
    }

    public Map<String, Object> asObject()
    {
        return isObject() ? (Map<String, Object>) data : null;
    }
    public boolean isObject()
    {
        return data instanceof Map;
    }
    public boolean isArray()
    {
        return data instanceof List;
    }
    public List<?> asArray()
    {
        return isArray() ? (List<?>) data : null;
    }

    public List<String> asArrayOfStrings(boolean allowNulls, String fieldName, ErrorCollection errors)
    {
        if (!(data instanceof List))
        {
            errors.addError(fieldName, "data was not an array", ErrorCollection.Reason.VALIDATION_FAILED);
            return Collections.emptyList();
        }

        List input = (List) data;
        int i = 0;
        for (Iterator iterator = input.iterator(); iterator.hasNext(); i++)
        {
            Object o = iterator.next();
            if (o == null && !allowNulls)
            {
                errors.addError(fieldName, "unexpected null at index " + i, ErrorCollection.Reason.VALIDATION_FAILED);
                return Collections.emptyList();
            }
            else if (! (o instanceof String))
            {
                errors.addError(fieldName, "string expected at index " + i, ErrorCollection.Reason.VALIDATION_FAILED);
                return Collections.emptyList();
            }
        }

        return (List<String>) input;
    }

    public List<String> asArrayOfObjectsWithId(String fieldName, ErrorCollection errors)
    {
        return asArrayOfObjectsWithProperty("id", fieldName, errors);
    }

    public String asObjectWithProperty(String propertyname, String fieldName, ErrorCollection errors)
    {
        return asObjectWithProperty(propertyname, fieldName, false, errors);
    }

    public String asObjectWithProperty(String propertyname, String fieldName, boolean expected, ErrorCollection errors)
    {
        if (isObject())
        {
            return asObjectWithProperty(data, propertyname, fieldName, expected, errors);
        }
        else
        {
            errors.addError(fieldName, "data was not an object", ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }
    }

    public List<String> asArrayOfObjectsWithProperty(String propertyname, String fieldName, ErrorCollection errors)
    {
        if (!(data instanceof List))
        {
            errors.addError(fieldName, "data was not an array", ErrorCollection.Reason.VALIDATION_FAILED);
            return Collections.emptyList();
        }

        List input = (List) data;
        List<String> results = new ArrayList<String>();
        for (Object o : input)
        {
            Object value = asObjectWithProperty(o, propertyname, fieldName, false, errors);
            if (value != null)
            {
                results.add((String) value);
            }
        }
        if (results.isEmpty())
        {
            return Collections.emptyList();
        }

        return results;
    }

    private static String asObjectWithProperty(Object o, String propertyname, String fieldName, boolean expected, ErrorCollection errors)
    {
        Object value = propertyFromObject(o, propertyname, fieldName, expected, errors);
        if (value != null && !(value instanceof String))
        {
            errors.addError(fieldName, String.format("expected '%s' property to be a string", propertyname), ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }
        return (String) value;
    }
    private static Object propertyFromObject(Object o, String propertyname, String fieldName, boolean expected, ErrorCollection errors)
    {
        if (!(o instanceof Map))
        {
            errors.addError(fieldName, "expected Object", ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }

        Map object = (Map) o;
        if (expected && !object.containsKey(propertyname)) {
            errors.addError(fieldName, String.format("expected Object containing a '%s' property", propertyname), ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }
        return object.get(propertyname);
    }

    public Long getObjectLongProperty(String propertyname, ErrorCollection errors)
    {
        Object value = propertyFromObject(data, propertyname, propertyname, false, errors);
        if (errors.hasAnyErrors() || value == null)
        {
            return null;
        }
        if (!(value instanceof Number))
        {
            errors.addError(propertyname, "expected a number");
            return null;
        }
        return ((Number)value).longValue();
    }

    public Date getObjectDateProperty(String propertyname, ErrorCollection errors)
    {
        Object value = propertyFromObject(data, propertyname, propertyname, false, errors);
        if (errors.hasAnyErrors() || value == null)
        {
            return null;
        }
        if (!(value instanceof String))
        {
            errors.addError(propertyname, "expected a string (date)");
            return null;
        }
        String s = (String) value;
        try
        {
            return Dates.fromTimeString(s);
        }
        catch (IllegalArgumentException e)
        {
            errors.addError(propertyname, e.getMessage());
            return null;
        }
    }

    public String getObjectStringProperty(String propertyname, String fieldName, ErrorCollection errors)
    {
        Object value = propertyFromObject(data, propertyname, fieldName, false, errors);
        if (errors.hasAnyErrors() || value == null)
        {
            return null;
        }
        if (!(value instanceof String))
        {
            errors.addError(propertyname, "expected a string");
            return null;
        }
        return (String) value;
    }

    public <T> T convertValue(String propertyname, Class<T> type, ErrorCollection errors)
    {
        try
        {
            return mapper().convertValue(data, type);
        }
        catch (IllegalArgumentException e)
        {
            final Throwable cause = e.getCause();
            errors.addError(propertyname, cause == null ? e.getMessage() : e.getCause().getMessage(), ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }
    }

    /**
     * Converts the value in this JsonData to a generic type. To get a list of UserJsonBean, for example, use:
     * <pre>
     * List&lt;UserJsonBean&gt; users = jsonData.convertValue("customfield_10000", new TypeRef&lt;List&lt;UserJsonBean&gt;&gt; {}, errors);
     * </pre>
     *
     * @param fieldId a String containing the field id
     * @param type a TypeRef used to capture the generic type information. this will generally be an anonymous class
     * @param errors an ErrorCollection to use for storing errors
     * @param <T> the type that we want to deserialise into
     * @return an instance of T, or null
     */
    public <T> T convertValue(String fieldId, TypeRef<T> type, ErrorCollection errors)
    {
        try
        {
            ObjectMapper mapper = mapper();
            JavaType t = mapper.getTypeFactory().constructType(type._type);
            return mapper.<T>convertValue(data, t);
        }
        catch (IllegalArgumentException e)
        {
            final Throwable cause = e.getCause();
            errors.addError(fieldId, cause == null ? e.getMessage() : e.getCause().getMessage(), ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }
    }

    private static ObjectMapper mapper() {
        return new ObjectMapper();
    }
}
