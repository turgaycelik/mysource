package com.atlassian.jira.functest.framework.locator;

import com.atlassian.jira.functest.framework.jsoup.JSoupNode;
import com.atlassian.jira.functest.framework.jsoup.JSoupW3CEmitter;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;

/**
 * A CSS locator that uses JSOUP {@link org.jsoup.nodes.Document) under the covers
 * <p/>
 * The CSS selectors are therefore the set that JSOUP can handle.
 * <p/>
 * See {@link org.jsoup.select.Selector} for the supported CSS selectors
 * <p/>
 * At present it is a subset of jQuery CSS support so be a bit careful with the more tricky selectors
 *
 * @since v4.3
 */
public class CssLocator extends AbstractLocator implements Locator
{

    private final boolean constructedViaTester;
    private final String cssSelector;
    private Node startNode;
    private JSoupW3CEmitter jSoupW3CEmitter;

    /**
     * Locates {@link org.w3c.dom.Node}'s using the specified {@link net.sourceforge.jwebunit.WebTester}
     *
     * @param tester the WebTester in play
     * @param cssSelector the CSS selector string
     */
    public CssLocator(WebTester tester, String cssSelector)
    {
        super(tester);
        if (cssSelector == null)
        {
            throw new IllegalArgumentException("The css selector must not be null");
        }
        this.cssSelector = cssSelector;
        this.constructedViaTester = true;
        this.startNode = null;
        //
        // new behaviour.  We initialise on construction
        checkStateOrInit();
    }

    /**
     * Locates {@link org.w3c.dom.Node}'s starting from the specified {@link org.w3c.dom.Node}
     * <p/>
     * The start node can ONLY be a node given out previous by another CssLocator.  It will throw and
     * IllegalArgumentException if you give it any other type of Node.
     *
     * @param startNode the {@link org.w3c.dom.Node} to start at.
     * @param cssSelector the CSS selector string
     */
    public CssLocator(Node startNode, String cssSelector)
    {
        constructedViaTester = false;
        if (!(startNode instanceof JSoupNode))
        {
            throw new IllegalArgumentException("You must provide a JSoupNode");
        }
        if (cssSelector == null)
        {
            throw new IllegalArgumentException("The css selector must not be null");
        }

        this.startNode = startNode;
        this.cssSelector = cssSelector;
        this.jSoupW3CEmitter = new JSoupW3CEmitter();
        //
        // new behaviour.  We initialise on construction
        checkStateOrInit();
    }

    private synchronized void checkStateOrInit()
    {
        if (nodes == null)
        {
            // we have run before so init the state
            if (constructedViaTester)
            {
                this.originalWebResponse = getWebResponse(tester);
                try
                {
                    jSoupW3CEmitter = JSoupW3CEmitter.parse(originalWebResponse.getText());
                    startNode = jSoupW3CEmitter.getDocument();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
            // ok init
            try
            {
                nodes = new Node[0];
                final NodeList list = jSoupW3CEmitter.select(cssSelector, startNode);
                if (list.getLength() > 0)
                {
                    this.nodes = makeArr(list);
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }


    public Node[] getNodes()
    {
        checkStateOrInit();
        return this.nodes;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder("CssLocator : ");
        sb.append(cssSelector);
        sb.append(" : ");
        sb.append(startNode);
        return toStringImpl(sb.toString());
    }

    private Node[] makeArr(NodeList nodeList)
    {
        Node[] nodes = new Node[nodeList.getLength()];
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Node node = nodeList.item(i);
            nodes[i] = node;
        }
        return nodes;
    }

    @Override
    protected Node betterNode(final Node node)
    {
        return node;
    }
}
