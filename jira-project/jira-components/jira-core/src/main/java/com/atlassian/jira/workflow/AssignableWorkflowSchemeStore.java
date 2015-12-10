package com.atlassian.jira.workflow;

/**
 * @since v5.2
 */
public interface AssignableWorkflowSchemeStore
        extends WorkflowSchemeStore<AssignableWorkflowSchemeStore.AssignableState>
{
    AssignableState.Builder builder();

    interface AssignableState extends WorkflowSchemeStore.State
    {
        String getName();
        String getDescription();

        Builder builder();

        interface Builder extends State.Builder<Builder>
        {
            String getName();
            String getDescription();

            Builder setName(String name);
            Builder setDescription(String description);

            AssignableState build();
        }
    }
}
