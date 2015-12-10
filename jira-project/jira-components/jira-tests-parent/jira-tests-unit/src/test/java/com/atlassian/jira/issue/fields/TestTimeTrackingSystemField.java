package com.atlassian.jira.issue.fields;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.TimeTrackingSystemField.TimeTrackingValue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.ErrorCollectionAssert.assert1FieldError;
import static com.atlassian.jira.util.ErrorCollectionAssert.assertFieldError;
import static com.atlassian.jira.util.ErrorCollectionAssert.assertNoErrors;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Responsible for holding unit tests for the TimeTrackingSystemField. This tests verify behaviour in modern mode.
 *
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestTimeTrackingSystemField
{
    @Mock private VelocityTemplatingEngine templatingEngine;
    @Mock private ApplicationProperties applicationProperties;
    @Mock private IssueManager issueManager;
    @Mock private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock private PermissionManager permissionManager;
    @Mock private JiraDurationUtils jiraDurationUtils;
    @Mock private OperationContext operationContext;
    @Mock private I18nHelper i18n;
    @Mock private FieldScreenRenderLayoutItem layoutItem;

    private MockIssue issue = new MockIssue();

    @After
    public void tearDown()
    {
        templatingEngine = null;
        applicationProperties = null;
        issueManager = null;
        jiraAuthenticationContext = null;
        permissionManager = null;
        jiraDurationUtils = null;
        operationContext = null;
        i18n = null;
        layoutItem = null;
        issue = null;
    }

    @Test
    public void testPopulatesDefaultsSetsANullValueForEstimates() throws Exception
    {
        whenTimeTrackingLegacyModeIsTurnedOff();

        issue.setOriginalEstimate(1000000L);
        issue.setEstimate(1000000L);

        final Map<String,Object> fieldsValueHolder = new FieldMap();
        final TimeTrackingSystemField timeTrackingField = fixture();
        timeTrackingField.populateDefaults(fieldsValueHolder, issue);

        assertContainsTimeTrackingValue(fieldsValueHolder);

        final TimeTrackingValue timeTrackingValue = (TimeTrackingValue)fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertThat("original", timeTrackingValue.getOriginalEstimateDisplayValue(), nullValue());
        assertThat("remaining", timeTrackingValue.getRemainingEstimateDisplayValue(), nullValue());
    }

    @Test
    public void testPopulateFromIssueAddsANullValueToTheFieldsValueHolderWhenTheEstimatesAreNull() throws Exception
    {
        whenTimeTrackingLegacyModeIsTurnedOff();

        final Map<String,Object> fieldsValueHolder = new FieldMap();
        final TimeTrackingSystemField timeTrackingField = fixture();
        timeTrackingField.populateFromIssue(fieldsValueHolder, issue);

        assertContainsTimeTrackingValue(fieldsValueHolder);

        final TimeTrackingValue timeTrackingValue = (TimeTrackingValue)fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertThat("original", timeTrackingValue.getOriginalEstimateDisplayValue(), nullValue());
        assertThat("remaining", timeTrackingValue.getRemainingEstimateDisplayValue(), nullValue());
    }

    @Test
    public void testPopulateFromIssueAddsDisplayFormattedEstimatesToTheFieldsValueHolder() throws Exception
    {
        final long originalEstimateIssueValue = 600000L;
        final String expectedOriginalEstimateFormattedValue = "10m";

        final long remainingEstimateIssueValue = 900000L;
        final String expectedRemainingEstimateFormattedValue = "15m";

        whenTimeTrackingLegacyModeIsTurnedOff();

        // Expect calls to format the raw estimates in the issue for display.
        when(jiraDurationUtils.getShortFormattedDuration(originalEstimateIssueValue, new Locale("en_UK"))).
                thenReturn(expectedOriginalEstimateFormattedValue);
        when(jiraDurationUtils.getShortFormattedDuration(remainingEstimateIssueValue, new Locale("en_UK"))).
                thenReturn(expectedRemainingEstimateFormattedValue);

        final Map<String,Object> fieldsValueHolder = new FieldMap();
        issue.setOriginalEstimate(originalEstimateIssueValue);
        issue.setEstimate(remainingEstimateIssueValue);

        final TimeTrackingSystemField timeTrackingField = fixture();
        timeTrackingField.populateFromIssue(fieldsValueHolder, issue);

        assertContainsTimeTrackingValue(fieldsValueHolder);

        final TimeTrackingValue timeTrackingValue = (TimeTrackingValue)fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertThat("original", timeTrackingValue.getOriginalEstimateDisplayValue(), is(expectedOriginalEstimateFormattedValue));
        assertThat("remaining", timeTrackingValue.getRemainingEstimateDisplayValue(), is(expectedRemainingEstimateFormattedValue));
    }

    @Test
    public void testValidateParamsAddsAnErrorIfTimeTrackingIsNotEnabled() throws Exception
    {
        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(new String[] { "1h" })
                .setRemainingEstimate(new String[] { "30m" })
                .build();
        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);
        final String expectedErrorMessage = "TIME TRACKING IS DISABLED OMG!!!";

        when(i18n.getText("createissue.error.timetracking.disabled")).thenReturn(expectedErrorMessage);
        when(operationContext.getFieldValuesHolder()).thenReturn(fieldsValueHolder);

        whenTimeTrackingIsDisabled();
        whenHasWorkStartedIsCalled();
        whenTimeTrackingIsARequiredField();

        final TimeTrackingSystemField timeTrackingField = fixture();
        final ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(operationContext, errorCollection, i18n, issue, layoutItem);
        assert1FieldError(errorCollection, IssueFieldConstants.TIMETRACKING, expectedErrorMessage);
    }

    @Test
    public void testValidateParamsDoesNotAddAnErrorIfBothEstimatesAreBlankAndTheFieldIsNotRequired() throws Exception
    {
        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(new String[] { "    " })
                .setRemainingEstimate(new String[] { "" })
                .build();
        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);

        when(operationContext.getFieldValuesHolder()).thenReturn(fieldsValueHolder);

        whenTimeTrackingIsEnabled();
        whenHasWorkStartedIsCalled();
        whenTimeTrackingIsNotARequiredField();

        final TimeTrackingSystemField timeTrackingField = fixture();
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        timeTrackingField.validateParams(operationContext, errorCollection, i18n, issue, layoutItem);

        assertNoErrors(errorCollection);
    }

    @Test
    public void testValidateParamsOnCreateAddsErrorIfBothEstimatesAreBlankAndTheFieldIsRequired() throws Exception
    {
        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setCreateIssue(new String[] { "true" })
                .setInLegacyMode(false)
                .setOriginalEstimate(new String[] { "    " })
                .setRemainingEstimate(new String[] { "" })
                .build();

        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);
        when(operationContext.getFieldValuesHolder()).thenReturn(fieldsValueHolder);

        whenTimeTrackingIsEnabled();
        whenHasWorkStartedIsCalled();
        whenTimeTrackingIsARequiredField();

        i18n = new MockI18nBean();
        final TimeTrackingSystemField timeTrackingField = fixture();
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        timeTrackingField.validateParams(operationContext, errorCollection, i18n, issue, layoutItem);

        assertFieldError(errorCollection, TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, "Original Estimate is required.");
        assertFieldError(errorCollection, TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE, "Remaining Estimate is required.");
    }

    @Test
    public void testValidateParamsOnCreateDoesntAddErrorIfOriginalEstimateIsBlankAndTheFieldIsRequired() throws Exception
    {
        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setCreateIssue(new String[] { "true" })
                .setInLegacyMode(false)
                .setOriginalEstimate(new String[] { "    " })
                .setRemainingEstimate(new String[] { "2h" })
                .build();
        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);

        when(operationContext.getFieldValuesHolder()).thenReturn(fieldsValueHolder);
        when(jiraAuthenticationContext.getLocale()).thenReturn(new Locale("en_UK"));
        when(jiraDurationUtils.parseDuration("2h", new Locale("en_UK"))).thenReturn(7200L);

        whenTimeTrackingIsEnabled();
        whenHasWorkStartedIsCalled();
        whenTimeTrackingIsARequiredField();

        final TimeTrackingSystemField timeTrackingField = fixture();
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        timeTrackingField.validateParams(operationContext, errorCollection, i18n, issue, layoutItem);

        assertNoErrors(errorCollection);

        // value of Remaining Estimate will be copied over to Original Estimate
        final TimeTrackingValue value = (TimeTrackingValue)fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertEquals("2h", value.getRemainingEstimateDisplayValue());
        assertEquals(value.getOriginalEstimateDisplayValue(), value.getRemainingEstimateDisplayValue());
    }

    @Test
    public void testValidateParamsOnCreateDoesntAddErrorIfRemainingEstimateIsBlankAndTheFieldIsRequired() throws Exception
    {
        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setCreateIssue(new String[] { "true" })
                .setInLegacyMode(false)
                .setOriginalEstimate(new String[] { "3h" })
                .setRemainingEstimate(new String[] { "" })
                .build();
        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);

        when(operationContext.getFieldValuesHolder()).thenReturn(fieldsValueHolder);
        when(jiraAuthenticationContext.getLocale()).thenReturn(new Locale("en_UK"));
        when(jiraDurationUtils.parseDuration("3h", new Locale("en_UK"))).thenReturn(10800L);

        whenTimeTrackingIsEnabled();
        whenHasWorkStartedIsCalled();
        whenTimeTrackingIsARequiredField();

        final TimeTrackingSystemField timeTrackingField = fixture();
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        timeTrackingField.validateParams(operationContext, errorCollection, i18n, issue, layoutItem);

        assertNoErrors(errorCollection);

        // value of Remaining Estimate will be copied over to Original Estimate
        final TimeTrackingValue value = (TimeTrackingValue)fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertEquals("3h", value.getRemainingEstimateDisplayValue());
        assertEquals(value.getOriginalEstimateDisplayValue(), value.getRemainingEstimateDisplayValue());
    }

    @Test
    public void testValidateParamsHappyFieldIsRequiredBothValueSupplied() throws Exception
    {
        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(new String[] { "3h" })
                .setRemainingEstimate(new String[] { "2h" })
                .build();
        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);

        when(operationContext.getFieldValuesHolder()).thenReturn(fieldsValueHolder);
        when(jiraAuthenticationContext.getLocale()).thenReturn(new Locale("en_UK"));
        when(jiraDurationUtils.parseDuration("3h", new Locale("en_UK"))).thenReturn(10800L);
        when(jiraDurationUtils.parseDuration("2h", new Locale("en_UK"))).thenReturn(7200L);

        whenTimeTrackingIsEnabled();
        whenHasWorkStartedIsCalled();
        whenTimeTrackingIsARequiredField();

        final TimeTrackingSystemField timeTrackingField = fixture();
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        timeTrackingField.validateParams(operationContext, errorCollection, i18n, issue, layoutItem);

        assertNoErrors(errorCollection);

        // value of Remaining Estimate will be copied over to Original Estimate
        final TimeTrackingValue value = (TimeTrackingValue) fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertEquals("2h", value.getRemainingEstimateDisplayValue());
        assertEquals("3h", value.getOriginalEstimateDisplayValue());
    }

    @Test
    public void testValidateParamsAddsErrorMessagesIfBothEstimatesAreBlankWhenTheFieldIsRequired() throws Exception
    {
        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(new String[] { "" })
                .setRemainingEstimate(new String[] { "   " })
                .build();
        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);
        final String expectedErrorMessage = "TIME TRACKING ESTIMATE IS BLANK OMG!!!";

        when(operationContext.getFieldValuesHolder()).thenReturn(fieldsValueHolder);

        whenTimeTrackingIsEnabled();

        when(i18n.getText("common.concepts.original.estimate")).thenReturn("ESTIMATE");
        when(i18n.getText("issue.field.required", "ESTIMATE")).thenReturn(expectedErrorMessage);
        when(i18n.getText("common.concepts.remaining.estimate")).thenReturn("ESTIMATE");
        when(i18n.getText("issue.field.required", "ESTIMATE")).thenReturn(expectedErrorMessage);

        whenHasWorkStartedIsCalled();
        whenTimeTrackingIsARequiredField();

        final TimeTrackingSystemField timeTrackingField = fixture();
        final ErrorCollection errorCollection = new SimpleErrorCollection();

        timeTrackingField.validateParams(operationContext, errorCollection, i18n, issue, layoutItem);
        assertThat(errorCollection.getErrorMessages(), hasSize(0));
        assertFieldError(errorCollection, TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, expectedErrorMessage);
        assertFieldError(errorCollection, TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE, expectedErrorMessage);
    }

    @Test
    public void testValidateParamsAddsAnErrorIfTheOriginalEstimateIsInvalid() throws Exception
    {
        final String originalEstimateInput = "30hours 15min. This input will be validated as incorrect.";
        final String originalEstimateExpectedErrorMessage = "The Original Estimate Input is Incorrect";
        final String remainingEstimateInput = "30h. This input will be validated as correct.";
        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(new String[] { originalEstimateInput })
                .setRemainingEstimate(new String[] { remainingEstimateInput })
                .build();
        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);

        when(operationContext.getFieldValuesHolder()).thenReturn(fieldsValueHolder);
        whenTimeTrackingIsEnabled();
        whenTimeTrackingIsNotARequiredField();

        when(i18n.getText("createissue.error.original.estimate.invalid")).thenReturn(originalEstimateExpectedErrorMessage);

        whenHasWorkStartedIsCalled();

        final TimeTrackingSystemField timeTrackingField = new TimeTrackingSystemField(templatingEngine,
                applicationProperties, issueManager, jiraAuthenticationContext, permissionManager,
                jiraDurationUtils)
        {
            @Override
            protected boolean isDurationInvalid(final String duration)
            {
                if (duration.equals(originalEstimateInput)) {return true;}
                if (duration.equals(remainingEstimateInput)) {return false;}
                throw new IllegalArgumentException("This method only expects to receive the following duration values: "
                        + "originalEstimateInput = " + originalEstimateInput + ", remainingEstimateInput = "
                        + remainingEstimateInput + '.' );
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        timeTrackingField.validateParams(operationContext, errorCollection, i18n, issue, layoutItem);

        assert1FieldError(errorCollection, TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, originalEstimateExpectedErrorMessage);
    }

    @Test
    public void testValidateParamsDoesNotAddAnErrorIfBothEstimatesAreValid() throws Exception
    {
        final String originalEstimateInput = "30hours 15min. This input will be validated as correct.";
        final String remainingEstimateInput = "30h. This input will be validated as correct.";
        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(new String[] { originalEstimateInput })
                .setRemainingEstimate(new String[] { remainingEstimateInput })
                .build();
        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);

        when(operationContext.getFieldValuesHolder()).thenReturn(fieldsValueHolder);

        whenTimeTrackingIsEnabled();
        whenHasWorkStartedIsCalled();
        whenTimeTrackingIsNotARequiredField();

        final TimeTrackingSystemField timeTrackingField = new TimeTrackingSystemField(templatingEngine,
                applicationProperties, issueManager, jiraAuthenticationContext, permissionManager,
                jiraDurationUtils)
        {
            @Override
            protected boolean isDurationInvalid(final String duration)
            {
                if (duration.equals(originalEstimateInput) || duration.equals(remainingEstimateInput)) {return false;}
                throw new IllegalArgumentException("This method only expects to receive the following duration values: "
                        + "originalEstimateInput = " + originalEstimateInput + ", remainingEstimateInput = "
                        + remainingEstimateInput + '.' );
            }
        };

        final ErrorCollection errorCollection = new SimpleErrorCollection();
        timeTrackingField.validateParams(operationContext, errorCollection, i18n, issue, layoutItem);
        assertNoErrors(errorCollection);
    }

    @Test
    public void testUpdateIssueSetsBothEstimatesWhenThereIsNoTargetSubField() throws Exception
    {
        FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);

        final String originalEstimateInput = "30";
        final String remainingEstimateInput = "20";
        final long originalEstimateInputInMillis = 180000L;
        final long remainingEstimateInputInMillis = 120000L;
        issue.setOriginalEstimate(60000L);
        issue.setEstimate(60000L);

        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(new String[] { originalEstimateInput })
                .setRemainingEstimate(new String[] { remainingEstimateInput })
                .build();

        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);

        when(jiraAuthenticationContext.getLocale()).thenReturn(new Locale("en_UK"));
        when(jiraDurationUtils.parseDuration(originalEstimateInput, new Locale("en_UK"))).thenReturn(originalEstimateInputInMillis);
        when(jiraDurationUtils.parseDuration(remainingEstimateInput, new Locale("en_UK"))).thenReturn(remainingEstimateInputInMillis);

        final TimeTrackingSystemField timeTrackingField = fixture();
        timeTrackingField.updateIssue(fieldLayoutItem, issue, fieldsValueHolder);

        assertEquals(issue.getOriginalEstimate(), new Long(originalEstimateInputInMillis));
        assertEquals(issue.getEstimate(), new Long(remainingEstimateInputInMillis));

        final Map<String, ModifiedValue> modifiedFields = issue.getModifiedFields();
        assertThat(modifiedFields, hasKey(IssueFieldConstants.TIMETRACKING));

        final ModifiedValue modifiedValue = modifiedFields.get(IssueFieldConstants.TIMETRACKING);
        final TimeTrackingValue expectedNewValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(originalEstimateInputInMillis)
                .setRemainingEstimate(remainingEstimateInputInMillis)
                .build();
        assertEquals(expectedNewValue, modifiedValue.getNewValue());

        final TimeTrackingValue expectedOldValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(60000L)
                .setRemainingEstimate(60000L)
                .build();
        assertEquals(expectedOldValue, modifiedValue.getOldValue());
    }

    @Test
    public void testUpdateIssueSetsOnlyTheRemainingEstimateIfTheTargetSubFieldIndicatesIt() throws Exception
    {
        final FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);
        final String originalEstimateInput = "30. This should not get set in the issue.";
        final String remainingEstimateInput = "20";
        final long remainingEstimateInputInMillis = 120000L;
        issue.setOriginalEstimate(60000L);
        issue.setEstimate(60000L);

        final TimeTrackingValue timeTrackingValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setTargetSubField(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE)
                .setOriginalEstimate(new String[] { originalEstimateInput })
                .setRemainingEstimate(new String[] { remainingEstimateInput })
                .build();
        final Map<String,Object> fieldsValueHolder = FieldMap.build(IssueFieldConstants.TIMETRACKING, timeTrackingValue);
        when(jiraAuthenticationContext.getLocale()).thenReturn(new Locale("en_UK"));
        when(jiraDurationUtils.parseDuration(remainingEstimateInput, new Locale("en_UK"))).thenReturn(remainingEstimateInputInMillis);

        final TimeTrackingSystemField timeTrackingField = fixture();
        timeTrackingField.updateIssue(fieldLayoutItem, issue, fieldsValueHolder);
        assertEquals(issue.getOriginalEstimate(), new Long(60000L));
        assertEquals(issue.getEstimate(), new Long(remainingEstimateInputInMillis));

        final Map<String, ModifiedValue> modifiedFields = issue.getModifiedFields();
        assertThat(modifiedFields, hasKey(IssueFieldConstants.TIMETRACKING));

        final ModifiedValue modifiedValue = modifiedFields.get(IssueFieldConstants.TIMETRACKING);
        final TimeTrackingValue expectedNewValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setTargetSubField(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE)
                .setRemainingEstimate(remainingEstimateInputInMillis)
                .build();
        assertEquals(expectedNewValue, modifiedValue.getNewValue());

        final TimeTrackingValue expectedOldValue = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setTargetSubField(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE)
                .setOriginalEstimate(60000L)
                .setRemainingEstimate(60000L)
                .build();
        assertEquals(expectedOldValue, modifiedValue.getOldValue());
    }

    @Test
    public void testUpdateValueDoesNotAddAChangeHistoryItemIfThePreviousAndNewValueOfTheOriginalEstimateAreEqual()
            throws Exception
    {
        final Long originalEstimateInputInMillis = 60000L;
        final Long remainingEstimateInputInMillis = 120000L;
        final Long originalEstimateInitialValue = originalEstimateInputInMillis;
        final Long remainingEstimateInitialValue = 60000L;
        final FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);
        final TimeTrackingValue timeTrackingValue1 = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(originalEstimateInitialValue)
                .setRemainingEstimate(remainingEstimateInitialValue)
                .build();
        final TimeTrackingValue timeTrackingValue2 = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(originalEstimateInputInMillis)
                .setRemainingEstimate(remainingEstimateInputInMillis)
                .build();
        final ModifiedValue modifiedTimeTrackingValue = new ModifiedValue(timeTrackingValue1, timeTrackingValue2);
        final DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();
        final TimeTrackingSystemField timeTrackingField = fixture();
        timeTrackingField.updateValue(fieldLayoutItem, issue, modifiedTimeTrackingValue, issueChangeHolder);

        final List<ChangeItemBean> changeItems = issueChangeHolder.getChangeItems();
        assertThat(changeItems, hasSize(1));
        assertThat(changeItems, hasItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE,
                remainingEstimateInitialValue.toString(), remainingEstimateInitialValue.toString(),
                remainingEstimateInputInMillis.toString(), remainingEstimateInputInMillis.toString())));
        assertThat(changeItems, not(hasItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE,
                originalEstimateInitialValue.toString(), originalEstimateInitialValue.toString(),
                originalEstimateInputInMillis.toString(), originalEstimateInputInMillis.toString()))));
    }

    @Test
    public void testUpdateValueDoesNotAddAChangeHistoryItemIfThePreviousAndNewValueOfTheOriginalEstimateAreNull()
            throws Exception
    {
        final Long originalEstimateInputInMillis = null;
        final Long remainingEstimateInputInMillis = 120000L;
        final Long originalEstimateInitialValue = originalEstimateInputInMillis;
        final Long remainingEstimateInitialValue = 60000L;
        final FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);
        final TimeTrackingValue timeTrackingValue1 = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(originalEstimateInitialValue)
                .setRemainingEstimate(remainingEstimateInitialValue)
                .build();
        final TimeTrackingValue timeTrackingValue2 = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(originalEstimateInputInMillis)
                .setRemainingEstimate(remainingEstimateInputInMillis)
                .build();
        final ModifiedValue modifiedTimeTrackingValue = new ModifiedValue(timeTrackingValue1, timeTrackingValue2);
        final DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();
        final TimeTrackingSystemField timeTrackingField = fixture();
        timeTrackingField.updateValue(fieldLayoutItem, issue, modifiedTimeTrackingValue, issueChangeHolder);

        final List<ChangeItemBean> changeItems = issueChangeHolder.getChangeItems();
        assertThat(changeItems, hasSize(1));
        assertThat(changeItems, not(hasItem(
                new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE,
                        null, null,
                        null, null)
        )));
        assertThat(changeItems, hasItem(
                new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE,
                        remainingEstimateInitialValue.toString(), remainingEstimateInitialValue.toString(),
                        remainingEstimateInputInMillis.toString(), remainingEstimateInputInMillis.toString())
        ));
    }

    @Test
    public void testUpdateValueAddsOneChangeHistoryItemWhenOriginalEstimateHasBeenSpecifiedAsTheTargetSubField()
            throws Exception
    {
        final Long originalEstimateInputInMillis = 300000L;
        final Long remainingEstimateInputInMillis = 120000L; // Should not cause the creation of a change item
        final Long originalEstimateInitialValue = 60000L;
        final Long remainingEstimateInitialValue = 60000L; // Should not cause the creation of a change item
        final FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);

        final TimeTrackingValue timeTrackingValue1 = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setTargetSubField(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE)
                .setOriginalEstimate(originalEstimateInitialValue)
                .setRemainingEstimate(remainingEstimateInitialValue)
                .build();
        final TimeTrackingValue timeTrackingValue2 = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setTargetSubField(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE)
                .setOriginalEstimate(originalEstimateInputInMillis)
                .setRemainingEstimate(remainingEstimateInputInMillis)
                .build();
        final ModifiedValue modifiedTimeTrackingValue = new ModifiedValue(timeTrackingValue1, timeTrackingValue2);
        final DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();
        final TimeTrackingSystemField timeTrackingField = fixture();
        timeTrackingField.updateValue(fieldLayoutItem, issue, modifiedTimeTrackingValue, issueChangeHolder);

        final List<ChangeItemBean> changeItems = issueChangeHolder.getChangeItems();
        assertThat(changeItems, hasSize(1));
        assertThat(changeItems, hasItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE,
                originalEstimateInitialValue.toString(), originalEstimateInitialValue.toString(),
                originalEstimateInputInMillis.toString(), originalEstimateInputInMillis.toString())));
        assertThat(changeItems, not(hasItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE,
                remainingEstimateInitialValue.toString(), remainingEstimateInitialValue.toString(),
                remainingEstimateInputInMillis.toString(), remainingEstimateInputInMillis.toString()))));
    }

    @Test
    public void testUpdateValueAddsTwoChangeHistoryItemsWhenBothEstimatesHaveChanged() throws Exception
    {
        final Long originalEstimateInputInMillis = 300000L;
        final Long remainingEstimateInputInMillis = 120000L; // Should not cause the creation of a change item
        final Long originalEstimateInitialValue = 60000L;
        final Long remainingEstimateInitialValue = 60000L; // Should not cause the creation of a change item
        final FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);
        final TimeTrackingValue timeTrackingValue1 = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(originalEstimateInitialValue)
                .setRemainingEstimate(remainingEstimateInitialValue)
                .build();
        final TimeTrackingValue timeTrackingValue2 = new TimeTrackingValue.Builder()
                .setInLegacyMode(false)
                .setOriginalEstimate(originalEstimateInputInMillis)
                .setRemainingEstimate(remainingEstimateInputInMillis)
                .build();
        final ModifiedValue modifiedTimeTrackingValue = new ModifiedValue(timeTrackingValue1, timeTrackingValue2);
        final DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();
        final TimeTrackingSystemField timeTrackingField = fixture();
        timeTrackingField.updateValue(fieldLayoutItem, issue, modifiedTimeTrackingValue, issueChangeHolder);

        final List<ChangeItemBean> changeItems = issueChangeHolder.getChangeItems();
        assertThat(changeItems, containsInAnyOrder(
                new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ORIGINAL_ESTIMATE,
                        originalEstimateInitialValue.toString(), originalEstimateInitialValue.toString(),
                        originalEstimateInputInMillis.toString(), originalEstimateInputInMillis.toString()),
                new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_ESTIMATE,
                        remainingEstimateInitialValue.toString(), remainingEstimateInitialValue.toString(),
                        remainingEstimateInputInMillis.toString(), remainingEstimateInputInMillis.toString())
        ));
    }

    @Test
    public void testIsShownReturnsFalseIfTimeTrackingIsDisabled() throws Exception
    {
        whenTimeTrackingIsDisabled();

        final TimeTrackingSystemField timeTrackingField = fixture();
        assertFalse("isShown", timeTrackingField.isShown(issue));
    }

    private void whenTimeTrackingIsEnabled()
    {
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).thenReturn(true);
    }

    private void whenTimeTrackingIsDisabled()
    {
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).thenReturn(false);
    }

    private void whenHasWorkStartedIsCalled() throws Exception
    {
        when(issueManager.getEntitiesByIssueObject("IssueWorklog", issue)).thenReturn(ImmutableList.<GenericValue>of());
    }

    private void whenTimeTrackingLegacyModeIsTurnedOff()
    {
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR)).thenReturn(false);
    }

    private void whenTimeTrackingIsARequiredField()
    {
        when(layoutItem.isRequired()).thenReturn(true);
    }

    private void whenTimeTrackingIsNotARequiredField()
    {
        when(layoutItem.isRequired()).thenReturn(false);
    }

    private static void assertContainsTimeTrackingValue(final Map<String,Object> fieldsValueHolder)
    {
        assertThat(fieldsValueHolder, hasKey(IssueFieldConstants.TIMETRACKING));

        final Object timeTrackingObjectValue = fieldsValueHolder.get(IssueFieldConstants.TIMETRACKING);
        assertThat(timeTrackingObjectValue, instanceOf(TimeTrackingValue.class));
    }


    TimeTrackingSystemField fixture()
    {
        return new TimeTrackingSystemField(templatingEngine, applicationProperties, issueManager,
                jiraAuthenticationContext, permissionManager, jiraDurationUtils);
    }
}
