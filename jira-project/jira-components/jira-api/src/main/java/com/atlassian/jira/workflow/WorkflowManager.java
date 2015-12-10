package com.atlassian.jira.workflow;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.user.ApplicationUser;

import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.spi.WorkflowStore;

import org.ofbiz.core.entity.GenericValue;

/**
 * The WorkflowManager is used to interface with the workflow implementation
 */
@PublicApi
public interface WorkflowManager
{
    /**
     * Retrieve all of the workflows in the system
     *
     * @return A Collection of {@link JiraWorkflow} objects.
     */
    public Collection<JiraWorkflow> getWorkflows();

    /**
     * Retrieve all of the workflows in the system including drafts. We return a list as we want to keep workflows and
     * associated drafts are adjacent.
     *
     * @return A List of {@link JiraWorkflow} objects.
     */
    public List<JiraWorkflow> getWorkflowsIncludingDrafts();

    /**
     * Determine whether or not a given workflow is active in the system.
     * <p/>
     * Active workflows are those currently assigned to schemes and associated with projects - they cannot be edited but
     * can be used.
     *
     * @param workflow the JiraWorkflow to check
     * @return true if the given workflow is active
     *
     * @throws WorkflowException RuntimeException wrapper for any errors.
     */
    boolean isActive(JiraWorkflow workflow) throws WorkflowException;

    /**
     * Determine whether or not a given workflow is a system workflow.
     * <p/>
     * Check for a system or XML based workflow - can not be edited.
     *
     * @param workflow the JiraWorkflow to check
     * @return true if the given workflow is a system workflow
     * @throws WorkflowException maybe - but it doesn't look like it?
     */
    boolean isSystemWorkflow(JiraWorkflow workflow) throws WorkflowException;

    /**
     * Retrieve all currently active workflows.
     *
     * @return Collection of JiraWorkflow objects.
     *
     * @throws WorkflowException RuntimeException wrapper for any errors.
     */
    Collection<JiraWorkflow> getActiveWorkflows() throws WorkflowException;

    /**
     * Retrieve a single workflow by name.  The returned {@link com.atlassian.jira.workflow.JiraWorkflow} contains a
     * descriptor that by default isn't mutable.
     * <p/>
     * If you need to edit a workflow, please see {@link #getWorkflowClone(String)}.
     *
     * @param name The workflow name
     * @return A {@link com.atlassian.jira.workflow.JiraWorkflow} that wraps an Immutable WorkflowDescriptor or null
     * @see #getWorkflowClone(String)
     */
    JiraWorkflow getWorkflow(String name);

    /**
     * This method returns a JiraWorkflow, that contains a {@link com.opensymphony.workflow.loader.WorkflowDescriptor}
     * that is mutable.  This method should be called, if you require to edit the workflow.
     * <p/>
     * If you only need to view a workflow, please use {@link #getWorkflow(String)} as it will provide better
     * performance. Cloning a workflow is expensive, as it requires de-constructing and re-constructing the underlying
     * {@link com.opensymphony.workflow.loader.WorkflowDescriptor} from XML.
     *
     * @param name The workflow name
     * @return A {@link com.atlassian.jira.workflow.JiraWorkflow} or null if it doesn't exist.
     * @see #getWorkflow(String)
     */
    JiraWorkflow getWorkflowClone(String name);

    /**
     * Retrieve a single draft workflow by name. If there is not an draft workflow associated with the name this will
     * return null.
     *
     * @param parentWorkflowName is the name of the Saved workflow which identifies then paritally edited draft workflow
     * to retrieve.
     * @return a JiraWorkflow that represents an draft edited workflow.
     * @throws IllegalArgumentException if no parentWorkflow with the name provided can be found.
     * @since v3.13
     */
    JiraWorkflow getDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException;

