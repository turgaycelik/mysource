package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 *
 */
public class SearchRequestFullContentView extends AbstractSearchRequestFullContentView
{
    public SearchRequestFullContentView(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil)
    {
        super(authenticationContext, applicationProperties,searchRequestViewBodyWriterUtil);
    }

    protected Class getIssueViewClass()
    {
        return IssueHtmlView.class;
    }

    protected boolean showLinkToIssueNavigator()
    {
        return true;
    }

}
