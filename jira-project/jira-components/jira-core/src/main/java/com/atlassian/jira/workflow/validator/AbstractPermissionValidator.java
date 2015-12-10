/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;

import java.util.Map;

import static com.atlassian.jira.component.ComponentAccessor.getJiraAuthenticationContext;
import static com.atlassian.jira.component.ComponentAccessor.getPermissionManager;
import static com.atlassian.jira.workflow.WorkflowDescriptorUtil.resolvePermissionKey;

public abstract class AbstractPermissionValidator implements Validator
{
    @VisibleForTesting
    void hasUserPermission(final Map args, final Map transientVars, final ApplicationUser user)
            throws InvalidInputException
    {
        ProjectPermissionKey key = resolvePermissionKey(args);
        PermissionManager permissionManager = getPermissionManager();
        Option<ProjectPermission> permission = permissionManager.getProjectPermission(key);
        // Pass the check if the permission doesn't exist.
        if (!permission.isDefined())
        {
            return;
        }

        Issue issue = (Issue) transientVars.get("issue");

        // Check Issue permission
        if (!hasUserPermissionForIssue(issue, key, user, permissionManager))
        {
            I18nHelper i18nHelper = getJiraAuthenticationContext().getI18nHelper();
            String permissionName = i18nHelper.getText(permission.get().getNameI18nKey());
            throw new InvalidInputException("User '" + user.getUsername() + "' doesn't have the '" + permissionName + "' permission");
        }
    }

    @SuppressWarnings ("SimplifiableIfStatement")
    @VisibleForTesting
    boolean hasUserPermissionForIssue(Issue issue, ProjectPermissionKey permissionKey, ApplicationUser user,
            PermissionManager permissionManager) throws InvalidInputException
    {
        //Check to see if we have an existing issue in the transient vars, if we do use that, otherwise use the project.
        if (issue.getGenericValue() != null)
        {
            return permissionManager.hasPermission(permissionKey, issue, user);
        }
        else if (issue.getProjectObject() != null)
        {
            return permissionManager.hasPermission(permissionKey, issue.getProjectObject(), user);
        }
        else
        {
            throw new InvalidInputException("Invalid project specified.");
        }
    }

    /**
     * @deprecated Use {@link #hasUserPermission(java.util.Map, java.util.Map, com.atlassian.jira.user.ApplicationUser)}
     *             instead. Since v6.0.
     */
    protected void hasUserPermission(final Map args, final Map transientVars, final User user)
            throws InvalidInputException
    {
        hasUserPermission(args, transientVars, ApplicationUsers.from(user));
    }
}
