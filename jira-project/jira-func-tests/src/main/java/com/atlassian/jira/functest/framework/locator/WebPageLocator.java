package com.atlassian.jira.functest.framework.locator;

import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This locator returns the whole of the web page as text.
 * <p/>
 * NOTE : {@link Locator}s are one shot objects.  A call to {@link Locator#getNodes()} should return the same Nodes every time it is called.  Even if the underlying
 * data source has changed (for example the WebTester page has changed) the {@link com.atlassian.jira.functest.framework.locator.Locator} MUST return the same data.
 *
 * @since v3.13
 */
public class WebPageLocator extends AbstractLocator implements Locator
{
    private String htmlResponse;

    public WebPageLocator(WebTester tester)
    {
        super(tester);
        //
        // new behaviour.  We initialise on construction.  Previously we did lazy loading of Nodes
        // but in retrospect its not a great idea.
        checkStateOrInit();
    }

    private synchronized void checkStateOrInit()
    {
        if (nodes == null)
        {
            // we have run before so init the state
            this.originalWebResponse = getWebResponse(tester);
            Document document = getDOM(tester);
            nodes = new Element[] { document.getDocumentElement() };
            htmlResponse = getTextFromWebTester();
        }
    }

    private String getTextFromWebTester()
    {
        if (tester.getDialog() != null)
        {
            return tester.getDialog().getResponseText();
        }
        else
        {
            return "";
        }
    }

    /**
     * Returns the single top level Document element
     *
     * @return the single top level Document element
     */
    public Node[] getNodes()
    {
        checkStateOrInit();
        return nodes;
    }

    /**
     * All of HTML of the web page
     *
     * @return the HTML of the web page
     */
    public String getHTML()
    {
        checkStateOrInit();
        return htmlResponse;
    }

    public String toString()
    {
        return toStringImpl("WebPageLocator");
    }
}
