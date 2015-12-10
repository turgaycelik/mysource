package com.atlassian.jira.event.workflow;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating a workflow scheme has been updated.
 *
 * @since v5.0
 */
public class WorkflowSchemeUpdatedEvent extends AbstractSchemeUpdatedEvent
{
    /**
     * @deprecated Use {@link #WorkflowSchemeUpdatedEvent(com.atlassian.jira.scheme.Scheme, com.atlassian.jira.scheme.Scheme)}. Since v6.2
     */
    @Deprecated
    @Internal
    public WorkflowSchemeUpdatedEvent(Scheme scheme)
    {
        super(scheme, null);
    }

    @Internal
    public WorkflowSchemeUpdatedEvent(Scheme scheme, Scheme originalScheme)
    {
        super(scheme, originalScheme);
    }
}