    /**
     * This will create an draft workflow of the named active workflow. This draft workflow can be used to edit active
     * workflows without overwriting the active workflow immediately. <br/> This method will not allow you to create an
     * draft workflow if the parent workflow is not active, the method will throw an {@link IllegalStateException} in
     * this case.
     *
     * @param parentWorkflowName identifies the parent workflow that should be used as the template for creating the
     * draft workflow.
     * @param username identifies the user performing the action so that we can keep an audit trail of who has last
     * saved the workflow. Empty string for anonymous user.  If null an {@link IllegalArgumentException} will be thrown
     * @return An instance of {@link JiraWorkflow} that represents a copy of the parent that can be edited without
     *         overwriting the active workflow immediately.
     * @throws IllegalStateException thrown if the parentWorkflow is not Active or the draft already exists
     * @throws IllegalArgumentException If the username is null
     * @since v3.13
     * @deprecated Use {@link #createDraftWorkflow(com.atlassian.jira.user.ApplicationUser, String)} instead. Since v6.0.
     */
    JiraWorkflow createDraftWorkflow(String username, String parentWorkflowName)
            throws IllegalStateException, IllegalArgumentException;

    /**
     * This will create an draft workflow of the named active workflow. This draft workflow can be used to edit active
     * workflows without overwriting the active workflow immediately. <br/> This method will not allow you to create an
     * draft workflow if the parent workflow is not active, the method will throw an {@link IllegalStateException} in
     * this case.
     *
     * @param parentWorkflowName identifies the parent workflow that should be used as the template for creating the
     * draft workflow.
     * @param user identifies the user performing the action so that we can keep an audit trail of who has last
     * saved the workflow. Empty string for anonymous user.  If null an {@link IllegalArgumentException} will be thrown
     * @return An instance of {@link JiraWorkflow} that represents a copy of the parent that can be edited without
     *         overwriting the active workflow immediately.
     * @throws IllegalStateException thrown if the parentWorkflow is not Active or the draft already exists
     * @throws IllegalArgumentException If the username is null
     * @since v3.13
     */
    JiraWorkflow createDraftWorkflow(ApplicationUser user, String parentWorkflowName)
            throws IllegalStateException, IllegalArgumentException;

    /**
     * Deletes all draft workflows (there should every be only one) for a given parent workflow.
     *
     * @param parentWorkflowName The parentworkflow for which the draft workflow needs to be deleted.
     * @return true if a draft workflow as deleted, false otherwise.
     * @throws IllegalArgumentException if the parentWorkflowName is null
     * @since v3.13
     */
    boolean deleteDraftWorkflow(String parentWorkflowName) throws IllegalArgumentException;

    /**
     * Retrieve the workflow for a given issue.
     *
     * @param issue the Issue
     * @return the workflow for the given issue.
     *
     * @throws WorkflowException RuntimeException wrapper for any errors.
     * 
     * @deprecated Use {@link #getWorkflow(com.atlassian.jira.issue.Issue)} instead. Since v6.2.
     */
    JiraWorkflow getWorkflow(GenericValue issue) throws WorkflowException;

    /**
     * Retrieve the workflow for a given issue.
     *
     * @param issue the Issue
     * @return the workflow for the given issue.
     *
     * @throws WorkflowException RuntimeException wrapper for any errors.
     */
    JiraWorkflow getWorkflow(Issue issue) throws WorkflowException;

    /**
     * Retrieve the workflow for a given project - issue type pair.
     *
     * @param projectId the Project
     * @param issueTypeId the IssueType ID
     * @return the workflow for the given project - issue type pair.
     *
     * @throws WorkflowException RuntimeException wrapper for any errors.
     */
    JiraWorkflow getWorkflow(Long projectId, String issueTypeId) throws WorkflowException;

    /**
     * Return the workflow in a particular scheme for a given issue type.
     *
     * @param scheme the Scheme
     * @param issueTypeId the IssueType ID
     * @return the workflow for the issue type in the scheme.
     *
     * @throws WorkflowException RuntimeException wrapper for any errors.
     */
    JiraWorkflow getWorkflowFromScheme(GenericValue scheme, String issueTypeId) throws WorkflowException;

    /**
     * Return the workflow in a particular scheme for a given issue type.
     *
     * @param scheme the Scheme
     * @param issueTypeId the IssueType ID
     * @return the workflow for the issue type in the scheme.
     *
     * @throws WorkflowException RuntimeException wrapper for any errors.
     */
    JiraWorkflow getWorkflowFromScheme(WorkflowScheme scheme, String issueTypeId) throws WorkflowException;

