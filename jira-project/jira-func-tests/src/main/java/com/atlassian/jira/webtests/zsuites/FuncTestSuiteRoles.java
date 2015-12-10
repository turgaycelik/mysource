package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityWithGroupsAndRoles;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityWithRoles;
import com.atlassian.jira.webtests.ztests.misc.TestFullContentShowsRoleEncoded;
import com.atlassian.jira.webtests.ztests.project.TestMultipleProjectsWithIssueSecurityWithRoles;
import com.atlassian.jira.webtests.ztests.project.TestProjectRoles;
import com.atlassian.jira.webtests.ztests.project.TestViewProjectRoleUsage;
import com.atlassian.jira.webtests.ztests.user.TestEditUserProjectRoles;
import com.atlassian.jira.webtests.ztests.user.TestGroupToRoleMappingTool;
import junit.framework.Test;

/**
 * A suite of tests related to Roles
 *
 * @since v4.0
 */
public class FuncTestSuiteRoles extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteRoles();

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

    public FuncTestSuiteRoles()
    {
        addTest(TestGroupToRoleMappingTool.class);
        addTest(TestEditUserProjectRoles.class);
        addTest(TestViewProjectRoleUsage.class);
        addTest(TestProjectRoles.class);
        addTest(TestIssueSecurityWithGroupsAndRoles.class);
        addTest(TestIssueSecurityWithRoles.class);
        addTest(TestMultipleProjectsWithIssueSecurityWithRoles.class);
        addTest(TestFullContentShowsRoleEncoded.class);
    }
}