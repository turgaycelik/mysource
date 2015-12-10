package com.atlassian.jira.plugin.headernav.navlinks.spi;


import com.atlassian.plugins.navlink.spi.Project;
import com.atlassian.plugins.navlink.spi.ProjectManager;
import com.atlassian.plugins.navlink.spi.ProjectNotFoundException;

public class NavlinksProjectManager implements ProjectManager
{
    private final com.atlassian.jira.project.ProjectManager projectManager;

    public NavlinksProjectManager(com.atlassian.jira.project.ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    @Override
    public Project getProjectByKey(final String key) throws ProjectNotFoundException
    {
        com.atlassian.jira.project.Project p = projectManager.getProjectObjByKey(key);
        if (p == null) {
            throw new ProjectNotFoundException(key);
        } else {
            return new NavlinkProject(key);
        }
    }
}
