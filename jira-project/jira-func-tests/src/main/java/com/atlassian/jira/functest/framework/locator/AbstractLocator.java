package com.atlassian.jira.functest.framework.locator;

import com.atlassian.jira.functest.framework.util.dom.DomKit;
import com.atlassian.jira.functest.framework.util.dom.SneakyDomExtractor;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.meterware.httpunit.WebResponse;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.Iterator;

/**
 * A base class to derive {@link Locator}s from
 *
 * @since v3.13
 */
public abstract class AbstractLocator implements Locator
{
    protected WebTester tester;
    protected WebResponse originalWebResponse;

    // all Locator's probably need an array of nodes
    protected Node[] nodes;


    protected AbstractLocator()
    {
        this.nodes = null;
    }

    protected AbstractLocator(WebTester tester)
    {
        if (tester == null)
        {
            throw new IllegalArgumentException("The WebTester must not be null");
        }
        this.tester = tester;
        this.nodes = null;
    }

    protected Node betterNode(Node node)
    {
        return DomKit.betterNode(node);
    }

    @Override
    public boolean exists()
    {
        return hasNodes();
    }

    public Node getNode()
    {
        Node[] nodes = getNodes();
        if (nodes.length == 0)
        {
            return null;
        }
        return betterNode(nodes[0]);
    }

    @Override
    public Iterator<LocatorEntry> iterator()
    {
        return new LocatorIterator(this);
    }

    @Override
    public Iterable<LocatorEntry> allMatches()
    {
        return CollectionUtil.toList(iterator());
    }

    public boolean hasNodes()
    {
        return getNodes().length > 0;
    }

    /**
     * Returns the DOM Document from the WebTester.  This is useful to override in tests
     *
     * @param tester the WebTester in play
     * @return the Document from the web tester
     */
    protected Document getDOM(WebTester tester)
    {
        return SneakyDomExtractor.getDOM(tester);
    }

    public String getText()
    {
        return getNodesTextImpl(getNodes(), LocatorTextOperation.COLLAPSED_TEXT);
    }

    public String getText(Node node)
    {
        return getNodeTextImpl(node, LocatorTextOperation.COLLAPSED_TEXT);

    }

    public String getRawText()
    {
        return getNodesTextImpl(getNodes(), LocatorTextOperation.RAW_TEXT);
    }

    public String getRawText(Node node)
    {
        return getNodeTextImpl(node, LocatorTextOperation.RAW_TEXT);

    }

    public String getHTML()
    {
        return getNodesTextImpl(getNodes(), LocatorTextOperation.HTML_TEXT);
    }

    public String getHTML(final Node node)
    {
        return getNodeTextImpl(node, LocatorTextOperation.HTML_TEXT);
    }

    protected interface LocatorTextOperation
    {
        static final LocatorTextOperation COLLAPSED_TEXT = new LocatorTextOperation()
        {
        };
        static final LocatorTextOperation RAW_TEXT = new LocatorTextOperation()
        {
        };
        static final LocatorTextOperation HTML_TEXT = new LocatorTextOperation()
        {
        };
    }

    /**
     * Called to get the test or HTML of an {@link org.w3c.dom.Node}
     *
     * @param node          the Node
     * @param textOperation the type of text operation
     * @return the text of the Node, inclusive of the node itself
     */
    protected String getNodeTextImpl(Node node, LocatorTextOperation textOperation)
    {
        if (!containsNode(this, node))
        {
            throw new IllegalArgumentException("The node provided must be one contained in a call to Locator.getNodes()");
        }
        if (textOperation == LocatorTextOperation.HTML_TEXT)
        {
            return DomKit.getHTML(node);
        }
        else if (textOperation == LocatorTextOperation.RAW_TEXT)
        {
            return DomKit.getRawText(node);
        }
        else
        {
            return DomKit.getCollapsedText(node);
        }
    }

    /**
     * Called to get all the text or HTML of a set of {@link org.w3c.dom.Node}'s.  Makes a call
     * to AbstractLocator#getNodeHtmlImpl for each node and appends it.
     *
     * @param nodes   the Nodes to get text for
     * @param textOperation the type of text operation
     * @return the text of all the Nodes appended together
     */
    protected String getNodesTextImpl(Node[] nodes, LocatorTextOperation textOperation)
    {
        StringBuilder sb = new StringBuilder();
        for (Node node : nodes)
        {
            sb.append(getNodeTextImpl(node, textOperation));
        }
        return sb.toString();
    }

    protected static boolean containsNode(Locator locator, Node node)
    {
        Node[] nodes = locator.getNodes();
        boolean found = false;
        for (Node node1 : nodes)
        {
            if (node1 == node)
            {
                found = true;
                break;
            }
        }
        return found;
    }

    protected WebResponse getWebResponse(WebTester tester)
    {
        WebResponse webResponse = null;
        if (tester.getDialog() != null)
        {
            webResponse = tester.getDialog().getResponse();
        }
        return webResponse;
    }

    protected synchronized String toStringImpl(String concreteImplementationName)
    {
        StringBuilder sb = new StringBuilder(concreteImplementationName);
        sb.append(" : ");
        if (this.nodes != null)
        {
            sb.append(nodes.length);
            sb.append(" node(s)");

            String text = getText();
            if (text != null) {
                text = StringUtils.abbreviate(text,40);
            }
            sb.append(" - '").append(text).append("'");
        }
        return sb.toString();
    }

}
