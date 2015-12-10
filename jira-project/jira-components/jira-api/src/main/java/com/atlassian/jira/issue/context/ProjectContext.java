package com.atlassian.jira.issue.context;

import com.atlassian.annotations.PublicApi;
import com.atlassian.bandana.BandanaContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

@PublicApi
public class ProjectContext extends AbstractJiraContext
{
    protected Long projectCategory;
    protected Long projectId;

    private final JiraContextTreeManager treeManager;

    public ProjectContext(final Long projectId)
    {
        this(projectId, null);
    }

    public ProjectContext(final Long projectId, JiraContextTreeManager treeManager)
    {
        this.projectId = projectId;
        this.treeManager = (treeManager != null) ? treeManager : ComponentAccessor.getComponent(JiraContextTreeManager.class);
    }

    /**
     * @deprecated Use {@link #ProjectContext(Project, JiraContextTreeManager)} instead. Since v5.0.
     */
    public ProjectContext(final GenericValue project, final JiraContextTreeManager treeManager)
    {
        this((project != null) ? project.getLong("id") : null, treeManager);
    }

    public ProjectContext(final Project project, final JiraContextTreeManager treeManager)
    {
        this((project != null) ? project.getId() : null, treeManager);
    }

    public ProjectContext(final IssueContext issueContext, final JiraContextTreeManager treeManager)
    {
        this(issueContext.getProjectId(), treeManager);
    }

    public BandanaContext getParentContext()
    {
        return new ProjectCategoryContext(getProjectCategoryObject(), treeManager);
    }

    public boolean hasParentContext()
    {
        return true;
    }

    public Map<String, Object> appendToParamsMap(final Map<String, Object> input)
    {
        return MapBuilder.newBuilder(input).add(FIELD_PROJECT, projectId).toMap();
    }

    public Project getProjectObject()
    {
        if (projectId == null)
        {
            return null;
        }
        return treeManager.getProjectManager().getProjectObj(projectId);
    }

    public GenericValue getProject()
    {
        return projectId != null ? treeManager.getProjectManager().getProject(projectId) : null;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    @Override
    public GenericValue getProjectCategory()
    {
        if ((projectCategory == null) && (projectId != null))
        {
            final GenericValue projectCategoryGv = treeManager.getProjectManager().getProjectCategoryFromProject(getProject());
            projectCategory = projectCategoryGv != null ? projectCategoryGv.getLong("id") : null;
        }

        return projectCategory != null ? treeManager.getProjectManager().getProjectCategory(projectCategory) : null;
    }

    @Override
    public ProjectCategory getProjectCategoryObject()
    {
        if ((projectCategory == null) && (projectId != null))
        {
            final ProjectCategory projectCategoryObj = treeManager.getProjectManager().getProjectCategoryForProject(getProjectObject());
            projectCategory = projectCategoryObj != null ? projectCategoryObj.getId() : null;
        }

        return projectCategory != null ? treeManager.getProjectManager().getProjectCategoryObject(projectCategory) : null;
    }

    public IssueType getIssueTypeObject()
    {
        return null;
    }

    public GenericValue getIssueType()
    {
        return null;
    }

    public String getIssueTypeId()
    {
        return null;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || !(o instanceof JiraContextNode))
        {
            return false;
        }

        final ProjectContext projectContext = (ProjectContext) o;
        if (projectId != null ? !projectId.equals(projectContext.projectId) : projectContext.projectId != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        final int result = 59 * (projectId != null ? projectId.hashCode() : 0) + 397;
        return result;
    }

    @Override
    public String toString()
    {
        return "ProjectContext[projectCategoryId=" + projectCategory + ",projectId=" + projectId + ']';
    }
}
