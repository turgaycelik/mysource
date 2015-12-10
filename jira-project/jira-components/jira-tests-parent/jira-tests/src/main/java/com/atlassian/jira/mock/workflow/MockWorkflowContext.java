/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock.workflow;

import com.opensymphony.workflow.WorkflowContext;

public class MockWorkflowContext implements WorkflowContext
{
    String caller;

    public MockWorkflowContext(String caller)
    {
        this.caller = caller;
    }

    public String getCaller()
    {
        return caller;
    }

    public void setRollbackOnly()
    {
        throw new UnsupportedOperationException();
    }
}
