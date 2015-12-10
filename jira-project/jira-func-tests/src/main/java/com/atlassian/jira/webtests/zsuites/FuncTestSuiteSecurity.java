package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityActions;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityWithCustomFields;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityWithGroupsAndRoles;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityWithRoles;
import com.atlassian.jira.webtests.ztests.issue.move.TestPromptUserForSecurityLevelOnBulkMove;
import com.atlassian.jira.webtests.ztests.issue.move.TestPromptUserForSecurityLevelOnMove;
import com.atlassian.jira.webtests.ztests.misc.TestForgotLoginDetails;
import com.atlassian.jira.webtests.ztests.navigator.TestSearchRequestViewSecurity;
import com.atlassian.jira.webtests.ztests.project.TestMultipleProjectsWithIssueSecurityWithRoles;
import com.atlassian.jira.webtests.ztests.security.TestBackendActionResolution;
import com.atlassian.jira.webtests.ztests.security.TestRedirectAfterLogin;
import com.atlassian.jira.webtests.ztests.security.TestSignupWithExternalUserManagement;
import com.atlassian.jira.webtests.ztests.security.TestWebActionResolution;
import com.atlassian.jira.webtests.ztests.security.TestWebResourceRetrievalDoesNotExposeProtectedResources;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionSecurityLevel;
import com.atlassian.jira.webtests.ztests.subtask.TestSecurityLevelOfSubtasks;
import com.atlassian.jira.webtests.ztests.subtask.TestSubTaskToIssueConversionSecurityLevel;
import com.atlassian.jira.webtests.ztests.subtask.TestSubtaskSecurity;
import junit.framework.Test;

/**
 * A suite of test related to Security
 *
 * @since v4.0
 */
public class FuncTestSuiteSecurity extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteSecurity();

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

    public FuncTestSuiteSecurity()
    {
        addTest(TestIssueSecurityActions.class);
        addTest(TestIssueSecurityWithGroupsAndRoles.class);
        addTest(TestIssueSecurityWithCustomFields.class);
        addTest(TestIssueSecurityWithRoles.class);
        addTest(TestIssueToSubTaskConversionSecurityLevel.class);
        addTest(TestSubTaskToIssueConversionSecurityLevel.class);
        addTest(TestSearchRequestViewSecurity.class);
        addTest(TestMultipleProjectsWithIssueSecurityWithRoles.class);
        addTest(TestSecurityLevelOfSubtasks.class);
        addTest(TestPromptUserForSecurityLevelOnMove.class);
        addTest(TestPromptUserForSecurityLevelOnBulkMove.class);
        addTest(TestSignupWithExternalUserManagement.class);
        addTest(TestRedirectAfterLogin.class);
        addTest(TestForgotLoginDetails.class);
        addTest(TestSubtaskSecurity.class);
        addTest(TestBackendActionResolution.class);
        addTest(TestWebActionResolution.class);
        addTest(TestWebResourceRetrievalDoesNotExposeProtectedResources.class);

        addTestsInPackage("com.atlassian.jira.webtests.ztests.security", true);
        addTestsInPackage("com.atlassian.jira.webtests.ztests.admin.security.xsrf", true);
        addTestsInPackage("com.atlassian.jira.webtests.ztests.issue.security.xsrf", true);
        addTestsInPackage("com.atlassian.jira.webtests.ztests.user.security.xsrf", true);
        addTestsInPackage("com.atlassian.jira.webtests.ztests.project.security.xss", true);
    }
}