package com.atlassian.jira.issue.fields.rest.json;

/**
 *
 * @since v5.0
 */
public class JsonTypeBuilder
{
    public static JsonType system(String type, String system)
    {
        return new JsonType(type, null, system, null, null);
    }
    public static JsonType systemArray(String items, String system)
    {
        return new JsonType(JsonType.ARRAY_TYPE, items, system, null, null);
    }
    public static JsonType customArray(String items, String custom, Long customId)
    {
        return new JsonType(JsonType.ARRAY_TYPE, items, null, custom, customId);
    }
    public static JsonType custom(String type, String custom, Long customId)
    {
        return new JsonType(type, null, null, custom, customId);
    }
}
