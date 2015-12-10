/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import org.apache.log4j.Logger;

import java.util.Map;

public class IssueReopenFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(IssueReopenFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        issue.setResolution(null);
    }
}
