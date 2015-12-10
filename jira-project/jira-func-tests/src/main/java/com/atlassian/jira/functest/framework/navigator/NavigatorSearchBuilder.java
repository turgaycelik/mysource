package com.atlassian.jira.functest.framework.navigator;

import java.util.ArrayList;
import java.util.List;

/**
 * Can be used to create NavigatorSearch objects.
 *
 * @since v3.13
 */
public class NavigatorSearchBuilder
{
    private ProjectCondition projectCondition = null;
    private IssueTypeCondition issueTypeCondition = null;
    private QuerySearchCondition queryCondition = null;
    
    public NavigatorSearchBuilder()
    {
    }

    public NavigatorSearchBuilder addProject(String project)
    {
        getProjectCondition().addProject(project);
        return this;
    }

    private ProjectCondition getProjectCondition()
    {
        if (projectCondition == null)
        {
            projectCondition = new ProjectCondition();
        }
        return projectCondition;
    }

    public NavigatorSearchBuilder addIssueType(String type)
    {
        getIssueTypeCondition().addIssueType(type);
        return this;
    }

    public NavigatorSearchBuilder addIssueType(IssueTypeCondition.IssueType type)
    {
        getIssueTypeCondition().addIssueType(type);
        return this;
    }

    private IssueTypeCondition getIssueTypeCondition()
    {
        if (issueTypeCondition == null)
        {
            issueTypeCondition = new IssueTypeCondition();
        }
        return issueTypeCondition;
    }

    public NavigatorSearchBuilder addQueryString(String query)
    {
        getQueryCondition().setQueryString(query);
        return this;
    }

    private QuerySearchCondition getQueryCondition()
    {
        if (queryCondition == null)
        {
            queryCondition = new QuerySearchCondition();
        }

        return queryCondition;

    }

    public NavigatorSearch createSearch()
    {
        List<NavigatorCondition> conditions = new ArrayList<NavigatorCondition>();
        if (projectCondition != null)
        {
            conditions.add(projectCondition.copyCondition());
        }

        if (issueTypeCondition != null)
        {
            conditions.add(issueTypeCondition.copyCondition());
        }

        if (queryCondition != null)
        {
            conditions.add(queryCondition.copyCondition());
        }
        return new NavigatorSearch(conditions);
    }
}
