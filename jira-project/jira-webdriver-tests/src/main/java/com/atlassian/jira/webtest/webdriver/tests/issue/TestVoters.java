package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.VotersComponent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore ("xml/TestVoters.xml")
public class TestVoters extends BaseJiraWebTest
{
    @Test
    public void testVotersShow() {
        final ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, "HSP-1");
        final VotersComponent votersComponent = issuePage.openVotersDialog();
        assertTrue(votersComponent.getVoters().contains("jiradev"));
    }

    @Test
    public void testVote() {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.quickLogin("jiradev", "jiradev", DashboardPage.class);
        final ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        issuePage.voteForIssue();
        final VotersComponent votersComponent = issuePage.openVotersDialog();
        assertTrue(votersComponent.getVoters().contains("jiradev"));
    }

    @Test
    public void testUnvote() {
        jira.quickLogin("jiradev", "jiradev", DashboardPage.class);
        final ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, "HSP-1");
        issuePage.unvoteForIssue();
        final VotersComponent votersComponent = issuePage.openVotersDialog();
        assertTrue(votersComponent.isShowingEmptyMessage());
    }

    @Test
    public void testShowMultipleVotes() {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        jira.quickLogin("jiradev", "jiradev", DashboardPage.class);
        ViewIssuePage issuePage = jira.goTo(ViewIssuePage.class, key);
        issuePage.voteForIssue();
        jira.quickLogin("nomanage", "nomanage", DashboardPage.class);
        issuePage = jira.goTo(ViewIssuePage.class, key);
        issuePage.voteForIssue();
        final VotersComponent votersComponent = issuePage.openVotersDialog();
        assertTrue(votersComponent.getVoters().contains("jiradev"));
        assertTrue(votersComponent.getVoters().contains("nomanage"));
    }


}
