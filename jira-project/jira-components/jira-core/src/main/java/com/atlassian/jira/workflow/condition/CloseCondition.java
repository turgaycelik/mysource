/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserUtils;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowContext;
import org.apache.log4j.Logger;

import java.util.Map;

public class CloseCondition extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(DisallowIfInStepCondition.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        try
        {
            Boolean close = (Boolean) transientVars.get("close");
            if (!close.booleanValue())
            {
                return false;
            }

            Issue issue = getIssue(transientVars);
            WorkflowContext context = (WorkflowContext) transientVars.get("context");

            String username = context.getCaller();
            User user = null;

            if (username != null)
                user = UserUtils.getUser(username);

            return ComponentAccessor.getPermissionManager().hasPermission(Permissions.CLOSE_ISSUE, issue, user);
        }
        catch (Exception e)
        {
            log.error("Exception: " + e, e);
            return false;
        }
    }
}
