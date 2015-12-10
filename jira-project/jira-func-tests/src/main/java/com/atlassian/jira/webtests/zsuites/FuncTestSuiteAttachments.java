package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.security.xsrf.TestXsrfAttachments;
import com.atlassian.jira.webtests.ztests.issue.clone.TestCloneIssueAttachments;
import com.atlassian.jira.webtests.ztests.issue.move.TestMoveIssueAttachment;
import junit.framework.Test;

/**
 * Tests for attachments.
 *
 * @since v4.0
 */
public class FuncTestSuiteAttachments extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteAttachments();

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
    
    public FuncTestSuiteAttachments()
    {
        addTestsInPackage("com.atlassian.jira.webtests.ztests.attachment", true);
        addTest(TestMoveIssueAttachment.class);
        addTest(TestXsrfAttachments.class);
        addTest(TestCloneIssueAttachments.class);
    }
}