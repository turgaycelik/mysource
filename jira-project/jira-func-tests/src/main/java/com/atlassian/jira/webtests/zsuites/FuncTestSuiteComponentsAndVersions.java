package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.customfield.TestVersionCustomField;
import com.atlassian.jira.webtests.ztests.project.TestComponentValidation;
import com.atlassian.jira.webtests.ztests.project.TestProjectComponentQuickSearch;
import com.atlassian.jira.webtests.ztests.project.TestVersionValidation;
import junit.framework.Test;

/**
 * A suite of tests related to Components and Versions
 *
 * @since v4.0
 */
public class FuncTestSuiteComponentsAndVersions extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteComponentsAndVersions();

    /**
     * The pattern in JUnit/IDEA JUnit runner is that if a class has a static suite() method that returns a Test, then
     * this is the entry point for running your tests.  So make sure you declare one of these in the FuncTestSuite
     * implementation.
     *
     * @return a Test that can be run by as JUnit TestRunner
     */
    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteComponentsAndVersions()
    {
        addTest(TestComponentValidation.class);
        addTest(TestVersionValidation.class);
        addTest(TestVersionCustomField.class);

        addTest(TestProjectComponentQuickSearch.class);
    }
}