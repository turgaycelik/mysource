package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.parser.Header;
import com.atlassian.jira.functest.framework.parser.HeaderImpl;
import com.atlassian.jira.functest.framework.parser.IssueNavigatorParser;
import com.atlassian.jira.functest.framework.parser.IssueNavigatorParserImpl;
import com.atlassian.jira.functest.framework.parser.IssueParser;
import com.atlassian.jira.functest.framework.parser.IssueParserImpl;
import com.atlassian.jira.functest.framework.parser.SystemInfoParser;
import com.atlassian.jira.functest.framework.parser.SystemInfoParserImpl;
import com.atlassian.jira.functest.framework.parser.filter.FilterParser;
import com.atlassian.jira.functest.framework.parser.filter.FilterParserImpl;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;


public class ParserImpl extends AbstractFuncTestUtil implements Parser
{
    protected IssueParser issue;
    protected FilterParser filter;
    protected SystemInfoParser systemInfoParser;
    protected final IssueNavigatorParser issueNavigatorParser;

    public ParserImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
        issue = new IssueParserImpl(tester, environmentData, 2);
        filter = new FilterParserImpl(tester, environmentData, 2);
        systemInfoParser = new SystemInfoParserImpl(tester, environmentData, 2, new NavigationImpl(tester, environmentData));
        issueNavigatorParser = new IssueNavigatorParserImpl(tester, environmentData, 2);
    }


    public IssueParser issue()
    {
        return issue;
    }

    public FilterParser filter()
    {
        return filter;
    }

    public SystemInfoParser systemInfo()
    {
        return systemInfoParser;
    }

    public IssueNavigatorParser navigatorParser()
    {
        return issueNavigatorParser;
    }

    @Override
    public Header header()
    {
        return new HeaderImpl(tester, environmentData, 2);
    }
}
