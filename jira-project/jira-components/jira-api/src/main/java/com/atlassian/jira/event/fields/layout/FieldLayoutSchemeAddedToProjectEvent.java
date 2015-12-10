package com.atlassian.jira.event.fields.layout;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.project.Project;

import com.google.common.base.Preconditions;

/**
 * Event indicating a workflow scheme has associated with a project.
 *
 * @since v5.0
 */
public class FieldLayoutSchemeAddedToProjectEvent extends AbstractFieldLayoutEvent
{
    @Nonnull
    private final Project project;

    @Internal
    public FieldLayoutSchemeAddedToProjectEvent(@Nonnull final FieldLayoutScheme scheme, @Nonnull final Project project)
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
