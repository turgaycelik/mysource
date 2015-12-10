package com.atlassian.jira.imports.project.parser;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValueImpl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestCustomFieldValueParserImpl
{
    CustomFieldValueParserImpl customFieldValueParser = new CustomFieldValueParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            customFieldValueParser.parse(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected!
        }
    }

    @Test
    public void testParseMissingId()
    {
        try
        {
            final Map attributes = EasyMap.build("issue", "10000", "customfield", "11111", "parentkey", "22222", "stringvalue", "string val", "numbervalue", "33333", "textvalue", "I am text");
            attributes.put("datevalue", "12/23/08");
            customFieldValueParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingIssueId()
    {
        try
        {
            final Map attributes = EasyMap.build("id", "10001", "customfield", "11111", "parentkey", "22222", "stringvalue", "string val", "numbervalue", "33333", "textvalue", "I am text");
            attributes.put("datevalue", "12/23/08");
            customFieldValueParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingCustomFieldId()
    {
        try
        {
            final Map attributes = EasyMap.build("id", "10001", "issue", "11111", "parentkey", "22222", "stringvalue", "string val", "numbervalue", "33333", "textvalue", "I am text");
            attributes.put("datevalue", "12/23/08");
            customFieldValueParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMinimalStringVal() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "customfield", "11111", "stringvalue", "string val");
        ExternalCustomFieldValue customFieldValue = customFieldValueParser.parse(attributes);
        assertEquals("10001", customFieldValue.getId());
        assertEquals("10000", customFieldValue.getIssueId());
        assertEquals("11111", customFieldValue.getCustomFieldId());
        assertEquals("string val", customFieldValue.getStringValue());
        assertEquals("string val", customFieldValue.getValue());
    }

    @Test
    public void testParseMinimalNumberVal() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "customfield", "11111", "numbervalue", "33333");
        ExternalCustomFieldValue customFieldValue = customFieldValueParser.parse(attributes);
        assertEquals("10001", customFieldValue.getId());
        assertEquals("10000", customFieldValue.getIssueId());
        assertEquals("11111", customFieldValue.getCustomFieldId());
        assertEquals("33333", customFieldValue.getNumberValue());
        assertEquals("33333", customFieldValue.getValue());
    }

    @Test
    public void testParseMinimalTextVal() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10001", "issue", "10000", "customfield", "11111", "textvalue", "text val");
        ExternalCustomFieldValue customFieldValue = customFieldValueParser.parse(attributes);
        assertEquals("10001", customFieldValue.getId());
        assertEquals("10000", customFieldValue.getIssueId());
        assertEquals("11111", customFieldValue.getCustomFieldId());
        assertEquals("text val", customFieldValue.getTextValue());
        assertEquals("text val", customFieldValue.getValue());
    }

    @Test
    public void testParseDateVal() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10001", "parentkey", "55555", "issue", "10000", "customfield", "11111", "datevalue", "12/12/08");
        ExternalCustomFieldValue customFieldValue = customFieldValueParser.parse(attributes);
        assertEquals("10001", customFieldValue.getId());
        assertEquals("10000", customFieldValue.getIssueId());
        assertEquals("11111", customFieldValue.getCustomFieldId());
        assertEquals("12/12/08", customFieldValue.getDateValue());
        assertEquals("12/12/08", customFieldValue.getValue());
        assertEquals("55555", customFieldValue.getParentKey());
    }

    @Test
    public void testGetEntityRepresentation()
    {
        ExternalCustomFieldValueImpl customFieldValue = new ExternalCustomFieldValueImpl("11111", "22222", "33333");
        customFieldValue.setStringValue("string value");
        customFieldValue.setParentKey("44444");

        final EntityRepresentation representation = customFieldValueParser.getEntityRepresentation(customFieldValue);
        assertNotNull(representation);
        assertEquals(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, representation.getEntityName());
        assertNull(representation.getEntityValues().get("id"));
        assertEquals("22222", representation.getEntityValues().get("customfield"));
        assertEquals("33333", representation.getEntityValues().get("issue"));
        assertEquals("string value", representation.getEntityValues().get("stringvalue"));
        assertEquals("44444", representation.getEntityValues().get("parentkey"));
        assertNull(representation.getEntityValues().get("numbervalue"));
        assertNull(representation.getEntityValues().get("textvalue"));
        assertNull(representation.getEntityValues().get("datevalue"));
    }

}
