package com.atlassian.jira.webtests.ztests.navigator.jql.changehistory;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Test change history in different timezones.
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL, Category.CHANGE_HISTORY })
public class TestChangeHistoryTimeZone extends FuncTestCase
{
    public void testTimeZones()
    {
        //This data looks like this (times in GMT):
        //HSP-2:
        //  00:00 25/06/2011 --> 20:59 25/06/2011 -> Inf
        //  Open                 Resolved
        //HSP-3:
        //  00:00 25/06/2011 --> 21:00 25/06/2011 -> Inf
        //  Open                 Resolved
        administration.restoreData("TestChangeHistoryTimeZone.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, FRED_USERNAME);

        //Admin is in time zone GMT. So (times in GMT):
        // status was resolved before "2011/6/26" => created < 00:00:00 2011/6/26.
        //This matches both HSP-3 and HSP-2.
        navigation.issueNavigator().createSearch("status was resolved before \"2011/6/26\"");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("HSP-3", "HSP-2");

        navigation.login("fred");

        //Fred is in GMT+3. So (time is GMT):
        // status was resolved before "2011/6/26" => created < 21:00:00 2011/6/25
        //This only matches HSP-2.
        navigation.issueNavigator().createSearch("status was resolved before \"2011/6/26\"");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("HSP-2");
    }
}
