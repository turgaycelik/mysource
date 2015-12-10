/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * This function will store the current issue (if it exists) and update the cache.
 *
 * Note: This function will not create a change history, or even set the updated timestamp. It ONLY stores.
 *
 * @see GenerateChangeHistoryFunction
 */
public class IssueStoreFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(IssueStoreFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        Issue issue = (Issue) transientVars.get("issue");

        if (issue == null)
        {
            log.warn("Issue is null - cannot store.");
            return;
        }

        try
        {
            issue.store();
        }
        catch (Exception e)
        {
            log.error("An exception occurred", e);
        }
    }

    public static FunctionDescriptor makeDescriptor()
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        descriptor.getArgs().put("class.name", IssueStoreFunction.class.getName());
        return descriptor;
    }
}
