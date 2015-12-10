package com.atlassian.jira.webtest.webdriver.tests.admin.dashboards;

import org.junit.Test;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.SkipInDevMode;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.components.menu.JiraAuiDropdownMenu;
import com.atlassian.jira.pageobjects.pages.admin.SharedDashboardsPage;

import static org.junit.Assert.assertTrue;

/**
 * Tests the behavior of the Share Dashboards page
 *
 * @since v6.0
 */
@RestoreOnce ("xml/TestSharedDashboards.xml")
@WebTest({Category.WEBDRIVER_TEST, Category.ADMINISTRATION})
@org.junit.experimental.categories.Category(SkipInDevMode.class)
public class TestSharedDashboards extends BaseJiraWebTest
{
    @Test
    public void shouldOpenTheConfigurationCog()
    {
        SharedDashboardsPage dashboards = pageBinder.navigateToAndBind(SharedDashboardsPage.class);
        JiraAuiDropdownMenu menu = dashboards.openOperationsDropdownForDashboard("10013");
        assertTrue(menu.isOpen());
    }

    @Test
    public void shouldOpenTheConfigurationCogAfterReordering()
    {
        SharedDashboardsPage dashboards = pageBinder.navigateToAndBind(SharedDashboardsPage.class);
        dashboards.sortBy("owner");
        JiraAuiDropdownMenu menu = dashboards.openOperationsDropdownForDashboard("10013");
        assertTrue(menu.isOpen());
    }
}
