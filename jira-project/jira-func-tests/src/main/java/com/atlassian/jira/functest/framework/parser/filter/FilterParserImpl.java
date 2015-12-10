package com.atlassian.jira.functest.framework.parser.filter;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

public class FilterParserImpl extends AbstractFuncTestUtil implements FilterParser
{
    public FilterParserImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
    }

    public FilterList parseFilterList(final String tableId)
    {
        TableLocator locator = new TableLocator(tester, tableId);
        if (locator.getTable() == null)
        {
            return null;
        }
        else
        {
            return new FilterList(locator);
        }
    }
}