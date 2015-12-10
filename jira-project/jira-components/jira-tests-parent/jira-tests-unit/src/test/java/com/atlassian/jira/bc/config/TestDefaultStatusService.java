package com.atlassian.jira.bc.config;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.StatusCategoryManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v6.1
 */
public class TestDefaultStatusService
{
    @Mock
    private StatusManager statusManager;
    @Mock
    private ConstantsManager constantsManager;
    @Mock
    private PermissionManager permissionManager;
    @AvailableInContainer (instantiateMe = true)
    private MockI18nHelper i18nHelper;
    @Mock
    private WorkflowManager workflowManager;

    @Rule
    public MockitoContainer mocks = new MockitoContainer(this);

    private DefaultStatusService defaultStatusService;

    private MockStatus status;
    @Mock
    private StatusCategory statusCategory;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private StatusCategoryManager statusCategoryManager;
    private ApplicationUser user;

    @Before
    public void setUp()
    {
        defaultStatusService = new DefaultStatusService(statusManager, i18nHelper, constantsManager, permissionManager, workflowManager, eventPublisher, statusCategoryManager);
        status = new MockStatus("123", "MyStatus", statusCategory);
        user = new MockApplicationUser("fred", "freddy");
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
    }

    @Test
    public void shouldNotAllowToDoAnythingWithoutProperPermission()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        ErrorCollection editErrors = testEditValidationFail("Name", "description", "http://www.example.com", statusCategory);
        assertThat(editErrors.getErrorMessages(), Matchers.contains("admin.errors.not.allowed.to.edit.status"));

        ErrorCollection addErrors = testAddValidationFail("Name", "description", "http://www.example.com", statusCategory);
        assertThat(addErrors.getErrorMessages(), Matchers.contains("admin.errors.not.allowed.to.create.status"));

        final ServiceResult removeErrors = defaultStatusService.removeStatus(user, status);
        assertThat(removeErrors.getErrorCollection().getErrorMessages(), Matchers.contains("admin.errors.not.allowed.to.remove.status"));

        final ServiceResult moveUpErrors = defaultStatusService.moveStatusUp(user, status.getId());
        assertThat(moveUpErrors.getErrorCollection().getErrorMessages(), Matchers.contains("admin.errors.not.allowed.to.edit.status"));

