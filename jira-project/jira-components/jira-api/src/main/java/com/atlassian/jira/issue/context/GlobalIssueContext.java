package com.atlassian.jira.issue.context;

import com.atlassian.annotations.PublicApi;
import com.atlassian.bandana.BandanaContext;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

@PublicApi
public class GlobalIssueContext extends AbstractJiraContext
{
    public static final String GLOBAL_CONTEXT_STR = "Global Context";

    private static final GlobalIssueContext INSTANCE = new GlobalIssueContext();

    private GlobalIssueContext() {}


    /**
     * It is both unnecessary and wasteful to construct a new {@code GlobalIssueContext}.
     * Please use the {@link #getInstance()} factory method, instead.
     *
     * @deprecated Use {@link #getInstance()} instead. Since v6.1.
     * @param treeManager ignored
     */
    @Deprecated
    public GlobalIssueContext(final JiraContextTreeManager treeManager)
    {
        this();
    }

    @Override
    public Map<String, Object> appendToParamsMap(final Map<String, Object> input)
    {
        return MapBuilder.newBuilder(input)
                .add(FIELD_PROJECT_CATEGORY, null)
                .add(FIELD_PROJECT, null)
                .toMap();
        // props.put(FIELD_ISSUE_TYPE, null);
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
    public Project getProjectObject()
    {
        return null;
    }

    @Override
    public GenericValue getProject()
    {
        return null;
    }

    @Override
    public Long getProjectId()
    {
        return null;
    }

    @Override
    public GenericValue getProjectCategory()
    {
        return null;
    }

    @Override
    public ProjectCategory getProjectCategoryObject()
    {
        return null;
    }

    @Override
    public boolean isInContext(final IssueContext issueContext)
    {
        return true;
    }

    @Override
    public BandanaContext getParentContext()
    {
        return null;
    }

    @Override
    public boolean hasParentContext()
    {
        return false;
    }

    public static JiraContextNode getInstance()
    {
        return INSTANCE;
    }


    @Override
    public String toString()
    {
        return GLOBAL_CONTEXT_STR;
    }

}
