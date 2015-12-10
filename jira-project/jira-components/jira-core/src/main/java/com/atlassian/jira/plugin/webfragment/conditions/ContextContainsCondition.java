package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * A condition that checks to see if a passed in param equals a specific
 */
public class ContextContainsCondition implements Condition
{
    public static final String CONTEXT_KEY = "context-key";
    public static final String CONTEXT_VALUE = "context-value";

    private String key;
    private String value;

    public void init(Map params) throws PluginParseException
    {
        key = (String) params.get(CONTEXT_KEY);
        value = (String) params.get(CONTEXT_VALUE);
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value))
        {
            throw new PluginParseException("Both context-key '" + key + "' and context-value '" + value + "' must be specified");
        }
    }

    public boolean shouldDisplay(Map context)
    {
        final Object newValue = context.get(key);

        return newValue != null && newValue.equals(value);

    }
}