package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.watchers.WatchersComponent;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;

import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore ("xml/TestWatchers.xml")
public class TestWatchers extends BaseJiraWebTest
{

    @Test
    public void testCurrentWatchersShow() {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        final ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        final WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        assertEquals(watchersComponent.getWatchers(), asList("admin"));
    }

    @Test
    public void testAddingWatchers() {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        watchersComponent.addWatcher("fred");
        assertTrue(watchersComponent.getWatchers().contains("fred"));
        assertTrue(watchersComponent.getWatchers().contains("admin"));
        issuePage = jira.goTo(ViewIssuePage.class, key);
        watchersComponent = issuePage.openWatchersDialog();
        assertTrue(watchersComponent.getWatchers().contains("fred"));
        assertTrue(watchersComponent.getWatchers().contains("admin"));
    }

    @Test
    public void testDeleteWatchers() {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        watchersComponent.removeWatcher("admin");
        assertEquals(watchersComponent.getWatchers(), new ArrayList<String>());
        issuePage = jira.goTo(ViewIssuePage.class, key);
        watchersComponent = issuePage.openWatchersDialog();
        assertEquals(watchersComponent.getWatchers(), new ArrayList<String>());
    }


    @Test
    public void testCurrentWatchersShowWithNoBrowsePermission() {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.quickLogin("jiradev", "jiradev", DashboardPage.class);
        final ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        final WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        assertEquals(watchersComponent.getWatchers(), asList("admin"));
    }

    @Test
    public void testAddingWatchersWithNoBrowsePermission() {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.quickLogin("jiradev", "jiradev", DashboardPage.class);
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        watchersComponent.addWatcher("fred");
        assertTrue(watchersComponent.getWatchers().contains("fred"));
        assertTrue(watchersComponent.getWatchers().contains("admin"));
        issuePage = jira.goTo(ViewIssuePage.class, key);
        watchersComponent = issuePage.openWatchersDialog();
        assertTrue(watchersComponent.getWatchers().contains("fred"));
        assertTrue(watchersComponent.getWatchers().contains("admin"));

    }

    @Test
    public void testDeleteWatchersWithNoBrowsePermission() {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.quickLogin("jiradev", "jiradev", DashboardPage.class);
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        watchersComponent.removeWatcher("admin");
        assertEquals(watchersComponent.getWatchers(), new ArrayList<String>());
        issuePage = jira.goTo(ViewIssuePage.class, key);
        watchersComponent = issuePage.openWatchersDialog();
        assertEquals(watchersComponent.getWatchers(), new ArrayList<String>());
    }

    @Test
    public void testNoManagePermissionShowsReadOnly() {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.quickLogin("nomanage", "nomanage", DashboardPage.class);
        final ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        final WatchersComponent watchersComponent = issuePage.openWatchersDialog();
        assertTrue(watchersComponent.isReadOnly());
        assertEquals(watchersComponent.getWatchers(), asList("admin"));
    }

}
