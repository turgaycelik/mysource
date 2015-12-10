package com.atlassian.jira.issue.comments;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyHelper;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraDateUtils;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.json.JSONObject;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

public class DefaultCommentManager implements CommentManager
{
    private final UserManager userManager;
    private final ProjectRoleManager projectRoleManager;
    private final CommentPermissionManager commentPermissionManager;
    private final OfBizDelegator delegator;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;
    private final JsonEntityPropertyManager jsonEntityPropertyManager;
    private final CommentPropertyHelper commentPropertyHelper;
    private final CommentSearchManager commentSearchManager;
    private final IssueEventManager issueEventManager;
    private final IssueEventBundleFactory issueEventBundleFactory;

    private static final String COMMENT_ID = "id";
    public static final String COMMENT_ENTITY = "Action";

    public DefaultCommentManager(ProjectRoleManager projectRoleManager,
            CommentPermissionManager commentPermissionManager, OfBizDelegator delegator, JiraAuthenticationContext jiraAuthenticationContext,
            TextFieldCharacterLengthValidator textFieldCharacterLengthValidator, UserManager userManager,
            IssueEventManager issueEventManager, IssueEventBundleFactory issueEventBundleFactory)
    {
        this(projectRoleManager, commentPermissionManager, delegator, jiraAuthenticationContext,
                textFieldCharacterLengthValidator, userManager, ComponentAccessor.getComponent(JsonEntityPropertyManager.class),
                ComponentAccessor.getComponent(CommentPropertyHelper.class), ComponentAccessor.getComponent(CommentSearchManager.class),
                issueEventManager, issueEventBundleFactory);
    }

    public DefaultCommentManager(ProjectRoleManager projectRoleManager,
            CommentPermissionManager commentPermissionManager, OfBizDelegator delegator,
            JiraAuthenticationContext jiraAuthenticationContext, TextFieldCharacterLengthValidator textFieldCharacterLengthValidator,
            UserManager userManager, JsonEntityPropertyManager jsonEntityPropertyManager, CommentPropertyHelper commentPropertyHelper,
            CommentSearchManager commentSearchManager, IssueEventManager issueEventManager, IssueEventBundleFactory issueEventBundleFactory)
    {
        this.commentSearchManager = commentSearchManager;
        this.projectRoleManager = projectRoleManager;
        this.commentPermissionManager = commentPermissionManager;
        this.delegator = delegator;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.textFieldCharacterLengthValidator = textFieldCharacterLengthValidator;
        this.userManager = userManager;
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
        this.commentPropertyHelper = commentPropertyHelper;
        this.issueEventManager = issueEventManager;
        this.issueEventBundleFactory = issueEventBundleFactory;
    }

    @Override
    public ProjectRole getProjectRole(Long projectRoleId)
    {
        return projectRoleManager.getProjectRole(projectRoleId);
    }

    @Override
    public Comment convertToComment(GenericValue gv)
    {
        return commentSearchManager.convertToComment(gv);
    }

    @Override
    public Comment getCommentById(Long commentId)
    {
        return commentSearchManager.getCommentById(commentId);
    }

    @Override
    public MutableComment getMutableComment(Long commentId)
    {
        return commentSearchManager.getMutableComment(commentId);
    }

    @Override
    public List<Comment> getCommentsForUser(Issue issue, ApplicationUser user)
    {
        return commentSearchManager.getCommentsForUser(issue, user);
    }

    @Override
    public Comment getLastComment(Issue issue)
    {
        final GenericValue commentGV = Select.from(COMMENT_ENTITY)
                .whereEqual("issue", issue.getId())
                .andEqual("type", ActionConstants.TYPE_COMMENT)
                .orderBy("created DESC")
                .limit(1)
                .runWith(delegator)
                .singleValue();

        return commentGV == null ? null : convertToComment(commentGV);
    }

    @Override
    public List<Comment> getCommentsForUser(Issue issue, User user)
    {
        return getCommentsForUser(issue, ApplicationUsers.from(user));
    }

    @Override
    @Nonnull
    public List<Comment> getCommentsForUserSince(@Nonnull Issue issue, @Nullable ApplicationUser user, @Nonnull Date since)
    {
        return commentSearchManager.getCommentsForUserSince(issue, user, since);
    }

    @Override
    public List<Comment> getComments(Issue issue)
    {
        return commentSearchManager.getComments(issue);
    }

    @Override
    public Comment create(Issue issue, ApplicationUser author, String body, boolean dispatchEvent)
    {
        return create(issue, author, body, null, null, dispatchEvent);
    }

