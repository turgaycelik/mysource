package com.atlassian.jira.imports.project.parser;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalChangeItem;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestChangeItemParserImpl
{
    ChangeItemParserImpl changeItemParser = new ChangeItemParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            changeItemParser.parse(null);
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
            //<ChangeItem id="10014" group="10006" fieldtype="jira" field="security" oldvalue="10000" oldstring="level1" newvalue="10001" newstring="level2"/>
            final Map attributes = EasyMap.build("group", "10006", "fieldtype", "jira", "field", "security", "oldvalue", "10000", "oldstring", "level1");
            attributes.put("newvalue", "10001");
            attributes.put("newstring", "level2");
            changeItemParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
            assertEquals("A change item must have an id specified.", e.getMessage());
        }
    }

    @Test
    public void testParseMissingGroup()
    {
        try
        {
            //<ChangeItem id="10014" group="10006" fieldtype="jira" field="security" oldvalue="10000" oldstring="level1" newvalue="10001" newstring="level2"/>
            final Map attributes = EasyMap.build("id", "10014", "fieldtype", "jira", "field", "security", "oldvalue", "10000", "oldstring", "level1");
            attributes.put("newvalue", "10001");
            attributes.put("newstring", "level2");
            changeItemParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
            assertEquals("Change item '10014' is missing the change group id.", e.getMessage());
        }
    }

    @Test
    public void testParseMissingField()
    {
        try
        {
            //<ChangeItem id="10014" group="10006" fieldtype="jira" field="security" oldvalue="10000" oldstring="level1" newvalue="10001" newstring="level2"/>
            final Map attributes = EasyMap.build("id", "10014", "group", "10006", "fieldtype", "jira", "oldvalue", "10000", "oldstring", "level1");
            attributes.put("newvalue", "10001");
            attributes.put("newstring", "level2");
            changeItemParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
            assertEquals("Change item '10014' is missing the 'field' attribute.", e.getMessage());
        }
    }

    @Test
    public void testParseHappy() throws ParseException
    {
        //<ChangeItem id="10014" group="10006" fieldtype="jira" field="security" oldvalue="10000" oldstring="level1" newvalue="10001" newstring="level2"/>
        final Map attributes = EasyMap.build("id", "10014", "group", "10006", "fieldtype", "jira", "field", "security", "oldvalue", "10000", "oldstring", "level1");
        attributes.put("newvalue", "10001");
        attributes.put("newstring", "level2");

        ExternalChangeItem changeItem = changeItemParser.parse(attributes);
        assertEquals("10014", changeItem.getId());
        assertEquals("10006", changeItem.getChangeGroupId());
        assertEquals("jira", changeItem.getFieldType());
        assertEquals("security", changeItem.getField());
        assertEquals("10000", changeItem.getOldValue());
        assertEquals("level1", changeItem.getOldString());
        assertEquals("10001", changeItem.getNewValue());
        assertEquals("level2", changeItem.getNewString());
    }

    @Test
    public void testGetEntityRepresentation()
    {
        ExternalChangeItem changeItem = new ExternalChangeItem("12", "15", "jira", "security", "10000", "level1", "10001", "level2");
        final EntityRepresentation representation = changeItemParser.getEntityRepresentation(changeItem);
        assertNotNull(representation);
        assertEquals("12", representation.getEntityValues().get("id"));
        assertEquals("15", representation.getEntityValues().get("group"));
        assertEquals("jira", representation.getEntityValues().get("fieldtype"));
        assertEquals("security", representation.getEntityValues().get("field"));
        assertEquals("10000", representation.getEntityValues().get("oldvalue"));
        assertEquals("level1", representation.getEntityValues().get("oldstring"));
        assertEquals("10001", representation.getEntityValues().get("newvalue"));
        assertEquals("level2", representation.getEntityValues().get("newstring"));

        assertEquals("ChangeItem", representation.getEntityName());
    }

}
