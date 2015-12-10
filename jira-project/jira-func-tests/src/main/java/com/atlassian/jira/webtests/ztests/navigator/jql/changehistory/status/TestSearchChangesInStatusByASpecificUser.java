package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.status;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.navigator.jql.changehistory.AbstractChangeHistoryFuncTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Responsible for verifying that a user is able to query all issues that had status changes made by a specific user.
 *
 * Story @ JRADEV-3736
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestSearchChangesInStatusByASpecificUser extends AbstractChangeHistoryFuncTest
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestWasSearch.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testOnlyReturnsTheIssuesChangedToAStatusByTheSpecifiedUser()
    {
        navigation.login(FRED_USERNAME);
        navigation.issue().closeIssue("HSP-1","Fixed","Fixed");

        navigation.login(ADMIN_USERNAME);
        navigation.issue().closeIssue("HSP-2","Fixed","Fixed");

        assertExactSearchWithResults("status was Closed order by key asc", "HSP-1", "HSP-2");

        assertSearchWithResults("status was Closed by fred", "HSP-1");
        assertSearchWithResults("status was Closed by admin", "HSP-2");
    }
}
