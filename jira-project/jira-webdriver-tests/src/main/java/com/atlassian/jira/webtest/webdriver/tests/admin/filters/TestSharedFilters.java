package com.atlassian.jira.webtest.webdriver.tests.admin.filters;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.SkipInDevMode;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.menu.JiraAuiDropdownMenu;
import com.atlassian.jira.pageobjects.pages.admin.SharedFiltersPage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests the behavior of the Share Filters page
 *
 * @since v6.0
 */
@RestoreOnce ("xml/TestSharedFilterSearchingByAdmins.xml")
@WebTest({Category.WEBDRIVER_TEST, Category.ADMINISTRATION})
@org.junit.experimental.categories.Category(SkipInDevMode.class)
public class TestSharedFilters extends BaseJiraWebTest
{
    @Test
    public void shouldOpenTheConfigurationCog()
    {
        SharedFiltersPage filters = pageBinder.navigateToAndBind(SharedFiltersPage.class);
        JiraAuiDropdownMenu menu = filters.openOperationsDropdownForFilter("10007");
        assertTrue(menu.isOpen());
    }

    @Test
    public void shouldOpenTheConfigurationCogAfterReordering()
    {
        SharedFiltersPage filters = pageBinder.navigateToAndBind(SharedFiltersPage.class);
        filters.sortBy("owner");
        JiraAuiDropdownMenu menu = filters.openOperationsDropdownForFilter("10007");
        assertTrue(menu.isOpen());
    }
}
