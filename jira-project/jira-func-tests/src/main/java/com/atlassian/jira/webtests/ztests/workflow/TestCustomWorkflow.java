/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.webtests.ztests.workflow;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.atlassian.jira.functest.framework.admin.ViewWorkflows;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import static com.google.common.collect.Iterables.getOnlyElement;

@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestCustomWorkflow extends JIRAWebTest
{
    public TestCustomWorkflow(String name)
    {
        super(name);
    }

    /**
     * Test that the workflow conditions use the unmodified issue. This is because:
     * To figure out what workflow operations are available we call to OSWorkflow passing it an issue.
     * When the workflow action is executed, we gather the information from the user for issue update and then
     * execute the transition. When the transition is executed OSWorkflow checks teh conditions again. However, the
     * issue has been modified by this stage. If the modified issue causes a workflow condition to fail, the user gets
     * an error.
     * We need to ensure that we do the condition checks against an unmodified issue.
     */
    public void testConditionsUseUnmodifiedIssue()
    {
        administration.restoreData("TestCustomWorkflow.xml");

        // Copy default JIRA workflow
        administration.workflows().goTo().copyWorkflow("jira_old", WORKFLOW_COPIED, "Workflow copied from JIRA default");
        assertTrue(administration.workflows().goTo().inactive().contains(WORKFLOW_COPIED));

        final ViewWorkflows.WorkflowItem copiedWorkflowItem = administration.workflows().goTo().inactive().get(0);
        assertTrue(copiedWorkflowItem.lastModified().contains(new SimpleDateFormat("dd/MMM/yy").format(new Date())));
        assertTrue(copiedWorkflowItem.lastModified().contains(ADMIN_FULLNAME));

        editTransitionScreen(WORKFLOW_COPIED, TRANSIION_NAME_START_PROGRESS, ASSIGN_FIELD_SCREEN_NAME);

        addWorkflowPostfunction(WORKFLOW_COPIED, STATUS_IN_PROGRESS, "Stop Progress", "com.atlassian.jira.plugin.system.workflow:assigntolead-function");

        enableWorkflow();

        // Create an issue for testing
        String key = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "Test Issue");

        try
        {
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
            getNavigation().issue().viewIssue(key);
            clickLinkWithText(TRANSIION_NAME_START_PROGRESS);

            selectOption(FIELD_ASSIGNEE, BOB_FULLNAME);
        }
        finally
        {
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }

        tester.submit();

        // Ensure we are on the View Issue Screen
        assertNotNull(tester.getDialog().getResponse().getURL());
        assertTrue(tester.getDialog().getResponse().getURL().getPath().endsWith("browse/" + key));

        // Ensure the status got set
        text.assertTextPresent(new IdLocator(tester, "status-val"), STATUS_IN_PROGRESS);
        // Ensure the issue was assigned correctly
        text.assertTextPresent(new IdLocator(tester, "assignee-val"), BOB_FULLNAME);

        // Login as bob
        navigation.logout();
        navigation.login(BOB_USERNAME, BOB_PASSWORD);

        // Navigate to the issue
        navigation.issue().gotoIssue(key);

        // Now stop progress on the issue
        tester.clickLinkWithText(TRANSIION_NAME_STOP_PROGRESS);

        // Ensure the status got set
        text.assertTextPresent(new IdLocator(tester, "status-val"), STATUS_OPEN);
        // Ensure the issue was assigned correctly
        text.assertTextPresent(new IdLocator(tester, "assignee-val"), ADMIN_FULLNAME);

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testLastModifiedWithFunnyCharacters()
    {
        administration.restoreData("blankWithOldDefault.xml");

        addUser("\"meta\"user", "meta", "\"meta\" user lastname", "meta@example.com");
        addUserToGroup("\"meta\"user", "jira-administrators");

        navigation.logout();

        navigation.login("\"meta\"user", "meta");

        // Copy default JIRA workflow
        administration.workflows().goTo().copyWorkflow("jira", WORKFLOW_COPIED, "Workflow copied from JIRA default");
        assertTrue(administration.workflows().goTo().inactive().contains(WORKFLOW_COPIED));

        final ViewWorkflows.WorkflowItem copiedWorkflowItem = getOnlyElement(administration.workflows().goTo().inactive());
        assertTrue(copiedWorkflowItem.lastModified().contains(new SimpleDateFormat("dd/MMM/yy").format(new Date())));
        assertTrue(copiedWorkflowItem.lastModified().contains("\"meta\" user lastname"));
    }

    private void enableWorkflow()
    {
        // Associate the project with the new workflow
        addWorkFlowScheme(WORKFLOW_SCHEME, "Test workflow scheme.");
        assignWorkflowScheme(10000L, "Bug", WORKFLOW_COPIED);
        administration.project().associateWorkflowScheme(PROJECT_HOMOSAP, WORKFLOW_SCHEME, null, true);
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_HOMOSAP, WORKFLOW_SCHEME);
    }
}
