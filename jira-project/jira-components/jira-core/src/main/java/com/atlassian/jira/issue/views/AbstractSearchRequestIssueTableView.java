package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.plugin.searchrequestview.AbstractSearchRequestView;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.PagerFilter;
import org.apache.log4j.Logger;

public abstract class AbstractSearchRequestIssueTableView extends AbstractSearchRequestView
{
    private static final Logger log = Logger.getLogger(AbstractSearchRequestIssueTableView.class);

    protected final JiraAuthenticationContext authenticationContext;
    protected final SearchProvider searchProvider;
    protected final ApplicationProperties applicationProperties;
    protected final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;

    protected AbstractSearchRequestIssueTableView(JiraAuthenticationContext authenticationContext, SearchProvider searchProvider, ApplicationProperties appProperties, SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil)
    {
        this.authenticationContext = authenticationContext;
        this.searchProvider = searchProvider;
        this.applicationProperties = appProperties;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
    }

    protected SearchResults getSearchResults(SearchRequest searchRequest) throws SearchException
    {
        return searchProvider.search((searchRequest != null) ? searchRequest.getQuery() : null, authenticationContext.getLoggedInUser(), PagerFilter.getUnlimitedFilter());
    }
}
