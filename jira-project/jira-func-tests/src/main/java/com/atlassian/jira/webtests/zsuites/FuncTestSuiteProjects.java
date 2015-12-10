package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.project.TestComponentValidation;
import com.atlassian.jira.webtests.ztests.project.TestDeleteProject;
import com.atlassian.jira.webtests.ztests.project.TestEditProject;
import com.atlassian.jira.webtests.ztests.project.TestEditProjectLeadAndDefaultAssignee;
import com.atlassian.jira.webtests.ztests.project.TestEditProjectPCounter;
import com.atlassian.jira.webtests.ztests.project.TestMultipleProjectsWithIssueSecurityWithRoles;
import com.atlassian.jira.webtests.ztests.project.TestProjectCategory;
import com.atlassian.jira.webtests.ztests.project.TestProjectComponentQuickSearch;
import com.atlassian.jira.webtests.ztests.project.TestProjectKeyEditOnEntityLinks;
import com.atlassian.jira.webtests.ztests.project.TestProjectKeyEditOnSearch;
import com.atlassian.jira.webtests.ztests.project.TestProjectRoles;
import com.atlassian.jira.webtests.ztests.project.TestVersionValidation;
import com.atlassian.jira.webtests.ztests.project.TestViewProjectRoleUsage;
import com.atlassian.jira.webtests.ztests.user.TestEditUserProjectRoles;
import junit.framework.Test;

/**
 * A suite of tests around JIRA projects
 *
 * @since v4.0
 */
public class FuncTestSuiteProjects extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteProjects();

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

    public FuncTestSuiteProjects()
    {
        addTest(TestComponentValidation.class);
        addTest(TestVersionValidation.class);
        addTest(TestDeleteProject.class);
        addTest(TestEditProject.class);
        addTest(TestEditProjectLeadAndDefaultAssignee.class);
        addTest(TestEditProjectPCounter.class);
        addTest(TestEditUserProjectRoles.class);
        addTest(TestViewProjectRoleUsage.class);
        addTest(TestProjectKeyEditOnEntityLinks.class);
        addTest(TestProjectKeyEditOnSearch.class);
        addTest(TestProjectRoles.class);
        addTest(TestProjectComponentQuickSearch.class);
        addTest(TestProjectCategory.class);
        addTest(TestMultipleProjectsWithIssueSecurityWithRoles.class);
    }
}