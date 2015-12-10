package com.atlassian.jira.auditing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.project.Project;

/**
 *
 * @since v6.3
 */
public class AffectedProject extends ParentlessAssociatedItem
{
    final private Long projectId;
    final private String projectName;

    public AffectedProject(final Project project) {
        this.projectId = project.getId();
        this.projectName = project.getName();
    }

    @Nonnull
    @Override
    public String getObjectName()
    {
        return projectName;
    }

    @Nullable
    @Override
    public String getObjectId()
    {
        return Long.toString(projectId);
    }

    @Nonnull
    @Override
    public Type getObjectType()
    {
        return Type.PROJECT;
    }
}
