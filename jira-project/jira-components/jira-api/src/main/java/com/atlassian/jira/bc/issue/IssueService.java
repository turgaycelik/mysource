package com.atlassian.jira.bc.issue;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.TransitionOptions;

import java.util.Map;
import javax.annotation.Nullable;

/**
 * This is used to perform create, update, delete, and transition operations in JIRA with {@link Issue}'s. This services methods
 * will make sure that when dealing with {@link Issue}'s that all of JIRA's business rules are enforced. This means
 * that permissions and data validation will be checked, proper events will be fired, and notifications will be
 * triggered.
 *
 * @since v4.1
 */
@PublicApi
public interface IssueService
{
    /**
     * This method will retrieve the issue with the provided issue id if the current user has permission to
     * view the issue.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issueId the database ID of the issue.
     * @return a result containing an {@link Issue} for the provided id if the user has the {@link com.atlassian.jira.security.Permissions#BROWSE}
     * permission for the issue, the issue will be null if the issue does not exist or the user does not have permission.
     * The result also contains an error collection that will contain any error messages that may have been generated
     * when performing the operation.
     */
    IssueResult getIssue(User user, Long issueId);

    /**
     * This method will retrieve the issue with the provided issue id if the current user has permission to
     * view the issue.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issueKey the key (e.g. TST-1) of the issue.
     * @return a result containing an {@link Issue} for the provided id if the user has the {@link com.atlassian.jira.security.Permissions#BROWSE}
     * permission for the issue, the issue will be null if the issue does not exist or the user does not have permission.
     * The result also contains an error collection that will contain any error messages that may have been generated
     * when performing the operation.
     */
    IssueResult getIssue(User user, String issueKey);

    /**
     * This method does the same as {@link #validateCreate(User, IssueInputParameters)}
     * and it allows you to specify a parentId such that the created issue will be a subtask of the provided parent id.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param parentId the id of the parent {@link Issue} for this subtask.
     * @param issueInputParameters this represents the issue values we want to validate. It contains access to a raw
     * map of "web" style input parameters. These parameters are used to allow the
     * fields to attain the user inputted values. If you would like to specify some seed values for the field values
     * holder that will be passed to the JIRA fields, then you should set your map on this input values. Also, if
     * you want to specify which fields have been provided for user input, set the provided fields on this input, if
     * not set this method will use the system defaults. Fields not in the provided fields collection
     * will not be validated but they will have their defaults set.
     * @return a result object containing a fully populated and validated {@link MutableIssue} if all validation
     * passes and the user has permission to create such an issue in the provided project, otherwise the issue will be null.
     * The result also contains an error collection that will contain any error messages that may have been generated
     * when performing the operation. It also contains a populated FieldValuesHolder that can be used by the fields
     * to re-render the user inputted values.
     */
    CreateValidationResult validateSubTaskCreate(User user, Long parentId, IssueInputParameters issueInputParameters);

    /**
     * This method will validate parameters and check permissions and if all checks pass it will create an {@link Issue}
     * that can be passed to the {@link #create(User, CreateValidationResult)}
     * method.
     * <p/>
     * This method will validate that the provided parameters are valid for the fields that are specified by the
     * configured create screen for the provided project/issue type, unless over-ridden with the provided fields collection
     * in the IssueInputParameters. Any fields that are not included on the create
     * screen or provided fields will be populated with their default values and have those values validated.
     * If any validation fails then this method will return a null Issue. Whether the methods validation is a success
     * or a failure the fieldValuesHolder in the result will be populated with the data that is needed for the fields to render the
     * inputted raw parameters.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issueInputParameters this represents the issue values we want to validate. It contains access to a raw
     * map of "web" style input parameters. These parameters are used to allow the
     * fields to attain the user inputted values. If you would like to specify some seed values for the field values
     * holder that will be passed to the JIRA fields, then you should set your map on this input values. Also, if
     * you want to specify which fields have been provided for user input, set the provided fields on this input, if
     * not set this method will use the system defaults. Fields not in the provided fields collection
     * will not be validated but they will have their defaults set.
     * @return a result object containing a fully populated and validated {@link MutableIssue} if all validation
     * passes and the user has permission to create such an issue in the provided project, otherwise the issue will be null.
     * The result also contains an error collection that will contain any error messages that may have been generated
     * when performing the operation. It also contains a populated FieldValuesHolder that can be used by the fields
     * to re-render the user inputted values.
     */
    CreateValidationResult validateCreate(User user, IssueInputParameters issueInputParameters);

