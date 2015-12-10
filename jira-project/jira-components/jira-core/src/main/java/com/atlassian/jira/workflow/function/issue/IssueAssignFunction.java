/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.FunctionProvider;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 *
 *
 * @author <a href="mailto:plightbo@atlassian.com">Pat Lightbody</a>
 */
public class IssueAssignFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(IssueAssignFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        String assignee = (String) transientVars.get("assignee");
        if (TextUtils.stringSet(assignee))
            issue.setAssigneeId(assignee);
        else
            issue.setAssignee(null);
    }
}
