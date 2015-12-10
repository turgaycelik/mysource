package com.atlassian.jira.config;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.annotations.VisibleForTesting;

/**
 * Context for global index operations. Only one global index operation is
 * allowed at one time. This should be instantiated only for task querying. For
 * submitting task please instantiate its children.
 *
 * @since v3.13
 */
@Internal
@VisibleForTesting
public class ReplicatedIndexTaskContext implements IndexTask
{
    private static final long serialVersionUID = 4412332782142922664L;

    private final String nodeId;

    public ReplicatedIndexTaskContext(final String nodeId)
    {
        this.nodeId = nodeId;
    }

    public String buildProgressURL(final Long taskId)
    {
        return "/secure/admin/jira/IndexProgress.jspa?taskId=" + taskId;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ReplicatedIndexTaskContext that = (ReplicatedIndexTaskContext) o;

        return nodeId == null ? that.nodeId == null : nodeId.equals(that.nodeId);
    }

    @Override
    public int hashCode()
    {
        return nodeId != null ? nodeId.hashCode() : 0;
    }

    @Override
    public String getTaskInProgressMessage(final I18nHelper i18n)
    {
        return null;
    }
}
