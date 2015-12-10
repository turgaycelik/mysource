package com.atlassian.jira.bc.issue.worklog;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.util.collect.PagedList;

import java.util.List;

/**
 * This is the business layer component that must be used to access all {@link com.atlassian.jira.bc.issue.worklog.WorklogService} functionality.
 * This will perform validation before it hands off to the {@link com.atlassian.jira.issue.worklog.WorklogManager}.
 * Operations will not be performed if validation fails.
 */
@PublicApi
public interface WorklogService
{
    /**
     * Determines whether worklogs are enabled in JIRA and if the user has the required permissions
     * as determined by calling {@link #hasPermissionToDelete(com.atlassian.jira.bc.JiraServiceContext,com.atlassian.jira.issue.worklog.Worklog)}
     * to delete a worklog for this issue.
     *
     * @param jiraServiceContext containing the user who wishes to create a worklog and the errorCollection
     *                           that will contain any errors in calling the method
     * @param worklogId          identifies the worklog that the update validation will occur against
     * @return WorklogResult which can be passed to the delete methods if has permission and the data passed in is valid,
     *         null otherwise
     */
    WorklogResult validateDelete(JiraServiceContext jiraServiceContext, Long worklogId);

    /**
     * Determines whether worklogs are enabled in JIRA and if the user has the required permissions
     * as determined by calling {@link #hasPermissionToDelete(com.atlassian.jira.bc.JiraServiceContext,com.atlassian.jira.issue.worklog.Worklog)}
     * to delete a worklog for this issue.
     *
     * @param jiraServiceContext containing the user who wishes to create a worklog and the errorCollection
     *                           that will contain any errors in calling the method
     * @param worklogId          identifies the worklog that the delete validation will occur against
     * @param newEstimate        The value to change the issues remaining estimate to.
     * @return Worklog which can be passed to the delete methods if has permission and the data passed in is valid,
     *         null otherwise
     */
    WorklogNewEstimateResult validateDeleteWithNewEstimate(JiraServiceContext jiraServiceContext, Long worklogId, String newEstimate);

    /**
     * Determines whether worklogs are enabled in JIRA and if the user has the required permissions
     * as determined by calling {@link #hasPermissionToDelete(com.atlassian.jira.bc.JiraServiceContext,com.atlassian.jira.issue.worklog.Worklog)}
     * to delete a worklog for this issue.
     *
     * @param jiraServiceContext containing the user who wishes to create a worklog and the errorCollection
     *                           that will contain any errors in calling the method
     * @param worklogId          identifies the worklog that the delete validation will occur against
     * @param adjustmentAmount   The value to increase the issues remaining estimate by.
     * @return Worklog which can be passed to the delete methods if has permission and the data passed in is valid,
     *         null otherwise
     */
    WorklogAdjustmentAmountResult validateDeleteWithManuallyAdjustedEstimate(JiraServiceContext jiraServiceContext, Long worklogId, String adjustmentAmount);

