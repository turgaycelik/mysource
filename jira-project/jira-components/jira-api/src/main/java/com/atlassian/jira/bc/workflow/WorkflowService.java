package com.atlassian.jira.bc.workflow;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;

/**
 * This class is responsible for validation before passing the actual call to the underlying
 * {@link com.atlassian.jira.workflow.WorkflowManager}.
 *
 * @since v3.13
 */
@PublicApi
public interface WorkflowService
{
    /**
     * Given a parentWorkflowName, this methods will retrieved the relevant draft workflow linked to that
     * parent.
     * <br/>
     * If the parentWorkflow doesn't exist, this method returns null and will log an error in the servicecontext
     * errorcollection.  If no DraftWorkflow exists, then this method will simply return null.
     *
     * @param jiraServiceContext service context with user and error collection
     * @param parentWorkflowName The parent workflow name to create an draft copy of.
     * @return A draft workflow or null if no draft workflow is found
     */
    JiraWorkflow getDraftWorkflow(JiraServiceContext jiraServiceContext, String parentWorkflowName);

    /**
     * Creates a copy of an active workflow for editing.  This will not overwrite the current active workflow
     *
     * @param jiraServiceContext service context with user and error collection
     * @param parentWorkflowName The parent workflow name to create an draft copy of.
     * @return A copy of the active workflow
     */
    JiraWorkflow createDraftWorkflow(JiraServiceContext jiraServiceContext, String parentWorkflowName);

    /**
     * Deletes draft workflows (there should only be one) that are associated with the given parent workflow name.
     *
     * @param jiraServiceContext service context with user and error collection
     * @param parentWorkflowName The parent workflow name that the draft workflow to be deleted is associated with
     * @return true if an draft workflow was deleted, false otherwise
     */
    boolean deleteDraftWorkflow(JiraServiceContext jiraServiceContext, String parentWorkflowName);

    /**
     * Deletes the workflow which has the passed name.
     *
     * @param deletingUser user who performs the deletion
     * @param workflowName name of the workflow to be deleted
     * @return the result of the operation.
     */
    ServiceOutcome<Void> deleteWorkflow(ApplicationUser deletingUser, String workflowName);

    /**
     * This method will overwrite the parentWorkflow with a draft Workflow if
     * it exists.  If it doesn't exist, this method will add an error to the errorcollection in the service context.
     * The method will also check for the existance of the parent, and call
     * {@link #validateOverwriteWorkflow(com.atlassian.jira.bc.JiraServiceContext, String)} to ensure the active
     * workflow isn't being overwritten with an invalid draft workflow.
     *
     * @param jiraServiceContext service context with user and error collection
     * @param parentWorkflowName The parent workflow name that will be overwritten with its draft workflow
     */
    void overwriteActiveWorkflow(JiraServiceContext jiraServiceContext, String parentWorkflowName);

    /**
     * Validates that the draft workflow with the given name is allowed to be saved into the corresponding active
     * workflow.
     * Basically you are allowed to add new Steps and change the transitions, but you are not allowed to remove any
     * steps/statuses, or change an existing association between a step ID and an Issue status.
     *
     * @param jiraServiceContext JiraServiceContext
     * @param workflowName       Name of the workflow to be validated.
     */
    void validateOverwriteWorkflow(JiraServiceContext jiraServiceContext, String workflowName);

    /**
     * Updates the workflow descriptor provided in the persistance mechanism implemented.  This method can
     * be used for draft workflows, as well as for copies of active workflows.
     * This method does not save the changes to the active workflow. Instead,
     * this is meant to be used to make persistent changes to your working edit. Once all changes
     * have been made you can over write the active workflow by calling
     * {@link #overwriteActiveWorkflow(com.atlassian.jira.bc.JiraServiceContext, String)}  }
     * <p/>
     *
     * @param jiraServiceContext service context with user and error collection
     * @param workflow           contains the descriptor that the workflow will be updated to.
     * @throws IllegalArgumentException if theworkflow or its descriptor is null. Also thrown if there is no draft
     *                                  workflow associated with the provided parentWorkflowName.
     */
    void updateWorkflow(JiraServiceContext jiraServiceContext, JiraWorkflow workflow);

    /**
     * Validates that the workflow with currentName can have its name and description changed to newWorkflowName and
     * newDescription.
     *
     * @param jiraServiceContext service context with user and error collection
     * @param currentWorkflow        The current workflow to be updated.
     * @param newWorkflowName        The new name to save with the workflow
     */
    void validateUpdateWorkflowNameAndDescription(JiraServiceContext jiraServiceContext, JiraWorkflow currentWorkflow, String newWorkflowName);

    /**
     * Used to change the name and description of an existing worfklow with the given name.
     *
     * @param jiraServiceContext service context with user and error collection
     * @param currentWorkflow    The current workflow to be updated.
     * @param newName            The new name to save with the workflow
     * @param newDescription     The new descriptio to save with the workflow
     */
    void updateWorkflowNameAndDescription(JiraServiceContext jiraServiceContext, JiraWorkflow currentWorkflow, String newName, String newDescription);

    /**
     * Returns the workflow with the given name.
     *
     * @param jiraServiceContext service context with user and error collection
     * @param name               the name of the workflow
     * @return the workflow with the given name.
     */
    JiraWorkflow getWorkflow(JiraServiceContext jiraServiceContext, String name);

    /**
     * Validates if a workflow can be cloned and saved with the provided name. This means checking
     * if the name is set, if it contains invalid characters, and if the workflow already exists.
     *
     * @param jiraServiceContext service context with user and error collection
     * @param newWorkflowName    The name of the cloned workflow.
     */
    void validateCopyWorkflow(JiraServiceContext jiraServiceContext, String newWorkflowName);

    /**
     * Clones and persists a new workflow with the name given.  This will create a complete deep copy of the
     * worfklow provided.
     *
     * @param jiraServiceContext        service context with user and error collection
     * @param clonedWorkflowName        The name to store the cloned workflow with.
     * @param clonedWorkflowDescription The description of the new copy.  May be null.
     * @param workflowToClone           The workflow to clone.
     * @return A clone of the workflow provided.
     */
    JiraWorkflow copyWorkflow(JiraServiceContext jiraServiceContext, String clonedWorkflowName, String clonedWorkflowDescription, JiraWorkflow workflowToClone);

    /**
     * Given a draft workflow and a step Id, this method returns true, if the step does not have any transitions on
     * the original workflow.
     * @param jiraServiceContext        service context with user and error collection
     * @param workflow The draft workflow with the new step
     * @param stepId The stepId of the step to check
     * @return true if the step does not have any transitions on the original workflow.
     */
    boolean isStepOnDraftWithNoTransitionsOnParentWorkflow(JiraServiceContext jiraServiceContext, JiraWorkflow workflow, int stepId);

    /**
     * Validates if a workflow transition can be added to a draft.  If the original workflow for the draft does not
     * have any outgoing transitions, then this method will add an error to the error collection.
     * @param jiraServiceContext service context with user and error collection
     * @param newJiraworkflow The draft workflow to which the transition is being added
     * @param stepId The step to which the transition is being added.
     */
    void validateAddWorkflowTransitionToDraft(JiraServiceContext jiraServiceContext, JiraWorkflow newJiraworkflow, int stepId);
}
