package com.atlassian.jira.bc.issue.comment;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.json.JSONObject;

/**
 * This is the business layer component that must be used to access all {@link com.atlassian.jira.issue.comments.Comment} functionality.
 * This will perform validation before it hands off to the {@link com.atlassian.jira.issue.comments.CommentManager}.
 * Operations will not be performed if validation fails.
 */
@PublicApi
public interface CommentService
{
    /**
     * Confirms the parameters to create a comment are valid and that the user has the permission to do so.
     * This method will validate the raw input parameters. This method only validates the parameters and will not
     * actually persist the changes, you must call {@link #create(ApplicationUser, com.atlassian.jira.bc.issue.comment.CommentService.CommentCreateValidationResult, boolean)} to persist the changes.
     * If an error is encountered then the {@link com.atlassian.jira.bc.issue.comment.CommentService.CommentCreateValidationResult} will contain the specific error message.
     *
     * @param user            The {@link com.atlassian.jira.user.ApplicationUser} who will be performing the operation.
     * @param commentParameters the comment parameters.
     * @return validation result.
     */
    CommentCreateValidationResult validateCommentCreate(ApplicationUser user, @Nonnull CommentParameters commentParameters);

    /**
     * Creates and persists a {@link Comment} on given {@link Issue}.
     *
     * @param user            The {@link com.atlassian.jira.user.ApplicationUser} who will be performing the operation.
     * @param commentValidationResult the results of {@link #validateCommentCreate(ApplicationUser, com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters)}.
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @return the created Comment object, or null if no object created.
     */
    Comment create(ApplicationUser user, CommentCreateValidationResult commentValidationResult, boolean dispatchEvent);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue}.
     *
     * @param user            The {@link com.atlassian.jira.user.ApplicationUser} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param created         The date of comment creation
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     * @deprecated use {@link #validateCommentCreate(ApplicationUser, com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters)} and {@link #create(ApplicationUser, CommentCreateValidationResult, boolean)}.
     */
    @Deprecated
    Comment create(ApplicationUser user, Issue issue, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue}.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param created         The date of comment creation
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     * @deprecated use {@link #validateCommentCreate(ApplicationUser, com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters)} and {@link #create(ApplicationUser, CommentCreateValidationResult, boolean)}.
     */
    @Deprecated
    Comment create(User user, Issue issue, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue} set with current date and time.
     *
     * @param user            The {@link com.atlassian.jira.user.ApplicationUser} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     * @deprecated use {@link #validateCommentCreate(ApplicationUser, com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters)} and {@link #create(ApplicationUser, CommentCreateValidationResult, boolean)}.
     */
    @Deprecated
    Comment create(ApplicationUser user, Issue issue, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue} set with current date and time.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     * @deprecated use {@link #validateCommentCreate(ApplicationUser, com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters)} and {@link #create(ApplicationUser, CommentCreateValidationResult, boolean)}.
     */
    @Deprecated
    Comment create(User user, Issue issue, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue} set with current date and time, visible to all
     * - no group level or role level restriction.
     *
     * @param user            The {@link com.atlassian.jira.user.ApplicationUser} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     * @deprecated use {@link #validateCommentCreate(ApplicationUser, com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters)} and {@link #create(ApplicationUser, CommentCreateValidationResult, boolean)}.
     */
    @Deprecated
    Comment create(ApplicationUser user, Issue issue, String body, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue} set with current date and time, visible to all
     * - no group level or role level restriction.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     * @deprecated use {@link #validateCommentCreate(ApplicationUser, com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters)} and {@link #create(ApplicationUser, CommentCreateValidationResult, boolean)}.
     */
    @Deprecated
    Comment create(User user, Issue issue, String body, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Confirms the parameters to update a comment are valid and that the updating user has the permission to do so.
     * This method will validate the raw input parameters. This method only validates the parameters and will not
     * actually persist the changes, you must call {@link #update} to persist the changes. If an error is encountered
     * then the {@link ErrorCollection} will contain the specific error message.
     *
     * @param user            The {@link com.atlassian.jira.user.ApplicationUser} who will be performing the operation and who will be the updatedAuthor.
     * @param commentId       The id of the comment to be updated. Permissions will be checked to ensure that the user
     *                        has the right to update this comment. If the comment does not exist an error will be reported.
     * @param body            The body of the comment to be updated.
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param errorCollection holder for any errors that were thrown attempting to update a comment
     * @deprecated use {@link #validateCommentUpdate(ApplicationUser, Long, com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters)}.
     */
    @Deprecated
    void validateCommentUpdate(ApplicationUser user, Long commentId, String body, String groupLevel, Long roleLevelId, ErrorCollection errorCollection);

    /**
     * Confirms the parameters to update a comment are valid and that the updating user has the permission to do so.
     * This method will validate the raw input parameters. This method only validates the parameters and will not
     * actually persist the changes, you must call {@link #update} to persist the changes. If an error is encountered
     * then the {@link ErrorCollection} will contain the specific error message.
     *
     * @param user            The {@link ApplicationUser} who will be performing the operation and who will be the updatedAuthor.
     * @param commentId       The id of the comment to be updated. Permissions will be checked to ensure that the user
     *                        has the right to update this comment. If the comment does not exist an error will be reported.
     * @param body            The body of the comment to be updated.
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param errorCollection holder for any errors that were thrown attempting to update a comment
     *
     * @deprecated use {@link #validateCommentUpdate(ApplicationUser, Long, com.atlassian.jira.bc.issue.comment.CommentService.CommentParameters)}.
     */
    @Deprecated
    void validateCommentUpdate(User user, Long commentId, String body, String groupLevel, Long roleLevelId, ErrorCollection errorCollection);

    /**
     * Confirms the parameters to update a comment are valid and that the updating user has the permission to do so.
     * This method will validate the raw input parameters. This method only validates the parameters and will not
     * actually persist the changes, you must call {@link #update} to persist the changes. If an error is encountered
     * then the {@link ErrorCollection} will contain the specific error message.
     *
     * @param user            The {@link com.atlassian.jira.user.ApplicationUser} who will be performing the operation and who will be the updatedAuthor.
     * @param commentId       The id of the comment to be updated. Permissions will be checked to ensure that the user
     *                        has the right to update this comment. If the comment does not exist an error will be reported.
     * @param commentParameters the comment parameters.
     */
    CommentUpdateValidationResult validateCommentUpdate(ApplicationUser user, Long commentId, CommentParameters commentParameters);

    /**
     * Updates a {@link Comment} and sets the comments updated date to be now and the updatedAuthor to be the
     * passed in user.
     *
     * @param user            the {@link com.atlassian.jira.user.ApplicationUser} who must have permission to update this comment and who will be used as the updateAuthor
     * @param comment         the object that contains the changes to the comment to persist.
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment update. If false then
     *                        the issue will not be reindexed.
     * @param errorCollection holder for any errors that were thrown attempting to update a comment
     * @throws IllegalArgumentException if comment or its id is null
     * @deprecated use {@link #update(ApplicationUser, CommentUpdateValidationResult, boolean)} instead.
     */
    @Deprecated
    void update(ApplicationUser user, MutableComment comment, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Updates a {@link Comment} and sets the comments updated date to be now and the updatedAuthor to be the
     * passed in user.
     *
     * @param user            the {@link User} who must have permission to update this comment and who will be used as the updateAuthor
     * @param comment         the object that contains the changes to the comment to persist.
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment update. If false then
     *                        the issue will not be reindexed.
     * @param errorCollection holder for any errors that were thrown attempting to update a comment
     * @throws IllegalArgumentException if comment or its id is null
     * @deprecated use {@link #update(ApplicationUser, CommentUpdateValidationResult, boolean)} instead.
     */
    @Deprecated
    void update(User user, MutableComment comment, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Updates a {@link Comment} and sets the comments updated date to be now and the updatedAuthor to be the
     * passed in user.
     *
     * @param user            the {@link ApplicationUser} who must have permission to update this comment and who will be used as the updateAuthor
     * @param updateCommentValidationResult the comment parameters.
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment update. If false then
     *                        the issue will not be reindexed.
     */
    void update(ApplicationUser user, CommentUpdateValidationResult updateCommentValidationResult, boolean dispatchEvent);

    /**
     * Will return a list of {@link Comment}s for the given user
     * Will return a list of {@link Comment}s for the given user.
     *
     * @param user            the user to check permissions against {@link com.atlassian.jira.user.ApplicationUser}, or anonymous if null.
     * @param issue           the issue with associated comments. Must not be null.
     * @return a possibly empty List of comments - will not be null.
     */
    @Nonnull
    public List<Comment> getCommentsForUser(@Nullable ApplicationUser user, @Nonnull Issue issue);

    /**
     * Will return a list of {@link Comment}s for the given user.
     *
     * @param currentUser     current {@link com.atlassian.jira.user.ApplicationUser}
     * @param issue           the issue with associated comments
     * @param errorCollection holder for any errors that were thrown attempting to retrieve comments
     * @return a List of comments
     *
     * @deprecated Use {@link #getCommentsForUser(ApplicationUser currentUser, Issue issue)} instead. Since v6.3.
     */
    @Deprecated
    public List<Comment> getCommentsForUser(ApplicationUser currentUser, Issue issue, ErrorCollection errorCollection);

    /**
     * Will return a list of {@link Comment}s for the given user.
     *
     * @param currentUser     current {@link User}
     * @param issue           the issue with associated comments
     * @param errorCollection holder for any errors that were thrown attempting to retrieve comments
     * @return a List of comments
     *
     * @deprecated Use {@link #getCommentsForUser(ApplicationUser currentUser, Issue issue)} instead. Since v6.3.
     */
    @Deprecated
    public List<Comment> getCommentsForUser(User currentUser, Issue issue, ErrorCollection errorCollection);

    /**
     * Will return a list of {@link Comment}s that were created or updated since the provided date, for the given user.
     *
     * @param user            the user to check permissions against {@link com.atlassian.jira.user.ApplicationUser}, or anonymous if null.
     * @param issue           the issue with associated comments. Must not be null.
     * @param since           only comments created or updated after this date will be returned. Must not be null.
     * @return a possibly empty List of comments - will not be null.
     * @since v6.3
     */
    @Nonnull
    public List<Comment> getCommentsForUserSince(@Nullable ApplicationUser user, @Nonnull Issue issue, @Nonnull Date since);

    public boolean hasPermissionToCreate(ApplicationUser user, Issue issue, ErrorCollection errorCollection);

    /**
     * Has the correct permission to create a comment for the given issue.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null.
     * @param errorCollection holder for any errors that were thrown attempting permission checks
     * @return true if permission check passes.
     *
     * @deprecated Use {@link #hasPermissionToCreate(ApplicationUser user, Issue issue, ErrorCollection errorCollection)} instead. Since v6.0.
     */
    @Deprecated
    public boolean hasPermissionToCreate(User user, Issue issue, ErrorCollection errorCollection);

    public boolean hasPermissionToEdit(ApplicationUser user, Comment comment, ErrorCollection errorCollection);

    /**
     * Determine whether the current user has the permission to edit the
     * comment. In case of errors, add error messages to the error collection.
     * <p/>
     * Passing in null comment or a comment with null ID will return false and
     * an error message will be added to the error collection.
     * <p/>
     * Passing in null error collection will throw NPE.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param comment         The {@link Comment} you wish to edit.
     * @param errorCollection holder for any errors that were thrown attempting permission checks
     * @return true if the user has edit permission, false otherwise
     *
     * @deprecated Use {@link #hasPermissionToCreate(ApplicationUser user, Issue issue, ErrorCollection errorCollection)} instead. Since v6.0.
     */
    @Deprecated
    public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection);

    /**
     * Validates that the body is a valid string, if not the appropriate error
     * is added to the <code>errorCollection</code>. This method was added so
     * the CommentSystemField can validate the body and set the appropriate error message.
     *
     * @param body            comment body to validate
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return true is the body is valid.
     */
    public boolean isValidCommentBody(String body, ErrorCollection errorCollection);

    /**
     * Validates that the body is a valid string, if not the appropriate error
     * is added to the <code>errorCollection</code>. This method was added so
     * the CommentSystemField can validate the body and set the appropriate error message.
     *
     * @param body            comment body to validate
     * @param errorCollection holder for any errors that can occur in process of validation
     * @param allowEmpty      indicates whether empty body is allowed
     * @return true is the body is valid.
     */
    public boolean isValidCommentBody(String body, ErrorCollection errorCollection, boolean allowEmpty);

    /**
     * This method validates if the comment has the correct role and group
     * levels set. If there is an error during validation the passed in
     * <code>errorCollection</code> will contain the errors.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           issue to associate the comment with
     * @param groupLevel      comment group visibility level
     * @param roleLevelId     comment project role visibility level id
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return true if the role and group level information has been set correctly for a comment
     *
     * @deprecated Use {@link #isValidCommentVisibility(ApplicationUser, Issue, com.atlassian.jira.bc.issue.visibility.Visibility, ErrorCollection)} instead. Since v6.4.
     */
    @Deprecated
    public boolean isValidCommentData(User user, Issue issue, String groupLevel, String roleLevelId, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #isValidCommentVisibility(ApplicationUser, Issue, com.atlassian.jira.bc.issue.visibility.Visibility, ErrorCollection)} instead. Since v6.4.
     */
    @Deprecated
    public boolean isValidCommentData(ApplicationUser user, Issue issue, String groupLevel, String roleLevelId, ErrorCollection errorCollection);

    /**
     * This method validates if the comment has the correct role and group
     * levels set. If there is an error during validation the passed in
     * <code>errorCollection</code> will contain the errors.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           issue to associate the comment with
     * @param visibility      comment visibility level
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return true if the role and group level information has been set correctly for a comment
     */
    public boolean isValidCommentVisibility(ApplicationUser user, Issue issue, Visibility visibility, ErrorCollection errorCollection);

    /**
     * Will call all other validate methods setting the appropriate errors
     * in the <code>errorCollection</code> if any errors occur.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           issue to associate the comment with
     * @param body            comment body
     * @param groupLevel      comment group visibility level
     * @param roleLevelId     comment project role visibility level id
     * @param errorCollection holder for any errors that can occur in process of validarion
     * @return true if validation passes
     *
     * @deprecated Use {@link #isValidAllCommentData(ApplicationUser, Issue, String, com.atlassian.jira.bc.issue.visibility.Visibility, ErrorCollection)} instead. Since v6.4.
     */
    @Deprecated
    public boolean isValidAllCommentData(User user, Issue issue, String body, String groupLevel, String roleLevelId, ErrorCollection errorCollection);

    /**
     * @deprecated Use {@link #isValidAllCommentData(ApplicationUser, Issue, String, com.atlassian.jira.bc.issue.visibility.Visibility, ErrorCollection)} instead. Since v6.4.
     */
    @Deprecated
    public boolean isValidAllCommentData(ApplicationUser user, Issue issue, String body, String groupLevel, String roleLevelId, ErrorCollection errorCollection);

    /**
     * Will call all other validate methods setting the appropriate errors
     * in the <code>errorCollection</code> if any errors occur.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           issue to associate the comment with
     * @param body            comment body
     * @param visibility      visibility level
     * @param errorCollection holder for any errors that can occur in process of validarion
     * @return true if validation passes
     */
    public boolean isValidAllCommentData(ApplicationUser user, Issue issue, String body, Visibility visibility, ErrorCollection errorCollection);

    /**
     * Returns the flag that indicates whether group visiblity is enabled
     *
     * @return true if enabled, false otherwise
     *
     * @deprecated Deprecated because of typo in signature. Use {@link #isGroupVisibilityEnabled()}. Since 6.4
     */
    @Deprecated
    boolean isGroupVisiblityEnabled();

    /**
     * Returns the flag that indicates whether project role visibility is enabled
     *
     * @return true if enabled, false otherwise
     *
     * @deprecated Deprecated because of typo in signature. Use {@link #isProjectRoleVisibilityEnabled()}. Since 6.4
     */
    @Deprecated
    boolean isProjectRoleVisiblityEnabled();

    /**
     * Returns the flag that indicates whether group visiblity is enabled
     *
     * @return true if enabled, false otherwise
     */
    boolean isGroupVisibilityEnabled();

    /**
     * Returns the flag that indicates whether project role visibility is enabled
     *
     * @return true if enabled, false otherwise
     */
    boolean isProjectRoleVisibilityEnabled();

    public Comment getCommentById(ApplicationUser user, Long commentId, ErrorCollection errorCollection);

    /**
     * Will return a comment for the passed in commentId. This will return null
     * if the user does not have permission to view the comment
     *
     * @param user            who is looking up the comment
     * @param commentId       the id representing the {@link Comment} you would like to retrieve.
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return a {@link Comment} or null  (if the user cannot browse the comment).
     *
     * @deprecated Use {@link #getCommentById(ApplicationUser user, Long commentId, ErrorCollection errorCollection)} instead. Since v6.0.
     */
    public Comment getCommentById(User user, Long commentId, ErrorCollection errorCollection);

    public MutableComment getMutableComment(ApplicationUser user, Long commentId, ErrorCollection errorCollection);

    /**
     * Will return a {@link MutableComment} for the passed in commentId. This
     * will return null if the user does not have permission to view the
     * comment. The difference between this method and
     * {@link #getCommentById(User, Long, ErrorCollection)} is that this method
     * returns a version of the {@link Comment} that we can set values on.
     *
     * @param user            the current user.
     * @param commentId       the id that we use to find the comment object.
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return the comment that is identified by the commentId.
     *
     * @deprecated Use {@link #getCommentById(com.atlassian.jira.user.ApplicationUser, Long, com.atlassian.jira.util.ErrorCollection)} instead. Since v6.0.
     */
    public MutableComment getMutableComment(User user, Long commentId, ErrorCollection errorCollection);

    /**
     * Determines whether the user can delete a comment. Will return true when the following are satisfied:
     * <ul>
     * <li>The user has the DELETE_COMMENT_ALL permission, or the user has the DELETE_COMMENT_OWN permission and is
     * attempting to delete a comment they authored</li>
     * <li>The issue is in an editable workflow state</li>
     * </ul>
     *
     * @param jiraServiceContext jiraServiceContext containing the user who wishes to delete a comment and the
     *                           errorCollection that will contain any errors encountered when calling the method
     * @param commentId          the id of the target comment (cannot be null)
     * @return true if the user has permission to delete the target comment, false otherwise
     */
    public boolean hasPermissionToDelete(JiraServiceContext jiraServiceContext, Long commentId);

    /**
     * Determines whether the user can edit a comment. Will return true when the following are satisfied:
     * <ul>
     * <li>The user has the comment edit all or comment edit own permission</li>
     * <li>The issue is in an editable workflow state</li>
     * </ul>
     *
     * @param jiraServiceContext JIRA service context containing the user who wishes to edit a comment and the
     *                           errorCollection that will contain any errors encountered when calling the method
     * @param commentId          the id of the target comment (cannot be null)
     * @return true if the user has permission to edit the comment, false otherwise
     */
    public boolean hasPermissionToEdit(JiraServiceContext jiraServiceContext, Long commentId);

    /**
     * Deletes a comment and updates the issue's change history and updated date. Expects that
     * {@link #hasPermissionToDelete(com.atlassian.jira.bc.JiraServiceContext, Long)} is successfully called first.
     *
     * @param jiraServiceContext containing the user who wishes to delete a comment and the errorCollection
     *                           that will contain any errors encountered when calling the method
     * @param comment            the comment to delete (cannot be null)
     * @param dispatchEvent      a flag indicating whether to dispatch an issue updated event. If this flag is false then
     *                           the issue will not be reindexed.
     */
    public void delete(JiraServiceContext jiraServiceContext, Comment comment, boolean dispatchEvent);

    final class CommentCreateValidationResult extends ServiceResultImpl
    {
        private final Option<CommentParameters> commentInputParameters;

        public CommentCreateValidationResult(ErrorCollection errorCollection, Option<CommentParameters> commentInputParameters)
        {
            super(errorCollection);
            this.commentInputParameters = commentInputParameters;
        }

        public Option<CommentParameters> getCommentInputParameters()
        {
            return commentInputParameters;
        }
    }

    final class CommentUpdateValidationResult extends ServiceResultImpl
    {
        private final Option<Comment> comment;
        private final Option<Map<String, JSONObject>> commentProperties;

        public CommentUpdateValidationResult(SimpleErrorCollection errorCollection, Option<Map<String, JSONObject>> commentProperties, Option<Comment> comment)
        {
            super(errorCollection);
            this.commentProperties = commentProperties;
            this.comment = comment;
        }

        public Option<Comment> getComment()
        {
            return comment;
        }

        public Option<Map<String, JSONObject>> getCommentProperties()
        {
            return commentProperties;
        }
    }

    final class CommentVisibility
    {
        private final String groupLevel;

        private final Long roleLevelId;

        public CommentVisibility(final String groupLevel, final Long roleLevelId)
        {
            this.groupLevel = groupLevel;
            this.roleLevelId = roleLevelId;
        }

        public String getGroupLevel()
        {
            return groupLevel;
        }

        public Long getRoleLevelId()
        {
            return roleLevelId;
        }
    }

    final class CommentParameters
    {
        private final String body;
        private final String groupLevel;
        private final Long roleLevelId;
        private final Visibility visibility;
        private final Date created;
        private final ApplicationUser author;
        private final Issue issue;
        private final Map<String, JSONObject> commentProperties;

        private CommentParameters(String body, String groupLevel, Long roleLevelId, Visibility visibility, Date created, ApplicationUser author,
                Issue issue, Map<String, JSONObject> commentProperties)
        {
            this.body = body;
            this.groupLevel = groupLevel;
            this.roleLevelId = roleLevelId;
            this.visibility = visibility;
            this.created = created;
            this.author = author;
            this.issue = issue;
            this.commentProperties = commentProperties;
        }

        public static CommentParametersBuilder builder()
        {
            return new CommentParametersBuilder();
        }

        public static CommentParametersBuilder builder(Comment comment)
        {
            return new CommentParametersBuilder()
                    .author(comment.getAuthorApplicationUser())
                    .body(comment.getBody())
                    .groupLevel(comment.getGroupLevel())
                    .roleLevelId(comment.getRoleLevelId())
                    .issue(comment.getIssue())
                    .created(comment.getCreated());
        }

        public String getBody()
        {
            return body;
        }

        /**
         * @deprecated Use {@link #getVisibility()} instead. Since 6.4
         */
        @Deprecated
        public String getGroupLevel()
        {
            return groupLevel;
        }

        /**
         * @deprecated Use {@link #getVisibility()} instead. Since 6.4
         */
        @Deprecated
        public Long getRoleLevelId()
        {
            return roleLevelId;
        }

        public Date getCreated()
        {
            return created;
        }

        public ApplicationUser getAuthor()
        {
            return author;
        }

        public Issue getIssue()
        {
            return issue;
        }

        @Nonnull
        public Map<String, JSONObject> getCommentProperties()
        {
            return commentProperties;
        }

        public Visibility getVisibility()
        {
            return visibility;
        }

        public static class CommentParametersBuilder
        {
            private String body;
            private String groupLevel;
            private Long roleLevelId;
            private Visibility visibility;
            private Date created;
            private ApplicationUser author;
            private Issue issue;
            private Map<String, JSONObject> commentProperties = new HashMap<String, JSONObject>();

            public CommentParametersBuilder body(final String body)
            {
                this.body = body;
                return this;
            }

            public CommentParametersBuilder issue(final Issue issue)
            {
                this.issue = issue;
                return this;
            }

            /**
             * @deprecated Use {@link #visibility(com.atlassian.jira.bc.issue.visibility.Visibility)} instead. Since 6.4
             */
            @Deprecated
            public CommentParametersBuilder groupLevel(final String groupLevel)
            {
                this.groupLevel = groupLevel;
                return this;
            }

            /**
             * @deprecated Use {@link #visibility(com.atlassian.jira.bc.issue.visibility.Visibility)} instead. Since 6.4
             */
            @Deprecated
            public CommentParametersBuilder roleLevelId(final Long roleLevelId)
            {
                this.roleLevelId = roleLevelId;
                return this;
            }

            public CommentParametersBuilder visibility(final Visibility visibility)
            {
                this.visibility = visibility;
                return this;
            }

            public CommentParametersBuilder created(final Date created)
            {
                this.created = created;
                return this;
            }

            public CommentParametersBuilder author(final ApplicationUser author)
            {
                this.author = author;
                return this;
            }

            public CommentParametersBuilder commentProperties(final Map<String, JSONObject> commentProperties)
            {
                this.commentProperties.putAll(commentProperties);
                return this;
            }

            public CommentParameters build()
            {
                if (visibility == null)
                {
                    visibility = Visibilities.fromGroupAndRoleId(groupLevel, roleLevelId);
                }
                return new CommentParameters(body, groupLevel, roleLevelId, visibility, created, author, issue, commentProperties);
            }
        }
    }

}
