package com.atlassian.jira.functest.unittests.locator;

import com.atlassian.jira.functest.framework.locator.AggregateLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.util.text.TextKit;
import com.atlassian.jira.functest.unittests.mocks.MockWebServer;
import com.atlassian.jira.functest.unittests.mocks.MockWebTester;
import com.atlassian.jira.functest.unittests.mocks.XmlParserHelper;
import com.meterware.httpunit.WebTable;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A Test for Locators
 *
 * @since v3.13
 */
public class TestLocator extends TestCase
{
    private static final String HTML_START = "<html><body>";
    private static final String HTML_END = "</body></html>";
    private static final String HTML_TEXT_SIMPLE = HTML_START + "<span id=\"s1\">some text</span><div>Some text <b> with bold tags</b>   and spaces </div>" + HTML_END;

    public static Test suite()
    {
        TestSuite testSuite = new TestSuite(TestLocator.class);
        return new TestSetup(testSuite)
        {
            protected void tearDown() throws Exception
            {
                MockWebServer.getInstance().stop();
            }
        };
    }

    public void testXpathLocator() throws Exception
    {
        XPathLocator xpathLocator;
        Node[] nodes;
        String text;

        xpathLocator = newXPathLocator(HTML_TEXT_SIMPLE, "//span");
        nodes = xpathLocator.getNodes();
        assertNotNull(nodes);
        assertEquals(1, nodes.length);
        assertTrue(xpathLocator.hasNodes());
        assertEquals("span", ((Element) nodes[0]).getTagName());

        text = xpathLocator.getHTML();
        assertNotNull(text);
        assertEquals("<span id=\"s1\">some text</span>", text);

        text = xpathLocator.getText();
        assertNotNull(text);
        assertEquals("some text", text);


        xpathLocator = newXPathLocator(HTML_TEXT_SIMPLE, "//div");
        nodes = xpathLocator.getNodes();
        assertNotNull(nodes);
        assertEquals(1, nodes.length);
        assertTrue(xpathLocator.hasNodes());
        assertEquals("div", ((Element) nodes[0]).getTagName());
        
        text = xpathLocator.getHTML();
        assertNotNull(text);
        assertEquals("<div>Some text <b> with bold tags</b>   and spaces </div>", text);

        text = xpathLocator.getHTML(nodes[0]);
        assertNotNull(text);
        assertEquals("<div>Some text <b> with bold tags</b>   and spaces </div>", text);

        text = xpathLocator.getText();
        assertNotNull(text);
        assertEquals("Some text with bold tags and spaces", text);

        text = xpathLocator.getText(nodes[0]);
        assertNotNull(text);
        assertEquals("Some text with bold tags and spaces", text);

        text = xpathLocator.getRawText();
        assertNotNull(text);
        assertEquals("Some text  with bold tags   and spaces ", text);

        text = xpathLocator.getRawText(nodes[0]);
        assertNotNull(text);
        assertEquals("Some text  with bold tags   and spaces ", text);
    }

    public void testWebPageLocator() throws Exception
    {
        Locator locator;
        Node[] nodes;
        String text;

        MockWebServer webServer = MockWebServer.getInstance();
        webServer.addPage("/", HTML_TEXT_SIMPLE);

        WebTester webTester = new MockWebTester(webServer);

        // test the collapser wrapping this xpath locator
        locator = new WebPageLocator(webTester);
        nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(1, nodes.length);
        assertTrue(locator.hasNodes());
        assertEquals("html", ((Element) nodes[0]).getTagName());
        text = locator.getHTML();
        assertNotNull(text);
        assertTrue(TextKit.containsTextSequence(text, new String[] { "<span id=\"s1\">some text</span><div>Some text <b> with bold tags</b>   and spaces </div>" }));


        text = locator.getText();
        assertNotNull(text);
        assertEquals(text, "some textSome text with bold tags and spaces");
    }

