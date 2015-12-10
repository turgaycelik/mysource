package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Condition to see if a system property option is set to true. Property is defined by the "property" param.
 *
 * @since v4.4
 */
public class IsApplicationPropertySetCondition implements Condition
{
    private final ApplicationProperties applicationProperties;
    private String property;

    public IsApplicationPropertySetCondition(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
        property = params.get("property");

    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return applicationProperties.getOption(property);

    }
}
