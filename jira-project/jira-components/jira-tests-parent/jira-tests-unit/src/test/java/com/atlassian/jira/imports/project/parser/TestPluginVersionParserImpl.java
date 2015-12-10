package com.atlassian.jira.imports.project.parser;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.plugin.PluginVersion;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestPluginVersionParserImpl
{
    PluginVersionParserImpl pluginVersionParser = new PluginVersionParserImpl();

    @Test
    public void testParseNullAttributeMap() throws ParseException
    {
        try
        {
            pluginVersionParser.parse(null);
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
            pluginVersionParser.parse(EasyMap.build("name", "Admin Menu Sections", "key", "jira.webfragments.admin", "version", "1.0"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingKey() throws ParseException
    {
        try
        {
            pluginVersionParser.parse(EasyMap.build("id", "10000", "name", "Admin Menu Sections", "version", "1.0"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingVersion() throws ParseException
    {
        try
        {
            pluginVersionParser.parse(EasyMap.build("id", "10000", "name", "Admin Menu Sections", "key", "jira.webfragments.admin"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseMissingName() throws ParseException
    {
        try
        {
            pluginVersionParser.parse(EasyMap.build("id", "10000", "key", "jira.webfragments.admin", "version", "1.0"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParseBadId() throws ParseException
    {
        try
        {
            pluginVersionParser.parse(EasyMap.build("id", "10000-string", "name", "Admin Menu Sections", "key", "jira.webfragments.admin", "version", "1.0"));
            fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            // Expected
        }
    }

    @Test
    public void testParse() throws ParseException
    {
        PluginVersion pluginVersion = pluginVersionParser.parse(EasyMap.build("id", "10000", "name", "Admin Menu Sections", "key", "jira.webfragments.admin", "version", "1.0"));
        assertEquals(new Long("10000"), pluginVersion.getId());
        assertEquals("jira.webfragments.admin", pluginVersion.getKey());
        assertEquals("Admin Menu Sections", pluginVersion.getName());
        assertEquals("1.0", pluginVersion.getVersion());
    }
    
}
