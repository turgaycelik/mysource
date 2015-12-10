package com.atlassian.jira.event.scheme;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;

/**
 * General event related to removing schema from project
 *
 * @since v6.2
 */
public class AbstractSchemeRemovedFromProjectEvent extends AbstractSchemeEvent
{
    @Nonnull
    private final Project project;

    @Internal
    public AbstractSchemeRemovedFromProjectEvent(@Nonnull final Scheme scheme, @Nonnull final Project project)
    {
        super(scheme);
        this.project = project;
    }

    @Nonnull
    public Project getProject()
    {
        return project;
    }

    @Nonnull
    public Long getProjectId()
    {
        return project.getId();
    }

    @Nonnull
    public String getProjectName()
    {
        return project.getName();
    }
}
