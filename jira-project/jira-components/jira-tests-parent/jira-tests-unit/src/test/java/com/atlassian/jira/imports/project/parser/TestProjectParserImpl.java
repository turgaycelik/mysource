package com.atlassian.jira.imports.project.parser;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestProjectParserImpl
{
    ProjectParserImpl projectParser = new ProjectParserImpl();

    @Test
    public void testParseProjectNullAttributeMap() throws ParseException
    {
        try
        {
            projectParser.parseProject(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected!
        }
    }

    @Test
    public void testParseProjectMissingId()
    {
        try
        {
            projectParser.parseProject(EasyMap.build("key", "HOMO"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseProjectMissingKey() throws ParseException
    {
        try
        {
            projectParser.parseProject(EasyMap.build("id", "qwe"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseProjectMinimal() throws ParseException
    {
        ExternalProject project = projectParser.parseProject(EasyMap.build("id", "10003", "key", "HSP"));
        assertEquals("10003", project.getId());
        assertEquals("HSP", project.getKey());
    }

    @Test
    public void testParseProjectFullMap() throws ParseException
    {
        final Map attributes = EasyMap.build("id", "10003", "key", "HSP");
        attributes.put("lead", "Anton");
        attributes.put("name", "Homosapien");
        attributes.put("url", "http://homosapien");
        attributes.put("description", "Stuff");
        attributes.put("assigneetype", "3");
        attributes.put("counter", "12");
        ExternalProject project = projectParser.parseProject(attributes);
        assertEquals("10003", project.getId());
        assertEquals("HSP", project.getKey());
        assertEquals("Anton", project.getLead());
        assertEquals("Homosapien", project.getName());
        assertEquals("http://homosapien", project.getUrl());
        assertEquals("Stuff", project.getDescription());
        assertEquals("3", project.getAssigneeType());
        assertEquals("12", project.getCounter());
    }

    @Test
    public void testParseOtherSimple() throws ParseException
    {
        ProjectParser parser = new ProjectParserImpl();
        // Send OSProperty for two projects
        // <OSPropertyEntry id="10143" entityName="Project" entityId="10000" propertyKey="jira.project.email.sender" type="5"/>
        parser.parseOther("OSPropertyEntry", EasyMap.build("id", "10143", "entityName", "Project", "entityId", "10000",
                "propertyKey", "jira.project.email.sender", "type", "5"));
        parser.parseOther("OSPropertyEntry", EasyMap.build("id", "10144", "entityName", "Project", "entityId", "10001",
                "propertyKey", "jira.project.email.sender", "type", "5"));

        // Now send the Property Values
        //     <OSPropertyString id="10143" value="dude@example.com"/>
        parser.parseOther("OSPropertyString", EasyMap.build("id", "10143", "value", "dude@example.com"));
        parser.parseOther("OSPropertyString", EasyMap.build("id", "10144", "value", "peter.griffin@family.guy"));

        // In order to see what the parser is doing, we need to parse the actual projects now.
        // First Project
        ExternalProject project = parser.parseProject(EasyMap.build("id", "10000", "key", "HSP"));
        assertEquals("dude@example.com", project.getEmailSender());
        // Second project
        project = parser.parseProject(EasyMap.build("id", "10001", "key", "MKY"));
        assertEquals("peter.griffin@family.guy", project.getEmailSender());
        // And now a project where the email sender was not set.
        project = parser.parseProject(EasyMap.build("id", "10002", "key", "RAT"));
        assertNull(project.getEmailSender());
    }

    @Test
    public void testParseOtherWithNoise() throws ParseException
    {
        // During this test, we will send OSProperty mail.sender properties for 2 projects, but include "noise", that is
        // other OSProperties that try to trick the parser by being "similar" to the real values

        ProjectParser parser = new ProjectParserImpl();
        // Send OSProperty for two projects
        // <OSPropertyEntry id="10143" entityName="Project" entityId="10000" propertyKey="jira.project.email.sender" type="5"/>
        parser.parseOther("OSPropertyEntry", EasyMap.build("id", "10143", "entityName", "Project", "entityId", "10000",
                "propertyKey", "jira.project.email.sender", "type", "5"));
        parser.parseOther("OSPropertyEntry", EasyMap.build("id", "10144", "entityName", "Project", "entityId", "10001",
                "propertyKey", "jira.project.email.sender", "type", "5"));
        // Now send some noise
        parser.parseOther("OSPropertyEntry", EasyMap.build("id", "10145", "entityName", "NotProject", "entityId", "10000",
                "propertyKey", "jira.project.email.sender", "type", "5"));
        parser.parseOther("OSPropertyEntry", EasyMap.build("id", "10146", "entityName", "Project", "entityId", "10001",
                "propertyKey", "jira.project.email.rubbish", "type", "5"));

        // Now send the Property Values
        //     <OSPropertyString id="10143" value="dude@example.com"/>
        parser.parseOther("OSPropertyString", EasyMap.build("id", "10143", "value", "dude@example.com"));
        parser.parseOther("OSPropertyString", EasyMap.build("id", "10144", "value", "peter.griffin@family.guy"));
        // Now send some noise
        parser.parseOther("OSPropertyString", EasyMap.build("id", "10145", "value", "haha"));
        parser.parseOther("OSPropertyString", EasyMap.build("id", "10146", "value", "lol"));


        // In order to see what the parser is doing, we need to parse the actual projects now.
        // First Project
        ExternalProject project = parser.parseProject(EasyMap.build("id", "10000", "key", "HSP"));
        assertEquals("dude@example.com", project.getEmailSender());
        // Second project
        project = parser.parseProject(EasyMap.build("id", "10001", "key", "MKY"));
        assertEquals("peter.griffin@family.guy", project.getEmailSender());
        // And now a project where the email sender was not set.
        project = parser.parseProject(EasyMap.build("id", "10002", "key", "RAT"));
        assertNull(project.getEmailSender());
    }

    @Test
    public void testParseOtherNullElementName()
    {
        try
        {
            projectParser.parseOther(null, new HashMap());
            fail("Should throw IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected.
        }
    }

}
