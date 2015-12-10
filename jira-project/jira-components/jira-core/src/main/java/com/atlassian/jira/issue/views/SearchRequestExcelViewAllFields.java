package com.atlassian.jira.issue.views;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.TableLayoutFactory;

public class SearchRequestExcelViewAllFields extends AbstractSearchRequestExcelView
{
    public SearchRequestExcelViewAllFields(JiraAuthenticationContext authenticationContext, SearchProvider searchProvider, ApplicationProperties appProperties, TableLayoutFactory tableLayoutFactory, SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil, DateTimeFormatter dateTimeFormatter)
    {
        super(authenticationContext, searchProvider, appProperties, tableLayoutFactory, searchRequestViewBodyWriterUtil, dateTimeFormatter);
    }

    protected IssueTableLayoutBean getColumnLayout(SearchRequest searchRequest, User user)
    {
        return tableLayoutFactory.getAllColumnsExcelLayout(searchRequest, user);
    }
}
