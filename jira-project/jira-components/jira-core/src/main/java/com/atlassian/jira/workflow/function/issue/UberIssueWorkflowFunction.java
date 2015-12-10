/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Apr 13, 2004
 * Time: 3:50:36 PM
 */
package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.workflow.function.event.FireIssueEventFunction;
import com.atlassian.jira.workflow.function.misc.CreateCommentFunction;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;

import java.util.Map;

public class UberIssueWorkflowFunction implements FunctionProvider
{
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        new UpdateIssueStatusFunction().execute(transientVars, args, ps);
        new CreateCommentFunction().execute(transientVars, args, ps);
        new GenerateChangeHistoryFunction().execute(transientVars, args, ps);
        new IssueReindexFunction().execute(transientVars, args, ps);

        if (args.containsKey("eventType"))
        {
            new FireIssueEventFunction().execute(transientVars, args, ps);
        }

    }

    public static FunctionDescriptor makeDescriptor(String eventType)
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        descriptor.getArgs().put("class.name", UberIssueWorkflowFunction.class.getName());
        if (eventType != null)
        {
            descriptor.getArgs().put("eventType", eventType);
        }
        return descriptor;
    }

}