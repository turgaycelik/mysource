package com.atlassian.jira.bc.issue.worklog;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.AnswerWith;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollectionAssert;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultWorklogService
{
    private static final String SHOULD_HAVE_EDIT_PERMISSIONS = "User should have edit permissions";
    private static final String SHOULD_HAVE_DELETE_PERMISSIONS = "User should have delete permissions";
    private static final String SHOULD_BE_SAME_AUTHOR = "Should be same author";
    private static final String SHOULD_HAVE_UPDATE_PERMISSIONS = "User should have update permissions";
    private static final String WORKLOG_SHOULD_BE_NULL = "Worklog should be null";
    private static final String RESULT_SHOULD_BE_NULL = "Result should be null";
    private static final String NO_ERRORS_EXPECTED = "No errors are expected";
    private static final String VALIDATION_ERROR_EXPECTED = "Validation error is expected";
    private static final String SHOULD_NOT_HAVE_CREATE_PERMISSIONS = "Should NOT have create permissions";
    private static final String SHOULD_NOT_HAVE_EDIT_PERMISSIONS = "Should NOT have edit permissions";
    private static final String SHOULD_NOT_HAVE_DELETE_PERMISSIONS = "Should NOT have delete permissions";
    private static final String SHOULD_NOT_BE_SAME_AUTHOR = "Should NOT be same author";
    private static final String SHOULD_NOT_HAVE_UPDATE_PERMISSIONS = "Should NOT have update permissions";
    private static final String NEW_ESTIMAGE_SHOULD_BE_NULL = "New estimate should be null";

    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private I18nHelper i18nHelper;

    @Mock
    @AvailableInContainer
    private UserManager userManager;

    @Mock
    private JiraDurationUtils jiraDurationUtils;

    @Mock
    private WorklogManager worklogManager;

    @Mock
    private Worklog worklog;

    @Mock
    private WorklogResult worklogResult;

    @Mock
    private Issue issue;

    @Mock
    private VisibilityValidator visibilityValidator;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private IssueManager issueManager;

    private ErrorCollection errorCollection;
    private JiraServiceContextImpl serviceContext;

    @Before
    public void setUp() throws Exception
    {
        errorCollection = new SimpleErrorCollection();
        serviceContext = new JiraServiceContextImpl((ApplicationUser) null, errorCollection, i18nHelper);

        when(i18nHelper.getText(anyString(), anyString())).thenAnswer(AnswerWith.firstParameter());
        when(i18nHelper.getText(anyString())).thenAnswer(AnswerWith.firstParameter());
    }

    @After
    public void tearDown() throws Exception
    {
        errorCollection = null;
        serviceContext = null;
    }

    @Test
    public void testValidateDeleteNoPermission()
    {
        when(worklogManager.getById(anyLong())).thenReturn(worklog);

        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null)
        {
            @Override
            public boolean hasPermissionToDelete(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return false;
            }
        };

        assertNull(RESULT_SHOULD_BE_NULL, worklogService.validateDelete(serviceContext, null));
        verify(worklogManager).getById(anyLong());
    }

    @Test
    public void testValidateDeleteWithNewEstimateNoPermission()
    {
        when(worklogManager.getById(anyLong())).thenReturn(worklog);

        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null)
        {
            @Override
            public boolean hasPermissionToDelete(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return false;
            }
        };

        assertNull(RESULT_SHOULD_BE_NULL, worklogService.validateDeleteWithNewEstimate(serviceContext, null, null));
        verify(worklogManager).getById(anyLong());
    }

    @Test
    public void testValidateDeleteWithNewEstimateInvalidNewEstimate()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean isValidNewEstimate(final JiraServiceContext jiraServiceContext, final String newEstimate, final String errorFieldPrefix)
            {
                return false;
            }

            @Override
            public WorklogResult validateDelete(final JiraServiceContext jiraServiceContext, final Long worklogId)
            {
                return worklogResult;
            }
        };

        assertNull(RESULT_SHOULD_BE_NULL, worklogService.validateDeleteWithNewEstimate(serviceContext, null, null));
    }

    @Test
    public void testValidateDeleteWithNewEstimateHappyPath()
    {
        when(worklogManager.getById(anyLong())).thenReturn(worklog);

        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean isValidCalled = new AtomicBoolean(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null)
        {
            @Override
            public boolean hasPermissionToDelete(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected boolean isValidNewEstimate(final JiraServiceContext jiraServiceContext, final String newEstimate, final String errorFieldPrefix)
            {
                isValidCalled.set(true);
                return true;
            }
        };

        assertEquals(worklog, worklogService.validateDeleteWithNewEstimate(serviceContext, null, null).getWorklog());
        assertTrue(methodNameMustBeCalledMessage("hasPermissionToDelete"), hasPermCalled.get());
        assertTrue(methodNameMustBeCalledMessage("isValidNewEstimate"), isValidCalled.get());
        assertFalse(NO_ERRORS_EXPECTED, errorCollection.hasAnyErrors());
        verify(worklogManager).getById(anyLong());
    }

    @Test
    public void testGetByIssueVisibleToUserGroupRestrictionNotVisible()
    {
        final AtomicBoolean isGroupCalled = new AtomicBoolean(false);
        final Worklog worklog = new WorklogImpl(null, null, 1234L, null, null, null, "testgroup", null, 1L);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public List getByIssue(final JiraServiceContext jiraServiceContext, final Issue issue)
            {
                return ImmutableList.of(worklog);
            }

            @Override
            protected boolean isUserInGroup(final ApplicationUser user, final String groupLevel)
            {
                isGroupCalled.set(true);
                return false;
            }
        };

        final List visibleIssues = worklogService.getByIssueVisibleToUser(serviceContext, null);
        assertTrue(methodNameMustBeCalledMessage("isUserInGroup"), isGroupCalled.get());
        assertEquals(0, visibleIssues.size());
    }

    @Test
    public void testGetByIssueVisibleToUserGroupRestrictionVisible()
    {
        final AtomicBoolean isGroupCalled = new AtomicBoolean(false);
        final Worklog worklog = new WorklogImpl(null, null, 1234L, null, null, null, "testgroup", null, 1L);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public List getByIssue(final JiraServiceContext jiraServiceContext, final Issue issue)
            {
                return ImmutableList.of(worklog);
            }

            @Override
            protected boolean isUserInGroup(final ApplicationUser user, final String groupLevel)
            {
                isGroupCalled.set(true);
                return true;
            }
        };

        final List visibleIssues = worklogService.getByIssueVisibleToUser(serviceContext, null);
        assertTrue(methodNameMustBeCalledMessage("isUserInGroup"), isGroupCalled.get());
        assertEquals(1, visibleIssues.size());
        assertEquals(worklog, visibleIssues.get(0));
    }

    @Test
    public void testGetByIssueVisibleToUserRoleRestrictionNotVisible()
    {
        final AtomicBoolean isInRoleCalled = new AtomicBoolean(false);
        final Worklog worklog = new WorklogImpl(null, null, 1234L, null, null, null, null, 1234L, 1L);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public List getByIssue(final JiraServiceContext jiraServiceContext, final Issue issue)
            {
                return ImmutableList.of(worklog);
            }

            @Override
            protected boolean isUserInRole(final Long roleLevel, final ApplicationUser user, final Issue issue)
            {
                isInRoleCalled.set(true);
                return false;
            }
        };

        final List visibleIssues = worklogService.getByIssueVisibleToUser(serviceContext, null);
        assertTrue(methodNameMustBeCalledMessage("isUserInRole"), isInRoleCalled.get());
        assertEquals(0, visibleIssues.size());
    }

    @Test
    public void testGetByIssueVisibleToUserRoleRestrictionVisible()
    {
        final AtomicBoolean isInRoleCalled = new AtomicBoolean(false);
        final Worklog worklog = new WorklogImpl(null, null, 1234L, null, null, null, null, 1234L, 1L);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public List getByIssue(final JiraServiceContext jiraServiceContext, final Issue issue)
            {
                return ImmutableList.of(worklog);
            }

            @Override
            protected boolean isUserInRole(final Long roleLevel, final ApplicationUser user, final Issue issue)
            {
                isInRoleCalled.set(true);
                return true;
            }
        };

        final List visibleIssues = worklogService.getByIssueVisibleToUser(serviceContext, null);
        assertTrue("isUserInRole", isInRoleCalled.get());
        assertEquals(1, visibleIssues.size());
        assertEquals(worklog, visibleIssues.get(0));
    }

    @Test
    public void testGetByIssueVisibleToUserNoRestriction()
    {
        final Worklog worklog = new WorklogImpl(null, null, 1234L, null, null, null, null, null, 1L);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public List getByIssue(final JiraServiceContext jiraServiceContext, final Issue issue)
            {
                return ImmutableList.of(worklog);
            }
        };

        final List visibleIssues = worklogService.getByIssueVisibleToUser(serviceContext, null);
        assertEquals(1, visibleIssues.size());
        assertEquals(worklog, visibleIssues.get(0));
    }

    @Test
    public void testValidateParamsAndCreateWorklog() throws Exception
    {
        final ApplicationUser testUser = createMockApplicationUser("test", "Test User", "test@atlassian.com");
        final Long timeSpentLong = 12345L;
        when(visibilityValidator.isValidVisibilityData(Matchers.<JiraServiceContext>anyObject(), anyString(),
                Matchers.<Issue>anyObject(), Mockito.<Visibility>any())).thenReturn(true);

        final AtomicBoolean isValidFieldsCalled = new AtomicBoolean(false);
        final AtomicBoolean getDurationCalled = new AtomicBoolean(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, visibilityValidator,
                null, null, null, null, null)
        {
            @Override
            protected boolean isValidWorklogInputFields(final JiraServiceContext jiraServiceContext, final Issue issue, final String timeSpent, final Date startDate, final String errorFieldPrefix)
            {
                isValidFieldsCalled.set(true);
                return true;
            }

            @Override
            protected long getDurationForFormattedString(final String timeSpent, final JiraServiceContext jiraServiceContext)
            {
                getDurationCalled.set(true);
                return timeSpentLong;
            }
        };

        final Date startDate = new Date();
        final String comment = "test comment";
        final String groupLevel = "testgroup";
        final Worklog worklog = worklogService.validateParamsAndCreateWorklog(serviceContext(testUser, errorCollection), null,
                testUser, Visibilities.groupVisibility(groupLevel), "2d", startDate, null, comment, null, null, null, null);
        assertNotNull(worklog);
        assertEquals(timeSpentLong, worklog.getTimeSpent());
        assertEquals(startDate, worklog.getStartDate());
        assertEquals(comment, worklog.getComment());
        assertEquals(groupLevel, worklog.getGroupLevel());
        assertEquals(testUser.getKey(), worklog.getAuthorKey());
        assertNull("The worklog role level should be null.", worklog.getRoleLevelId());
        assertTrue(methodNameMustBeCalledMessage("isValidWorklogInputFields"), isValidFieldsCalled.get());
        assertTrue(methodNameMustBeCalledMessage("getDurationForFormattedString"), getDurationCalled.get());
        verify(visibilityValidator).isValidVisibilityData(Matchers.<JiraServiceContext>anyObject(), anyString(), Matchers.<Issue>anyObject(), Mockito.<Visibility>any());
    }

    @Test
    public void testValidateCreateWithNewEstimateInvalidNewEstimate()
    {
        final AtomicBoolean isValidNewEstCalled = new AtomicBoolean(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean isValidNewEstimate(final JiraServiceContext jiraServiceContext, final String newEstimate, final String errorFieldPrefix)
            {
                isValidNewEstCalled.set(true);
                return false;
            }

            @Override
            public WorklogResult validateCreate(final JiraServiceContext jiraServiceContext, final WorklogInputParameters params)
            {
                return WorklogResultFactory.create(null);
            }
        };

        WorklogNewEstimateInputParameters params = WorklogInputParametersImpl.builder().buildNewEstimate();
        final WorklogResult worklogNewEstimateResult = worklogService.validateCreateWithNewEstimate(serviceContext, params);
        assertNull(RESULT_SHOULD_BE_NULL, worklogNewEstimateResult);
        assertTrue(methodNameMustBeCalledMessage("isValidNewEstimate"), isValidNewEstCalled.get());
    }

    @Test
    public void testValidateCreate()
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean validateAndCreateCalled = new AtomicBoolean(false);
        when(visibilityValidator.isValidVisibilityData(Mockito.<JiraServiceContext>anyObject(), anyString(),
                Mockito.<Issue>anyObject(), Mockito.<Visibility>any())).thenReturn(true);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, visibilityValidator, null, null, null, null, null)
        {
            @Override
            protected Worklog validateParamsAndCreateWorklog(final JiraServiceContext jiraServiceContext, final Issue issue,
                    final ApplicationUser author, final Visibility visibility, final String timeSpent, final Date startDate, final Long worklogId, final String comment, final Date created, final Date updated, final ApplicationUser updateAuthor, final String errorFieldPrefix)
            {
                validateAndCreateCalled.set(true);
                return null;
            }

            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                hasPermCalled.set(true);
                return true;
            }
        };

        final WorklogInputParameters params = WorklogInputParametersImpl.timeSpent("2d").build();
        worklogService.validateCreate(serviceContext(null, errorCollection), params);
        assertTrue(methodNameMustBeCalledMessage("hasPermissionToCreate"), hasPermCalled.get());
        assertTrue(methodNameMustBeCalledMessage("validateParamsAndCreateWorklog"), validateAndCreateCalled.get());
    }

    @Test
    public void testIsValidWorklogInputFieldsInvalidTimeSpent()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean isValidDuration(final String duration, final JiraServiceContext jiraServiceContext)
            {
                return false;
            }
        };
        assertFalse(VALIDATION_ERROR_EXPECTED, worklogService.isValidWorklogInputFields(serviceContext, null, "INVALID", new Date(), null));
        ErrorCollectionAssert.assertFieldError(errorCollection, "timeLogged", "worklog.service.error.invalid.time.duration");
    }

    @Test
    public void testIsValidWorklogInputFieldsInvalidTimeSpentZero()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean isValidDuration(final String duration, final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            @Override
            protected long getDurationForFormattedString(final String timeSpent, final JiraServiceContext jiraServiceContext)
            {
                return 0;
            }
        };
        assertFalse(VALIDATION_ERROR_EXPECTED, worklogService.isValidWorklogInputFields(serviceContext, null, "0", new Date(), null));
        ErrorCollectionAssert.assertFieldError(errorCollection, "timeLogged", "worklog.service.error.timespent.zero");
    }

    @Test
    public void testIsValidWorklogInputFieldsNullTimeSpentNullStartDate()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        assertFalse(VALIDATION_ERROR_EXPECTED, worklogService.isValidWorklogInputFields(serviceContext, null, null, null, null));
        ErrorCollectionAssert.assertFieldError(errorCollection, "timeLogged", "worklog.service.error.timespent.required");
        ErrorCollectionAssert.assertFieldError(errorCollection, "startDate", "worklog.service.error.invalid.worklog.date");
    }

    @Test
    public void testIsValidNewEstimate()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean isValidDuration(final String duration, final JiraServiceContext jiraServiceContext)
            {
                return false;
            }

            @Override
            boolean hasEditIssuePermission(final User user, final Issue issue)
            {
                return true;
            }
        };

        assertFalse(VALIDATION_ERROR_EXPECTED, worklogService.isValidNewEstimate(serviceContext, "Blah"));
        ErrorCollectionAssert.assertFieldError(errorCollection, "newEstimate", "worklog.service.error.newestimate");
    }

    @Test
    public void testReduceEstimateDoesNotGoBelowZero()
    {
        when(issue.getEstimate()).thenReturn(1000L);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        assertEquals(0L, (long) worklogService.reduceEstimate(issue, 1500L));
    }

    @Test
    public void testUpdateNullWorklog()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        assertNull(WORKLOG_SHOULD_BE_NULL, worklogService.update(serviceContext, null, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.worklog.null", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testUpdateNullWorklogIssue()
    {
        when(worklog.getIssue()).thenReturn(null);

        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        assertNull(WORKLOG_SHOULD_BE_NULL, worklogService.update(serviceContext, worklogResult, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.issue.null", errorCollection.getErrorMessages().iterator().next());
        verify(worklog).getIssue();
    }

    @Test
    public void testUpdate()
    {
        when(worklogManager.update(Matchers.<User>anyObject(), Matchers.<Worklog>anyObject(),
                Matchers.<Long>anyObject(), anyBoolean())).thenReturn(null);
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);

        final AtomicBoolean hasEditPermCalled = new AtomicBoolean(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null)
        {
            @Override
            public boolean hasPermissionToUpdate(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                hasEditPermCalled.set(true);
                return true;
            }
        };
        assertNull(WORKLOG_SHOULD_BE_NULL, worklogService.update(serviceContext, worklogResult, null, true));
        assertTrue(methodNameMustBeCalledMessage("hasEditPermCalled"), hasEditPermCalled.get());
        assertFalse(NO_ERRORS_EXPECTED, errorCollection.hasAnyErrors());
        verify(worklogManager).update(Matchers.<User>anyObject(), Matchers.<Worklog>anyObject(), Matchers.<Long>anyObject(), anyBoolean());
    }

    @Test
    public void testUpdateWithNewRemainingEstimate()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(issue.getId()).thenReturn(1L);

        final Long newEstimate = 12345L;
        when(worklogManager.update(Mockito.isNull(User.class), eq(worklog), eq(newEstimate), eq(true))).thenReturn(null);
        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null)
        {
            @Override
            public boolean hasPermissionToUpdate(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return true;
            }
        };

        worklogService.updateWithNewRemainingEstimate(serviceContext, WorklogResultFactory.createNewEstimate(worklog, newEstimate), true);
        verify(worklogManager).update(Mockito.isNull(User.class), eq(worklog), eq(newEstimate), eq(true));
    }

    @Test
    public void testUpdateAndRetainRemainingEstimate()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(issue.getId()).thenReturn(1L);
        when(worklogResult.getWorklog()).thenReturn(worklog);
        when(worklogManager.update(isNull(User.class), eq(worklog), isNull(Long.class), eq(true))).thenReturn(null);
        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null)
        {
            @Override
            public boolean hasPermissionToUpdate(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return true;
            }
        };

        worklogService.updateAndRetainRemainingEstimate(serviceContext, worklogResult, true);
        verify(worklogManager).update(isNull(User.class), eq(worklog), isNull(Long.class), eq(true));
    }

    @Test
    public void testUpdateAndAutoAdjustRemainingEstimate()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);

        final Long origTimeSpent = 2323L;
        Worklog originalWorklog = mock(Worklog.class);
        when(originalWorklog.getTimeSpent()).thenReturn(origTimeSpent);

        when(worklogManager.getById(anyLong())).thenReturn(originalWorklog);

        final Long newEstimate = 12345L;
        when(worklogManager.update(Mockito.isNull(User.class), eq(worklog), eq(newEstimate), eq(true))).thenReturn(null);

        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null)
        {
            @Override
            public boolean hasPermissionToUpdate(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return true;
            }

            @Override
            protected Long getAutoAdjustNewEstimateOnUpdate(final Issue issue, final Long newTimeSpent, final Long originalTimeSpent)
            {
                assertEquals(origTimeSpent, originalTimeSpent);
                return newEstimate;
            }

        };

        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        worklogService.updateAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
        verify(worklogManager).update(Mockito.<User>anyObject(), Matchers.<Worklog>anyObject(), anyLong(), anyBoolean());
        verify(worklog, times(3)).getIssue();
        verify(worklog, times(3)).getId();
        verify(originalWorklog).getTimeSpent();
    }

    @Test
    public void testReduceEstimate()
    {
        when(issue.getEstimate()).thenReturn(1000L);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        assertEquals(500L, (long) worklogService.reduceEstimate(issue, 500L));
    }

    @Test
    public void testGetAutoAdjustNewEstimateOnUpdateDoesNotGoBelowZero()
    {
        when(issue.getEstimate()).thenReturn(1000L);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        assertEquals(0L, (long) worklogService.getAutoAdjustNewEstimateOnUpdate(issue, 2500L, 1000L));
    }

    @Test
    public void testGetAutoAdjustNewEstimateOnUpdate()
    {
        when(issue.getEstimate()).thenReturn(1000L);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        assertEquals(1500L, (long) worklogService.getAutoAdjustNewEstimateOnUpdate(issue, 500L, 1000L));
    }

    @Test
    public void testCreateAndAutoAdjustRemainingEstimate()
    {
        when(worklog.getIssue()).thenReturn(issue);

        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        final Long newEstimate = 12345L;
        when(worklogManager.create(Matchers.<User>anyObject(), eq(worklog), eq(newEstimate), eq(true))).thenReturn(null);

        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null)
        {
            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                return true;
            }

            @Override
            protected Long reduceEstimate(final Issue issue, final Long timeSpent)
            {
                return newEstimate;
            }
        };

        worklogService.createAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
        verify(worklogManager).create(Matchers.<User>anyObject(), Matchers.<Worklog>anyObject(), Matchers.<Long>anyObject(), anyBoolean());
    }

    @Test
    public void testCreateAndRetainRemainingEstimate()
    {
        when(worklog.getIssue()).thenReturn(issue);
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);

        when(worklogManager.create(Matchers.<User>anyObject(), eq(worklog), isNull(Long.class), eq(true))).thenReturn(null);
        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null)
        {
            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                return true;
            }

            @Override
            protected boolean hasEditOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return super.hasEditOwnPermission(user, worklog);
            }
        };

        worklogService.createAndRetainRemainingEstimate(serviceContext, worklogResult, true);
        verify(worklogManager).create(Matchers.<User>anyObject(), Matchers.<Worklog>anyObject(), Matchers.<Long>anyObject(), anyBoolean());
    }

    @Test
    public void testCreateWithNewRemainingEstimate()
    {
        when(worklog.getIssue()).thenReturn(issue);
        final Long newEstimate = 12345L;
        final WorklogNewEstimateResult worklogResult = WorklogResultFactory.createNewEstimate(worklog, newEstimate);
        when(worklogManager.create(Matchers.<User>anyObject(), eq(worklog), eq(newEstimate), eq(true))).thenReturn(null);
        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null)
        {
            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                return true;
            }
        };

        worklogService.createWithNewRemainingEstimate(serviceContext, worklogResult, true);
        verify(worklogManager).create(Matchers.<User>anyObject(), Matchers.<Worklog>anyObject(), Matchers.<Long>anyObject(), anyBoolean());
    }

    @Test
    public void testCreateWithNullIssue()
    {
        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, 1000L);
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        worklogService.create(serviceContext, worklogResult, null, true);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.issue.null", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testCreateWithNullWorklog()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        final WorklogResult worklogResult = WorklogResultFactory.create(null);
        worklogService.create(serviceContext, worklogResult, null, true);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.worklog.null", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testHasPermissionToCreateWithUser() throws Exception
    {
        final ApplicationUser testUser = createMockApplicationUser("test", "Test User", "test@atlassian.com");

        when(permissionManager.hasPermission(eq(ProjectPermissions.WORK_ON_ISSUES), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_CREATE_PERMISSIONS, worklogService.hasPermissionToCreate(serviceContext(testUser, errorCollection), new MockIssue(), true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.no.permission",
                errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testHasPermissionToCreateNonEditableWorkflowState()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.WORK_ON_ISSUES), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(false);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return false;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_CREATE_PERMISSIONS, worklogService.hasPermissionToCreate(serviceContext, new MockIssue(), true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.issue.not.editable.workflow.state", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testHasPermissionToCreateWithNullUser()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.WORK_ON_ISSUES), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(false);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_CREATE_PERMISSIONS, worklogService.hasPermissionToCreate(serviceContext, new MockIssue(), true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.no.permission.no.user", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testHasEditOwnPermission()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.EDIT_OWN_WORKLOGS), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(true);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null)
        {
            @Override
            protected boolean isSameAuthor(final ApplicationUser user, final Worklog worklog)
            {
                return true;
            }
        };

        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, 123L);
        assertTrue(SHOULD_HAVE_EDIT_PERMISSIONS, worklogService.hasEditOwnPermission(null, worklog));
    }

    @Test
    public void testHasEditOwnPermissionUnauthorised()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.EDIT_OWN_WORKLOGS), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(false);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null)
        {
            @Override
            protected boolean isSameAuthor(final ApplicationUser user, final Worklog worklog)
            {
                return true;
            }
        };

        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, 123L);
        assertFalse(SHOULD_NOT_HAVE_EDIT_PERMISSIONS, worklogService.hasEditOwnPermission(null, worklog));
    }

    @Test
    public void testHasEditOwnPermissionNotSameAuthor()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.EDIT_OWN_WORKLOGS), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(true);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null)
        {
            @Override
            protected boolean isSameAuthor(final ApplicationUser user, final Worklog worklog)
            {
                return false;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_EDIT_PERMISSIONS, worklogService.hasEditOwnPermission(null, null));
    }

    @Test
    public void testHasEditAllPermission()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.EDIT_ALL_WORKLOGS), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(true);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null);

        assertTrue(SHOULD_HAVE_EDIT_PERMISSIONS, worklogService.hasEditAllPermission(null, null));
    }

    @Test
    public void testHasEditAllPermissionUnauthorised()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.EDIT_ALL_WORKLOGS), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(false);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null);

        assertFalse(SHOULD_NOT_HAVE_EDIT_PERMISSIONS, worklogService.hasEditAllPermission(null, null));
    }

    @Test
    public void testHasDeleteOwnPermission()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.DELETE_OWN_WORKLOGS), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(true);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null)
        {
            @Override
            protected boolean isSameAuthor(final ApplicationUser user, final Worklog worklog)
            {
                return true;
            }
        };

        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, 123L);
        assertTrue(SHOULD_HAVE_DELETE_PERMISSIONS, worklogService.hasDeleteOwnPermission(null, worklog));
    }

    @Test
    public void testHasDeleteOwnPermissionUnauthorised()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.DELETE_OWN_WORKLOGS), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(false);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null)
        {
            @Override
            protected boolean isSameAuthor(final ApplicationUser user, final Worklog worklog)
            {
                return true;
            }
        };

        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, 123L);
        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, worklogService.hasDeleteOwnPermission(null, worklog));
    }

    @Test
    public void testHasDeleteOwnPermissionNotSameAuthor()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.DELETE_OWN_WORKLOGS), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject()))
                .thenReturn(true);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null)
        {
            @Override
            protected boolean isSameAuthor(final ApplicationUser user, final Worklog worklog)
            {
                return false;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, worklogService.hasDeleteOwnPermission(null, null));
    }

    @Test
    public void testHasDeleteAllPermission()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.DELETE_ALL_WORKLOGS), Matchers.<Issue>anyObject(), Matchers.<ApplicationUser>anyObject()))
                .thenReturn(true);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null);

        assertTrue(SHOULD_HAVE_DELETE_PERMISSIONS, worklogService.hasDeleteAllPermission(null, null));
    }

    @Test
    public void testHasDeleteAllPermissionUnauthorised()
    {
        when(permissionManager.hasPermission(eq(ProjectPermissions.DELETE_ALL_WORKLOGS), Matchers.<Issue>anyObject(), Matchers.<ApplicationUser>anyObject()))
                .thenReturn(false);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, permissionManager, null,
                null, null, null, null, null);

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, worklogService.hasDeleteAllPermission(null, null));
    }

    @Test
    public void testIsSameAuthorSpecificAuthor()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final ApplicationUser user = createMockApplicationUser("test");
        when(userManager.getUserByKey(eq("test"))).thenReturn(user);

        final Worklog worklog = new WorklogImpl(null, null, null, user.getKey(), null, null, null, null, 123L);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        assertTrue(SHOULD_BE_SAME_AUTHOR, worklogService.isSameAuthor(user, worklog));
    }

    @Test
    public void testIsSameAuthorAnonymous()
    {
        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, 123L);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        assertTrue(SHOULD_BE_SAME_AUTHOR, worklogService.isSameAuthor(null, worklog));
    }

    @Test
    public void testIsSameAuthorDifferentAuthors()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final ApplicationUser user1 = createMockApplicationUser("jimmy");
        final ApplicationUser user2 = createMockApplicationUser("sally");
        final Worklog worklog = new WorklogImpl(null, null, null, user1.getKey(), null, null, null, null, 123L);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        assertFalse(SHOULD_NOT_BE_SAME_AUTHOR, worklogService.isSameAuthor(user2, worklog));
    }

    @Test
    public void testIsSameAuthorOneAnonymousAuthor()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final ApplicationUser user = createMockApplicationUser("test");
        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, 123L);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        assertFalse(SHOULD_NOT_BE_SAME_AUTHOR, worklogService.isSameAuthor(user, worklog));
    }

    @Test
    public void testHasPermissionToUpdateNullWorklog()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };
        assertFalse(SHOULD_NOT_BE_SAME_AUTHOR, defaultWorklogService.hasPermissionToUpdate(serviceContext, null));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.worklog.null", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testHasPermissionToUpdateNullIssueForWorklog()
    {
        when(worklog.getIssue()).thenReturn(null);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };
        assertFalse(SHOULD_NOT_HAVE_UPDATE_PERMISSIONS, defaultWorklogService.hasPermissionToUpdate(serviceContext, worklog));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.issue.null", errorCollection.getErrorMessages().iterator().next());

        verify(worklog).getIssue();
    }

    @Test
    public void testHasPermissionToUpdateNullWorklogId()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(null);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };
        assertFalse(SHOULD_NOT_HAVE_UPDATE_PERMISSIONS, defaultWorklogService.hasPermissionToUpdate(serviceContext, worklog));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.worklog.id.null", errorCollection.getErrorMessages().iterator().next());

        verify(worklog).getIssue();
        verify(worklog).getId();
    }

    @Test
    public void testHasPermissionToUpdateNoPermissionAnonymous()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean hasEditOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return false;
            }

            @Override
            protected boolean hasEditAllPermission(final ApplicationUser user, final Issue issue)
            {
                return false;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_UPDATE_PERMISSIONS, defaultWorklogService.hasPermissionToUpdate(serviceContext, worklog));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.no.edit.permission.no.user", errorCollection.getErrorMessages().iterator().next());

        verify(worklog, times(2)).getIssue();
        verify(worklog, times(1)).getId();
    }

    @Test
    public void testHasPermissionToUpdateNoPermissionSpecificUser() throws Exception
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean hasEditOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return false;
            }

            @Override
            protected boolean hasEditAllPermission(final ApplicationUser user, final Issue issue)
            {
                return false;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        final ApplicationUser user = createMockApplicationUser("test", "Tim Fullname", "yo@yo.net");
        serviceContext = serviceContext(user, errorCollection);

        assertFalse(SHOULD_NOT_HAVE_UPDATE_PERMISSIONS, defaultWorklogService.hasPermissionToUpdate(serviceContext, worklog));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.no.edit.permission", errorCollection.getErrorMessages().iterator().next());

        verify(worklog, times(2)).getIssue();
        verify(worklog, times(1)).getId();
    }

    @Test
    public void testHasPermissionToUpdateHappyPathEditOwn()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);

        when(visibilityValidator.isValidVisibilityData(Mockito.<JiraServiceContext>anyObject(), anyString(),
                Mockito.<Issue>anyObject(), Mockito.<Visibility>any())).thenReturn(true);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
                visibilityValidator, null, null, null, null, null)
        {
            @Override
            protected boolean hasEditOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return true;
            }

            @Override
            protected boolean hasEditAllPermission(final ApplicationUser user, final Issue issue)
            {
                return false;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertTrue(SHOULD_HAVE_UPDATE_PERMISSIONS, defaultWorklogService.hasPermissionToUpdate(serviceContext, worklog));
        assertFalse(NO_ERRORS_EXPECTED, errorCollection.hasAnyErrors());

        verify(worklog, times(3)).getIssue();
        verify(worklog, times(1)).getId();
    }

    @Test
    public void testHasPermissionToUpdateInvalidVisibilityData()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);

        when(visibilityValidator.isValidVisibilityData(Mockito.<JiraServiceContext>anyObject(), anyString(),
                Mockito.<Issue>anyObject(), Matchers.<Visibility>any())).thenReturn(false);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
                visibilityValidator, null, null, null, null, null)
        {
            @Override
            protected boolean hasEditOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return true;
            }

            @Override
            protected boolean hasEditAllPermission(final ApplicationUser user, final Issue issue)
            {
                return false;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_UPDATE_PERMISSIONS, defaultWorklogService.hasPermissionToUpdate(serviceContext, worklog));
        assertFalse(NO_ERRORS_EXPECTED, errorCollection.hasAnyErrors());

        verify(worklog, times(3)).getIssue();
        verify(worklog, times(1)).getId();
    }

    @Test
    public void testHasPermissionToUpdateHappyPathEditAll()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);

        when(visibilityValidator.isValidVisibilityData(Mockito.<JiraServiceContext>anyObject(), anyString(),
                Mockito.<Issue>anyObject(), Mockito.<Visibility>any())).thenReturn(true);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
                visibilityValidator, null, null, null, null, null)
        {
            @Override
            protected boolean hasEditOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return false;
            }

            @Override
            protected boolean hasEditAllPermission(final ApplicationUser user, final Issue issue)
            {
                return true;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertTrue(SHOULD_HAVE_UPDATE_PERMISSIONS, defaultWorklogService.hasPermissionToUpdate(serviceContext, worklog));
        assertFalse(NO_ERRORS_EXPECTED, errorCollection.hasAnyErrors());

        verify(worklog, times(3)).getIssue();
        verify(worklog, times(1)).getId();
    }

    @Test
    public void testUpdateAndAutoAdjustRemainingEstimateFailToFindOriginalWorklog()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);

        when(worklogManager.getById(anyLong())).thenReturn(null);

        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, null, null, null,
                null, null, null, null);

        assertNull(WORKLOG_SHOULD_BE_NULL, worklogService.updateAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.no.worklog.for.id", errorCollection.getErrorMessages().iterator().next());

        verify(worklogManager).getById(anyLong());
        verify(worklog).getIssue();
        verify(worklog, times(3)).getId();
    }

    @Test
    public void testValidateUpdateWithNewEstimate() throws Exception
    {
        final Date createdDate = new Date(1000);
        when(worklog.getCreated()).thenReturn(createdDate);
        when(worklog.getIssue()).thenReturn(issue);

        final ApplicationUser user = createMockApplicationUser("test", "Tim Fullname", "yo@yo.net");
        serviceContext = serviceContext(user, errorCollection);

        when(worklogManager.getById(anyLong())).thenReturn(worklog);
        when(jiraDurationUtils.parseDuration(anyString(), Mockito.<Locale>anyObject())).thenReturn(42L);
        when(issueManager.isEditable(eq(issue))).thenReturn(true);
        when(permissionManager.hasPermission(eq(ProjectPermissions.EDIT_ALL_WORKLOGS), Matchers.<Issue>anyObject(),
                Matchers.<ApplicationUser>anyObject())).thenReturn(true);
        when(visibilityValidator.isValidVisibilityData(Matchers.<JiraServiceContext>anyObject(), anyString(),
                Matchers.<Issue>anyObject(), Mockito.<Visibility>any())).thenReturn(true);

        final AtomicBoolean validateCalled = new AtomicBoolean(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService(worklogManager, permissionManager, visibilityValidator, null,
                issueManager, null, jiraDurationUtils, null)
        {
            @Override
            protected Worklog validateParamsAndCreateWorklog(final JiraServiceContext jiraServiceContext, final Issue issue, final ApplicationUser author,
                    final Visibility visibility, final String timeSpent, final Date startDate, final Long worklogId, final String comment,
                    final Date created, final Date updated, final ApplicationUser updateAuthor, final String errorFieldPrefix)
            {
                assertEquals(createdDate, created);
                assertTrue("Updated time must be greater then created time", updated.getTime() > createdDate.getTime());
                validateCalled.set(true);
                return null;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        WorklogNewEstimateInputParameters params = WorklogInputParametersImpl.builder().newEstimate("1124").buildNewEstimate();
        final WorklogNewEstimateResult worklogNewEstimateResult = worklogService.validateUpdateWithNewEstimate(serviceContext, params);

        verify(worklogManager).getById(anyLong());
        assertNull(RESULT_SHOULD_BE_NULL, worklogNewEstimateResult);
        assertTrue(methodNameMustBeCalledMessage("validateParamsAndCreateWorklog"), validateCalled.get());
    }

    @Test
    public void testValidateUpdateOrDeletePermissionCheckParamsNullWorklog()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        defaultWorklogService.validateUpdateOrDeletePermissionCheckParams(null, errorCollection, serviceContext);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.worklog.null", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateUpdateOrDeletePermissionCheckParamsNullIssue()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        when(worklog.getIssue()).thenReturn(null);

        defaultWorklogService.validateUpdateOrDeletePermissionCheckParams(worklog, errorCollection, serviceContext);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.issue.null", errorCollection.getErrorMessages().iterator().next());

        verify(worklog).getIssue();
    }

    @Test
    public void testValidateUpdateOrDeletePermissionCheckParamsNullWorklogId()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(null);

        defaultWorklogService.validateUpdateOrDeletePermissionCheckParams(worklog, errorCollection, serviceContext);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.worklog.id.null", errorCollection.getErrorMessages().iterator().next());

        verify(worklog).getIssue();
        verify(worklog).getId();
    }

    @Test
    public void testValidateUpdateOrDeletePermissionCheckParamsIssueInNonEditableWorkflowState()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return false;
            }
        };

        when(worklog.getIssue()).thenReturn(issue);

        defaultWorklogService.validateUpdateOrDeletePermissionCheckParams(worklog, errorCollection, serviceContext);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.issue.not.editable.workflow.state", errorCollection.getErrorMessages().iterator().next());

        verify(worklog).getIssue();
    }

    @Test
    public void testValidateUpdateOrDeletePermissionCheckParamsHappyPath()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);

        defaultWorklogService.validateUpdateOrDeletePermissionCheckParams(worklog, errorCollection, serviceContext);
        assertFalse(NO_ERRORS_EXPECTED, errorCollection.hasAnyErrors());

        verify(worklog).getIssue();
        verify(worklog).getId();
    }

    @Test
    public void testHasPermissionToDeleteNoPermissionAnonymous()
    {
        when(worklog.getIssue()).thenReturn(null);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            void validateUpdateOrDeletePermissionCheckParams(final Worklog worklog, final ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
            {}

            @Override
            protected boolean hasDeleteOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return false;
            }

            @Override
            protected boolean hasDeleteAllPermission(final ApplicationUser user, final Issue issue)
            {
                return false;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_UPDATE_PERMISSIONS, defaultWorklogService.hasPermissionToDelete(serviceContext, worklog));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.no.delete.permission.no.user", errorCollection.getErrorMessages().iterator().next());

        verify(worklog).getIssue();
    }

    @Test
    public void testHasPermissionToDeleteNoPermissionSpecificUser() throws Exception
    {
        when(worklog.getIssue()).thenReturn(null);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            void validateUpdateOrDeletePermissionCheckParams(final Worklog worklog, final ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
            {}

            @Override
            protected boolean hasDeleteOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return false;
            }

            @Override
            protected boolean hasDeleteAllPermission(final ApplicationUser user, final Issue issue)
            {
                return false;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        final ApplicationUser user = createMockApplicationUser("test", "Tim Fullname", "yo@yo.net");
        serviceContext = serviceContext(user, errorCollection);

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultWorklogService.hasPermissionToDelete(serviceContext, worklog));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.no.delete.permission",
                errorCollection.getErrorMessages().iterator().next());

        verify(worklog).getIssue();
    }

    @Test
    public void testHasPermissionToDeleteInvalidVisibilityData()
    {
        when(worklog.getIssue()).thenReturn(null);

        when(visibilityValidator.isValidVisibilityData(Matchers.<JiraServiceContext>anyObject(), anyString(),
                Matchers.<Issue>anyObject(), Matchers.<Visibility>any())).thenReturn(false);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, visibilityValidator, null, null, null, null, null)
        {
            @Override
            void validateUpdateOrDeletePermissionCheckParams(final Worklog worklog, final ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
            {}

            @Override
            protected boolean hasDeleteOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return true;
            }

            @Override
            protected boolean hasDeleteAllPermission(final ApplicationUser user, final Issue issue)
            {
                return false;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultWorklogService.hasPermissionToDelete(serviceContext, worklog));

        verify(visibilityValidator).isValidVisibilityData(Matchers.<JiraServiceContext>anyObject(), anyString(),
                Matchers.<Issue>anyObject(), Mockito.<Visibility>any());
        verify(worklog, times(2)).getIssue();
    }

    @Test
    public void testHasPermissionToDeleteHappyPathDeleteOwn()
    {
        when(worklog.getIssue()).thenReturn(null);

        when(visibilityValidator.isValidVisibilityData(Matchers.<JiraServiceContext>anyObject(), anyString(),
                Matchers.<Issue>anyObject(), Mockito.<Visibility>any())).thenReturn(true);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
                visibilityValidator, null, null, null, null, null)
        {
            @Override
            void validateUpdateOrDeletePermissionCheckParams(final Worklog worklog, final ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
            {}

            @Override
            protected boolean hasDeleteOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return true;
            }

            @Override
            protected boolean hasDeleteAllPermission(final ApplicationUser user, final Issue issue)
            {
                return false;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        assertTrue(SHOULD_HAVE_DELETE_PERMISSIONS, defaultWorklogService.hasPermissionToDelete(serviceContext, worklog));
        assertFalse(NO_ERRORS_EXPECTED, errorCollection.hasAnyErrors());

        verify(visibilityValidator).isValidVisibilityData(Matchers.<JiraServiceContext>anyObject(), anyString(),
                Matchers.<Issue>anyObject(), Mockito.<Visibility>any());
        verify(worklog, times(2)).getIssue();
    }

    @Test
    public void testHasPermissionToDeleteHappyPathDeleteAll()
    {
        when(worklog.getIssue()).thenReturn(null);

        when(visibilityValidator.isValidVisibilityData(Matchers.<JiraServiceContext>anyObject(), anyString(),
                Matchers.<Issue>anyObject(), Mockito.<Visibility>any())).thenReturn(true);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
                visibilityValidator, null, null, null, null, null)
        {
            @Override
            void validateUpdateOrDeletePermissionCheckParams(final Worklog worklog, final ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
            {}

            @Override
            protected boolean hasDeleteOwnPermission(final ApplicationUser user, final Worklog worklog)
            {
                return false;
            }

            @Override
            protected boolean hasDeleteAllPermission(final ApplicationUser user, final Issue issue)
            {
                return true;
            }

            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        assertTrue(SHOULD_HAVE_DELETE_PERMISSIONS, defaultWorklogService.hasPermissionToDelete(serviceContext, worklog));
        assertFalse(NO_ERRORS_EXPECTED, errorCollection.hasAnyErrors());

        verify(visibilityValidator).isValidVisibilityData(Matchers.<JiraServiceContext>anyObject(), anyString(),
                Matchers.<Issue>anyObject(), Mockito.<Visibility>any());
        verify(worklog, times(2)).getIssue();
    }

    @Test
    public void testHasPermissionToUpdateTimeTrackingDisabled()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return false;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_UPDATE_PERMISSIONS, defaultWorklogService.hasPermissionToUpdate(serviceContext, null));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.time.tracking.not.enabed",
                errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testHasPermissionToDeleteTimeTrackingDisabled()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return false;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultWorklogService.hasPermissionToDelete(serviceContext, null));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.time.tracking.not.enabed",
                errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testHasPermissionToCreateTimeTrackingDisabled()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return false;
            }
        };

        assertFalse(SHOULD_NOT_HAVE_CREATE_PERMISSIONS, defaultWorklogService.hasPermissionToCreate(serviceContext, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.time.tracking.not.enabed",
                errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testDeleteNullWorklog()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        assertFalse(defaultWorklogService.delete(serviceContext, null, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.worklog.null", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testDeleteNullIssue()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        when(worklog.getIssue()).thenReturn(null);
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultWorklogService.delete(serviceContext, worklogResult, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.issue.null", errorCollection.getErrorMessages().iterator().next());

        verify(worklog).getIssue();
    }

    @Test
    public void testDeleteNullWorklogId()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(null);
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultWorklogService.delete(serviceContext, worklogResult, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("worklog.service.error.worklog.id.null", errorCollection.getErrorMessages().iterator().next());

        verify(worklog).getIssue();
        verify(worklog).getId();
    }

    @Test
    public void testDeleteNoPermission()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            public boolean hasPermissionToDelete(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return false;
            }
        };

        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);

        assertFalse(SHOULD_NOT_HAVE_DELETE_PERMISSIONS, defaultWorklogService.delete(serviceContext, worklogResult, null, true));
        assertFalse(NO_ERRORS_EXPECTED, errorCollection.hasAnyErrors());

        verify(worklog).getIssue();
        verify(worklog).getId();
    }

    @Test
    public void testDeleteHappyPath()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);

        when(worklogManager.delete(Matchers.<User>anyObject(), eq(worklog), isNull(Long.class), eq(true))).thenReturn(true);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(worklogManager, null, null,
                null, null, null, null, null)
        {
            @Override
            public boolean hasPermissionToDelete(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return true;
            }
        };

        assertTrue(SHOULD_HAVE_DELETE_PERMISSIONS, defaultWorklogService.delete(serviceContext, worklogResult, null, true));
        assertFalse(NO_ERRORS_EXPECTED, errorCollection.hasAnyErrors());

        verify(worklogManager).delete(Matchers.<User>anyObject(), Matchers.<Worklog>anyObject(), anyLong(), anyBoolean());
        verify(worklog).getIssue();
        verify(worklog).getId();
    }

    @Test
    public void testIncreaseEstimate()
    {
        when(issue.getEstimate()).thenReturn(1000L);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        assertEquals(1500L, (long) defaultWorklogService.increaseEstimate(issue, 500L));
    }

    @Test
    public void testDeleteWithNewRemainingEstimate()
    {
        final Long newRemainingEstimate = 12345L;
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean delete(final JiraServiceContext jiraServiceContext, final WorklogResult worklogResult, final Long newEstimate, final boolean dispatchEvent)
            {
                assertEquals(newRemainingEstimate, newEstimate);
                return true;
            }
        };

        defaultWorklogService.deleteWithNewRemainingEstimate(serviceContext, WorklogResultFactory.createNewEstimate((Worklog) null, newRemainingEstimate),
                true);
    }

    @Test
    public void testDeleteAndRetainRemainingEstimate()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean delete(final JiraServiceContext jiraServiceContext, final WorklogResult worklogResult, final Long newEstimate, final boolean dispatchEvent)
            {
                assertNull(NEW_ESTIMAGE_SHOULD_BE_NULL, newEstimate);
                return true;
            }
        };

        defaultWorklogService.deleteAndRetainRemainingEstimate(serviceContext, null, true);
    }

    @Test
    public void testDeleteAndAutoAdjustRemainingEstimate()
    {
        when(worklog.getIssue()).thenReturn(issue);
        when(worklog.getId()).thenReturn(1L);
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);

        final AtomicBoolean deleteCalled = new AtomicBoolean(false);
        final Long autoEstimate = 12345L;
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            protected boolean delete(final JiraServiceContext jiraServiceContext, final WorklogResult worklogResult, final Long newEstimate, final boolean dispatchEvent)
            {
                assertEquals(autoEstimate, newEstimate);
                deleteCalled.set(true);
                return true;
            }

            @Override
            protected Long increaseEstimate(final Issue issue, final Long timeSpent)
            {
                return autoEstimate;
            }
        };

        assertTrue("Method deleteAndAutoAdjustRemainingEstimate should succeed", defaultWorklogService.deleteAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true));
        assertTrue(methodNameMustBeCalledMessage("delete"), deleteCalled.get());
    }

    @Test
    public void testIsValidNewEstimateNotSpecified()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            boolean hasEditIssuePermission(final User user, final Issue issue)
            {
                return true;
            }
        };

        assertFalse("Should not be valid estimate", defaultWorklogService.isValidNewEstimate(serviceContext, "")); //empty String fails TextUtils.stringset()
        ErrorCollectionAssert.assertFieldError(errorCollection, "newEstimate", "worklog.service.error.new.estimate.not.specified");
    }

    private JiraServiceContextImpl serviceContext(ApplicationUser user, ErrorCollection errorCollection)
    {
        return new JiraServiceContextImpl(user, errorCollection, i18nHelper);
    }

    private ApplicationUser createMockApplicationUser(final String userName)
    {
        return createMockApplicationUser(userName, userName, userName + "@example.com");
    }

    private ApplicationUser createMockApplicationUser(final String userName, final String name, final String email)
    {
        return new MockApplicationUser(userName, name, email);
    }

    private String methodNameMustBeCalledMessage(final String methodName)
    {
        return String.format("method '%s' must be called", methodName);
    }

}
