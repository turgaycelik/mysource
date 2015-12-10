package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestNextPreviousPager extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("BigIssueSet.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testAlwaysGreen()
    {
        assert(true);
    }

//    TODO: Move to Kickass. As of JIRA 6.0 pager is generated on the client side. It is not longer possible to get a page that contains a pager from the server
//    public void testPager()
//    {
//        // No Issues 10011
//        navigation.issueNavigator().loadFilter(10011, null);
//
//        Locator locator = new WebPageLocator(tester);
//        tester.assertElementNotPresent("issuetable");
//
//        navigation.issue().viewIssue("TEST-1");
//        locator = new WebPageLocator(tester);
//        tester.assertLinkNotPresent("previous-issue");
//
//        tester.clickLink("find_link");
//
//        locator = new WebPageLocator(tester);
//        tester.assertElementNotPresent("issuetable");
//
//        // 11 issues 10012
//        navigation.issueNavigator().loadFilter(10012, null);
//
//        locator = new WebPageLocator(tester);
//        tester.assertTextPresent(">1</span>");
//        tester.assertTextPresent(">11</span>");
//        tester.assertTextPresent(">11</span>");
//
//        // Test that iterating over the list forward works starting from first issue
//        assertPagerListForwards("11 Issues", "TEST-99", "TEST-990", "TEST-991", "TEST-992", "TEST-993", "TEST-994", "TEST-995", "TEST-996", "TEST-997", "TEST-998", "TEST-999");
//
//        // Get new filter into session
//        navigation.issueNavigator().loadFilter(10011, null);
//        navigation.issue().viewIssue("TEST-99");
//
//        // Test that iterating over the list backwards works starting from the end
//        navigation.issueNavigator().loadFilter(10012, null);
//        assertPagerListBackwards("11 Issues", "TEST-99", "TEST-990", "TEST-991", "TEST-992", "TEST-993", "TEST-994", "TEST-995", "TEST-996", "TEST-997", "TEST-998", "TEST-999");
//
//        // Edit issue (to force reader to swapped out from underneath
//        tester.gotoPage("/secure/EditIssue!default.jspa?id=11168");
//        tester.setFormElement("summary", "NEW SUMMARY for TEST-99");
//
//        // assert pager is still working
//        assertPagerListForwards("11 Issues", "TEST-99", "TEST-990", "TEST-991", "TEST-992", "TEST-993", "TEST-994", "TEST-995", "TEST-996", "TEST-997", "TEST-998", "TEST-999");
//
//        // 57 Issues 10013
//        // Test that issue key cache doesn't break
//        navigation.issueNavigator().loadFilter(10013, null);
//        navigation.issue().viewIssue("TEST-1");
//        assertPager("TEST-1", "57 Issues", 57, 1);
//
//        navigation.issue().viewIssue("TEST-163");
//        assertPager("TEST-163", "57 Issues", 57, 20);
//
//        tester.clickLink("next-issue");
//
//        navigation.issue().viewIssue("TEST-164");
//        assertPager("TEST-164", "57 Issues", 57, 21);
//
//        // Get new filter into session
//        navigation.issueNavigator().loadFilter(10011, null);
//        navigation.issue().viewIssue("TEST-99");
//
//
//        // 57 Issues 10013
//        // Test cache when reader gets swapped out.
//        navigation.issueNavigator().loadFilter(10013, null);
//        navigation.issue().viewIssue("TEST-1");
//        assertPager("TEST-1", "57 Issues", 57, 1);
//
//        // Edit issue (to force reader to swapped out from underneath
//        tester.gotoPage("/secure/EditIssue!default.jspa?id=11168");
//        tester.setFormElement("summary", "Yet Another NEW SUMMARY for TEST-99");
//
//        navigation.issue().viewIssue("TEST-183");
//        assertPager("TEST-183", "57 Issues", 57, 40);
//
//    }

    private void assertPagerListForwards(String filterName, String... keys)
    {
        navigation.issue().viewIssue(keys[0]);
        for (int i = 0; i < keys.length; i++)
        {
            assertPager(keys[i], filterName, keys.length, i + 1);
            if (i != keys.length - 1)
            {
                tester.clickLink("next-issue");
            }
        }
        for (int i = keys.length; i > 0; i--)
        {
            assertPager(keys[i - 1], filterName, keys.length, i );
            if (i != 1)
            {
                tester.clickLink("previous-issue");
            }
        }


    }
    private void assertPagerListBackwards(String filterName, String... keys)
    {
        navigation.issue().viewIssue(keys[keys.length - 1]);
        for (int i = keys.length; i > 0; i--)
        {
            assertPager(keys[i - 1], filterName, keys.length, i );
            if (i != 1)
            {
                tester.clickLink("previous-issue");
            }
        }
        for (int i = 0; i < keys.length; i++)
        {
            assertPager(keys[i], filterName, keys.length, i + 1);
            if (i != keys.length - 1)
            {
                tester.clickLink("next-issue");
            }
        }


    }

    private void assertPager(String key, String filter, int total, int position)
    {
        Locator locator;
        locator = new WebPageLocator(tester);

        text.assertTextPresent(locator, "Test issue " + key);
        tester.assertLinkPresent("return-to-search");
        if (position == 1)
        {
            tester.assertLinkNotPresent("previous-issue");
        }
        else
        {
            text.assertTextSequence(locator, "Previous Issue", position + " of " + total);
            tester.assertLinkPresent("previous-issue");
        }
        if (position == total)
        {
            tester.assertLinkNotPresent("next-issue");
        }
        else
        {
            text.assertTextSequence(locator, position + " of " + total, "Next Issue");
            tester.assertLinkPresent("next-issue");
        }
    }

}
