package com.atlassian.jira.bulkedit.operation;

import com.atlassian.jira.task.TaskContext;

/**
 * Context for bulk edit operations. There can be only one such operation
 * per user per operation type at any given time.
 *
 * @since v6.3.6
 */
public class BulkEditTaskContext implements TaskContext
{
    private static final long serialVersionUID = 2156317751998854335L;

    private final String username;
    private final String operationName;

    public BulkEditTaskContext(final String username, final String operationName)
    {
        this.username = username;
        this.operationName = operationName;
    }

    @Override
    public String buildProgressURL(final Long taskId)
    {
        return "/secure/views/bulkedit/BulkOperationProgress.jspa?taskId=" + taskId;
    }

    @Override
    public int hashCode()
    {
        int result = username.hashCode();
        result = 31 * result + operationName.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        BulkEditTaskContext that = (BulkEditTaskContext) o;

        if (!operationName.equals(that.operationName))
        {
            return false;
        }
        if (!username.equals(that.username))
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return "BulkEditTaskContext{" + "username=" + username + ", operationName=" + operationName + "}";
    }
}
