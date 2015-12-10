/*
 * Copyright (c) 2002-2007
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import java.sql.Timestamp;
import java.util.Map;

public class IssueCreateFunction implements FunctionProvider
{

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        try
        {
            FieldManager fieldManager = ComponentAccessor.getFieldManager();

            MutableIssue issue = (MutableIssue) transientVars.get("issue");

            // Initialise the time stamps
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (issue.getCreated() == null)
            {
                issue.setCreated(now);
            }
            if (issue.getUpdated() == null)
            {
                issue.setUpdated(now);
            }

            // Set the issue key
            long incCount = ComponentAccessor.getProjectManager().getNextId(issue.getProjectObject());
            issue.setKey(issue.getProjectObject().getKey() + "-" + incCount);

            // Initialise votes
            if (issue.getVotes() == null)
            {
                issue.setVotes(0L);
            }
            // Initialise watches
            if (issue.getWatches() == null)
            {
                issue.setWatches(0L);
            }

            // Initialise workflow entry
            WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");
            issue.setWorkflowId(entry.getId());

            // Initialise issue's status
            Step step = (Step) ((WorkflowStore) transientVars.get("store")).findCurrentSteps(entry.getId()).get(0);
            WorkflowDescriptor descriptor = (WorkflowDescriptor) transientVars.get("descriptor");
            StepDescriptor stepDescriptor = descriptor.getStep(step.getStepId());
            issue.setStatusId((String) stepDescriptor.getMetaAttributes().get("jira.status.id"));

            // Store the issue
            issue.store();

            // Maybe move this code to the issue.store() method
            Map<String, ModifiedValue> modifiedFields = issue.getModifiedFields();
            for (final String fieldId : modifiedFields.keySet())
            {
                if (fieldManager.isOrderableField(fieldId))
                {
                    OrderableField field = fieldManager.getOrderableField(fieldId);
                    Object newValue = modifiedFields.get(fieldId).getNewValue();
                    if (newValue != null)
                    {
                        field.createValue(issue, newValue);
                    }
                }
            }
            // Reset the fields as they all have been persisted to the db. Maybe move this code to the "createValue"
            // method of the issue, so that the field removes itself from the modified list as soon as it is persisted.
            issue.resetModifiedFields();
        }
        catch (StoreException e)
        {
            throw new WorkflowException(e);
        }
    }

    public static FunctionDescriptor makeDescriptor()
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        //noinspection unchecked
        descriptor.getArgs().put("class.name", IssueCreateFunction.class.getName());
        return descriptor;
    }
}
