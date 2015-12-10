/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.spi.SimpleStep;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:plightbo@atlassian.com">Pat Lightbody</a>
 */
public class UpdateIssueStatusFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(UpdateIssueStatusFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps) throws StoreException
    {
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");

        SimpleStep step = (SimpleStep) ((WorkflowStore) transientVars.get("store")).findCurrentSteps(entry.getId()).get(0);

        WorkflowDescriptor descriptor = (WorkflowDescriptor) transientVars.get("descriptor");
        StepDescriptor stepDescriptor = descriptor.getStep(step.getStepId());

        GenericValue oldStatus = issue.getStatus();
        GenericValue newStatus = ComponentAccessor.getConstantsManager().getStatus((String) stepDescriptor.getMetaAttributes().get("jira.status.id"));

        // Update issue
        issue.setStatusId(newStatus.getString("id"));

        // Generate status change item
        List changeItems = (List) transientVars.get("changeItems");
        if (changeItems == null)
        {
            changeItems = new LinkedList();
        }

        String from = null;
        String fromString = null;
        if (oldStatus != null)
        {
            from = oldStatus.getString("id");
            fromString = oldStatus.getString("name");
        }

        String to = newStatus.getString("id");
        String toString = newStatus.getString("name");

        changeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.STATUS, from, fromString, to, toString));
        transientVars.put("changeItems", changeItems);
    }

    public static FunctionDescriptor makeDescriptor()
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        descriptor.getArgs().put("class.name", UpdateIssueStatusFunction.class.getName());
        return descriptor;
    }
}
