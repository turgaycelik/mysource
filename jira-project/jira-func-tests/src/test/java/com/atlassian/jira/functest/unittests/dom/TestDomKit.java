package com.atlassian.jira.functest.unittests.dom;

import com.atlassian.jira.functest.framework.util.dom.DomKit;
import com.atlassian.jira.functest.unittests.mocks.XmlParserHelper;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.lang.reflect.Proxy;

/**
 * Tests the DomKit class
 *
 * @since v3.13
 */
public class TestDomKit extends TestCase
{
    private static final String WITHOUT_SPACES = "text without spaces";
    private static final String WITH_PREFIX_SPACES = "   \ttext with prefix spaces";
    private static final String WITH_SUFFIX_SPACES = "text with suffix spaces \n\t";
    private static final String WITH_SPACES = "\t \ntext with suffix spaces \n \t";
    private static final String WITH_SPACES_IN_THE_MIDDLE = "\t \ntext with     spaces in the middle \n \t";

    private static final String WITH_SPACES_AROUND_A_TAG = "\t \n<b>   text with spaces in  </b>  the middle \n \t";
    private static final String COLLAPSED_WITH_SPACES_AROUND_A_TAG = "text with spaces in the middle";
    private static final String RAW_WITH_SPACES_AROUND_A_TAG = "\t \n   text with spaces in    the middle \n \t";

    public void testGetCollapsedText() throws Exception
    {
        String htmlText;
        Document document;
        String actualText;

        htmlText = "<HTML><BODY>" + WITH_SPACES_AROUND_A_TAG + "</BODY></HTML>";
        document = parseXml(htmlText);
        actualText = DomKit.getCollapsedText(document);
        assertEquals(COLLAPSED_WITH_SPACES_AROUND_A_TAG, actualText);

        htmlText = "<HTML><BODY>" + WITHOUT_SPACES + "</BODY></HTML>";
        document = parseXml(htmlText);
        actualText = DomKit.getCollapsedText(document);
        assertEquals(WITHOUT_SPACES, actualText);

        htmlText = "<HTML><BODY>" + WITHOUT_SPACES + "<b>" + WITH_PREFIX_SPACES + "</b>" + "</BODY></HTML>";
        document = parseXml(htmlText);
        actualText = DomKit.getCollapsedText(document);
        assertEquals(buildExpectedCollapsedString(new String[]{WITHOUT_SPACES, WITH_PREFIX_SPACES}), actualText);

        htmlText = "<HTML><BODY>" + WITHOUT_SPACES + "<b>" + WITH_PREFIX_SPACES + "</b>" + WITH_SPACES_IN_THE_MIDDLE + "</BODY></HTML>";
        document = parseXml(htmlText);
        actualText = DomKit.getCollapsedText(document);
        assertEquals(buildExpectedCollapsedString(new String[]{WITHOUT_SPACES, WITH_PREFIX_SPACES, WITH_SPACES_IN_THE_MIDDLE}), actualText);

        htmlText = "<HTML><BODY>" + WITHOUT_SPACES + "<b>" + WITH_PREFIX_SPACES + "</b>" + WITH_SPACES_IN_THE_MIDDLE + "</BODY></HTML>";
        document = parseXml(htmlText);
        actualText = DomKit.getCollapsedText(document);
        assertEquals(buildExpectedCollapsedString(new String[]{WITHOUT_SPACES, WITH_PREFIX_SPACES, WITH_SPACES_IN_THE_MIDDLE}), actualText);


        htmlText = "<HTML><BODY>" + WITHOUT_SPACES + "<b>" + WITH_PREFIX_SPACES + "</b>" + WITH_SPACES_IN_THE_MIDDLE + "<span>" + WITH_SUFFIX_SPACES + "</span>" + "</BODY></HTML>";
        document = parseXml(htmlText);
        actualText = DomKit.getCollapsedText(document);
        assertEquals(buildExpectedCollapsedString(new String[]{WITHOUT_SPACES, WITH_PREFIX_SPACES, WITH_SPACES_IN_THE_MIDDLE, WITH_SUFFIX_SPACES}), actualText);

        htmlText = "<HTML><BODY>" + WITHOUT_SPACES + "<b>" + WITH_PREFIX_SPACES + "</b>" + WITH_SPACES_IN_THE_MIDDLE + "<span>" + WITH_SUFFIX_SPACES + "</span>" + "<div>" + WITH_SPACES + "</div>" + "</BODY></HTML>";
        document = parseXml(htmlText);
        actualText = DomKit.getCollapsedText(document);
        assertEquals(buildExpectedCollapsedString(new String[]{WITHOUT_SPACES, WITH_PREFIX_SPACES, WITH_SPACES_IN_THE_MIDDLE, WITH_SUFFIX_SPACES, WITH_SPACES}), actualText);
    }

