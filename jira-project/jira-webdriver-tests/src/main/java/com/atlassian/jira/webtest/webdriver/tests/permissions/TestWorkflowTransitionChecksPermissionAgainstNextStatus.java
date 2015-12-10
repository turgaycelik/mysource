package com.atlassian.jira.webtest.webdriver.tests.permissions;

import javax.inject.Inject;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.components.fields.AssigneeField;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Checks that the permission checks made when making a workflow transition are done taking into account the next status
 * of the issue after the transition, and not the current status.
 */
@WebTest (Category.WEBDRIVER_TEST)
public class TestWorkflowTransitionChecksPermissionAgainstNextStatus extends BaseJiraWebTest
{
    public static final String ISSUE_KEY = "TEST-3";

    // Scenario of the xml being restored:
    //
    // A project test with a very simple workflow where issues go from "Open" to "In Progress".
    // The workflow is set up so only users with role "a" can be assignees on "Open" issues
    // The workflow is set up so only users with role "b" can be assignees on "In Progress" issues
    //
    // We have an user "userb" with role "b"
    //
    // The issue being transitioned will go from "Open" to "In Progress", and we'll be changing the assignee on the transition dialog.

    @Test
    @Restore ("xml/TestWorkflowTransitionChecksPermissionAgainstNextStatus.xml")
    public void userbCanBeAssignedOnTransitionSinceItHasTheRequiredRoleOnFinalIssueStatus()
    {
        jira.goToViewIssue(ISSUE_KEY);
        IssueTransitionDialog dialog = pageBinder.bind(IssueTransitionDialog.class);

        dialog.open().selectAssignee("userb").finishExpectingViewIssue(ISSUE_KEY);
    }

    public static class IssueTransitionDialog
    {
        @ElementBy (id = "action_id_11")
        protected PageElement openDialogButton;
        @ElementBy (id = "issue-workflow-transition-submit")
        protected PageElement finishTransitionButton;
        @ElementBy (id = "issue-workflow-transition")
        protected PageElement form;

        @Inject
        private TraceContext traceContext;
        @Inject
        protected PageBinder pageBinder;

        private IssueTransitionDialog open()
        {
            openDialogButton.click();
            waitUntilTrue(form.timed().isPresent());
            waitUntilTrue(form.timed().isVisible());
            return this;
        }

        public IssueTransitionDialog selectAssignee(String user)
        {
            AssigneeField assigneeField = pageBinder.bind(AssigneeField.class);
            assigneeField.typeAssignee(user);
            return this;
        }

        public void finishExpectingViewIssue(String issueKey)
        {
            Tracer tracer = traceContext.checkpoint();
            finishTransitionButton.click();
            ViewIssuePage viewIssuePage = pageBinder.bind(ViewIssuePage.class, issueKey);
            viewIssuePage.waitForAjaxRefresh(tracer);
        }
    }
}
