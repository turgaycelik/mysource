package com.atlassian.jira.project;

import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * @since v3.13
 */
public class MockProjectFactory implements ProjectFactory
{
    public MockProjectFactory()
    {
    }

    public Project getProject(final GenericValue projectGV)
    {
        return new ProjectImpl(projectGV);
    }

    @Nonnull
    public List<Project> getProjects(@Nonnull Collection<GenericValue> projectGVs)
    {
        final List<Project> projects = new ArrayList<Project>(projectGVs.size());
        for (final GenericValue projectGV : projectGVs)
        {
            projects.add(getProject(projectGV));
        }
        return projects;
    }
}
