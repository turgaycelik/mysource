package com.atlassian.jira.workflow;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.user.ApplicationUser;

/**
 * This store is used to persist copies of active workflows.  This
 * is useful when editing an active workflow, without applying any of the changes yet.
 * @since v3.13
 */
public interface DraftWorkflowStore
{
    /**
     * This will retrieve an draft workflow that is related to the named saved workflow. If an
     * draft workflow is not associated with the passed in name this will return null.
     *
     * @param parentWorkflowName of the saved parent workflow.
     * @return workflow that represents the draft "copy" of the named active workflow. This will be of
     *         type {@link JiraDraftWorkflow}.
     * @throws DataAccessException DataAccessException
     */
    JiraWorkflow getDraftWorkflow(String parentWorkflowName) throws DataAccessException;

    /**
     * Creates a temporary workflow linked back to the named active workflow by id. If a temporary
     * workflow already exists, this method will throw an exception.
     *
     * @param author The user making the edit
     * @param parentWorkflow The parent workflow to copy for the draft workflow.
     * @return workflow that represents the draft "copy" of the named active workflow. This will be of
     *         type {@link JiraDraftWorkflow}.
     * @throws DataAccessException if there are any db errors
     * @throws IllegalStateException thrown if you are adding an draft workflow which already exists for the
     *                               provided parent workflow
     * @throws IllegalArgumentException If the username is null
     */
    JiraWorkflow createDraftWorkflow(ApplicationUser author, JiraWorkflow parentWorkflow) throws DataAccessException, IllegalStateException, IllegalArgumentException;

    /**
     * Removes a temporary workflow linked to the workflow name.
     *
     * @param parentWorkflowName of the saved parent workflow.
     * @throws DataAccessException RuntimeException wrapper around a DB Exception.
     * @return True if deleted successfully
     */
    boolean deleteDraftWorkflow(String parentWorkflowName) throws DataAccessException;

    /**
     * Updates a temporary workflow with the one provided for the parent workflow name passed in.
     * Throws an exception, if no temporary workflow exists for the one being updated.
     *
     * @param user is the user updating this draft workflow.
     * @param parentWorkflowName of the saved parent workflow.
     * @param workflow           The {@link JiraWorkflow} to use for the update.
     * @return workflow that represents the draft "copy" of the named active workflow. This will be of
     *         type {@link JiraDraftWorkflow}.
     * @throws DataAccessException RuntimeException wrapper around a DB Exception.
     */
    JiraWorkflow updateDraftWorkflow(ApplicationUser user, String parentWorkflowName, JiraWorkflow workflow) throws DataAccessException;


    /**
     * Updates a temporary workflow with the one provided for the parent workflow name passed in.
     * Throws an exception, if no temporary workflow exists for the one being updated. This will
     * not update the draft author or the draft date.
     *
     * This method should only be used by upgrade tasks. Use {@link #updateDraftWorkflow(ApplicationUser, String, JiraWorkflow)}
     * instead.
     *
     * @param parentWorkflowName of the saved parent workflow.
     * @param workflow           The {@link JiraWorkflow} to use for the update.
     * @return workflow that represents the draft "copy" of the named active workflow. This will be of
     *         type {@link JiraDraftWorkflow}.
     * @throws DataAccessException RuntimeException wrapper around a DB Exception.
     */
    JiraWorkflow updateDraftWorkflowWithoutAudit(String parentWorkflowName, JiraWorkflow workflow) throws DataAccessException;
}
