package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.NodeLocator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import org.w3c.dom.Node;

/**
 * A factory class for creating locators from FuncTestCase and JIRAWebTest test cases.  The idea is to make that test
 * code that much more succinct and hence more readable.
 *
 * @since v4.2
 */
public interface LocatorFactory
{
    WebPageLocator page();

    XPathLocator xpath(final String xpathExpression);
    
    CssLocator css(final String cssSelector);

    IdLocator id(final String id);

    NodeLocator node(final Node node);

    TableLocator table(final String tableId);

    TableCellLocator cell(final String tableId, final int row, final int col);
}
