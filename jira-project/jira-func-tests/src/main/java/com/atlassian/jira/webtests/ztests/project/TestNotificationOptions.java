package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestNotificationOptions extends FuncTestCase
{
    public void testComponentLeadIsAnOptionForIssueCreatedNotifications() throws Exception
    {
        goToDefaultNotificationScheme();
        goToAddIssueCreatedNotificationPage();

        text.assertTextPresent(locator.page(), "Component Lead");
    }

    public void testProjectRoleIsAnOptionForIssueCreatedNotifications()
    {
        administration.restoreData("TestSchemesProjectRoles.xml");
        goToDefaultNotificationScheme();
        goToAddIssueCreatedNotificationPage();

        text.assertTextPresent(locator.page(), "Choose a project role");

        tester.checkCheckbox("type", "Project_Role");
        tester.selectOption("Project_Role", "test role");
        tester.submit();

        text.assertTextPresent(locator.page(), "(test role)");
    }

    private void goToDefaultNotificationScheme()
    {
        navigation.gotoAdminSection("notification_schemes");
        tester.clickLinkWithText("Notifications");
    }

    private void goToAddIssueCreatedNotificationPage()
    {
        tester.clickLink("add_1");
    }
}