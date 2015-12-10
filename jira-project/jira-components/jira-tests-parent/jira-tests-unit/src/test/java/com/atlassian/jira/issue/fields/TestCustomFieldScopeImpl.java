package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCustomFieldScopeImpl
{
    private static final Long ANY_PROJECT = null;
    private static final Long PROJECT_1 = 1L;
    private static final Long PROJECT_2 = 2L;

    private static final String ANY_ISSUE_TYPE = null;
    private static final String BUG = "bug";
    private static final String TASK = "task";

    @Mock
    private CustomField customField;
    @Mock
    private FieldConfigSchemeManager fieldConfigSchemeManager;

    private CustomFieldScopeImpl scope;

    @Before
    public void setUp()
    {
        scope = new CustomFieldScopeImpl(customField, fieldConfigSchemeManager);
    }

    @Test
    public void isIncludedInReturnsTrueIfTheIssueContextIsNull()
    {
        IssueContext issueContext = null;
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(ANY_PROJECT, ANY_ISSUE_TYPE));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsTrueIfTheIssueContextIsAnyProjectAndAnyIssueType()
    {
        IssueContext issueContext = anyProjectAndAnyIssueType();
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(ANY_PROJECT, ANY_ISSUE_TYPE));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsTrueWhenIssueContextIsForAnyProjectAndSpecificIssueTypeAndCustomFieldHasASchemeForAnyProjectAndAnyIssueType()
    {
        IssueContext issueContext = issueContextFor(ANY_PROJECT, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(ANY_PROJECT, ANY_ISSUE_TYPE));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsTrueWhenIssueContextIsForAnyProjectAndSpecificIssueTypeAndCustomFieldHasASchemeForAnyProjectAndSameIssueType()
    {
        IssueContext issueContext = issueContextFor(ANY_PROJECT, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(ANY_PROJECT, BUG));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsTrueWhenIssueContextIsForAnyProjectAndSpecificIssueTypeAndCustomFieldHasASchemeForASpecificProjectAndSameIssueType()
    {
        IssueContext issueContext = issueContextFor(ANY_PROJECT, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(PROJECT_1, BUG));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsFalseWhenIssueContextIsForAnyProjectAndSpecificIssueTypeAndCustomFieldDoesNotHaveAMatchingScheme()
    {
        IssueContext issueContext = issueContextFor(ANY_PROJECT, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(ANY_PROJECT, TASK));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(false));
    }

    @Test
    public void isIncludedInWhenIssueContextIsForAnyProjectAndSpecificIssueTypeIteratesThroughTheCustomFieldSchemesToDetermineWhetherisIncludedIn()
    {
        IssueContext issueContext = issueContextFor(ANY_PROJECT, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(
                configSchemeFor(ANY_PROJECT, TASK),
                configSchemeFor(PROJECT_1, TASK),
                configSchemeFor(PROJECT_1, BUG) // this is the scheme that makes the custom field in scope
        );

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsTrueWhenIssueContextIsForASpecificProjectAndAnyIssueTypeAndCustomFieldHasASchemeForAnyProjectAndAnyIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, ANY_ISSUE_TYPE);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(ANY_PROJECT, ANY_ISSUE_TYPE));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsTrueWhenIssueContextIsForASpecificProjectAndAnyIssueTypeAndCustomFieldHasASchemeForSameProjectAndAnyIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, ANY_ISSUE_TYPE);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(PROJECT_1, ANY_ISSUE_TYPE));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsTrueWhenIssueContextIsForASpecificProjectAndAnyIssueTypeAndCustomFieldHasASchemeForTheSameProjectAndASpecificIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, ANY_ISSUE_TYPE);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(PROJECT_1, BUG));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsFalseWhenIssueContextIsForASpecificProjectAndAnyIssueTypeAndCustomFieldDoesNotHaveAMatchingScheme()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, ANY_ISSUE_TYPE);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(PROJECT_2, ANY_ISSUE_TYPE));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(false));
    }

    @Test
    public void isIncludedInWhenIssueContextIsForASpecificProjectAndAnyIssueTypeIteratesThroughTheCustomFieldSchemesToDetermineWhetherisIncludedIn()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, ANY_ISSUE_TYPE);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(
                configSchemeFor(PROJECT_2, TASK),
                configSchemeFor(PROJECT_2, BUG),
                configSchemeFor(PROJECT_1, BUG) // this is the scheme that makes the custom field in scope
        );

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsTrueWhenIssueContextIsForASpecificProjectAndSpecificIssueTypeAndCustomFieldHasASchemeForAnyProjectAndAnyIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(ANY_PROJECT, ANY_ISSUE_TYPE));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsTrueWhenIssueContextIsForASpecificProjectAndSpecificIssueTypeAndCustomFieldHasASchemeForAnyProjectAndSameIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(ANY_PROJECT, BUG));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsFalseWhenIssueContextIsForASpecificProjectAndSpecificIssueTypeAndCustomFieldHasASchemeForAnyProjectAndADifferentIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(ANY_PROJECT, TASK));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(false));
    }

    @Test
    public void isIncludedInReturnsTrueWhenIssueContextIsForASpecificProjectAndSpecificIssueTypeAndCustomFieldHasASchemeForSameProjectAndAnyIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(PROJECT_1, ANY_ISSUE_TYPE));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsFalseWhenIssueContextIsForASpecificProjectAndSpecificIssueTypeAndCustomFieldHasASchemeForADifferentProjectAndAnyIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(PROJECT_2, ANY_ISSUE_TYPE));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(false));
    }

    @Test
    public void isIncludedInReturnsTrueWhenIssueContextIsForASpecificProjectAndSpecificIssueTypeAndCustomFieldHasASchemeForTheSameProjectAndSameIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(configSchemeFor(PROJECT_1, BUG));

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInWhenIssueContextIsForASpecificProjectAndASpecificIssueTypeChecksIfThereIsASchemeThatSatisfiesBothBeingincludedByProjectAndByIssueType()
    {
        // An incorrect implementation of this method would check if there is at least one scheme that is in scope by project
        // and also if there is at least one scheme that is in scope by issue type.
        //
        // What the method must check when the given issue context specifies a project and an issue type is if there
        // is at least one scheme that is in scope both by project and by issue type.
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(
                configSchemeFor(PROJECT_1, TASK), // not in scope by issue type
                configSchemeFor(PROJECT_2, BUG)   // not in scope by project
        );

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(false));
    }

    @Test
    public void isIncludedInWhenIssueContextIsForASpecificProjectAndASpecificIssueTypeIteratesThroughTheCustomFieldSchemesToDetermineWhetherisIncludedIn()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(
                configSchemeFor(PROJECT_1, TASK),
                configSchemeFor(PROJECT_2, BUG),
                configSchemeFor(PROJECT_1, BUG) // this is the scheme that makes the custom field in scope
        );

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInHandlesCorrectlyFieldSchemesThatHaveMoreThanOneProjectId()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(
                configSchemeFor(Arrays.asList(PROJECT_2, PROJECT_1), BUG)
        );

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInHandlesCorrectlyFieldSchemesThatHaveMoreThanOneIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(
                configSchemeFor(PROJECT_1, Arrays.asList(TASK, BUG))
        );

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInHandlesCorrectlyFieldSchemesThatHaveMoreThanOneProjectAndOneIssueType()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Arrays.asList(
                configSchemeFor(Arrays.asList(PROJECT_2, PROJECT_1), Arrays.asList(TASK, BUG))
        );

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(true));
    }

    @Test
    public void isIncludedInReturnsFalseWhenTheCustomFieldHasNullAsItsListOfFieldSchemes()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = null;

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(false));
    }

    @Test
    public void isIncludedInReturnsFalseWhenTheCustomFieldAListOfFieldSchemesThatIsEmpty()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Collections.emptyList();

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        boolean included = scope.isIncludedIn(issueContext);

        assertThat(included, is(false));
    }

    @Test
    public void isIncludedInLoadsTheCustomFieldSchemesJustOnce()
    {
        IssueContext issueContext = issueContextFor(PROJECT_1, BUG);
        List<FieldConfigScheme> customFieldSchemes = Collections.emptyList();

        when(fieldConfigSchemeManager.getConfigSchemesForField(customField)).thenReturn(customFieldSchemes);

        scope.isIncludedIn(issueContext);
        scope.isIncludedIn(issueContext);

        verify(fieldConfigSchemeManager, times(1)).getConfigSchemesForField(customField);
    }

    private IssueContext issueContextFor(final Long project, final String issueType)
    {
        return new IssueContextImpl(project, issueType);
    }

    private IssueContext anyProjectAndAnyIssueType()
    {
        return new IssueContextImpl(ANY_PROJECT, ANY_ISSUE_TYPE);
    }

    private FieldConfigScheme configSchemeFor(Long projectId, String issueType)
    {
        return configSchemeFor(projectId, Arrays.asList(issueType));
    }

    private FieldConfigScheme configSchemeFor(final List<Long> projectIds, final String issueType)
    {
        return configSchemeFor(projectIds, Arrays.asList(issueType));
    }

    private FieldConfigScheme configSchemeFor(final Long projectId, final List<String> issueTypes)
    {
        return configSchemeFor(projectIdsListForConfigScheme(projectId), issueTypes);
    }

    private FieldConfigScheme configSchemeFor(final List<Long> projectIds, final List<String> issueTypes)
    {
        FieldConfigScheme fieldConfigScheme = mock(FieldConfigScheme.class);

        when(fieldConfigScheme.getAssociatedProjectIds()).thenReturn(projectIds);
        when(fieldConfigScheme.getConfigs()).thenReturn(fieldConfigsByIssueType(issueTypes));

        return fieldConfigScheme;
    }

    private List<Long> projectIdsListForConfigScheme(Long projectId)
    {
        // when a FieldConfigScheme applies to any project, the call to getAssociatedProjectIds returns an empty list
        return projectId == (ANY_PROJECT) ? Collections.<Long>emptyList() : Arrays.asList(projectId);
    }

    private Map<String, FieldConfig> fieldConfigsByIssueType(final List<String> issueTypes)
    {
        // when a FieldConfigScheme applies to any issue type, there is a mapping between null and the FieldConfig
        Map<String, FieldConfig> fieldConfigs = new HashMap<String, FieldConfig>();
        for (String issueType : issueTypes)
        {
            fieldConfigs.put(issueType, mock(FieldConfig.class));
        }
        return fieldConfigs;
    }
}
