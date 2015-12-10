package com.atlassian.jira.plugin.headernav;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.google.common.annotations.VisibleForTesting;

import java.util.Map;

/**
 * Responsible for setting the context variables for the application switcher velocity template
 *
 * @since v5.2
 */
public class AppSwitcherContextProvider implements ContextProvider
{
    private final MapBuilder<String, Object> contextMapBuilder = MapBuilder.newBuilder();

    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public AppSwitcherContextProvider(final ApplicationProperties applicationProperties, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.applicationProperties = applicationProperties;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
        contextMapBuilder.addAll(params);
    }

    @Override
    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        contextMapBuilder.add("baseurl", velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl());
        contextMapBuilder.add("logoUrl", getLookAndFeelSettings().getAbsoluteLogoUrl());
        contextMapBuilder.add("logoWidth", getLookAndFeelSettings().getLogoWidth());
        contextMapBuilder.add("applicationTitle", applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE));
        contextMapBuilder.add("logoHeight", "30 px");
        return CompositeMap.of(context, contextMapBuilder.toMutableMap());
    }

    @VisibleForTesting
    LookAndFeelBean getLookAndFeelSettings()
    {
        return LookAndFeelBean.getInstance(applicationProperties);
    }
}
