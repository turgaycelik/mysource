package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.issue.TestCloneIssueWithSubTasks;
import com.atlassian.jira.webtests.ztests.issue.TestCloneIssueWithValidation;
import com.atlassian.jira.webtests.ztests.issue.clone.TestCloneIssueAttachments;
import junit.framework.Test;

/**
 *A suite of SubTask related test
 *
 * @since v4.0
 */
public class FuncTestSuiteCloneIssue extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteCloneIssue();

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

    public FuncTestSuiteCloneIssue()
    {
        addTest(TestCloneIssueWithSubTasks.class);
        addTest(TestCloneIssueAttachments.class);
        addTest(TestCloneIssueWithValidation.class);
    }
}