    @Override
    public Comment create(Issue issue, String author, String body, boolean dispatchEvent)
    {
        return create(issue, userManager.getUserByKeyEvenWhenUnknown(author), body, dispatchEvent);
    }

    @Override
    public Comment create(Issue issue, ApplicationUser author, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent)
            throws DataAccessException
    {
        return create(issue, author, body, groupLevel, roleLevelId, new Date(), dispatchEvent);
    }

    @Override
    public Comment create(Issue issue, String author, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent)
    {
        return create(issue, userManager.getUserByKeyEvenWhenUnknown(author), body, groupLevel, roleLevelId, dispatchEvent);
    }

    @Override
    public Comment create(Issue issue, ApplicationUser author, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent)
            throws DataAccessException
    {
        return create(issue, author, author, body, groupLevel, roleLevelId, created, created, dispatchEvent);
    }

    @Override
    public Comment create(Issue issue, ApplicationUser author, String body, String groupLevel, Long roleLevelId, Date created, Map<String, JSONObject> commentProperties, boolean dispatchEvent)
    {
        return create(issue, author, author, body, groupLevel, roleLevelId, created, created, commentProperties, dispatchEvent, true);
    }

    @Override
    public Comment create(Issue issue, String author, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent)
    {
        return create(issue, userManager.getUserByKeyEvenWhenUnknown(author), body, groupLevel, roleLevelId, created, dispatchEvent);
    }

    @Override
    public Comment create(Issue issue, ApplicationUser author, ApplicationUser updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, boolean dispatchEvent)
            throws DataAccessException
    {
        return create(issue, author, updateAuthor, body, groupLevel, roleLevelId, created, updated, dispatchEvent, true);
    }

    @Override
    public Comment create(Issue issue, String author, String updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, boolean dispatchEvent)
    {
        return create(issue, userManager.getUserByKeyEvenWhenUnknown(author), userManager.getUserByKeyEvenWhenUnknown(updateAuthor), body, groupLevel, roleLevelId, created, updated, dispatchEvent);
    }

    @Override
    public Comment create(Issue issue, ApplicationUser author, ApplicationUser updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, boolean dispatchEvent, boolean modifyIssueUpdateDate)
    {
        return create(issue, author, updateAuthor, body, groupLevel, roleLevelId, created, updated, Collections.EMPTY_MAP, dispatchEvent, modifyIssueUpdateDate);
    }

    public Comment create(Issue issue, ApplicationUser author, ApplicationUser updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, Map<String, JSONObject> commentProperties, boolean dispatchEvent, boolean modifyIssueUpdateDate)
    {

        if (textFieldCharacterLengthValidator.isTextTooLong(body))
        {
            final long maximumNumberOfCharacters = textFieldCharacterLengthValidator.getMaximumNumberOfCharacters();
            String errorMessage = getText("field.error.text.toolong", String.valueOf(maximumNumberOfCharacters));
            throw new IllegalArgumentException(errorMessage);
        }

        // create new instance of comment
        CommentImpl comment = new CommentImpl(projectRoleManager, author, updateAuthor, body, groupLevel, roleLevelId, created, updated, issue);

        // create persistable generic value
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("issue", issue.getId());
        fields.put("type", ActionConstants.TYPE_COMMENT);

        ApplicationUser commentAuthor = comment.getAuthorApplicationUser();
        ApplicationUser commentUpdateAuthor = comment.getUpdateAuthorApplicationUser();
        fields.put("author", commentAuthor == null ? null : commentAuthor.getKey());
        fields.put("updateauthor", commentUpdateAuthor == null ? null : commentUpdateAuthor.getKey());
        fields.put("body", comment.getBody());
        fields.put("level", comment.getGroupLevel());
        fields.put("rolelevel", comment.getRoleLevelId());
        fields.put("created", new Timestamp(comment.getCreated().getTime()));
        fields.put("updated", new Timestamp(comment.getUpdated().getTime()));

        GenericValue commentGV = EntityUtils.createValue(COMMENT_ENTITY, fields);
        // set the ID on comment object
        comment.setId(commentGV.getLong(COMMENT_ID));

        // Update the issue object if required
        if (modifyIssueUpdateDate)
        {
            // JRA-36334: Only modify the Issue updated date if it would move forward - we don't want it to go back in time
            if (comment.getUpdated().getTime() > issue.getUpdated().getTime())
            {
                IssueFactory issueFactory = ComponentAccessor.getComponentOfType(IssueFactory.class);
                MutableIssue mutableIssue = issueFactory.getIssue(issue.getGenericValue());
                //JRA-15723: Use the comments updated time for the updated time of the issue.  This allows users to
                // import old comments without setting the updated time on the issue to now, but to the date
                // of the old comments.
                mutableIssue.setUpdated(new Timestamp(comment.getUpdated().getTime()));
                issue.store();
            }
        }

        if (commentProperties != null)
        {
            setProperties(author, comment, commentProperties);
        }

        // Dispatch an event if required
        if (dispatchEvent)
        {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("eventsource", IssueEventSource.ACTION);
            dispatchIssueCommentAddedEvent(comment, params);
        }
        return comment;
    }

