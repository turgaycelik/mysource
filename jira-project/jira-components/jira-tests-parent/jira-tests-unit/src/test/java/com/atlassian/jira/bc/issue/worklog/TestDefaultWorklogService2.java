package com.atlassian.jira.bc.issue.worklog;

import java.util.Date;
import java.util.Locale;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollectionAssert;
import com.atlassian.jira.util.JiraDurationUtils;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This TestCase unit tests DefaultWorklogService without using JiraMockTestCase.
 *
 * @since v3.13
 */
public class TestDefaultWorklogService2 extends MockControllerTestCase
{
    private static final long HOUR = 3600;
    //------------------------------------------------------------------------------------------------------------------
    //  validateCreateWithManuallyAdjustedEstimate
    //------------------------------------------------------------------------------------------------------------------

    @Test
    public void testValidateCreate_NoPermission()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                jiraServiceContext.getErrorCollection().addErrorMessage("You don't have permission to do this.");
                return false;
            }
        };

        WorklogInputParameters params = WorklogInputParametersImpl
                .issue(new MockIssue())
                .timeSpent("4h")
                .build();
        assertNull(defaultWorklogService.validateCreate(jiraServiceContext, params));
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "You don't have permission to do this.");
    }

    @Test
    public void testValidateCreate_HappyPath()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        final Worklog worklog = createMock(Worklog.class);
        final VisibilityValidator visibilityValidator = mock(VisibilityValidator.class);
        when(visibilityValidator.isValidVisibilityData(Mockito.<JiraServiceContext>any(), Mockito.<String>any(),
                Mockito.<Issue>any(), Mockito.<Visibility>any())).thenReturn(true);

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, visibilityValidator, null, null, null, null, null)
        {
            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                return true;
            }

            @Override
            protected Worklog validateParamsAndCreateWorklog(final JiraServiceContext jiraServiceContext, final Issue issue,
                    final ApplicationUser author, final Visibility visibility, final String timeSpent, final Date startDate, final Long worklogId, final String comment, final Date created, final Date updated, final ApplicationUser updateAuthor, final String errorFieldPrefix)
            {
                return worklog;
            }
        };

        WorklogInputParameters params = WorklogInputParametersImpl
                .issue(new MockIssue())
                .timeSpent("4h")
                .build();

        replay();

        final WorklogResult worklogResult = defaultWorklogService.validateCreate(jiraServiceContext, params);
        assertNotNull(worklogResult);
        assertEquals(worklog, worklogResult.getWorklog());
        assertTrue(worklogResult.isEditableCheckRequired());

        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateCreate_HappyPathEditableCheckNotRequired()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        final Worklog worklog = createMock(Worklog.class);
        final VisibilityValidator visibilityValidator = mock(VisibilityValidator.class);
        when(visibilityValidator.isValidVisibilityData(Mockito.<JiraServiceContext>any(), Mockito.<String>any(),
                Mockito.<Issue>any(), Mockito.<Visibility>any())).thenReturn(true);

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, visibilityValidator, null, null, null, null, null)
        {
            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                return true;
            }

            @Override
            protected Worklog validateParamsAndCreateWorklog(final JiraServiceContext jiraServiceContext, final Issue issue,
                    final ApplicationUser author, final Visibility visibility, final String timeSpent, final Date startDate, final Long worklogId, final String comment, final Date created, final Date updated, final ApplicationUser updateAuthor, final String errorFieldPrefix)
            {
                return worklog;
            }
        };

        WorklogInputParameters params = WorklogInputParametersImpl
                .issue(new MockIssue())
                .timeSpent("4h")
                .editableCheckRequired(false)
                .build();

        replay();

        final WorklogResult worklogResult = defaultWorklogService.validateCreate(jiraServiceContext, params);
        assertNotNull(worklogResult);
        assertEquals(worklog, worklogResult.getWorklog());
        assertFalse(worklogResult.isEditableCheckRequired());

        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateCreateWithManuallyAdjustedEstimate_NoPermission() throws InvalidDurationException
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        JiraDurationUtils jiraDurationUtils = createMock(JiraDurationUtils.class);
        expect(jiraDurationUtils.parseDuration("2h", Locale.ENGLISH)).andReturn(10L);
        replay(jiraDurationUtils);

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, jiraDurationUtils, null)
        {
            public WorklogResult validateCreate(final JiraServiceContext jiraServiceContext, final WorklogInputParameters params)
            {
                jiraServiceContext.getErrorCollection().addErrorMessage("You don't have permission to do this.");
                return null;
            }
        };

        WorklogAdjustmentAmountInputParameters params = WorklogInputParametersImpl
                .issue(new MockIssue())
                .timeSpent("4h")
                .adjustmentAmount("2h")
                .buildAdjustmentAmount();
        WorklogResult result = defaultWorklogService.validateCreateWithManuallyAdjustedEstimate(jiraServiceContext, params);

        // Now we expect to have failed because of the permission failure
        assertNull(result);
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "You don't have permission to do this.");
    }

    @Test
    public void testValidateCreateWithManuallyAdjustedEstimate_NullAdjustmentAmount()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            public WorklogResult validateCreate(final JiraServiceContext jiraServiceContext, final WorklogInputParameters params)
            {
                return WorklogResultFactory.createAdjustmentAmount(new WorklogImpl(null, null, null, null, null, new Date(), null, null, 3600L), null);
            }
        };

        WorklogAdjustmentAmountInputParameters params = WorklogInputParametersImpl
                .issue(new MockIssue())
                .timeSpent("4h")
                .buildAdjustmentAmount();
        WorklogResult result = defaultWorklogService.validateCreateWithManuallyAdjustedEstimate(jiraServiceContext, params);

        // Now we expect to have failed because of the missing amount
        assertNull(result);
        ErrorCollectionAssert.assertFieldError(jiraServiceContext.getErrorCollection(), "adjustmentAmount", "You must supply a valid amount of time to adjust the estimate by.");
    }

    @Test
    public void testValidateCreateWithManuallyAdjustedEstimate_EmptyAdjustmentAmount()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            public WorklogResult validateCreate(final JiraServiceContext jiraServiceContext, final WorklogInputParameters params)
            {
                return WorklogResultFactory.createAdjustmentAmount(new WorklogImpl(null, null, null, null, null, new Date(), null, null, 3600L), null);
            }
        };

        WorklogAdjustmentAmountInputParameters params = WorklogInputParametersImpl
                .issue(new MockIssue())
                .timeSpent("4h")
                .adjustmentAmount("")
                .buildAdjustmentAmount();
        WorklogResult result = defaultWorklogService.validateCreateWithManuallyAdjustedEstimate(jiraServiceContext, params);

        // Now we expect to have failed because of the missing amount
        assertNull(result);
        ErrorCollectionAssert.assertFieldError(jiraServiceContext.getErrorCollection(), "adjustmentAmount", "You must supply a valid amount of time to adjust the estimate by.");
    }

    @Test
    public void testValidateCreateWithManuallyAdjustedEstimate_NullWorklogResult() throws InvalidDurationException
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        JiraDurationUtils jiraDurationUtils = createMock(JiraDurationUtils.class);
        expect(jiraDurationUtils.parseDuration("2h", Locale.ENGLISH)).andReturn(10L);
        replay(jiraDurationUtils);

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, jiraDurationUtils, null)
        {
            public WorklogResult validateCreate(final JiraServiceContext jiraServiceContext, final WorklogInputParameters params)
            {
                // as this validate returns null, so too will the call to validateCreateWithManuallyAdjustedEstimate
                return null;
            }
        };

        WorklogAdjustmentAmountInputParameters params = WorklogInputParametersImpl
                .issue(new MockIssue())
                .adjustmentAmount("2h")
                .buildAdjustmentAmount();

        assertNull(defaultWorklogService.validateCreateWithManuallyAdjustedEstimate(jiraServiceContext, params));
    }

    @Test
    public void testValidateCreateWithManuallyAdjustedEstimate_BadAdjustmentAmount() throws InvalidDurationException
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        JiraDurationUtils jiraDurationUtils = createMock(JiraDurationUtils.class);
        expect(jiraDurationUtils.parseDuration("two hours", Locale.ENGLISH)).andThrow(new InvalidDurationException());
        replay(jiraDurationUtils);

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, jiraDurationUtils, null)
        {
            public WorklogResult validateCreate(final JiraServiceContext jiraServiceContext, final WorklogInputParameters params)
            {
                return WorklogResultFactory.createAdjustmentAmount(new WorklogImpl(null, null, null, null, null, new Date(), null, null, 3600L), null);
            }
        };

        WorklogAdjustmentAmountInputParameters params = WorklogInputParametersImpl
                .issue(new MockIssue())
                .timeSpent("4h")
                .adjustmentAmount("two hours")
                .buildAdjustmentAmount();
        WorklogResult result = defaultWorklogService.validateCreateWithManuallyAdjustedEstimate(jiraServiceContext, params);

        // Now we expect to have failed because of the not parsable amount
        assertNull(result);
        ErrorCollectionAssert.assertFieldError(jiraServiceContext.getErrorCollection(), "adjustmentAmount", "Invalid time entered for adjusting the estimate.");
    }

    @Test
    public void testValidateCreateWithManuallyAdjustedEstimate_HappyPath() throws InvalidDurationException
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        JiraDurationUtils jiraDurationUtils = createMock(JiraDurationUtils.class);
        expect(jiraDurationUtils.parseDuration("2h", Locale.ENGLISH)).andReturn(7200L).times(2);
        replay(jiraDurationUtils);

        MockApplicationProperties applicationProperites = new MockApplicationProperties();
        applicationProperites.setString("jira.timetracking.hours.per.day", "8");
        applicationProperites.setString("jira.timetracking.days.per.week", "5");
        applicationProperites.setString("jira.timetracking.default.unit", "minute");

        final TimeTrackingConfiguration trackingConfiguration = new TimeTrackingConfiguration.PropertiesAdaptor(applicationProperites);

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, trackingConfiguration, jiraDurationUtils, null)
        {
            public WorklogResult validateCreate(final JiraServiceContext jiraServiceContext, final WorklogInputParameters params)
            {
                return WorklogResultFactory.createAdjustmentAmount(new WorklogImpl(null, null, null, null, null, new Date(), null, null, 3600L), null);
            }
        };

        WorklogAdjustmentAmountInputParameters params = WorklogInputParametersImpl
                .issue(new MockIssue())
                .timeSpent("4h")
                .adjustmentAmount("2h")
                .buildAdjustmentAmount();
        WorklogAdjustmentAmountResult result = defaultWorklogService.validateCreateWithManuallyAdjustedEstimate(jiraServiceContext, params);

        // Now we expect to have passed.
        assertNotNull(result.getWorklog());
        assertEquals(7200, result.getAdjustmentAmount().longValue());
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testCreateAndAutoAdjustRemainingEstimate_NullWorklogResult()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        assertNull(defaultWorklogService.createAndAutoAdjustRemainingEstimate(jiraServiceContext, null, false));
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "Worklog must not be null.");
    }

    @Test
    public void testCreateAndAutoAdjustRemainingEstimate_NullWorklog()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        WorklogResult result = WorklogResultFactory.create(null);

        assertNull(defaultWorklogService.createAndAutoAdjustRemainingEstimate(jiraServiceContext, result, false));
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "Worklog must not be null.");
    }

    @Test
    public void testCreateAndAutoAdjustRemainingEstimate_NullIssue()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);

        Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, 7200L);
        WorklogResult result = WorklogResultFactory.create(worklog);

        assertNull(defaultWorklogService.createAndAutoAdjustRemainingEstimate(jiraServiceContext, result, false));
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "Can not modify a worklog without an issue specified.");
    }

    @Test
    public void testCreateAndAutoAdjustRemainingEstimate_NoPermission()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                jiraServiceContext.getErrorCollection().addErrorMessage("You don't have permission to do this.");
                return false;
            }
        };
        final MockIssue issue = new MockIssue();
        // current estimate is 10h
        issue.setEstimate(new Long(36000));
        // We have done 2 hours
        Worklog worklog = new WorklogImpl(null, issue, null, null, null, null, null, null, 7200L);

        WorklogResult result = WorklogResultFactory.create(worklog);
        assertNull(defaultWorklogService.createAndAutoAdjustRemainingEstimate(jiraServiceContext, result, false));
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "You don't have permission to do this.");
    }

    //------------------------------------------------------------------------------------------------------------------
    //  createWithManuallyAdjustedEstimate
    //------------------------------------------------------------------------------------------------------------------

    @Test
    public void testCreateWithManuallyAdjustedEstimate()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        final MutableBoolean me = new MutableBoolean();

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            // Create is public and should be tested elsewhere.
            protected Worklog create(final JiraServiceContext jiraServiceContext, final WorklogResult worklogResult, final Long newEstimate, final boolean dispatchEvent)
            {
                // Check that the new Estimate is 4h
                if (newEstimate != 4 * 3600)
                {
                    fail("Tried to create Worklog with wrong new Estimate.");
                }
                me.called = true;
                return worklogResult.getWorklog();
            }
        };
        final MockIssue issue = new MockIssue();
        // current estimate is 10h
        issue.setEstimate(new Long(36000));
        // We have done 2 hours
        Worklog worklog = new WorklogImpl(null, issue, null, null, null, null, null, null, 7200L);
        // but ask to reduce by 6h
        WorklogAdjustmentAmountResult result = WorklogResultFactory.createAdjustmentAmount(worklog, 6 * HOUR);
        defaultWorklogService.createWithManuallyAdjustedEstimate(jiraServiceContext, result, false);

        // Now we expect to have passed.
        //assertNotNull(worklog);
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        // Make sure we called our create.
        assertTrue(me.called);
    }

    /**
     * This test will test what happens when we ask to manually reduce the estimate but the current estimate is undefined (null).
     */
    @Test
    public void testCreateWithManuallyAdjustedEstimate_NoCurrentEstimate()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        final MutableBoolean me = new MutableBoolean();

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            // Create is public and should be tested elsewhere.
            protected Worklog create(final JiraServiceContext jiraServiceContext, final WorklogResult worklogResult, final Long newEstimate, final boolean dispatchEvent)
            {
                // Check that the new Estimate is 0h
                if (newEstimate != 0)
                {
                    fail("Tried to create Worklog with wrong new Estimate.");
                }
                me.called = true;
                return worklogResult.getWorklog();
            }
        };
        final MockIssue issue = new MockIssue();
        issue.setEstimate(null);
        // We have done 2 hours
        Worklog worklog = new WorklogImpl(null, issue, null, null, null, null, null, null, 7200L);
        // but ask to reduce by 6h
        WorklogAdjustmentAmountResult result = WorklogResultFactory.createAdjustmentAmount(worklog, 6 * HOUR);
        defaultWorklogService.createWithManuallyAdjustedEstimate(jiraServiceContext, result, false);

        // Now we expect to have passed.
        //assertNotNull(worklog);
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        // Make sure we called our create.
        assertTrue(me.called);
    }

    @Test
    public void testCreateWithManuallyAdjustedEstimate_NullIssue()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, 7200L);
        WorklogAdjustmentAmountResult result = WorklogResultFactory.createAdjustmentAmount(worklog, 6 * HOUR);
        assertNull(defaultWorklogService.createWithManuallyAdjustedEstimate(jiraServiceContext, result, false));
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testCreateWithManuallyAdjustedEstimate_NullWorklog()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        WorklogAdjustmentAmountResult result = WorklogResultFactory.createAdjustmentAmount((Worklog) null, 6 * HOUR);
        assertNull(defaultWorklogService.createWithManuallyAdjustedEstimate(jiraServiceContext, result, false));
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testCreateWithManuallyAdjustedEstimate_NullWorklogResult()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        assertNull(defaultWorklogService.createWithManuallyAdjustedEstimate(jiraServiceContext, null, false));
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    //------------------------------------------------------------------------------------------------------------------
    //  validateDeleteWithManuallyAdjustedEstimate
    //------------------------------------------------------------------------------------------------------------------

    @Test
    public void testValidateDeleteWithManuallyAdjustedEstimate_NoPermission()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            public WorklogResult validateDelete(JiraServiceContext jiraServiceContext, Long worklogId)
            {
                jiraServiceContext.getErrorCollection().addErrorMessage("You don't have permission to do this.");
                return null;
            }
        };
        WorklogResult result = defaultWorklogService.validateDeleteWithManuallyAdjustedEstimate(jiraServiceContext, null, "4h");

        // Now we expect to have failed because of the permission failure
        assertNull(result);
        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "You don't have permission to do this.");
    }

    @Test
    public void testValidateDeleteWithManuallyAdjustedEstimate_NullAdjustmentAmount()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            public WorklogResult validateDelete(JiraServiceContext jiraServiceContext, Long worklogId)
            {
                return WorklogResultFactory.create(new WorklogImpl(null, null, null, null, null, new Date(), null, null, 3600L));
            }
        };
        WorklogResult result = defaultWorklogService.validateDeleteWithManuallyAdjustedEstimate(jiraServiceContext, null, null);

        // Now we expect to have failed because of the missing amount
        assertNull(result);
        ErrorCollectionAssert.assertFieldError(jiraServiceContext.getErrorCollection(), "adjustmentAmount", "You must supply a valid amount of time to adjust the estimate by.");
    }

    @Test
    public void testValidateDeleteWithManuallyAdjustedEstimate_EmptyAdjustmentAmount()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            public WorklogResult validateDelete(JiraServiceContext jiraServiceContext, Long worklogId)
            {
                return WorklogResultFactory.create(new WorklogImpl(null, null, null, null, null, new Date(), null, null, 3600L));
            }
        };
        WorklogResult result = defaultWorklogService.validateDeleteWithManuallyAdjustedEstimate(jiraServiceContext, null, "");

        // Now we expect to have failed because of the missing amount
        assertNull(result);
        ErrorCollectionAssert.assertFieldError(jiraServiceContext.getErrorCollection(), "adjustmentAmount", "You must supply a valid amount of time to adjust the estimate by.");
    }

    @Test
    public void testValidateDeleteWithManuallyAdjustedEstimate_BadAdjustmentAmount() throws InvalidDurationException
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        JiraDurationUtils jiraDurationUtils = createMock(JiraDurationUtils.class);
        expect(jiraDurationUtils.parseDuration("I can't parse!", Locale.ENGLISH)).andThrow(new InvalidDurationException());
        replay(jiraDurationUtils);

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, jiraDurationUtils, null)
        {
            public WorklogResult validateDelete(JiraServiceContext jiraServiceContext, Long worklogId)
            {
                return WorklogResultFactory.create(new WorklogImpl(null, null, null, null, null, new Date(), null, null, 3600L));
            }
        };
        WorklogResult result = defaultWorklogService.validateDeleteWithManuallyAdjustedEstimate(jiraServiceContext, null, "I can't parse!");

        // Now we expect to have failed because of the bad amount
        assertNull(result);
        ErrorCollectionAssert.assertFieldError(jiraServiceContext.getErrorCollection(), "adjustmentAmount", "Invalid time entered for adjusting the estimate.");
    }

    @Test
    public void testValidateDeleteWithManuallyAdjustedEstimate_HappyPath() throws InvalidDurationException
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        JiraDurationUtils jiraDurationUtils = createMock(JiraDurationUtils.class);
        expect(jiraDurationUtils.parseDuration("4h", Locale.ENGLISH)).andReturn(14400L).times(2);
        replay(jiraDurationUtils);

        MockApplicationProperties applicationProperites = new MockApplicationProperties();
        applicationProperites.setString("jira.timetracking.hours.per.day", "8");
        applicationProperites.setString("jira.timetracking.days.per.week", "5");
        applicationProperites.setString("jira.timetracking.default.unit", "minute");

        final TimeTrackingConfiguration trackingConfiguration = new TimeTrackingConfiguration.PropertiesAdaptor(applicationProperites);

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, trackingConfiguration, jiraDurationUtils, null)
        {
            public WorklogResult validateDelete(JiraServiceContext jiraServiceContext, Long worklogId)
            {
                return WorklogResultFactory.create(new WorklogImpl(null, null, null, null, null, new Date(), null, null, 3600L));
            }
        };
        WorklogAdjustmentAmountResult result = defaultWorklogService.validateDeleteWithManuallyAdjustedEstimate(jiraServiceContext, null, "4h");

        // Now we expect to have passed.
        assertNotNull(result.getWorklog());
        assertEquals(4 * HOUR, result.getAdjustmentAmount().longValue());
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    //------------------------------------------------------------------------------------------------------------------
    //  deleteWithManuallyAdjustedEstimate
    //------------------------------------------------------------------------------------------------------------------

    @Test
    public void testDeleteWithManuallyAdjustedEstimate_HappyPath()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        final MutableBoolean me = new MutableBoolean();

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            // Create is public and should be tested elsewhere.
            protected boolean delete(final JiraServiceContext jiraServiceContext, final WorklogResult worklog, final Long newEstimate, final boolean dispatchEvent)
            {
                // Check that the new Estimate is 10h
                if (newEstimate != 10 * HOUR)
                {
                    fail("Tried to create Worklog with wrong new Estimate.");
                }
                me.called = true;
                return true;
            }
        };
        final MockIssue issue = new MockIssue();
        // current estimate is 4h
        issue.setEstimate(new Long(4 * HOUR));
        // Remove worklog where we did 2 hours
        Worklog worklog = new WorklogImpl(null, issue, null, null, null, null, null, null, 2 * HOUR);
        // but ask to increase by 6h
        defaultWorklogService.deleteWithManuallyAdjustedEstimate(jiraServiceContext, WorklogResultFactory.createAdjustmentAmount(worklog, 6 * HOUR), false);

        // Now we expect to have passed.
        //assertNotNull(worklog);
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        // Make sure we called our create.
        assertTrue(me.called);
    }

    /**
     * This test will test what happens when we ask to manually increase the estimate but the current estimate is undefined (null).
     */
    @Test
    public void testDeleteWithManuallyAdjustedEstimate_NoCurrentEstimate()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        final MutableBoolean me = new MutableBoolean();

        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null)
        {
            // Create is public and should be tested elsewhere.
            protected boolean delete(final JiraServiceContext jiraServiceContext, final WorklogResult worklog, final Long newEstimate, final boolean dispatchEvent)
            {
                // Check that the new Estimate is 6h
                if (newEstimate != 6 * HOUR)
                {
                    fail("Tried to create Worklog with wrong new Estimate.");
                }
                me.called = true;
                return true;
            }
        };
        final MockIssue issue = new MockIssue();
        issue.setEstimate(null);
        // Remove worklog where we did 2 hours
        Worklog worklog = new WorklogImpl(null, issue, null, null, null, null, null, null, 2 * HOUR);
        // but ask to increase by 6h
        defaultWorklogService.deleteWithManuallyAdjustedEstimate(jiraServiceContext, WorklogResultFactory.createAdjustmentAmount(worklog, 6 * HOUR), false);

        // Now we expect to have passed.
        //assertNotNull(worklog);
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
        // Make sure we called our create.
        assertTrue(me.called);
    }

    @Test
    public void testDeleteWithManuallyAdjustedEstimate_NullWorklogResult()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        assertFalse(defaultWorklogService.deleteWithManuallyAdjustedEstimate(jiraServiceContext, null, false));
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testDeleteWithManuallyAdjustedEstimate_NullWorklog()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        assertFalse(defaultWorklogService.deleteWithManuallyAdjustedEstimate(jiraServiceContext, WorklogResultFactory.createAdjustmentAmount((Worklog) null, null), false));
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testDeleteWithManuallyAdjustedEstimate_NullIssue()
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");
        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, 2 * HOUR);
        assertFalse(defaultWorklogService.deleteWithManuallyAdjustedEstimate(jiraServiceContext, WorklogResultFactory.createAdjustmentAmount(worklog, null), false));
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    //------------------------------------------------------------------------------------------------------------------
    //  Autoadjust
    //------------------------------------------------------------------------------------------------------------------
    @Test
    public void testReduceEstimate()
    {
        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        MockIssue mockIssue = new MockIssue();

        mockIssue.setEstimate(null);
        Long newEstimate = defaultWorklogService.reduceEstimate(mockIssue, 10L);
        assertEquals(new Long(0), newEstimate);

        mockIssue.setEstimate(new Long(40));
        newEstimate = defaultWorklogService.reduceEstimate(mockIssue, 10L);
        assertEquals(new Long(30), newEstimate);

        mockIssue.setEstimate(new Long(5));
        newEstimate = defaultWorklogService.reduceEstimate(mockIssue, 10L);
        assertEquals(new Long(0), newEstimate);
    }

    @Test
    public void testIncreaseEstimate()
    {
        DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null, null);
        MockIssue mockIssue = new MockIssue();

        mockIssue.setEstimate(null);
        long newEstimate = defaultWorklogService.increaseEstimate(mockIssue, 10L);
        assertEquals(10L, newEstimate);

        mockIssue.setEstimate(40L);
        newEstimate = defaultWorklogService.increaseEstimate(mockIssue, 10L);
        assertEquals(50L, newEstimate);

        // This is a bullshit situation that should not occur, but we have coded against it, so I test it.
        mockIssue.setEstimate(-25L);
        newEstimate = defaultWorklogService.increaseEstimate(mockIssue, 10L);
        assertEquals(0L, newEstimate);
    }

    @Test
    public void testHasPermissionToCreate_TimeTrackDisabled() throws Exception
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        MockIssue issue = new MockIssue();

        TimeTrackingConfiguration timeTrackingConfiguration = createMock(TimeTrackingConfiguration.class);
        expect(timeTrackingConfiguration.enabled()).andReturn(false);

        DefaultWorklogService defaultWorklogService = instantiate(DefaultWorklogService.class);

        assertFalse(defaultWorklogService.hasPermissionToCreate(jiraServiceContext, issue, true));

        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "Can not perform the requested operation, time tracking is disabled in JIRA.");
    }

    @Test
    public void testHasPermissionToCreate_IssueNull() throws Exception
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        TimeTrackingConfiguration timeTrackingConfiguration = createMock(TimeTrackingConfiguration.class);
        expect(timeTrackingConfiguration.enabled()).andReturn(true);

        DefaultWorklogService defaultWorklogService = instantiate(DefaultWorklogService.class);

        assertFalse(defaultWorklogService.hasPermissionToCreate(jiraServiceContext, null, true));

        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "Can not modify a worklog without an issue specified.");
    }

    @Test
    public void testHasPermissionToCreate_EditableCheckRequiredAndNotEditable() throws Exception
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        MockIssue issue = new MockIssue();

        TimeTrackingConfiguration timeTrackingConfiguration = createMock(TimeTrackingConfiguration.class);
        expect(timeTrackingConfiguration.enabled()).andReturn(true);

        IssueManager issueManager = createMock(IssueManager.class);
        expect(issueManager.isEditable(issue)).andReturn(false);

        DefaultWorklogService defaultWorklogService = instantiate(DefaultWorklogService.class);

        assertFalse(defaultWorklogService.hasPermissionToCreate(jiraServiceContext, issue, true));

        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "You can not edit the issue as it is in a non-editable workflow state.");
    }

    @Test
    public void testHasPermissionToCreate_EditableCheckNotRequiredAndNotEditable() throws Exception
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs");

        MockIssue issue = new MockIssue();

        TimeTrackingConfiguration timeTrackingConfiguration = createMock(TimeTrackingConfiguration.class);
        expect(timeTrackingConfiguration.enabled()).andReturn(true);

        PermissionManager permissionManager = createMock(PermissionManager.class);
        expect(permissionManager.hasPermission(ProjectPermissions.WORK_ON_ISSUES, issue, jiraServiceContext.getLoggedInApplicationUser())).andReturn(true);

        DefaultWorklogService defaultWorklogService = instantiate(DefaultWorklogService.class);

        assertTrue(defaultWorklogService.hasPermissionToCreate(jiraServiceContext, issue, false));

        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testHasPermissionToCreate_UserHasNoPermission() throws Exception
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext("wburroughs", "Willy B");

        MockIssue issue = new MockIssue();

        TimeTrackingConfiguration timeTrackingConfiguration = createMock(TimeTrackingConfiguration.class);
        expect(timeTrackingConfiguration.enabled()).andReturn(true);

        IssueManager issueManager = createMock(IssueManager.class);
        expect(issueManager.isEditable(issue)).andReturn(true);

        PermissionManager permissionManager = createMock(PermissionManager.class);
        expect(permissionManager.hasPermission(ProjectPermissions.WORK_ON_ISSUES, issue, jiraServiceContext.getLoggedInApplicationUser())).andReturn(false);

        DefaultWorklogService defaultWorklogService = instantiate(DefaultWorklogService.class);

        assertFalse(defaultWorklogService.hasPermissionToCreate(jiraServiceContext, issue, true));

        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "Willy B, you do not have the permission to associate a worklog to this issue.");
    }

    @Test
    public void testHasPermissionToCreate_AnonymousUserHasNoPermission() throws Exception
    {
        JiraServiceContext jiraServiceContext = new MockJiraServiceContext((User) null);

        MockIssue issue = new MockIssue();

        TimeTrackingConfiguration timeTrackingConfiguration = createMock(TimeTrackingConfiguration.class);
        expect(timeTrackingConfiguration.enabled()).andReturn(true);

        IssueManager issueManager = createMock(IssueManager.class);
        expect(issueManager.isEditable(issue)).andReturn(true);

        PermissionManager permissionManager = createMock(PermissionManager.class);
        expect(permissionManager.hasPermission(ProjectPermissions.WORK_ON_ISSUES, issue, jiraServiceContext.getLoggedInApplicationUser())).andReturn(false);

        DefaultWorklogService defaultWorklogService = instantiate(DefaultWorklogService.class);

        assertFalse(defaultWorklogService.hasPermissionToCreate(jiraServiceContext, issue, true));

        ErrorCollectionAssert.assert1ErrorMessage(jiraServiceContext.getErrorCollection(), "You do not have the permission to associate a worklog to this issue.");
    }

    //------------------------------------------------------------------------------------------------------------------
    //  Helpers
    //------------------------------------------------------------------------------------------------------------------

    private class MutableBoolean
    {
        public boolean called = false;
    }
}
