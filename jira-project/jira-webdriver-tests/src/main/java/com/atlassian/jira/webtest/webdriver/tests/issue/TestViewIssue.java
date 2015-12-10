package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.elements.query.Poller;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Tests for creating an issue using quick create
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@ResetData
public class TestViewIssue extends BaseJiraWebTest
{
    @Test
    public void testEnableDisableInlineEdit()
    {
        String key1 = backdoor.issues().createIssue("HSP", "xxx").key;
        String key2 = backdoor.issues().createIssue("HSP", "yyy").key;

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, key1);

        Poller.waitUntilTrue("Dude, where's my kickass?", viewIssuePage.isIssueTypeEditable());

        backdoor.applicationProperties().disableInlineEdit();

        viewIssuePage = jira.goTo(ViewIssuePage.class, key2);
        Poller.waitUntilFalse("Kickass was disabled but the issue page still is editable!?", viewIssuePage.isIssueTypeEditable());
    }

    @Test
    public void testEmptyDescription()
    {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, key);
        assertTrue("Description should show when empty", viewIssuePage.hasDescription());
    }

    @Test
    public void testDescriptionWithValueAlwaysShows()
    {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        backdoor.issues().setDescription(key, "I am not empty");
        ViewIssuePage viewIssuePage = jira.quickLogin("fred", "fred", ViewIssuePage.class, key);
        assertTrue("Description should show with value", viewIssuePage.hasDescription());
    }

    @Test
    public void testHiddenDescription()
    {
        final String key = backdoor.issues().createIssue("HSP", "xxx").key;
        backdoor.issues().setDescription(key, "I am not empty");
        backdoor.fieldConfiguration().hideField("Default Field Configuration", "description");
        ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, key);
        assertFalse("Description should NOT show when hidden", viewIssuePage.hasDescription());
        backdoor.fieldConfiguration().hideField("Default Field Configuration", "description");
        viewIssuePage = jira.goTo(ViewIssuePage.class, key);
        assertFalse("Description should NOT show when hidden", viewIssuePage.hasDescription());
    }

    @Test
    public void testAddCommentFromAnchor()
    {
        final String key1 = backdoor.issues().createIssue("HSP", "xxx").key;
        final String key2 = backdoor.issues().createIssue("HSP", "yyy").key;

        //comment form isnt visible normally
        final ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, key1);
        assertFalse("add comment dialog is active", viewIssuePage.isAddCommentModuleActive());

        //check that add comment is only visible when anchor is there
        final ViewIssuePage viewIssuePage2 = jira.goTo(ViewIssuePage.class, key2, "add-comment");
        assertTrue("add comment dialog is not active", viewIssuePage2.isAddCommentModuleActive());

    }

    @Test
    public void testViewIssueRedirectsIfInLowerCase()
    {
        final String key1 = backdoor.issues().createIssue("HSP", "xxx").key;

        // go to old URL
        jira.getTester().getDriver().navigate().to(jira.getProductInstance().getBaseUrl() + new ViewIssuePage(key1.toLowerCase()).getUrl());
        final ViewIssuePage newIssuePage = jira.getPageBinder().bind(ViewIssuePage.class, key1);
        assertEquals(key1, newIssuePage.getIssueKey());
        assertThat(jira.getTester().getDriver().getCurrentUrl(), endsWith("/" + key1));
    }
}

