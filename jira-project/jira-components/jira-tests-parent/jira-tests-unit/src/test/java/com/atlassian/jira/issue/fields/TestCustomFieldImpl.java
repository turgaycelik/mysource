package com.atlassian.jira.issue.fields;

import com.atlassian.fugue.Option;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigImpl;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptors;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptors;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCustomFieldImpl
{
    @Mock
    private CustomFieldDescription customFieldDescription;

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
    }

    @Test
    public void testFieldConfigComparison()
    {
        final FieldConfig fieldConfigX = new FieldConfigImpl(new Long(1), null, null, null, null);
        final FieldConfig fieldConfigY = new FieldConfigImpl(new Long(2), null, null, null, null);

        assertEquals(CustomFieldImpl.areDifferent(null, null), false);
        assertEquals(CustomFieldImpl.areDifferent(fieldConfigX, null), true);
        assertEquals(CustomFieldImpl.areDifferent(null, fieldConfigX), true);
        assertEquals(CustomFieldImpl.areDifferent(fieldConfigX, fieldConfigX), false);
        assertEquals(CustomFieldImpl.areDifferent(fieldConfigX, fieldConfigY), true);
        assertEquals(CustomFieldImpl.areDifferent(fieldConfigY, fieldConfigX), true);
    }

    @Test
    public void testExceptionDuringSearcherInit() throws Exception
    {
        final String customFieldKey = "mockkey";
        final GenericValue customFieldGV = new MockGenericValue("customField",
                MapBuilder.<String, Object>newBuilder()
                        .add("id", (long) 1)
                        .add("customfieldsearcherkey", customFieldKey).toMap());

        final CustomFieldSearcher searcher = mock(CustomFieldSearcher.class);
        doThrow(new RuntimeException()).when(searcher).init(any(CustomField.class));

        final CustomFieldManager customFieldManager = mock(CustomFieldManager.class);
        when(customFieldManager.getCustomFieldSearcher(customFieldKey)).thenReturn(searcher);

        final FieldConfigSchemeClauseContextUtil contextUtil = null;
        final JiraAuthenticationContext authenticationContext = null;
        final FieldConfigSchemeManager fieldConfigSchemeManager = null;
        final PermissionManager permissionManager = null;
        final RendererManager rendererManager = null;
        final FeatureManager featureManager = null;
        final TranslationManager translationManager = null;
        final CustomFieldScopeFactory scopeFactory = null;
        final CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors = null;
        final CustomFieldSearcherModuleDescriptors customFieldSearcherModuleDescriptors = mock(CustomFieldSearcherModuleDescriptors.class);
        when(customFieldSearcherModuleDescriptors.getCustomFieldSearcher(anyString())).thenReturn(Option.some(searcher));

        final CustomFieldImpl customField = new CustomFieldImpl(
                customFieldGV,
                authenticationContext,
                fieldConfigSchemeManager,
                permissionManager,
                rendererManager,
                contextUtil,
                customFieldDescription,
                featureManager,
                translationManager,
                scopeFactory,
                customFieldTypeModuleDescriptors,
                customFieldSearcherModuleDescriptors
        );

        // we should swallow the exception from the searcher and return a null searcher instead.
        assertNull(customField.getCustomFieldSearcher());
    }

    @Test
    public void testIssueComparatorEquals()
    {
        final CustomField mockCustomField = mock(CustomField.class);

        final CustomFieldImpl.CustomFieldIssueSortComparator comparator = new CustomFieldImpl.CustomFieldIssueSortComparator(mockCustomField);
        assertEquals(comparator, comparator);

        final String cfId = "customfield_2000";
        when(mockCustomField.getId()).thenReturn(cfId);

        assertEquals(comparator.hashCode(), comparator.hashCode());
        assertEquals(cfId.hashCode(), comparator.hashCode());
        verify(mockCustomField, atLeastOnce()).getId();

        final CustomField mockCustomField2 = mock(CustomField.class);
        when(mockCustomField2.getId()).thenReturn(cfId);

        final CustomFieldImpl.CustomFieldIssueSortComparator comparator2 = new CustomFieldImpl.CustomFieldIssueSortComparator(mockCustomField2);
        assertEquals(comparator, comparator2);
        verify(mockCustomField2).getId();
        assertEquals(comparator.hashCode(), comparator2.hashCode());

        final CustomField mockCustomField3 = mock(CustomField.class);
        final String anotherCfId = "customfield_1001";
        when(mockCustomField3.getId()).thenReturn(anotherCfId);

        final CustomFieldImpl.CustomFieldIssueSortComparator comparator3 = new CustomFieldImpl.CustomFieldIssueSortComparator(mockCustomField3);
        assertFalse(comparator.equals(comparator3));
        assertFalse(comparator2.equals(comparator3));
        verify(mockCustomField3, atLeastOnce()).getId();

        assertFalse(comparator.hashCode() == comparator3.hashCode());
        assertFalse(comparator2.hashCode() == comparator3.hashCode());

        assertEquals(anotherCfId.hashCode(), comparator3.hashCode());
    }

    @Test
    public void isInScopeForSearchGracefullyHandlesANullProjectPassedIn()
    {
        CustomFieldScope scope = mock(CustomFieldScope.class);

        CustomFieldImpl customField = customFieldWithScope(scope);
        customField.isInScopeForSearch(null, anyIssueTypeIds());

        verify(scope, atLeastOnce()).isIncludedIn(any(IssueContext.class));
    }

    @Test
    public void isInScopeForSearchGracefullyHandlesANullListOfIssueTypesPassedIn()
    {
        CustomFieldScope scope = mock(CustomFieldScope.class);

        CustomFieldImpl customField = customFieldWithScope(scope);
        customField.isInScopeForSearch(anyProject(), null);

        verify(scope, atLeastOnce()).isIncludedIn(any(IssueContext.class));
    }

    @Test
    public void isInScopeForSearchReturnsFalseIfItIsNotInScopeOfAtLeastOneIssueContext()
    {
        CustomFieldScope scope = mock(CustomFieldScope.class);
        when(scope.isIncludedIn(any(IssueContext.class))).thenReturn(false);

        CustomFieldImpl customField = customFieldWithScope(scope);
        boolean inScope = customField.isInScopeForSearch(anyProject(), anyIssueTypeIds());

        assertThat(inScope, is(false));
    }

    @Test
    public void isInScopeForSearchReturnsTrueIfItIsInScopeOfAtLeastOneIssueContext()
    {
        CustomFieldScope scope = mock(CustomFieldScope.class);
        CustomFieldImpl customField = customFieldWithScope(scope);

        Long projectId = 1L;
        List<String> issueTypeIds = Arrays.asList("bug", "task");
        List<IssueContext> issueContexts = asList(
                new IssueContextImpl(projectId, "bug"),
                new IssueContextImpl(projectId, "task")
        );

        when(scope.isIncludedIn(issueContexts.get(0))).thenReturn(false);
        when(scope.isIncludedIn(issueContexts.get(1))).thenReturn(true);

        boolean inScope = customField.isInScopeForSearch(projectWithId(projectId), issueTypeIds);

        assertThat(inScope, is(true));
    }

    @Test
    public void storeUpdatesTheCustomFieldUsingTheCustomFieldManager()
    {
        CustomFieldManager customFieldManager = mock(CustomFieldManager.class);
        bindCustomFieldManagerToComponentAccessor(customFieldManager);

        CustomFieldImpl customField = anyCustomField();
        customField.store();

        verify(customFieldManager, times(1)).updateCustomField(customField);
    }
    
    @Test
    public void getDefaultValueReturnsNullWhenTheRelevantConfigForTheGivenIssueIsNull()
    {
        FieldConfigSchemeManager fieldConfigSchemeManager = mock(FieldConfigSchemeManager.class);
        CustomFieldImpl customField = customFieldWith(fieldConfigSchemeManager);

        Issue issue = mock(Issue.class);
        when(fieldConfigSchemeManager.getRelevantConfig(issue, customField)).thenReturn(null);

        Object defaultValue = customField.getDefaultValue(issue);

        assertThat(defaultValue, is(nullValue()));
    }
    
    @Test
    public void getJsonDefaultValueReturnsNullWhenTheRelevantConfigForTheGivenIssueContextIsNull()
    {
        FieldConfigSchemeManager fieldConfigSchemeManager = mock(FieldConfigSchemeManager.class);
        CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors = mock(CustomFieldTypeModuleDescriptors.class);
        CustomFieldImpl customField = customFieldWith(fieldConfigSchemeManager, customFieldTypeModuleDescriptors);

        IssueContext issueContext = mock(IssueContext.class);
        when(fieldConfigSchemeManager.getRelevantConfig(issueContext, customField)).thenReturn(null);
        when(customFieldTypeModuleDescriptors.getCustomFieldType(anyString())).thenReturn(Option.some(mock(CustomFieldType.class)));

        Object defaultValue = customField.getJsonDefaultValue(issueContext);

        assertThat(defaultValue, is(nullValue()));
    }

    private CustomFieldImpl customFieldWith(final FieldConfigSchemeManager fieldConfigSchemeManager, final CustomFieldTypeModuleDescriptors customFieldTypeModuleDescriptors)
    {
        return new CustomFieldImpl(
                anyGenericValue(),
                mock(JiraAuthenticationContext.class),
                fieldConfigSchemeManager,
                mock(PermissionManager.class),
                mock(RendererManager.class),
                mock(FieldConfigSchemeClauseContextUtil.class),
                mock(CustomFieldDescription.class),
                mock(FeatureManager.class),
                mock(TranslationManager.class),
                mock(CustomFieldScopeFactory.class),
                customFieldTypeModuleDescriptors,
                mock(CustomFieldSearcherModuleDescriptors.class)
        );
    }

    private CustomFieldImpl customFieldWith(final FieldConfigSchemeManager fieldConfigSchemeManager)
    {
        return new CustomFieldImpl(
                anyGenericValue(),
                mock(JiraAuthenticationContext.class),
                fieldConfigSchemeManager,
                mock(PermissionManager.class),
                mock(RendererManager.class),
                mock(FieldConfigSchemeClauseContextUtil.class),
                mock(CustomFieldDescription.class),
                mock(FeatureManager.class),
                mock(TranslationManager.class),
                mock(CustomFieldScopeFactory.class),
                mock(CustomFieldTypeModuleDescriptors.class),
                mock(CustomFieldSearcherModuleDescriptors.class)
        );
    }

    private void bindCustomFieldManagerToComponentAccessor(final CustomFieldManager customFieldManager)
    {
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.addMock(CustomFieldManager.class, customFieldManager);
        componentWorker.init();
    }

    private List<IssueContext> asList(final IssueContext... issueContexts)
    {
        return Arrays.asList(issueContexts);
    }

    private Project projectWithId(final Long id)
    {
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(id);
        return project;
    }

    private Project anyProject()
    {
        return projectWithId(null);
    }

    private List<String> anyIssueTypeIds()
    {
        return Arrays.asList("bug", "task");
    }

    private static GenericValue anyGenericValue()
    {
        return mock(GenericValue.class);
    }

    private CustomFieldImpl customFieldWithScope(final CustomFieldScope scope)
    {
        CustomFieldScopeFactory scopeFactory = mock(CustomFieldScopeFactory.class);
        CustomFieldImpl customField = new CustomFieldImpl(
                anyGenericValue(),
                mock(JiraAuthenticationContext.class),
                mock(FieldConfigSchemeManager.class),
                mock(PermissionManager.class),
                mock(RendererManager.class),
                mock(FieldConfigSchemeClauseContextUtil.class),
                mock(CustomFieldDescription.class),
                mock(FeatureManager.class),
                mock(TranslationManager.class),
                scopeFactory,
                mock(CustomFieldTypeModuleDescriptors.class),
                mock(CustomFieldSearcherModuleDescriptors.class)
        );

        when(scopeFactory.createFor(customField)).thenReturn(scope);

        return customField;
    }

    private CustomFieldImpl anyCustomField()
    {
        return new CustomFieldImpl(
                anyGenericValue(),
                mock(JiraAuthenticationContext.class),
                mock(FieldConfigSchemeManager.class),
                mock(PermissionManager.class),
                mock(RendererManager.class),
                mock(FieldConfigSchemeClauseContextUtil.class),
                mock(CustomFieldDescription.class),
                mock(FeatureManager.class),
                mock(TranslationManager.class),
                mock(CustomFieldScopeFactory.class),
                mock(CustomFieldTypeModuleDescriptors.class),
                mock(CustomFieldSearcherModuleDescriptors.class)
        );
    }
}
