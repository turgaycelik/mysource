package com.atlassian.jira.imports.project.parser;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComponent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestProjectComponentParserImpl
{
    ProjectComponentParserImpl projectComponentParser = new ProjectComponentParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            projectComponentParser.parse(null);
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
            projectComponentParser.parse(EasyMap.build("project", "10000", "name", "version1"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingProject()
    {
        try
        {
            projectComponentParser.parse(EasyMap.build("id", "10000", "name", "version1"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingName()
    {
        try
        {
            projectComponentParser.parse(EasyMap.build("id", "10000", "project", "10000"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMinimal() throws ParseException
    {
        // <Component id="10010" project="10001" name="comp 1" lead="admin" assigneetype="0"/>
        final ExternalComponent externalComponent = projectComponentParser.parse(EasyMap.build("id", "10000", "project", "10002", "name", "Tom"));
        assertEquals("10000", externalComponent.getId());
        assertEquals("10002", externalComponent.getProjectId());
        assertEquals("Tom", externalComponent.getName());
    }

    @Test
    public void testParseFullMap() throws ParseException
    {
        final ExternalComponent externalComponent = projectComponentParser.parse(EasyMap.build("id", "10000", "project", "10002", "name", "Tom", "lead", "Dick", "assigneetype", "Harry", "description", "I am desc"));
        assertEquals("10000", externalComponent.getId());
        assertEquals("10002", externalComponent.getProjectId());
        assertEquals("Tom", externalComponent.getName());
        assertEquals("Dick", externalComponent.getLead());
        assertEquals("Harry", externalComponent.getAssigneeType());
        assertEquals("I am desc", externalComponent.getDescription());
    }

}