    /**
     * This method will store the provided issue to the JIRA datastore. This method will only do so if the current
     * user has the create permission within the specified project. The issue will be created and indexed and placed
     * into the correct initial workflow step. All workflow post-functions associated with the workflows initial
     * action will be executed.
     *
     * This method will fire the {@link com.atlassian.jira.event.type.EventType#ISSUE_CREATED_ID} event, thus
     * triggering any notifications that may need to be sent.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param createValidationResult contains the issue to store. This should have been created by one of the
     * validateCreate methods. The result must have {@link com.atlassian.jira.bc.ServiceResult#isValid()} return
     * true. If false this method will throw an IllegalStateException.
     * @return the a result object containing the persisted {@link Issue} if all went. If there was an error creating
     * the issue then the issue will be null and the error collection will contain details of what went wrong.
     */
    IssueResult create(User user, CreateValidationResult createValidationResult);

    /**
     * This method will store the provided issue to the JIRA datastore. This method will only do so if the current
     * user has the create permission within the specified project. The issue will be created and indexed and placed
     * into the correct initial workflow step. All workflow post-functions associated with the workflows initial
     * action will be executed.
     * <p/>
     * This method should only be used if you have a <b>very</b> complicated custom workflow where you have defined
     * more than one initial action. In this case the auxilarySubmitButtonValue will indicate to the workflow
     * manager which initial action should be executed.
     *
     * This method will fire the {@link com.atlassian.jira.event.type.EventType#ISSUE_CREATED_ID} event, thus
     * triggering any notifications that may need to be sent.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param createValidationResult contains the issue to store. This should have been created by one of the
     * validateCreate methods. The result must have {@link com.atlassian.jira.bc.ServiceResult#isValid()} return
     * true. If false this method will throw an IllegalStateException.
     * @param initialWorkflowActionName indicates which initial action to execute.
     * @return the a result object containing the persisted {@link Issue} if all went. If there was an error creating
     * the issue then the issue will be null and the error collection will contain details of what went wrong.
     */
    IssueResult create(User user, CreateValidationResult createValidationResult, String initialWorkflowActionName);

    /**
     * This method will store the provided issue to the JIRA datastore. Use this method to perform the default
     * behavior for updating an issue in JIRA. This method will only update the issue if the current
     * user has the edit permission within the specified project. The issue will be saved and re-indexed.
     * <p/>
     *
     * This method will fire the {@link com.atlassian.jira.event.type.EventType#ISSUE_UPDATED_ID} event, thus
     * triggering any notifications that may need to be sent.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param updateValidationResult that contains the issue to update, this should have been created via the
     * {@link #validateUpdate(User, Long, IssueInputParameters)} method.
     * The result must have {@link com.atlassian.jira.bc.ServiceResult#isValid()} return
     * true. If false this method will throw an IllegalStateException.
     * @return the a result object containing the persisted {@link Issue} if all went. If there was an error updating
     * the issue then the issue will be null and the error collection will contain details of what went wrong.
     */
    IssueResult update(User user, UpdateValidationResult updateValidationResult);

