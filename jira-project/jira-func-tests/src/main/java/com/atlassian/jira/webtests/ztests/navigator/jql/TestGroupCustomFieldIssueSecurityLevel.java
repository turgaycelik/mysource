package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * This is test to verify the function of security levels used together
 * with custom fields group selectors such as "single select" or "multi select".
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestGroupCustomFieldIssueSecurityLevel extends FuncTestCase
{
    public void testUserWithCorrectPermissionsShouldSeeTheResult()
    {
        administration.restoreData("JRA-29196.xml");

        navigation.login("test-user");
        navigation.gotoPage("issues/?jql=");

        tester.assertTextPresent("III-1"); // multi select group field issue
        tester.assertTextPresent("JJJ-1"); // single select group field issue

        //user "dev" with insufficient permissions should not see that issues
        navigation.login("dev");
        navigation.gotoPage("issues/?jql=");

        tester.assertTextNotPresent("III-1");
        tester.assertTextNotPresent("JJJ-1");
    }

}
