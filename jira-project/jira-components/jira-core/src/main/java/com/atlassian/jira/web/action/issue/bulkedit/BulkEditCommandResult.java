package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;

import java.io.Serializable;

/**
 * Bulk edit tasks return this as their result. It's a collection of errors.
 *
 * @since 6.3.6
 */
public class BulkEditCommandResult implements Serializable
{
    private static final long serialVersionUID = 8259444784061906071L;

    private final SimpleErrorCollection errorCollection;
    private final long bulkEditTime;

    public BulkEditCommandResult(final long bulkEditTime, final ErrorCollection errorCollection)
    {
        Assertions.notNull("errorCollection", errorCollection);
        this.bulkEditTime = bulkEditTime;
        this.errorCollection = new SimpleErrorCollection(errorCollection);
    }

    public ErrorCollection getErrorCollection()
    {
        return errorCollection;
    }

    public long getBulkEditTime()
    {
        return bulkEditTime;
    }

    public boolean isSuccessful()
    {
        return !errorCollection.hasAnyErrors();
    }
}
