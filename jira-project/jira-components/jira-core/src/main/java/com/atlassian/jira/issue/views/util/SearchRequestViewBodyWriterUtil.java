package com.atlassian.jira.issue.views.util;

import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.SingleIssueWriter;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.component.IssueTableWriter;

import java.io.IOException;
import java.io.Writer;

public interface SearchRequestViewBodyWriterUtil
{
    /**
     * Writes the body of a sinlge issue.  This is used mostly by the issue full content views.  It allows the specific
     * SearchRequestViews (i.e.: Excel, Word, etc) to provide a callback class implementing the {@link
     * com.atlassian.jira.issue.views.SingleIssueWriter} interface specifying how a single Issue should be written to
     * the display. It uses the details provided by the {@link com.atlassian.jira.plugin.issueview.AbstractIssueView} to write
     * the response.
     *
     * @param writer used to stream the response to.
     * @param issueView contains the details of how a single issue should be written (i.e.: HTML for header, body and footer)
     * @param searchRequest The original searchRequest that defines the issues to be viewed.
     * @param singleIssueWriter Provides a callback method that is defined in the specific SearchRequestViews.  It will
     * be invoked to write the issue to the writer.
     * @param pagerFilter Used to limit the number of issues being written.
     * @throws IOException if things are seriously wrong
     * @throws SearchException if there is something wrong with the SearchRequest
     */
    void writeBody(final Writer writer, final AbstractIssueView issueView, final SearchRequest searchRequest, final SingleIssueWriter singleIssueWriter, final PagerFilter pagerFilter) throws IOException, SearchException;

    /**
     * Writes the body of a single issue in a table view (i.e.: Printable or Excel). It allows the specific
     * SearchRequestViews (i.e.: Excel, Word, etc) to provide a callback class implementing the {@link
     * com.atlassian.jira.web.component.IssueTableWriter} interface specifying how a single Issue should be written to
     * the display.
     *
     * @param writer used to stream the response to.
     * @param issueTableWriter Defines how a single issue is displayed in a table view.
     * @param searchRequest The original searchRequest that defines the issues to be viewed.
     * be invoked to write the issue to the writer.
     * @param pagerFilter Used to limit the number of issues being written.
     * @throws IOException if things are seriously wrong
     * @throws SearchException if there is something wrong with the SearchRequest
     */
    void writeTableBody(final Writer writer, final IssueTableWriter issueTableWriter, final SearchRequest searchRequest, final PagerFilter pagerFilter) throws IOException, SearchException;

    /**
     * Returns the search count
     *
     * @param searchRequest The original searchRequest that defines the issues to be viewed.
     * @return the search count
     * @throws SearchException if there is something wrong with the SearchRequest
     */
    long searchCount(final SearchRequest searchRequest) throws SearchException;
}