    public void testAggregate() throws Exception
    {
        XPathLocator xpathLocator1 = newXPathLocator(HTML_TEXT_SIMPLE, "//span");
        XPathLocator xpathLocator2 = newXPathLocator(HTML_TEXT_SIMPLE, "//div");

        AggregateLocator aggregateLocator = new AggregateLocator(xpathLocator1, xpathLocator2);
        Node[] nodes = aggregateLocator.getNodes();
        assertNotNull(nodes);
        assertNotNull(aggregateLocator.getNode());
        assertEquals(2, nodes.length);
        assertTrue(aggregateLocator.hasNodes());

        String text = aggregateLocator.getText();
        assertNotNull(text);
        assertTrue(text.indexOf("some text") != -1);
        assertTrue(text.indexOf("bold tags") != -1);

        Node[] nodes2 = aggregateLocator.getNodes();
        assertNotNull(nodes2);
        assertNotNull(aggregateLocator.getNode());
        assertEquals(2, nodes2.length);
        assertTrue(aggregateLocator.hasNodes());
        assertSame(nodes[0], nodes2[0]);
        assertSame(nodes[1], nodes2[1]);
    }

    public void testTableLocator() throws Exception
    {
        String TABLE_HTML = "<html><body><table id=\"t1\"><tr><td>r0c1</td><td>r0c2</td></tr><tr><td>r1c1</td><td>r1c2</td></tr><tr><td>r2c1</td><td>r2c2</td></tr></table></body></html>";

        String text;
        MockWebServer webServer = MockWebServer.getInstance();
        webServer.addPage("/table", TABLE_HTML);

        WebTester webTester = new MockWebTester(webServer);
        webTester.gotoPage("/table");

        TableLocator tableLocator = new TableLocator(webTester, "t1");
        assertNotNull(tableLocator.getNodes());
        assertEquals(1, tableLocator.getNodes().length);
        assertTrue(tableLocator.hasNodes());
        assertEquals("table", ((Element) tableLocator.getNodes()[0]).getTagName());

        text = tableLocator.getText();
        assertNotNull(text);

        WebTable webTable = tableLocator.getTable();
        assertNotNull(webTable);
        assertEquals("r0c1", webTable.getCellAsText(0, 0));
        assertEquals("r0c2", webTable.getCellAsText(0, 1));

        text = tableLocator.getText();
        assertNotNull(text);
    }

    public void testXpathAttributeSelectSupport() throws Exception
    {
        String someHTML = "<html><body><div id=\"1 2\t 3 \t\n4\">div1</div><div id=\"456\" lang=\"es\">div2</div></body></html>";
        XPathLocator xpathLocator = newXPathLocator(someHTML, "//div/@id");
        assertTrue(xpathLocator.hasNodes());
        assertEquals(2,xpathLocator.getNodes().length);

        xpathLocator = newXPathLocator(someHTML, "//div[1]/@id");
                assertTrue(xpathLocator.hasNodes());
                assertEquals(1,xpathLocator.getNodes().length);

        String actualText;
        actualText = xpathLocator.getText();
        assertEquals("1 2  3   4",actualText);

        actualText = xpathLocator.getRawText();
        assertEquals("1 2  3   4",actualText);

        actualText = xpathLocator.getHTML();
        assertEquals("id=\"1 2  3   4\"",actualText);

        xpathLocator = newXPathLocator(someHTML, "//div/@lang");
        assertTrue(xpathLocator.hasNodes());
        assertEquals(1,xpathLocator.getNodes().length);

        xpathLocator = newXPathLocator(someHTML, "//div/@doesntexist");
        assertFalse(xpathLocator.hasNodes());
        assertEquals(0,xpathLocator.getNodes().length);

    }

    private XPathLocator newXPathLocator(final String xmlText, String xpathExpression)
    {
        return new XPathLocator(getDOM(xmlText), xpathExpression);
    }

    private Document getDOM(String xmlText)
    {
        try
        {
            return XmlParserHelper.parseXml(xmlText);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
