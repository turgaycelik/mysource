package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.Lists;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestMetaTagOrdering extends FuncTestCase
{
    @Override
    public void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    @Override
    public void tearDownTest()
    {
        backdoor.darkFeatures().disableForSite("com.atlassian.plugins.SEND_HEAD_EARLY");
    }

    public void testDashboard()
    {
        tester.gotoPage("/secure/Dashboard.jspa");
        verifyMetaTags();
    }

    public void testDashboardSendHeadEarly()
    {
        backdoor.darkFeatures().enableForSite("com.atlassian.plugins.SEND_HEAD_EARLY");
        tester.gotoPage("/secure/Dashboard.jspa");
        verifyMetaTags();
    }

    // Verifies that the <meta content="utf-8"> and <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    // tags come at the head of the document. These must be before any content.
    public void verifyMetaTags()
    {
        List<Node> headChildren = elements(locator.css("head").getNode().getChildNodes());

        // First child must be <meta charset="utf-8">
        Node firstChild = headChildren.get(0);
        assertTagName("meta", firstChild);
        assertAttribute("charset", "utf-8", firstChild);

        // Second child must be <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
        Node secondChild = headChildren.get(1);
        assertTagName("meta", secondChild);
        assertAttribute("http-equiv", "X-UA-Compatible", secondChild);
        assertAttribute("content", "IE=Edge", secondChild);
    }

    // Filters nodelist to return elements nodes only
    private List<Node> elements(NodeList nodes)
    {
        List<Node> filtered = Lists.newArrayList();
        for (int i = 0; i < nodes.getLength(); ++i)
        {
            if (1 == nodes.item(i).getNodeType()) // node type 1 is ELEMENT_NODE
            {
                filtered.add(nodes.item(i));
            }
        }
        return filtered;
    }

    private void assertTagName(String expected, Node node)
    {
        assertEquals(expected, node.getNodeName());
    }

    private void assertAttribute(String expectedAttribute, String expectedValue, Node node)
    {
        assertEquals(expectedValue, node.getAttributes().getNamedItem(expectedAttribute).getNodeValue());
    }
}