    public void testRawText() throws Exception
    {
        String htmlText;
        Document document;
        String actualText;

        htmlText = "<HTML><BODY>" + WITH_SPACES_AROUND_A_TAG + "</BODY></HTML>";
        document = parseXml(htmlText);
        actualText = DomKit.getRawText(document);
        assertEquals(RAW_WITH_SPACES_AROUND_A_TAG, actualText);

        htmlText = "<HTML><BODY><div>" + WITH_SPACES_AROUND_A_TAG + "</div><span></span></BODY></HTML>";
        document = parseXml(htmlText);
        actualText = DomKit.getRawText(document);
        assertEquals(RAW_WITH_SPACES_AROUND_A_TAG, actualText);

    }


    public void testGetBody() throws Exception
    {
        String htmlText;
        Document document;
        Element bodyE;

        htmlText = "<HTML><BODY instance=\"1\"></BODY></HTML>";
        document = parseXml(htmlText);
        bodyE = DomKit.getBodyElement(document);
        assertNotNull(bodyE);
        assertEquals("1", bodyE.getAttribute("instance"));

        document = parseXml("<HTML></HTML>");
        bodyE = DomKit.getBodyElement(document);
        assertNull(bodyE);

        htmlText = "<HTML><BODY instance=\"1\"></BODY><BODY instance=\"2\"></BODY></HTML>";
        document = parseXml(htmlText);
        bodyE = DomKit.getBodyElement(document);
        assertNotNull(bodyE);
        assertEquals("1", bodyE.getAttribute("instance"));

        htmlText = "<HTML><BODGY instance=\"1\"></BODGY><BODY instance=\"2\"></BODY></HTML>";
        document = parseXml(htmlText);
        bodyE = DomKit.getBodyElement(document);
        assertNotNull(bodyE);
        assertEquals("2", bodyE.getAttribute("instance"));


    }

    public void testDOMOutput() throws Exception
    {
        String origHtml;
        Document document;

        origHtml = SOME_HTML;
        document = parseXml(origHtml);
        String htmlText = DomKit.getHTML(document.getDocumentElement());
        assertNotNull(htmlText);
        assertEquals(origHtml, htmlText);

        htmlText = DomKit.getHTML(document.getDocumentElement().getFirstChild());
        assertNotNull(htmlText);
        assertEquals("<body class=\"bodyclass\"><div id=\"1\">div1</div><div id=\"2\">div2<!-- and a comment --></div><span class=\"c1\">some text with \n \t whitespace</span></body>", htmlText);

        htmlText = DomKit.getHTML(document.getDocumentElement().getFirstChild().getFirstChild());
        assertNotNull(htmlText);
        assertEquals("<div id=\"1\">div1</div>", htmlText);

        htmlText = DomKit.getInnerHTML(document.getDocumentElement().getFirstChild());
        assertNotNull(htmlText);
        assertEquals("<div id=\"1\">div1</div><div id=\"2\">div2<!-- and a comment --></div><span class=\"c1\">some text with \n \t whitespace</span>", htmlText);

    }

    private static final String SOME_HTML = "<html><body class=\"bodyclass\"><div id=\"1\">div1</div><div id=\"2\">div2<!-- and a comment --></div><span class=\"c1\">some text with \n \t whitespace</span></body></html>";
    private static final String SOME_HTML_UPPERCASE = "<HTML><BODY class=\"bodyclass\"><DIV id=\"1\">div1</DIV><DIV id=\"2\">div2<!-- and a comment --></DIV><SPAN class=\"c1\">some text with \n \t whitespace</SPAN></BODY></HTML>";
    private static final String SOME_HTML_MIXEDCASE = "<HTML><body class=\"bodyclass\"><DIV id=\"1\">div1</DIV><div id=\"2\">div2<!-- and a comment --></div><SPAN class=\"c1\">some text with \n \t whitespace</SPAN></body></HTML>";

