package com.atlassian.jira.bc.issue.comment;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comments.CommentPermissionManager;
import com.atlassian.jira.issue.comments.MockComment;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.json.JSONObject;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.atlassian.jira.entity.property.EntityPropertyService.EntityPropertyInput;
import static com.atlassian.jira.entity.property.EntityPropertyService.PropertyInput;
import static com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link DefaultCommentService}.
 */
@SuppressWarnings("deprecation")
public class TestDefaultCommentService
{
    private static final long COMMENT_ID = 1;

    @Mock private CommentManager commentManager;
    @Mock private CommentPermissionManager commentPermissionManager;
    @Mock private Issue issue;
    @Mock private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock private JiraServiceContext jiraServiceContext;
    @Mock private PermissionManager permissionManager;
    @Mock @AvailableInContainer private CommentPropertyService commentPropertyService;
    @Mock private TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;

    @Rule
    public MockitoContainer mockitoMocksInContainer = MockitoMocksInContainer.rule(this);

    private ApplicationUser testUser = new MockApplicationUser("testuser");
    private User user = new MockUser("owen", "Owen", "owen@secret.place.com");

    @Before
    public void setUp() throws Exception
    {
        when(issue.getId()).thenReturn(COMMENT_ID);
        when(jiraAuthenticationContext.getUser()).thenReturn(testUser);
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper());
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        when(jiraServiceContext.getErrorCollection()).thenReturn(errorCollection);
    }

    // NOTE: this also tests the getMutableComment method as they run through the same code
    @Test
    public void testGetCommentByIdHappyPath()
    {
        // Set up
        final MutableComment mockComment = new MockComment("dude", "comm-ent");
        final long commentId = 1000L;
        when(commentManager.getMutableComment(commentId)).thenReturn(mockComment);
        when(commentPermissionManager.hasBrowsePermission(any(ApplicationUser.class), any(Comment.class)))
                .thenReturn(true);
        final CommentService commentService = new DefaultCommentService(commentManager, null, null, commentPermissionManager, null, null, null, null);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        final Comment comment = commentService.getCommentById(user, commentId, errorCollection);
        
        // Check
        assertThat(errorCollection.hasAnyErrors(), Matchers.is(false));
        assertThat(comment, Matchers.notNullValue());
        assertThat(mockComment, Matchers.is(comment));
    }

    // NOTE: this also tests the getMutableComment method as they run through the same code
    @Test
    public void testGetCommentByIdNoPermission()
    {
        // Set up
        final MutableComment mockComment = new MockComment("dude", "comm-ent");
        final long commentId = 1000L;
        when(commentManager.getMutableComment(commentId)).thenReturn(mockComment);
        when(commentPermissionManager.hasBrowsePermission(testUser, mockComment)).thenReturn(false);
        final CommentService commentService = new DefaultCommentService(commentManager, null, jiraAuthenticationContext,
                commentPermissionManager, null, null, null, null);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        final Comment comment = commentService.getCommentById(user, commentId, errorCollection);

        // Check
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.no.permission")));
        assertThat(comment, Matchers.nullValue());
    }

    // NOTE: this also tests the getMutableComment method as they run through the same code
    @Test
    public void testGetCommentByIdNoCommentFound()
    {
        // Set up
        when(commentManager.getMutableComment(1000L)).thenReturn(null);
        final CommentService commentService = new DefaultCommentService(commentManager, null,
                jiraAuthenticationContext, null, null, null, null, null);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        final Comment comment = commentService.getCommentById(user, 1000L, errorCollection);

        // Check
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.no.comment.for.id")));
        assertThat(comment, Matchers.nullValue());
    }

    // NOTE: this also tests the getMutableComment method as they run through the same code
    @Test
    public void testGetCommentByIdNullId()
    {
        // Set up
        final CommentService commentService = new DefaultCommentService(null, null,
                jiraAuthenticationContext, null, null, null, null, null);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        final Comment comment = commentService.getCommentById(user, null, errorCollection);
        
        // Check
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.no.id.specified")));
        assertThat(comment, Matchers.nullValue());
    }

    @Test
    public void testValidateCommentUpdateHappyPath()
    {
        // Set up
        final MutableComment mockComment = new MockComment("dude", "comm-ent");
        final long commentId = 1000L;
        when(commentManager.getMutableComment(commentId)).thenReturn(mockComment);
        when(commentPermissionManager.hasBrowsePermission(any(ApplicationUser.class), any(Comment.class)))
                .thenReturn(true);
        final TextFieldCharacterLengthValidator mockTextFieldCharacterLengthValidator = mock(TextFieldCharacterLengthValidator.class);
        final String body = "test body";
        when(mockTextFieldCharacterLengthValidator.isTextTooLong(body)).thenReturn(false);

        final CommentService commentService = new DefaultCommentService(
                commentManager, null, jiraAuthenticationContext,
                commentPermissionManager, null, null, null, mockTextFieldCharacterLengthValidator)
        {
            public boolean isValidCommentVisibility(ApplicationUser currentUser, Issue issue, Visibility visibility, ErrorCollection errorCollection)
            {
                return true;
            }

            public boolean hasPermissionToEdit(ApplicationUser user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        commentService.validateCommentUpdate(user, commentId, body, null, null, errorCollection);
        
        // Check
        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testValidateCommentUpdateNoId()
    {
        CommentService commentService = new DefaultCommentService(null, null,
                jiraAuthenticationContext, null, null, null, null, null)
        {
            public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        commentService.validateCommentUpdate(user, null, "test body", null, null, errorCollection);

        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.update.null.comment.id")));
    }

    @Test
    public void testValidateCommentUpdateNoSuchComment()
    {
        // Set up
        final CommentService commentService = new DefaultCommentService(commentManager, null,
                jiraAuthenticationContext, null, null, null, null, null)
        {
            public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        commentService.validateCommentUpdate(user, null, "test body", null, null, errorCollection);
        
        // Check
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.update.null.comment.id")));
    }

    @Test
    public void testUpdateCommentHappyPath()
    {
        // Set up
        final Date date = new Date(System.currentTimeMillis() - 100);
        final MutableComment mockComment = new MockComment(1000L, "dude", "updateAuthor", "comm-ent", "group-level", null, date, date, issue);
        IssueManager issueManager = mock(IssueManager.class);
        when(issueManager.isEditable(issue)).thenReturn(true);
        VisibilityValidator visibilityValidator = mock(VisibilityValidator.class);
        when(visibilityValidator.isValidVisibilityData(Mockito.<JiraServiceContext>any(), anyString(), eq(issue), Mockito.<Visibility>any())).thenReturn(true);
        when(commentManager.getMutableComment(1000L)).thenReturn(mockComment);
        when(commentPermissionManager.hasBrowsePermission(testUser, mockComment)).thenReturn(true);
        when(commentPermissionManager.hasEditPermission(testUser, mockComment)).thenReturn(true);
        when(jiraAuthenticationContext.getUser()).thenReturn(testUser);

        final CommentService commentService = new DefaultCommentService(commentManager, null, jiraAuthenticationContext,
                commentPermissionManager, null, issueManager, visibilityValidator, null)
        {
            @Override
            public boolean isValidCommentBody(final String body, final ErrorCollection errorCollection, final boolean allowEmpty)
            {
                return true;
            }
        };

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final boolean dispatchEvent = true;

        // Invoke
        commentService.update(testUser, mockComment, dispatchEvent, errorCollection);
        
        // Check
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>empty());
        assertThat(date.getTime(), Matchers.lessThan(mockComment.getUpdated().getTime()));
        assertThat(mockComment.getUpdateAuthor(), Matchers.is("testuser"));

        verify(commentManager).update(mockComment, Collections.<String, JSONObject>emptyMap(), dispatchEvent);
    }

    @Test
    public void testUpdateCommentNullComment()
    {
        // Set up
        final CommentService commentService = new DefaultCommentService(
                null, null, jiraAuthenticationContext, null, null, null, null, null)
        {
            public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        commentService.update(user, null, true, errorCollection);
        
        // Check
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.update.null.comment")));
    }

    @Test
    public void testUpdateCommentNullCommentId()
    {
        // Set up
        final Date date = new Date();
        final MutableComment mockComment =
                new MockComment(null, "dude", "updateAuthor", "comm-ent", "group-level", 1L, date, date, null);
        final CommentService commentService = new DefaultCommentService(null, null,
                jiraAuthenticationContext, null, null, null, null, null)
        {
            public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
            {
                return true;
            }
        };
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        commentService.update(user, mockComment, true, errorCollection);
        
        // Check
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.update.null.comment")));
    }

    @Test
    public void testHasPermissionToEditNullComment()
    {
        // Set up
        final CommentService commentService = new DefaultCommentService(null, null,
                jiraAuthenticationContext, null, null, null, null, null);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        
        // Invoke
        final boolean canEdit = commentService.hasPermissionToEdit((User) null, null, errorCollection);
        
        // Check
        assertThat(canEdit, Matchers.is(false));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.update.null.comment")));
    }

    @Test
    public void testHasPermissionToEditCommentNullId()
    {
        // Set up
        final CommentService commentService = new DefaultCommentService(null, null,
                jiraAuthenticationContext, null, null, null, null, null);
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final MutableComment mockComment =
                new MockComment(null, "dude", "updateAuthor", "comm-ent", "group-level", 1L, null, null, null);
        
        // Invoke
        final boolean canEdit = commentService.hasPermissionToEdit((ApplicationUser) null, mockComment, errorCollection);
        
        // Check
        assertThat(canEdit, Matchers.is(false));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.update.null.comment")));
    }

    @Test
    public void testHasPermissionToEditNoPermission()
    {
        // Set up
        final MutableComment mockComment =
                new MockComment(9L, "dude", "updateAuthor", "comm-ent", "group-level", 1L, null, null, null);
        when(commentPermissionManager.hasEditPermission(testUser, mockComment)).thenReturn(false);
        final VisibilityValidator visibilityValidator = mock(VisibilityValidator.class);
        when(visibilityValidator.isGroupVisibilityEnabled()).thenReturn(false);
        when(visibilityValidator.isProjectRoleVisibilityEnabled()).thenReturn(false);
        when(visibilityValidator.isValidVisibilityData(Mockito.<JiraServiceContext>any(),
                anyString(), Mockito.<Issue>any(), Mockito.<Visibility>any())).thenReturn(false);

        final CommentService commentService = new DefaultCommentService(
                null, null, jiraAuthenticationContext, commentPermissionManager, null,
                null, visibilityValidator, null)
        {
            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        final boolean canEdit = commentService.hasPermissionToEdit(user, mockComment, errorCollection);

        // Check
        assertThat(canEdit, Matchers.is(false));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.no.comment.visibility")));
    }

    @Test
    public void testHasPermissionToEditWithNullApplicationUser()
    {
        // Set up
        final MutableComment mockComment = new MockComment(9L, "dude", "updateAuthor", "comm-ent", "group-level", 1L, null, null, null);
        when(commentPermissionManager.hasEditPermission((ApplicationUser) null, mockComment)).thenReturn(false);

        final VisibilityValidator visibilityValidator = mock(VisibilityValidator.class);
        when(visibilityValidator.isGroupVisibilityEnabled()).thenReturn(false);
        when(visibilityValidator.isProjectRoleVisibilityEnabled()).thenReturn(false);
        when(visibilityValidator.isValidVisibilityData(Mockito.<JiraServiceContext>any(),
                anyString(), Mockito.<Issue>any(), Mockito.<Visibility>any())).thenReturn(false);

        final CommentService commentService = new DefaultCommentService(
                null, null, jiraAuthenticationContext, commentPermissionManager, null,
                null, visibilityValidator, null)
        {
            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        final boolean canEdit = commentService.hasPermissionToEdit((ApplicationUser) null, mockComment, errorCollection);

        // Check
        assertThat(canEdit, Matchers.is(false));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.no.comment.visibility.no.user")));
    }

    @Test
    public void testHasPermissionToEditHappyPath()
    {
        // Set up
        final MutableComment mockComment = new MockComment(9L, "dude", "updateAuthor", "comm-ent", "group-level", 1L, null, null, null);
        when(commentPermissionManager.hasEditPermission(testUser, mockComment)).thenReturn(true);
        final VisibilityValidator visibilityValidator = mock(VisibilityValidator.class);
        when(visibilityValidator.isGroupVisibilityEnabled()).thenReturn(false);
        when(visibilityValidator.isProjectRoleVisibilityEnabled()).thenReturn(false);
        when(visibilityValidator.isValidVisibilityData(Mockito.<JiraServiceContext>any(),
                anyString(), Mockito.<Issue>any(), Mockito.<Visibility>any())).thenReturn(true);

        CommentService commentService = new DefaultCommentService(null, null, jiraAuthenticationContext, commentPermissionManager,
                null, null, visibilityValidator, null)
        {
            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }
        };
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        final boolean canEdit = commentService.hasPermissionToEdit(testUser, mockComment, errorCollection);

        // Check
        assertTrue(canEdit);
        assertFalse(errorCollection.hasAnyErrors());
    }

    @Test
    public void testHasPermissionToEditNonEditableWorkflowState()
    {
        // Set up
        final MutableComment mockComment =
                new MockComment(9L, "dude", "updateAuthor", "comm-ent", "group-level", 1L, null, null, null);
        when(commentPermissionManager.hasEditPermission(testUser, mockComment)).thenReturn(true);
        final CommentService commentService = new DefaultCommentService(null, null, jiraAuthenticationContext,
                commentPermissionManager, null, null, null, null)
        {
            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return false;
            }
        };
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        // Invoke
        final boolean canEdit = commentService.hasPermissionToEdit(testUser, mockComment, errorCollection);

        // Check
        assertThat(canEdit, Matchers.is(false));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(errorCollection.getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.edit.issue.non.editable")));
    }

    @Test
    public void testUserHasCommentDeleteAllPermission()
    {
        // Set up
        when(permissionManager.hasPermission(ProjectPermissions.DELETE_ALL_COMMENTS, issue, testUser)).thenReturn(true);
        final DefaultCommentService defaultCommentService =
                new DefaultCommentService(null, permissionManager, null, null, null, null, null, null);

        // Invoke
        final boolean hasPermission = defaultCommentService.userHasCommentDeleteAllPermission(issue, testUser);

        // Check
        assertThat(hasPermission, Matchers.is(true));
    }

    @Test
    public void testUserDoesNotHaveCommentDeleteAllPermission()
    {
        // Set up
        when(permissionManager.hasPermission(ProjectPermissions.DELETE_ALL_COMMENTS, issue, testUser)).thenReturn(false);
        final DefaultCommentService defaultCommentService =
                new DefaultCommentService(null, permissionManager, null, null, null, null, null, null);

        // Invoke
        final boolean hasPermission = defaultCommentService.userHasCommentDeleteAllPermission(issue, testUser);

        // Check
        assertThat(hasPermission, Matchers.is(false));
    }

    @Test
    public void testUserHasCommentDeleteOwnPermission()
    {
        // Set up
        when(permissionManager.hasPermission(ProjectPermissions.DELETE_OWN_COMMENTS, issue, testUser)).thenReturn(true);
        final DefaultCommentService defaultCommentService =
                new DefaultCommentService(null, permissionManager, null, null, null, null, null, null);

        // Invoke
        final boolean hasPermission = defaultCommentService.userHasCommentDeleteOwnPermission(issue, testUser);

        // Check
        assertThat(hasPermission, Matchers.is(true));
    }

    @Test
    public void testUserDoesNotHaveCommentDeleteOwnPermission()
    {
        // Set up
        when(permissionManager.hasPermission(ProjectPermissions.DELETE_OWN_COMMENTS, issue, testUser)).thenReturn(false);
        final DefaultCommentService defaultCommentService =
                new DefaultCommentService(null, permissionManager, null, null, null, null, null, null);

        // Invoke
        final boolean hasPermission = defaultCommentService.userHasCommentDeleteOwnPermission(issue, testUser);

        // Check
        assertThat(hasPermission, Matchers.is(false));
    }

    @Test
    public void testDeleteWithNullComment()
    {
        // Set up
        final DefaultCommentService defaultCommentService =
                new DefaultCommentService(null, null, jiraAuthenticationContext, null, null, null, null, null);
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, new SimpleErrorCollection());

        // Invoke
        defaultCommentService.delete(jiraServiceContext, null, true);

        // Check
        assertThat(jiraServiceContext.getErrorCollection().getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(jiraServiceContext.getErrorCollection().getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.delete.null.comment")));
    }

    @Test
    public void testDeleteWithNullCommentId()
    {
        // Set up
        final DefaultCommentService defaultCommentService =
                new DefaultCommentService(null, null, jiraAuthenticationContext, null, null, null, null, null);
        final Comment mockComment = mock(Comment.class);
        when(mockComment.getId()).thenReturn(null);

        // Invoke
        defaultCommentService.delete(jiraServiceContext, mockComment, true);

        // Check
        assertThat(jiraServiceContext.getErrorCollection().getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(jiraServiceContext.getErrorCollection().getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.delete.null.comment")));
    }

    @Test
    public void testHasPermissionToDeleteIssueNotEditable()
    {
        final Comment mockComment = mock(Comment.class);
        when(mockComment.getIssue()).thenReturn(issue);
        final DefaultCommentService defaultCommentService =
                new DefaultCommentService(null, null, jiraAuthenticationContext, null, null, null, null, null)
        {
            protected boolean hasVisibility(JiraServiceContext jiraServiceContext, Comment comment)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return false;
            }

            public Comment getCommentById(ApplicationUser user, Long commentId, ErrorCollection errorCollection)
            {
                return mockComment;
            }
        };

        // Invoke
        final boolean canDelete = defaultCommentService.hasPermissionToDelete(jiraServiceContext, COMMENT_ID);

        // Check
        assertFalse(canDelete);
        assertThat(jiraServiceContext.getErrorCollection().getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(jiraServiceContext.getErrorCollection().getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.delete.issue.non.editable")));
    }

    @Test
    public void testHasPermissionToDeleteHasDeleteAllPermission()
    {
        // Set up
        final Comment mockComment = mock(Comment.class);
        when(mockComment.getIssue()).thenReturn(issue);
        final DefaultCommentService defaultCommentService =
                new DefaultCommentService(null, null, jiraAuthenticationContext, null, null, null, null, null)
        {
            protected boolean hasVisibility(JiraServiceContext jiraServiceContext, Comment comment)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }

            public Comment getCommentById(ApplicationUser user, Long commentId, ErrorCollection errorCollection)
            {
                return mockComment;
            }

            boolean userHasCommentDeleteAllPermission(Issue issue, ApplicationUser user)
            {
                return true;
            }
        };

        // Invoke
        final boolean canDelete = defaultCommentService.hasPermissionToDelete(jiraServiceContext, COMMENT_ID);

        // Check
        assertTrue(canDelete);
    }

    @Test
    public void testHasPermissionToDeleteHasDeleteOwnPermissionUserIsAuthor()
    {
        // Set up
        final Comment mockComment = mock(Comment.class);
        when(mockComment.getIssue()).thenReturn(issue);
        when(commentManager.isUserCommentAuthor(jiraServiceContext.getLoggedInApplicationUser(), mockComment)).thenReturn(true);
        final DefaultCommentService defaultCommentService = new DefaultCommentService(
                commentManager, null, jiraAuthenticationContext, null, null, null, null, null)
        {
            protected boolean hasVisibility(JiraServiceContext jiraServiceContext, Comment comment)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }

            public Comment getCommentById(ApplicationUser user, Long commentId, ErrorCollection errorCollection)
            {
                return mockComment;
            }

            boolean userHasCommentDeleteAllPermission(Issue issue, ApplicationUser user)
            {
                return false;
            }

            boolean userHasCommentDeleteOwnPermission(Issue issue, ApplicationUser user)
            {
                return true;
            }
        };

        // Invoke
        final boolean canDelete = defaultCommentService.hasPermissionToDelete(jiraServiceContext, COMMENT_ID);

        // Check
        assertTrue(canDelete);
    }

    @Test
    public void testHasPermissionToDeleteHasDeleteOwnUserPermissionIsNotAuthor()
    {
        // Set up
        final Comment mockComment = mock(Comment.class);
        when(mockComment.getIssue()).thenReturn(issue);
        when(mockComment.getId()).thenReturn(COMMENT_ID);
        when(commentManager.isUserCommentAuthor(jiraAuthenticationContext.getUser(), mockComment)).thenReturn(false);
        final DefaultCommentService defaultCommentService = new DefaultCommentService(
                commentManager, null, jiraAuthenticationContext, null, null, null, null, null)
        {
            protected boolean hasVisibility(JiraServiceContext jiraServiceContext, Comment comment)
            {
                return true;
            }

            boolean isIssueInEditableWorkflowState(Issue issue)
            {
                return true;
            }

            public Comment getCommentById(ApplicationUser user, Long commentId, ErrorCollection errorCollection)
            {
                return mockComment;
            }

            boolean userHasCommentDeleteAllPermission(Issue issue, ApplicationUser user)
            {
                return false;
            }

            boolean userHasCommentDeleteOwnPermission(Issue issue, ApplicationUser user)
            {
                return true;
            }
        };

        // Invoke
        final boolean canDelete = defaultCommentService.hasPermissionToDelete(jiraServiceContext, COMMENT_ID);

        // Check
        assertFalse(canDelete);

        assertThat(jiraServiceContext.getErrorCollection().getErrorMessages(), Matchers.<String>iterableWithSize(1));
        assertThat(jiraServiceContext.getErrorCollection().getErrorMessages(), Matchers.<String>hasItem(Matchers.containsString("comment.service.error.delete.no.permission")));
    }

    @Test
    public void setCommentPropertiesDuringCommentCreation()
    {
        TextFieldCharacterLengthValidator textFieldCharacterLengthValidator = mock(TextFieldCharacterLengthValidator.class);
        when(textFieldCharacterLengthValidator.isTextTooLong(anyString())).thenReturn(false);
        VisibilityValidator visibilityValidator = mock(VisibilityValidator.class);
        when(visibilityValidator.isGroupVisibilityEnabled()).thenReturn(false);
        when(visibilityValidator.isProjectRoleVisibilityEnabled()).thenReturn(false);
        when(visibilityValidator.isValidVisibilityData(any(JiraServiceContext.class), anyString(), any(Issue.class), Mockito.<Visibility>any())).thenReturn(true);

        when(commentPropertyService.validatePropertyInput(Mockito.<PropertyInput>any())).thenReturn(new SimpleErrorCollection());
        Comment mockComment = mock(Comment.class);
        when(commentManager.create(eq(issue), eq(testUser), eq("comment-body"), anyString(), anyLong(), any(Date.class), Mockito.<Map<String, JSONObject>>any(), anyBoolean())).thenReturn(mockComment);
        when(commentPropertyService.validateSetProperty(eq(testUser), anyLong(), any(PropertyInput.class))).thenReturn(
                new SetPropertyValidationResult(new SimpleErrorCollection(), Option.some(new EntityPropertyInput("value", "key", 0l, "prop"))));

        final DefaultCommentService commentService = new DefaultCommentService(commentManager, permissionManager, jiraAuthenticationContext, commentPermissionManager,
                null, null, visibilityValidator, textFieldCharacterLengthValidator)
        {
            @Override
            public boolean hasPermissionToCreate(final ApplicationUser user, final Issue issue, final ErrorCollection errorCollection)
            {
                return true;
            }
        };

        final Map<String, JSONObject> properties = ImmutableMap.of(
                "property-key", new JSONObject(ImmutableMap.<String, Object>of("x", true))
        );
        CommentService.CommentParameters commentParameters = CommentService.CommentParameters.builder()
                .author(testUser)
                .body("comment-body")
                .commentProperties(properties)
                .issue(issue)
                .build();
        CommentService.CommentCreateValidationResult validationResult = commentService.validateCommentCreate(testUser, commentParameters);

        assertThat(validationResult.isValid(), Matchers.is(true));

        Comment comment = commentService.create(testUser, validationResult, true);

        assertThat(comment, Matchers.notNullValue());
    }

    @Test
    public void hasPermissionToCreateDelegatesOnPermissionManager()
    {
        Issue issue = issueWithProject();
        ApplicationUser anyUser = mock(ApplicationUser.class);
        when(permissionManager.hasPermission(ProjectPermissions.ADD_COMMENTS, issue, anyUser)).thenReturn(true);

        boolean hasPermission = createCommentService().hasPermissionToCreate(anyUser, issue, mock(ErrorCollection.class));

        assertThat(hasPermission, is(true));
    }

    @Test
    public void isValidCommentBodyValidatesBodyLengthAgainstCharacterLimit()
    {
        final String body = "Comment body\nwith\r\nnewline";
        final long limit = body.length();

        when(textFieldCharacterLengthValidator.isTextTooLong(anyString())).thenAnswer(new Answer<Boolean>()
        {
            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable
            {
                String input = (String) invocation.getArguments()[0];
                return input.length() > limit;
            }
        });
        when(textFieldCharacterLengthValidator.getMaximumNumberOfCharacters()).thenReturn(limit);

        final CommentService commentService = new DefaultCommentService(null, null, jiraAuthenticationContext,
                null, null, null, null, textFieldCharacterLengthValidator);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        boolean valid = commentService.isValidCommentBody(body, errorCollection);

        assertTrue("Comment should be considered as valid", valid);
        assertThat("No errors in errorcollection", errorCollection.hasAnyErrors(), is(false));

        verify(textFieldCharacterLengthValidator).isTextTooLong(body);
    }

    @Test
    public void isValidCommentBodyDetectsTooLongComment()
    {
        final String body = "Comment body\nwith\r\nnewline";
        final long limit = body.length() - 1;

        when(textFieldCharacterLengthValidator.isTextTooLong(anyString())).thenAnswer(new Answer<Boolean>()
        {
            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable
            {
                String input = (String) invocation.getArguments()[0];
                return input.length() > limit;
            }
        });
        when(textFieldCharacterLengthValidator.getMaximumNumberOfCharacters()).thenReturn(limit);

        final CommentService commentService = new DefaultCommentService(null, null, jiraAuthenticationContext,
                null, null, null, null, textFieldCharacterLengthValidator);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        boolean valid = commentService.isValidCommentBody(body, errorCollection);

        assertFalse("Comment should be considered as too long", valid);
        assertThat("Error exists in errorcollection", errorCollection.hasAnyErrors(), is(true));
        verify(textFieldCharacterLengthValidator).isTextTooLong(body);
    }

    private Issue issueWithProject()
    {
        Issue issue = mock(Issue.class);
        when(issue.getProjectObject()).thenReturn(mock(Project.class));
        return issue;
    }

    private DefaultCommentService createCommentService()
    {
        return new DefaultCommentService(
                mock(CommentManager.class),
                permissionManager,
                jiraAuthenticationContext,
                mock(CommentPermissionManager.class),
                mock(IssueUpdater.class),
                mock(IssueManager.class),
                mock(VisibilityValidator.class),
                mock(TextFieldCharacterLengthValidator.class)
        );
    }
}
