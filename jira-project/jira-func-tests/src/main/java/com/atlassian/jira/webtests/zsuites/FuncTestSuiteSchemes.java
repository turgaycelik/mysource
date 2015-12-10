package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.TestPermissionSchemes;
import com.atlassian.jira.webtests.ztests.admin.issuetypes.TestIssueTypeSchemeMigration;
import com.atlassian.jira.webtests.ztests.admin.issuetypes.TestIssueTypeSchemes;
import com.atlassian.jira.webtests.ztests.admin.scheme.TestSchemeComparisonTool;
import com.atlassian.jira.webtests.ztests.admin.scheme.TestSchemeMergeTool;
import com.atlassian.jira.webtests.ztests.admin.scheme.TestSchemePurgeTool;
import com.atlassian.jira.webtests.ztests.admin.scheme.TestSchemeTools;
import com.atlassian.jira.webtests.ztests.fields.TestFieldConfigurationSchemes;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkFlowSchemes;

import junit.framework.Test;

/**
 * A suite of tests around Schemes
 *
 * @since v4.0
 */
public class FuncTestSuiteSchemes extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteSchemes();

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

    public FuncTestSuiteSchemes()
    {
        addTest(TestSchemeComparisonTool.class);
        addTest(TestSchemeMergeTool.class);
        addTest(TestSchemePurgeTool.class);
        addTest(TestSchemeTools.class);
        addTest(TestPermissionSchemes.class);
        addTest(TestFieldConfigurationSchemes.class);
        addTest(TestWorkFlowSchemes.class);
        addTest(TestIssueTypeSchemes.class);
        addTest(TestIssueTypeSchemeMigration.class);
    }
}