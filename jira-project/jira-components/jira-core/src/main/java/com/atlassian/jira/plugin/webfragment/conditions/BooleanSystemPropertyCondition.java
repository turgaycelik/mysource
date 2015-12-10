package com.atlassian.jira.plugin.webfragment.conditions;


import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * A condition which is true iff the configured system property is set and has case insensitive value "true".
 *
 * @since v5.1
 */
public final class BooleanSystemPropertyCondition implements Condition
{
    private String property;

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        property = params.get("property");

    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return property != null && JiraSystemProperties.getInstance().getBoolean(property);
    }
}