    /**
     * This method will validate parameters and check permissions and if all checks pass it will construct a new
     * instance of the {@link Issue} and will update it with the new parameters.
     * This object can then be passed to the {@link #update(User, UpdateValidationResult)}
     * method.
     * <p/>
     * This method will validate that the provided parameters are valid for the fields that are specified by the
     * configured edit screen for the issues project/issue type. Any fields that are not included on the edit
     * screen will not be validated or populated.
     * <p/>
     * If any validation fails then this method will return a null Issue. Whether the methods validation is a success
     * or a failure the fieldValuesHolder will be populated with the data that is needed for the fields to render
     * the inputted raw parameters.
     * <p/>
     * The issue must also be in a open workflow state for this call to succeed.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issueId the unique identifer for the issue to update, this must be an issue that exists in JIRA with a valid project/issue type.
     * @param issueInputParameters this represents the issue values we want to validate. It contains access to a raw
     * map of "web" style input parameters. These parameters are used to allow the
     * fields to attain the user inputted values. If you would like to specify some seed values for the field values
     * holder that will be passed to the JIRA fields, then you should set your map on this input values. Also, if
     * you want to specify which fields have been provided for user input, set the provided fields on this input, if
     * not set this method will use the system defaults. Fields not in the provided fields collection
     * will not be validated but they will have their defaults set.
     *
     * @return a result that can be passed to the
     * {@link #update(User, UpdateValidationResult)} method which
     * contains an {@link com.atlassian.jira.issue.MutableIssue} populated with the updated values and the error
     * collection that contains any validation errors that may have occurred. It also contains a populated FieldValuesHolder
     * that can be used by the fields to re-render the user inputted values.
     */
    UpdateValidationResult validateUpdate(User user, Long issueId, IssueInputParameters issueInputParameters);

    /**
     * This method will store the provided issue to the JIRA datastore. This method will only update the issue if the
     * current user has the edit permission within the specified project. The issue will be saved and re-indexed.
     * <p/>
     *
     * This method should be used if you want to exert more control over what happens when JIRA updates an issue. This
     * method will allow you to specify if an event is dispatched and if so which event is dispatched, see
     * {@link com.atlassian.jira.event.type.EventDispatchOption}. This method also allows you to specify if email
     * notifications should be send to notify users of the update.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param updateValidationResult contains the issue to update, this should have been created via the
     * {@link #validateUpdate(User, Long, com.atlassian.jira.issue.IssueInputParameters)} method.
     * The result must have {@link com.atlassian.jira.bc.ServiceResult#isValid()} return
     * true. If false this method will throw an IllegalStateException.
     * @param eventDispatchOption specifies if an event should be sent and if so which should be sent.
     * @param sendMail if true mail notifications will be sent, otherwise mail notifications will be suppressed.
     * @return the a result object containing the persisted {@link Issue} if all went. If there was an error updating
     * the issue then the issue will be null and the error collection will contain details of what went wrong.
     */
    IssueResult update(User user, UpdateValidationResult updateValidationResult, EventDispatchOption eventDispatchOption, boolean sendMail);

    /**
     * This method will check permissions to see if a user has the {@link com.atlassian.jira.security.Permissions#DELETE_ISSUE}
     * permission in relation to the provided issue. If the current user does not have permission then an
     * error will be added to the service contexts error collection.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issueId the unique identifier of the issue to delete
     * @return a result that can be passed to the
     * {@link #delete(User, com.atlassian.jira.bc.issue.IssueService.DeleteValidationResult)} method which
     * contains an {@link com.atlassian.jira.issue.MutableIssue} and the error
     * collection that contains any validation errors that may have occurred.
     */
    DeleteValidationResult validateDelete(User user, Long issueId);

    /**
     * This method will delete an issue from JIRA. This will happen only if the current user has the
     * {@link com.atlassian.jira.security.Permissions#DELETE_ISSUE} permission. This will clean up all issue
     * associations in JIRA and will de-index the issue.
     * <p/>
     * This method will dispatch the {@link com.atlassian.jira.event.type.EventType#ISSUE_DELETED_ID} event and will
     * generate email notifications.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param deleteValidationResult contains the issue to delete, this should have been created via the
     * {@link #validateDelete(User, Long)} method.
     * The result must have {@link com.atlassian.jira.bc.ServiceResult#isValid()} return
     * true. If false this method will throw an IllegalStateException.
     * @return If there was an error deleting the issue then the error collection will contain details of what went wrong.
     */
    ErrorCollection delete(User user, DeleteValidationResult deleteValidationResult);

