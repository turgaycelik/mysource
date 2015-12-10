package com.atlassian.jira.imports.project.parser;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.imports.project.core.BackupOverviewBuilderImpl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestCustomFieldParserImpl
{
    CustomFieldParserImpl customFieldParser = new CustomFieldParserImpl();

    @Test
    public void testParseNullAttributeMapCustomField() throws ParseException
    {
        try
        {
            customFieldParser.parseCustomField(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected!
        }
    }

    @Test
    public void testParseMissingIdCustomField()
    {
        try
        {
            customFieldParser.parseCustomField(EasyMap.build("customfieldtypekey", "textCF", "name", "customField1"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingCustomFieldTypeKeyCustomField()
    {
        try
        {
            customFieldParser.parseCustomField(EasyMap.build("id", "10000", "name", "customField1"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingNameCustomField()
    {
        try
        {
            customFieldParser.parseCustomField(EasyMap.build("id", "10000", "customfieldtypekey", "textCF"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseCustomField() throws ParseException
    {
        final ExternalCustomField externalCustomField = customFieldParser.parseCustomField(EasyMap.build("id", "10000", "customfieldtypekey", "textCF", "name", "TomCF"));
        assertEquals("10000", externalCustomField.getId());
        assertEquals("textCF", externalCustomField.getTypeKey());
        assertEquals("TomCF", externalCustomField.getName());
    }

    @Test
    public void testParseNullAttributeMapCustomFieldConfiguration() throws ParseException
    {
        try
        {
            customFieldParser.parseCustomFieldConfiguration(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected!
        }
    }

    @Test
    public void testParseMissingIdCustomFieldConfiguration()
    {
        try
        {
            customFieldParser.parseCustomFieldConfiguration(EasyMap.build("fieldconfigscheme", "10001", "key", "customfield_10001"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingKeyCustomFieldConfiguration()
    {
        try
        {
            customFieldParser.parseCustomFieldConfiguration(EasyMap.build("id", "10000", "fieldconfigscheme", "10001"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingFieldConfigSchemeCustomFieldConfiguration()
    {
        try
        {
            customFieldParser.parseCustomFieldConfiguration(EasyMap.build("id", "10000", "key", "customfield_10001"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingKeyPrefixCustomFieldConfiguration() throws ParseException
    {
        // The parser only handles ConfigurationContext's that have a key prefixed with customfield_ so we want to
        // make sure that when we pass a wrong one we get back null.
        final BackupOverviewBuilderImpl.ConfigurationContext configurationContext = customFieldParser.parseCustomFieldConfiguration(EasyMap.build("id", "10000", "key", "issuetype", "fieldconfigscheme", "10002"));
        assertNull(configurationContext);
    }

    @Test
    public void testParseMinimalCustomFieldConfiguration() throws ParseException
    {
        final BackupOverviewBuilderImpl.ConfigurationContext configurationContext = customFieldParser.parseCustomFieldConfiguration(EasyMap.build("id", "10005", "key", "customfield_10000", "fieldconfigscheme", "10001"));
        assertEquals("10001", configurationContext.getConfigSchemeId());
        assertEquals("10000", configurationContext.getCustomFieldId());
    }

    @Test
    public void testParseFullCustomFieldConfiguration() throws ParseException
    {
        final BackupOverviewBuilderImpl.ConfigurationContext configurationContext = customFieldParser.parseCustomFieldConfiguration(EasyMap.build("id", "10005", "key", "customfield_10000", "fieldconfigscheme", "10001", "project", "10004"));
        assertEquals("10001", configurationContext.getConfigSchemeId());
        assertEquals("10000", configurationContext.getCustomFieldId());
        assertEquals("10004", configurationContext.getProjectId());
    }

    @Test
    public void testParseNullAttributeMapFieldConfigSchemeIssueType() throws ParseException
    {
        try
        {
            customFieldParser.parseFieldConfigSchemeIssueType(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected!
        }
    }

    @Test
    public void testParseMissingIdFieldConfigSchemeIssueType()
    {
        try
        {
            customFieldParser.parseFieldConfigSchemeIssueType(EasyMap.build("issuetype", "Bug", "fieldconfigscheme", "10001"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingIssueTypeFieldConfigSchemeIssueType() throws ParseException
    {
        final BackupOverviewBuilderImpl.FieldConfigSchemeIssueType fieldConfigSchemeIssueType = customFieldParser.parseFieldConfigSchemeIssueType(EasyMap.build("id", "10000", "fieldconfigscheme", "10001"));
        assertNull(fieldConfigSchemeIssueType.getIssueType());
    }

    @Test
    public void testParseMissingFieldConfigSchemeFieldConfigSchemeIssueType()
    {
        try
        {
            customFieldParser.parseFieldConfigSchemeIssueType(EasyMap.build("id", "10000", "issuetype", "Bug"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseFieldConfigSchemeIssueType() throws ParseException
    {
        final BackupOverviewBuilderImpl.FieldConfigSchemeIssueType fieldConfigSchemeIssueType = customFieldParser.parseFieldConfigSchemeIssueType(EasyMap.build("id", "10000", "issuetype", "Bug", "fieldconfigscheme", "10001"));
        assertEquals("10001", fieldConfigSchemeIssueType.getFieldConfigScheme());
        assertEquals("Bug", fieldConfigSchemeIssueType.getIssueType());
    }


}
