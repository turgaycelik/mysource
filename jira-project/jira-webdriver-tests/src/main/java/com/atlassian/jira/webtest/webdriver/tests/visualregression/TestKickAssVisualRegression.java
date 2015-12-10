package com.atlassian.jira.webtest.webdriver.tests.visualregression;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.thoughtworks.selenium.SeleniumException;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Webdriver test for visual regression on the screens from the kick-ass plugin.
 *
 * Deals with issue nav and view issue.
 *
 * @since v5.0
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.VISUAL_REGRESSION })
@Restore ("xml/TestVisualRegressionSmoke.zip")
public class TestKickAssVisualRegression extends JiraVisualRegressionTest
{
    private static final String KICKASS_SWITCH_TO_BASIC_SEARCH = ".switcher-item[data-id='advanced']";
    private static final String KICKASS_SWITCH_TO_ADVANCED_SEARCH = ".switcher-item[data-id='basic']";
    private static final String KICKASS_ADVANCED_SEARCH_FIELD = "#advanced-search";
    private static final String KICKASS_SEARCH_BUTTON = ".search-container .search-button";

    @Test
    public void testIssueNavAdvanced() throws InterruptedException
    {
        doAnIssueSearchFor("cf[10010] = aaa ORDER BY issuekey DESC");
        visualComparer.setWaitforJQueryTimeout(5000);
        assertUIMatches("issue-nav-advanced");
    }

    @Test
    public void testIssueNavSimple() throws InterruptedException
    {
        doAnIssueSearchFor("cf[10010] = aaa ORDER BY issuekey DESC");
        clickOnElement(KICKASS_SWITCH_TO_BASIC_SEARCH);
        visualComparer.setWaitforJQueryTimeout(5000);
        assertUIMatches("issue-nav-simple");
    }

    private void doAnIssueSearchFor(String something)
    {
        AtlassianWebDriver driver = jira.getTester().getDriver();
        goTo("/issues/?jql="); // so that there's no "last search"
        boolean inAdvancedSearch = false;
        try
        {
            inAdvancedSearch = driver.elementIsVisible(By.cssSelector(KICKASS_ADVANCED_SEARCH_FIELD));
        }
        catch (SeleniumException e)
        {
            // isVisible likes to throw exceptions if it can't find elements. Handy!
        }
        if (!inAdvancedSearch)
        {
            clickOnElement(KICKASS_SWITCH_TO_ADVANCED_SEARCH);
        }
        driver.findElement(By.cssSelector(KICKASS_ADVANCED_SEARCH_FIELD)).sendKeys(something);
        new Actions(driver).moveToElement(driver.findElement(By.id("jira"))).perform(); // de-focus the search field... and hope that firefox will do its spell-checking on blur.
        clickOnElement(KICKASS_SEARCH_BUTTON);
    }

}
