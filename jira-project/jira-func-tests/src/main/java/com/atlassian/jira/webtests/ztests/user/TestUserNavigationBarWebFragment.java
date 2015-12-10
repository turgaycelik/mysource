package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests that the web fragment links on the user navigation bar is visible with correct permissions.
 * 
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.USERS_AND_GROUPS })
public class TestUserNavigationBarWebFragment extends FuncTestCase
{
    private static final String BACK_TO_PREVIOUS_VIEW = "Back to previous view";
    private static final String ISSUE_SUMMARY = "test printable";

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestWebFragment.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    @Override
    public void tearDownTest()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.restoreBlankInstance();
        super.tearDownTest();
    }

    public void testUserNavigationBarWebFragment()
    {
        _testLinkVisibilityWhileLoggedIn();
        _testLinkVisibilityWhileNotLoggedIn();
    }

    /**
     * Tests that the printable view link is rendered properly, even for non-servlet's - JRA-11527
     */
    public void testPrintableViewLink()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        //assert printable link is valid in issue view (no query string)
        final String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", ISSUE_SUMMARY);
        navigation.issue().viewIssue(issueKey);
        tester.assertTextPresent("Details");
        tester.assertLinkPresentWithText(issueKey);
        tester.gotoPage("/si/jira.issueviews:issue-html/HSP-1/HSP-1.html");
        tester.assertLinkPresentWithText(BACK_TO_PREVIOUS_VIEW);
        tester.assertTextPresent("[" + issueKey + "]");
        tester.assertLinkPresentWithText(ISSUE_SUMMARY);

        //assert printable link is valid in the issue navigator (has query string)
        tester.gotoPage("/secure/IssueNavigator.jspa?reset=true&sorter/field=issuekey&sorter/order=DESC");
        tester.assertTextPresent("Issue Navigator");
        navigation.issueNavigator().displayPrintableAllIssues();
        tester.assertLinkPresentWithText(BACK_TO_PREVIOUS_VIEW);
        tester.assertTextPresent(issueKey);
        tester.assertTextPresent(ISSUE_SUMMARY);

        //assert printable link is valid for non-servlet based page (eg. securitybreach.jsp)
        tester.gotoPage("/secure/views/securitybreach.jsp");
        tester.assertTextPresent("Access Denied");
        tester.gotoPage("/secure/views/securitybreach.jsp?decorator=printable");
        tester.assertLinkPresentWithText(BACK_TO_PREVIOUS_VIEW);
        tester.assertTextPresent("Access Denied");
    }

    private void _testLinkVisibilityWhileLoggedIn()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        tester.assertLinkNotPresentWithText("Log In");

        assertTrue(navigation.userProfile().link().isPresent());
        assertEquals(ADMIN_FULLNAME, navigation.userProfile().userName());

        tester.assertLinkPresentWithText("Online Help");
        tester.assertLinkPresentWithText("Profile");
        tester.assertLinkPresentWithText("About JIRA");
        tester.assertLinkPresentWithText("Profile");
        tester.assertLinkPresentWithText("Log Out");
    }

    private void _testLinkVisibilityWhileNotLoggedIn()
    {
        //check the links after logging out
        navigation.logout();
        tester.beginAt("/secure/Dashboard.jspa"); //go back to the dashboard
        tester.assertLinkPresentWithText("Log In");
        tester.assertLinkNotPresentWithText("Log Out");

        assertFalse(navigation.userProfile().link().isPresent());
    }
}
