package com.atlassian.jira.issue.operation;

import com.opensymphony.workflow.loader.ActionDescriptor;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public interface WorkflowIssueOperation extends IssueOperation
{
    ActionDescriptor getActionDescriptor();
}
