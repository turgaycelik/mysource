/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.plugin.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.comparator.IssueStatusComparator;
import com.atlassian.jira.issue.status.Status;

import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Factory for configuring a {@link com.atlassian.jira.workflow.condition.SubTaskBlockingCondition} with a set of statuses.
 */
public class WorkflowSubTaskBlockingConditionFactoryImpl extends AbstractWorkflowPluginFactory implements WorkflowPluginConditionFactory
{
    private final ConstantsManager constantsManager;
    private static final String STATUS_ID_PARAM_KEY = "issue_statuses";

    public WorkflowSubTaskBlockingConditionFactoryImpl(ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    protected void getVelocityParamsForInput(Map velocityParams)
    {
        ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
        Collection<Status> statuses = constantsManager.getStatusObjects();
        velocityParams.put("statuses", Collections.unmodifiableCollection(statuses));
    }

    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor)
    {
        getVelocityParamsForInput(velocityParams);
        velocityParams.put("selectedStatuses", getSelectedStatusIds(descriptor));
    }

    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor)
    {
        Collection<String> selectedStatusIds = getSelectedStatusIds(descriptor);
        List selectedStatuses = new LinkedList();
        for (final String statusId : selectedStatusIds)
        {
            selectedStatuses.add(constantsManager.getStatusObject(statusId));
        }

        // Sort the list of statuses so as they are displayed consistently
        Collections.sort(selectedStatuses, new IssueStatusComparator());

        velocityParams.put("statuses", Collections.unmodifiableCollection(selectedStatuses));
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        String[] statusIds = (String[]) conditionParams.get(STATUS_ID_PARAM_KEY);

        if (statusIds != null)
        {
            StringBuilder statIds = new StringBuilder();
            for (String statusId : statusIds)
            {
                statIds.append(statusId).append(",");
            }

            return EasyMap.build("statuses", statIds.substring(0, statIds.length() - 1));
        }
        else
        {
            throw new IllegalArgumentException("Please select at least one status.");
        }
    }

    private Collection<String> getSelectedStatusIds(AbstractDescriptor descriptor)
    {
        Collection<String> selectedStatusIds = new LinkedList<String>();
        if (!(descriptor instanceof ConditionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a ConditionDescriptor.");
        }

        ConditionDescriptor conditionDescriptor = (ConditionDescriptor) descriptor;

        String statuses = (String) conditionDescriptor.getArgs().get("statuses");
        StringTokenizer st = new StringTokenizer(statuses, ",");

        while (st.hasMoreTokens())
        {
            selectedStatusIds.add(st.nextToken());
        }

        return selectedStatusIds;
    }
}
