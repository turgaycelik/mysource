package com.atlassian.jira.plugin.link.confluence.service;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraPropertiesImpl;
import com.atlassian.jira.config.properties.SystemPropertiesAccessor;
import com.atlassian.jira.plugin.link.confluence.Builder;
import com.atlassian.jira.plugin.link.confluence.service.rpc.AbstractConfluenceSaxHandler;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;

public class TestAbstractConfluenceSaxHandler
{
    private static String FILE_ENTITY = "<!DOCTYPE soapenv:Envelope [<!ENTITY readme SYSTEM '/etc/nonexistentfile'>]><x>&readme;</x>";

    private final JiraProperties jiraSystemProperties = new JiraPropertiesImpl(new SystemPropertiesAccessor());


    private static class StringGathererBuilder implements Builder<String>
    {
        private final JiraProperties jiraSystemProperties;
        private StringBuilder sb;

        StringGathererBuilder(final JiraProperties jiraSystemProperties)
        {
            this.jiraSystemProperties = jiraSystemProperties;
            sb = new StringBuilder();
        }

        public void member(String name, String value)
        {
            sb.append(name);
            sb.append(" : ");
            sb.append(value);
            sb.append(jiraSystemProperties.getProperty("line.separator"));
        }

        @Override
        public String build()
        {
            return sb.toString();
        }

        @Override
        public void clear()
        {
            sb = new StringBuilder();
        }
    }

    private static class GatheringConfluenceSaxHandler extends AbstractConfluenceSaxHandler<String,StringGathererBuilder>
    {
        private StringBuilder sb;

        protected GatheringConfluenceSaxHandler(final StringGathererBuilder builder)
        {
            super(builder);
            sb = new StringBuilder();
        }

        @Override
        protected void addMember(NameValuePair member, StringGathererBuilder builder)
        {
            builder.member(member.getName(),member.getValue());
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            sb.append(ch, start, length);
        }

        @Override
        public String toString()
        {
            return sb.toString();
        }
    }

    /**
     * Listen on a socket, flag connection attempts.
     */
    public class HttpAttemptDetector implements Runnable
    {
        private final ServerSocket serverSocket;
        private final AtomicBoolean someoneConnected = new AtomicBoolean(false);

        public HttpAttemptDetector() throws IOException
        {
            serverSocket = new ServerSocket(0);
        }

        public String getUrl()
        {
            return "http://localhost:" + serverSocket.getLocalPort();
        }

        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    Socket s = serverSocket.accept();
                    someoneConnected.set(true);
                    s.close();
                }
                catch (IOException e)
                {
                }
            }
        }

        public boolean wasAttempted()
        {
            return someoneConnected.get();
        }
    }

    @Test
    public void fileEntityIsNotExpanded() throws SAXException, ParserConfigurationException
    {
        final StringGathererBuilder builder = new StringGathererBuilder(jiraSystemProperties);
        final AbstractConfluenceSaxHandler<String,StringGathererBuilder> handler = new GatheringConfluenceSaxHandler(builder);

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser saxParser = factory.newSAXParser();
        final InputStream inputStream = new ByteArrayInputStream(FILE_ENTITY.getBytes());

        try
        {
            saxParser.parse(inputStream, handler);
        }
        catch (IOException e)
        {
            throw new AssertionError("AbstractConfluenceSaxHandler should prevent filesystem being accessed for entity expansion");
        }
    }

    @Test
    public void externalParameterEntityIsNotRead() throws IOException, SAXException, ParserConfigurationException
    {
        final StringGathererBuilder builder = new StringGathererBuilder(jiraSystemProperties);
        final AbstractConfluenceSaxHandler<String,StringGathererBuilder> handler = new GatheringConfluenceSaxHandler(builder);

        HttpAttemptDetector detector = new HttpAttemptDetector();
        new Thread(detector).start();

        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser saxParser = factory.newSAXParser();
        final InputStream inputStream = new ByteArrayInputStream(FILE_ENTITY.replaceAll("/etc/nonexistentfile",detector.getUrl()).getBytes());

        try
        {
            saxParser.parse(inputStream,handler);
        }
        catch (SocketException e)
        {
            throw new AssertionError("AbstractConfluenceSaxHandler should prevent network being accessed for entity expansion");
        }
        assertFalse("AbstractConfluenceSaxHandler should prevent network being accessed for entity expansion", detector.wasAttempted());
    }
}