    /**
     * This method will delete an issue from JIRA. This will happen only if the current user has the
     * {@link com.atlassian.jira.security.Permissions#DELETE_ISSUE} permission. This will clean up all issue
     * associations in JIRA and will de-index the issue.
     * <p/>
     *
     * This method should be used if you want to exert more control over what happens when JIRA deletes an issue. This
     * method will allow you to specify if an event is dispatched and if so which event is dispatched, see
     * {@link com.atlassian.jira.event.type.EventDispatchOption}. This method also allows you to specify if email
     * notifications should be send to notify users of the deletion.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param deleteValidationResult contains the issue to delete, this should have been created via the
     * {@link #validateDelete(User, Long)} method.
     * The result must have {@link com.atlassian.jira.bc.ServiceResult#isValid()} return
     * true. If false this method will throw an IllegalStateException.
     * @param eventDispatchOption specifies if an event should be sent and if so which should be sent.
     * @param sendMail if true mail notifications will be sent, otherwise mail notifications will be suppressed.
     * @return If there was an error deleting the issue then the error collection will contain details of what went wrong.
     */
    ErrorCollection delete(User user, DeleteValidationResult deleteValidationResult, EventDispatchOption eventDispatchOption, boolean sendMail);

    /**
     * Returns <code>true</code> if the issue can be edited by the current user. This is determined by looking at both the
     * user's permissions and the workflow step the issue is in.
     * @param issue the issue you want to edit
     * @param user the user who will be performing the edit
     * @return <code>true</code> if the user has permission and the issue is in an editable workflow step
     */
    boolean isEditable(Issue issue, User user);

    /**
     * This method will validate parameters and check the transition conditions and if all checks pass it will construct a new
     * instance of the {@link Issue} and will update it with the new parameters. This object
     * can then be passed to the {@link #transition(User, TransitionValidationResult)}
     * method.
     * <p/>
     * This method will validate that the provided parameters are valid for the fields that are specified by the
     * configured workflow transition screen for the issues project/issue type. Any fields that are not included on the
     * screen will not be validated or populated. If the transition has no view then the issue fields will not be validated.
     * <p/>
     * If any validation fails then this method will return a null TransitionResult. Whether the methods validation is a success
     * or a failure the fieldValuesHolder will be populated with the data that is needed for the fields to render
     * the inputted raw parameters.
     * <p/>
     * This will call validateTransition below with {@link com.atlassian.jira.workflow.TransitionOptions#defaults()}.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issueId the unique identifier for the issue to update, this must be an issue that exists in JIRA with a valid project/issue type.
     * @param actionId is the id of the workflow action that you would like to transition the issue through.
     * @param issueInputParameters this represents the issue values we want to validate. It contains access to a raw
     * map of "web" style input parameters. These parameters are used to allow the
     * fields to attain the user inputted values. If you would like to specify some seed values for the field values
     * holder that will be passed to the JIRA fields, then you should set your map on this input values. Also, if
     * you want to specify which fields have been provided for user input, set the provided fields on this input, if
     * not set this method will use the system defaults. Fields not in the provided fields collection
     * will not be validated but they will have their defaults set.
     * @return a TransitionResult that contains a {@link com.atlassian.jira.issue.MutableIssue} populated with the
     * updated values and some additional parameters that are used when transitioning the issue through workflow and
     * the error collection that contains any validation errors that may have occurred. It also contains a populated FieldValuesHolder
     * that can be used by the fields to re-render the user inputted values.
     */
    TransitionValidationResult validateTransition(User user, Long issueId, int actionId, IssueInputParameters issueInputParameters);

