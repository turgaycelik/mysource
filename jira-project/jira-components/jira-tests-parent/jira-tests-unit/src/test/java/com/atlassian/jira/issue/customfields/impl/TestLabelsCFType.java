package com.atlassian.jira.issue.customfields.impl;

import java.util.Set;

import com.atlassian.jira.issue.customfields.ProjectImportLabelFieldParser;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static com.atlassian.jira.util.ErrorCollectionAssert.assert1FieldError;
import static com.atlassian.jira.util.ErrorCollectionAssert.assertNoErrors;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestLabelsCFType
{
    @Test
    public void testValidate()
    {
        final ProjectImportLabelFieldParser projectImportLabelFieldParser = new ProjectImportLabelFieldParserImpl();
        final JiraAuthenticationContext context = mock(JiraAuthenticationContext.class);
        final JiraBaseUrls jiraBaseUrls = mock(JiraBaseUrls.class);
        when(context.getI18nHelper()).thenReturn(new MockI18nHelper());
        final FieldConfig mockFieldConfig = mock(FieldConfig.class);
        final CustomField mockCustomField = mock(CustomField.class);
        when(mockCustomField.getId()).thenReturn("customfield_10000");
        when(mockFieldConfig.getCustomField()).thenReturn(mockCustomField);

        final LabelsCFType labelsCFType = new LabelsCFType(context, null, null, null, null, projectImportLabelFieldParser, jiraBaseUrls);
        final CustomFieldParams params = new CustomFieldParamsImpl();
        final ErrorCollection errors = new SimpleErrorCollection();

        labelsCFType.validateFromParams(params, errors, null);
        assertNoErrors(errors);
        
        //try empty
        params.put(null, ImmutableList.<String>of());
        labelsCFType.validateFromParams(params, errors, null);
        assertNoErrors(errors);

        //now let's provide some valid labels
        params.put(null, ImmutableList.of("blah", "dude", "awesome"));
        labelsCFType.validateFromParams(params, errors, null);
        assertNoErrors(errors);

        final String reallyLongLabel = StringUtils.repeat("0123456789ABCDEF", "", 16);  // 256 chars
        params.put(null, ImmutableList.of("label", reallyLongLabel));
        labelsCFType.validateFromParams(params, errors, mockFieldConfig);
        assert1FieldError(errors, "customfield_10000", "label.service.error.label.toolong [" + reallyLongLabel + ']');
    }

    @Test
    public void testEmptySetAndNullValuesEqual()
    {
        final Set<Label> emptySet = ImmutableSet.of();
        assertTrue("emptySet equals null", newEmptyLabelsCFType().valuesEqual(emptySet, null));
        assertTrue("null equals emptySet", newEmptyLabelsCFType().valuesEqual(null, emptySet));
        assertTrue("null equals null", newEmptyLabelsCFType().valuesEqual(null, null));
        assertTrue("emptySet equals emptySet", newEmptyLabelsCFType().valuesEqual(emptySet, emptySet));
    }

    private static LabelsCFType newEmptyLabelsCFType()
    {
        final ProjectImportLabelFieldParser projectImportLabelFieldParser = new ProjectImportLabelFieldParserImpl();
        final JiraBaseUrls jiraBaseUrls = mock(JiraBaseUrls.class);
        return new LabelsCFType(null, null, null, null, null, projectImportLabelFieldParser, jiraBaseUrls);
    }
}
