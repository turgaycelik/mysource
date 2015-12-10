package com.atlassian.jira.dev.reference.plugin.security;

import javax.annotation.Nullable;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.security.plugin.ProjectPermissionOverride;
import com.atlassian.jira.user.ApplicationUser;

import static com.atlassian.jira.permission.ProjectPermissions.ADD_COMMENTS;

public class CantCommentProjectPermissionOverride implements ProjectPermissionOverride
{
    @Override
    public Decision hasPermission(final ProjectPermissionKey projectPermissionKey, final Project project, @Nullable final ApplicationUser applicationUser)
    {

        if (applicationUser == null || projectPermissionKey == null)
        {
            return Decision.ABSTAIN;
        }
        else if (applicationUser.getName().equals("brad_the_odlaw") && projectPermissionKey.equals(ADD_COMMENTS))
        {
            return Decision.DENY;
        }
        else
        {
            return Decision.ABSTAIN;
        }

    }

    @Override
    public Reason getReason(final ProjectPermissionKey projectPermissionKey, final Project project, final ApplicationUser applicationUser)
    {
        return hasPermission(projectPermissionKey, project, applicationUser) == Decision.ABSTAIN ?
                new Reason("reference-plugin", "brad.does.have.permissions") :
                new Reason("reference-plugin", "brad.doesnt.have.permissions");
    }
}
