package com.atlassian.jira.webtest.webdriver.tests.menu;

import java.util.List;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.components.menu.IssuesMenu;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests dynamic web-item functionality in the top nav. Fairly basic test since most of the functionality is already
 * unit tested in the various web-item providers.
 *
 * @since v6.3
 */
@WebTest ({ Category.WEBDRIVER_TEST })
public class TestIssuesMenu extends BaseJiraWebTest
{
    @Before
    public void onSetUp()
    {
        backdoor.restoreBlankInstance();
    }

    @Test
    public void testRecentIssuesAddedToMenu()
    {
        jira.gotoHomePage();
        JiraHeader header = pageBinder.bind(JiraHeader.class);
        IssuesMenu issuesMenu = header.getIssuesMenu().open();
        List<String> issues = issuesMenu.getRecentIssues();
        //initially there shouldn't be any
        assertEquals(0, issues.size());

        backdoor.issues().createIssue("HSP", "Test Issue 1");
        backdoor.issues().createIssue("HSP", "Test Issue 2");
        backdoor.issues().createIssue("HSP", "Test Issue 3");

        //viewing them adds them to history.
        jira.goToViewIssue("HSP-1");
        jira.goToViewIssue("HSP-2");
        jira.goToViewIssue("HSP-3");

        issuesMenu.close();
        issues = issuesMenu.open().getRecentIssues();
        assertEquals(3, issues.size());
        assertEquals("HSP-3 Test Issue 3", issues.get(0));
        assertEquals("HSP-2 Test Issue 2", issues.get(1));
        assertEquals("HSP-1 Test Issue 1", issues.get(2));

        //deleting an issue removes it from history.
        backdoor.issues().deleteIssue("HSP-2", true);
        issuesMenu.close();
        issues = issuesMenu.open().getRecentIssues();
        assertEquals(2, issues.size());
        assertEquals("HSP-3 Test Issue 3", issues.get(0));
        assertEquals("HSP-1 Test Issue 1", issues.get(1));
    }

    @Test
    public void testRecentSavedSearchesAddedToMenu()
    {
        jira.gotoHomePage();
        JiraHeader header = pageBinder.bind(JiraHeader.class);
        IssuesMenu issuesMenu = header.getIssuesMenu().open();
        List<String> searches = issuesMenu.getRecentSavedSearches();
        //initially there shouldn't be any
        assertEquals(2, searches.size());
        assertEquals("My Open Issues", searches.get(0));
        assertEquals("Reported by Me", searches.get(1));

        backdoor.filters().createFilter("project = HSP", "Test Filter 1", true);
        backdoor.filters().createFilter("project = HSP", "Test Filter 2", true);
        backdoor.filters().createFilter("project = HSP", "Test Filter 3", true);
        backdoor.filters().createFilter("project = HSP", "Test Filter 4", false); //only favourite filter will show. this one wont

        issuesMenu.close();
        searches = issuesMenu.open().getRecentSavedSearches();
        assertEquals(5, searches.size());
        assertEquals("My Open Issues", searches.get(0));
        assertEquals("Reported by Me", searches.get(1));
        assertEquals("Test Filter 1", searches.get(2));
        assertEquals("Test Filter 2", searches.get(3));
        assertEquals("Test Filter 3", searches.get(4));
    }
}
