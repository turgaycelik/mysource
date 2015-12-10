package com.atlassian.jira.functest.framework.parser;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * @since v5.2
 */
public class HeaderImpl extends AbstractFuncTestUtil implements Header
{
    public HeaderImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
    }

    @Override
    public String getFullUserName()
    {
        return locators.id("header-details-user-fullname").getNode().getAttributes().getNamedItem("data-displayname").getTextContent();
    }
}