    /**
     * Deletes the specified {@link com.atlassian.jira.issue.worklog.Worklog}. This method will
     * adjust the issues remaining estimate to be the new value which has been passed to this method, the old
     * remaining estimate value will be lost.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to update the supplied worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors encountered
     *                           in calling the method
     * @param worklogNewEstimate the Worklog and new estimate for the issue.
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog delete @return the deleted Worklog object, or null if no object has been deleted.
     * @return true if the {@link Worklog} was successfully deleted, false otherwise
     */
    boolean deleteWithNewRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateResult worklogNewEstimate, boolean dispatchEvent);

    /**
     * Deletes the specified {@link com.atlassian.jira.issue.worklog.Worklog}. This method will
     * adjust the issues remaining estimate to be the new value which has been passed to this method, the old
     * remaining estimate value will be lost.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to update the supplied worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors encountered
     *                           in calling the method
     * @param worklogAdjustmentAmount the Worklog and adjustmentAmount for the issue.
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog delete @return the deleted Worklog object, or null if no object has been deleted.
     * @return true if the {@link Worklog} was successfully deleted, false otherwise
     */
    boolean deleteWithManuallyAdjustedEstimate(JiraServiceContext jiraServiceContext, WorklogAdjustmentAmountResult worklogAdjustmentAmount, boolean dispatchEvent);

    /**
     * Deletes the specified {@link com.atlassian.jira.issue.worklog.Worklog}. This method will
     * make no adjustment to the issues remaining estimate.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to update the supplied worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors encountered
     *                           in calling the method
     * @param worklogResult      result of the call to {@link #validateDelete(com.atlassian.jira.bc.JiraServiceContext,Long)} 
     *                           which contains the {@link Worklog} to delete
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog delete
     * @return true if the {@link Worklog} was successfully deleted, false otherwise
     */
    boolean deleteAndRetainRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent);

    /**
     * Deletes the specified {@link com.atlassian.jira.issue.worklog.Worklog}. This method will auto-adjust the issues
     * remaining estimate by adding the time spent on the deleted worklog.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to update the supplied worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors encountered
     *                           in calling the method
     * @param worklogResult      result of the call to {@link #validateDelete(com.atlassian.jira.bc.JiraServiceContext,Long)}
     *                           which contains the {@link Worklog} to delete
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog delete
     * @return true if the {@link Worklog} was successfully deleted, false otherwise
     */
    boolean deleteAndAutoAdjustRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent);

    /**
     * Determines whether worklogs are enabled in JIRA and if the user has the required permissions
     * as determined by calling {@link #hasPermissionToUpdate(com.atlassian.jira.bc.JiraServiceContext,com.atlassian.jira.issue.worklog.Worklog)}
     * to update a worklog for this issue.
     *
     * @param jiraServiceContext containing the user who wishes to create a worklog and the errorCollection
     *                           that will contain any errors in calling the method
     * @param params             parameter object that contains all the values required to validate
     * @return WorklogResult which can be passed to the update methods if has permission and the data passed in is valid,
     *         null otherwise
     */
    WorklogResult validateUpdate(JiraServiceContext jiraServiceContext, WorklogInputParameters params);

    /**
     * Determines whether worklogs are enabled in JIRA and if the user has the required permissions
     * as determined by calling {@link #hasPermissionToUpdate(com.atlassian.jira.bc.JiraServiceContext,com.atlassian.jira.issue.worklog.Worklog)}
     * to update a worklog for this issue.
     *
     * @param jiraServiceContext containing the user who wishes to create a worklog and the errorCollection
     *                           that will contain any errors in calling the method
     * @param params             parameter object that contains all the values required to validate
     * @return WorklogResult which can be passed to the update methods if has permission and the data passed in is valid,
     *         null otherwise
     */
    WorklogNewEstimateResult validateUpdateWithNewEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateInputParameters params);

    /**
     * Updates the provided {@link com.atlassian.jira.issue.worklog.Worklog}. This method will
     * adjust the issues remaining estimate to be the new value which has been passed to this method, the old
     * remaining estimate value will be lost.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to create a worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors in calling the method
     * @param worklogNewEstimate the Worklog and new estimate for the issue.
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog update @return the updated Worklog object, or null if no object has been updated.
     * @return the updated Worklog object, or null if no object has been updated.
     */
    Worklog updateWithNewRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateResult worklogNewEstimate, boolean dispatchEvent);

    /**
     * Updates the provided {@link com.atlassian.jira.issue.worklog.Worklog}. This method will
     * make no adjustment to the issues remaining estimate.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to update the supplied worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors encountered
     *                           in calling the method
     * @param worklogResult      result of the call to {@link #validateUpdate(com.atlassian.jira.bc.JiraServiceContext,WorklogInputParameters)}
     *                           which contains the {@link Worklog} to update
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog update
     * @return the updated Worklog object, or null if no object has been updated.
     */
    Worklog updateAndRetainRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent);

    /**
     * Updates the provided {@link com.atlassian.jira.issue.worklog.Worklog}. This method will auto-adjust the issues
     * remaining estimate based on the updated value of the time spent on the work.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to update the worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors
     *                           in calling the method
     * @param worklogResult      result of the call to {@link #validateUpdate(com.atlassian.jira.bc.JiraServiceContext,WorklogInputParameters)}
     *                           which contains the {@link Worklog} to update
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog update
     * @return the update Worklog object, or null if no object has been updated.
     */
    Worklog updateAndAutoAdjustRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent);

    /**
     * Determine whether the current user has the permission to update the supplied
     * worklog, timetracking is enabled in JIRA and that the associated issue is in an editable workflow state.
     * <p/>
     * In case of errors, add error messages to the error collection within the servicecontext.
     * <p/>
     * Passing in null worklog or a worklog with null ID will return false and
     * an error message will be added to the error collection.
     * <p/>
     * Passing in null error collection will throw NPE.
     * <p/>
     * This method will return true if the user is a member of the worklog's group/role level (if specified) <b>AND</b><br/>
     * <ul>
     * <li>The user has the {@link com.atlassian.jira.security.Permissions#WORKLOG_EDIT_ALL} permission; <b>OR</b></li>
     * <li>The user is the {@link Worklog} author and has the {@link com.atlassian.jira.security.Permissions#WORKLOG_EDIT_OWN} permission</li>
     * </ul>
     * and false otherwise.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to update the worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors
     *                           in calling the method
     * @param worklog            the {@link Worklog} the user wishes to update
     * @return true if the user has permission to update the supplied worklog, false otherwise
     */
    boolean hasPermissionToUpdate(JiraServiceContext jiraServiceContext, Worklog worklog);

    /**
     * Determine whether the current user has the permission to delete the supplied
     * worklog, timetracking is enabled in JIRA and that the associated issue is in an editable workflow state.
     * <p/>
     * In case of errors, add error messages to the error collection within the servicecontext.
     * <p/>
     * Passing in null worklog or a worklog with null ID will return false and
     * an error message will be added to the error collection.
     * <p/>
     * Passing in null error collection will throw NPE.
     * <p/>
     * This method will return true if the user is a member of the worklog's group/role level (if specified) <b>AND</b><br/>
     * <ul>
     * <li>The user has the {@link com.atlassian.jira.security.Permissions#WORKLOG_DELETE_ALL} permission; <b>OR</b></li>
     * <li>The user is the {@link Worklog} author and has the {@link com.atlassian.jira.security.Permissions#WORKLOG_DELETE_OWN} permission</li>
     * </ul>
     * and false otherwise.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to delete the worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors
     *                           in calling the method
     * @param worklog            the {@link Worklog} the user wishes to delete
     * @return true if the user has permission to delete the supplied worklog, false otherwise
     */
    boolean hasPermissionToDelete(JiraServiceContext jiraServiceContext, Worklog worklog);

    /**
     * Determines whether worklogs are enabled in JIRA and if the user has the required permissions
     * as determined by calling {@link #hasPermissionToCreate(com.atlassian.jira.bc.JiraServiceContext,com.atlassian.jira.issue.Issue,boolean)}
     * to create a worklog for this issue.
     *
     * @param jiraServiceContext containing the user who wishes to create a worklog and the errorCollection
     *                           that will contain any errors in calling the method
     * @param params             parameter object that contains all the values required to validate
     * @return WorklogResult which can be passed to the create methods if has permission and the data passed in is valid,
     *         null otherwise
     */
    WorklogResult validateCreate(JiraServiceContext jiraServiceContext, WorklogInputParameters params);

    /**
     * Determines whether worklogs are enabled in JIRA and if the user has the required permission
     * as determined by calling {@link #hasPermissionToCreate(com.atlassian.jira.bc.JiraServiceContext,com.atlassian.jira.issue.Issue,boolean)}
     * to create a worklog for this issue.
     *
     * @param jiraServiceContext containing the user who wishes to create a worklog and the errorCollection
     *                           that will contain any errors in calling the method
     * @param params             parameter object that contains all the values required to validate
     * @return WorklogNewEstimateResult the Worklog of which can be passed to the create methods if has permission and the data passed in is valid,
     *         null otherwise
     */
    WorklogNewEstimateResult validateCreateWithNewEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateInputParameters params);

    /**
     * Determines whether worklogs are enabled in JIRA and if the user has the required permission
     * as determined by calling {@link #hasPermissionToCreate(com.atlassian.jira.bc.JiraServiceContext,com.atlassian.jira.issue.Issue,boolean)}
     * to create a worklog for this issue.
     *
     * @param jiraServiceContext containing the user who wishes to create a worklog and the errorCollection
     *                           that will contain any errors in calling the method
     * @param params             parameter object that contains all the values required to validate
     * @return WorklogAdjustmentAmountResult the Worklog of which can be passed to the create methods if has permission and the data passed in is valid,
     *         null otherwise
     */
    WorklogAdjustmentAmountResult validateCreateWithManuallyAdjustedEstimate(JiraServiceContext jiraServiceContext, WorklogAdjustmentAmountInputParameters params);

    /**
     * Persists a new {@link com.atlassian.jira.issue.worklog.Worklog} on the given {@link Issue}. This method will
     * adjust the issues remaining estimate to be the new value which has been passed to this method, the old
     * remaining estimate value will be lost.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to create a worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors in calling the method
     * @param worklogNewEstimate the Worklog and new estimate for the issue.
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog creation
     * @return the created Worklog object or null if no object was created.
     * @see #validateCreateWithNewEstimate(com.atlassian.jira.bc.JiraServiceContext, WorklogNewEstimateInputParameters) 
     */
    Worklog createWithNewRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateResult worklogNewEstimate, boolean dispatchEvent);

    /**
     * Persists a new {@link com.atlassian.jira.issue.worklog.Worklog} on the given {@link Issue}. This method will
     * adjust the issues remaining estimate by reducing by the adjustmentAmount which has been passed to this method.
     * Before calling this method, you must call validateCreateWithManuallyAdjustedEstimate() to ensure that the
     * creation is OK.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to create a worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors in calling the method
     * @param worklogAdjustmentAmount the Worklog and adjustmentAmount for the issue.
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog creation
     * @return the created Worklog object or null if no object was created.
     * @see #validateCreateWithManuallyAdjustedEstimate
     */
    Worklog createWithManuallyAdjustedEstimate(JiraServiceContext jiraServiceContext, WorklogAdjustmentAmountResult worklogAdjustmentAmount, boolean dispatchEvent);

    /**
     * Persists a new {@link com.atlassian.jira.issue.worklog.Worklog} on the given {@link Issue}. This method will
     * make no adjustment to the issues remaining estimate.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to create a worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors in calling the method
     * @param worklogResult      the WorklogResult generated by the validate call
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog creation  @return the created Worklog object, or null if no object created.
     * @return the created Worklog object or null if no object was created.
     */
    Worklog createAndRetainRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent);

    /**
     * Persists a new {@link com.atlassian.jira.issue.worklog.Worklog} on the given {@link Issue}. This method will
     * auto-adjust the issues remaining estimate based on the value of the time spent on the work.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to create a worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors in calling the method
     * @param worklogResult      the WorklogResult generated by the validate call
     * @param dispatchEvent      whether or not you want to have an event dispatched on Worklog creation  @return the created Worklog object, or null if no object created.
     * @return the created Worklog object or null if no object was created.
     */
    Worklog createAndAutoAdjustRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent);

    /**
     * Determines if the user has the {@link com.atlassian.jira.security.Permissions#WORK_ISSUE} permission,
     * that timetracking is enabled in JIRA and that the associated issue is in an editable workflow state.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to create a worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors in calling the method
     * @param issue              the issue to add the worklog to
     * @param isEditableCheckRequired set to true if we require the issue to be in an editable state. This should always
     *                           be the case except when logging work on transition
     * @return true if the user has permission to create a worklog on the specified issue, false otherwise
     */
    boolean hasPermissionToCreate(JiraServiceContext jiraServiceContext, Issue issue, final boolean isEditableCheckRequired);

    /**
     * Used to get a worklog by its id.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to create a worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors in calling the method
     * @param id                 uniquely identifies the worklog
     * @return returns the worklog for the passed in id, null if not found.
     */
    Worklog getById(JiraServiceContext jiraServiceContext, Long id);

    /**
     * Returns all child worklogs of a specified issue.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to create a worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors in calling the method
     * @param issue              the specified parent issue (not null)
     * @return a List of Worklogs, ordered by creation date. An empty List will be returned if none are found
     */
    List getByIssue(JiraServiceContext jiraServiceContext, Issue issue);

    /**
     * Returns all child worklogs of a specified issue that the provided user has permission to see.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to create a worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors in calling the method
     * @param issue              the specified parent issue (not null)
     * @return a List of Worklogs, ordered by creation date. An empty List will be returned if none are found
     */
    List<Worklog> getByIssueVisibleToUser(JiraServiceContext jiraServiceContext, Issue issue);


    /**
     * Returns a PagedList over all all child worklogs of a specified issue that the provided user has permission to see.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who wishes to create a worklog and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors in calling the method
     * @param issue              the specified parent issue (not null)
     * @param pageSize           the number of worklogs per page
     * @return a List of Worklogs, ordered by creation date. An empty List will be returned if none are found
     */
    PagedList<Worklog> getByIssueVisibleToUser(JiraServiceContext jiraServiceContext, Issue issue, int pageSize);

    /**
     * Will return true if {@link com.atlassian.jira.config.properties.APKeys#JIRA_OPTION_TIMETRACKING} is true, false
     * otherwise.
     *
     * @return true if enabled, false otherwise.
     */
    boolean isTimeTrackingEnabled();

    /**
     * Will return true if the issue is in an editable workflow state.
     *
     * @param issue the issue to see if it is in an editable store
     * @return true if editable, false otherwise
     */
    boolean isIssueInEditableWorkflowState(Issue issue);
}
