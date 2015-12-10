/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 6:04:20 PM
 */
package com.atlassian.jira.workflow;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;

import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.collect.CollectionBuilder.list;

/**
 * Domain object representing the permitted states and transitions of issues.
 */
@PublicApi
public interface JiraWorkflow extends Comparable<JiraWorkflow>
{
    String JIRA_META_ATTRIBUTE_KEY_PREFIX = "jira.";
    String JIRA_META_ATTRIBUTE_PERMISSION = "jira.permission";
    String JIRA_META_ATTRIBUTE_EDIT_ALLOWED = "jira.issue.editable";
    String JIRA_META_ATTRIBUTE_EXCLUDE_RESOLUTION = "jira.field.resolution.exclude";
    String JIRA_META_ATTRIBUTE_INCLUDE_RESOLUTION = "jira.field.resolution.include";
    String JIRA_META_ATTRIBUTE_I18N = "jira.i18n.title";
    String JIRA_META_ATTRIBUTE_I18N_SUBMIT = "jira.i18n.submit";

    /**
     * Key used to store the last modifications author in the workflow xml
     *
     * @deprecated Use {@link #JIRA_META_UPDATE_AUTHOR_KEY} instead. Since v6.0.
     */
    String JIRA_META_UPDATE_AUTHOR_NAME = "jira.update.author.name";

    /* Key used to store the last modifications author in the workflow xml*/
    String JIRA_META_UPDATE_AUTHOR_KEY = "jira.update.author.key";

    /**
     * Key used to store the last modification date in the workflow xml
     */
    String JIRA_META_UPDATED_DATE = "jira.updated.date";
    /**
     * Allowed 'jira.*' prefixes for workflow properties.
     */
    String[] JIRA_META_ATTRIBUTE_ALLOWED_LIST = new String[] { JIRA_META_ATTRIBUTE_PERMISSION, JIRA_META_ATTRIBUTE_EDIT_ALLOWED, JIRA_META_ATTRIBUTE_I18N, JIRA_META_ATTRIBUTE_EXCLUDE_RESOLUTION, JIRA_META_ATTRIBUTE_INCLUDE_RESOLUTION, JIRA_META_ATTRIBUTE_I18N_SUBMIT };

    String STEP_STATUS_KEY = JIRA_META_ATTRIBUTE_KEY_PREFIX + "status.id";
    String WORKFLOW_DESCRIPTION_ATTRIBUTE = JIRA_META_ATTRIBUTE_KEY_PREFIX + "description";
    String WORKFLOW_VIEW_FIELDLAYOUT_KEY_ATTRIBUTE = JIRA_META_ATTRIBUTE_KEY_PREFIX + "view.fieldlayout.key";

    String DEFAULT_WORKFLOW_NAME = "jira";

    String ACTION_TYPE_INITIAL = "initial";
    String ACTION_TYPE_GLOBAL = "global";
    String ACTION_TYPE_COMMON = "common";
    String ACTION_TYPE_ORDINARY = "ordinary";
    static final String DRAFT = "draft";
    static final String LIVE = "live";
    Collection<String> ACTION_TYPE_ALL = list(ACTION_TYPE_INITIAL, ACTION_TYPE_GLOBAL, ACTION_TYPE_COMMON, ACTION_TYPE_ORDINARY);

    /**
     * May be used as the destination step in common actions, if the action should not result in a step change.
     */
    static final int ACTION_ORIGIN_STEP_ID = -1;

    String getName();

    String getDisplayName();

    String getDescription();

    WorkflowDescriptor getDescriptor();

    /**
     * Get all the actions in this workflow, global, common and from steps.
     * @return A collection of {@link ActionDescriptor}s.
     */
    Collection<ActionDescriptor> getAllActions();

    /**
     * Get all the actions which have a particular step as their unconditional result.
     * @return all the actions which have a particular step as their unconditional result.
     */
    Collection<ActionDescriptor> getActionsWithResult(StepDescriptor stepDescriptor);

    /**
     * Remove a step from the workflow.
     * <p>
     * This method will also remove all actions with this step ID as their unconditional result.
     * @return <code>true</code> if the remove was successful
     */
    boolean removeStep(StepDescriptor stepDescriptor);

    /**
     * Get the StepDescriptor linked to the given status for this workflow.
     *
     * @param status the Status
     * @return The StepDescriptor linked, or null if no steps are linked to this status.
     *
     * @deprecated Use {@link #getLinkedStep(com.atlassian.jira.issue.status.Status)} instead. Since v5.0.
     */
    StepDescriptor getLinkedStep(GenericValue status);

    /**
     * Get the StepDescriptor linked to the given status for this workflow.
     *
     * @param status the Status
     * @return The StepDescriptor linked, or null if no steps are linked to this status.
     */
    StepDescriptor getLinkedStep(Status status);

    /**
     * Returns all statuses for this workflow
     * @return a {@link List} of {@link GenericValue}
     * @deprecated Use {@link #getLinkedStatusObjects()} instead. Since v5.0.
     */
    List<GenericValue> getLinkedStatuses();

