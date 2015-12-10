package com.atlassian.jira.functest.unittests.mocks;

import com.meterware.httpunit.HttpUnitOptions;
import net.sourceforge.jwebunit.WebTester;

/**
 * A Mock WebTester will turn off JavaScript and beingAt("/")
 *
 * @since v3.13
 */
public class MockWebTester extends WebTester
{
    public MockWebTester(MockWebServer mockWebServer)
    {
        this.getTestContext().setBaseUrl(mockWebServer.getHostAndPort());
        HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        this.beginAt("/");
    }
}