    @Override
    public Comment create(Issue issue, String author, String updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, boolean dispatchEvent, boolean modifyIssueUpdateDate)
    {
        return create(issue, userManager.getUserByKeyEvenWhenUnknown(author), userManager.getUserByKeyEvenWhenUnknown(updateAuthor), body, groupLevel, roleLevelId, created, updated, dispatchEvent, modifyIssueUpdateDate);
    }

    @Override
    public void update(Comment comment, boolean dispatchEvent)
    {
        update(comment, Collections.EMPTY_MAP, dispatchEvent);
    }

    @Override
    public void update(Comment comment, Map<String, JSONObject> commentProperties, boolean dispatchEvent)
    {
        if (comment == null)
        {
            throw new IllegalArgumentException("Comment must not be null");
        }
        if (comment.getId() == null)
        {
            throw new IllegalArgumentException("Comment ID must not be null");
        }

        // create persistable generic value
        GenericValue commentGV;

        // We need an in-memory copy of the old comment so we can pass it through in the fired event and to make sure
        // that some fields have changed.
        Comment originalComment = getCommentById(comment.getId());
        if (originalComment == null)
        {
            throw new IllegalArgumentException("Can not find a comment in the datastore with id: " + comment.getId());
        }

        // Make sure that either the comment body or visibility data has changed, otherwise do not update the datastore
        if (!areCommentsEquivalent(originalComment, comment))
        {
            try
            {
                commentGV = delegator.findById(COMMENT_ENTITY, comment.getId());
                populateGenericValueFromComment(comment, commentGV);
                commentGV.store();
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException(e);
            }

            // Update the issue object
            IssueFactory issueFactory = ComponentAccessor.getComponentOfType(IssueFactory.class);
            GenericValue issueGV = comment.getIssue().getGenericValue();
            MutableIssue mutableIssue = issueFactory.getIssue(issueGV);
            mutableIssue.setUpdated(UtilDateTime.nowTimestamp());
            mutableIssue.store();
        }

        // Update comment properties
        if (commentProperties != null)
        {
            setProperties(comment.getAuthorApplicationUser(), comment, commentProperties);
        }

        // Dispatch an event if required
        if (dispatchEvent)
        {
            dispatchIssueCommentEditedEvent(comment, MapBuilder.build("eventsource", IssueEventSource.ACTION, EVENT_ORIGINAL_COMMENT_PARAMETER, originalComment));
        }
    }

    @Override
    public ChangeItemBean delete(Comment comment)
    {
        ChangeItemBean changeItemBean = constructChangeItemBeanForCommentDelete(comment);
        // TODO: move this into the Store (when it gets created)
        delegator.removeByAnd("Action", FieldMap.build("id", comment.getId(), "type", ActionConstants.TYPE_COMMENT));
        this.jsonEntityPropertyManager.deleteByEntity(EntityPropertyType.COMMENT_PROPERTY.getDbEntityName(), comment.getId());
        return changeItemBean;
    }

    @Override
    public boolean isUserCommentAuthor(ApplicationUser user, Comment comment)
    {
        return commentPermissionManager.isUserCommentAuthor(user, comment);
    }

    @Override
    public boolean isUserCommentAuthor(User user, Comment comment)
    {
        return isUserCommentAuthor(ApplicationUsers.from(user), comment);
    }

