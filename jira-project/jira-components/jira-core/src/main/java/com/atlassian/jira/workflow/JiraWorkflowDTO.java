package com.atlassian.jira.workflow;

/**
 * Used as a simple transfer object that the workflow store can pass back. This allows us to not
 * build a heavy-weight object such as a JiraWorkflow from the Store.
 *
 * @since v3.13
 */
public interface JiraWorkflowDTO
{
    Long getId();

    ImmutableWorkflowDescriptor getDescriptor();

    String getName();
}
