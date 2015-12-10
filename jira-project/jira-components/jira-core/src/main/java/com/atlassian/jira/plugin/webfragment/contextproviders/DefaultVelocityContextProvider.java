package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import java.util.Map;

public class DefaultVelocityContextProvider implements ContextProvider
{
    private final JiraAuthenticationContext authenticationContext;

    public DefaultVelocityContextProvider(JiraAuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Map<String, Object> defaultParms = JiraVelocityUtils.getDefaultVelocityParams(context, authenticationContext);
        return defaultParms;
    }
}
