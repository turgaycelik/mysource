package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.comment.TestAddCommentFooterLink;
import com.atlassian.jira.webtests.ztests.comment.TestAddCommentHeaderLink;
import com.atlassian.jira.webtests.ztests.comment.TestCommentDelete;
import com.atlassian.jira.webtests.ztests.comment.TestCommentNotifications;
import com.atlassian.jira.webtests.ztests.comment.TestCommentVisibility;
import com.atlassian.jira.webtests.ztests.comment.TestEditComment;
import com.atlassian.jira.webtests.ztests.issue.TestWikiRendererXSS;
import com.atlassian.jira.webtests.ztests.issue.comments.TestCommentPermissions;
import com.atlassian.jira.webtests.ztests.issue.move.TestDeleteHiddenFieldOnMove;
import com.atlassian.jira.webtests.ztests.misc.TestReplacedLocalVelocityMacros;
import junit.framework.Test;

/**
 * A suite of test related to comments
 *
 * @since v4.0
 */
public class FuncTestSuiteComments extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteComments();

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

    public FuncTestSuiteComments()
    {
        addTest(TestDeleteHiddenFieldOnMove.class);
        addTest(TestEditComment.class);
        addTest(TestCommentDelete.class);
        addTest(TestCommentNotifications.class);
        // NOTE: This test has needs to have some tests commented in once we figure out why httpunit is not
        addTest(TestAddCommentHeaderLink.class);
        addTest(TestAddCommentFooterLink.class);
        addTest(TestCommentVisibility.class);
        addTest(TestCommentPermissions.class);
        addTest(TestReplacedLocalVelocityMacros.class);
        addTest(TestWikiRendererXSS.class);
    }
}