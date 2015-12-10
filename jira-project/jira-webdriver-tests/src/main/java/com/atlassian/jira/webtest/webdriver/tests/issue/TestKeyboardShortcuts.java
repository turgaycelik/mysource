package com.atlassian.jira.webtest.webdriver.tests.issue;

import javax.inject.Inject;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.pages.viewissue.AssignIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.people.PeopleSection;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for keyboard shortcuts for the issueaction context
 *
 * @since v5.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore ("xml/TestActionsAndOperations.xml")
public class TestKeyboardShortcuts extends BaseJiraWebTest
{
    @Inject
    private TraceContext traceContext;

    @Test
    public void testAssignToMeShortcut() throws Exception
    {
        final ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, "HSP-1");

        final AssignIssueDialog assignIssueDialog = viewIssuePage.assignIssueViaKeyboardShortcut();
        assignIssueDialog.setAssignee("Unassigned");

        Tracer tracer = traceContext.checkpoint();
        assignIssueDialog.submit();
        viewIssuePage.waitForAjaxRefresh(tracer);

        final PeopleSection peopleSection = viewIssuePage.getPeopleSection();

        assertEquals("Unassigned", peopleSection.getAssignee());
        tracer = traceContext.checkpoint();
        viewIssuePage.execKeyboardShortcut(DefaultIssueActions.ASSIGN_TO_ME.shortcut());
        viewIssuePage.waitForAjaxRefresh(tracer);

        assertEquals("Administrator", peopleSection.getAssignee());
    }
}
