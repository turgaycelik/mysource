package com.atlassian.jira.issue.comments;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultCommentSearchManager
{
    private static final Long L1 = 1L;
    private static final Long L1000 = 1000L;
    private static final Long L1001 = 1001L;

    private Issue issueObject;
    private ApplicationUser author;
    private ApplicationUser updater;
    private Timestamp timestamp;

    @Mock @AvailableInContainer private UserManager mockUserManager;
    @AvailableInContainer OfBizDelegator mockDelegator = new MockOfBizDelegator();
    @Mock private IssueManager mockIssueManager;
    @Mock private ProjectRoleManager mockProjectRoleManager;
    @Mock private CommentPermissionManager mockCommentPermissionManager;

    private CommentSearchManager commentSearchManager;

    @Rule
    public RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    private GenericValue commentGv;

    @Before
    public void setUp() throws Exception
    {

        author = new MockApplicationUser("Owen");
        updater = new MockApplicationUser("MrUpdater");

        final GenericValue project = mockDelegator.createValue("Project", FieldMap.build("name", "test project"));

        timestamp = new Timestamp(System.currentTimeMillis());
        final String key = "TST-1";
        final GenericValue issue = mockDelegator.createValue("Issue", FieldMap.build("id", L1, "project", project.getLong("id"), "key",
                key, "updated", timestamp));
        issueObject = new MockIssue(issue);
        when(mockIssueManager.getIssueObject(anyLong())).thenReturn((MockIssue) issueObject);


        commentGv = mockDelegator.createValue("Action", FieldMap.build("id", L1000, "issue", L1, "body", "a comment", "type",
                ActionConstants.TYPE_COMMENT).add("level", "Group A").add("created", timestamp));
        when(mockIssueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issueObject)).thenReturn(Collections.singletonList(commentGv));

        commentSearchManager = new CommentSearchManager(mockUserManager, mockDelegator,
                mockIssueManager, mockProjectRoleManager, mockCommentPermissionManager);
    }

    @Test
    public void testGetMutableComment() throws Exception
    {
        when(mockCommentPermissionManager.hasBrowsePermission(eq(author), any(Comment.class))).thenReturn(true);

        final Comment comment = commentSearchManager.getMutableComment(L1000);
        assertNotNull(comment);
        assertEquals(L1000, comment.getId());

        try
        {
            commentSearchManager.getMutableComment(null);
            fail("Null comment id should throw IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }

    }

    @Test
    public void testActionsOneComment() throws Exception
    {
        when(mockCommentPermissionManager.hasBrowsePermission(eq(author), any(Comment.class))).thenReturn(true);

        List<Comment> comments = commentSearchManager.getCommentsForUser(issueObject, (ApplicationUser) null);
        assertNotNull(comments);
        assertTrue(comments.isEmpty());
        comments = commentSearchManager.getCommentsForUser(issueObject, author);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(L1000, comments.get(0).getId());
    }

    @Test
    public void testActionsMultipleComments() throws Exception
    {

        // Setup another comment
        final Timestamp anotherTimestamp = new Timestamp(timestamp.getTime() + 1);
        GenericValue commentGv2 = mockDelegator.createValue("Action", FieldMap.build("id", L1001, "issue", L1, "body", "the body of the comment", "type",
                ActionConstants.TYPE_COMMENT).add("level", "Group A").add("created", anotherTimestamp));
        when(mockIssueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issueObject)).thenReturn(Lists.newArrayList(commentGv, commentGv2));
        when(mockCommentPermissionManager.hasBrowsePermission(eq(author), any(Comment.class))).thenReturn(true);

        List<Comment> comments = commentSearchManager.getCommentsForUser(issueObject, (ApplicationUser) null);
        assertNotNull(comments);
        assertTrue(comments.isEmpty());
        comments = commentSearchManager.getCommentsForUser(issueObject, author);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals(L1000, comments.get(0).getId());
        assertEquals(L1001, comments.get(1).getId());
    }

    @Test
    public void testComments()
            throws GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidGroupException, InvalidCredentialException
    {
        when(mockCommentPermissionManager.hasBrowsePermission(eq(author), any(Comment.class))).thenReturn(true);

        final List<Comment> comments = commentSearchManager.getCommentsForUser(issueObject, author);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(L1000, comments.get(0).getId());
    }

    @Test
    public void testCommentsNotInGroup() throws GenericEntityException
    {
        final List<Comment> comments = commentSearchManager.getCommentsForUser(issueObject, author);
        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    @Test
    public void testCommentsGroup() throws Exception
    {
        when(mockCommentPermissionManager.hasBrowsePermission(eq(author), any(Comment.class))).thenReturn(true);

        final List<Comment> comments = commentSearchManager.getCommentsForUser(issueObject, author);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(L1000, comments.get(0).getId());
    }

    @Test
    public void testGetCommentById() throws Exception
    {
        when(mockCommentPermissionManager.hasBrowsePermission(eq(author), any(Comment.class))).thenReturn(true);

        final Comment comment = commentSearchManager.getCommentById(L1000);
        assertNotNull(comment);
        assertEquals(L1000, comment.getId());

        try
        {
            commentSearchManager.getCommentById(null);
            fail("Null comment id should throw IllegalArgumentException");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getCommentsForUserSinceShouldPassCorrectConditionToDelegator()
    {
        // Set up
        Date sinceDate = new Date();
        final OfBizDelegator mockOfBizDelegator = mock(OfBizDelegator.class);
        ReflectionTestUtils.setField(commentSearchManager, "delegator", mockOfBizDelegator);

        // Invoke
        commentSearchManager.getCommentsForUserSince(issueObject, author, sinceDate);

        // Check
        final ArgumentCaptor<EntityCondition> entityConditionCaptor = ArgumentCaptor.forClass(EntityCondition.class);
        verify(mockOfBizDelegator).findByCondition(eq(DefaultCommentManager.COMMENT_ENTITY),
                entityConditionCaptor.capture(), isNull(List.class), eq(ImmutableList.of("updated DESC", "id ASC")));
        final EntityCondition entityCondition = entityConditionCaptor.getValue();
        assertEquals(EntityConditionList.class, entityCondition.getClass());
        final EntityConditionList entityConditionList = (EntityConditionList) entityCondition;
        final EntityExpr dateCondition = (EntityExpr) entityConditionList.getCondition(1);
        assertThat((String) dateCondition.getLhs(), equalTo("updated"));
        assertThat(dateCondition.getOperator(), equalTo(EntityOperator.GREATER_THAN));
        assertThat(((Timestamp) dateCondition.getRhs()).getTime(), equalTo(sinceDate.getTime()));
    }
}
