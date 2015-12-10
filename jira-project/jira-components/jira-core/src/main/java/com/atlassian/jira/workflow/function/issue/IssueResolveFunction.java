/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;

/**
 * @author <a href="mailto:plightbo@atlassian.com">Pat Lightbody</a>
 */
public class IssueResolveFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(IssueResolveFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        String resolution = (String) transientVars.get("resolution");
        Collection fixVersions = (Collection) transientVars.get("fixVersions");

        if (fixVersions != null)
        {
            issue.setFixVersions(fixVersions);
        }

        if (resolution == null)
            issue.setResolution(null);
        else
            issue.setResolution(ComponentAccessor.getConstantsManager().getResolution(resolution));
    }
}
