package com.atlassian.jira.functest.framework.locator;

import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A {@link Locator} that looks for the first {@link Element} with the specified id
 * <p/>
 * NOTE : {@link Locator}s are one shot objects.  A call to {@link Locator#getNodes()} should return the same Nodes every time it is called.  Even if the underlying
 * data source has changed (for example the WebTester page has changed) the {@link com.atlassian.jira.functest.framework.locator.Locator} MUST return the same data.
 *
 * @since v3.13
 */
public class IdLocator extends AbstractLocator implements Locator
{
    private final String elementId;

    public IdLocator(WebTester tester, String elementId)
    {
        super(tester);
        if (tester == null)
        {
            throw new IllegalArgumentException("The WebTester must not be null");
        }
        if (elementId == null)
        {
            throw new IllegalArgumentException("The elementId must not be null");
        }
        this.elementId = elementId;
        //
        // new behaviour.  We initialise on construction.  Previously we did lazy loading of Nodes
        // but in retrospect its not a great idea.
        checkStateOrInit();
    }

    private void checkStateOrInit()
    {
        if (nodes == null)
        {
            final XPathLocator locator = new XPathLocator(tester, "//*[@id='" + elementId + "']");
            this.nodes = locator.getNodes();
        }
    }

    /** @return returns at most 1 {@link org.w3c.dom.Element} */
    public Node[] getNodes()
    {
        checkStateOrInit();
        return nodes;
    }


    public String toString()
    {
        StringBuilder sb = new StringBuilder("IdLocator elementId : ");
        sb.append(this.elementId);
        return toStringImpl(sb.toString());
    }
}
