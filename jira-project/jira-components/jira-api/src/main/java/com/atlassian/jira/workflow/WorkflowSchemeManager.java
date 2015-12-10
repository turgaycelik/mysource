package com.atlassian.jira.workflow;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.user.ApplicationUser;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public interface WorkflowSchemeManager extends SchemeManager
{
    String getSchemeEntityName();

    String getEntityName();

    String getAssociationType();

    String getSchemeDesc();

    GenericValue getWorkflowScheme(GenericValue project) throws GenericEntityException;

    GenericValue getDefaultEntity(GenericValue scheme) throws GenericEntityException;

    AssignableWorkflowScheme getDefaultWorkflowScheme();

    List<GenericValue> getNonDefaultEntities(GenericValue scheme) throws GenericEntityException;

    /**
     * @return A collection of all workflow names currently active (ie assigned to schemes & associated with projects).
     */
    Collection<String> getActiveWorkflowNames() throws GenericEntityException, WorkflowException;

    void addWorkflowToScheme(GenericValue scheme, String workflowName, String issueTypeId)
            throws GenericEntityException;

    /**
     * Updates Workflow Schemes's such that schemes associated to the workflow with name oldWorkflowName will be changed
     * to newWorkflowName.
     * <p/>
     * Note: There is no validation performed by this method to determine if the provided oldWorkflowName or
     * newWorkflowName are valid workflow names or if the workflow is active/inactive. These validations must be done by
     * the caller.
     *
     * @param oldWorkflowName name of the workflow to re-assign all its associated schemes from
     * @param newWorkflowName name of the workflow to assign all the schemes associated to targetWorkflow
     */
    void updateSchemesForRenamedWorkflow(String oldWorkflowName, String newWorkflowName);

    /**
     * Returns all workflow schemes that the passed workflow is assigned to, not including draft schemes.
     * @param workflow the workflow whose schemes must be returned
     * @return workflow schemes that the passed workflow is assigned to, not including draft schemes.
     */
    Collection<GenericValue> getSchemesForWorkflow(JiraWorkflow workflow);

    /**
     * Returns all workflow schemes that the passed workflow is assigned to, including draft schemes.
     * @param workflow the workflow whose schemes must be returned
     * @return workflow schemes that the passed workflow is assigned to, including draft schemes.
     */
    Iterable<WorkflowScheme> getSchemesForWorkflowIncludingDrafts(JiraWorkflow workflow);

    void clearWorkflowCache();

    /**
     * Returns a map representation of a workflow scheme for a passed project. The returned map stores {issuetype ->
     * workflowName}. A null issuetype points out the default workflow for the scheme.
     *
     * @param project the project whose scheme should be returned.
     * @return the map representation of a workflow scheme. Each key represents an issuetype which its associated value
     *         the name of the workflow assigned to that issue type. A null issuetype points out the default workflow
     *         for that scheme.
     */
    Map<String, String> getWorkflowMap(Project project);

    /**
     * Get the name of the workflow associated with the passed project and issue type.
     *
     * @param project the project used in the search.
     * @param issueType the issue type used in the search.
     * @return the name of the workflow associated with the passed project and issue type.
     */
    String getWorkflowName(Project project, String issueType);

    /**
     * Get the name of the workflow from the passed scheme associated with the passed issue type.
     *
     * @param scheme the scheme to search.
     * @param issueType the issue type used in the search.
     * @return the name of the workflow associated with the scheme and issue type.
     */
    String getWorkflowName(GenericValue scheme, String issueType);

    /**
     * Tells the caller if the passed project is using the default workflow scheme.
     *
     * @param project the project to check.
     * @return true if the passed project is using the default scheme, false otherwise.
     */
    boolean isUsingDefaultScheme(Project project);

    /**
     * Tells the caller if the passed workflow scheme has a draft.
     *
     * @param workflowScheme the workflow scheme to check. It is illegal to pass a draft workflow scheme to this method.
     * @return true if the passed workflow has a draft false otherwise.
     */
    boolean hasDraft(@Nonnull AssignableWorkflowScheme workflowScheme);

    /**
     * Return a builder that can be used to create a new {@link AssignableWorkflowScheme}.
     *
     * @return the builder that can be used to create the new workflow scheme.
     */
    AssignableWorkflowScheme.Builder assignableBuilder();

    /**
     * Return a builder that can be used to create a new {@link DraftWorkflowScheme} for the passed workflow scheme.
     *
     * @return the builder that can be used to create the new workflow scheme.
     */
    DraftWorkflowScheme.Builder draftBuilder(AssignableWorkflowScheme parent);

    /**
     * Create a new workflow scheme.
     *
     * @param workflowScheme the workflow scheme to create a draft of. It cannot be a draft, the default scheme, a scheme
     * that already has a draft or a scheme that is not already in the database (i.e. does not have an ID).
     *
     * @return the new draft scheme.
     */
    @Nonnull
    AssignableWorkflowScheme createScheme(@Nonnull AssignableWorkflowScheme workflowScheme);

    /**
     * Create a draft for the passed workflow scheme. It is illegal to pass:
     * <ul>
     *     <li>A scheme that already has a draft.</li>
     *     <li>A scheme that is not currently in the database.</li>
     *     <li>The default workflow scheme</li>
     *     <li>A draft workflow scheme.</li>
     * </ul>
     *
     *
     *
     * @param creator the user that is going to create the draft. This user is recorded as the person who last
     * modified the new draftscheme.
     * @param workflowScheme the workflow scheme to create a draft of. It cannot be a draft, the default scheme, a scheme
     * that already has a draft or a scheme that is not already in the database (i.e. does not have an ID).
     *
     * @return the new draft scheme.
     */
    @Nonnull
    DraftWorkflowScheme createDraftOf(ApplicationUser creator, @Nonnull AssignableWorkflowScheme workflowScheme);

    /**
     * Create the passed draft workflow scheme. A draft can be created using the {@link #draftBuilder(AssignableWorkflowScheme)}
     * method.
     *
     * <ul>
     *     <li>A scheme that already has a draft.</li>
     *     <li>A scheme that is not currently in the database.</li>
     *     <li>The default workflow scheme</li>
     *     <li>A draft workflow scheme.</li>
     * </ul>

     * @param creator the user that is going to create the draft. This user is recorded as the person who last
     * modified the new draftscheme.
     * @param workflowScheme the workflow scheme to create.
     *
     * @return the new draft scheme.
     */
    @Nonnull
    DraftWorkflowScheme createDraft(ApplicationUser creator, @Nonnull DraftWorkflowScheme workflowScheme);

    /**
     * Return all the assignable workflow schemes.
     *
     * @return a list of all the assignable workflow schemes.
     */
    @Nonnull
    Iterable<AssignableWorkflowScheme> getAssignableSchemes();

    /**
     * Return the workflow scheme with the passed id.
     *
     * @param id the id to search.
     * @return the workflow scheme with the given id or null if no such scheme exists.
     */
    @Nullable
    AssignableWorkflowScheme getWorkflowSchemeObj(long id);

    /**
     * Return the workflow scheme with the passed name.
     *
     * @param name the name to search.
     * @return the workflow scheme with the given name or null if no such scheme exists.
     */
    @Nullable
    AssignableWorkflowScheme getWorkflowSchemeObj(String name);

    /**
     * Return the workflow scheme associated with the passed project.
     *
     * @param project the project whose scheme is to be returned.
     * @return the scheme the passed project is using. Never null.
     */
    @Nonnull
    AssignableWorkflowScheme getWorkflowSchemeObj(@Nonnull Project project);

    /**
     * Return the draft workflow scheme for the passed workflow scheme.
     *
     * @param workflowScheme the workflow scheme whose draft is being sought.
     * @return the draft of the passed workflow scheme or null if it does not exist.
     */
    DraftWorkflowScheme getDraftForParent(@Nonnull AssignableWorkflowScheme workflowScheme);

    /**
     * Return the draft workflow scheme with the given id.
     *
     * @param id the id of the draft workflow scheme to be returned
     * @return the draft with the given id or null if it does not exist.
     */
    DraftWorkflowScheme getDraft(long id);

    /**
     * Return the original workflow scheme for the passed draft workflow scheme.
     *
     * @param draftSchemeId the id of the draft workflow scheme whose parent is being sought.
     * @return the parent of the passed draft workflow scheme.
     */
    AssignableWorkflowScheme getParentForDraft(long draftSchemeId);

    /**
     * Return true if the passed workflow scheme is being used by a project.
     *
     * @param workflowScheme the workflow scheme to test.
     * @return true if the passed workflow scheme us being used by a project; false otherwise.
     */
    boolean isActive(@Nonnull WorkflowScheme workflowScheme);

    /**
     * Delete the passed workflow scheme. It is illegal to delete an active scheme, the default
     * scheme or a scheme that is not already in the database.
     *
     * @param scheme the scheme to delete. Cannot be the default scheme, active scheme or a scheme that is not already
     * in the database.
     *
     * @return true if the scheme was deleted; false otherwise.
     */
    boolean deleteWorkflowScheme(@Nonnull WorkflowScheme scheme);

    /**
     * Save changes to the passed draft workflow scheme.
     *
     * @param user the user making the changes.
     * @param scheme the draft scheme to change.
     * @return the draft scheme as now stored in the database.
     */
    DraftWorkflowScheme updateDraftWorkflowScheme(ApplicationUser user, @Nonnull DraftWorkflowScheme scheme);

    /**
     * Save changes to the passed workflow scheme.
     *
     *
     * @param scheme the scheme to change.
     * @return the scheme that is now stored in the database.
     */
    AssignableWorkflowScheme updateWorkflowScheme(@Nonnull AssignableWorkflowScheme scheme);

    /**
     * Return the list of projects that use the passed workflow scheme.
     *
     * @param workflowScheme the workflow scheme to check.
     * @return the list of projects that use the passed workflow scheme.
     */
    @Nonnull
    List<Project> getProjectsUsing(@Nonnull AssignableWorkflowScheme workflowScheme);

    /**
     * If the project's workflow scheme is only used by one project and if this scheme has a draft,
     * then the draft is copied to a separate scheme and deleted.
     *
     * @param project project who's workflow scheme draft is to be copied to a separate scheme.
     * @param user the user making the changes.
     * @return copied scheme or null if it was not created.
     */
    AssignableWorkflowScheme cleanUpSchemeDraft(Project project, User user);

    AssignableWorkflowScheme copyDraft(DraftWorkflowScheme draft, User user, String newDescription);

    void replaceSchemeWithDraft(DraftWorkflowScheme draft);

    /**
     * If the passed workflow scheme is currently being edited, then this method blocks until the editing is finished.
     * Executes the passed Callable task in the end and returns its result. Workflow scheme editing will be blocked
     * until the task execution finishes.
     *
     * @param scheme the workflow scheme.
     * @param task task to execute.
     * @param <T> return type of the task.
     * @return the value returned by the passed task.
     * @throws Exception any exception occurred during task execution.
     */
    <T> T waitForUpdatesToFinishAndExecute(AssignableWorkflowScheme scheme, Callable<T> task) throws Exception;
}