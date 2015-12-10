package com.atlassian.jira.workflow;

import java.util.Map;

/**
 * @since v5.2
 */
interface WorkflowSchemeStore<T extends WorkflowSchemeStore.State>
{
    T create(T state);
    T update(T state);
    boolean delete(long id);
    boolean delete(T state);

    T get(long id);
    Iterable<T> getAll();

    boolean renameWorkflow(String oldName, String newName);

    Iterable<T> getSchemesUsingWorkflow(JiraWorkflow jiraWorkflow);

    interface State
    {
        String getDefaultWorkflow();
        Long getId();
        Map<String, String> getMappings();

        interface Builder<B extends Builder<B>>
        {
            String getDefaultWorkflow();
            Long getId();
            String getDefault();
            Map<String, String> getMappings();

            B setMappings(Map<String, String> mappings);
        }
    }
}
