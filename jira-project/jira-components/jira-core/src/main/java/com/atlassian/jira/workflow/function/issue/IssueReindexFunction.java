/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IndexException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.log4j.Logger;

import java.util.Map;

public class IssueReindexFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(IssueReindexFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        Issue issue = (Issue) transientVars.get("issue");

        try
        {
            ComponentAccessor.getIssueIndexManager().reIndex(issue);
        }
        catch (IndexException e)
        {
            log.error("Could not reindex issue: " + e, e);
        }
    }

    public static FunctionDescriptor makeDescriptor()
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        descriptor.getArgs().put("class.name", IssueReindexFunction.class.getName());
        return descriptor;
    }
}
