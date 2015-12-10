package com.atlassian.jira.issue.views;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;

import java.io.IOException;
import java.io.Writer;

/**
 * Interface that defines how a single issue is written out.
 */
public interface SingleIssueWriter
{
    /**
     * The writeIssue method provides information about the issue and a writer. Any information you wish to display in
     * the body about the issue should be written to the writer.
     *
     * @param issue the issue which will have information written to the display
     * @param issueView the content view into the issue (i.e. html, xml, etc)
     * @param writer the stream to write the display details to
     */
    void writeIssue(Issue issue, AbstractIssueView issueView, Writer writer) throws IOException;

}
