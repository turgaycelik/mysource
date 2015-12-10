/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.log4j.Logger;

import java.util.Map;

public class AllowOnlyAssignee extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(AllowOnlyAssignee.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        ApplicationUser caller = getCallerUser(transientVars, args);
        ApplicationUser assignee = null;
        try
        {
            assignee = ApplicationUsers.from(getIssue(transientVars).getAssignee());
        }
        catch (DataAccessException e)
        {
            log.warn("Could not retrieve assignee with id '" + getIssue(transientVars).getAssigneeId() + "' of issue '" + getIssue(transientVars).getKey() + "'");
        }
        if (caller != null && assignee != null && caller.equals(assignee))
            return true;
        else
            return false;
    }

}