    @Override
    public int swapCommentGroupRestriction(String groupName, String swapGroup)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("You must provide a non null group name.");
        }

        if (swapGroup == null)
        {
            throw new IllegalArgumentException("You must provide a non null swap group name.");
        }

        return delegator.bulkUpdateByAnd("Action", FieldMap.build("level", swapGroup), FieldMap.build("level", groupName, "type", ActionConstants.TYPE_COMMENT));
    }

    @Override
    public long getCountForCommentsRestrictedByGroup(String groupName)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("You must provide a non null group name.");
        }

        EntityCondition condition = new EntityFieldMap(FieldMap.build("level", groupName, "type", ActionConstants.TYPE_COMMENT), EntityOperator.AND);
        List commentCount = delegator.findByCondition("ActionCount", condition, EasyList.build("count"), Collections.EMPTY_LIST);
        if (commentCount != null && commentCount.size() == 1)
        {
            GenericValue commentCountGV = (GenericValue) commentCount.get(0);
            return commentCountGV.getLong("count").longValue();
        }
        else
        {
            throw new DataAccessException("Unable to access the count for the Action table");
        }
    }

    /**
     * Constructs an issue update bean for a comment delete. The comment text will be masked if the security levels are
     * set
     */
    ChangeItemBean constructChangeItemBeanForCommentDelete(Comment comment)
    {
        // Check the level of the comment, if the level is not null we need to override the comment
        // This is necessary as part of JRA-9394 to remove comment text from the change history for security (or lack thereof)
        String message;
        final String groupLevel = comment.getGroupLevel();
        final String roleLevel = (comment.getRoleLevel() == null) ? null : comment.getRoleLevel().getName();
        final String actionLevel = groupLevel == null ? roleLevel : groupLevel;
        if (actionLevel != null)
        {
            message = getText("comment.manager.deleted.comment.with.restricted.level", actionLevel);
        }
        else
        {
            message = comment.getBody();
        }

        return new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Comment", message, null);
    }

    private void dispatchIssueCommentAddedEvent(Comment comment, Map<String, Object> parameters)
    {
        IssueEventBundle issueCommentBundle = issueEventBundleFactory.createCommentAddedBundle(comment.getIssue(), comment.getUpdateAuthorApplicationUser(), comment, parameters);
        issueEventManager.dispatchEvent(issueCommentBundle);
        dispatchEvent(EventType.ISSUE_COMMENTED_ID, comment, parameters);
    }

    private void dispatchIssueCommentEditedEvent(Comment comment, Map<String, Object> parameters)
    {
        IssueEventBundle issueCommentBundle = issueEventBundleFactory.createCommentEditedBundle(comment.getIssue(), comment.getUpdateAuthorApplicationUser(), comment, parameters);
        issueEventManager.dispatchEvent(issueCommentBundle);
        dispatchEvent(EventType.ISSUE_COMMENT_EDITED_ID, comment, parameters);
    }

    // TODO: the event generation should live in the service
    // This is mostly here for testing purposes so we do not really need to dispatch the event to know it was called correctly
    void dispatchEvent(Long eventTypeId, Comment comment, Map<String, Object> parameters)
    {
        issueEventManager.dispatchRedundantEvent(eventTypeId, comment.getIssue(), comment.getUpdateAuthorUser(), comment, null, null, parameters);
    }

    private String getText(String key, String param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }

    private void populateGenericValueFromComment(Comment updatedComment, GenericValue commentGV)
    {
        ApplicationUser updateAuthor = updatedComment.getUpdateAuthorApplicationUser();
        commentGV.setString("updateauthor", updateAuthor == null ? null : updateAuthor.getKey());
        commentGV.setString("body", updatedComment.getBody());
        commentGV.setString("level", updatedComment.getGroupLevel());
        commentGV.set("rolelevel", updatedComment.getRoleLevelId());
        commentGV.set("updated", JiraDateUtils.copyOrCreateTimestampNullsafe(updatedComment.getUpdated()));
    }

    private void setProperties(final ApplicationUser applicationUser, final Comment comment, Map<String, JSONObject> properties)
    {
        for (Map.Entry<String, JSONObject> property : properties.entrySet())
        {
            jsonEntityPropertyManager.put(applicationUser, commentPropertyHelper.getEntityPropertyType().getDbEntityName(),
                    comment.getId(), property.getKey(), property.getValue().toString(), commentPropertyHelper.createSetPropertyEventFunction(), true);
        }
    }

    /**
     * Returns true if both comments have equal bodies, group levels and role level ids, false otherwise.
     *
     * @param comment1 comment to compare
     * @param comment2 comment to compare
     * @return true if both comments have equal bodies, group levels and role level ids, false otherwise
     */
    private boolean areCommentsEquivalent(Comment comment1, Comment comment2)
    {
        return ObjectUtils.equalsNullSafe(comment1.getBody(), comment2.getBody())
                && ObjectUtils.equalsNullSafe(comment1.getGroupLevel(), comment2.getGroupLevel())
                && ObjectUtils.equalsNullSafe(comment1.getRoleLevelId(), comment2.getRoleLevelId());
    }
}
