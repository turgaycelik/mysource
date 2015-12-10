package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.util.DefaultSearchRequestPreviousView;
import com.atlassian.jira.issue.views.util.SearchRequestPreviousView;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.plugin.searchrequestview.AbstractSearchRequestView;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.io.IOException;
import java.io.Writer;

public abstract class AbstractSearchRequestFullContentView extends AbstractSearchRequestView
{
    protected final ApplicationProperties applicationProperties;
    private SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final SearchRequestPreviousView searchRequestPreviousView;
    private static final String PAGE_BREAK_HTML = "<br /><br style='page-break-before:always;'/><br />";
    private static final String HR_CLASS_FULLCONTENT = "<hr class='fullcontent'>";

    public AbstractSearchRequestFullContentView(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil)
    {
        this.applicationProperties = applicationProperties;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
        this.searchRequestPreviousView = new DefaultSearchRequestPreviousView(authenticationContext, applicationProperties);
    }

    public void writeSearchResults(final SearchRequest searchRequest, final SearchRequestParams searchRequestParams, final Writer writer)
    {
        AbstractIssueHtmlView issueView = (AbstractIssueHtmlView) SearchRequestViewUtils.getIssueView(getIssueViewClass());

        try
        {
            if (issueView == null)
            {
                writer.write("Could not find plugin of class '" + getIssueViewClass() + "'.  This is needed for this plugin to work");
                return;
            }

            String title = SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getString(APKeys.JIRA_TITLE));
            String linkToPrevious = null;
            if (showLinkToIssueNavigator())
            {
                linkToPrevious = searchRequestPreviousView.getLinkToPrevious(searchRequest, descriptor);
            }

            writer.write(issueView.getHeader(title, linkToPrevious));
            SingleIssueWriter singleIssueWriter = new SingleIssueWriter()
            {
                public void writeIssue(Issue issue, AbstractIssueView issueView, Writer writer) throws IOException
                {
                    writer.write(issueView.getBody(issue, searchRequestParams));
                    writer.write(HR_CLASS_FULLCONTENT);
                    if (applicationProperties.getOption(APKeys.FULL_CONTENT_VIEW_PAGEBREAKS))
                    {
                        writer.write(PAGE_BREAK_HTML);
                    }
                }
            };
            searchRequestViewBodyWriterUtil.writeBody(writer, issueView, searchRequest, singleIssueWriter, searchRequestParams.getPagerFilter());
            writer.write(issueView.getFooter(null));
        }
        catch (SearchException e)
        {
            throw new DataAccessException(e);
        }
        catch (IOException e)
        {
            throw new DataAccessException(e);
        }

    }

    /**
     * Return a class that extends {@link AbstractIssueHtmlView}
     *
     * @return
     */
    protected abstract Class getIssueViewClass();

    /**
     * With a word view of an issue - you do not want to show a link back to the previous
     *
     * @return true if you want links to Issue Navigator to be shown, false otherwise
     */
    protected abstract boolean showLinkToIssueNavigator();
}
