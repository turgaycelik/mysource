package com.atlassian.jira.issue.customfields.impl;

import java.util.Locale;

import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.converters.DoubleConverterImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.MockCustomField;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.security.MockAuthenticationContext;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestNumberCFType
{
    @Test
    public void testGetProjectImporter() throws Exception
    {
        NumberCFType numberCFType = new NumberCFType(null, null, null);
        assertTrue(numberCFType.getProjectImporter() instanceof NoTransformationCustomFieldImporter);
    }

    @Test
    public void testLocaleSensitivity() throws Exception {
        MockI18nHelper i18nHelper = new MockI18nHelper();
        i18nHelper.setLocale(Locale.ENGLISH);
        DoubleConverter converter = new DoubleConverterImpl(new MockAuthenticationContext(null, i18nHelper));

        NumberCFType numberCFType = new NumberCFType(null, converter, null);

        CustomField field = new MockCustomField();

        assertEquals("123343.23", numberCFType.getStringFromSingularObject(123343.23d));
        assertEquals("123343.23", numberCFType.getChangelogValue(field, 123343.23d));
        assertEquals(null, numberCFType.getChangelogString(field, 123343.23d));

        i18nHelper.setLocale(new Locale("pl", "pl"));

        assertEquals("123343,23", numberCFType.getStringFromSingularObject(123343.23d));
        assertEquals("123343.23", numberCFType.getChangelogValue(field, 123343.23d));
        assertEquals(null, numberCFType.getChangelogString(field, 123343.23d));
    }

}
