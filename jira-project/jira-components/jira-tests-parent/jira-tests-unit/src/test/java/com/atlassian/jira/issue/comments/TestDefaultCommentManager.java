/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comments;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.fugue.Function2;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyHelper;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.json.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith (MockitoJUnitRunner.class)
public class TestDefaultCommentManager
{
    private static final Long L1 = 1L;
    private static final Long L1000 = 1000L;
    private static final Long L1001 = 1001L;
    private static final String AN_UPDATED_COMMENT_BODY = "an updated comment body";
    private static final String UPDATED_AUTHOR = "updated author";
    private static final String A_TEST_COMMENT = "a test comment";

    private Issue issueObject;
    private ApplicationUser author;
    private ApplicationUser updater;
    private Timestamp timestamp;

    @Mock
    private IssueManager mockIssueManager;
    @Mock
    private ProjectRoleManager mockProjectRoleManager;
    @Mock
    private CommentPermissionManager mockCommentPermissionManager;
    @Mock
    private JiraAuthenticationContext mockJiraAuthenticationContext;
    @Mock
    private TextFieldCharacterLengthValidator mockTextFieldCharacterLengthValidator;
    @Mock
    private PermissionManager mockPermissionManager;
    @Mock
    private I18nHelper mockI18nHelper;
    @Mock
    private JsonEntityPropertyManager jsonEntityPropertyManager;
    @Mock
    private CommentPropertyHelper commentPropertyHelper;
    @Mock
    @AvailableInContainer
    private UserManager mockUserManager;
    @Mock
    @AvailableInContainer
    private IssueFactory mockissueFactory;
    @AvailableInContainer
    MockOfBizDelegator mockDelegator = new MockOfBizDelegator();
    @Mock
    private IssueEventManager mockIssueEventManager;
    @Mock
    private IssueEventBundleFactory mockIssueEventBundleFactory;

    private CommentManager commentManager;

