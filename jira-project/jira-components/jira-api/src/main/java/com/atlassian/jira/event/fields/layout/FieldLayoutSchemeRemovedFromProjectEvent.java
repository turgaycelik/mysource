package com.atlassian.jira.event.fields.layout;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.project.Project;

/**
 * General event related to removing schema from project
 *
 * @since v6.2
 */
public class FieldLayoutSchemeRemovedFromProjectEvent extends AbstractFieldLayoutEvent
{
    @Nonnull
    private final Project project;

    @Internal
    public FieldLayoutSchemeRemovedFromProjectEvent(@Nonnull final FieldLayoutScheme scheme, @Nonnull final Project project)
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
