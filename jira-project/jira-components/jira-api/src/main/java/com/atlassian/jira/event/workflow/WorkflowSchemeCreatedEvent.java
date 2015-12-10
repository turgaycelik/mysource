package com.atlassian.jira.event.workflow;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.scheme.Scheme;

/**
 * Event indicating a workflow scheme has been created.
 *
 * @since v5.0
 */
public class WorkflowSchemeCreatedEvent extends AbstractSchemeEvent
{
    @Internal
    public WorkflowSchemeCreatedEvent(Scheme scheme)
    {
        super(scheme);
    }
}
