package com.atlassian.jira.functest.unittests.config.xml;

import com.atlassian.jira.functest.config.ConfigXmlUtils;
import junit.framework.TestCase;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.dom.DOMElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.functest.config.ConfigXmlUtils}.
 *
 * @since v4.1
 */
public class TestConfigXmlUtils extends TestCase
{
    private static final String WHITESPACE = "\n    ";

    public void testCreateNewElementBlank() throws Exception
    {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");

        Element newElement = ConfigXmlUtils.createNewElement(root, "cool");
        @SuppressWarnings ({ "unchecked" }) List<Node> list = root.content();

        assertTrue(list.get(0) instanceof Text);
        assertEquals(WHITESPACE, list.get(0).getText());
        assertSame(list.get(1), newElement);
    }

    public void testCreateNewElementNoOthers() throws Exception
    {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");
        for (int i = 0; i < 5; i++)
        {
            root.addElement("a").setText(String.valueOf(i));
        }

        Element newElement = ConfigXmlUtils.createNewElement(root, "addme");
        @SuppressWarnings ({ "unchecked" }) List<Node> list = root.content();

        assertTrue(list.get(0) instanceof Text);
        assertEquals(WHITESPACE, list.get(0).getText());
        assertSame(list.get(1), newElement);
    }

    public void testCreateNewElementOthersAlreadyThere() throws Exception
    {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");

        root.addElement("a");
        root.addElement("b");
        root.addElement("c");

        Element newElement = ConfigXmlUtils.createNewElement(root, "b");
        @SuppressWarnings ({ "unchecked" }) List<Node> list = root.content();

        assertTrue(list.get(2) instanceof Text);
        assertEquals(WHITESPACE, list.get(2).getText());
        assertSame(list.get(3), newElement);
    }

    public void testRemoveAttribute() throws Exception
    {
        final String REMOVE = "remove";

        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");
        root.addAttribute(REMOVE, "me");

        ConfigXmlUtils.removeAttribute(root, REMOVE);
        assertNull(root.element(REMOVE));

        root.addElement(REMOVE).setText("somecrap");
        ConfigXmlUtils.removeAttribute(root, REMOVE);
        assertNull(root.element(REMOVE));

        root.addAttribute(REMOVE, "me");
        root.addElement(REMOVE).setText("somecrap");
        ConfigXmlUtils.removeAttribute(root, REMOVE);
        assertNull(root.element(REMOVE));
        assertNull(root.element(REMOVE));
    }

    public void testSetAttributeRemove() throws Exception
    {
        final String REMOVE = "remove";

        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");
        root.addAttribute(REMOVE, "me");
        root.addElement(REMOVE).setText("somecrap");

        ConfigXmlUtils.setAttribute(root, REMOVE, null);
        assertNull(root.element(REMOVE));
        assertNull(root.element(REMOVE));
    }

    public void testSetAttributeXmlAttribute() throws Exception
    {
        final String ATTRIB = "attrib";
        final String NEW = "new";
        final String NEWER = "newer";

        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");

        ConfigXmlUtils.setAttribute(root, ATTRIB, NEW);
        assertEquals(NEW, root.attributeValue(ATTRIB));

        //Add an "element" attribute
        root.addElement(ATTRIB).setText("value");
        ConfigXmlUtils.setAttribute(root, ATTRIB, NEWER);
        assertEquals(NEWER, root.attributeValue(ATTRIB));
        assertNull(root.element(ATTRIB));
    }

    public void testSetAttributeXmlElement() throws Exception
    {
        final String ATTRIB = "attrib";
        final String NEW = "n\new";
        final String NEWER = "ne\rwer";

        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");

        ConfigXmlUtils.setAttribute(root, ATTRIB, NEW);
        assertNull(root.attribute(ATTRIB));
        assertEquals(NEW, root.element(ATTRIB).getText());

        //Add an "element" attribute
        root.addAttribute(ATTRIB, "Value");
        ConfigXmlUtils.setAttribute(root, ATTRIB, NEWER);
        assertNull(root.attribute(ATTRIB));
        assertEquals(NEWER, root.element(ATTRIB).getText());
    }

    public void testGetStringValue() throws Exception
    {
        final String ATTRIB = "attrib";
        final String NEW = "new";
        final String NEWER = "ne\rwer";

        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");

        assertNull(ConfigXmlUtils.getTextValue(root, ATTRIB));

        root.addAttribute(ATTRIB, NEW);
        assertEquals(NEW, ConfigXmlUtils.getTextValue(root, ATTRIB));

        root.addElement(ATTRIB).setText(NEWER);
        assertEquals(NEW, ConfigXmlUtils.getTextValue(root, ATTRIB));

        root.remove(root.attribute(ATTRIB));

        assertEquals(NEWER, ConfigXmlUtils.getTextValue(root, ATTRIB));
    }

    public void testGetLongValue() throws Exception
    {
        final String ATTRIB = "attrib";
        final long NEW = 1;
        final long NEWER = 3345;

        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");

        assertNull(ConfigXmlUtils.getLongValue(root, ATTRIB));

        root.addAttribute(ATTRIB, String.valueOf(NEW));
        assertEquals(NEW, (long)ConfigXmlUtils.getLongValue(root, ATTRIB));

        root.addElement(ATTRIB).setText(String.valueOf(NEWER));
        assertEquals(NEW, (long)ConfigXmlUtils.getLongValue(root, ATTRIB));

        root.remove(root.attribute(ATTRIB));

        assertEquals(NEWER, (long)ConfigXmlUtils.getLongValue(root, ATTRIB));

        root.addAttribute(ATTRIB, "badnumber");
        assertNull(ConfigXmlUtils.getLongValue(root, ATTRIB));
    }

