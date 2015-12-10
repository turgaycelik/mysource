package com.atlassian.jira.webtest.webdriver.tests.visualregression;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * WebDriver tests for visual regressions of key integrated plugins.
 *
 * @since v6.0
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.VISUAL_REGRESSION })
public class TestIntegratedPluginsVisualRegression extends JiraVisualRegressionTest
{
    @Inject
    private PageElementFinder finder;

    @Override
    protected void removeUPMCount()
    {
        // Do nothing, as we want to check this icon in these tests
    }

    @BeforeClass
    public static void restoreInstance()
    {
        //we cannot restore instance via @Restore annotation as it doesn't support restoring license from backup file
        backdoor.dataImport().restoreDataFromResource("xml/TestVisualRegressionSmoke.zip", "");
    }

    @Test
    public void testManagePlugins()
    {
        visualComparer.setWaitforJQueryTimeout(1000);
        visualComparer.setRefreshAfterResize(false);
        goTo("/plugins/servlet/upm/manage/action-required");
        Poller.waitUntilFalse(finder.find(By.className(".loading.filter-box")).withTimeout(TimeoutType.SLOW_PAGE_LOAD).timed().isPresent());
        assertUIMatches("upm-manage");
    }

}
