package com.atlassian.jira.mock.issue.search;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.project.Project;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple search context for testing. Most methods don't actually work.
 *
 * @since v4.0
 */
public class MockSearchContext implements SearchContext
{
    private List<Project> projects;

    public MockSearchContext()
    {
        projects = Collections.emptyList();
    }

    public MockSearchContext(Project ... projects)
    {
        this.projects = new ArrayList<Project>();
        this.projects.addAll(Arrays.asList(projects));
    }

    public boolean isForAnyProjects()
    {
        return projects.isEmpty();
    }

    public boolean isForAnyIssueTypes()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isSingleProjectContext()
    {
        return 1 == projects.size();
    }

    @Override
    public Project getSingleProject()
    {
        return projects.get(0);
    }

    public List getProjectCategoryIds()
    {
        throw new UnsupportedOperationException();
    }

    public List<Long> getProjectIds()
    {
        return Lists.transform(projects, new Function<Project, Long>()
        {
            @Override
            public Long apply(@Nullable Project project)
            {
                return project.getId();
            }
        });
    }

    public GenericValue getOnlyProject()
    {
        return getSingleProject().getGenericValue();
    }

    public List<String> getIssueTypeIds()
    {
        throw new UnsupportedOperationException();
    }

    public List<IssueContext> getAsIssueContexts()
    {
        throw new UnsupportedOperationException();
    }

    public void verify()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Project> getProjects()
    {
        return Lists.newArrayList(projects);
    }

    @Override
    public List<IssueType> getIssueTypes()
    {
        throw new UnsupportedOperationException();
    }
}
