/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;

import java.util.Map;

/**
 * Assigns the issue to the reporter.
 */
public class AssignToReporterFunction implements FunctionProvider
{
    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        issue.setAssignee(issue.getReporter());

        // JRA-15120: issue.store() should never have been called in this function, as it can cause the Issue object
        // to be persisted to the database prematurely. However, since it has been here for a while, removing it could
        // break existing functionality for lots of users. But, because an NPE is only thrown when this function is used
        // in the Create step, all we have to do to prevent this error from occuring is check if the issue has already
        // been stored before. If it has, we can call store() to update the issue, which maintains working (albeit
        // incorrect) behaviour. If it hasn't, we defer the store() call, as it should have been implemented initially.
        if (issue.isCreated())
        {
            issue.store();
        }
    }
}
