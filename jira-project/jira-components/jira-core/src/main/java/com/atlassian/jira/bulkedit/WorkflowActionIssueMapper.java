/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit;

import com.atlassian.core.util.collection.EasyList;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class WorkflowActionIssueMapper
{
    private String workflowName;
    private Map wfActionsToIssuesMap;
    private Collection actionsWithIssues;

    public WorkflowActionIssueMapper(String workflowName)
    {
        this.workflowName = workflowName;
        wfActionsToIssuesMap = new HashMap();
        actionsWithIssues = new HashSet();
    }

    public String getWorkflowName()
    {
        return workflowName;
    }

    public void setWorkflowName(String workflowName)
    {
        this.workflowName = workflowName;
    }

    // Retrieve the collection of issuekeys associated with the specified status
    public Collection getActionIssuesCollection(String wfaction)
    {
        return (Collection) wfActionsToIssuesMap.get(wfaction);
    }

    public void addIssueToActionMap(String wfaction, String issueKey)
    {
        Collection issueKeys;

        if(wfActionsToIssuesMap.keySet().contains(wfaction))
        {
            issueKeys = (Collection) wfActionsToIssuesMap.get(wfaction);
            issueKeys.add(issueKey);
        }
        else
        {
            issueKeys = EasyList.build(issueKey);
        }

        wfActionsToIssuesMap.put(wfaction, issueKeys);
        actionsWithIssues.add(wfaction);
    }
}
