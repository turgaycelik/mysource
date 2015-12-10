package com.atlassian.jira.issue.views.util;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class that is used to print a link from a specific view of a search request back to the issue navigator.
 */
public class DefaultSearchRequestPreviousView implements SearchRequestPreviousView
{
    private static final String PREVIOUS_VIEW_TEMPLATE_NAME = "previous";

    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;

    public DefaultSearchRequestPreviousView(final JiraAuthenticationContext authenticationContext, final ApplicationProperties applicationProperties)
    {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
    }

    /* (non-Javadoc)
     * @see com.atlassian.jira.issue.views.util.SearchRequestPreviousView#getLinkToPrevious(com.atlassian.jira.issue.search.SearchRequest, com.atlassian.jira.plugin.JiraResourcedModuleDescriptor)
     */
    public String getLinkToPrevious(final SearchRequest searchRequest, final JiraResourcedModuleDescriptor<?> descriptor)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String baseUrl = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext().getBaseUrl();
        params.put("i18n", authenticationContext.getI18nHelper());
        params.put("link", SearchRequestViewUtils.getLink(searchRequest, baseUrl, authenticationContext.getLoggedInUser()));

        return descriptor.getHtml(PREVIOUS_VIEW_TEMPLATE_NAME, params);
    }

    /* (non-Javadoc)
     * @see com.atlassian.jira.issue.views.util.SearchRequestPreviousView#getLinkToPrevious(com.atlassian.jira.issue.Issue, com.atlassian.jira.plugin.JiraResourcedModuleDescriptor)
     */
    public String getLinkToPrevious(final Issue issue, final JiraResourcedModuleDescriptor<?> descriptor)
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        final String baseUrl = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext().getBaseUrl();
        params.put("i18n", authenticationContext.getI18nHelper());
        params.put("link", baseUrl + "/browse/" + issue.getKey());

        return descriptor.getHtml(PREVIOUS_VIEW_TEMPLATE_NAME, params);
    }
}
