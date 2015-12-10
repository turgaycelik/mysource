package com.atlassian.jira.plugin.headernav.navlinks.spi;

import com.atlassian.jira.plugin.headernav.customcontentlinks.admin.ProjectAdminPermissionChecker;
import com.atlassian.plugins.navlink.spi.Project;
import com.atlassian.plugins.navlink.spi.ProjectPermissionManager;

public class NavlinksProjectPermissionManager implements ProjectPermissionManager
{
    private final ProjectAdminPermissionChecker projectAdminPermissionChecker;
    private ThreadLocal<Boolean> isSysAdmin = new ThreadLocal<Boolean>();

    public NavlinksProjectPermissionManager(ProjectAdminPermissionChecker projectAdminPermissionChecker) {
        this.projectAdminPermissionChecker = projectAdminPermissionChecker;
    }

    @Override
    public boolean canAdminister(Project project, String userName)
    {
        if (isSysAdmin.get() != null) {
            return true;
        }
        return projectAdminPermissionChecker.canAdminister(project.getKey(), userName);
    }

    // used when this service is used during startup, when there is no request context
    public void setSysAdmin(boolean value) {
        if (value) {
            isSysAdmin.set(true);
        } else {
            isSysAdmin.remove();
        }
    }
}
