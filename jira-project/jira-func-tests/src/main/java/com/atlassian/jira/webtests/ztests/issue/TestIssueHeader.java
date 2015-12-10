package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueHeader extends FuncTestCase
{
    public void testIssueHeaderTablePresent()
    {
        administration.restoreData("TestIssueHeader.xml");

        //check components/versions present on view issue page
        navigation.issue().viewIssue("HSP-1");
        tester.assertLinkPresentWithText("New Component 1");
        tester.assertLinkNotPresentWithText("New Version 1");
        tester.assertLinkPresentWithText("New Version 5");
    }

    public void testSubTaskBreadcrumbs() throws Exception
    {
        administration.restoreData("TestIssueHeaderSubTaskBreadcrumbs.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.issue().viewIssue("HSP-1");

        // test XSS (note: can't use Locator-based assertions since they convert encoded characters into their proper form)
        text.assertTextPresent("Test &lt;em&gt;Bug One&lt;/em&gt; which has a long long summary longer than the abbreviation limit");
        text.assertTextNotPresent("Test <em>Bug One</em> which has a long long summary longer than the abbreviation limit");

        navigation.issue().viewIssue("HSP-3");

        // test XSS
        text.assertTextPresent("HSP-1 Test &lt;em&gt;Bug One&lt;/em&gt; which has a long long summary longer than the abbreviation limit");
        text.assertTextNotPresent("Test <em>Bug One</em> which has a long long summary longer than the abbreviation limit");

        // test tooltip shows full summary - note we must use non-escaped format
        assertions.assertNodeExists("//a[@id='parent_issue_summary'][@title='Test <em>Bug One</em> which has a long long summary longer than the abbreviation limit']");
    }
}
