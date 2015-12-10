package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import java.util.Map;

/**
 * Context Provider that provides the base url of the application.  Referenced by "${baseurl}"
 *
 * @since v4.4
 */
public class BaseUrlContextProvider implements ContextProvider
{

    private final VelocityRequestContextFactory requestContextFactory;

    public BaseUrlContextProvider(VelocityRequestContextFactory requestContextFactory)
    {
        this.requestContextFactory = requestContextFactory;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final String baseUrl = requestContextFactory.getJiraVelocityRequestContext().getBaseUrl();

        final Map<String, Object> i18nHelperMap = MapBuilder.<String, Object>build("baseurl", baseUrl);
        return i18nHelperMap;
    }
}
