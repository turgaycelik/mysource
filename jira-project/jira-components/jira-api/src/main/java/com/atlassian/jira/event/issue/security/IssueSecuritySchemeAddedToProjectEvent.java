package com.atlassian.jira.event.issue.security;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeAddedToProjectEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;

/**
 * @since v6.2
 */
public class IssueSecuritySchemeAddedToProjectEvent extends AbstractSchemeAddedToProjectEvent
{
    @Internal
    public IssueSecuritySchemeAddedToProjectEvent(@Nonnull final Scheme scheme, @Nonnull final Project project)
    {
        super(scheme, project);
    }
}
