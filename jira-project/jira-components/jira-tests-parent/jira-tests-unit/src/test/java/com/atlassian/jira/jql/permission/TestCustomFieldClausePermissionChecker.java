package com.atlassian.jira.jql.permission;

import java.util.Set;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCustomFieldClausePermissionChecker
{
    private static final Set<FieldLayout> NULL_LAYOUT = null;

    @Mock private CustomField customField;
    @Mock private FieldManager fieldManager;
    @Mock private FieldConfigSchemeClauseContextUtil contextUtil;

    @After
    public void tearDown()
    {
        customField = null;
        fieldManager = null;
        contextUtil = null;
    }

    @Test
    public void testFieldIsHidden() throws Exception
    {
        when(fieldManager.isFieldHidden(NULL_LAYOUT, customField)).thenReturn(true);

        final CustomFieldClausePermissionChecker permissionChecker = new CustomFieldClausePermissionChecker(customField, fieldManager, contextUtil);
        final boolean hasPermission = permissionChecker.hasPermissionToUseClause(null);
        assertThat("hasPermissionToUseClause", hasPermission, is(false));
    }

    @Test
    public void testFieldNotHiddenTwoConfigSchemesOneVisible() throws Exception
    {
        final FieldConfigScheme configScheme1 = mock(FieldConfigScheme.class);
        final FieldConfigScheme configScheme2 = mock(FieldConfigScheme.class);

        when(customField.getConfigurationSchemes()).thenReturn(ImmutableList.of(configScheme1, configScheme2));
        when(contextUtil.getContextForConfigScheme(null, configScheme1)).thenReturn(new ClauseContextImpl());
        when(contextUtil.getContextForConfigScheme(null, configScheme2)).thenReturn(ClauseContextImpl.createGlobalClauseContext());

        final CustomFieldClausePermissionChecker permissionChecker = new CustomFieldClausePermissionChecker(customField, fieldManager, contextUtil);
        final boolean hasPermission = permissionChecker.hasPermissionToUseClause(null);
        assertThat("hasPermissionToUseClause", hasPermission, is(true));
    }

    @Test
    public void testFieldNotHiddenTwoConfigSchemesNoneVisible() throws Exception
    {
        final FieldConfigScheme configScheme1 = mock(FieldConfigScheme.class);
        final FieldConfigScheme configScheme2 = mock(FieldConfigScheme.class);

        when(customField.getConfigurationSchemes()).thenReturn(ImmutableList.of(configScheme1, configScheme2));
        when(contextUtil.getContextForConfigScheme(null, configScheme1)).thenReturn(new ClauseContextImpl());
        when(contextUtil.getContextForConfigScheme(null, configScheme2)).thenReturn(new ClauseContextImpl());

        final CustomFieldClausePermissionChecker permissionChecker = new CustomFieldClausePermissionChecker(customField, fieldManager, contextUtil);
        final boolean hasPermission = permissionChecker.hasPermissionToUseClause(null);
        assertThat("hasPermissionToUseClause", hasPermission, is(false));
    }

    @Test
    public void testFieldNotHiddenNoConfigSchemes() throws Exception
    {
        when(customField.getConfigurationSchemes()).thenReturn(ImmutableList.<FieldConfigScheme>of());

        final CustomFieldClausePermissionChecker permissionChecker = new CustomFieldClausePermissionChecker(customField, fieldManager, contextUtil);
        final boolean hasPermission = permissionChecker.hasPermissionToUseClause(null);
        assertThat("hasPermissionToUseClause", hasPermission, is(false));
    }
}
