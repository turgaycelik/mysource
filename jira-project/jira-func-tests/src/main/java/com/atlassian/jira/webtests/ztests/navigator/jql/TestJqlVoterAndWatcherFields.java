package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestJqlVoterAndWatcherFields extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestJqlVoterAndWatcherFields.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, BOB_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, FRED_USERNAME);
    }

    public void testVoterField()
    {
        assertSearchWithResultsForUser(BOB_USERNAME, "voter = currentUser()",  "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(BOB_USERNAME, "voter = bob", "HSP-4", "HSP-3");
        // check that it ignores case
        assertSearchWithResultsForUser(BOB_USERNAME, "voter = BoB", "HSP-4", "HSP-3");

        assertSearchWithResultsForUser(BOB_USERNAME, "voter = fred");
        assertSearchWithResultsForUser(BOB_USERNAME, "voter = FreD");

        // sees own issue despite not being able to see other votes
        assertSearchWithResultsForUser(FRED_USERNAME, "voter = currentUser()", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(FRED_USERNAME, "voter = fred", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(FRED_USERNAME, "voter = FreD", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(FRED_USERNAME, "voter = bob", "HSP-4");
        // check that it ignores case
        assertSearchWithResultsForUser(FRED_USERNAME, "voter = BoB", "HSP-4");

        // reassign HSP-2 to fred
        assertSearchWithResultsForUser(ADMIN_USERNAME, "voter = fred", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(ADMIN_USERNAME, "voter = FreD", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(ADMIN_USERNAME, "voter = bob", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(ADMIN_USERNAME, "voter = BoB", "HSP-4", "HSP-3");

        assertSearchWithWarningForUser(ADMIN_USERNAME, "voter = dingbat", "The value 'dingbat' does not exist for the field 'voter'.");
    }

    public void testWatcherField()
    {
        assertSearchWithResultsForUser(BOB_USERNAME, "watcher = currentUser()",  "HSP-3");
        assertSearchWithResultsForUser(BOB_USERNAME, "watcher = bob", "HSP-3");
        // check that it ignores case
        assertSearchWithResultsForUser(BOB_USERNAME, "watcher = BoB", "HSP-3");

        assertSearchWithResultsForUser(BOB_USERNAME, "watcher = fred");
        assertSearchWithResultsForUser(BOB_USERNAME, "watcher = FreD");

        // sees own issue despite not being able to see other votes
        assertSearchWithResultsForUser(FRED_USERNAME, "watcher = currentUser()", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(FRED_USERNAME, "watcher = fred", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(FRED_USERNAME, "watcher = FreD", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(FRED_USERNAME, "watcher = bob");
        // check that it ignores case
        assertSearchWithResultsForUser(FRED_USERNAME, "watcher = BoB");

        // reassign HSP-2 to fred
        assertSearchWithResultsForUser(ADMIN_USERNAME, "watcher = fred", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(ADMIN_USERNAME, "watcher = FreD", "HSP-4", "HSP-3");
        assertSearchWithResultsForUser(ADMIN_USERNAME, "watcher = bob", "HSP-3");
        assertSearchWithResultsForUser(ADMIN_USERNAME, "watcher = BoB", "HSP-3");

        assertSearchWithWarningForUser(ADMIN_USERNAME, "watcher = dingbat", "The value 'dingbat' does not exist for the field 'watcher'.");
    }
}