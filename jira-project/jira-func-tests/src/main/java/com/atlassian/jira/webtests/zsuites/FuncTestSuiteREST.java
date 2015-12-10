package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import junit.framework.Test;

/**
 * @since v4.2
 */
public class FuncTestSuiteREST extends FuncTestSuite
{
    public static final FuncTestSuite SUITE = new FuncTestSuiteREST();

    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteREST()
    {
        addTestsInPackageBundledPluginsOnly("com.atlassian.jira.webtests.ztests.bundledplugins2.rest", true);
    }
}
