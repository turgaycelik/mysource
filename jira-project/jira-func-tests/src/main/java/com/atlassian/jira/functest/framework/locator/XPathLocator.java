package com.atlassian.jira.functest.framework.locator;

import com.atlassian.jira.functest.framework.util.dom.DomKit;
import net.sourceforge.jwebunit.WebTester;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;

/**
 * A {@link com.atlassian.jira.functest.framework.locator.Locator} that uses XPath to locate {@link org.w3c.dom.Node}'s
 * <p/>
 * For more information on XPath look here:
 * <p/>
 * <a href="http://www.w3.org/TR/xpath">http://www.w3.org/TR/xpath</a>
 * <p/>
 * For a tutorial on XPath look here:
 * <p/>
 * <a href="http://www.w3schools.com/xpath/default.asp">http://www.w3schools.com/xpath/default.asp</a>
 * <p/>
 * <a href="http://www.zvon.org/xxl/XPathTutorial/General/examples.html">http://www.zvon.org/xxl/XPathTutorial/General/examples.html</a>
 * <p/>
 * NOTE : {@link Locator}s are one shot objects.  A call to {@link Locator#getNodes()} should return the same Nodes every time it is called.  Even if the underlying
 * data source has changed (for example the WebTester page has changed) the {@link com.atlassian.jira.functest.framework.locator.Locator} MUST return the same data.
 *
 * @since v3.13
 */
public class XPathLocator extends AbstractLocator implements Locator
{
    private final XPath xpath;
    private final String xPathExpressionStr;
    private Node startNode;
    private final boolean constructedViaTester;

    /**
     * Locates {@link org.w3c.dom.Node}'s using the specified {@link net.sourceforge.jwebunit.WebTester}
     *
     * @param tester             the WebTester in play
     * @param xPathExpressionStr the xpath string
     * @throws RuntimeException if the xPathExpressionStr cannot be compiled into valid XPATH
     */
    public XPathLocator(WebTester tester, String xPathExpressionStr)
    {
        super(tester);
        if (tester == null)
        {
            throw new IllegalArgumentException("The WebTester must not be null");
        }
        if (xPathExpressionStr == null)
        {
            throw new IllegalArgumentException("The xPathExpressionStr must not be null");
        }
        constructedViaTester = true;
        this.startNode = null;
        this.xPathExpressionStr = xPathExpressionStr;
        try
        {
            this.xpath = getXpathExpression(xPathExpressionStr);
        }
        catch (Exception e)
        {
            throw new RuntimeException("The XPath provided could not be complied : '" + xPathExpressionStr + "'", e);
        }
        //
        // new behaviour.  We initialise on construction.  Previously we did lazy loading of Nodes
        // but in retrospect its not a great idea.
        checkStateOrInit();
    }

    /**
     * Locates {@link org.w3c.dom.Node}'s starting from the specified {@link org.w3c.dom.Node}
     *
     * @param startNode          the {@link org.w3c.dom.Node} to start at
     * @param xPathExpressionStr the xpath string
     * @throws RuntimeException if the xPathExpressionStr cannot be compiled into valid XPATH
     */
    public XPathLocator(Node startNode, String xPathExpressionStr)
    {
        constructedViaTester = false;
        if (startNode == null)
        {
            throw new IllegalArgumentException("You must provide a startNode");
        }
        if (xPathExpressionStr == null)
        {
            throw new IllegalArgumentException("The xPathExpressionStr must not be null");
        }

        this.startNode = startNode;
        this.xPathExpressionStr = xPathExpressionStr;
        try
        {
            this.xpath = getXpathExpression(xPathExpressionStr);
        }
        catch (Exception e)
        {
            throw new RuntimeException("The XPath provided could not be complied : '" + xPathExpressionStr + "'", e);
        }
        //
        // new behaviour.  We initialise on construction
        checkStateOrInit();
    }

    private synchronized void checkStateOrInit()
    {
        if (nodes == null)
        {
            // we have run before so init the state
            final Document document;
            if (constructedViaTester)
            {
                this.originalWebResponse = getWebResponse(tester);
                document = getDOM(tester);
                startNode = document;
            }
            // ok init
            try
            {
                nodes = new Node[0];
                List nodes = this.xpath.selectNodes(startNode);
                if (nodes != null)
                {
                    this.nodes = makeArr(nodes);
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
        StringBuilder sb = new StringBuilder("XPathLocator : ");
        sb.append(xPathExpressionStr);
        return toStringImpl(sb.toString());
    }

    private Node[] makeArr(List nodeList)
    {
        Node[] nodes = new Node[nodeList.size()];
        for (int i = 0; i < nodeList.size(); i++)
        {
            Node node = (Node) nodeList.get(i);
            nodes[i] = DomKit.betterNode(node);
        }
        return nodes;
    }

    private XPath getXpathExpression(String xpathExpressionStr) throws Exception
    {
        return new DOMXPath(xpathExpressionStr);
    }
}
