package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import junit.framework.Test;

/**
 * Class of tests which run tests remotely in JIRA.
 *
 * @since v5.0
 */
public class FuncTestSuiteRemote extends FuncTestSuite
{
    public static final FuncTestSuite SUITE = new FuncTestSuiteRemote();

    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteRemote()
    {
        addTestsInPackageBundledPluginsOnly("com.atlassian.jira.webtests.ztests.remote", true);
    }
}
