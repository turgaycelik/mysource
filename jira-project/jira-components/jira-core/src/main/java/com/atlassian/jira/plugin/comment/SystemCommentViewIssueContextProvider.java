package com.atlassian.jira.plugin.comment;

import java.util.Map;

import com.atlassian.jira.plugin.webfragment.contextproviders.BaseUrlContextProvider;
import com.atlassian.jira.plugin.webfragment.contextproviders.I18nContextProvider;
import com.atlassian.jira.plugin.webfragment.contextproviders.ModifierKeyContextProvider;
import com.atlassian.jira.plugin.webfragment.contextproviders.XsrfTokenContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import com.google.common.collect.ImmutableSet;

/**
 * Provides context for system comments.
 */
public class SystemCommentViewIssueContextProvider implements ContextProvider
{
    private final ImmutableSet<ContextProvider> contextProviders;

    public SystemCommentViewIssueContextProvider(JiraAuthenticationContext authenticationContext,
            VelocityRequestContextFactory velocityRequestContextFactory,
            XsrfTokenGenerator xsrfTokenGenerator)
    {
        this.contextProviders = ImmutableSet.<ContextProvider>builder()
                .add(new I18nContextProvider(authenticationContext))
                .add(new BaseUrlContextProvider(velocityRequestContextFactory))
                .add(new XsrfTokenContextProvider(xsrfTokenGenerator))
                .add(new ModifierKeyContextProvider(authenticationContext))
                .build();
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        Map<String, Object> returnContext = MapBuilder.newBuilder(context).toMutableMap();

        for (ContextProvider provider : contextProviders)
        {
            returnContext = CompositeMap.of(returnContext, provider.getContextMap(context));
        }
        return returnContext;
    }
}
