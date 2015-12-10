/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow;

import com.opensymphony.workflow.spi.WorkflowEntry;

public class MockWorkflowEntry implements WorkflowEntry
{
    long id;
    String workflowName;
    boolean initialized;

    public MockWorkflowEntry(long id, String workflowName, boolean initalized)
    {
        this.id = id;
        this.workflowName = workflowName;
        this.initialized = initalized;
    }

    public long getId()
    {
        return id;
    }

    public String getWorkflowName()
    {
        return workflowName;
    }

    public boolean isInitialized()
    {
        return initialized;
    }

    public int getState()
    {
        if (isInitialized())
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
}
