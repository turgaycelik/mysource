/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.atlassian.jira.user.ApplicationUser;

import com.opensymphony.module.propertyset.PropertySet;

import static com.atlassian.jira.component.ComponentAccessor.getPermissionManager;
import static com.atlassian.jira.workflow.WorkflowDescriptorUtil.resolvePermissionKey;

public class PermissionCondition extends AbstractJiraCondition
{
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        ProjectPermissionKey key = resolvePermissionKey(args);
        PermissionManager permissionManager = getPermissionManager();

        // Pass the check if the permission doesn't exist.
        if (!permissionManager.getProjectPermission(key).isDefined())
        {
            return true;
        }

        ApplicationUser caller = getCallerUser(transientVars, args);
        Issue issue = getIssue(transientVars);
        return permissionManager.hasPermission(key, issue, caller);
    }
}
