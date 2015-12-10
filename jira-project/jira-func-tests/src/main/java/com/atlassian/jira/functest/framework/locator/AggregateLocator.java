package com.atlassian.jira.functest.framework.locator;

import com.atlassian.jira.functest.framework.util.dom.DomKit;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * AggregateLocator will "aggregate" together the results of the provided {@link Locator}s
 * <p/>
 * NOTE : {@link Locator}s are one shot objects.  A call to {@link Locator#getNodes()} should return the same Nodes every time it is called.  Even if the underlying
 * data source has changed (for example the WebTester page has changed) the {@link com.atlassian.jira.functest.framework.locator.Locator} MUST return the same data.
 *
 * @since v3.13
 */
public class AggregateLocator extends AbstractLocator implements Locator
{
    private final Locator[] locators;

    /**
     * Creates an agggregate of the firstLocator and the secondLocator
     *
     * @param firstLocator  the first Locator to aggregate
     * @param secondLocator the second Locator to aggregate
     */
    public AggregateLocator(Locator firstLocator, Locator secondLocator)
    {
        this(new Locator[] { firstLocator, secondLocator });
        //
        // new behaviour.  We initialise on construction.  Previously we did lazy loading of Nodes
        // but in retrospect its not a great idea.
        getNodes();

    }


    /**
     * Aggregates together the values of the specified array of {@link com.atlassian.jira.functest.framework.locator.Locator}s
     *
     * @param locators must be non null and have a lenght > 0 and have non null array Locator members
     */
    public AggregateLocator(Locator[] locators)
    {
        boolean ok = true;
        if (locators == null || locators.length == 0)
        {

        }
        for (Locator locator : locators)
        {
            if (locator == null)
            {
                ok = false;
                break;
            }
        }
        if (!ok)
        {
            throw new IllegalArgumentException("The provided Locators array must be nulll null and have non null Locator instances in it");
        }
        this.locators = locators;
    }


    public Node[] getNodes()
    {
        if (nodes == null)
        {
            List list = new ArrayList();
            for (Locator locator : locators)
            {
                Node[] nodes = locator.getNodes();
                for (Node node : nodes)
                {
                    list.add(DomKit.betterNode(node));
                }
            }
            nodes = (Element[]) list.toArray(new Element[list.size()]);
        }
        return nodes;
    }


    public String toString()
    {
        return toStringImpl("AggregateLocator");
    }
}