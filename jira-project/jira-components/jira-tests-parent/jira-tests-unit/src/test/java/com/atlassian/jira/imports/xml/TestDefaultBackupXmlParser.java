package com.atlassian.jira.imports.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.handler.ChainedSaxHandler;
import com.atlassian.jira.imports.project.handler.ImportEntityHandler;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestDefaultBackupXmlParser
{
    public static final String FS = File.separator;

    @Test
    public void testParseBackupXml() throws IOException, ParserConfigurationException, SAXException
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_IMPORT_CLEAN_XML)), Boolean.FALSE);
        DefaultBackupXmlParser parser = new DefaultBackupXmlParser((ApplicationProperties) mockApplicationProperties.proxy());

        final Map entityMap = new HashMap();
        ChainedSaxHandler handler = new ChainedSaxHandler();
        handler.registerHandler(new ImportEntityHandler()
        {
            public void handleEntity(String entityName, Map attributes) throws ParseException
            {
                entityMap.put(entityName, attributes);
            }

            public void startDocument()
            {
            }

            public void endDocument()
            {
            }
        });

        parser.parseBackupXml(getFilePath("TestBackupParser.xml"), handler);

        // Verify the stuff in our map
        assertEquals(3, entityMap.size());
        assertTrue(entityMap.containsKey("Action"));
        assertTrue(entityMap.containsKey("Issue"));
        assertTrue(entityMap.containsKey("Project"));
        assertEquals(7, ((Map) entityMap.get("Action")).size());
        assertEquals(16, ((Map) entityMap.get("Issue")).size());
        assertEquals(3, handler.getEntityCount());
    }

    @Test
    public void testParseBackupXmlWithNotValidCharacters()
            throws IOException, ParserConfigurationException, SAXException
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_IMPORT_CLEAN_XML)), Boolean.TRUE);
        DefaultBackupXmlParser parser = new DefaultBackupXmlParser((ApplicationProperties) mockApplicationProperties.proxy());

        final Map<String, Map<String, String>> entityMap = new HashMap();
        ChainedSaxHandler handler = new ChainedSaxHandler();
        handler.registerHandler(new ImportEntityHandler()
        {
            public void handleEntity(String entityName, Map attributes) throws ParseException
            {
                entityMap.put(entityName, attributes);
            }

            public void startDocument()
            {
            }

            public void endDocument()
            {
            }
        });

        parser.parseBackupXml(getFilePath("TestUnescapedCharactersBackupParser.xml"), handler);

        // Verify the stuff in our map
        assertEquals(1, entityMap.size());
        assertTrue(entityMap.containsKey("Project"));
        assertTrue(entityMap.get("Project").containsKey("description"));
        assertTrue(entityMap.get("Project").containsKey("name"));
        String description = entityMap.get("Project").get("description");
        String name = entityMap.get("Project").get("name");
        assertEquals(33, description.length());
        for (int i = 0; i < description.length(); i++)
        {
            if (i == 9 || i == 10 || i== 13 || i >= 32) {
                assertEquals('X', description.charAt(i));
                assertEquals('X', name.charAt(i));
            } else {
                assertEquals(i, description.charAt(i));
                assertEquals(i, name.charAt(i));
            }
        }


    }

    @Test
    public void testParseBackupZip() throws IOException, ParserConfigurationException, SAXException
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_IMPORT_CLEAN_XML)), Boolean.FALSE);
        DefaultBackupXmlParser parser = new DefaultBackupXmlParser((ApplicationProperties) mockApplicationProperties.proxy());

        final Map entityMap = new HashMap();
        ChainedSaxHandler handler = new ChainedSaxHandler();
        handler.registerHandler(new ImportEntityHandler()
        {
            public void handleEntity(String entityName, Map attributes) throws ParseException
            {
                entityMap.put(entityName, attributes);
            }

            public void startDocument()
            {
            }

            public void endDocument()
            {
            }

        });

        parser.parseBackupXml(getFilePath("TestBackupParser.zip"), handler);

        // Verify the stuff in our map
        assertEquals(3, entityMap.size());
        assertTrue(entityMap.containsKey("Action"));
        assertTrue(entityMap.containsKey("Issue"));
        assertTrue(entityMap.containsKey("Project"));
        assertEquals(7, ((Map) entityMap.get("Action")).size());
        assertEquals(16, ((Map) entityMap.get("Issue")).size());
        assertEquals(3, handler.getEntityCount());
    }

    @Test
    public void testSaxExceptionThrown() throws IOException, ParserConfigurationException
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_IMPORT_CLEAN_XML)), Boolean.FALSE);
        DefaultBackupXmlParser parser = new DefaultBackupXmlParser((ApplicationProperties) mockApplicationProperties.proxy());

        ChainedSaxHandler handler = new ChainedSaxHandler();
        handler.registerHandler(new ImportEntityHandler()
        {
            public void handleEntity(String entityName, Map<String, String> attributes) throws ParseException
            {
                throw new ParseException("F**k off");
            }

            public void startDocument()
            {
            }

            public void endDocument()
            {
            }
        });

        try
        {
            parser.parseBackupXml(getFilePath("TestBackupParser.zip"), handler);
        }
        catch (SAXException e)
        {
            // expected
        }
    }

    @Test
    public void testFileNotFound() throws ParserConfigurationException, SAXException, IOException
    {
        DefaultBackupXmlParser parser = new DefaultBackupXmlParser(null);
        try
        {
            parser.parseBackupXml("/iamafilethatwillneverexist", null);
            fail();
        }
        catch (FileNotFoundException e)
        {
            // expected
        }
    }

    private String getFilePath(String fileName)
    {
        return new File(this.getClass().getResource("/" + this.getClass().getName().replace('.', '/') + ".class").getFile()).getParent() + "/" + fileName;
    }
}
