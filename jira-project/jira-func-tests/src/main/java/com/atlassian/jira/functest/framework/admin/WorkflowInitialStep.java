package com.atlassian.jira.functest.framework.admin;

/**
 * Represents the 'Initial Action' page functionality
 */
public interface WorkflowInitialStep
{
    WorkflowTransition createTransition();
}
