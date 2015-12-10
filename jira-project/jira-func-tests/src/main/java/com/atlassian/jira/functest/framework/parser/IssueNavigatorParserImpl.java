package com.atlassian.jira.functest.framework.parser;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.navigator.NavigatorCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import java.util.Collection;

/**
 * Simple implementation of the {@link com.atlassian.jira.functest.framework.parser.IssueNavigatorParser} interface.
 *
 * @since v3.13
 */
public class IssueNavigatorParserImpl extends AbstractFuncTestUtil implements IssueNavigatorParser
{
    public IssueNavigatorParserImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
    }

    public NavigatorSearch parseSettings(WebTester tester, Collection /*<NavigatorCondition>*/ conditions)
    {
        for (final Object condition : conditions)
        {
            NavigatorCondition navigatorCondition = (NavigatorCondition) condition;
            navigatorCondition.parseCondition(tester);
        }

        return new NavigatorSearch(conditions);
    }
}
