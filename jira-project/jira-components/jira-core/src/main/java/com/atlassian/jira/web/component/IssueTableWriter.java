package com.atlassian.jira.web.component;

import com.atlassian.jira.issue.Issue;

import java.io.IOException;

/**
 * A callback to allow the IssueTableWebComponent to write an issue.  This should be used when writing
 * multiple issues.  Method {@link #close()} should be called to write a footer.
 */
public interface IssueTableWriter
{
    /**
     * For each issue that you wish to be written to the table, you need to call write.  You must also call
     * {@link #close()} after you have finished writing the issues
     *
     * @param issue The issue to write to the table
     * @throws IOException thrown in case of I/O error
     */
    public void write(Issue issue) throws IOException;

    /**
     * Writes the footer of the table.
     *
     * @throws IOException thrown if an error occurred writing the output
     */
    public void close() throws IOException;
}
