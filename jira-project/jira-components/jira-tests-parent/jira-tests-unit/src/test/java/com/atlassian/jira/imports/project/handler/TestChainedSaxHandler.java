package com.atlassian.jira.imports.project.handler;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.taskprogress.EntityTypeTaskProgressProcessor;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestChainedSaxHandler
{

    public static final String DESCRIPTION = "description\n" + "\n" + "that \n" + "\n" + "spans \n" + "\n" + "many \n" + "\n" + "lines";

    public static final String ENVIRONMENT = "environment\n" + "\n" + "that \n" + "\n" + "spans \n" + "\n" + "many \n" + "\n" + "lines";

    public static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<entity-engine-xml>\n" + "    <Action id=\"10000\" issue=\"10000\" author=\"admin\" type=\"comment\" created=\"2008-01-10 16:16:40.55\" updateauthor=\"admin\" updated=\"2008-01-10 16:16:40.55\">\n" + "        <body><![CDATA[I am a comment\n" + "]]></body>\n" + "    </Action>" + "    <Issue id=\"10000\" key=\"MKY-1\" project=\"10001\" reporter=\"fred\" assignee=\"admin\" type=\"1\" summary=\"test\" priority=\"3\" status=\"1\" created=\"2008-01-07 16:24:43.46\" updated=\"2008-01-24 16:03:39.836\" votes=\"0\" timeestimate=\"0\" timespent=\"72000\" workflowId=\"10000\" security=\"10001\">\n" + "        <description><![CDATA[" + DESCRIPTION + "]]></description>\n" + "        <environment><![CDATA[" + ENVIRONMENT + "]]></environment>\n"

    + "    </Issue>" + "<Project id=\"10001\" name=\"monkey\" lead=\"admin\" description=\"project for monkeys\" key=\"MKY\" counter=\"1\" assigneetype=\"2\"/>" + "</entity-engine-xml>";

    @Test
    public void testHandleEntitySimple() throws SAXException
    {
        final ChainedSaxHandler handler = new ChainedSaxHandler()
        {
            protected void handleEntity(final String entityName, final Map attributes) throws ParseException
            {
                assertEquals("TestEntity", entityName);
                assertEquals("attValue1", attributes.get("attName1"));
                assertEquals("attValue2", attributes.get("attName2"));
                assertEquals("hello world", attributes.get("NestedElement"));
            }
        };

        final MockControl mockTopLevelAttributesControl = MockClassControl.createControl(Attributes.class);
        final Attributes mockTopLevelAttributes = (Attributes) mockTopLevelAttributesControl.getMock();

        mockTopLevelAttributes.getLength();
        mockTopLevelAttributesControl.setReturnValue(2, 3);

        mockTopLevelAttributes.getQName(0);
        mockTopLevelAttributesControl.setReturnValue("attName1");

        mockTopLevelAttributes.getValue(0);
        mockTopLevelAttributesControl.setReturnValue("attValue1");

        mockTopLevelAttributes.getQName(1);
        mockTopLevelAttributesControl.setReturnValue("attName2");

        mockTopLevelAttributes.getValue(1);
        mockTopLevelAttributesControl.setReturnValue("attValue2");

        mockTopLevelAttributesControl.replay();

        handler.startDocument();
        handler.startElement(null, null, "entity-engine-xml", null);
        handler.startElement(null, null, "TestEntity", mockTopLevelAttributes);

        // Set a nested element value
        handler.startElement(null, null, "NestedElement", null);
        final String helloWorld = "hello world";
        handler.characters(helloWorld.toCharArray(), 0, helloWorld.length());
        handler.endElement(null, null, "NestedElement");

        handler.endElement(null, null, "TestEntity");
        handler.endElement(null, null, "entity-engine-xml");

        mockTopLevelAttributesControl.verify();
    }

    @Test
    public void testHandlerWithRealXml() throws ParserConfigurationException, SAXException, IOException
    {
        final ChainedSaxHandler handler = new ChainedSaxHandler();
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser saxParser = factory.newSAXParser();
        final InputSource inputSource = new InputSource(new StringReader(XML));
        // Scan the XML
        saxParser.parse(inputSource, handler);

        // Validate we got what we were after
        assertEquals(3, handler.getEntityCount());
    }

    @Test
    public void testHandlerWithRealXmlAndTaskSink() throws ParserConfigurationException, SAXException, IOException
    {
        final AtomicBoolean actionCalled = new AtomicBoolean(false);
        final AtomicBoolean issueCalled = new AtomicBoolean(false);
        final AtomicBoolean projectCalled = new AtomicBoolean(false);
        final TaskProgressSink taskProgressSink = new TaskProgressSink()
        {
            public void makeProgress(final long taskProgress, final String currentSubTask, final String message)
            {
                if ("Processing Action".equals(currentSubTask))
                {
                    actionCalled.set(true);
                    assertEquals(0, taskProgress);
                }
                if ("Processing Issue".equals(currentSubTask))
                {
                    issueCalled.set(true);
                    assertEquals(33, taskProgress);
                }
                if ("Processing Project".equals(currentSubTask))
                {
                    projectCalled.set(true);
                    assertEquals(66, taskProgress);
                }
            }
        };
        final ChainedSaxHandler handler = new ChainedSaxHandler(new EntityTypeTaskProgressProcessor(3, taskProgressSink, new MockI18nBean()))
        {
            // Always call make progress so we can verify through our test
            @Override
            boolean callMakeProgress()
            {
                return true;
            }
        };
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser saxParser = factory.newSAXParser();
        final InputSource inputSource = new InputSource(new StringReader(XML));
        // Scan the XML
        saxParser.parse(inputSource, handler);

        // Validate we got what we were after
        assertEquals(3, handler.getEntityCount());

        // Validate we got what we were after from the make progress
        assertTrue(actionCalled.get());
        assertTrue(issueCalled.get());
        assertTrue(projectCalled.get());
    }

    @Test
    public void testHandlerNoRoot()
    {
        final ChainedSaxHandler handler = new ChainedSaxHandler()
        {
            protected void handleEntity(final String entityName, final Map attributes) throws ParseException
            {}
        };

        // Try to start an element before we reach the root
        try
        {
            handler.startElement(null, null, "Issue", null);
            fail("Should throw a SAX exception for no root");
        }
        catch (final SAXException e)
        {
            // expected
        }

        // Try to end an element before we reach the root
        try
        {
            handler.endElement(null, null, "Issue");
            fail("Should throw a SAX exception for no root");
        }
        catch (final SAXException e)
        {
            // expected
        }

        // Try to end the document while we are in an element
        try
        {
            final MockControl mockTopLevelAttributesControl = MockClassControl.createControl(Attributes.class);
            final Attributes mockTopLevelAttributes = (Attributes) mockTopLevelAttributesControl.getMock();

            mockTopLevelAttributes.getLength();
            mockTopLevelAttributesControl.setReturnValue(0, 1);

            handler.startDocument();
            handler.startElement(null, null, "entity-engine-xml", null);
            handler.startElement(null, null, "TestEntity", mockTopLevelAttributes);
            handler.endDocument();
            fail("Should throw a SAX exception for bad closing");
        }
        catch (final SAXException e)
        {
            // expected
        }
    }

    @Test
    public void testInEndElementWithoutStartElement()
    {
        final ChainedSaxHandler handler = new ChainedSaxHandler()
        {
            protected void handleEntity(final String entityName, final Map attributes) throws ParseException
            {}
        };

        try
        {
            handler.startDocument();
            handler.startElement(null, null, "entity-engine-xml", null);
            handler.endElement(null, null, "TestEntity");
            fail("Should throw a SAX exception for bad closing");
        }
        catch (final SAXException e)
        {
            // expected
        }
    }

    @Test
    public void testHandleEntityThrowsParseException()
    {
        final ChainedSaxHandler handler = new ChainedSaxHandler();
        handler.registerHandler(new ImportEntityHandler()
        {
            public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
            {
                throw new ParseException("Test message");
            }

            public void startDocument()
            {}

            public void endDocument()
            {}
        });

        try
        {
            final MockControl mockTopLevelAttributesControl = MockClassControl.createControl(Attributes.class);
            final Attributes mockTopLevelAttributes = (Attributes) mockTopLevelAttributesControl.getMock();

            mockTopLevelAttributes.getLength();
            mockTopLevelAttributesControl.setReturnValue(0, 1);

            handler.startDocument();
            handler.startElement(null, null, "entity-engine-xml", null);
            handler.startElement(null, null, "TestEntity", mockTopLevelAttributes);
            handler.endElement(null, null, "TestEntity");
            fail("Should throw a SAX exception from handler");
        }
        catch (final SAXException e)
        {
            // expected
            assertTrue(e.getException() instanceof ParseException);
        }

    }

    @Test
    public void testHandlerDelegatesRealXML() throws ParseException, ParserConfigurationException, SAXException, IOException
    {
        final ChainedSaxHandler chainedSaxHandler = new ChainedSaxHandler();
        final TestHandler testHandler1 = new TestHandler();
        final TestHandler testHandler2 = new TestHandler();
        chainedSaxHandler.registerHandler(testHandler1);
        chainedSaxHandler.registerHandler(testHandler2);

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser saxParser = factory.newSAXParser();
        final InputSource inputSource = new InputSource(new StringReader(TestChainedSaxHandler.XML));
        // Scan the XML
        saxParser.parse(inputSource, chainedSaxHandler);

        // Validate we got what we were after
        assertEquals(3, testHandler1.entityMapAndValues.size());
        assertTrue(testHandler1.entityMapAndValues.containsKey("Action"));
        assertTrue(testHandler1.entityMapAndValues.containsKey("Issue"));
        assertTrue(testHandler1.entityMapAndValues.containsKey("Project"));
        assertEquals(8, ((Map) testHandler1.entityMapAndValues.get("Action")).size());
        assertEquals(18, ((Map) testHandler1.entityMapAndValues.get("Issue")).size());
        assertEquals(TestChainedSaxHandler.DESCRIPTION, ((Map) testHandler1.entityMapAndValues.get("Issue")).get("description"));
        assertEquals(TestChainedSaxHandler.ENVIRONMENT, ((Map) testHandler1.entityMapAndValues.get("Issue")).get("environment"));

        assertEquals(3, testHandler2.entityMapAndValues.size());
        assertTrue(testHandler2.entityMapAndValues.containsKey("Action"));
        assertTrue(testHandler2.entityMapAndValues.containsKey("Issue"));
        assertTrue(testHandler2.entityMapAndValues.containsKey("Project"));
        assertEquals(8, ((Map) testHandler2.entityMapAndValues.get("Action")).size());
        assertEquals(18, ((Map) testHandler2.entityMapAndValues.get("Issue")).size());
        assertEquals(TestChainedSaxHandler.DESCRIPTION, ((Map) testHandler2.entityMapAndValues.get("Issue")).get("description"));
        assertEquals(TestChainedSaxHandler.ENVIRONMENT, ((Map) testHandler2.entityMapAndValues.get("Issue")).get("environment"));
    }

    private class TestHandler implements ImportEntityHandler
    {
        public Map entityMapAndValues = new HashMap();

        public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
        {
            entityMapAndValues.put(entityName, attributes);
        }

        public void startDocument()
        {}

        public void endDocument()
        {}
    }
}
