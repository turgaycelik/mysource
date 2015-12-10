package com.atlassian.jira.sharing.type;

import com.atlassian.jira.issue.comparator.ProjectNameComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleComparator;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Compares to Project share types.
 *
 * @since v3.13
 */
public class ProjectSharePermissionComparator extends DefaultSharePermissionComparator
{
    private final ProjectManager projectManager;
    private final ProjectRoleManager projectRoleManager;

    public ProjectSharePermissionComparator(final ProjectManager projectManager, final ProjectRoleManager projectRoleManager)
    {
        super(ProjectShareType.TYPE);

        Assertions.notNull("projectManager", projectManager);
        Assertions.notNull("projectRoleManager", projectRoleManager);
        this.projectManager = projectManager;
        this.projectRoleManager = projectRoleManager;
    }

    @Override
    public int comparePermissions(final SharePermission perm1, final SharePermission perm2)
    {
        int compareResult = compareNull(perm1.getParam1(), perm2.getParam1());
        if ((compareResult == 0) && (perm1.getParam1() != null))
        {
            final Project project1 = projectManager.getProjectObj(new Long(perm1.getParam1()));
            final Project project2 = projectManager.getProjectObj(new Long(perm2.getParam1()));
            compareResult = ProjectNameComparator.COMPARATOR.compare(project1, project2);
            if (compareResult == 0)
            {
                compareResult = compareNull(perm1.getParam2(), perm2.getParam2());
                if ((compareResult == 0) && (perm1.getParam2() != null))
                {
                    final ProjectRole role1 = projectRoleManager.getProjectRole(new Long(perm1.getParam2()));
                    final ProjectRole role2 = projectRoleManager.getProjectRole(new Long(perm2.getParam2()));
                    compareResult = ProjectRoleComparator.COMPARATOR.compare(role1, role2);
                }
            }
        }
        return compareResult;
    }
}
