package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.annotation.Nonnull;

/**
 * @since v5.0
 */
public class IssueLinksBeanBuilderFactoryImpl implements IssueLinksBeanBuilderFactory
{
    private final ApplicationProperties applicationProperties;
    private final IssueLinkManager issueLinkManager;
    private final JiraAuthenticationContext authContext;
    private final JiraBaseUrls jiraBaseUrls;

    public IssueLinksBeanBuilderFactoryImpl(ApplicationProperties applicationProperties, IssueLinkManager issueLinkManager, JiraAuthenticationContext authContext, JiraBaseUrls jiraBaseUrls)
    {
        this.applicationProperties = applicationProperties;
        this.issueLinkManager = issueLinkManager;
        this.authContext = authContext;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    /**
     * Returns a new instance of an IssueLinkBeanBuilder.
     *
     * @param issue an Issue
     * @return an IssueLinkBeanBuilder
     */
    @Nonnull
    @Override
    public IssueLinksBeanBuilder newIssueLinksBeanBuilder(Issue issue)
    {
        return new IssueLinksBeanBuilder(applicationProperties, issueLinkManager, authContext, jiraBaseUrls, issue);
    }
}
