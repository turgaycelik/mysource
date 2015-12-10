package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS })
public class TestBulkTransition extends FuncTestCase
{
    /**
     * Tests for regression of http://jira.atlassian.com/browse/JRA-18359
     */
    public void testBulkTransitionDuplicateWorkflows()
    {
        administration.restoreData("TestBulkTransitionDuplicateWorkflows.xml");
        // Click Link 'Issues' (id='find_link').
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.checkCheckbox("bulkedit_10001", "on");
        tester.checkCheckbox("bulkedit_10002", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.assertTextPresent("Step 2 of 4: Choose Operation");
        tester.submit("Next");
        tester.assertTextPresent("Step 3 of 4: Operation Details");
        tester.assertTextPresent("Select the workflow transition to execute on the associated issues.");
        // Assert that we don't have multiple copies of Workflow
        text.assertTextPresentNumOccurences("Workflow: classic default workflow", 1);
    }
}
