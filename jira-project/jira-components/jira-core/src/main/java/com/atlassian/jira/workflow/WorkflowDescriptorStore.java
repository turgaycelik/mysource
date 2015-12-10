package com.atlassian.jira.workflow;

import com.atlassian.jira.exception.DataAccessException;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import java.util.List;

/**
 * This interface provides methods responsible for persisting a {@link com.opensymphony.workflow.loader.WorkflowDescriptor}
 * to the database.
 *
 * @since v3.13
 */
public interface WorkflowDescriptorStore
{
    /**
     * Retrieves a {@link com.opensymphony.workflow.loader.WorkflowDescriptor} from the underlying store.  Returns
     * null if no WorkflowDescriptor can be found
     *
     * @param name The workflow name
     * @return Null or the matching WorkflowDescriptor
     * @throws FactoryException If there's an error constructing the WorkflowDescriptor from its underlying representation
     */
    ImmutableWorkflowDescriptor getWorkflow(String name) throws FactoryException;

    /**
     * Removes a workflow from the underlying store.
     *
     * @param name The workflow name
     * @return true if any records were delete, false otherwise
     */
    boolean removeWorkflow(String name);

    /**
     * Saves or updates a workflowDescriptor.  If the descriptor already exists, and the replace
     * flag is true, an update will be done.  Otherwise, this method will simply create a new value in the database (if
     * none exists yet).
     *
     * @param name The name of the workflow
     * @param workflowDescriptor The {@link com.opensymphony.workflow.loader.WorkflowDescriptor} to save/update in the underlying store
     * @param replace    true if an update should be done, if the workflow already exists, false otherwise
     * @return true if the update was carried out successfully, false otherwise
     * @throws DataAccessException If there was a problem, storing the workflowdescriptor
     */
    boolean saveWorkflow(String name, WorkflowDescriptor workflowDescriptor, boolean replace) throws DataAccessException;

    /**
     * Returns an array of all the workflowNames stored.
     *
     * @return an array of all the workflowNames stored.
     */
    String[] getWorkflowNames();

    /**
     * Returns a list of all the workflows stored in the underlying store.
     * @return A list of {@link com.atlassian.jira.workflow.JiraWorkflowDTO}s
     */
    List<JiraWorkflowDTO> getAllJiraWorkflowDTOs();
}
