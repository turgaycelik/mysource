package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * JRA-12739
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.PROJECTS, Category.ROLES, Category.SECURITY })
public class TestMultipleProjectsWithIssueSecurityWithRoles extends FuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestMultipleProjectsWithIssueSecurityWithRoles.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testIssueWithSecurityLevelNotAccessibleInNavigator()
    {
        //first login as a user not in the developer role.
        //Should not be able to see Test Issue 2 in project 1.
        //Should be able to see My Bug which has security level set to reporter
        navigation.logout();
        navigation.login("nondeveloper", "nondeveloper");
        navigation.issueNavigator().gotoNavigator();
        text.assertTextPresent(locator.page(), "Test Issue 1 (TP2)");
        text.assertTextNotPresent(locator.page(), "Test Issue 2 (TP1)");
        text.assertTextPresent(locator.page(), "Test Issue 1 (TP1)");
        text.assertTextPresent(locator.page(), "My Bug");
        navigation.issue().gotoIssue("TPONE-2");
        text.assertTextPresent(locator.page(), "It seems that you have tried to perform an operation which you are not permitted to perform.");

        // login as user in developer role.  Should be able to see Test Issue 2(TP1) but
        // not My Bug which has reporter security level
        navigation.logout();
        navigation.login("developer", "developer");
        navigation.issueNavigator().gotoNavigator();
        text.assertTextPresent(locator.page(), "Test Issue 1 (TP2)");
        text.assertTextPresent(locator.page(), "Test Issue 2 (TP1)");
        text.assertTextPresent(locator.page(), "Test Issue 1 (TP1)");
        text.assertTextNotPresent(locator.page(), "My Bug");
    }
}