    /**
     * Returns all workflows for a given scheme.
     *
     * @param workflowScheme the Workflow Scheme.
     * @return Collection of workflow schemes, empty collection if none exists.
     *
     * @throws WorkflowException RuntimeException wrapper for any errors.
     * @deprecated Since 5.0. Use {@link #getWorkflowsFromScheme(com.atlassian.jira.scheme.Scheme)} instead.
     */
    Collection<JiraWorkflow> getWorkflowsFromScheme(GenericValue workflowScheme) throws WorkflowException;

    /**
     * Returns all workflows for a given scheme.
     *
     * @param workflowScheme the Workflow Scheme.
     * @return Collection of workflow schemes, empty collection if none exists.
     *
     * @throws WorkflowException RuntimeException wrapper for any errors.
     */
    Iterable<JiraWorkflow> getWorkflowsFromScheme(Scheme workflowScheme) throws WorkflowException;

    public JiraWorkflow getDefaultWorkflow() throws WorkflowException;

    /**
     * Create an issue in the database.
     *
     * @param remoteUserName Issue creator
     * @param fields Map of fields. The key is the name of the field, and the type of the value depends on the key. Must
     * include a key "issue", which contains {@link com.atlassian.jira.issue.MutableIssue} object (holding values, not
     * yet persisted to disk). May include other fields (eg "pkey"->String key of project, "originalissueobject"->Issue
     * object) passed onto the workflow engine.
     * @return The created issue GenericValue
     * @throws WorkflowException If any errors occur while trying to create the issue.
     */
    public GenericValue createIssue(String remoteUserName, Map<String, Object> fields) throws WorkflowException;

    public void removeWorkflowEntries(GenericValue issue);

    public void doWorkflowAction(WorkflowProgressAware from);

    /**
     * Get the remote User
     * @param transientVars ??
     * @return Remote user
     * @deprecated Please use {@link WorkflowUtil#getCaller(java.util.Map)} or {@link WorkflowUtil#getCallerName(java.util.Map)}. Deprecated since 4.3
     */
    public User getRemoteUser(Map transientVars);

    public WorkflowStore getStore() throws StoreException;

    /**
     *
     * @param username
     * @param workflow
     * @throws WorkflowException
     * @deprecated Use {@link #createWorkflow(com.atlassian.jira.user.ApplicationUser, JiraWorkflow)} instead. Since v6.0.
     */
    void createWorkflow(String username, JiraWorkflow workflow) throws WorkflowException;

    /**
     *
     * @param user
     * @param workflow
     * @throws WorkflowException
     * @deprecated Use {@link #createWorkflow(com.atlassian.jira.user.ApplicationUser, JiraWorkflow)} instead. Since v6.0.
     */
    void createWorkflow(User user, JiraWorkflow workflow) throws WorkflowException;


    void createWorkflow(ApplicationUser user, JiraWorkflow workflow) throws WorkflowException;

    /**
     * This method will save the workflow and it will not affect the updatedDate and updatedAuthorName meta attributes
     * of the workflow. This should only ever be invoked by system operations such as upgrade tasks. All other saves
     * should use the method {@link #updateWorkflow(String, JiraWorkflow)}
     *
     * @param workflow the workflow to save.
     *
     * @throws WorkflowException RuntimeException wrapper for any errors.
     */
    void saveWorkflowWithoutAudit(JiraWorkflow workflow) throws WorkflowException;

    void deleteWorkflow(JiraWorkflow workflow) throws WorkflowException;

    ActionDescriptor getActionDescriptor(WorkflowProgressAware workflowProgressAware) throws Exception;

    /**
     * Migrates given issue to new workflow and sets new status on it.
     *
     * @param issue issue to migrate
     * @param newWorkflow new workflow
     * @param status new status
     * @throws WorkflowException if migration fails
     */
    public void migrateIssueToWorkflow(MutableIssue issue, JiraWorkflow newWorkflow, Status status)
            throws WorkflowException;

