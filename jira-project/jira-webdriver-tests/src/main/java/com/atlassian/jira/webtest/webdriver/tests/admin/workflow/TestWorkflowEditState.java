package com.atlassian.jira.webtest.webdriver.tests.admin.workflow;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowSteps;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowHeader;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test to make sure that we remember which mode we are in (text, diagram) when navigating around the UI.
 *
 * @since v5.1
 */
@WebTest ({Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.WORKFLOW })
public class TestWorkflowEditState extends BaseJiraWebTest
{
    private static final String SYSTEM_WORKFLOW = "jira";
    private static final String ACTIVE_WORKFLOW = "One Workflow";

    @Restore("WorkflowHeader.xml")
    @Test
    public void testWorkflowViewState()
    {
        //We have no state. We should be in diagram mode.
        WorkflowHeader header = jira.goTo(ViewWorkflowSteps.class, SYSTEM_WORKFLOW, false);
        assertTrue(header.getCurrentMode() == WorkflowHeader.WorkflowMode.DIAGRAM);

        //Switch into text and make sure it is remebered.
        header.setCurrentViewMode(WorkflowHeader.WorkflowMode.TEXT);
        header = jira.goTo(ViewWorkflowSteps.class, SYSTEM_WORKFLOW, false);
        assertTrue(header.getCurrentMode() == WorkflowHeader.WorkflowMode.TEXT);

        //Switch back into diagram and make sure it is remebered.
        header.setCurrentViewMode(WorkflowHeader.WorkflowMode.DIAGRAM);
        header = jira.goTo(ViewWorkflowSteps.class, ACTIVE_WORKFLOW, false);
        assertTrue(header.getCurrentMode() == WorkflowHeader.WorkflowMode.DIAGRAM);

        //Make sure that moving into edit keeps the state.
        header = header.createDraft(WorkflowHeader.WorkflowMode.DIAGRAM);
        assertTrue(header.getCurrentMode() == WorkflowHeader.WorkflowMode.DIAGRAM);

        //Make sure that discarding the change keeps the state.
        header = header.openDiscardDialog().submitAndGotoViewWorkflow();
        assertTrue(header.getCurrentMode() == WorkflowHeader.WorkflowMode.DIAGRAM);

        //Switch into TEXT mode and make sure it is remebered as we move into edit.
        header.setCurrentViewMode(WorkflowHeader.WorkflowMode.TEXT);
        header = header.createDraft(WorkflowHeader.WorkflowMode.TEXT);
        assertTrue(header.getCurrentMode() == WorkflowHeader.WorkflowMode.TEXT);

        //On the editor switch into DIAGRAM mode.
        header = header.setCurrentEditMode(WorkflowHeader.WorkflowMode.DIAGRAM);
        assertTrue(header.getCurrentMode() == WorkflowHeader.WorkflowMode.DIAGRAM);

        //We should now remeber we are in DIAGRAM mode
        header = header.openPublishDialog().disableBackup().submitAndGotoViewWorkflow();
        assertTrue(header.getCurrentMode() == WorkflowHeader.WorkflowMode.DIAGRAM);
    }
}
