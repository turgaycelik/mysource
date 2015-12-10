package com.atlassian.jira.webtests.ztests.plugin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Verifying that issues can be created during a transition
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.PLUGINS, Category.REFERENCE_PLUGIN })
public class TestCreateIsueinPostFunction extends FuncTestCase
{
    public void testCreateIssues() throws Exception
    {
        administration.restoreData("TestCreateIssueDuringTransition.xml");
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLink("action_id_711");
        navigation.issueNavigator().displayAllIssues();
        tester.assertLinkPresentWithText("HSP-1");
        tester.assertLinkPresentWithText("HSP-2");
        tester.assertLinkPresentWithText("HSP-3");

        tester.clickLinkWithText("HSP-2");
        tester.assertLinkPresentWithText("HSP-2");
        navigation.issueNavigator().displayAllIssues();
        tester.clickLinkWithText("HSP-3");
        tester.assertLinkPresentWithText("HSP-3");
        navigation.issueNavigator().displayAllIssues();
    }


}
