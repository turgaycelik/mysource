package com.atlassian.jira.event.project;

import com.atlassian.fugue.Option;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;

import com.google.common.base.Objects;

/**
 * Event published when category assigned to project changes
 *
 * @since v6.3
 */
public class ProjectCategoryChangeEvent
{
    private Project project;
    private ProjectCategory oldProjectCategory;
    private ProjectCategory newProjectCategory;

    private ProjectCategoryChangeEvent(final Project project, final ProjectCategory oldProjectCategory, final ProjectCategory newProjectCategory)
    {
        this.project = project;
        this.oldProjectCategory = oldProjectCategory;
        this.newProjectCategory = newProjectCategory;
    }

    public Project getProject()
    {
        return project;
    }

    public ProjectCategory getOldProjectCategory()
    {
        return oldProjectCategory;
    }

    public ProjectCategory getNewProjectCategory()
    {
        return newProjectCategory;
    }

    public static final class Builder
    {

        private ProjectCategory oldCategory;
        private ProjectCategory newCategory;
        private Project project;

        public Builder(Project project) {
            this.project = project;
        }

        public Builder addOldCategory(final ProjectCategory projectCategory)
        {
            this.oldCategory = projectCategory;
            return this;
        }

        public Builder addNewCategory(final ProjectCategory projectCategory)
        {
            this.newCategory = projectCategory;
            return this;
        }

        public Builder addProject(final Project project)
        {
            this.project = project;
            return this;
        }

        public ProjectCategoryChangeEvent build()
        {
            return new ProjectCategoryChangeEvent(project, oldCategory, newCategory);
        }

        public boolean canBePublished()
        {
            return (newCategory != null && oldCategory == null) ||
                   (newCategory == null && oldCategory != null) ||
                   (newCategory != null && oldCategory != null &&
                           !Objects.equal(newCategory.getId(), oldCategory.getId()));
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final ProjectCategoryChangeEvent that = (ProjectCategoryChangeEvent) o;

        if (newProjectCategory != null ? !newProjectCategory.equals(that.newProjectCategory) : that.newProjectCategory != null)
        {
            return false;
        }
        if (oldProjectCategory != null ? !oldProjectCategory.equals(that.oldProjectCategory) : that.oldProjectCategory != null)
        {
            return false;
        }
        if (project != null ? !project.equals(that.project) : that.project != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = project != null ? project.hashCode() : 0;
        result = 31 * result + (oldProjectCategory != null ? oldProjectCategory.hashCode() : 0);
        result = 31 * result + (newProjectCategory != null ? newProjectCategory.hashCode() : 0);
        return result;
    }
}
