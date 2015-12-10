package com.atlassian.jira.web.action.admin.index;

import java.io.Serializable;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Re-indexing tasks return this as their result.  Its either a collection of errors or an index time.
 *
 * @since 3.13
 */
public class IndexCommandResult implements Serializable
{
    private static final long serialVersionUID = -7811204677156333013L;
    private final SimpleErrorCollection errorCollection;
    private final long reindexTime;

    public IndexCommandResult(final ErrorCollection errorCollection)
    {
        Assertions.notNull("errorCollection", errorCollection);
        this.errorCollection = new SimpleErrorCollection(errorCollection);
        reindexTime = 0;
    }

    public IndexCommandResult(final long reindexTime)
    {
        this.errorCollection = new SimpleErrorCollection();
        this.reindexTime = reindexTime;
    }

    public ErrorCollection getErrorCollection()
    {
        return errorCollection;
    }

    public long getReindexTime()
    {
        return reindexTime;
    }

    public boolean isSuccessful()
    {
        return !errorCollection.hasAnyErrors();
    }
}
