package com.atlassian.jira.functest.framework.locator;

import com.atlassian.jira.functest.framework.util.dom.DomKit;
import org.w3c.dom.Node;

/**
 * A {@link com.atlassian.jira.functest.framework.locator.Locator} that uses the provided {@link org.w3c.dom.Node}
 * as the source of text.  This is useful when iterating a array of nodes from another locator and
 * being able to transform each found Node into another Locator.
 * <p/>
 * NOTE : {@link Locator}s are one shot objects.  A call to {@link Locator#getNodes()} should return the same Nodes every time it is called.  Even if the underlying
 * data source has changed (for example the WebTester page has changed) the {@link com.atlassian.jira.functest.framework.locator.Locator} MUST return the same data.
 *
 * @since v3.13
 */
public class NodeLocator extends AbstractLocator
{
    private final Node node;

    public NodeLocator(Node node)
    {
        super();
        if (node == null)
        {
            throw new IllegalArgumentException("You must provided a non null node");
        }
        this.node = DomKit.betterNode(node);
        //
        // new behaviour.  We initialise on construction.  Previously we did lazy loading of Nodes
        // but in retrospect its not a great idea.
        checkStateOrInit();
    }

    private synchronized void checkStateOrInit()
    {
        // ok init
        nodes = new Node[0];
        this.nodes = new Node[] { node };
    }


    public Node[] getNodes()
    {
        return this.nodes;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder("NodeLocator : ");
        sb.append(node.getClass().getName());
        return toStringImpl(sb.toString());
    }
}
