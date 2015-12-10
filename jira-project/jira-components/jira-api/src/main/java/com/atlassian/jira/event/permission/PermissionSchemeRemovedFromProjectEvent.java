package com.atlassian.jira.event.permission;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.event.scheme.AbstractSchemeRemovedFromProjectEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;

/**
 * @since v6.2
 */
public class PermissionSchemeRemovedFromProjectEvent extends AbstractSchemeRemovedFromProjectEvent
{
    @Internal
    public PermissionSchemeRemovedFromProjectEvent(@Nonnull final Scheme scheme, @Nonnull final Project project)
    {
        super(scheme, project);
    }
}