    /**
     * This method will validate parameters and check the transition conditions and if all checks pass it will construct a new
     * instance of the {@link Issue} and will update it with the new parameters. This object
     * can then be passed to the {@link #transition(User, TransitionValidationResult)}
     * method.
     * <p/>
     * This method will validate that the provided parameters are valid for the fields that are specified by the
     * configured workflow transition screen for the issues project/issue type. Any fields that are not included on the
     * screen will not be validated or populated. If the transition has no view then the issue fields will not be validated.
     * <p/>
     * If any validation fails then this method will return a null TransitionResult. Whether the methods validation is a success
     * or a failure the fieldValuesHolder will be populated with the data that is needed for the fields to render
     * the inputted raw parameters.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issueId the unique identifier for the issue to update, this must be an issue that exists in JIRA with a valid project/issue type.
     * @param actionId is the id of the workflow action that you would like to transition the issue through.
     * @param issueInputParameters this represents the issue values we want to validate. It contains access to a raw
     * map of "web" style input parameters. These parameters are used to allow the
     * fields to attain the user inputted values. If you would like to specify some seed values for the field values
     * holder that will be passed to the JIRA fields, then you should set your map on this input values. Also, if
     * you want to specify which fields have been provided for user input, set the provided fields on this input, if
     * not set this method will use the system defaults. Fields not in the provided fields collection
     * will not be validated but they will have their defaults set.
     * @param transitionOptions holds whether you wish to skip conditions or validators for this API call.
     * @return a TransitionResult that contains a {@link com.atlassian.jira.issue.MutableIssue} populated with the
     * updated values and some additional parameters that are used when transitioning the issue through workflow and
     * the error collection that contains any validation errors that may have occurred. It also contains a populated FieldValuesHolder
     * that can be used by the fields to re-render the user inputted values.
     * @since v6.3
     */
    TransitionValidationResult validateTransition(User user, Long issueId, int actionId, IssueInputParameters issueInputParameters, TransitionOptions transitionOptions);

    /**
     * This method will store the provided issue to the JIRA datastore and will transition it through workflow.
     * Use this method to perform the default behavior for updating and transitioning an issue in JIRA.
     * The issue will be saved and re-indexed, the comment, if provided, will be added and the transition will be made.
     * <p/>
     *
     * This method will fire the event associated with the workflow transition, thus
     * triggering any notifications that may need to be sent.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param transitionResult contains the issue to transition, with any updates that need to be persisted and
     * the action id which is the id of the workflow action that you would like to transition the issue through.
     * The result must have {@link com.atlassian.jira.bc.ServiceResult#isValid()} return
     * true. If false this method will throw an IllegalStateException.
     * @return result containing the updated transitioned issue and any errors that may have occurred.
     */
    IssueResult transition(User user, TransitionValidationResult transitionResult);

    /**
     * This method will check permissions to see if a user has the permission in relation to the provided issue, and
     * that the assignee is assignable.
     * If validation fails then an error will be added to the service contexts error collection.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param issueId the unique identifier for the issue to update, this must be an issue that exists in JIRA with a valid project/issue type.
     * @param assignee is the id of the user to assign the issue to.

     * @return an AssignValidationResult
     * that contains an {@link com.atlassian.jira.issue.MutableIssue} populated with the updated values and the error
     * collection that contains any validation errors that may have occurred. It also contains a populated FieldValuesHolder
     * that can be used by the fields to re-render the user inputted values.
     */
    AssignValidationResult validateAssign(User user, Long issueId, String assignee);

