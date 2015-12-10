package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueBrowseBadProjectRegex extends FuncTestCase
{
    /**
     * Test browsing an Issue that does exist with a bad project REGEX
     */
    public void testViewIssuePageWithInvalidAssigneeAndReporters()
    {
        //import pro/ent data (with subtasks)
        administration.restoreData("TestIssueBrowseBadProjectRegex.xml");

        //Test browsing an Issue that does exist with a bad project REGEX
        tester.gotoPage("/browse/M02-1");
        text.assertTextPresent(locator.page(), "Monkey bug 1");

        //Test browsing an Issue that masks a project key.
        //This should return issue.
        tester.gotoPage("/browse/H01-1");
        text.assertTextPresent(locator.page(), "adgvadgvad");

        //Test browsing an Issue that does exist with a bad project REGEX
        tester.gotoPage("/browse/H01-1-1");
        text.assertTextPresent(locator.page(), "Awful regex bug");

        // Test browsing a link that is not found that matches a valid issue key
        tester.gotoPage("/browse/HSP-1");
        text.assertTextPresent(locator.page(), "Issue Does Not Exist");

        // Test browsing a link that is not found that DOES NOT match a valid issue key (a bad project REGEX)
        try
        {
            tester.gotoPage("/browse/H23-1");
            text.assertTextNotPresent(locator.page(), "Issue Does Not Exist");
        }
        catch (Exception e)
        {
            // Good result. Throws not found 404.
        }
    }
}
