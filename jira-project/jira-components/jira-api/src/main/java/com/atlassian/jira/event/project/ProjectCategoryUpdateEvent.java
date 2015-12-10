package com.atlassian.jira.event.project;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;

import java.util.Collection;

/**
 * Event published when ProjectCategory is updated (i.e.: name of the category changes)
 * @since 6.3
 */
public class ProjectCategoryUpdateEvent
{
    private final ProjectCategory oldProjectCategory;
    private final ProjectCategory newProjectCategory;

    public ProjectCategoryUpdateEvent(final ProjectCategory oldProjectCategory, final ProjectCategory newProjectCategory)
    {
        this.oldProjectCategory = oldProjectCategory;
        this.newProjectCategory = newProjectCategory;
    }

    public ProjectCategory getOldProjectCategory()
    {
        return oldProjectCategory;
    }

    public ProjectCategory getNewProjectCategory()
    {
        return newProjectCategory;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ProjectCategoryUpdateEvent that = (ProjectCategoryUpdateEvent) o;

        if (newProjectCategory != null ? !newProjectCategory.equals(that.newProjectCategory) : that.newProjectCategory != null)
        {
            return false;
        }

        if (oldProjectCategory != null ? !oldProjectCategory.equals(that.oldProjectCategory) : that.oldProjectCategory != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = oldProjectCategory != null ? oldProjectCategory.hashCode() : 0;
        result = 31 * result + (newProjectCategory != null ? newProjectCategory.hashCode() : 0);
        return result;
    }
}
