package com.atlassian.jira.workflow.names;

import com.atlassian.jira.util.InjectableComponent;

import java.util.Locale;

/**
 * Creates the name to be used for a copy of a given workflow.
 *
 * @since v5.1
 */
@InjectableComponent
public interface WorkflowCopyNameFactory
{
    /**
     * Creates the name to be used for a copy of a workflow based on the name of the workflow to be copied.
     *
     * @param sourceWorkflowName The name of the workflow to be copied.
     *
     * @param locale
     * @return A String containing the name to be used for the copy.
     */
    String createFrom(String sourceWorkflowName, Locale locale);

    public static class UnableToGenerateASuitableWorkflowName extends RuntimeException {}
}