    /**
     * Returns all statuses for this workflow
     * @return a {@link List} of {@link com.atlassian.jira.issue.status.Status} objects
     */
    List<Status> getLinkedStatusObjects();

    /**
     * Returns all status ids for this workflow
     * @return a {@link Set} of {@link java.lang.String} status id's
     */
    Set<String> getLinkedStatusIds();

    /**
     * Determine whether this workflow object is currently active.
     * @return <code>true</code> if this workflow object is active.
     * @throws WorkflowException Runtime Exception indicating a problem in the WorkflowManager.
     */
    boolean isActive() throws WorkflowException;

    /**
     * Determines if the workflow is loaded from XML or the database
     * @return <code>true</code> if this workflow object is the uneditable system workflow.
     * @throws WorkflowException Runtime Exception indicating a problem in the WorkflowManager.
     */
    boolean isSystemWorkflow() throws WorkflowException;

    /**
     * Determines if the workflow can be modifed within JIRA.
     * <p>
     * System workflows are never editable.
     * The "published" version of an active workflow is not editable, but the draft version is.
     * Inactive workflows are editable.
     * </p>
     * @return <code>true</code> if this workflow is editable.
     * @throws WorkflowException Runtime Exception indicating a problem in the WorkflowManager.
     */
    boolean isEditable() throws WorkflowException;

    /**
     * Determines if the workflow is the default JIRA workflow
     * @return <code>true</code> if this is the workflow is the default JIRA workflow.
     */
    boolean isDefault();

    /**
     * Determines if the workflow is an draft edit of an active workflow.
     * @since v3.13
     * @return <code>true</code> if an draft workflow, false otherwise.
     */
    boolean isDraftWorkflow();

    /**
     * Determines if this workflow has a draft edit copy.
     * @since v3.13
     * @return <code>true</code> if this workflow has a draft edit copy.
     */
    boolean hasDraftWorkflow();

    /**
     * Get the next available action id
     * @return The next available action id.
     */
    int getNextActionId();

    /**
     * Returns a collection of all step descriptors that reference the given common action.
     * @return a collection of all step descriptors that reference the given common action.
     */
    Collection<StepDescriptor> getStepsForTransition(ActionDescriptor action);

    /**
     * Returns all post-functions of the transition, including the ones on all conditional results,
     * unconditional results and 'global' (non-result) postfunctions.
     * @return all post-functions of the transition, including the ones on all conditional results,
     * unconditional results and 'global' (non-result) postfunctions.
     */
    Collection<FunctionDescriptor> getPostFunctionsForTransition(ActionDescriptor actionDescriptor);

    boolean isInitialAction(ActionDescriptor actionDescriptor);

    boolean isCommonAction(ActionDescriptor actionDescriptor);

    boolean isGlobalAction(ActionDescriptor actionDescriptor);

    boolean isOrdinaryAction(ActionDescriptor actionDescriptor);

    /**
     * Get the Status associated with the given StepDescriptor.
     *
     * @param stepDescriptor the StepDescriptor
     * @return the Status associated with the given StepDescriptor.
     * @deprecated Use {@link #getLinkedStatusObject(com.opensymphony.workflow.loader.StepDescriptor)} instead. Since
     *             v5.0.
     */
    GenericValue getLinkedStatus(StepDescriptor stepDescriptor);

    /**
     * Get the Status associated with the given StepDescriptor.
     *
     * @param stepDescriptor the StepDescriptor
     * @return the Status associated with the given StepDescriptor.
     */
    Status getLinkedStatusObject(StepDescriptor stepDescriptor);

    /**
     * Get the id of the {@link Status} associated with the given {@link StepDescriptor}
     *
     * @param stepDescriptor the StepDescriptor
     * @return the id of the status associated with the given StepDescriptor.
     */
    String getLinkedStatusId(StepDescriptor stepDescriptor);

    String getActionType(ActionDescriptor actionDescriptor);

    void reset();

    Collection<ActionDescriptor> getActionsForScreen(FieldScreen fieldScreen);

    /**
     * Returns the most recent authors username.
     * @since v3.13
     * @return Returns the authors username
     */
    String getUpdateAuthorName();

    /**
     * Returns the most recent author</p> <b>Notice:</b> This method will also return proxy user even when is not
     * existing. Please use {@link com.atlassian.jira.user.util.UserManager#isUserExisting(com.atlassian.jira.user.ApplicationUser)}
     * if you want to check user's existence.
     * @return The update author.
     * @since v6.0
     */
    ApplicationUser getUpdateAuthor();

    /**
     * Returns the date of the most recent update to this workflow.
     *
     * @return date of the most recent update to this workflow
     * @since v3.13
     */
    Date getUpdatedDate();

    /**
     * Returns either {@link #DRAFT} or {@link #LIVE} depending on the workflow implementation.
     *
     * @return {@link #DRAFT} or {@link #LIVE}
     * @since v3.13
     */
    String getMode();
}