    /**
     * This method will store the provided issue to the JIRA datastore and will transition it through workflow.
     * Use this method to perform the default behavior for updating and transitioning an issue in JIRA.
     * The issue will be saved and re-indexed, the comment, if provided, will be added and the transition will be made.
     * <p/>
     *
     * This method will fire the event associated with the workflow transition, thus
     * triggering any notifications that may need to be sent.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user).
     * @param assignResult contains the issue and assigneeId for the assignment.
     * The result must have {@link com.atlassian.jira.bc.ServiceResult#isValid()} return
     * true. If false this method will throw an IllegalStateException.
     * @return result containing the updated transitioned issue and any errors that may have occurred.
     */
    IssueResult assign(User user, AssignValidationResult assignResult);

    /**
     * Constructs a new IssueInputParameters object with no initial values.
     * <p>
     * IssueInputParameters is used as input to many of the methods in IssueService.
     *
     * @return a new IssueInputParameters object with no initial values.
     *
     * @see #newIssueInputParameters(java.util.Map)
     */
    IssueInputParameters newIssueInputParameters();

    /**
     * Constructs a new IssueInputParameters object with initial values provided by the input Map.
     * <p>
     * This is often used to pass HTTP Parameters with Issue values eg:<br>
     * <tt>&nbsp;&nbsp;  issueService.newIssueInputParameters(ActionContext.getParameters()) </tt>
     * <p>
     * IssueInputParameters is used as input to many of the methods in IssueService.
     *
     * @param actionParameters Parameters as received by a HTTP Request
     *
     * @return a new IssueInputParameters object with initial values provided by the input Map.
     *
     * @see #newIssueInputParameters()
     */
    IssueInputParameters newIssueInputParameters(Map<String, String[]> actionParameters);

    /**
     * A simple object that holds the information about validating a create issue operation. This object should not
     * be constructed directly, you should invoke the
     * {@link #validateCreate(User, IssueInputParameters)}
     * method to obtain this.
     */
    public static class CreateValidationResult extends IssueValidationResult
    {
        private Map<String, Object> fieldValuesHolder;

        @Internal
        public CreateValidationResult(final MutableIssue issue, final ErrorCollection errors, final Map<String, Object> fieldValuesHolder)
        {
            super(issue, errors);
            this.fieldValuesHolder = fieldValuesHolder;
        }

        /**
         * @return can be used by the JIRA fields to re-render the inputted values.
         */
        public Map<String, Object> getFieldValuesHolder()
        {
            return fieldValuesHolder;
        }
    }

    /**
     * A simple object that holds the information about validating an update issue operation. This object should not
     * be constructed directly, you should invoke the
     * {@link com.atlassian.jira.bc.issue.IssueService#validateUpdate(User, Long, com.atlassian.jira.issue.IssueInputParameters)}
     * method to obtain this.
     */
    public static class UpdateValidationResult extends IssueValidationResult
    {
        @Nullable
        private final HistoryMetadata historyMetadata;
        private Map<String, Object> fieldValuesHolder;

        @Internal
        public UpdateValidationResult(final MutableIssue issue, final ErrorCollection errors, final Map<String, Object> fieldValuesHolder)
        {
            this(issue, errors, fieldValuesHolder, null);
        }

        /**
         * @since JIRA 6.3
         */
        @Internal
        UpdateValidationResult(final MutableIssue issue, final ErrorCollection errors, final Map<String, Object> fieldValuesHolder,
                @Nullable final HistoryMetadata historyMetadata)
        {
            super(issue, errors);
            this.fieldValuesHolder = fieldValuesHolder;
            this.historyMetadata = historyMetadata;
        }

        /**
         * @return can be used by the JIRA fields to re-render the inputted values.
         */
        public Map<String, Object> getFieldValuesHolder()
        {
            return fieldValuesHolder;
        }

        /**
         * @return the history metadata associated with the update
         * @since JIRA 6.3
         */
        @Nullable
        public HistoryMetadata getHistoryMetadata()
        {
            return historyMetadata;
        }
    }

    /**
     * A simple object that holds the information about validating an update issue operation. This object should not
     * be constructed directly, you should invoke the
     * {@link com.atlassian.jira.bc.issue.IssueService#validateUpdate(User, Long, com.atlassian.jira.issue.IssueInputParameters)}
     * method to obtain this.
     */
    public static class AssignValidationResult extends IssueValidationResult
    {
        private String assigneeId;

