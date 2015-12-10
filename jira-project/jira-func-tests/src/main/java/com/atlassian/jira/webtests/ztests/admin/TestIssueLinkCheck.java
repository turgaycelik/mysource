package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.ISSUES })
public class TestIssueLinkCheck extends FuncTestCase
{
    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueLinkCheck.xml");
    }

    public void testIssueLinkCheck()
    {
        navigation.gotoAdminSection("integrity_checker");

        tester.checkCheckbox("integrity_check_1_3", "3");
        tester.submit("check");
        text.assertTextPresent(locator.page(), "Choose the errors you would like to fix, or return to the previous screen");
        text.assertTextPresent(locator.page(), "The following Issue Link will be removed due to a related invalid issue: IssueLink (ID:10002)");
        text.assertTextPresent(locator.page(), "The following Issue Link will be removed due to a related invalid issue: IssueLink (ID:10003)");
        text.assertTextPresent(locator.page(), "The following Issue Link will be removed due to a related invalid issue: IssueLink (ID:10004)");

        tester.checkCheckbox("integrity_check_1_3", "3");
        tester.submit("fix");
        text.assertTextPresent(locator.page(), "3 error(s) were corrected");
        text.assertTextPresent(locator.page(), "The following Issue Link has been removed due to a related invalid issue: IssueLink (ID:10002)");
        text.assertTextPresent(locator.page(), "The following Issue Link has been removed due to a related invalid issue: IssueLink (ID:10003)");
        text.assertTextPresent(locator.page(), "The following Issue Link has been removed due to a related invalid issue: IssueLink (ID:10004)");

        navigation.gotoAdminSection("integrity_checker");

        tester.checkCheckbox("integrity_check_1_3", "3");
        tester.submit("check");
        text.assertTextPresent(locator.page(), "No errors were found");
        text.assertTextSequence(locator.page(), "PASSED", "Check that all Issue Links are associated with valid issues");
    }
}