    @Rule
    public RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
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
        when(mockissueFactory.getIssue(any(GenericValue.class))).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return new MockIssue((GenericValue) invocation.getArguments()[0]);
            }
        });
        when(mockIssueManager.getIssueObject(anyLong())).thenReturn((MockIssue) issueObject);
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        when(mockI18nHelper.getText(anyString())).thenReturn("Message");
        when(mockI18nHelper.getText(eq("comment.manager.deleted.comment.with.restricted.level"), anyString())).
                thenAnswer(new Answer<Object>()
                {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable
                    {
                        return (String.format("A comment with security level '%s' was removed.", invocation.getArguments()[1]));
                    }
                });


        commentGv = mockDelegator.createValue("Action", FieldMap.build("id", L1000, "issue", L1, "body", "a comment", "type",
                ActionConstants.TYPE_COMMENT).add("level", "Group A").add("created", timestamp).add("updated", timestamp));
        when(mockIssueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issueObject)).thenReturn(Collections.singletonList(commentGv));

        CommentSearchManager commentSearchManager = new CommentSearchManager(mockUserManager, mockDelegator,
                mockIssueManager, mockProjectRoleManager, mockCommentPermissionManager);

        commentManager = new DefaultCommentManager(mockProjectRoleManager, mockCommentPermissionManager,
                mockDelegator, mockJiraAuthenticationContext, mockTextFieldCharacterLengthValidator, mockUserManager, jsonEntityPropertyManager, commentPropertyHelper,
                commentSearchManager, mockIssueEventManager, mockIssueEventBundleFactory);
    }

    @Test
    public void testLastCommentWhenThereAreComments()
            throws GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidGroupException, InvalidCredentialException
    {
        // Setup another comment
        final Timestamp anotherTimestamp = new Timestamp(timestamp.getTime() + 1);
        GenericValue commentGv2 = mockDelegator.createValue("Action", FieldMap.build("id", L1001, "issue", L1, "body", "the body of the comment", "type",
                ActionConstants.TYPE_COMMENT).add("level", "Group A").add("created", anotherTimestamp).add("updated", anotherTimestamp).add("author", "owen"));
        when(mockIssueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issueObject)).thenReturn(Lists.newArrayList(commentGv, commentGv2));
        when(mockCommentPermissionManager.hasBrowsePermission(eq(author), any(Comment.class))).thenReturn(true);
        when(mockUserManager.getUserByKeyEvenWhenUnknown("owen")).thenReturn(author);

        final List<Comment> comments = commentManager.getCommentsForUser(issueObject, author);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        Comment lastComment = commentManager.getLastComment(issueObject);
        assertNotNull(lastComment);
        assertNotNull(lastComment.getAuthorApplicationUser());
        assertEquals(lastComment.getAuthorApplicationUser().getDirectoryId(), author.getDirectoryId());
        assertNotNull(lastComment.getCreated());
        assertEquals(lastComment.getCreated().getTime(), anotherTimestamp.getTime());
    }

    @Test
    public void testLastCommentDoesntFindCommentsForOtherIssue()
            throws GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidGroupException, InvalidCredentialException
    {
        when(mockCommentPermissionManager.hasBrowsePermission(eq(author), any(Comment.class))).thenReturn(true);
        when(mockUserManager.getUserByKeyEvenWhenUnknown("owen")).thenReturn(author);

        final Issue issueWithoutComments = new MockIssue(2l);
        final Comment lastComment = commentManager.getLastComment(issueWithoutComments);

        assertThat(lastComment, nullValue());
    }

    @Test
    public void testLastCommentWhenNoThereIsNoComment()
            throws GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidGroupException, InvalidCredentialException
    {
        mockDelegator.removeValue(commentGv);
        when(mockIssueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issueObject)).thenReturn(Collections.EMPTY_LIST);
        when(mockCommentPermissionManager.hasBrowsePermission(eq(author), any(Comment.class))).thenReturn(true);

        final List<Comment> comments = commentManager.getCommentsForUser(issueObject, author);
        assertNotNull(comments);
        assertEquals(0, comments.size());
        Comment lastComment = commentManager.getLastComment(issueObject);
        assertNull(lastComment);
    }

    @Test
    public void testGetCommentsForUserWithRoleLevels() throws Exception
    {
        final ProjectRole projectRole = MockProjectRoleManager.PROJECT_ROLE_TYPE_2;
        when(mockProjectRoleManager.getProjectRole(projectRole.getId())).thenReturn(projectRole);
        when(mockProjectRoleManager.isUserInProjectRole(author, projectRole, issueObject.getProjectObject())).thenReturn(true);

        when(mockTextFieldCharacterLengthValidator.isTextTooLong(anyString())).thenReturn(false);

        // check that user in this role can see the comment
        Comment comment = commentManager.create(issueObject, author, A_TEST_COMMENT, null, projectRole.getId(), false);
        when(mockCommentPermissionManager.hasBrowsePermission(author, comment)).thenReturn(true);
        when(mockIssueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issueObject)).thenReturn(mockDelegator.findAll("Action"));
        when(mockUserManager.getUserByKeyEvenWhenUnknown("owen")).thenReturn(author);
        final List<Comment> comments = commentManager.getCommentsForUser(issueObject, author);
        assertEquals("User was in the role so he should see the comment", 1, comments.size());
        comments.get(0);
    }

    @Test
    public void testCommentsForUserWithRoleLevelsNotPermitted() throws Exception
    {
        final ProjectRoleManager mockProjectRoleManager = mock(ProjectRoleManager.class);

        //CommentManager commentManager = (CommentManager) ComponentManager.getComponentInstanceOfType(CommentManager.class);

        final ProjectRole adminProjectRole = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;
        when(mockProjectRoleManager.getProjectRole(adminProjectRole.getId())).thenReturn(adminProjectRole);
        when(mockProjectRoleManager.isUserInProjectRole(author, adminProjectRole, issueObject.getProjectObject())).thenReturn(false);
        when(mockTextFieldCharacterLengthValidator.isTextTooLong(anyString())).thenReturn(false);

        // check that user cannot see admin's comment
        final ApplicationUser admin = new MockApplicationUser("Admin");
        Comment comment = commentManager.create(issueObject, admin, "comment for admins", null, adminProjectRole.getId(), false);

        when(mockCommentPermissionManager.hasBrowsePermission(author, comment)).thenReturn(false);
        when(mockIssueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issueObject)).thenReturn(mockDelegator.findAll("Action"));

        final List<Comment> comments = commentManager.getCommentsForUser(issueObject, author);
        assertEquals("User was NOT in the role so he should not see the comment", 0, comments.size());
    }

    @Test
    public void getCommentsForUserSinceShouldOnlyReturnCommentWhenHasBrowsePermission() throws Exception
    {
        final ProjectRole projectRole = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        // check that user in this role can see the comment
        Comment comment = commentManager.create(issueObject, author, A_TEST_COMMENT, null, projectRole.getId(), false);

        when(mockCommentPermissionManager.hasBrowsePermission(author, comment)).thenReturn(true);
        when(mockUserManager.getUserByKeyEvenWhenUnknown("owen")).thenReturn(author);

        final List<Comment> comments = commentManager.getCommentsForUserSince(issueObject, author, new Date(comment.getUpdated().getTime() - 100));
        assertThat("User had browse permission so he should see the comment", comments.size(), equalTo(1));
    }

    @Test
    public void getCommentsForUserSinceShouldNotReturnCommentWhenDoesNotHaveBrowsePermission() throws Exception
    {
        final ProjectRole projectRole = MockProjectRoleManager.PROJECT_ROLE_TYPE_1;

        // check that user not in this role cannot see the comment
        Comment comment = commentManager.create(issueObject, author, A_TEST_COMMENT, null, projectRole.getId(), false);

        when(mockCommentPermissionManager.hasBrowsePermission(author, comment)).thenReturn(false);
        when(mockUserManager.getUserByKeyEvenWhenUnknown("owen")).thenReturn(author);

        final List<Comment> comments = commentManager.getCommentsForUserSince(issueObject, author, new Date(comment.getUpdated().getTime() - 100));
        assertThat("User did not have browse permission so he should not see the comment", comments.size(), equalTo(0));
    }

    @Test
    public void testCreateComment() throws Exception
    {
        final Comment comment = commentManager.create(issueObject, author, A_TEST_COMMENT, false);

        assertNotNull(comment.getId());
        assertNotNull(comment.getCreated());
        assertEquals(comment.getCreated(), comment.getUpdated());
        assertNull(comment.getGroupLevel());
        assertEquals(author, comment.getAuthorApplicationUser());
        assertEquals(author, comment.getUpdateAuthorApplicationUser());
        assertEquals(A_TEST_COMMENT, comment.getBody());
        when(mockCommentPermissionManager.hasBrowsePermission(author, comment)).thenReturn(true);
        when(mockIssueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issueObject)).thenReturn(mockDelegator.findAll("Action"));
        when(mockUserManager.getUserByKeyEvenWhenUnknown("owen")).thenReturn(author);
        final List<Comment> comments = commentManager.getCommentsForUser(issueObject, author);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(comment, comments.get(0));
    }

    @Test
    public void testCreateCommentWithProperties() throws Exception
    {
        Map<String, Object> jsonMap = Maps.newHashMap();
        jsonMap.put("key", true);
        JSONObject jsonObject = new JSONObject(jsonMap);
        Map<String, JSONObject> properties = Maps.newHashMap();
        properties.put("key.again", jsonObject);
        InOrder inOrder = Mockito.inOrder(jsonEntityPropertyManager, mockIssueEventManager);

        when(commentPropertyHelper.getEntityPropertyType()).thenReturn(EntityPropertyType.COMMENT_PROPERTY);

        commentManager.create(issueObject, author, A_TEST_COMMENT, null, null, new Date(), properties, true);

        inOrder.verify(jsonEntityPropertyManager, times(1)).put(any(ApplicationUser.class), anyString(), anyLong(), anyString(), anyString(), any(Function2.class), anyBoolean());
        inOrder.verify(mockIssueEventManager, times(1)).dispatchEvent(any(IssueEventBundle.class));
        inOrder.verify(mockIssueEventManager, times(1)).dispatchRedundantEvent(anyLong(), any(Issue.class), any(User.class), any(Comment.class), any(Worklog.class), any(GenericValue.class), anyMap());
    }

    @Test
    public void testCreateCommentIncUpdate() throws Exception
    {
        final Date updated = new Date();
        final Comment comment = commentManager.create(issueObject, author, updater, A_TEST_COMMENT, null, null, null,
                updated, false);

        assertNotNull(comment.getId());
        assertNotNull(comment.getCreated());
        assertEquals(updated, comment.getUpdated());
        assertNull(comment.getGroupLevel());
        assertEquals(author, comment.getAuthorApplicationUser());
        assertEquals(updater, comment.getUpdateAuthorApplicationUser());
        assertEquals(A_TEST_COMMENT, comment.getBody());
        when(mockCommentPermissionManager.hasBrowsePermission(author, comment)).thenReturn(true);
        when(mockIssueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issueObject)).thenReturn(mockDelegator.findAll("Action"));
        when(mockUserManager.getUserByKeyEvenWhenUnknown("owen")).thenReturn(author);
        when(mockUserManager.getUserByKeyEvenWhenUnknown("mrupdater")).thenReturn(updater);
        final List<Comment> comments = commentManager.getCommentsForUser(issueObject, author);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(comment, comments.get(0));
    }

    @Test
    public void testCreateActionAddComment() throws Exception
    {
        commentManager.create(issueObject, author, "somebody", true);
        verify(mockIssueEventManager, times(1)).dispatchEvent(any(IssueEventBundle.class));
        verify(mockIssueEventManager, times(1)).dispatchRedundantEvent(anyLong(), any(Issue.class), any(User.class), any(Comment.class), any(Worklog.class), any(GenericValue.class), anyMap());
    }

    @Test
    public void testUpdateCommentWithDefaultUpdateDate() throws Exception
    {
        final Comment comment = commentManager.create(issueObject, author, A_TEST_COMMENT, false);
        final Date originalIssueUpdateDate = comment.getIssue().getUpdated();
        final Date originalUpdateDate = comment.getUpdated();
        when(mockUserManager.getUserByKeyEvenWhenUnknown("owen")).thenReturn(author);
        when(mockUserManager.getUserByKeyEvenWhenUnknown(UPDATED_AUTHOR)).thenReturn(new MockApplicationUser(UPDATED_AUTHOR));
        final MutableComment mutableComment = commentManager.getMutableComment(comment.getId());
        // Update the comment body, updateAuthor
        mutableComment.setBody(AN_UPDATED_COMMENT_BODY);
        mutableComment.setUpdateAuthor(UPDATED_AUTHOR);
        // A null date will set the updated date to now
        mutableComment.setUpdated(null);

        // This is to make sure that the updated time will be different than the original time
        Thread.sleep(100);

        // Store the comment this calls through to the manager
        commentManager.update(mutableComment, false);

        // re-Get the comment from the database
        final Comment updatedComment = commentManager.getCommentById(mutableComment.getId());
        when(mockCommentPermissionManager.hasBrowsePermission(author, comment)).thenReturn(true);
        when(mockIssueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issueObject)).thenReturn(mockDelegator.findAll("Action"));

        assertEquals(AN_UPDATED_COMMENT_BODY, updatedComment.getBody());
        assertEquals(UPDATED_AUTHOR, updatedComment.getUpdateAuthor());
        assertEquals(comment.getAuthor(), updatedComment.getAuthor());
        assertEquals(comment.getRoleLevelId(), updatedComment.getRoleLevelId());
        assertEquals(comment.getGroupLevel(), updatedComment.getGroupLevel());
        assertEquals(comment.getCreated(), updatedComment.getCreated());
        assertTrue(originalUpdateDate.getTime() < updatedComment.getUpdated().getTime());
    }

    @Test
    public void testUpdateCommentWithSetUpdateDate()
    {
        final Comment comment = commentManager.create(issueObject, author, A_TEST_COMMENT, false);
        final MutableComment mutableComment = commentManager.getMutableComment(comment.getId());

        // Update the comment body, updateAuthor
        mutableComment.setBody(AN_UPDATED_COMMENT_BODY);
        // A null date will set the updated date to now
        final Date MY_UPDATED_DATE = new Date();
        mutableComment.setUpdated(MY_UPDATED_DATE);

        // Store the comment this calls through to the manager
        commentManager.update(mutableComment, false);

        // re-Get the comment from the database
        final Comment updatedComment = commentManager.getCommentById(mutableComment.getId());

        assertEquals(MY_UPDATED_DATE.getTime(), updatedComment.getUpdated().getTime());
        assertEquals(AN_UPDATED_COMMENT_BODY, updatedComment.getBody());
    }

    @Test
    public void testUpdateCommentDispatchEvent()
    {
        final Date MY_UPDATED_DATE = new Date();

        final AtomicBoolean dispatchCalled = new AtomicBoolean(false);


        CommentSearchManager commentSearchManager = new CommentSearchManager(mockUserManager, mockDelegator,
                mockIssueManager, mockProjectRoleManager, mockCommentPermissionManager);

        final CommentManager commentManager = new DefaultCommentManager(mockProjectRoleManager, mockCommentPermissionManager,
                mockDelegator, mockJiraAuthenticationContext, mockTextFieldCharacterLengthValidator, mockUserManager, jsonEntityPropertyManager, commentPropertyHelper,
                commentSearchManager, mockIssueEventManager, mockIssueEventBundleFactory)
        {
            // This is mostly here for testing purposes so we do not really need to dispatch the event to know it was called correctly
            @Override
            void dispatchEvent(final Long eventTypeId, final Comment comment, final Map<String, Object> parameters)
            {
                dispatchCalled.set(true);
                // Verify we get what we care about.
                assertEquals(EventType.ISSUE_COMMENT_EDITED_ID, eventTypeId);
                assertEquals(AN_UPDATED_COMMENT_BODY, comment.getBody());
                assertEquals(MY_UPDATED_DATE, comment.getUpdated());
                final Comment originalComment = (Comment) parameters.get(CommentManager.EVENT_ORIGINAL_COMMENT_PARAMETER);
                assertNotNull(originalComment);
                assertEquals(A_TEST_COMMENT, originalComment.getBody());
            }
        };

        final Comment comment = commentManager.create(issueObject, author, A_TEST_COMMENT, false);

        final MutableComment mutableComment = commentManager.getMutableComment(comment.getId());
        // Update the comment body, updateAuthor
        mutableComment.setBody(AN_UPDATED_COMMENT_BODY);
        // A null date will set the updated date to now
        mutableComment.setUpdated(MY_UPDATED_DATE);

        // Store the comment this calls through to the manager
        commentManager.update(mutableComment, true);

        // re-Get the comment from the database
        final Comment updatedComment = commentManager.getCommentById(mutableComment.getId());

        assertTrue(dispatchCalled.get());
        assertEquals(MY_UPDATED_DATE.getTime(), updatedComment.getUpdated().getTime());
        assertEquals(AN_UPDATED_COMMENT_BODY, updatedComment.getBody());

    }

    @Test
    public void testUpdateCommentNoChangesCommentNotUpdated() throws Exception
    {
        final Comment comment = commentManager.create(issueObject, author, A_TEST_COMMENT, false);
        final MutableComment mutableComment = commentManager.getMutableComment(comment.getId());

        // Lets nuke the updated date so that it will become now
        mutableComment.setUpdated(null);

        // Lets sleep for a few millis to make sure that the update date will not be the same if an update occurs
        Thread.sleep(20);

        // Do not update the comment so that the update date will not be set and nothing will be persisted.
        // Store the comment this calls through to the manager, this should do nothing
        commentManager.update(mutableComment, false);

        // re-Get the comment from the database
        final Comment updatedComment = commentManager.getCommentById(mutableComment.getId());

        assertEquals(comment.getUpdated(), updatedComment.getUpdated());
    }

    @Test
    public void testConstructChangeItemBeanForCommentDeleteWithGroupLevel()
    {
        final DefaultCommentManager commentManager = new DefaultCommentManager(null, null, null,
                mockJiraAuthenticationContext, null, mockUserManager, mockIssueEventManager, mockIssueEventBundleFactory);

        final Comment commentWithGroupLevel = new CommentImpl(null, null, null, null, "testgroup", null, null, null, null);
        final ChangeItemBean changeItemBean = commentManager.constructChangeItemBeanForCommentDelete(commentWithGroupLevel);
        assertNotNull(changeItemBean);
        assertEquals("A comment with security level 'testgroup' was removed.", changeItemBean.getFrom());
    }

    @Test
    public void testConstructChangeItemBeanForCommentDeleteWithRoleLevel()
    {
        CommentSearchManager commentSearchManager = new CommentSearchManager(mockUserManager, mockDelegator,
                mockIssueManager, mockProjectRoleManager, mockCommentPermissionManager);

        final DefaultCommentManager commentManager = new DefaultCommentManager(mockProjectRoleManager, mockCommentPermissionManager,
                mockDelegator, mockJiraAuthenticationContext, mockTextFieldCharacterLengthValidator, mockUserManager, jsonEntityPropertyManager, commentPropertyHelper,
                commentSearchManager, mockIssueEventManager, mockIssueEventBundleFactory);

        final ProjectRole mockProjectRole = mock(ProjectRole.class);
        when(mockProjectRole.getName()).thenReturn("testrole");
        when(mockProjectRoleManager.getProjectRole(Mockito.anyLong())).thenReturn(mockProjectRole);

        final Comment commentWithGroupLevel = new CommentImpl(mockProjectRoleManager, null, null, null, null, L1, null, null, null);
        final ChangeItemBean changeItemBean = commentManager.constructChangeItemBeanForCommentDelete(commentWithGroupLevel);
        assertNotNull(changeItemBean);
        assertEquals("A comment with security level 'testrole' was removed.", changeItemBean.getFrom());
    }

    @Test
    public void testConstructChangeItemBeanForCommentDeleteWithNoLevel()
    {
        final DefaultCommentManager commentManager = new DefaultCommentManager(null, null, null,
                ComponentAccessor.getJiraAuthenticationContext(), null, mockUserManager, mockIssueEventManager, mockIssueEventBundleFactory);

        final Comment commentWithGroupLevel = new CommentImpl(mockProjectRoleManager, null, null, "testbody", null, null, null, null, null);
        final ChangeItemBean changeItemBean = commentManager.constructChangeItemBeanForCommentDelete(commentWithGroupLevel);
        assertNotNull(changeItemBean);
        assertEquals("testbody", changeItemBean.getFrom());
    }

    @Test
    public void testGetCountForCommentsRestrictedByGroupNullGroup()
    {
        try
        {
            commentManager.getCountForCommentsRestrictedByGroup(null);
            fail();
        }
        catch (final Exception e)
        {
            // this should happen
            assertEquals("You must provide a non null group name.", e.getMessage());
        }
    }

    @Test
    public void testSwapCommentGroupRestrictionNullGroups()
    {
        try
        {
            commentManager.swapCommentGroupRestriction(null, "AnotherGroup");
        }
        catch (final Exception e)
        {
            assertEquals("You must provide a non null group name.", e.getMessage());
        }
        try
        {
            commentManager.swapCommentGroupRestriction("Group", null);
        }
        catch (final Exception e)
        {
            assertEquals("You must provide a non null swap group name.", e.getMessage());
        }
    }

    @Test
    public void testSwapCommentGroupRestriction()
    {
        assertEquals(1, commentManager.swapCommentGroupRestriction("Group A", "AnotherGroup"));
        final Comment comment = commentManager.getCommentById(L1000);
        assertEquals("AnotherGroup", comment.getGroupLevel());
    }

    @Test
    public void testCommentPropertyRemovedWhenCommentRemoved()
    {
        Comment comment = mock(Comment.class);
        when(comment.getId()).thenReturn(1l);
        commentManager.delete(comment);

        Mockito.verify(jsonEntityPropertyManager).deleteByEntity(Mockito.eq(EntityPropertyType.COMMENT_PROPERTY.getDbEntityName()), Mockito.eq(1l));
    }

    @Test
    public void testTooLongCommentBodyCreation()
    {
        when(mockTextFieldCharacterLengthValidator.isTextTooLong(anyString())).thenReturn(true);
        when(mockTextFieldCharacterLengthValidator.getMaximumNumberOfCharacters()).thenReturn(10L);
        final String errorMsg = "The entered text is too long. It exceeds the allowed limit of 10 characters.";
        when(mockI18nHelper.getText("field.error.text.toolong", "10")).thenReturn(errorMsg);
        final CommentManager commentManager = new DefaultCommentManager(null, null, null, mockJiraAuthenticationContext,
                mockTextFieldCharacterLengthValidator, mockUserManager, mockIssueEventManager, mockIssueEventBundleFactory);
        try
        {
            commentManager.create(null, (ApplicationUser) null, null, null, null, null, null, null, false, false);
            fail("Exception should be thrown as text length validation failed");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(errorMsg, e.getMessage());
        }
    }

    @Test
    public void testMultilineCommentLengthValidatedWithUnalteredEOLCharacters() throws Exception
    {
        final String commentBody = "This is a multiline comment\nand newlines should be passed\r\nas they are\rwithout replacing LF with CRLF";
        when(mockTextFieldCharacterLengthValidator.isTextTooLong(commentBody)).thenReturn(false);

        final CommentManager commentManager = new DefaultCommentManager(null, null, null, mockJiraAuthenticationContext,
                mockTextFieldCharacterLengthValidator, mockUserManager, mockIssueEventManager, mockIssueEventBundleFactory);

        commentManager.create(issueObject, (ApplicationUser) null, null, commentBody, null, null, null, null, false, false);

        verify(mockTextFieldCharacterLengthValidator).isTextTooLong(commentBody);
    }
}
