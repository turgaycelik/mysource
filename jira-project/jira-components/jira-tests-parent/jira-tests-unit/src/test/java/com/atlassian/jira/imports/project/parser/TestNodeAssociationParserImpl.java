package com.atlassian.jira.imports.project.parser;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestNodeAssociationParserImpl
{
    NodeAssociationParser nodeAssociationParser = new NodeAssociationParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            nodeAssociationParser.parse(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected!
        }
    }
//    <NodeAssociation sourceNodeId="10001" sourceNodeEntity="Issue" sinkNodeId="10001" sinkNodeEntity="Component" associationType="IssueComponent"/>

    @Test
    public void testParseMissingSourceId()
    {
        try
        {
            final Map attributes = EasyMap.build("sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Version", "associationType", "FixVersion");
            nodeAssociationParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingSourceEntity()
    {
        try
        {
            final Map attributes = EasyMap.build("sourceNodeId", "10001", "sinkNodeId", "20001", "sinkNodeEntity", "Version", "associationType", "FixVersion");
            nodeAssociationParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingSinkNodeId()
    {
        try
        {
            final Map attributes = EasyMap.build("sourceNodeId", "10001", "sourceNodeEntity", "Issue", "sinkNodeEntity", "Version", "associationType", "FixVersion");
            nodeAssociationParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingSinkNodeEntity()
    {
        try
        {
            final Map attributes = EasyMap.build("sourceNodeId", "10001", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "associationType", "FixVersion");
            nodeAssociationParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingAssociationType()
    {
        try
        {
            final Map attributes = EasyMap.build("sourceNodeId", "10001", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Version");
            nodeAssociationParser.parse(attributes);
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseNodeAssocation() throws ParseException
    {
        final Map attributes = EasyMap.build("sourceNodeId", "10001", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Version", "associationType", "FixVersion");
        final ExternalNodeAssociation nodeAssociation = nodeAssociationParser.parse(attributes);
        assertEquals("10001", nodeAssociation.getSourceNodeId());
        assertEquals("Issue", nodeAssociation.getSourceNodeEntity());
        assertEquals("20001", nodeAssociation.getSinkNodeId());
        assertEquals("Version", nodeAssociation.getSinkNodeEntity());
        assertEquals("FixVersion", nodeAssociation.getAssociationType());
    }

    @Test
    public void testGetEntityRepresentation() throws Exception
    {
        ExternalNodeAssociation externalNodeAssociation = new ExternalNodeAssociation("10001", "Issue", "20001", "Version", "FixVersion");
        final EntityRepresentation representation = nodeAssociationParser.getEntityRepresentation(externalNodeAssociation);
        assertEquals(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, representation.getEntityName());
        assertEquals("10001", representation.getEntityValues().get("sourceNodeId"));
        assertEquals("Issue", representation.getEntityValues().get("sourceNodeEntity"));
        assertEquals("20001", representation.getEntityValues().get("sinkNodeId"));
        assertEquals("Version", representation.getEntityValues().get("sinkNodeEntity"));
        assertEquals("FixVersion", representation.getEntityValues().get("associationType"));
    }

}