    public void testGetIntValue() throws Exception
    {
        final String ATTRIB = "attrib";
        final int NEW = 1;
        final int NEWER = 3345;

        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");

        assertNull(ConfigXmlUtils.getIntegerValue(root, ATTRIB));

        root.addAttribute(ATTRIB, String.valueOf(NEW));
        assertEquals(NEW, (int)ConfigXmlUtils.getIntegerValue(root, ATTRIB));

        root.addElement(ATTRIB).setText(String.valueOf(NEWER));
        assertEquals(NEW, (int)ConfigXmlUtils.getIntegerValue(root, ATTRIB));

        root.remove(root.attribute(ATTRIB));

        assertEquals(NEWER, (int)ConfigXmlUtils.getIntegerValue(root, ATTRIB));

        root.addAttribute(ATTRIB, "badnumber");
        assertNull(ConfigXmlUtils.getIntegerValue(root, ATTRIB));
    }

    public void testRemoveElementWithoutWhiteSpace() throws Exception
    {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");

        Element removeElement = root.addElement("removeMe");
        Element newFirst = root.addElement("newFirst");

        assertTrue(ConfigXmlUtils.removeElement(removeElement));
        assertEquals(1, root.content().size());
        assertEquals(newFirst, root.content().get(0));

        removeElement = root.addElement("removeMeAlso");
        assertTrue(ConfigXmlUtils.removeElement(removeElement));
        assertEquals(1, root.content().size());
        assertEquals(newFirst, root.content().get(0));
    }

    public void testRemoveElementRoot() throws Exception
    {
        assertFalse(ConfigXmlUtils.removeElement(new DOMElement("bad")));
    }

    public void testRemoveElementWithSpace() throws Exception
    {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        Element root = document.addElement("root");

        root.addText("       ");
        Element removeElement = root.addElement("removeMe");
        Element newFirst = root.addElement("newFirst");

        assertTrue(ConfigXmlUtils.removeElement(removeElement));
        assertEquals(1, root.content().size());
        assertEquals(newFirst, root.content().get(0));

        Element notWhiteSpace = root.addElement("notwhiteSpace");
        removeElement = root.addElement("removeMeAlso");
        assertTrue(ConfigXmlUtils.removeElement(removeElement));
        assertEquals(2, root.content().size());
        assertEquals(newFirst, root.content().get(0));
        assertEquals(notWhiteSpace, root.content().get(1));
    }

    public void testGetTopElementsByName() throws Exception
    {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        final Element root = document.addElement("root");
        final Element element = root.addElement("one");
        final Element element2  = root.addElement("one");
        final Element element3  = root.addElement("two");
        element3.addElement("one");

        assertEquals(Collections.<Element>emptyList(), ConfigXmlUtils.getTopElementsByName(document, "three"));
        assertEquals(Arrays.asList(element, element2), ConfigXmlUtils.getTopElementsByName(document, "one"));
        assertEquals(Arrays.asList(element3), ConfigXmlUtils.getTopElementsByName(document, "two"));
    }

    public void testGetElementByXpath() throws Exception
    {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        final Element root = document.addElement("root");
        root.addElement("one").addAttribute("bad", "match");
        root.addElement("one");
        final Element element3  = root.addElement("two");
        element3.addElement("one");

        //Matching more than one node should fail.
        try
        {
            ConfigXmlUtils.getElementByXpath(document, "//one");
            fail("Matching more than one node should have failed.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }

        //Matching a non element should fail.
        try
        {
            ConfigXmlUtils.getElementByXpath(document, "//*/@bad");
            fail("Matching a non-element should fail.");
        }
        catch (IllegalArgumentException expected)
        {
            //expected.
        }

        assertNull(ConfigXmlUtils.getElementByXpath(document, "/dontmatch"));
        assertEquals(element3, ConfigXmlUtils.getElementByXpath(document, "//two"));
    }

    public void testGetElementsByXpath() throws Exception
    {
        DocumentFactory factory = DocumentFactory.getInstance();
        Document document = factory.createDocument();
        final Element root = document.addElement("root");
        final Element element1 = root.addElement("one").addAttribute("bad", "match");
        final Element element2 = root.addElement("one");
        final Element element3  = root.addElement("two");
        final Element element4 = element3.addElement("one");

        assertEquals(Arrays.asList(element1, element2, element4), ConfigXmlUtils.getElementsByXpath(document, "//one"));
        assertEquals(Arrays.asList(element1, element2), ConfigXmlUtils.getElementsByXpath(document, "/root/one"));
        assertEquals(Collections.<Element>emptyList(), ConfigXmlUtils.getElementsByXpath(document, "nothing"));

        try
        {
            ConfigXmlUtils.getElementsByXpath(document, "//*/@*");
            fail("Should fail when it finds an attribute.");
        }
        catch (IllegalArgumentException expected)
        {
            //good.
        }
    }
}