        final ServiceResult moveDownErrors = defaultStatusService.moveStatusDown(user, status.getId());
        assertThat(moveDownErrors.getErrorCollection().getErrorMessages(), Matchers.contains("admin.errors.not.allowed.to.edit.status"));
    }

    @Test
    public void shouldNotAllowStatusWithBlankName()
    {
        testEditValidationFail("  \t\n", null, "http://test.com", statusCategory, "name");
        testAddValidationFail("  \t\n", null, "http://test.com", statusCategory, "name");
    }

    @Test
    public void shouldNotAllowStatusWithLongName()
    {
        //status names cannot be longer than 60 characters.
        final String statusNameSixtyOneCharacters = "This is a long name.This is a long name.This is a long name.X";
        testEditValidationFail(statusNameSixtyOneCharacters, null, "http://test.com", statusCategory, "name");
        testAddValidationFail(statusNameSixtyOneCharacters, null, "http://test.com", statusCategory, "name");
    }

    @Test
    public void shouldNotAllowStatusWithoutIconUrl()
    {
        testEditValidationFail("NewName", null, "  \t", statusCategory, "iconurl");
        testAddValidationFail("NewName", null, "  \t", statusCategory, "iconurl");
    }

    @Test
    public void shouldNotAllowEditStatusWithoutGivingStatusObject()
    {
        //clearing current status
        status = null;
        testEditValidationFail("MyName", null, "http://test.com", statusCategory);
    }

    @Test
    public void shouldNotAllowDuplicateName()
    {
        MockStatus existingStatus = new MockStatus("3", "MyExistingStatus", statusCategory);
        when(constantsManager.getStatusByNameIgnoreCase("MyExistingStatus")).thenReturn(existingStatus);

        testEditValidationFail("MyExistingStatus", null, "http://test.com", statusCategory, "name");
        testAddValidationFail("MyExistingStatus", null, "http://test.com", statusCategory, "name");
    }

    @Test
    public void shouldReturnBothErrorsWhenNoNameAndIconGive()
    {
        testEditValidationFail(" ", null, " ", statusCategory, "name", "iconurl");
        testAddValidationFail(" ", null, " ", statusCategory, "name", "iconurl");
    }


    @Test
    public void shouldTrimAllStringsBeforeEdition()
    {
        when(statusCategory.getKey()).thenReturn("status category key");
        defaultStatusService.editStatus(user, status, "  MyName  ", "  Description  ", "\thttp://text.com", statusCategory);
        verify(statusManager).editStatus(status, "MyName", "Description", "http://text.com", statusCategory);
    }

    @Test
    public void shouldTrimAllStringsBeforeAddition()
    {
        defaultStatusService.createStatus(user, "  MyName  ", "  Description  ", "\thttp://text.com", statusCategory);
        verify(statusManager).createStatus("MyName", "Description", "http://text.com", statusCategory);
    }

    @Test
    public void shouldNotAllowToRemoveAssociatedStatus()
    {
        JiraWorkflow wf1 = mock(JiraWorkflow.class);
        when(wf1.getLinkedStatusObjects()).thenReturn(ImmutableList.<Status>of(new MockStatus("1", "stat1"), new MockStatus("2", "stat2")));
        JiraWorkflow wf2 = mock(JiraWorkflow.class);
        when(wf2.getLinkedStatusObjects()).thenReturn(ImmutableList.<Status>of(new MockStatus("1", "stat1"), status));
        when(workflowManager.getWorkflowsIncludingDrafts()).thenReturn(ImmutableList.of(wf1, wf2));

        when(statusManager.getStatus(status.getId())).thenReturn(status);

        testRemoveFail("admin.error.status.is.associated.with.workflow");
    }


    @Test
    public void shouldNotAllowToRemoveNonExistingStatus()
    {
        when(statusManager.getStatus(status.getId())).thenReturn(new MockStatus("1", "strangeStatus"));
        testRemoveFail("admin.error.given.status.does.not.exist");
    }

    @Test
    public void shouldDelegateToManagerRemovalWhenEverythingIsOK()
    {
        when(statusManager.getStatus(status.getId())).thenReturn(status);
        when(workflowManager.getWorkflowsIncludingDrafts()).thenReturn(ImmutableList.<JiraWorkflow>of());

        final ServiceResult validationResult = defaultStatusService.validateRemoveStatus(user, status);

        assertTrue(validationResult.isValid());

        final ServiceResult removalResult = defaultStatusService.removeStatus(user, status);
        assertTrue(removalResult.isValid());
        verify(statusManager).removeStatus(status.getId());

    }

    private ErrorCollection testEditValidationFail(final String name, final String description, final String iconUrl, final StatusCategory statusCategory, final String... failingParameterNames)
    {
        //check validation result
        ServiceResult validationResult = defaultStatusService.validateEditStatus(user, this.status, name, description, iconUrl, statusCategory);
        assertFalse("Validation should be negative", validationResult.isValid());
        assertEquals(failingParameterNames.length, validationResult.getErrorCollection().getErrors().size());
        for (String failingParameterName : failingParameterNames)
        {
            assertTrue("Validation result should contain errors regarding " + failingParameterName, validationResult.getErrorCollection().getErrors().containsKey(failingParameterName));
        }

        ServiceOutcome<Status> updateResult = defaultStatusService.editStatus(user, this.status, name, description, iconUrl, statusCategory);
        assertFalse("Validation should be negative", updateResult.isValid());
        assertEquals("Errors collection from validation and edition should equal", validationResult.getErrorCollection(), updateResult.getErrorCollection());
        verify(statusManager, never()).editStatus(Mockito.<Status>any(), anyString(), anyString(), anyString(), Mockito.<StatusCategory>any());
        return updateResult.getErrorCollection();
    }

    private ErrorCollection testAddValidationFail(final String name, final String description, final String iconUrl, final StatusCategory statusCategory, final String... failingParameterNames)
    {
        //check validation result
        ServiceResult validationResult = defaultStatusService.validateCreateStatus(user, name, description, iconUrl, statusCategory);
        assertFalse("Validation should be negative", validationResult.isValid());
        assertEquals(failingParameterNames.length, validationResult.getErrorCollection().getErrors().size());
        for (String failingParameterName : failingParameterNames)
        {
            assertTrue("Validation result should contain errors regarding " + failingParameterName, validationResult.getErrorCollection().getErrors().containsKey(failingParameterName));
        }

        ServiceOutcome<Status> addResult = defaultStatusService.createStatus(user, name, description, iconUrl, statusCategory);
        assertFalse("Validation should be negative", addResult.isValid());
        assertEquals("Errors collection from validation and edition should equal", validationResult.getErrorCollection(), addResult.getErrorCollection());
        verify(statusManager, never()).editStatus(Mockito.<Status>any(), anyString(), anyString(), anyString(), Mockito.<StatusCategory>any());
        return addResult.getErrorCollection();
    }

    private void testRemoveFail(final String expectedError)
    {
        final ServiceResult validationResult = defaultStatusService.validateRemoveStatus(user, status);

        assertFalse(validationResult.isValid());
        assertThat(validationResult.getErrorCollection().getErrorMessages(), Matchers.contains(expectedError));


        final ServiceResult removalResult = defaultStatusService.removeStatus(user, status);
        assertEquals(validationResult.getErrorCollection(), removalResult.getErrorCollection());
    }

}