    /**
     * Migrates given issue to new workflow and sets new status on it.
     *
     * @param issue issue to migrate
     * @param newWorkflow new workflow
     * @param status new status
     * @throws WorkflowException if migration fails
     *
     * @deprecated Please use {@link #migrateIssueToWorkflow(com.atlassian.jira.issue.MutableIssue,JiraWorkflow,com.atlassian.jira.issue.status.Status)}.
     *             Since: 3.9.
     */
    void migrateIssueToWorkflow(GenericValue issue, JiraWorkflow newWorkflow, GenericValue status)
            throws WorkflowException;

    /**
     * Migrates given issue to new workflow and sets new status on it. It returns true iff the issue that was migrated
     * needs a reindex. The passed issue is not reindexed even if necessary.
     *
     * @param issue issue to migrate
     * @param newWorkflow new workflow
     * @param status new status
     * @throws WorkflowException if migration fails
     * @return true if the issue
     */
    boolean migrateIssueToWorkflowNoReindex(GenericValue issue, JiraWorkflow newWorkflow, GenericValue status)
            throws WorkflowException;

    /**
     * Prepares {@link Workflow} object with given username as caller
     *
     * @param userName caller username
     * @return created {@link Workflow}
     * @deprecated Use {@link #makeWorkflow(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     */
    Workflow makeWorkflow(String userName);


    /**
     * Prepares {@link Workflow} object with given user as caller
     * @param user caller
     * @return created {@link Workflow}
     * @deprecated Use {@link #makeWorkflow(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     */
    Workflow makeWorkflow(User user);

    /**
     * Prepares {@link Workflow} object with given user as caller
     * @param user caller
     * @return created {@link Workflow}
     */
    Workflow makeWorkflow(ApplicationUser user);

    Workflow makeWorkflowWithUserName(String userName);

    Workflow makeWorkflowWithUserKey(String userKey);

    boolean workflowExists(String name) throws WorkflowException;

    boolean isEditable(Issue issue);

    /**
     * Retrieve a map: actions -> post functions for a workflow.
     *
     * @param workflow The Workflow
     *
     * @return Map of actions-> post functions
     */
    Map<ActionDescriptor, Collection<FunctionDescriptor>> getPostFunctionsForWorkflow(JiraWorkflow workflow);

    /**
     * Gets the first stepId for the given action and workflow name.
     *
     * @param actionDescriptorId id of the ActionDescriptor
     * @param workflowName name of the JiraWorkflow
     *
     * @return the first stepId for the given action and workflow name.
     */
    String getStepId(long actionDescriptorId, String workflowName);

    /**
     * Saves the draft workflow into the active workflow.
     *
     * @param username User will be added to the workflows Audit trail
     * @param workflowName The workflow to be overwritten
     * @deprecated Use {@link #overwriteActiveWorkflow(com.atlassian.jira.user.ApplicationUser, String)} instead. Since v6.0.
     */
    void overwriteActiveWorkflow(String username, String workflowName);

    /**
     * Saves the draft workflow into the active workflow.
     *
     * @param user User will be added to the workflows Audit trail
     * @param workflowName The workflow to be overwritten
     * @since v6.0
     */
    void overwriteActiveWorkflow(ApplicationUser user, String workflowName);

    /**
     * Saves the given JiraWorkflow, which may be either a "live" or "draft" version. This method does not save the
     * changes to the active workflow. Instead, this is meant to be used to make persistent changes to your working
     * edit. Once all changes have been made you can over write the active workflow by calling {@link
     * #overwriteActiveWorkflow(String, String)}
     *
     * @param username User making the request.
     * @param workflow The JiraWorkflow to save.
     * @throws IllegalArgumentException if the workflow or its descriptor is null.
     * @since v3.13
     * @deprecated Use {@link #updateWorkflow(com.atlassian.jira.user.ApplicationUser, JiraWorkflow)} instead. Since v6.0.
     */
    void updateWorkflow(String username, JiraWorkflow workflow);

