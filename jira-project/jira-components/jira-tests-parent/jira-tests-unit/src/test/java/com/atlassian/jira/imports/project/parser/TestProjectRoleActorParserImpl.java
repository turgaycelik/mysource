package com.atlassian.jira.imports.project.parser;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestProjectRoleActorParserImpl
{
    ProjectRoleActorParserImpl projectRoleActorParser = new ProjectRoleActorParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            projectRoleActorParser.parse(null);
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
            // <ProjectRoleActor id="10010" pid="10001" projectroleid="10000" roletype="atlassian-group-role-actor" roletypeparameter="jira-users"/>
            final Map attributes = EasyMap.build("pid", "10001", "projectroleid", "3", "roletype", "atlassian-group-role-actor", "roletypeparameter", "jira-users");
            projectRoleActorParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingProjectRoleId()
    {
        try
        {
            // <ProjectRoleActor id="10010" pid="10001" projectroleid="10000" roletype="atlassian-group-role-actor" roletypeparameter="jira-users"/>
            final Map attributes = EasyMap.build("id", "10000", "pid", "10001", "roletype", "atlassian-group-role-actor", "roletypeparameter", "jira-users");
            projectRoleActorParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingRoleType()
    {
        try
        {
            // <ProjectRoleActor id="10010" pid="10001" projectroleid="10000" roletype="atlassian-group-role-actor" roletypeparameter="jira-users"/>
            final Map attributes = EasyMap.build("id", "10000", "pid", "10001", "projectroleid", "3", "roletypeparameter", "jira-users");
            projectRoleActorParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingRoleTypeParameter()
    {
        try
        {
            // <ProjectRoleActor id="10010" pid="10001" projectroleid="10000" roletype="atlassian-group-role-actor" roletypeparameter="jira-users"/>
            final Map attributes = EasyMap.build("id", "10000", "pid", "10001", "projectroleid", "3", "roletype", "atlassian-group-role-actor");
            projectRoleActorParser.parse(attributes);
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
        // <ProjectRoleActor id="10010" pid="10001" projectroleid="10000" roletype="atlassian-group-role-actor" roletypeparameter="jira-users"/>
        final Map attributes = EasyMap.build("id", "10000", "projectroleid", "3", "roletype", "atlassian-group-role-actor", "roletypeparameter", "jira-users");
        ExternalProjectRoleActor projectRoleActor = projectRoleActorParser.parse(attributes);
        assertEquals("10000", projectRoleActor.getId());
        assertEquals(null, projectRoleActor.getProjectId());
        assertEquals("3", projectRoleActor.getRoleId());
        assertEquals("atlassian-group-role-actor", projectRoleActor.getRoleType());
        assertEquals("jira-users", projectRoleActor.getRoleActor());
    }

    @Test
    public void testParseFull() throws ParseException
    {
        // <ProjectRoleActor id="10010" pid="10001" projectroleid="10000" roletype="atlassian-group-role-actor" roletypeparameter="jira-users"/>
        final Map attributes = EasyMap.build("id", "10000", "pid", "10001", "projectroleid", "3", "roletype", "atlassian-group-role-actor", "roletypeparameter", "jira-users");

        ExternalProjectRoleActor projectRoleActor = projectRoleActorParser.parse(attributes);
        assertEquals("10000", projectRoleActor.getId());
        assertEquals("10001", projectRoleActor.getProjectId());
        assertEquals("3", projectRoleActor.getRoleId());
        assertEquals("atlassian-group-role-actor", projectRoleActor.getRoleType());
        assertEquals("jira-users", projectRoleActor.getRoleActor());
    }

    @Test
    public void testGetEntityRepresentation()
    {
        ProjectRoleActorParserImpl projectRoleActorParser = new ProjectRoleActorParserImpl();
        ExternalProjectRoleActor projectRoleActor = new ExternalProjectRoleActor("12", "13", "14", "Dog", "Pluto");

        EntityRepresentation entityRepresentation = projectRoleActorParser.getEntityRepresentation(projectRoleActor);
        assertEquals("ProjectRoleActor", entityRepresentation.getEntityName());
        Map values = entityRepresentation.getEntityValues();
        assertEquals("12", values.get("id"));
        assertEquals("13", values.get("pid"));
        assertEquals("14", values.get("projectroleid"));
        assertEquals("Dog", values.get("roletype"));
        assertEquals("Pluto", values.get("roletypeparameter"));
        assertEquals(5, values.size());
    }
}
