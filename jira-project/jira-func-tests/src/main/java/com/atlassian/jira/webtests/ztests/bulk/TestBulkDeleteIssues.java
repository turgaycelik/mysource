package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUES })
public class TestBulkDeleteIssues extends FuncTestCase
{
    private static final String SESSION_TIMEOUT_MESSAGE = "Your session timed out while performing bulk operation on issues.";

    /**
     * SETUP_ISSUE_COUNT is the number of 'known' issues to add<br> 'known' issues are issues that are used to control
     * some of the events<br> and to validate through the bulk edit process
     */
    private static final int SETUP_ISSUE_COUNT = 51;


    /**
     * Setup for an actual test
     */
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        backdoor.restoreBlankInstance();
        produceIssues(PROJECT_HOMOSAP_KEY, SETUP_ISSUE_COUNT);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }


    private void produceIssues(final String projectKey, final int howMany)
    {
        for (int i = 0; i < howMany; i++)
        {
            final String summary = Integer.toBinaryString(i);
            if (backdoor.issues().createIssue(projectKey, summary).id() == null)
            {
                fail(String.format("Failed at adding issue: '%s' while adding %d out of %d issues.", summary, i + 1, SETUP_ISSUE_COUNT));
            }
        }
    }

    /*
     * Tests that the bulk operation limits work on issue navigator and through the bulk delete wizard.
     * NOTE!! If this test runs out of memory, increase the amount of heap for the web client process. 256m is good.
     */
    public void testBulkDeleteIssuesLimited() throws Exception
    {
        // test for JRA-9828 OOME on bulk delete
        produceIssues(PROJECT_MONKEY_KEY, 123);

        final String overflowProtectionPropertyKey = "jira.bulk.edit.limit.issue.count";
        final String currentLimitSetting = backdoor.applicationProperties().getString(overflowProtectionPropertyKey);
        backdoor.applicationProperties().setString(overflowProtectionPropertyKey, "100");
        try
        {
            navigation.issueNavigator().runSearch("project=" + PROJECT_MONKEY_KEY);
            final BulkChangeWizard wizard = navigation.issueNavigator()
                    .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);

            tester.assertTextPresent("Bulk changes are currently limited to 100 issues."); // tooltip

            wizard.selectAllIssues().chooseOperation(BulkChangeWizard.BulkOperationsImpl.DELETE).complete();
            waitAndReloadBulkOperationProgressPage();
            tester.assertLinkPresentWithText("MKY-23");
            tester.assertLinkNotPresentWithText("MKY-24");
            assertions.getURLAssertions().assertCurrentURLMatchesRegex(".*/issues/\\?jql=project.*MKY");
        }
        finally
        {
            backdoor.applicationProperties().setString(overflowProtectionPropertyKey, currentLimitSetting);
        }
    }

    /**
     * tests to see if deleting all issues in the current page works.
     */
    public void testBulkDeleteAllIssuesInCurrentPage()
    {
        navigation.issueNavigator().displayAllIssues();

        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.CURRENT_PAGE)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.DELETE)
                .complete();
        waitAndReloadBulkOperationProgressPage();

        // goes back to issue nav?
        assertions.getURLAssertions().assertCurrentURLMatchesRegex(".*/issues/\\?jql.*");
        navigation.issueNavigator().displayAllIssues();
        tester.assertLinkNotPresentWithText(Integer.toBinaryString(25));
        tester.assertLinkPresentWithText(Integer.toBinaryString(0));
        assertIssueNotIndexed("HSP-10");
    }

    /**
     * tests to see if deleting all issues in all the pages works.<br> ie. deletes all issues
     */
    public void testBulkDeleteAllIssuesInAllPages()
    {
        navigation.issueNavigator().displayAllIssues();

        navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.DELETE)
                .complete();
        waitAndReloadBulkOperationProgressPage();

        // goes back to issue nav?
        assertions.getURLAssertions().assertCurrentURLMatchesRegex(".*/issues/\\?jql.*");

        navigation.issueNavigator().displayAllIssues();
        // no issues:
        tester.assertElementNotPresent("issuetable");
    }

    public void testBulkDeleteSessionTimeouts()
    {
        log("Bulk Delete - Test that you get redirected to the session timeout page when jumping into the wizard");

        navigation.gotoPage("secure/views/bulkedit/BulkDeleteDetails.jspa");
        tester.assertTextPresent(SESSION_TIMEOUT_MESSAGE);
        navigation.gotoPage("secure/BulkDeleteDetailsValidation.jspa");
        tester.assertTextPresent(SESSION_TIMEOUT_MESSAGE);
    }

    private void assertIssueNotIndexed(final String key)
    {
        log("Checking that item " + key + " was deleted in the index.");
        try
        {
            navigation.gotoPage("/si/jira.issueviews:issue-xml/" + key + "/" + key + ".xml?jira.issue.searchlocation=index");
            fail("Unexpectedly - issue page exists, issue was not deleted.");
        }
        catch (RuntimeException e)
        {
            // expected - cant get more specific in terms of exception type however.
        }
    }
}