    /**
     * Saves the given JiraWorkflow, which may be either a "live" or "draft" version. This method does not save the
     * changes to the active workflow. Instead, this is meant to be used to make persistent changes to your working
     * edit. Once all changes have been made you can over write the active workflow by calling {@link
     * #overwriteActiveWorkflow(ApplicationUser, String)}
     *
     * @param user User making the request.
     * @param workflow The JiraWorkflow to save.
     * @throws IllegalArgumentException if the workflow or its descriptor is null.
     * @since v6.0
     */
    void updateWorkflow(ApplicationUser user, JiraWorkflow workflow);

    /**
     * Clones a workflow by creating a deep copy of the workflow provided.
     *
     * @param username The user performing the operation
     * @param clonedWorkflowName The name to store the new workflow with.
     * @param clonedWorkflowDescription The description to store with the cloned workflow. Can be null.
     * @param workflowToClone The workflow to copy.
     * @return A cloned copy of the original workflow.
     * @deprecated Use {@link #copyWorkflow(com.atlassian.jira.user.ApplicationUser, String, String, JiraWorkflow)} instead. Since v6.0.
     */
    JiraWorkflow copyWorkflow(String username, String clonedWorkflowName, String clonedWorkflowDescription, JiraWorkflow workflowToClone);

    /**
     * Clones a workflow by creating a deep copy of the workflow provided.
     *
     * @param user The user performing the operation
     * @param clonedWorkflowName The name to store the new workflow with.
     * @param clonedWorkflowDescription The description to store with the cloned workflow. Can be null.
     * @param workflowToClone The workflow to copy.
     * @return A cloned copy of the original workflow.
     */
    JiraWorkflow copyWorkflow(ApplicationUser user, String clonedWorkflowName, String clonedWorkflowDescription, JiraWorkflow workflowToClone);

    /**
     * Used to change the name and description of an existing worfklow with the given name.
     *
     * @param username The user performing the operation
     * @param currentWorkflow The workflow to update.
     * @param newName The new name to save with the workflow
     * @param newDescription The new description to save with the workflow
     * @deprecated Use {@link #updateWorkflowNameAndDescription(com.atlassian.jira.user.ApplicationUser, JiraWorkflow, String, String)} instead. Since v6.0.
     */
    void updateWorkflowNameAndDescription(String username, JiraWorkflow currentWorkflow, String newName, String newDescription);

    /**
     * Used to change the name and description of an existing worfklow with the given name.
     *
     * @param user The user performing the operation
     * @param currentWorkflow The workflow to update.
     * @param newName The new name to save with the workflow
     * @param newDescription The new description to save with the workflow
     */
    void updateWorkflowNameAndDescription(ApplicationUser user, JiraWorkflow currentWorkflow, String newName, String newDescription);

    /**
     * Provided a set of workflows, this method will check, if the passed in inactive workflows have any drafts linked
     * to them. If they do, it will copy the draft into a new inactive workflow, and then delete the draft.
     *
     * @param user The user performing the operation
     * @param workflows A set of parent {@link com.atlassian.jira.workflow.JiraWorkflow}s
     *
     * @deprecated Since 5.1. This method should not be used directly by external developers as it is an operation
     * that only makes sense in the context of a higher level operation in JIRA (i.e. project removal / changing the
     * workflow scheme of a given project).
     *
     * Please use the API calls for these higher level operations instead.
     */
    void copyAndDeleteDraftWorkflows(User user, Set<JiraWorkflow> workflows);

    /**
     * Provided a set of workflows, this method will check, if the passed in inactive workflows have any drafts linked
     * to them. If they do, it will copy the draft into a new inactive workflow, and then delete the draft.
     *
     * This method is only meant to be used internally in JIRA. External developers should not call this method
     * directly.
     *
     * @param user The user performing the operation
     * @param workflows A set of parent {@link com.atlassian.jira.workflow.JiraWorkflow}s
     */
    @Internal
    void copyAndDeleteDraftsForInactiveWorkflowsIn(User user, Iterable<JiraWorkflow> workflows);

    /**
     * Given an issue and the identifier of a workflow action, returns the next status object for the issue if the action gets executed.
     * @param issue The issue
     * @param actionId The identifier of a workflow action
     * @return The status object corresponding to the next status of the issue if the action got executed
     */
    @Internal
    @Nonnull
    String getNextStatusIdForAction(@Nonnull Issue issue, int actionId);
}
