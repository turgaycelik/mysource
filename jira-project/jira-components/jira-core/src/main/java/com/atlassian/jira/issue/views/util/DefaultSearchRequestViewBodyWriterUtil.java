package com.atlassian.jira.issue.views.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.util.DocumentHitCollector;
import com.atlassian.jira.issue.views.SingleIssueWriter;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.component.IssueTableWriter;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;
import java.io.Writer;

/**
 * A simple class to centralize the body writes for search request view implementations.
 */
public class DefaultSearchRequestViewBodyWriterUtil implements SearchRequestViewBodyWriterUtil
{
    private final SearchProviderFactory searchProviderFactory;
    private final IssueFactory issueFactory;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchProvider searchProvider;

    public DefaultSearchRequestViewBodyWriterUtil(final SearchProviderFactory searchProviderFactory, final IssueFactory issueFactory, final JiraAuthenticationContext authenticationContext, final SearchProvider searchProvider)
    {
        this.searchProviderFactory = searchProviderFactory;
        this.issueFactory = issueFactory;
        this.authenticationContext = authenticationContext;
        this.searchProvider = searchProvider;
    }

    /* (non-Javadoc)
     * @see com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil#writeBody(java.io.Writer, com.atlassian.jira.plugin.issueview.AbstractIssueView, com.atlassian.jira.issue.search.SearchRequest, com.atlassian.jira.issue.views.SingleIssueWriter, com.atlassian.jira.web.bean.PagerFilter)
     */
    public void writeBody(final Writer writer, final AbstractIssueView issueView, final SearchRequest searchRequest, final SingleIssueWriter singleIssueWriter, final PagerFilter pagerFilter) throws IOException, SearchException
    {
        final IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        final DocumentHitCollector hitCollector = new IssueWriterHitCollector(searcher, writer, issueFactory)
        {
            @Override
            protected void writeIssue(final Issue issue, final Writer writer) throws IOException
            {
                singleIssueWriter.writeIssue(issue, issueView, writer);
            }
        };

        searchProvider.searchAndSort((searchRequest != null) ? searchRequest.getQuery() : null, authenticationContext.getLoggedInUser(), hitCollector, pagerFilter);
    }

    /* (non-Javadoc)
     * @see com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil#writeTableBody(java.io.Writer, com.atlassian.jira.web.component.IssueTableWriter, com.atlassian.jira.issue.search.SearchRequest, com.atlassian.jira.web.bean.PagerFilter)
     */
    public void writeTableBody(final Writer writer, final IssueTableWriter issueTableWriter, final SearchRequest searchRequest, final PagerFilter pagerFilter) throws IOException, SearchException
    {
        final IndexSearcher searcher = searchProviderFactory.getSearcher(SearchProviderFactory.ISSUE_INDEX);
        final DocumentHitCollector hitCollector = new IssueWriterHitCollector(searcher, writer, issueFactory)
        {
            @Override
            protected void writeIssue(final Issue issue, final Writer writer) throws IOException
            {
                issueTableWriter.write(issue);
            }
        };

        searchProvider.searchAndSort((searchRequest != null) ? searchRequest.getQuery() : null, authenticationContext.getLoggedInUser(), hitCollector, pagerFilter);
        issueTableWriter.close();
    }

    /* (non-Javadoc)
     * @see com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil#searchCount(com.atlassian.jira.issue.search.SearchRequest)
     */
    public long searchCount(final SearchRequest searchRequest) throws SearchException
    {
        return searchProvider.searchCount((searchRequest != null) ? searchRequest.getQuery() : null, authenticationContext.getLoggedInUser());
    }
}
