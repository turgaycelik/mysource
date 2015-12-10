package com.atlassian.jira.webtest.webdriver.tests.visualregression;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Webdriver test for visual regression on the screens from the Auditing plugin.
 *
 * @since v6.2
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.VISUAL_REGRESSION })
public class TestAuditLogVisualRegression extends JiraVisualRegressionTest
{
    final Backdoor testkit = jira.backdoor().getTestkit();

    @Inject
    private PageElementFinder elementFinder;

    @Before
    public void restoreInstance(){
        //we cannot restore instance via @Restore annotation as it doesn't support restoring license from backup file
        backdoor.dataImport().restoreDataFromResource("xml/TestVisualRegressionSmoke.zip", "");
        visualComparer.setWaitforJQueryTimeout(1000);
        visualComparer.setRefreshAfterResize(false);
    }

    @Test
    public void testAuditingEmpty() throws InterruptedException
    {
        goTo("/auditing/view");
        assertUIMatches("auditing-empty");
    }

    @Test
    public void testAuditingView() throws InterruptedException
    {
        testkit.auditing().clearAllRecords();
        generateRecords(300);

        goTo("/auditing/view");
        clickOnElement(".show-details");

        sleep(1000);
        addElementsToIgnore(By.className("auditing-date"));
        assertUIMatches("auditing-view");
    }

    @Test
    public void testAuditingViewSearchPending() throws InterruptedException
    {
        visualComparer.setWaitforJQueryTimeout(0);
        testkit.auditing().clearAllRecords();
        generateRecords(3);

        goTo("/auditing/view");
        jira.backdoor().barrier().raiseBarrierAndRun("auditingGetRecords", new Runnable()
        {
            @Override
            public void run()
            {
                elementFinder.find(By.id("searcher-query")).type("test");
                clickOnElement(".search-button");
                //we do additional click to ensure focus stays out of search input
                clickOnElement(".show-details");
                sleep(1000);
                addElementsToIgnore(By.className("auditing-date"));
                assertUIMatches("auditing-search-pending");
            }
        });

    }

    private void generateRecords(final int numberOfRecordsToGenerate)
    {
        for (int i = 0; i < numberOfRecordsToGenerate / 3; i++)
        {
            final String username = "test" + i;
            testkit.usersAndGroups().addUser(username);
            testkit.usersAndGroups().deleteUser(username);
        }
    }

    private void sleep(final int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
