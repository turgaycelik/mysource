package com.atlassian.jira.webtests.ztests.issue.move;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.MOVE_ISSUE })
public class TestRedirectToMovedIssues extends JIRAWebTest
{
    public TestRedirectToMovedIssues(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestRedirectToMovedIssues.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    /*
        Test data consists of two chains of issue move events:

            HSP-1 -> MKY-2 -> HSP-3
            and
            MKY-1 -> HSP-2`
     */

    public void testRedirectToMovedIssueBrowse()
    {
        gotoPage("/browse/MKY-1");
        assertTrue(getRedirect().endsWith("/browse/HSP-2"));
        //assert that we're definitely looking at a normal browse issue view (create issue link should be present)
        assertLinkPresent("create_link");
        assertTextPresent("Test Bug 2");
    }

    public void testRedirectToMovedIssueXML()
    {
        //goto an issue that's been moved
        gotoPage("/si/jira.issueviews:issue-xml/MKY-1/MKY-1.xml");
        //assert that we've been redirected
        assertTrue(getRedirect().endsWith("/si/jira.issueviews:issue-xml/HSP-2/HSP-2.xml"));
        //assert that we're definitely looking at an XML view
        assertTextPresent("This file is an XML representation of an issue");
        //assert that we're definitely looking at the right bug
        assertTextPresent("Test Bug 2");
    }

    public void testRedirectToMovedIssuePrintable()
    {
        gotoPage("/si/jira.issueviews:issue-html/MKY-1/MKY-1.html");
        assertTrue(getRedirect().endsWith("/si/jira.issueviews:issue-html/HSP-2/HSP-2.html"));
        //assert that we're definitely looking at a Printable view (back to previous view link should be present)
        assertTextPresent("Back to previous view");
        assertTextPresent("Test Bug 2");

    }

    public void testRedirectToMovedIssueWord()
    {
        gotoPage("/si/jira.issueviews:issue-word/MKY-1/MKY-1.doc");
        assertTrue(getRedirect().endsWith("/si/jira.issueviews:issue-word/HSP-2/HSP-2.doc"));
        //assert that we're definitely looking at an MS-Word view (META content header should contain "application/vnd.ms-word")
        assertTextPresent("application/vnd.ms-word");
        assertTextPresent("Test Bug 2");
    }

    public void testRedirectToTwiceMovedIssue()
    {
        //assert QuickLinkServlet (for /browse/ url patterns) is working
        gotoPage("/browse/HSP-1");
        assertTrue(getRedirect().endsWith("/browse/HSP-3"));

        gotoPage("/browse/MKY-2");
        assertTrue(getRedirect().endsWith("/browse/HSP-3"));

        //and IssueViewURLHandler (for plugin issue views) is working too
        gotoPage("/si/jira.issueviews:issue-xml/HSP-1/HSP-1.xml");
        assertTrue(getRedirect().endsWith("/si/jira.issueviews:issue-xml/HSP-3/HSP-3.xml"));

        gotoPage("/si/jira.issueviews:issue-xml/MKY-2/MKY-2.xml");
        assertTrue(getRedirect().endsWith("/si/jira.issueviews:issue-xml/HSP-3/HSP-3.xml"));
    }

    public void testRedirectToMovedIssueBrowseWithQueryString()
    {
        //only one query string param
        gotoPage("/browse/MKY-1?jql=haha");
        assertTrue(getRedirect().endsWith("/browse/HSP-2?jql=haha"));
    }

    public void testRedirectToMovedIssueXMLWithQueryString()
    {
        //couple of query string params, for good luck
        gotoPage("/si/jira.issueviews:issue-html/MKY-1/MKY-1.html?key1=value1&key2=value2");
        assertTrue(getRedirect().endsWith("/si/jira.issueviews:issue-html/HSP-2/HSP-2.html?key1=value1&key2=value2"));
    }

}
