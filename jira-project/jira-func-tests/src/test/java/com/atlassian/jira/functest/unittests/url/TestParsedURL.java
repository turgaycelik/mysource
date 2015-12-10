package com.atlassian.jira.functest.unittests.url;

import com.atlassian.jira.functest.framework.util.url.ParsedURL;
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 */
public class TestParsedURL extends TestCase
{
    public void testBasicParse()
    {
        ParsedURL url = new ParsedURL("http://localhost:666/path1/path2?p1=v1&p2=v2");
        assertEquals("http", url.getProtocol());
        assertEquals("localhost", url.getHost());
        assertEquals(666, url.getPort());
        assertEquals("/path1/path2", url.getPath());
        assertEquals("/path1/path2?p1=v1&p2=v2", url.getFile());
        assertEquals("p1=v1&p2=v2", url.getQuery());
    }

    public void testNoPort()
    {
        ParsedURL url = new ParsedURL("http://localhost/path1/path2?p1=v1&p2=v2");
        assertEquals("http", url.getProtocol());
        assertEquals("localhost", url.getHost());
        assertEquals(-1, url.getPort());
        assertEquals("/path1/path2", url.getPath());
        assertEquals("/path1/path2?p1=v1&p2=v2", url.getFile());
        assertEquals("p1=v1&p2=v2", url.getQuery());
    }

    public void testNoProtocol()
    {
        ParsedURL url = new ParsedURL("localhost/path1/path2?p1=v1&p2=v2");
        assertEquals("http", url.getProtocol());
        assertEquals("", url.getHost());
        assertEquals(-1, url.getPort());
        assertEquals("/localhost/path1/path2", url.getPath());
        assertEquals("/localhost/path1/path2?p1=v1&p2=v2", url.getFile());
        assertEquals("p1=v1&p2=v2", url.getQuery());
    }

    public void testNoParameters()
    {
        ParsedURL url = new ParsedURL("http://localhost/path1/path2");
        assertEquals("http", url.getProtocol());
        assertEquals("localhost", url.getHost());
        assertEquals(-1, url.getPort());
        assertEquals("/path1/path2", url.getPath());
        assertEquals("/path1/path2", url.getFile());
        assertEquals("", url.getQuery());
    }

    public void testMultipleParameters()
    {
        ParsedURL url = new ParsedURL("http://localhost/path1/path2?p2=v2b&p2=v2a&p1=v1");

        final Map<String, List<String>> parameters = url.getMultiQueryParameters();
        assertTrue(parameters.containsKey("p1"));
        assertTrue(parameters.containsKey("p2"));
        assertEquals(1, parameters.get("p1").size());
        assertEquals(2, parameters.get("p2").size());
        assertTrue(parameters.get("p2").contains("v2a"));
        assertTrue(parameters.get("p2").contains("v2b"));

        // assert ordering
        final Iterator<String> keyIt = parameters.keySet().iterator();
        assertEquals("p1", keyIt.next());
        assertEquals("p2", keyIt.next());
    }

    public void testSimpleParameters()
    {
        ParsedURL url = new ParsedURL("http://localhost/path1/path2?p2=v2b&p2=v2a&p3=v3&p1=v1");

        final Map<String, String> parameters = url.getQueryParameters();
        assertTrue(parameters.containsKey("p1"));
        assertTrue(parameters.containsKey("p2"));
        assertTrue(parameters.containsKey("p3"));
        assertEquals("v1", parameters.get("p1"));
        assertEquals("v2a", parameters.get("p2"));
        assertEquals("v3", parameters.get("p3"));

        // assert ordering
        final Iterator<String> keyIt = parameters.keySet().iterator();
        assertEquals("p1", keyIt.next());
        assertEquals("p2", keyIt.next());
        assertEquals("p3", keyIt.next());
    }
}
