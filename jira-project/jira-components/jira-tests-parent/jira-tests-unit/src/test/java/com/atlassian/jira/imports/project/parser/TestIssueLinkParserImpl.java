package com.atlassian.jira.imports.project.parser;

import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLink;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestIssueLinkParserImpl
{
    @Test
    public void testParse() throws ParseException
    {
        IssueLinkParserImpl issueLinkParser = new IssueLinkParserImpl();
        //     <IssueLink id="10010" linktype="10001" source="10030" destination="10031" sequence="0"/>
        ExternalLink externalLink = issueLinkParser.parse(EasyMap.build("id", "10", "linktype", "10030", "source", "30", "destination", "40", "sequence", "1"));
        assertEquals("10", externalLink.getId());
        assertEquals("10030", externalLink.getLinkType());
        assertEquals("30", externalLink.getSourceId());
        assertEquals("40", externalLink.getDestinationId());
        assertEquals("1", externalLink.getSequence());
    }

    @Test
    public void testParseNoId()
    {
        IssueLinkParserImpl issueLinkParser = new IssueLinkParserImpl();
        //     <IssueLink id="10010" linktype="10001" source="10030" destination="10031" sequence="0"/>
        try
        {
            issueLinkParser.parse(EasyMap.build("linktype", "10030", "source", "30", "destination", "40", "sequence", "1"));
            fail("Should throw ParseException.");
        }
        catch (ParseException e)
        {
            // Expected.
            assertEquals("No 'id' field for IssueLink.", e.getMessage());
        }
    }

    @Test
    public void testParseNoLinktype()
    {
        IssueLinkParserImpl issueLinkParser = new IssueLinkParserImpl();
        //     <IssueLink id="10010" linktype="10001" source="10030" destination="10031" sequence="0"/>
        try
        {
            issueLinkParser.parse(EasyMap.build("id", "10", "source", "30", "destination", "40", "sequence", "1"));
            fail("Should throw ParseException.");
        }
        catch (ParseException e)
        {
            // Expected.
            assertEquals("No 'linktype' field for IssueLink 10.", e.getMessage());
        }
    }

    @Test
    public void testParseNoSequence() throws ParseException
    {
        IssueLinkParserImpl issueLinkParser = new IssueLinkParserImpl();
        //     <IssueLink id="10010" linktype="10001" source="10030" destination="10031" sequence="0"/>
        ExternalLink externalLink = issueLinkParser.parse(EasyMap.build("id", "10", "linktype", "10030", "source", "30", "destination", "40"));
        // This is allowed - sequence is optional.
        assertEquals("10", externalLink.getId());
        assertEquals("10030", externalLink.getLinkType());
        assertEquals("30", externalLink.getSourceId());
        assertEquals("40", externalLink.getDestinationId());
    }

    @Test
    public void testParseNoSourceOrDestination() throws ParseException
    {
        IssueLinkParserImpl issueLinkParser = new IssueLinkParserImpl();
        //     <IssueLink id="10010" linktype="10001" source="10030" destination="10031" sequence="0"/>
        // A link without source or destination is useless - but we have seen this in real world data (JAC) - test we can handle it.
        ExternalLink externalLink = issueLinkParser.parse(EasyMap.build("id", "10", "linktype", "10030"));
        // This is allowed - sequence is optional.
        assertEquals("10", externalLink.getId());
        assertEquals("10030", externalLink.getLinkType());
        assertEquals(null, externalLink.getSourceId());
        assertEquals(null, externalLink.getDestinationId());
    }

    @Test
    public void testParseNull() throws ParseException
    {
        try
        {
            new IssueLinkParserImpl().parse(null);
            fail("Uncool.");
        }
        catch (IllegalArgumentException e)
        {
            // Cool.
        }
    }

    @Test
    public void testGetEntityRepresentation()
    {
        IssueLinkParserImpl issueLinkParser = new IssueLinkParserImpl();
        ExternalLink issueLink = new ExternalLink();
        issueLink.setId("12");
        issueLink.setLinkType("23");
        issueLink.setSourceId("88");
        issueLink.setDestinationId("99");
        issueLink.setSequence("3");

        EntityRepresentation entityRepresentation = issueLinkParser.getEntityRepresentation(issueLink);
        assertEquals("IssueLink", entityRepresentation.getEntityName());
        Map values = entityRepresentation.getEntityValues();
        assertEquals("12", values.get("id"));
        assertEquals("23", values.get("linktype"));
        assertEquals("88", values.get("source"));
        assertEquals("99", values.get("destination"));
        assertEquals("3", values.get("sequence"));
        assertEquals(5, values.size());
    }
}
