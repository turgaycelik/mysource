package com.atlassian.jira.bc.issue.comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.bc.issue.visibility.VisibilityVisitors;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comments.CommentPermissionManager;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyInput;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of comment service.
 */
public class DefaultCommentService implements CommentService
{
    private static final String ERROR_NO_PERMISSION = "comment.service.error.no.permission";
    private static final String ERROR_NO_PERMISSION_NO_USER = "comment.service.error.no.permission.no.user";
    private static final String ERROR_NO_EDIT_PERMISSION = "comment.service.error.no.edit.permission";
    private static final String ERROR_NO_EDIT_PERMISSION_NO_USER = "comment.service.error.no.edit.permission.no.user";
    private static final String ERROR_NO_COMMENT_VISIBILITY = "comment.service.error.no.comment.visibility";
    private static final String ERROR_NO_COMMENT_VISIBILITY_NO_USER = "comment.service.error.no.comment.visibility.no.user";
    private static final String ERROR_NULL_ISSUE = "comment.service.error.issue.null";
    private static final String ERROR_NULL_BODY = "comment.service.error.body.null";
    private static final String ERROR_NO_COMMENT_FOR_ID = "comment.service.error.no.comment.for.id";
    private static final String ERROR_NO_ID_SPECIFIED = "comment.service.error.no.id.specified";
    public static final String ERROR_NULL_COMMENT_ID = "comment.service.error.update.null.comment.id";
    public static final String ERROR_NULL_COMMENT = "comment.service.error.update.null.comment";
    public static final String ERROR_NULL_COMMENT_DELETE = "comment.service.error.delete.null.comment";
    public static final String ERROR_NULL_COMMENT_ID_DELETE = "comment.service.error.delete.null.comment.id";
    public static final String ERROR_COMMENT_DELETE_ISSUE_UPDATE_FAILED = "comment.service.error.delete.issue.update.failed";
    public static final String ERROR_COMMENT_EDIT_NON_EDITABLE_ISSUE = "comment.service.error.edit.issue.non.editable";
    public static final String ERROR_COMMENT_DELETE_NON_EDITABLE_ISSUE = "comment.service.error.delete.issue.non.editable";
    public static final String ERROR_COMMENT_DELETE_NO_PERMISSION = "comment.service.error.delete.no.permission";

    private static final String COMMENT_I18N_PREFIX = "comment";
    public static final String ERROR_BODY_TOOLONG = "field.error.text.toolong";
    private final CommentManager commentManager;

    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final CommentPermissionManager commentPermissionManager;
    private final IssueUpdater issueUpdater;
    private final IssueManager issueManager;
    private final VisibilityValidator visibilityValidator;
    private final TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;
    private final CommentPropertyService commentPropertyService;

    public DefaultCommentService(CommentManager commentManager, PermissionManager permissionManager,
            JiraAuthenticationContext jiraAuthenticationContext,
            CommentPermissionManager commentPermissionManager, IssueUpdater issueUpdater,
            IssueManager issueManager, VisibilityValidator visibilityValidator,
            TextFieldCharacterLengthValidator textFieldCharacterLengthValidator)
    {
        this.commentManager = commentManager;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.commentPermissionManager = commentPermissionManager;
        this.issueUpdater = issueUpdater;
        this.issueManager = issueManager;
        this.visibilityValidator = visibilityValidator;
        this.textFieldCharacterLengthValidator = textFieldCharacterLengthValidator;
        this.commentPropertyService = ComponentAccessor.getComponent(CommentPropertyService.class);
    }

    @Override
    public void validateCommentUpdate(ApplicationUser user, Long commentId, String body, String groupLevel, Long roleLevelId, ErrorCollection errorCollection)
    {
        final CommentParameters commentParameters = CommentParameters.builder()
                .body(body)
                .visibility(Visibilities.fromGroupAndRoleId(groupLevel, roleLevelId))
                .build();
        CommentUpdateValidationResult commentValidationResult = validateCommentUpdate(user, commentId, commentParameters);
        errorCollection.addErrorCollection(commentValidationResult.getErrorCollection());
    }

