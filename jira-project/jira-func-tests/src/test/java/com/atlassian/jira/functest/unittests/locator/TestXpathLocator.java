package com.atlassian.jira.functest.unittests.locator;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.unittests.mocks.XmlParserHelper;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 */
public class TestXpathLocator extends TestCase
{
    public void testMultipleOccurrences()
    {
        String HTML = "<html><head></head><body><div class='classy'>"
                + "<span class='lowlife'>There Was A Young Man From Horsham</span>"
                + "<span class='lowlife'>Who Took Out His Balls To Wash Em</span>"
                + "<span class='lowlife'>His Mum Said Jack</span>"
                + "<span class='lowlife'>If You Dont Put Em Back</span>"
                + "<span class='lowlife'>I'll Step On The Bastards And Squarsh Em!</span>"
                + "</div></body></html>";

        XPathLocator locator = new XPathLocator(doc(HTML),"//span[@class='lowlife']");
        assertTrue(locator.hasNodes());
        assertNotNull(locator.getNode());
        final Node[] nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(5, nodes.length);
        assertEquals("There Was A Young Man From HorshamWho Took Out His Balls To Wash EmHis Mum Said JackIf You Dont Put Em BackI'll Step On The Bastards And Squarsh Em!", locator.getText());
        assertEquals("<span class=\"lowlife\">There Was A Young Man From Horsham</span><span class=\"lowlife\">Who Took Out His Balls To Wash Em</span><span class=\"lowlife\">His Mum Said Jack</span><span class=\"lowlife\">If You Dont Put Em Back</span><span class=\"lowlife\">I'll Step On The Bastards And Squarsh Em!</span>",locator.getHTML());
    }

      private Document doc(String xmlText)
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
