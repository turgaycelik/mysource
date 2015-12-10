/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * A Condition which passes when the user is the issue's reporter.
 */
public class AllowOnlyReporter extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(AllowOnlyReporter.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        ApplicationUser caller = getCallerUser(transientVars, args);
        ApplicationUser reporter = null;
        try
        {
            reporter = ApplicationUsers.from(getIssue(transientVars).getReporter());
        }
        catch (DataAccessException e)
        {
            log.warn("Could not retrieve reporter with id '" + getIssue(transientVars).getAssigneeId() + "' of issue '" + getIssue(transientVars).getKey() + "'");
        }
        if (caller != null && reporter != null && caller.equals(reporter))
            return true;
        else
            return false;
    }
}
