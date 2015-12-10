package com.atlassian.jira.functest.framework.locator;

import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This will locate HTML tables by 'tableId' and return the {@link com.meterware.httpunit.WebTable} ready for more
 * assertions.  It also returns the {@link org.w3c.dom.Node} that makes up the table as well as its combined text.
 *
 * @since v3.13
 */
public class TableLocator extends AbstractLocator
{
    private final String tableId;
    private final XPathLocator delegateXpathLocator;
    private WebTable webTable;

    public TableLocator(WebTester tester, String tableId)
    {
        super(tester);
        if (tableId == null)
        {
            throw new IllegalArgumentException("The tableId must not be null");
        }
        delegateXpathLocator = new XPathLocator(tester, "//table[@id='" + tableId + "']");
        this.tableId = tableId;
        //
        // new behaviour.  We initialise on construction.  Previously we did lazy loading of Nodes
        // but in retrospect its not a great idea.
        checkStateOrInit();
    }

    private synchronized void checkStateOrInit()
    {
        if (nodes == null)
        {
            try
            {
                this.webTable = tester.getDialog().getResponse().getTableWithID(this.tableId);
            }
            catch (SAXException e)
            {
                throw new RuntimeException(e);
            }
            this.originalWebResponse = getWebResponse(tester);

            // ok init the nodes via the delegate xpath locator
            // we can then use the of the table as a Node s well as a WebTable mechanism
            this.nodes = delegateXpathLocator.getNodes();
        }
    }

    public Node[] getNodes()
    {
        checkStateOrInit();
        return this.nodes;
    }

    /**
     * This will return the {@link com.meterware.httpunit.WebTable} using the tableId passed in on contruction of this Locator.
     *
     * @return a {@link com.meterware.httpunit.WebTable} with the specified id
     * @throws RuntimeException if the underlying {@link net.sourceforge.jwebunit.WebTester} is not in a good place.
     */
    public WebTable getTable()
    {
        checkStateOrInit();
        return webTable;
    }


}
