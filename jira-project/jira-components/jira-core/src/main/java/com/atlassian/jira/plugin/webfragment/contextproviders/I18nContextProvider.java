package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import java.util.Map;

public class I18nContextProvider implements ContextProvider
{
    private final JiraAuthenticationContext authenticationContext;

    public I18nContextProvider(JiraAuthenticationContext authenticationContext)
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
        final Map<String, Object> i18nHelperMap = MapBuilder.<String, Object>build("i18n", authenticationContext.getI18nHelper());
        return i18nHelperMap;
    }
}
