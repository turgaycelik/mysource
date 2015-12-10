package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.issue.move.TestDeleteHiddenFieldOnMove;
import com.atlassian.jira.webtests.ztests.issue.move.TestMoveIssue;
import com.atlassian.jira.webtests.ztests.issue.move.TestMoveIssueAndRemoveFields;
import com.atlassian.jira.webtests.ztests.issue.move.TestMoveIssueAttachment;
import com.atlassian.jira.webtests.ztests.issue.move.TestMoveIssueForEnterprise;
import com.atlassian.jira.webtests.ztests.issue.move.TestPromptUserForSecurityLevelOnBulkMove;
import com.atlassian.jira.webtests.ztests.issue.move.TestPromptUserForSecurityLevelOnMove;
import com.atlassian.jira.webtests.ztests.issue.move.TestRedirectToMovedIssues;
import junit.framework.Test;

/**
 * Tests for moving issues. For bulk operations see {@link com.atlassian.jira.webtests.zsuites.FuncTestSuiteBulkOperations}.
 *
 * @since v4.0
 */
public class FuncTestSuiteMoveIssue extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteMoveIssue();

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

    public FuncTestSuiteMoveIssue()
    {
        addTest(TestDeleteHiddenFieldOnMove.class);
        addTest(TestMoveIssue.class);
        addTest(TestMoveIssueAndRemoveFields.class);
        addTest(TestMoveIssueAttachment.class);
        addTest(TestMoveIssueForEnterprise.class);
        addTest(TestPromptUserForSecurityLevelOnBulkMove.class);
        addTest(TestPromptUserForSecurityLevelOnMove.class);
        addTest(TestRedirectToMovedIssues.class);
    }
}