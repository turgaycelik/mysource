package com.atlassian.jira.issue.label;

import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.bc.issue.label.DefaultLabelService;
import com.atlassian.jira.bc.issue.label.LabelService;
import com.atlassian.jira.bc.issue.label.LabelService.AddLabelValidationResult;
import com.atlassian.jira.bc.issue.label.LabelService.LabelsResult;
import com.atlassian.jira.bc.issue.label.LabelService.SetLabelValidationResult;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.MockCustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.ErrorCollectionAssert.assert1ErrorMessage;
import static com.atlassian.jira.util.ErrorCollectionAssert.assert1FieldError;
import static com.atlassian.jira.util.ErrorCollectionAssert.assertNoErrors;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @since v4.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultLabelService
{
    private static final long ISSUE_ID = 1L;
    private static final long CUSTOM_FIELD_ID = 2L;
    private static final Label LABEL = new Label(1L, ISSUE_ID, null, "label");
    private static final Set<Label> LABEL_SINGLETON = ImmutableSet.of(LABEL);
    private static final Set<String> LABEL_STRING_SINGLETON = ImmutableSet.of(LABEL.getLabel());
    private static final User USER = new MockUser("Fred");

    @Mock private LabelManager labelManager;
    @Mock private PermissionManager permissionManager;
    @Mock private CustomFieldManager customFieldManager;
    @Mock private FieldLayoutManager fieldLayoutManager;

    private MutableIssue issue;
    private MockIssueManager issueManager;
    private LabelService labelService;

    @Before
    public void setUp()
    {
        issue = new MockIssue(ISSUE_ID);
        issueManager = new MockIssueManager();
        issueManager.addIssue(issue);

        labelService = new DefaultLabelService(permissionManager, issueManager, labelManager,
                new NoopI18nFactory(), customFieldManager, fieldLayoutManager);
    }

    @After
    public void tearDown()
    {
        labelManager = null;
        permissionManager = null;
        customFieldManager = null;
        fieldLayoutManager = null;
        issue = null;
        issueManager = null;
        labelService = null;
    }

    @Test
    public void testGetSystemFieldLabels()
    {
        mockBrowsePermission();
        when(labelManager.getLabels(ISSUE_ID)).thenReturn(ImmutableSet.<Label>of());

        final LabelsResult result = labelService.getLabels(USER, ISSUE_ID);
        assertNoErrors(result.getErrorCollection());
        assertThat(result.getLabels(), hasSize(0));
    }

    @Test
    public void testGetCustomFieldLabels()
    {
        mockBrowsePermission();
        when(customFieldManager.getCustomFieldObject(CUSTOM_FIELD_ID)).thenReturn(new MockCustomField());
        when(labelManager.getLabels(ISSUE_ID, CUSTOM_FIELD_ID)).thenReturn(ImmutableSet.<Label>of());

        final LabelsResult result = labelService.getLabels(USER, ISSUE_ID, CUSTOM_FIELD_ID);
        assertNoErrors(result.getErrorCollection());
        assertThat(result.getLabels(), hasSize(0));
    }

    @Test
    public void testGetSystemFieldLabelsWithoutPermission()
    {
        final LabelsResult result = labelService.getLabels(USER, ISSUE_ID);
        assertNoBrowse(result);
        assertThat(result.getLabels(), hasSize(0));
    }

    @Test
    public void testValidateSetLabelsWithoutPermission()
    {
        issueManager.setEditable(false);
        final SetLabelValidationResult result = labelService.validateSetLabels(USER, ISSUE_ID, ImmutableSet.<String>of());
        assertNoEdit(result);
        assertThat(result.getLabels(), hasSize(0));
    }

    @Test
    public void testValidateAddLabelWithoutPermission()
    {
        issueManager.setEditable(false);
        final AddLabelValidationResult result = labelService.validateAddLabel(USER, ISSUE_ID, LABEL.getLabel());
        assertNoEdit(result);
    }

    @Test
    public void testValidateAddLabelWithInvalidCharacters()
    {
        issueManager.setEditable(true);

        final AddLabelValidationResult result = labelService.validateAddLabel(USER, ISSUE_ID, "bad label");
        assert1FieldError(result.getErrorCollection(), "labels", "label.service.error.label.invalid{[bad label]}");
    }

    @Test
    public void testValidateSetLabelsWithInvalidCharacters()
    {
        issueManager.setEditable(true);

        final SetLabelValidationResult result = labelService.validateSetLabels(USER, ISSUE_ID, ImmutableSet.of("b ad"));
        assert1FieldError(result.getErrorCollection(), "labels", "label.service.error.label.invalid{[b ad]}");
    }

    @Test
    public void testValidateAddLabelWithTooLongLabel()
    {
        final String tooLongLabel = StringUtils.repeat('x', LabelParser.MAX_LABEL_LENGTH + 1);
        issueManager.setEditable(true);

        final AddLabelValidationResult result = labelService.validateAddLabel(USER, ISSUE_ID, tooLongLabel);
        assert1FieldError(result.getErrorCollection(), "labels", "label.service.error.label.toolong{[" + tooLongLabel + "]}");
    }

    @Test
    public void testValidateAddLabelFieldRequired()
    {
        final FieldLayout mockFieldLayout = mock(FieldLayout.class);
        final FieldLayoutItem mockFieldLayoutItem = mock(FieldLayoutItem.class);
        final OrderableField mockField = mock(OrderableField.class);

        issueManager.setEditable(true);
        when(fieldLayoutManager.getFieldLayout(issueManager.getIssueObject(ISSUE_ID))).thenReturn(mockFieldLayout);
        when(mockFieldLayout.getFieldLayoutItem("labels")).thenReturn(mockFieldLayoutItem);
        when(mockFieldLayoutItem.isRequired()).thenReturn(true);
        when(mockFieldLayoutItem.getOrderableField()).thenReturn(mockField);
        when(mockField.getNameKey()).thenReturn("issue.field.labels");

        final SetLabelValidationResult result = labelService.validateSetLabels(USER, ISSUE_ID, ImmutableSet.<String>of());
        assert1FieldError(result.getErrorCollection(), "labels", "issue.field.required{[issue.field.labels{[]}]}");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSetLabelValidationResultRejected()
    {
        labelService.setLabels(USER, mock(SetLabelValidationResult.class), false, false);
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidAddLabelValidationResultRejected()
    {
        labelService.addLabel(USER, mock(AddLabelValidationResult.class), false);
    }

    @Test
    public void testSetSystemFieldLabels()
    {
        when(labelManager.setLabels(USER, ISSUE_ID, LABEL_STRING_SINGLETON, false, false))
                .thenReturn(LABEL_SINGLETON);

        final SetLabelValidationResult validationResult = createSetLabelsValidationResult(ISSUE_ID, null, LABEL_STRING_SINGLETON);
        final LabelsResult result = labelService.setLabels(USER, validationResult, false, false);

        assertNoErrors(result.getErrorCollection());
        assertEquals(LABEL_SINGLETON, result.getLabels());
    }

    @Test
    public void testSetCustomFieldLabels()
    {
        when(labelManager.setLabels(USER, ISSUE_ID, CUSTOM_FIELD_ID, LABEL_STRING_SINGLETON, false, false))
                .thenReturn(LABEL_SINGLETON);

        final SetLabelValidationResult validationResult = createSetLabelsValidationResult(ISSUE_ID, CUSTOM_FIELD_ID, LABEL_STRING_SINGLETON);
        final LabelsResult result = labelService.setLabels(USER, validationResult, false, false);

        assertNoErrors(result.getErrorCollection());
        assertEquals(LABEL_SINGLETON, result.getLabels());
    }

    @Test
    public void testAddSystemFieldLabel()
    {
        mockBrowsePermission();
        when(labelManager.addLabel(USER, ISSUE_ID, LABEL.getLabel(), false)).thenReturn(LABEL);
        when(labelManager.getLabels(ISSUE_ID)).thenReturn(LABEL_SINGLETON);

        final AddLabelValidationResult validationResult = createAddLabelValidationResult(ISSUE_ID, null, LABEL.getLabel());
        final LabelsResult result = labelService.addLabel(USER, validationResult, false);

        assertNoErrors(result.getErrorCollection());
        assertEquals(LABEL_SINGLETON, result.getLabels());
    }

    @Test
    public void testAddCustomFieldLabel()
    {
        mockBrowsePermission();
        when(labelManager.addLabel(USER, ISSUE_ID, CUSTOM_FIELD_ID, LABEL.getLabel(), false)).thenReturn(LABEL);
        when(customFieldManager.getCustomFieldObject(CUSTOM_FIELD_ID)).thenReturn(new MockCustomField());
        when(labelManager.getLabels(ISSUE_ID, CUSTOM_FIELD_ID)).thenReturn(LABEL_SINGLETON);

        final AddLabelValidationResult validationResult = createAddLabelValidationResult(ISSUE_ID, CUSTOM_FIELD_ID, LABEL.getLabel());
        final LabelsResult result = labelService.addLabel(USER, validationResult, false);

        assertNoErrors(result.getErrorCollection());
        assertEquals(LABEL_SINGLETON, result.getLabels());
    }

    @Test
    public void testValidateLabelSuggestions()
    {
        LabelService.LabelSuggestionResult labelSuggestionResult = labelService.getSuggestedLabels(USER, 10000L, " ");
        assert1ErrorMessage(labelSuggestionResult.getErrorCollection(), "label.service.error.label.invalid{[ ]}");

        labelSuggestionResult = labelService.getSuggestedLabels(USER, 10000L, "abc d");
        assert1ErrorMessage(labelSuggestionResult.getErrorCollection(), "label.service.error.label.invalid{[abc d]}");

        when(customFieldManager.getCustomFieldObject(22323L)).thenReturn(null);
        labelSuggestionResult = labelService.getSuggestedLabels(USER, 10000L, 22323L, "abcd");
        assert1ErrorMessage(labelSuggestionResult.getErrorCollection(), "label.service.error.custom.field.doesnt.exist{[22323]}");
    }

    @Test
    public void testGetLabelSuggestions()
    {
        final Set<String> expectedSet = ImmutableSortedSet.of("abcd1", "abcd2");
        when(labelManager.getSuggestedLabels(USER, 10000L, "abcd")).thenReturn(expectedSet);

        final LabelService.LabelSuggestionResult labelSuggestionResult = labelService.getSuggestedLabels(USER, 10000L, "abcd");
        assertNoErrors(labelSuggestionResult.getErrorCollection());
        assertEquals(expectedSet, labelSuggestionResult.getSuggestions());

    }

    @Test
    public void testGetLabelSuggestionsWithCustomField()
    {
        final Set<String> expectedSet = ImmutableSortedSet.of("abcd1", "abcd2");
        when(labelManager.getSuggestedLabels(USER, 10000L, CUSTOM_FIELD_ID, "abcd")).thenReturn(expectedSet);
        when(customFieldManager.getCustomFieldObject(CUSTOM_FIELD_ID)).thenReturn(new MockCustomField());

        final LabelService.LabelSuggestionResult labelSuggestionResult = labelService.getSuggestedLabels(USER, 10000L, CUSTOM_FIELD_ID, "abcd");
        assertNoErrors(labelSuggestionResult.getErrorCollection());
        assertEquals(expectedSet, labelSuggestionResult.getSuggestions());

    }

    @Test
    public void testGetLabelsForNonexistenIssue()
    {
        final LabelsResult result = labelService.getLabels(USER, 42L);
        assert1ErrorMessage(result.getErrorCollection(), "label.service.error.issue.doesnt.exist{[42]}");
    }

    @Test
    public void testGetLabelsForNonexistentCustomField()
    {
        mockBrowsePermission();
        final LabelsResult result = labelService.getLabels(USER, ISSUE_ID, CUSTOM_FIELD_ID);
        assert1ErrorMessage(result.getErrorCollection(), "label.service.error.custom.field.doesnt.exist{[" + CUSTOM_FIELD_ID + "]}");
    }



    private void mockBrowsePermission()
    {
        when(permissionManager.hasPermission(Permissions.BROWSE, issue, USER)).thenReturn(true);
    }

    private static SetLabelValidationResult createSetLabelsValidationResult(Long issueId, Long customFieldId, Set<String> labels)
    {
        return new SetLabelValidationResult(issueId, customFieldId, new SimpleErrorCollection(), labels);
    }

    private static AddLabelValidationResult createAddLabelValidationResult(Long issueId, Long customFieldId, String label)
    {
        return new AddLabelValidationResult(issueId, customFieldId, new SimpleErrorCollection(), label);
    }

    private static void assertNoBrowse(ServiceResultImpl result)
    {
        assert1ErrorMessage(result.getErrorCollection(), "label.service.error.issue.no.permission{[null]}");
    }

    private static void assertNoEdit(ServiceResultImpl result)
    {
        assert1ErrorMessage(result.getErrorCollection(), "label.service.error.issue.no.edit.permission{[null]}");
    }
}
