package com.atlassian.jira.issue.comments;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraDateUtils;

import com.google.common.collect.ImmutableList;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * The CommentSearchManager is used to retrieve comments in JIRA.
 * Comments are always associated with an issue. This manager is only used internally.
 */
public class CommentSearchManager
{
    private final UserManager userManager;
    private final OfBizDelegator delegator;
    private final IssueManager issueManager;
    private final ProjectRoleManager projectRoleManager;
    private final CommentPermissionManager commentPermissionManager;

    private static final String COMMENT_ID = "id";

    public CommentSearchManager(final UserManager userManager, final OfBizDelegator delegator, final IssueManager issueManager, final ProjectRoleManager projectRoleManager, final CommentPermissionManager commentPermissionManager){
        this.userManager = userManager;
        this.delegator = delegator;
        this.issueManager = issueManager;
        this.projectRoleManager = projectRoleManager;
        this.commentPermissionManager = commentPermissionManager;
    }

    public Comment convertToComment(GenericValue gv)
    {
        return convertToComment(gv, issueManager.getIssueObject(gv.getLong("issue")));
    }

    public Comment getCommentById(Long commentId)
    {
        return getMutableComment(commentId);
    }

    public MutableComment getMutableComment(Long commentId)
    {
        if (commentId == null)
        {
            throw new IllegalArgumentException("The comment id must not be null.");
        }

        GenericValue gv = delegator.findById(DefaultCommentManager.COMMENT_ENTITY, commentId);
        if (gv != null)
        {
            return convertToComment(gv, issueManager.getIssueObject(gv.getLong("issue")));
        }
        return null;
    }

    public List<Comment> getComments(Issue issue)
    {
        List<Comment> comments = new ArrayList<Comment>();

        try
        {
            // get a List<GenericValue> of comments
            List allComments = issueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issue);

            for (final Object allComment : allComments)
            {
                Comment comment = convertToComment((GenericValue) allComment, issue);
                comments.add(comment);
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        Collections.sort(comments, CommentComparator.COMPARATOR);
        return comments;
    }

    @Nonnull
    public List<Comment> getCommentsForUserSince(@Nonnull Issue issue, @Nullable ApplicationUser user, @Nonnull Date since)
    {
        EntityCondition issueCommentsCondition = new EntityFieldMap(FieldMap.build("issue", issue.getId(), "type", ActionConstants.TYPE_COMMENT), EntityOperator.AND);
        EntityCondition dateCondition = new EntityExpr("updated", EntityOperator.GREATER_THAN, new Timestamp(since.getTime()));
        EntityCondition finalCondition = new EntityConditionList(Arrays.asList(issueCommentsCondition, dateCondition), EntityOperator.AND);

        // get a List<GenericValue> of comments since date
        List<GenericValue> commentsSinceDate = delegator.findByCondition(DefaultCommentManager.COMMENT_ENTITY, finalCondition, null, ImmutableList.of("updated DESC", "id ASC"));

        return getVisibleComments(issue, user, commentsSinceDate);
    }

    public List<Comment> getCommentsForUser(Issue issue, ApplicationUser user)
    {
        List<Comment> visibleComments;
        try
        {
            // get a List<GenericValue> of comments
            List<GenericValue> allComments = issueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issue);
            visibleComments = getVisibleComments(issue, user, allComments);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        Collections.sort(visibleComments, CommentComparator.COMPARATOR);
        return visibleComments;
    }

    private List<Comment> getVisibleComments(Issue issue, ApplicationUser user, List<GenericValue> commentValues)
    {
        List<Comment> visibleComments = new ArrayList<Comment>();

        for (final GenericValue commentGV : commentValues)
        {
            Comment comment = convertToComment(commentGV, issue);

            if (commentPermissionManager.hasBrowsePermission(user, comment))
            {
                visibleComments.add(comment);
            }
        }
        return visibleComments;
    }

    private MutableComment convertToComment(GenericValue gv, Issue issue)
    {
        Timestamp createdTS = gv.getTimestamp("created");
        Timestamp updatedTS = gv.getTimestamp("updated");
        CommentImpl comment = new CommentImpl(projectRoleManager,
                userManager.getUserByKeyEvenWhenUnknown(gv.getString("author")),
                userManager.getUserByKeyEvenWhenUnknown(gv.getString("updateauthor")),
                gv.getString("body"),
                gv.getString("level"),
                gv.getLong("rolelevel"),
                JiraDateUtils.copyDateNullsafe(createdTS),
                JiraDateUtils.copyDateNullsafe(updatedTS),
                issue);

        comment.setId(gv.getLong(COMMENT_ID));
        return comment;
    }
}
