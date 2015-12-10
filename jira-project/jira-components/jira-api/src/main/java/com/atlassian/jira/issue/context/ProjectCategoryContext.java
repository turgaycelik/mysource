package com.atlassian.jira.issue.context;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.bandana.BandanaContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

@PublicApi
public class ProjectCategoryContext extends AbstractJiraContext
{
    // ------------------------------------------------------------------------------------------------- Type Properties
    protected ProjectCategory projectCategory;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final JiraContextTreeManager treeManager;

    // ---------------------------------------------------------------------------------------------------- Constructors

    /**
     * @deprecated Use {@link #ProjectCategoryContext(com.atlassian.jira.project.ProjectCategory, com.atlassian.jira.issue.context.manager.JiraContextTreeManager)} instead. Since v5.2.
     * @param projectCategoryGV
     * @param treeManager
     */
    public ProjectCategoryContext(final GenericValue projectCategoryGV, final JiraContextTreeManager treeManager)
    {
        this.projectCategory = projectCategoryGV == null ? null : treeManager.getProjectManager().getProjectCategoryObject(projectCategoryGV.getLong("id"));
        this.treeManager = treeManager;
    }

    public ProjectCategoryContext(ProjectCategory projectCategory, JiraContextTreeManager treeManager)
    {
        this.projectCategory = projectCategory;
        this.treeManager = treeManager;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    @Override
    public BandanaContext getParentContext()
    {
        return GlobalIssueContext.getInstance();
    }

    @Override
    public boolean hasParentContext()
    {
        return true;
    }


    // -------------------------------------------------------------------------------------------------- Helper Methods
    @Override
    public Map<String, Object> appendToParamsMap(final Map<String, Object> input)
    {
        return MapBuilder.newBuilder(input).add(FIELD_PROJECT_CATEGORY, getProjectCategoryObject() != null ? getProjectCategoryObject().getId() : null).add(
            FIELD_PROJECT, null).toMap();
        //        props.put(FIELD_ISSUE_TYPE, null);
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    @Override
    public Project getProjectObject()
    {
        return null;
    }

    @Override
    public GenericValue getProject()
    {
        return null;
    }

    public Long getProjectId()
    {
        return null;
    }

    @Override
    public GenericValue getProjectCategory()
    {
        return projectCategory == null ? null : treeManager.getProjectManager().getProjectCategory(projectCategory.getId());
    }

    @Override
    public ProjectCategory getProjectCategoryObject()
    {
        return projectCategory;
    }

    @Override
    public IssueType getIssueTypeObject()
    {
        return null;
    }

    @Override
    public GenericValue getIssueType()
    {
        return null;
    }

    public String getIssueTypeId()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return "ProjectCategoryContext[projectCategoryId=" + projectCategory + ']';
    }
}