        @Internal
        public AssignValidationResult(final MutableIssue issue, final ErrorCollection errors, final String assigneeId)
        {
            super(issue, errors);
            this.assigneeId = assigneeId;
        }

        public String getAssigneeId()
        {
            return assigneeId;
        }
    }

    /**
     * A simple object that holds the information about validating a delete issue operation. This object should not
     * be constructed directly, you should invoke the
     * {@link com.atlassian.jira.bc.issue.IssueService#validateDelete(User, Long)}
     * method to obtain this.
     */
    public static class DeleteValidationResult extends IssueValidationResult
    {
        @Internal
        public DeleteValidationResult(final MutableIssue issue, final ErrorCollection errors)
        {
            super(issue, errors);
        }
    }

    /**
     * A simple object that holds the information about an issue operation. This object should not
     * be constructed directly, you should invoke the
     * {@link com.atlassian.jira.bc.issue.IssueService#getIssue(User, String)} or
     * {@link com.atlassian.jira.bc.issue.IssueService#getIssue(User, Long)} or
     * {@link com.atlassian.jira.bc.issue.IssueService#create(User, com.atlassian.jira.bc.issue.IssueService.CreateValidationResult)} or
     * {@link com.atlassian.jira.bc.issue.IssueService#create(User, com.atlassian.jira.bc.issue.IssueService.CreateValidationResult, String)} or
     * {@link com.atlassian.jira.bc.issue.IssueService#update(User, com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult)} or
     * {@link com.atlassian.jira.bc.issue.IssueService#update(User, com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult, com.atlassian.jira.event.type.EventDispatchOption, boolean)} or
     * {@link com.atlassian.jira.bc.issue.IssueService#transition(User, com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult)}
     * method to obtain this.
     */
    @PublicApi
    public static class IssueResult extends IssueValidationResult
    {
        @Internal
        public IssueResult(final MutableIssue issue)
        {
            super(issue, new SimpleErrorCollection());
        }

        @Internal
        public IssueResult(final MutableIssue issue, final ErrorCollection errors)
        {
            super(issue, errors);
        }
    }

    /**
     * A simple result object that holds the information required to make a successful issue transition. This object should not
     * be constructed directly, you should invoke the
     * {@link com.atlassian.jira.bc.issue.IssueService#validateTransition(User, Long, int, com.atlassian.jira.issue.IssueInputParameters)}
     * method to obtain this.
     */
    public static class TransitionValidationResult extends ServiceResultImpl
    {
        private final MutableIssue issue;
        private final Map additionInputs;
        private final Map<String, Object> fieldValuesHolder;
        private final int actionId;

        public TransitionValidationResult(final MutableIssue issue, final ErrorCollection errors, final Map<String, Object> fieldValuesHolder, final Map additionInputs, final int actionId)
        {
            super(errors);
            this.issue = issue;
            this.fieldValuesHolder = fieldValuesHolder;
            this.additionInputs = additionInputs;
            this.actionId = actionId;
        }

        public MutableIssue getIssue()
        {
            return issue;
        }

        public Map getAdditionInputs()
        {
            return additionInputs;
        }

        public int getActionId()
        {
            return actionId;
        }

        public Map<String, Object> getFieldValuesHolder()
        {
            return fieldValuesHolder;
        }
    }

    /**
     * A simple base object that holds the information about performing an issue operation.
     */
    @PublicApi
    public static abstract class IssueValidationResult extends ServiceResultImpl
    {
        private final MutableIssue issue;

        @Internal
        public IssueValidationResult(final MutableIssue issue, final ErrorCollection errors)
        {
            super(errors);
            this.issue = issue;
        }

        public MutableIssue getIssue()
        {
            return issue;
        }
    }
}
