package com.atlassian.jira.bc.issue.attachment;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.AnswerWith;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static com.atlassian.jira.security.Permissions.ATTACHMENT_DELETE_ALL;
import static com.atlassian.jira.security.Permissions.ATTACHMENT_DELETE_OWN;
import static com.atlassian.jira.security.Permissions.CREATE_ATTACHMENT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultAttachmentService
{
    private static final String SHOULD_HAVE_DELETE_PERMISSIONS = "Should have delete permissions";
    private static final String SHOULD_BE_AUTHOR_OF_ATTACHMENT = "Should be author of attachment";
    private static final String SHOULD_HAVE_DELETE_ALL_PERMISSIONS = "Should have delete all permissions";
    private static final String SHOULD_HAVE_DELETE_OWN_PERMISSIONS = "Should have delete own permissions";
    private static final String SHOULD_HAVE_CREATE_ATTACHMENT_PERMISSIONS = "Should have create attachment permissions";
    private static final String SHOULD_HAVE_MANAGE_ATTACHMENT_PERMISSIONS = "Should have manage attachments permissions";
    private static final String SHOULD_HAVE_ATTACH_SCREENSHOT_PERMISSIONS = "Should have attach screenshot permissions";
    private static final String APPLET_SHOULD_BE_SUPPORTED = "Appplet should be supported";
    private static final String TEST_ERROR_MESSAGE = "Test Error Message";
    private static final String SHOULD_NOT_HAVE_ANY_ERRORS = "Should NOT have any errors";
    private static final String SHOULD_NOT_HAVE_DELETE_PERMISSIONS = "Should NOT have delete permissions";
    private static final String SHOULD_NOT_HAVE_CREATE_ATTACHMENT_PERMISSIONS = "Should NOT have create attachment permissions";
    private static final String SHOULD_NOT_HAVE_ATTACH_SCREENSHOT_PERMISSIONS = "Should NOT have attach screenshot permissions";
    private static final String SHOULD_NOT_HAVE_MANAGE_ATTACHMENTS_PERMISSIONS = "Should NOT have manage attachments permissions";
    private static final String SHOULD_NOT_BE_AUTHOR_OF_ATTACHMENT = "Should NOT be author of single attachment";
    private static final String SHOULD_NOT_HAVE_DELETE_ALL_PERMISSIONS = "Should NOT have delete all permissions";
    private static final String SHOULD_NOT_HAVE_DELETE_OWN_PERMISSIONS = "Should NOT have delete own permissions";
    private static final String APPLET_SHOULD_NOT_BE_ENABLED_AND_SUPPORTED = "Appplet should NOT be enabled and supported";
    private static final Long ATTACHMENT_ID = 1L;

    @Rule
    public final RuleChain mockito = MockitoMocksInContainer.forTest(this);

    @Mock
    private AttachmentManager mockAttachmentManager;

    @Mock
    private IssueManager mockIssueManager;

    @Mock
    private IssueUpdater mockIssueUpdater;

    @Mock
    private PermissionManager mockPermissionManager;

    @Mock
    private I18nHelper i18nHelper;

    @Mock
    private Attachment mockAttachment;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    private ErrorCollection errorCollection;

    private Issue mockIssue;

    private JiraServiceContext jiraServiceContext;

    private Project mockProject;

    private ApplicationUser testUser;

    private ApplicationUser otherUser;

    private List<Attachment> userCreatedAttachments;

    private DefaultAttachmentService defaultAttachmentService;

    @Before
    public void setUp() throws Exception
    {
        testUser = createMockApplicationUser("testuser");
        otherUser = createMockApplicationUser("otheruser");
        mockIssue = new MockIssue(ATTACHMENT_ID);
        mockProject = new MockProject(10040);
        errorCollection = new SimpleErrorCollection();
        jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);

        when(i18nHelper.getText(anyString())).thenAnswer(AnswerWith.firstParameter());
        when(i18nHelper.getText(anyString(), anyString())).thenAnswer(AnswerWith.firstParameter());
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);

        userCreatedAttachments = new ArrayList<Attachment>();
        userCreatedAttachments.add(mockAttachment);

        defaultAttachmentService = new DefaultAttachmentService(mockAttachmentManager, mockPermissionManager, jiraAuthenticationContext, mockIssueUpdater, mockIssueManager);
    }

    @Test
    public void testCanDeleteAnyAttachmentValidation()
    {
        defaultAttachmentService.canDeleteAnyAttachment(null, null, errorCollection);

        verifyErrorMessageExists("attachment.service.error.null.issue");
    }

    @Test
    public void testCanDeleteAnyAttachmentWithDeleteAllPermission()
    {
        setupPermissionForUser(Permissions.ATTACHMENT_DELETE_ALL, null);

        assertTrue(SHOULD_HAVE_DELETE_PERMISSIONS, defaultAttachmentService.canDeleteAnyAttachment(null, mockIssue, errorCollection));
    }

    @Test
    public void testCanDeleteAnyAttachmentWithDeleteOwnPermissionAndUserHasAuthoredAttachment()
    {
        setupPermissionForUser(Permissions.ATTACHMENT_DELETE_OWN, testUser);
        setupAttachmentForUser(testUser);

        assertTrue(SHOULD_HAVE_DELETE_PERMISSIONS, defaultAttachmentService.canDeleteAnyAttachment(testUser, mockIssue, errorCollection));
    }

    @Test
    public void testCanDeleteAnyAttachmentWithDeleteOwnPermissionAndUserHasNotAuthoredAttachment()
    {
        setupPermissionForUser(Permissions.ATTACHMENT_DELETE_OWN, testUser);
        setupAttachmentForUser(otherUser);

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultAttachmentService.canDeleteAnyAttachment(testUser, mockIssue, errorCollection));
    }

    @Test
    public void testCanDeleteAnyAttachmentWithNoPermissionsAndUserHasAuthoredAttachment()
    {
        // no permissions are set up
        setupAttachmentForUser(testUser);

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultAttachmentService.canDeleteAnyAttachment(testUser, mockIssue, errorCollection));
    }

    @Test
    public void testCanDeleteAnyAttachmentWithNoPermissionsAndUserHasNotAuthoredAttachment()
    {
        // no permissions are set up

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultAttachmentService.canDeleteAnyAttachment(testUser, mockIssue, errorCollection));
    }

    @Test
    public void testIsAuthorOfAtLeastOneAttachmentTrue()
    {
        setupAttachmentForUser(testUser);

        assertTrue(SHOULD_BE_AUTHOR_OF_ATTACHMENT, defaultAttachmentService.isAuthorOfAtLeastOneAttachment(mockIssue, testUser));
    }

    @Test
    public void testIsAuthorOfAtLeastOneAttachmentFalse()
    {
        setupAttachmentForUser(otherUser);

        assertFalse(SHOULD_NOT_BE_AUTHOR_OF_ATTACHMENT, defaultAttachmentService.isAuthorOfAtLeastOneAttachment(mockIssue, testUser));
    }

    @Test
    public void testIsUserAttachmentAuthorWithMatchingNames()
    {
        setupAttachmentForUser(testUser);

        assertTrue(SHOULD_BE_AUTHOR_OF_ATTACHMENT, defaultAttachmentService.isUserAttachmentAuthor(mockAttachment, testUser));
    }

    @Test
    public void testIsUserAttachmentAuthorWithMismatchedNames()
    {
        setupAttachmentForUser(otherUser);

        assertFalse(SHOULD_NOT_BE_AUTHOR_OF_ATTACHMENT, defaultAttachmentService.isUserAttachmentAuthor(mockAttachment, testUser));
    }

    @Test
    public void testIsUserAttachmentAuthorWithAuthorNull()
    {
        setupAttachmentForUser(null);

        assertFalse(SHOULD_NOT_BE_AUTHOR_OF_ATTACHMENT, defaultAttachmentService.isUserAttachmentAuthor(mockAttachment, testUser));
    }

    @Test
    public void testIsUserAttachmentAuthorWithUserNull()
    {
        setupAttachmentForUser(testUser);

        assertFalse(SHOULD_NOT_BE_AUTHOR_OF_ATTACHMENT, defaultAttachmentService.isUserAttachmentAuthor(mockAttachment, null));
    }

    @Test
    public void testIsUserAttachmentAuthorWithUserAndAuthorNull()
    {
        setupAttachmentForUser(null);

        assertTrue(SHOULD_BE_AUTHOR_OF_ATTACHMENT, defaultAttachmentService.isUserAttachmentAuthor(mockAttachment, null));
    }

    @Test
    public void testUserHasAttachmentDeleteAllPermission()
    {
        setupPermissionForUser(ATTACHMENT_DELETE_ALL, testUser);

        assertTrue(SHOULD_HAVE_DELETE_ALL_PERMISSIONS, defaultAttachmentService.userHasAttachmentDeleteAllPermission(mockIssue, testUser));
    }

    @Test
    public void testUserDoesNotHaveAttachmentDeleteAllPermission()
    {
        setupNoPermissionForUser(ATTACHMENT_DELETE_ALL, testUser);

        assertFalse(SHOULD_NOT_HAVE_DELETE_ALL_PERMISSIONS, defaultAttachmentService.userHasAttachmentDeleteAllPermission(mockIssue, testUser));
    }

    @Test
    public void testUserHasAttachmentDeleteOwnPermission()
    {
        setupPermissionForUser(ATTACHMENT_DELETE_OWN, testUser);

        assertTrue(SHOULD_HAVE_DELETE_OWN_PERMISSIONS, defaultAttachmentService.userHasAttachmentDeleteOwnPermission(mockIssue, testUser));
    }

    @Test
    public void testUserDoesNotHaveAttachmentDeleteOwnPermission()
    {
        setupNoPermissionForUser(ATTACHMENT_DELETE_OWN, testUser);

        assertFalse(SHOULD_NOT_HAVE_DELETE_OWN_PERMISSIONS, defaultAttachmentService.userHasAttachmentDeleteOwnPermission(mockIssue, testUser));
    }

    @Test
    public void testUserHasCreateAttachmentPermission()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);

        assertTrue(SHOULD_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.userHasCreateAttachmentPermission(mockIssue, testUser));
    }

    @Test
    public void testUserDoesNotHaveCreateAttachmentPermission()
    {
        setupNoPermissionForUser(CREATE_ATTACHMENT, testUser);

        assertFalse(SHOULD_NOT_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.userHasCreateAttachmentPermission(mockIssue, testUser));
    }

    @Test
    public void testCanDeleteAttachmentHappyPathWithPopulatedErrorCollectionPassedIn()
    {
        setupPermissionForUser(ATTACHMENT_DELETE_ALL, testUser);
        setupAttachmentForUser(testUser);
        setupIssueEditable(true);

        //error messages passed in shall not stray us from the happy path!
        jiraServiceContext.getErrorCollection().addErrorMessage(TEST_ERROR_MESSAGE);

        assertTrue(SHOULD_HAVE_DELETE_PERMISSIONS, defaultAttachmentService.canDeleteAttachment(jiraServiceContext, ATTACHMENT_ID));
        verifyErrorMessageExists(TEST_ERROR_MESSAGE);
    }

    @Test
    public void testCanDeleteAttachmentWithDeleteAllPermission()
    {
        setupPermissionForUser(ATTACHMENT_DELETE_ALL, testUser);
        setupAttachmentForUser(testUser);
        setupIssueEditable(true);

        assertTrue(SHOULD_HAVE_DELETE_PERMISSIONS, defaultAttachmentService.canDeleteAttachment(jiraServiceContext, ATTACHMENT_ID));
        assertFalse(SHOULD_NOT_HAVE_ANY_ERRORS, errorCollection.hasAnyErrors());
    }

    @Test
    public void testCanDeleteAttachmentWithDeleteAllPermissionIssueNotInEditableWorkflowState()
    {
        setupPermissionForUser(ATTACHMENT_DELETE_OWN, testUser);
        setupAttachmentForUser(testUser);
        setupIssueEditable(false);

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultAttachmentService.canDeleteAttachment(jiraServiceContext, ATTACHMENT_ID));
        verifyErrorMessageExists("attachment.service.error.delete.issue.non.editable");
    }

    @Test
    public void testCanDeleteAttachmentWithDeleteOwnPermissionAndDoesOwnAttachment()
    {
        setupPermissionForUser(ATTACHMENT_DELETE_OWN, testUser);
        setupAttachmentForUser(testUser);
        setupIssueEditable(true);

        assertTrue(SHOULD_HAVE_DELETE_PERMISSIONS, defaultAttachmentService.canDeleteAttachment(jiraServiceContext, ATTACHMENT_ID));
        assertFalse(SHOULD_NOT_HAVE_ANY_ERRORS, errorCollection.hasAnyErrors());
    }

    @Test
    public void testCanDeleteAttachmentWithDeleteOwnPermissionAndDoesntOwnAttachment()
    {
        setupPermissionForUser(ATTACHMENT_DELETE_OWN, testUser);
        setupAttachmentForUser(otherUser);
        setupIssueEditable(true);

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultAttachmentService.canDeleteAttachment(jiraServiceContext, ATTACHMENT_ID));
        verifyErrorMessageExists("attachment.service.error.delete.no.permission");
    }

    @Test
    public void testGetAndVerifyAttachmentWithNullId()
    {
        assertNull(defaultAttachmentService.getAndVerifyAttachment(null, errorCollection));
        verifyErrorMessageExists("attachment.service.error.null.attachment.id");
    }

    @Test
    public void testGetAndVerifyAttachmentWithNullAttachment()
    {
        when(mockAttachmentManager.getAttachment(ATTACHMENT_ID)).thenReturn(null);

        final Attachment attachment = defaultAttachmentService.getAndVerifyAttachment(ATTACHMENT_ID, errorCollection);

        assertNull(attachment);
        verifyErrorMessageExists("attachment.service.error.null.attachment");
    }

    @Test
    public void testGetAndVerifyIssueWithNullIssue()
    {
        setupIssueAndAttachmentForUser(testUser, null);

        assertNull(defaultAttachmentService.getAndVerifyIssue(mockAttachment, errorCollection));
        verifyErrorMessageExists("attachment.service.error.null.issue.for.attachment");
    }

    @Test
    public void testDeleteHappyPath() throws Exception
    {
        setupAttachmentForUser(testUser);

        defaultAttachmentService.delete(jiraServiceContext, ATTACHMENT_ID);

        assertFalse(SHOULD_NOT_HAVE_ANY_ERRORS, errorCollection.hasAnyErrors());
        verify(mockAttachmentManager).deleteAttachment(mockAttachment);
        verify(mockIssueUpdater).doUpdate(any(IssueUpdateBean.class), anyBoolean());
    }

    @Test
    public void testDeleteAttachmentManagerThrowsException() throws Exception
    {
        doThrow(new RemoveException()).when(mockAttachmentManager).deleteAttachment(mockAttachment);
        setupAttachmentForUser(testUser);

        defaultAttachmentService.delete(jiraServiceContext, ATTACHMENT_ID);

        verifyErrorMessageExists("attachment.service.error.delete.attachment.failed");
    }

    @Test
    public void testCanCreateAttachmentsNullIssue()
    {
        assertFalse(SHOULD_NOT_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canCreateAttachments(jiraServiceContext, (Issue) null));
        verifyErrorMessageExists("attachment.service.error.null.issue");
    }

    @Test
    public void testCanCreateAttachmentsNullProject()
    {
        assertFalse(SHOULD_NOT_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canCreateAttachments(jiraServiceContext, (Project) null));
        verifyErrorMessageExists("attachment.service.error.null.project");
    }

    @Test
    public void testCanCreateAttachmentsHappyPath()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(true);
        setupIssueEditable(true);

        assertTrue(SHOULD_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockIssue));
    }

    @Test
    public void testCanCreateAttachmentsForProjectHappyPath()
    {
        setupPermissionsForProject(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(true);
        setupIssueEditable(true);

        assertTrue(SHOULD_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockProject));
    }

    @Test
    public void testCanCreateAttachmentsAttachmentsDisabled()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(false);
        setupIssueEditable(true);

        assertFalse(SHOULD_NOT_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockIssue));
        verifyErrorMessageExists("attachment.service.error.attachments.disabled");
    }

    @Test
    public void testCanCreateAttachmentsAttachmentsForProjectDisabled()
    {
        setupNoPermissionsForProject(CREATE_ATTACHMENT, testUser);
        setupIssueEditable(true);

        assertFalse(SHOULD_NOT_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockProject));
        verifyErrorMessageExists("attachment.service.error.attachments.disabled");
    }

    @Test
    public void testCanCreateAttachmentsNoCreateAttachmentPermission()
    {
        setupNoPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(true);
        setupIssueEditable(true);

        assertFalse(SHOULD_NOT_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockIssue));
        verifyErrorMessageExists("attachment.service.error.create.no.permission");
    }

    @Test
    public void testCanCreateAttachmentsNoCreateAttachmentPermissionForProject()
    {
        setupNoPermissionsForProject(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(true);
        setupIssueEditable(true);

        assertFalse(SHOULD_NOT_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockProject));
        verifyErrorMessageExists("attachment.service.error.create.no.permission.project");
    }

    @Test
    public void testCanCreateAttachmentsNotInEditableWorkflowState()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(true);
        setupIssueEditable(false);

        assertFalse(SHOULD_NOT_HAVE_CREATE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canCreateAttachments(jiraServiceContext, mockIssue));
        verifyErrorMessageExists("attachment.service.error.create.issue.non.editable");
    }

    @Test
    public void testCanAttachScreenshotsHappyPath()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(true);
        setupIssueEditable(true);
        setupScreenshotAppletEnabledAndSupportedByOS(true, true);

        assertTrue(SHOULD_HAVE_ATTACH_SCREENSHOT_PERMISSIONS, defaultAttachmentService.canAttachScreenshots(jiraServiceContext, mockIssue));
    }

    @Test
    public void testCanAttachScreenshotsCantCreateAttachments()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(false);
        setupIssueEditable(true);

        assertFalse(SHOULD_NOT_HAVE_ATTACH_SCREENSHOT_PERMISSIONS, defaultAttachmentService.canAttachScreenshots(jiraServiceContext, mockIssue));
    }

    @Test
    public void testCanAttachScreenshotsScreenshotAppletDisabled()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(true);
        setupIssueEditable(true);
        setupScreenshotAppletEnabledAndSupportedByOS(false, false);

        assertFalse(SHOULD_NOT_HAVE_ATTACH_SCREENSHOT_PERMISSIONS, defaultAttachmentService.canAttachScreenshots(jiraServiceContext, mockIssue));
    }

    @Test
    public void testCanManageAttachmentsHappyPathWithPopulatedErrorCollectionPassedIn()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(true);
        setupIssueEditable(true);

        //error messages passed in shall not stray us from the happy path!
        jiraServiceContext.getErrorCollection().addErrorMessage(TEST_ERROR_MESSAGE);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertTrue(SHOULD_HAVE_MANAGE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
        verifyErrorMessageExists(TEST_ERROR_MESSAGE);
    }

    @Test
    public void testCanManageAttachmentsFullPermissionsHappyPath()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(true);
        setupIssueEditable(true);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertTrue(SHOULD_HAVE_MANAGE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
    }

    @Test
    public void testCanManageAttachmentsCreatePermissionHappyPath()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(true);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertTrue(SHOULD_HAVE_MANAGE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
    }

    @Test
    public void testCanManageAttachmentsDeletePermissionHappyPath()
    {
        setupPermissionForUser(ATTACHMENT_DELETE_ALL, testUser);
        setupAttachmentEnabled(true);
        setupIssueEditable(true);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertTrue(SHOULD_HAVE_MANAGE_ATTACHMENT_PERMISSIONS, defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
    }

    @Test
    public void testCanManageAttachmentsNoAttachmentPermissions()
    {
        // no mockAttachment permissions
        setupAttachmentEnabled(true);
        setupIssueEditable(true);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertFalse(SHOULD_NOT_HAVE_MANAGE_ATTACHMENTS_PERMISSIONS, defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
        verifyErrorMessageExists("attachment.service.error.manage.no.permission");
    }

    @Test
    public void testCanManageAttachmentsAttachmentsDisabled()
    {
        setupPermissionForUser(CREATE_ATTACHMENT, testUser);
        setupAttachmentEnabled(false);
        setupIssueEditable(true);

        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);
        assertFalse(SHOULD_NOT_HAVE_MANAGE_ATTACHMENTS_PERMISSIONS, defaultAttachmentService.canManageAttachments(jiraServiceContext, mockIssue));
        verifyErrorMessageExists("attachment.service.error.attachments.disabled");
    }

    @Test
    public void testIsScreenshotAppletEnabledAndSupportedByOSHappyPath()
    {
        setupScreenshotAppletEnabledAndSupportedByOS(true, true);

        final boolean supportedByOS =
                defaultAttachmentService.isScreenshotAppletEnabledAndSupportedByOS(jiraServiceContext);

        assertTrue(APPLET_SHOULD_BE_SUPPORTED, supportedByOS);
        assertFalse(SHOULD_NOT_HAVE_ANY_ERRORS, jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testIsScreenshotAppletEnabledAndSupportedByOSAppletDisabled()
    {
        setupScreenshotAppletEnabledAndSupportedByOS(false, true);

        assertFalse(APPLET_SHOULD_NOT_BE_ENABLED_AND_SUPPORTED, defaultAttachmentService.isScreenshotAppletEnabledAndSupportedByOS(jiraServiceContext));
        verifyErrorMessageExists("attachment.service.error.screenshot.applet.disabled");
    }

    @Test
    public void testIsScreenshotAppletEnabledAndSupportedByOSUnsupportedOS()
    {
        setupScreenshotAppletEnabledAndSupportedByOS(true, false);

        assertFalse(APPLET_SHOULD_NOT_BE_ENABLED_AND_SUPPORTED, defaultAttachmentService.isScreenshotAppletEnabledAndSupportedByOS(jiraServiceContext));
        verifyErrorMessageExists("attachment.service.error.screenshot.applet.unsupported.os");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ApplicationUser createMockApplicationUser(final String userName)
    {
        return createMockApplicationUser(userName, userName, userName + "@example.com");
    }

    private ApplicationUser createMockApplicationUser(final String userName, final String name, final String email)
    {
        return new MockApplicationUser(userName, name, email);
    }

    private void verifyErrorMessageExists(final String errorMessage)
    {
        assertThat(errorCollection.getErrorMessages(), Matchers.contains(errorMessage));
    }

    private void setupAttachmentForUser(final ApplicationUser applicationUser)
    {
        setupIssueAndAttachmentForUser(applicationUser, mockIssue);
    }

    private void setupIssueAndAttachmentForUser(final ApplicationUser applicationUser, Issue issue)
    {
        when(mockAttachmentManager.getAttachments(eq(issue))).thenReturn(userCreatedAttachments);
        when(mockAttachmentManager.getAttachment(anyLong())).thenReturn(mockAttachment);
        when(mockAttachment.getAuthorObject()).thenReturn(applicationUser);
        when(mockAttachment.getIssueObject()).thenReturn(issue);
    }

    private void setupIssueEditable(final boolean editable)
    {
        when(mockIssueManager.isEditable(eq(mockIssue))).thenReturn(editable);
    }

    private void setupPermissionForUser(final int permissionId, final ApplicationUser applicationUser)
    {
        when(mockPermissionManager.hasPermission(eq(permissionId), eq(mockIssue), eq(applicationUser))).thenReturn(true);
    }

    private void setupPermissionsForProject(final int permissionId, final ApplicationUser applicationUser)
    {
        when(mockPermissionManager.hasPermission(eq(permissionId), eq(mockProject), eq(applicationUser))).thenReturn(true);
    }

    private void setupNoPermissionsForProject(final int permissionId, final ApplicationUser applicationUser)
    {
        when(mockPermissionManager.hasPermission(eq(permissionId), eq(mockProject), eq(applicationUser))).thenReturn(false);
    }

    private void setupNoPermissionForUser(final int permissionId, final ApplicationUser applicationUser)
    {
        when(mockPermissionManager.hasPermission(eq(permissionId), eq(mockIssue), eq(applicationUser))).thenReturn(false);
    }

    private void setupAttachmentEnabled(final boolean enabled)
    {
        when(mockAttachmentManager.attachmentsEnabled()).thenReturn(enabled);
    }

    private void setupScreenshotAppletEnabledAndSupportedByOS(final boolean enabled, final boolean supportedByOs)
    {
        when(mockAttachmentManager.isScreenshotAppletEnabled()).thenReturn(enabled);
        when(mockAttachmentManager.isScreenshotAppletSupportedByOS()).thenReturn(supportedByOs);
    }
}