    @Override
    public CommentUpdateValidationResult validateCommentUpdate(final ApplicationUser user, final Long commentId, final CommentParameters commentParameters)
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        if (commentId == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT_ID));
            return new CommentUpdateValidationResult(errorCollection, Option.<Map<String, JSONObject>>none(), Option.<Comment>none());
        }

        // get the mutable comment
        MutableComment comment = getMutableComment(user, commentId, errorCollection);
        if (comment == null)
        {
            // no need to add any message as getMutableComment() did it already
            return new CommentUpdateValidationResult(errorCollection, Option.<Map<String, JSONObject>>none(), Option.<Comment>none());
        }

        hasPermissionToEdit(user, comment, errorCollection);
        if (errorCollection.hasAnyErrors())
        {
            return new CommentUpdateValidationResult(errorCollection, Option.<Map<String, JSONObject>>none(), Option.<Comment>none());
        }

        CommentParameters parameters = CommentParameters.builder()
                .body(commentParameters.getBody())
                .commentProperties(commentParameters.getCommentProperties())
                .visibility(commentParameters.getVisibility())
                .created(comment.getCreated())
                .author(comment.getAuthorApplicationUser())
                .issue(comment.getIssue())
                .build();

        if (validInput(user, parameters, errorCollection))
        {
            comment.setBody(commentParameters.getBody());
            final String groupLevel = commentParameters.getVisibility().accept(VisibilityVisitors.returningGroupLevelVisitor()).getOrNull();
            final Long roleLevelId = commentParameters.getVisibility().accept(VisibilityVisitors.returningRoleLevelIdVisitor()).getOrNull();
            comment.setGroupLevel(groupLevel);
            comment.setRoleLevelId(roleLevelId);
            comment.setUpdateAuthor(user);
            comment.setUpdated(new Date());

            return new CommentUpdateValidationResult(errorCollection, Option.some(parameters.getCommentProperties()), Option.<Comment>some(comment));
        }
        else
        {
            return new CommentUpdateValidationResult(errorCollection, Option.<Map<String, JSONObject>>none(), Option.<Comment>none());
        }
    }

    @Override
    public void validateCommentUpdate(User user, Long commentId, String body, String groupLevel, Long roleLevelId, ErrorCollection errorCollection)
    {
        validateCommentUpdate(ApplicationUsers.from(user), commentId, body, groupLevel, roleLevelId, errorCollection);
    }

    @Override
    public void update(User user, MutableComment comment, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        update(ApplicationUsers.from(user), comment, dispatchEvent, errorCollection);
    }

    @Override
    public void update(ApplicationUser user, MutableComment comment, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        if (comment == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT));
            return;
        }
        CommentParameters parameters = CommentParameters.builder()
                .author(comment.getAuthorApplicationUser())
                .body(comment.getBody())
                .visibility(Visibilities.fromGroupAndRoleId(comment.getGroupLevel(), comment.getRoleLevelId()))
                .created(comment.getCreated())
                .build();

        CommentUpdateValidationResult commentValidationResult = validateCommentUpdate(user, comment.getId(), parameters);

        if (commentValidationResult.isValid())
        {
            update(user, commentValidationResult, dispatchEvent);
        }
        else
        {
            errorCollection.addErrorCollection(commentValidationResult.getErrorCollection());
        }
    }

    @Override
    public void update(final ApplicationUser user, final CommentUpdateValidationResult validationResult, final boolean dispatchEvent)
    {
        validationResult.getComment().foreach(new Effect<Comment>()
        {
            @Override
            public void apply(final Comment comment)
            {
                commentManager.update(comment, validationResult.getCommentProperties().getOrElse(Collections.EMPTY_MAP), dispatchEvent);
            }
        });
    }

    @Override
    public CommentCreateValidationResult validateCommentCreate(final ApplicationUser user, @Nonnull final CommentParameters commentParameters)
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        if (hasPermissionToCreate(user, commentParameters.getIssue(), errorCollection)
                && validInput(user, commentParameters, errorCollection))
        {
            return new CommentCreateValidationResult(errorCollection, Option.some(commentParameters));
        }
        else
        {
            return new CommentCreateValidationResult(errorCollection, Option.<CommentParameters>none());
        }
    }

    private boolean validInput(ApplicationUser user, CommentParameters commentParameters, ErrorCollection errorCollection)
    {
        return isValidAllCommentData(user,
                commentParameters.getIssue(),
                commentParameters.getBody(),
                commentParameters.getVisibility(),
                errorCollection)
                && validCommentProperty(commentParameters, errorCollection);
    }

    private boolean validCommentProperty(final CommentParameters commentParameters, ErrorCollection errorCollection)
    {
        for (Map.Entry<String, JSONObject> property : commentParameters.getCommentProperties().entrySet())
        {
            errorCollection.addErrorCollection(commentPropertyService.validatePropertyInput(new PropertyInput(property.getValue().toString(), property.getKey())));
        }
        return !errorCollection.hasAnyErrors();
    }

    @Override
    public Comment create(User user, Issue issue, String body, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        ApplicationUser applicationUser = ApplicationUsers.from(user);
        return create(applicationUser, issue, body, dispatchEvent, errorCollection);
    }

    @Override
    public Comment create(ApplicationUser user, Issue issue, String body, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        CommentCreateValidationResult validationResult = validateCommentCreate(user, CommentParameters.builder().body(body).issue(issue).build());

        if (validationResult.isValid())
        {
            return create(user, validationResult, dispatchEvent);
        }
        else
        {
            errorCollection.addErrorCollection(validationResult.getErrorCollection());
            return null;
        }
    }

    @Override
    public Comment create(User user, Issue issue, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        return create(ApplicationUsers.from(user), issue, body, groupLevel, roleLevelId, dispatchEvent, errorCollection);
    }

    @Override
    public Comment create(ApplicationUser user, Issue issue, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        CommentParameters commentParameters = CommentParameters.builder()
                .body(body)
                .visibility(Visibilities.fromGroupAndRoleId(groupLevel, roleLevelId))
                .issue(issue)
                .author(user)
                .build();

        CommentCreateValidationResult validationResult = validateCommentCreate(user, commentParameters);

        if (validationResult.isValid())
        {
            return create(user, validationResult, dispatchEvent);
        }
        else
        {
            errorCollection.addErrorCollection(validationResult.getErrorCollection());
            return null;
        }
    }

    @Override
    public Comment create(User user, Issue issue, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        return create(ApplicationUsers.from(user), issue, body, groupLevel, roleLevelId, created, dispatchEvent, errorCollection);
    }

    @Override
    public Comment create(ApplicationUser user, Issue issue, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        CommentParameters commentParameters = CommentParameters.builder()
                .body(body)
                .visibility(Visibilities.fromGroupAndRoleId(groupLevel, roleLevelId))
                .created(created)
                .issue(issue)
                .build();

        CommentCreateValidationResult validationResult = validateCommentCreate(user, commentParameters);

        if (validationResult.isValid())
        {
            return create(user, validationResult, dispatchEvent);
        }
        else
        {
            errorCollection.addErrorCollection(validationResult.getErrorCollection());
            return null;
        }
    }

    @Override
    public Comment create(final ApplicationUser applicationUser, final CommentCreateValidationResult validationResult, final boolean dispatchEvent)
    {
        return validationResult.getCommentInputParameters().fold(new Supplier<Comment>()
        {
            @Override
            public Comment get()
            {
                return null;
            }
        }, new Function<CommentParameters, Comment>()
        {
            @Override
            public Comment apply(final CommentParameters parameters)
            {
                final String groupLevel = parameters.getVisibility().accept(VisibilityVisitors.returningGroupLevelVisitor()).getOrNull();
                final Long roleLevelId = parameters.getVisibility().accept(VisibilityVisitors.returningRoleLevelIdVisitor()).getOrNull();
                return commentManager.create(
                        parameters.getIssue(),
                        applicationUser,
                        parameters.getBody(),
                        groupLevel,
                        roleLevelId,
                        parameters.getCreated(),
                        parameters.getCommentProperties(),
                        dispatchEvent
                );
            }
        });
    }

    @Override
    public List<Comment> getCommentsForUser(ApplicationUser currentUser, Issue issue, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        List<Comment> comments = new ArrayList<Comment>();

        if (issue == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_ISSUE));
            internalError = true;
        }

        if (!internalError)
        {
            comments = commentManager.getCommentsForUser(issue, currentUser);
        }

        return comments;
    }

    @Override
    public List<Comment> getCommentsForUser(User currentUser, Issue issue, ErrorCollection errorCollection)
    {
        return getCommentsForUser(ApplicationUsers.from(currentUser), issue);
    }

    @Override
    @Nonnull
    public List<Comment> getCommentsForUser(@Nullable ApplicationUser user, @Nonnull Issue issue)
    {
        notNull("issue", issue);
        return commentManager.getCommentsForUser(issue, user);
    }

    @Override
    @Nonnull
    public List<Comment> getCommentsForUserSince(@Nullable final ApplicationUser currentUser, @Nonnull final Issue issue, @Nonnull final Date since)
    {
        notNull("issue", issue);
        notNull("since", since);
        return commentManager.getCommentsForUserSince(issue, currentUser, since);
    }

    @Override
    public boolean isValidCommentData(ApplicationUser currentUser, Issue issue, String groupLevel, String roleLevelId, ErrorCollection errorCollection)
    {
        final Visibility visibility = Visibilities.fromGroupAndStrRoleId(groupLevel, roleLevelId);
        return isValidCommentVisibility(currentUser, issue, visibility, errorCollection);
    }

    @Override
    public boolean isValidCommentData(User user, Issue issue, String groupLevel, String roleLevelId, ErrorCollection errorCollection)
    {
        final Visibility visibility = Visibilities.fromGroupAndStrRoleId(groupLevel, roleLevelId);
        return isValidCommentVisibility(ApplicationUsers.from(user), issue, visibility, errorCollection);
    }

    @Override
    public boolean isValidCommentVisibility(ApplicationUser currentUser, Issue issue, Visibility visibility, ErrorCollection errorCollection)
    {
        return visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(currentUser, errorCollection), "comment", issue, visibility);
    }

    @Override
    public boolean isValidCommentBody(String body, ErrorCollection errorCollection)
    {
        return isValidCommentBody(body, errorCollection, false);
    }

    @Override
    public boolean isValidCommentBody(String body, ErrorCollection errorCollection, boolean allowEmpty)
    {
        boolean valid = true;
        if (!allowEmpty && StringUtils.isBlank(body))
        {
            valid = false;
            errorCollection.addError("comment", getText(ERROR_NULL_BODY), Reason.VALIDATION_FAILED);
        }
        if (textFieldCharacterLengthValidator.isTextTooLong(body))
        {
            valid = false;
            final long maximumNumberOfCharacters = textFieldCharacterLengthValidator.getMaximumNumberOfCharacters();
            errorCollection.addError("comment", getText(ERROR_BODY_TOOLONG, String.valueOf(maximumNumberOfCharacters)));
        }

        return valid;
    }

    @Override
    public boolean isValidAllCommentData(ApplicationUser currentUser, Issue issue, String body, String groupLevel, String roleLevelId, ErrorCollection errorCollection)
    {
        final Visibility visibility = Visibilities.fromGroupAndStrRoleId(groupLevel, roleLevelId);
        return isValidAllCommentData(currentUser, issue, body, visibility, errorCollection);
    }

    @Override
    public boolean isValidAllCommentData(User user, Issue issue, String body, String groupLevel, String roleLevelId, ErrorCollection errorCollection)
    {
        final Visibility visibility = Visibilities.fromGroupAndStrRoleId(groupLevel, roleLevelId);
        return isValidAllCommentData(ApplicationUsers.from(user), issue, body, visibility, errorCollection);
    }

    @Override
    public boolean isValidAllCommentData(ApplicationUser currentUser, Issue issue, String body, Visibility visibility, ErrorCollection errorCollection)
    {
        boolean validCommentBody = isValidCommentBody(body, errorCollection);
        boolean validCommentData = isValidCommentVisibility(currentUser, issue, visibility, errorCollection);
        return validCommentBody && validCommentData;
    }

    @Override
    public boolean isGroupVisiblityEnabled()
    {
        return isGroupVisibilityEnabled();
    }

    @Override
    public boolean isProjectRoleVisiblityEnabled()
    {
        return isProjectRoleVisibilityEnabled();
    }

    @Override
    public boolean isGroupVisibilityEnabled()
    {
        return visibilityValidator.isGroupVisibilityEnabled();
    }

    @Override
    public boolean isProjectRoleVisibilityEnabled()
    {
        return visibilityValidator.isProjectRoleVisibilityEnabled();
    }

    @Override
    public Comment getCommentById(ApplicationUser user, Long commentId, ErrorCollection errorCollection)
    {
        return getMutableComment(user, commentId, errorCollection);
    }

    @Override
    public Comment getCommentById(User user, Long commentId, ErrorCollection errorCollection)
    {
        return getCommentById(ApplicationUsers.from(user), commentId, errorCollection);
    }

    @Override
    public MutableComment getMutableComment(ApplicationUser user, Long commentId, ErrorCollection errorCollection)
    {
        if (commentId == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NO_ID_SPECIFIED));
            return null;
        }
        MutableComment comment = commentManager.getMutableComment(commentId);

        // Check that the comment exists
        if (comment == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NO_COMMENT_FOR_ID, commentId.toString()));
            return null;
        }

        if (commentPermissionManager.hasBrowsePermission(user, comment))
        {
            return comment;
        }
        else
        {
            if (user == null)
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_PERMISSION_NO_USER));
            }
            else
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_PERMISSION, user.getDisplayName()));
            }
        }
        return null;
    }

    @Override
    public MutableComment getMutableComment(User user, Long commentId, ErrorCollection errorCollection)
    {
        return getMutableComment(ApplicationUsers.from(user), commentId, errorCollection);
    }

    public boolean hasPermissionToDelete(JiraServiceContext jiraServiceContext, Long commentId)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();

        // This will do the checks against the commentId and comment object existing
        Comment comment = getCommentById(user, commentId, errorCollection);

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        // Do a check to make sure that the user is a member of the role or group if the comment is protected by
        // any of these visibility levels
        if (!hasVisibility(jiraServiceContext, comment))
        {
            return false;
        }

        Issue issue = comment.getIssue();
        if (!isIssueInEditableWorkflowState(issue))
        {
            errorCollection.addErrorMessage(getText(ERROR_COMMENT_DELETE_NON_EDITABLE_ISSUE));
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        if (userHasCommentDeleteAllPermission(issue, user)
                || (userHasCommentDeleteOwnPermission(issue, user) && commentManager.isUserCommentAuthor(user, comment)))
        {
            return true;
        }

        // Add an error about not having permission
        errorCollection.addErrorMessage(getText(ERROR_COMMENT_DELETE_NO_PERMISSION, String.valueOf(comment.getId())));
        jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
        return false;
    }

    protected boolean hasVisibility(JiraServiceContext jiraServiceContext, Comment comment)
    {
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();

        final Issue issue = comment.getIssue();

        // Do a check to make sure that the user is a member of the role or group if the worklog is protected by
        // any of these visibility levels
        boolean visible = visibilityValidator.isValidVisibilityData(
                new JiraServiceContextImpl(user, errorCollection),
                COMMENT_I18N_PREFIX,
                issue,
                Visibilities.fromGroupAndRoleId(comment.getGroupLevel(), comment.getRoleLevelId()));

        if (!visible)
        {
            if (user != null)
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_COMMENT_VISIBILITY, user.getDisplayName()));
            }
            else
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_COMMENT_VISIBILITY_NO_USER));
            }
        }
        return visible;
    }

    public void delete(JiraServiceContext jiraServiceContext, Comment comment, boolean dispatchEvent)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        // Check that the comment exists
        if (comment == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT_DELETE, null));
            return;
        }

        if (comment.getId() == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT_ID_DELETE));
            return;
        }

        // Re-do the permission check
        if (hasPermissionToDelete(jiraServiceContext, comment.getId()))
        {
            // Do the actual delete and get the change item caused by the delete
            ChangeItemBean changeItem = commentManager.delete(comment);

            // Persist the changeItem and update the issues update date
            doUpdateWithChangelog(EventType.ISSUE_COMMENT_DELETED_ID, ImmutableList.of(changeItem), comment.getIssue(), jiraServiceContext.getLoggedInApplicationUser(), dispatchEvent);
        }

    }

    @Override
    public boolean hasPermissionToCreate(ApplicationUser user, Issue issue, ErrorCollection errorCollection)
    {
        boolean hasPerm = permissionManager.hasPermission(ProjectPermissions.ADD_COMMENTS, issue, user);

        if (!hasPerm)
        {
            //JRA-11539 ApplicationUser may be null if the session has timed out or the user has logged out while entering the comment
            if (user != null)
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_PERMISSION, user.getDisplayName()), Reason.FORBIDDEN);
            }
            else
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_PERMISSION_NO_USER), Reason.FORBIDDEN);
            }
        }
        return hasPerm;
    }

    @Override
    public boolean hasPermissionToCreate(User user, Issue issue, ErrorCollection errorCollection)
    {
        return hasPermissionToCreate(ApplicationUsers.from(user), issue, errorCollection);
    }

    @Override
    public boolean hasPermissionToEdit(JiraServiceContext jiraServiceContext, Long commentId)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final ApplicationUser user = jiraServiceContext.getLoggedInApplicationUser();

        // This will do the checks against the commentId and comment object existing
        final Comment comment = getCommentById(user, commentId, errorCollection);

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        return hasPermissionToEdit(user, comment, jiraServiceContext.getErrorCollection());
    }

    @Override
    public boolean hasPermissionToEdit(ApplicationUser user, Comment comment, ErrorCollection errorCollection)
    {
        if (comment == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT));
            return false;
        }
        if (comment.getId() == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT_ID));
            return false;
        }

        if (!isIssueInEditableWorkflowState(comment.getIssue()))
        {
            errorCollection.addErrorMessage(getText(ERROR_COMMENT_EDIT_NON_EDITABLE_ISSUE));
            return false;
        }

        // Do a check to make sure that the user is a member of the role or group if the comment is protected by
        // any of these visibility levels
        if (!hasVisibility(new JiraServiceContextImpl(user, errorCollection), comment))
        {
            return false;
        }

        boolean hasPerm = commentPermissionManager.hasEditPermission(user, comment);
        if (!hasPerm)
        {
            //JRA-11539 ApplicationUser may be null if the session has timed out or the user has logged out while entering the comment
            if (user != null)
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_EDIT_PERMISSION, user.getDisplayName()));
            }
            else
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_EDIT_PERMISSION_NO_USER));
            }
        }
        return hasPerm;
    }

    @Override
    public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
    {
        return hasPermissionToEdit(ApplicationUsers.from(user), comment, errorCollection);
    }

    /**
     * This method 'completes' the update of an issue entity.
     * <p/>
     * It sets the update timestamp, stores the issue, updated the cache if needed,
     * creates the changelog and dispatches the event (if desired).
     * <p/>
     * This method will ALWAYS generate an update - see also doUpdateIfNeeded.
     *
     * @param eventTypeId   event type id
     * @param changeItems   list of change items
     * @param issue         issue to update
     * @param user          user performing this operation
     * @param dispatchEvent dispatch event flag
     * @throws JiraException if update fails
     */
    protected void doUpdateWithChangelog(Long eventTypeId, List<ChangeItemBean> changeItems, Issue issue, ApplicationUser user, boolean dispatchEvent)
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issue.getGenericValue(), issue.getGenericValue(), eventTypeId, user);
        issueUpdateBean.setChangeItems(changeItems);
        if (dispatchEvent)
        {
            issueUpdateBean.setDispatchEvent(true);
        }

        issueUpdater.doUpdate(issueUpdateBean, false);

    }

    boolean isIssueInEditableWorkflowState(Issue issue)
    {
        return issueManager.isEditable(issue);
    }

    boolean userHasCommentDeleteAllPermission(Issue issue, ApplicationUser user)
    {
        return permissionManager.hasPermission(ProjectPermissions.DELETE_ALL_COMMENTS, issue, user);
    }

    boolean userHasCommentDeleteOwnPermission(Issue issue, ApplicationUser user)
    {
        return permissionManager.hasPermission(ProjectPermissions.DELETE_OWN_COMMENTS, issue, user);
    }

    private String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }

    private String getText(String key, String param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }

}
