package com.atlassian.jira.functest.unittests.locator;

import com.atlassian.jira.functest.framework.jsoup.JSoupW3CEmitter;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import junit.framework.TestCase;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test for the new JSoup based CSS Locators
 *
 * @since v4.3
 */
public class TestCssLocator extends TestCase
{
    private static final String HTML_HORSHAM = "<html><head></head><body><div class='classy'>"
            + "<span class='lowlife'>There Was A Young Man From Horsham</span>"
            + "<span class='lowlife'>Who Took Out His Balls To Wash Em</span>"
            + "<span class='lowlife'>His Mum Said Jack</span>"
            + "<span class='lowlife'>If You Dont Put Em Back</span>"
            + "<span class='lowlife'>I'll Step On The Bastards And Squarsh Em!</span>"
            + "</div></body></html>";

    private static final String HTML_SMALL = "<html><head></head><body>"
            + "<div id='horshamite' class='classy'><span class='lowlife'>His Mum Said Jack</span></div>"
            + "</body></html>";

    private static final String HTML_TABLES = "<html><head></head><body>"
            + "<table id='horshamite' class='classy'><tr><td><span class='lowlife'>His Mum Said Jack</span></td></tr></table>"
            + "</body></html>";

    private static final String HTML_LISTS = "<html><head></head><body><div>"
            + "<ol class='classy'>"
            + "<li>Item 1</li>"
            + "<li>Item 2</li>"
            + "<li>Item 3</li>"
            + "</ol>"
            + "<ul>"
            + "<li>Item 4</li>"
            + "</ul>"
            + "</div></body></html>";

    private static final String HTML_MULTI_CLASS = "<div>"
            + "<span class='classy classic'>His Mum Said Jack</span>"
            + "<span class='classical'>If You Dont Put Em Back</span>"
            + "<span class='classy'>I'll Step On The Bastards And Squarsh Em!</span>"
            + "</div>";

    public void testBasicLocation()
    {

        CssLocator locator = new CssLocator(doc(HTML_SMALL),"div.classy");
        assertTrue(locator.hasNodes());
        assertNotNull(locator.getNode());
        final Node[] nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(1, nodes.length);
        assertEquals("His Mum Said Jack", locator.getText());
    }

    public void testMultipleOccurrences()
    {

        CssLocator locator = new CssLocator(doc(HTML_HORSHAM),"span.lowlife");
        assertTrue(locator.hasNodes());
        assertNotNull(locator.getNode());
        final Node[] nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(5, nodes.length);
        assertEquals("There Was A Young Man From HorshamWho Took Out His Balls To Wash EmHis Mum Said JackIf You Dont Put Em BackI'll Step On The Bastards And Squarsh Em!", locator.getText());
        assertEquals("<span class=\"lowlife\">There Was A Young Man From Horsham</span><span class=\"lowlife\">Who Took Out His Balls To Wash Em</span><span class=\"lowlife\">His Mum Said Jack</span><span class=\"lowlife\">If You Dont Put Em Back</span><span class=\"lowlife\">I'll Step On The Bastards And Squarsh Em!</span>",locator.getHTML());
    }

    public void testById()
    {
        CssLocator locator = new CssLocator(doc(HTML_SMALL),"div#horshamite");
        assertTrue(locator.hasNodes());
        final Node node = locator.getNode();
        assertNotNull(node);
        assertTrue(node.hasAttributes());

        Element element = (Element) node;
        assertEquals("horshamite",element.getAttribute("id"));

        final Attr attr = element.getAttributeNode("id");
        assertNotNull(attr);
        assertEquals("horshamite",attr.getValue());

        final Node[] nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(1, nodes.length);
        assertEquals("His Mum Said Jack", locator.getText());
    }

    public void testByCombination_DirectDescendant()
    {
        CssLocator locator = new CssLocator(doc(HTML_SMALL),"div > span");
        assertTrue(locator.hasNodes());
        final Node node = locator.getNode();
        assertNotNull(node);

        final Node[] nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(1, nodes.length);
        assertEquals("His Mum Said Jack", locator.getText());
    }

    public void testByCombination_Descendant()
    {
        CssLocator locator = new CssLocator(doc(HTML_TABLES),"table td");
        assertTrue(locator.hasNodes());
        final Node node = locator.getNode();
        assertNotNull(node);

        final Node[] nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(1, nodes.length);
        assertEquals("His Mum Said Jack", locator.getText());
    }

    public void testByCombination_DirectDescendant_Fail()
    {
        CssLocator locator = new CssLocator(doc(HTML_TABLES),"table > td");
        assertLocatorFail(locator);
    }


    public void test_Combinatitons_ImmeditatelyPrecending()
    {
        CssLocator locator = new CssLocator(doc(HTML_LISTS),"li + li");
        assertTrue(locator.hasNodes());
        final Node node = locator.getNode();
        assertNotNull(node);

        final Node[] nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(2, nodes.length);
        assertEquals("Item 2Item 3", locator.getText());
    }

    public void test_Combinatitons_Precending()
    {
        CssLocator locator = new CssLocator(doc(HTML_LISTS),"ol ~ ul");
        assertTrue(locator.hasNodes());
        final Node node = locator.getNode();
        assertNotNull(node);

        final Node[] nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(1, nodes.length);
        assertEquals("Item 4", locator.getText());
    }

    public void test_Combinatitons_ClassDescendents()
    {
        CssLocator locator = new CssLocator(doc(HTML_LISTS),"div ol.classy li");
        assertTrue(locator.hasNodes());
        final Node node = locator.getNode();
        assertNotNull(node);

        final Node[] nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(3, nodes.length);
        assertEquals("Item 1Item 2Item 3", locator.getText());
    }

    public void test_AttributeMatching()
    {

        CssLocator locator = new CssLocator(doc(HTML_MULTI_CLASS),"span[class*=classic]");
        assertTrue(locator.hasNodes());
        final Node node = locator.getNode();
        assertNotNull(node);

        Node[] nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(2, nodes.length);
        assertEquals("His Mum Said JackIf You Dont Put Em Back", locator.getText());

        //
        // note the now smaller match
        //
        locator = new CssLocator(doc(HTML_MULTI_CLASS),"span[class*=class]");
        assertTrue(locator.hasNodes());

        nodes = locator.getNodes();
        assertNotNull(nodes);
        assertEquals(3, nodes.length);
        assertEquals("His Mum Said JackIf You Dont Put Em BackI'll Step On The Bastards And Squarsh Em!", locator.getText());

    }

    private void assertLocatorFail(final CssLocator locator)
    {
        assertFalse(locator.hasNodes());
        assertNull(locator.getNode());
        assertEquals("", locator.getText());
    }

    Document doc(String htmlInput)
    {
        return JSoupW3CEmitter.parse(htmlInput).getDocument();
    }
}
