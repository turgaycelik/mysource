package com.atlassian.jira.web.util;

import com.google.common.collect.Maps;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MockExternalLinkUtil implements ExternalLinkUtil
{
    private Map<String, String> props = Maps.newHashMap();

    @Override
    public String getPropertiesFilename()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getProperty(String key)
    {
        if(props.containsKey(key))
        {
            return props.get(key);
        }
        else
        {
            return key;
        }
    }

    public String getProperty(String key, String value1)
    {
        return getProperty(key, Arrays.asList(value1));
    }

    public String getProperty(String key, String value1, String value2)
    {
        return getProperty(key, Arrays.asList(value1, value2));
    }

    public String getProperty(String key, String value1, String value2, String value3)
    {
        return getProperty(key, Arrays.asList(value1, value2, value3));
    }

    public String getProperty(String key, String value1, String value2, String value3, String value4)
    {
        return getProperty(key, Arrays.asList(value1, value2, value3, value4));
    }

    public String getProperty(String string, Object parameters)
    {
        Object[] params;
        if (parameters instanceof List)
        {
            params = ((List<?>) parameters).toArray();
        }
        else if (parameters instanceof Object[])
        {
            params = (Object[]) parameters;
        }
        else
        {
            params = new Object[]{parameters};
        }

        String message = getProperty(string);
        MessageFormat mf = new MessageFormat(message);
        return mf.format(params);
    }

    public MockExternalLinkUtil addLink(String key, String name)
    {
        props.put(key, name);
        return this;
    }

    public MockExternalLinkUtil addLinks(Map<String, String> props)
    {
        this.props.putAll(props);
        return this;
    }
}
