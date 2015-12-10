package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.locator.*;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;

/**
 * An implementation of the Locator Factory
 *
 * @since v4.2
 */
public class LocatorFactoryImpl implements LocatorFactory
{
    private final WebTester tester;

    public LocatorFactoryImpl(final WebTester tester)
    {
        this.tester = tester;
    }

    public XPathLocator xpath(final String xpathExpression)
    {
        return new XPathLocator(tester, xpathExpression);
    }

    public CssLocator css(String cssSelector)
    {
        return new CssLocator(tester,cssSelector);
    }

    public WebPageLocator page()
    {
        return new WebPageLocator(tester);
    }

    public IdLocator id(final String id)
    {
        return new IdLocator(tester, id);
    }

    public NodeLocator node(final Node node)
    {
        return new NodeLocator(node);
    }

    public TableLocator table(final String tableId)
    {
        return new TableLocator(tester, tableId);
    }

    public TableCellLocator cell(final String tableId, final int row, final int col)
    {
        return new TableCellLocator(tester, tableId, row, col);
    }
}
