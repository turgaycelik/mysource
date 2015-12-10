package com.atlassian.jira.imports.project.parser;

import java.util.Map;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLabel;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestLabelParserImpl
{
    @Test
    public void testParse()
    {
        LabelParserImpl parser = new LabelParserImpl();

        Map attributes = MapBuilder.newBuilder().add("id", "10000").add("issue", "10001").add("fieldid", "12000").add("label","alabel").toMap();
        ExternalLabel externalLabel = null;
        try
        {
            externalLabel = parser.parse(attributes);
        }
        catch (ParseException e)
        {
            fail("Exception occurred!");
        }
        ExternalLabel expected = new ExternalLabel();
        expected.setId("10000");
        expected.setIssueId("10001");
        expected.setCustomFieldId("12000");
        expected.setLabel("alabel");
        assertEquals(expected, externalLabel);

        attributes = MapBuilder.newBuilder().add("issue", "10001").add("fieldid", "12000").add("label","alabel").toMap();
        try
        {
            parser.parse(attributes);
            fail("Should have gotten exception!");
        }
        catch (ParseException e)
        {
        }
        attributes = MapBuilder.newBuilder().add("id", "10001").add("fieldid", "12000").add("label","alabel").toMap();
        try
        {
            parser.parse(attributes);
            fail("Should have gotten exception!");
        }
        catch (ParseException e)
        {
        }
        attributes = MapBuilder.newBuilder().add("issue", "10001").add("fieldid", "12000").add("id","10000").toMap();
        try
        {
            parser.parse(attributes);
            fail("Should have gotten exception!");
        }
        catch (ParseException e)
        {
        }
        attributes = MapBuilder.newBuilder().add("id", "10000").add("issue", "10001").add("fieldid", "12000").add("label","bad label").toMap();
        try
        {
            parser.parse(attributes);
            fail("Should have gotten exception!");
        }
        catch (ParseException e)
        {
        }
    }
    
    @Test
    public void testGetEntityRepresentation()
    {
        LabelParserImpl parser = new LabelParserImpl();
        ExternalLabel externalLabel = new ExternalLabel();
        externalLabel.setId("10000");
        externalLabel.setIssueId("10001");
        externalLabel.setCustomFieldId("12000");
        externalLabel.setLabel("alabel");
        final EntityRepresentation entity = parser.getEntityRepresentation(externalLabel);
        Map expected = MapBuilder.newBuilder().add("id", "10000").add("issue", "10001").add("fieldid", "12000").add("label","alabel").toMap();
        assertEquals(expected, entity.getEntityValues());
        assertEquals("Label", entity.getEntityName());

    }

}
