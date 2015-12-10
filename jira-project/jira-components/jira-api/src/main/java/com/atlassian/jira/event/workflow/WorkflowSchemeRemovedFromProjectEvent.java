package com.atlassian.jira.event.workflow;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeRemovedFromProjectEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;

/**
 */
public class WorkflowSchemeRemovedFromProjectEvent extends AbstractSchemeRemovedFromProjectEvent
{
    @Internal
    public WorkflowSchemeRemovedFromProjectEvent(@Nonnull final Scheme scheme, @Nonnull final Project project)
    {
        super(scheme, project);
    }
}
