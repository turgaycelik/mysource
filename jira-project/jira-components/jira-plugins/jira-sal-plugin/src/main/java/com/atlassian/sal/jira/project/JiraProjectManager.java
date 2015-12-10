package com.atlassian.sal.jira.project;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;

import java.util.Collection;
import java.util.HashSet;

/**
 * JIRA implementation of the SAL project manager
 */
public class JiraProjectManager implements com.atlassian.sal.api.project.ProjectManager
{
    private com.atlassian.jira.project.ProjectManager projectManager;
    private ProjectFactory projectFactory;

    public JiraProjectManager(com.atlassian.jira.project.ProjectManager projectManager, ProjectFactory projectFactory)
    {
        this.projectFactory = projectFactory;
        this.projectManager = projectManager;
    }

    /**
     * Get all project keys
     *
     * @return All the project keys
     */
    public Collection<String> getAllProjectKeys()
    {
        Collection<String> results = new HashSet<String>();
        Collection projectGVs = projectManager.getProjects();
        Collection<Project> projects = projectFactory.getProjects(projectGVs);
        for (Project project : projects)
        {
            results.add(project.getKey());
        }
        return results;
    }

}
