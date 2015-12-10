package com.atlassian.jira.event.workflow;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeDeletedEvent;

/**
 * Event indicating a workflow scheme has been deleted.
 *
 * @since v5.0
 */
public class WorkflowSchemeDeletedEvent extends AbstractSchemeDeletedEvent
{
    /**
     *
     * @deprecated Please use {@link #WorkflowSchemeDeletedEvent(Long, String)}. Since v6.2
     */
    @Deprecated
    @Internal
    public WorkflowSchemeDeletedEvent(Long id)
    {
        super(id, null);
    }

    @Internal
    public WorkflowSchemeDeletedEvent(Long id, String name)
    {
        super(id, name);
    }
}
