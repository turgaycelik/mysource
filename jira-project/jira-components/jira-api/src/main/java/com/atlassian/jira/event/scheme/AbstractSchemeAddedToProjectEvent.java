package com.atlassian.jira.event.scheme;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;

import com.google.common.base.Preconditions;

/**
 * Event indicating a workflow scheme has associated with a project.
 *
 * @since v5.0
 */
public class AbstractSchemeAddedToProjectEvent extends AbstractSchemeEvent
{
    @Nonnull
    private final Project project;

    @Internal
    public AbstractSchemeAddedToProjectEvent(@Nonnull final Scheme scheme, @Nonnull final Project project)
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

    @Nonnull
    public String getSchemeName()
    {
        return Preconditions.checkNotNull(getScheme()).getName();
    }

    @Nonnull
    public Long getSchemeId()
    {
        return Preconditions.checkNotNull(getScheme()).getId();
    }
}
