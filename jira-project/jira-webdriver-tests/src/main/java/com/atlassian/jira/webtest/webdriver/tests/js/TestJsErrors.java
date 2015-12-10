package com.atlassian.jira.webtest.webdriver.tests.js;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.ResetDataOnce;
import com.atlassian.jira.pageobjects.framework.util.JiraLocators;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

/**
 * <p/>
 * This test navigates to various pages in JIRA and makes sure there are not JS errors.
 *
 * <p/>
 * It requires the func test plugin to be present on the tested instance,
 *
 * @since 5.2
 */
@WebTest({Category.WEBDRIVER_TEST, Category.DEV_MODE})
@ResetDataOnce
public class TestJsErrors extends BaseJiraWebTest
{
    private static final String TEST = "TEST";

    @Inject PageElementFinder elementFinder;
    @Inject WebDriver webDriver;

    @BeforeClass
    public static void setUp()
    {
        backdoor.project().addProject("Test", TEST, "admin");
    }

    @Test
    public void testNoErrorsOnViewIssue()
    {
        final String key = backdoor.issues().createIssue(TEST, "Some issue").key();
        jira.goToViewIssue(key);
        assertNoErrors();
    }

    @Test
    public void testNoErrorsInTheAdmin()
    {
        jira.goToAdminHomePage();
        assertNoErrors();
    }

    // might add more here


    private void assertNoErrors()
    {
        Poller.waitUntil("There were JS errors on " + webDriver.getCurrentUrl(),
                errorListQuery(), Matchers.<Map<String,String>>emptyIterable());
    }

    @SuppressWarnings ("unchecked")
    private TimedQuery<Iterable<Map<String,String>>> errorListQuery()
    {
        return (TimedQuery) elementFinder.find(JiraLocators.body()).javascript().executeTimed(List.class,
                "return window.JIRA.DevMode.Errors;");
    }

}
