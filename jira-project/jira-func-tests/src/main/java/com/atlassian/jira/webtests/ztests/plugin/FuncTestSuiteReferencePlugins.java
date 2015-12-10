package com.atlassian.jira.webtests.ztests.plugin;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import junit.framework.Test;

/**
 * Test suite for all functional tests against plugin modules reloadability. 
 *
 * @since v4.3
 */
public class FuncTestSuiteReferencePlugins extends FuncTestSuite
{

    public static final FuncTestSuiteReferencePlugins SUITE = new FuncTestSuiteReferencePlugins();

    public static Test suite()
    {
        return SUITE;
    }

    public FuncTestSuiteReferencePlugins()
    {
        addTest(TestWebWork1PrepareActionInterface.class);
        addTest(TestCreateIsueinPostFunction.class);
        addTest(TestPluginWikiRenderer.class);
    }
}
