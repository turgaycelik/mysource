package com.atlassian.jira.event.workflow;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeAddedToProjectEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;

/**
 * @since v6.2
 */
public class WorkflowSchemeAddedToProjectEvent extends AbstractSchemeAddedToProjectEvent
{
    @Internal
    public WorkflowSchemeAddedToProjectEvent(@Nonnull final Scheme scheme, @Nonnull final Project project)
    {
        super(scheme, project);
    }
}
