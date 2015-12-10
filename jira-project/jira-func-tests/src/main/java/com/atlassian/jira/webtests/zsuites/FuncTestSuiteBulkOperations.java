package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkChangeIssues;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkDeleteIssues;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkEditEnvironment;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkEditIssues;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkEditIssuesXss;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkEditUserGroups;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveAttachments;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveIssues;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveIssuesForEnterprise;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveIssuesNotifications;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveMappingVersionsAndComponents;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveWithMultiContexts;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkOperationCustomField;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkOperationIssueNavigator;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkOperationsIndexing;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkTransition;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkWorkflowTransition;
import com.atlassian.jira.webtests.ztests.email.TestBulkDeleteIssuesNotifications;
import com.atlassian.jira.webtests.ztests.email.TestBulkWorkflowTransitionNotification;
import com.atlassian.jira.webtests.ztests.email.TestSendBulkMail;
import com.atlassian.jira.webtests.ztests.issue.TestLabelsFormats;
import com.atlassian.jira.webtests.ztests.issue.move.TestPromptUserForSecurityLevelOnBulkMove;
import junit.framework.Test;

/**
 * A suite of tests related to Bulk Operations
 *
 * @since v4.0
 */
public class FuncTestSuiteBulkOperations extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteBulkOperations();

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

    public FuncTestSuiteBulkOperations()
    {
        addTest(TestSendBulkMail.class);
        addTest(TestBulkEditUserGroups.class);
        addTest(TestBulkChangeIssues.class);
        // Bulk Move Tests
        addTest(TestBulkMoveIssues.class);
        addTest(TestBulkMoveMappingVersionsAndComponents.class);
        addTest(TestBulkMoveIssuesForEnterprise.class);
        addTest(TestBulkMoveIssuesNotifications.class);
        // NOTE: The Bulk Edit tests delete all previously created issues
        addTest(TestBulkEditIssues.class);
        addTest(TestBulkEditIssuesXss.class);
        addTest(TestBulkDeleteIssues.class);
        addTest(TestBulkMoveWithMultiContexts.class);
        addTest(TestBulkEditEnvironment.class);
        // Used to test the bulk transition functions
        addTest(TestBulkWorkflowTransition.class);
        addTest(TestBulkOperationCustomField.class);
        addTest(TestBulkOperationsIndexing.class);
        addTest(TestBulkOperationIssueNavigator.class);
        addTest(TestBulkWorkflowTransitionNotification.class);
        addTest(TestBulkDeleteIssuesNotifications.class);
        addTest(TestPromptUserForSecurityLevelOnBulkMove.class);
        addTest(TestBulkTransition.class);
        addTest(TestLabelsFormats.class);
        addTest(TestBulkMoveAttachments.class);
    }
}