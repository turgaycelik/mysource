package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.FILTERS })
public class TestDanglingGroups extends FuncTestCase
{
    /**
     * This test uses a specially crafted XML which has a "dangling" group. The group has a subscription
     * but doesn't exist in JIRA. This simulates a group that is externally managed (i.e. via Crowd) being deleted.
     * In that scenario, JIRA never has a chance to "clean up" these groups. But we still want to call them out
     * to the admin in the View Subscriptions screen.
     */
    public void testDanglingGroups()
    {
        administration.restoreData("TestDanglingGroups.xml");

        navigation.manageFilters().goToDefault();
        navigation.manageFilters().manageSubscriptions(10000);

        // assert that the group is shown and is inside a span with a title
        assertEquals
                (
                        locator.css("span.warning[title]").getNode().getAttributes().getNamedItem("title").getNodeValue(),
                        "Warning! This group has been deleted."
                );
        text.assertTextPresent(locator.css("span.warning[title"),"delete-me");
    }
}