    public void testCopyDOM() throws Exception
    {
        Node srcNode;
        Node targetNode;
        Element targetElement;
        Element srcElement;

        srcNode = parseXml(SOME_HTML);
        targetNode = DomKit.copyDOM(srcNode, false);
        assertEquals(targetNode.getNodeType(), srcNode.getNodeType());
        assertEquals(Node.DOCUMENT_NODE, targetNode.getNodeType());
        srcElement = ((Document) srcNode).getDocumentElement();
        targetElement = ((Document) targetNode).getDocumentElement();
        assertEquals(targetElement.getTagName(), srcElement.getTagName());
        assertEquals("html", targetElement.getTagName());
        assertEquals("body", targetElement.getFirstChild().getNodeName());
        targetElement = (Element) targetElement.getFirstChild();
        assertEquals(2, targetElement.getElementsByTagName("div").getLength());
        assertEquals(0, targetElement.getElementsByTagName("DIV").getLength());

        srcNode = parseXml(SOME_HTML_UPPERCASE);
        targetNode = DomKit.copyDOM(srcNode, false);
        assertEquals(targetNode.getNodeType(), srcNode.getNodeType());
        assertEquals(Node.DOCUMENT_NODE, targetNode.getNodeType());
        srcElement = ((Document) srcNode).getDocumentElement();
        targetElement = ((Document) targetNode).getDocumentElement();
        assertEquals(targetElement.getTagName(), srcElement.getTagName());
        assertEquals("HTML", targetElement.getTagName());
        assertEquals("BODY", targetElement.getFirstChild().getNodeName());
        targetElement = (Element) targetElement.getFirstChild();
        assertEquals(0, targetElement.getElementsByTagName("div").getLength());
        assertEquals(2, targetElement.getElementsByTagName("DIV").getLength());


        srcNode = parseXml(SOME_HTML_MIXEDCASE);
        targetNode = DomKit.copyDOM(srcNode, false);
        assertEquals(targetNode.getNodeType(), srcNode.getNodeType());
        assertEquals(Node.DOCUMENT_NODE, targetNode.getNodeType());
        srcElement = ((Document) srcNode).getDocumentElement();
        targetElement = ((Document) targetNode).getDocumentElement();
        assertEquals(targetElement.getTagName(), srcElement.getTagName());
        assertEquals("HTML", targetElement.getTagName());
        assertEquals("body", targetElement.getFirstChild().getNodeName());
        targetElement = (Element) targetElement.getFirstChild();
        assertEquals(1, targetElement.getElementsByTagName("div").getLength());
        assertEquals(1, targetElement.getElementsByTagName("DIV").getLength());

        // switch to lower case mode
        srcNode = parseXml(SOME_HTML_MIXEDCASE);
        targetNode = DomKit.copyDOM(srcNode, true);
        assertEquals(targetNode.getNodeType(), srcNode.getNodeType());
        assertEquals(Node.DOCUMENT_NODE, targetNode.getNodeType());
        srcElement = ((Document) srcNode).getDocumentElement();
        targetElement = ((Document) targetNode).getDocumentElement();
        assertEquals(targetElement.getTagName(), srcElement.getTagName().toLowerCase());     // now in lower case
        assertEquals("html", targetElement.getTagName());
        assertEquals("body", targetElement.getFirstChild().getNodeName());
        targetElement = (Element) targetElement.getFirstChild();
        assertEquals(2, targetElement.getElementsByTagName("div").getLength());
        assertEquals(0, targetElement.getElementsByTagName("DIV").getLength());

        // ok try a little further along the dom tree
        srcNode = parseXml(SOME_HTML);
        srcNode = ((Document) srcNode).getElementsByTagName("div").item(1);

        targetNode = DomKit.copyDOM(srcNode, true);
        assertEquals(targetNode.getNodeType(), srcNode.getNodeType());
        assertEquals(Node.ELEMENT_NODE, targetNode.getNodeType());
        targetElement = (Element) targetNode;
        assertEquals("div", targetElement.getTagName());
        Node textNode = targetElement.getFirstChild();
        assertEquals(Node.TEXT_NODE, textNode.getNodeType());
        assertEquals("div2", textNode.getNodeValue());

        assertEquals("2", targetElement.getAttribute("id"));
    }

    public void testBetterNodeWrapping() throws Exception
    {

        // can it handle null
        assertNull(DomKit.betterNode(null));

        Node origNode = parseXml(SOME_HTML_MIXEDCASE);
        assertFalse(Proxy.isProxyClass(origNode.getClass()));

        Node betterNode = DomKit.betterNode(origNode);
        assertNotNull(betterNode);

        Class betterNodeClass = betterNode.getClass();
        assertNotSame(betterNode, origNode);
        assertTrue(Node.class.isAssignableFrom(betterNodeClass));
        assertTrue(Proxy.isProxyClass(betterNodeClass));

        Node doubleWrappedNode = DomKit.betterNode(betterNode);
        assertSame(betterNode, doubleWrappedNode);

        // are child objects wrapped as well
        Node childNode = betterNode.getFirstChild();
        assertNotNull(childNode);

        Class childNodeClass = childNode.getClass();
        assertTrue(Node.class.isAssignableFrom(childNodeClass));
        assertTrue(Proxy.isProxyClass(childNodeClass));

        Node doubleWrappedChildNode = DomKit.betterNode(childNode);
        assertSame(childNode, doubleWrappedChildNode);

        // and finally does the to string look ok
        String toString = betterNode.toString();
        assertNotNull(toString);
        assertTrue(toString.startsWith("#document-->"));

        toString = childNode.toString();
        assertNotNull(toString);
        assertTrue(toString.startsWith("HTML-->"));
    }

    private String buildExpectedCollapsedString(String[] segments)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.length; i++)
        {
            String segment = segments[i];
            sb.append(segment.trim());
            if (segments.length > 1 && i < segments.length - 1)
            {
                sb.append(" ");
            }
        }
        return sb.toString();
    }


    Document parseXml(String xmlText) throws Exception
    {
        return XmlParserHelper.parseXml(xmlText);
    }

}
