package com.atlassian.jira.webtest.webdriver.tests.admin.workflow;

import java.util.Collections;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.pages.admin.workflow.AddWorkflowDialog;
import com.atlassian.jira.pageobjects.pages.admin.workflow.CopyWorkflowDialog;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowSteps;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowHeader;
import com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowsPage;

import org.junit.Test;

import static com.atlassian.jira.pageobjects.pages.admin.workflow.WorkflowsPage.names;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PROJECTS, Category.WORKFLOW })
public class TestListWorkflows extends BaseJiraWebTest
{
    private static final String SYSTEM_WORKFLOW = "jira";

    //JRADEV-12813: Active is always open when page load. Inactive is always closed when the page loads.
    @Test
    public void testModuleState()
    {
        backdoor.restoreBlankInstance();
        WorkflowsPage workflowsPage = jira.gotoLoginPage()
                .loginAsSysAdmin(WorkflowsPage.class);

        assertTrue(workflowsPage.isActiveOpen());
        assertFalse(workflowsPage.isInactiveOpen());

        workflowsPage.closeActive();
        workflowsPage.openInactive();

        workflowsPage = pageBinder.navigateToAndBind(WorkflowsPage.class);
        assertTrue(workflowsPage.isActiveOpen());
        assertFalse(workflowsPage.isInactiveOpen());
    }

    @Test
    public void testNoWorkflowsToShow()
    {
        backdoor.restoreData("noprojects.xml");
        WorkflowsPage workflowsPage = jira.gotoLoginPage()
                .loginAsSysAdmin(WorkflowsPage.class);

        assertTrue(workflowsPage.active().isEmpty());
        assertEquals(Collections.<String>singletonList(SYSTEM_WORKFLOW), names(workflowsPage.inactive()));
        assertEquals("There are no active workflows.", workflowsPage.getActiveText());

        backdoor.project().addProject("One", "ONE", "admin");

        workflowsPage = jira.goTo(WorkflowsPage.class);
        assertTrue(workflowsPage.inactive().isEmpty());
        assertEquals(Collections.<String>singletonList(SYSTEM_WORKFLOW), names(workflowsPage.active()));
        assertEquals("There are no inactive workflows.", workflowsPage.getInactiveText());
    }

    @Test
    @Restore ("blankWithOldDefault.xml")
    public void testCopyWorkflow()
    {
        //Need to make sure we set the view mode.
        jira.gotoLoginPage()
                .loginAsSysAdmin(ViewWorkflowSteps.class, SYSTEM_WORKFLOW, false)
                .setCurrentViewMode(WorkflowHeader.WorkflowMode.TEXT);

        //Open the copy dialog.
        final WorkflowsPage workflowsPage = jira.goTo(WorkflowsPage.class);
        final WorkflowsPage.Workflow workflow = workflowsPage.findWorkflow(SYSTEM_WORKFLOW);
        CopyWorkflowDialog copyWorkflowDialog = workflow.openCopyDialog();

        //Make sure the initial state of the dialog is correct.
        assertEquals(String.format("Copy of %s", workflow.getName()), copyWorkflowDialog.getName());
        assertEquals(workflow.getDescription(), copyWorkflowDialog.getDescription());

        //Check for errors when we type the same name twice.
        copyWorkflowDialog.setName(workflow.getName());
        copyWorkflowDialog = copyWorkflowDialog.submitFail();

        assertThat(copyWorkflowDialog.getFormErrors(),
                hasEntry(CopyWorkflowDialog.FIELD_NAME, "A workflow with this name already exists."));

        //Check for error that occurs when you don't enter a name.
        copyWorkflowDialog.setName("");
        copyWorkflowDialog = copyWorkflowDialog.submitFail();
        assertThat(copyWorkflowDialog.getFormErrors(),
                hasEntry(CopyWorkflowDialog.FIELD_NAME, "You must specify a workflow name."));

        //Check the successful case
        final String newDescription = "New Description";
        final String newName = "<b>New Name</b>";
        final WorkflowHeader header = copyWorkflowDialog
                .setDescription(newDescription)
                .setName(newName)
                .submit(WorkflowHeader.WorkflowMode.TEXT);

        assertEquals(newName, header.getWorkflowName());
        assertEquals(newDescription, header.getWorkflowDescription());
    }

    @Test
    public void testAddWorkflow()
    {
        backdoor.restoreBlankInstance();

        //Need to make sure we set the view mdoe.
        jira.gotoLoginPage()
                .loginAsSysAdmin(ViewWorkflowSteps.class, SYSTEM_WORKFLOW, false)
                .setCurrentViewMode(WorkflowHeader.WorkflowMode.TEXT);

        final WorkflowsPage workflowsPage = jira.goTo(WorkflowsPage.class);
        AddWorkflowDialog workflowDialog = workflowsPage.openAddWorkflowDialog();

        //Make sure the initial state of the dialog is correct.
        assertNull(workflowDialog.getName());
        assertNull(workflowDialog.getDescription());

        //Check for errors when we type the same name twice.
        workflowDialog.setName(SYSTEM_WORKFLOW);
        workflowDialog = workflowDialog.submitFail();

        assertThat(workflowDialog.getFormErrors(),
                hasEntry(CopyWorkflowDialog.FIELD_NAME, "A workflow with this name already exists."));

        //Check for error that occurs when you don't enter a name.
        workflowDialog.setName("");
        workflowDialog = workflowDialog.submitFail();
        assertThat(workflowDialog.getFormErrors(),
                hasEntry(CopyWorkflowDialog.FIELD_NAME, "You must specify a workflow name."));

        //Check the successful case
        final String newDescription = "New Description";
        final String newName = "<b>New Name</b>";
        WorkflowHeader header = workflowDialog
                .setDescription(newDescription)
                .setName(newName)
                .submit(WorkflowHeader.WorkflowMode.TEXT);

        assertEquals(newName, header.getWorkflowName());
        assertEquals(newDescription, header.getWorkflowDescription());

        //Switch to diagram mode.
        header.setCurrentEditMode(WorkflowHeader.WorkflowMode.DIAGRAM);

        //Create a new workflow and check that we are redirected to diagram mode.
        workflowDialog = jira.goTo(WorkflowsPage.class).openAddWorkflowDialog();
        final String reallyReallyNew = "ReallyReallyNew";
        header = workflowDialog.setName(reallyReallyNew)
                .submit(WorkflowHeader.WorkflowMode.DIAGRAM);

        assertEquals(reallyReallyNew, header.getWorkflowName());
        assertNull(header.getWorkflowDescription());
    }

    @Restore("SysAdminJiraAdmin.xml")
    @Test
    public void testImportLinks()
    {
        WorkflowsPage workflowsPage = jira.goTo(WorkflowsPage.class);

        assertTrue(workflowsPage.isImportFromXmlLinkPresent());
        assertTrue(workflowsPage.isImportFromBundleLinkPresent());
        assertFalse(workflowsPage.isImportFromMarketplaceLinkPresent());

        workflowsPage = jira.quickLogin("jiraadmin", "jiraadmin", WorkflowsPage.class);

        assertFalse(workflowsPage.isImportFromXmlLinkPresent());
        assertFalse(workflowsPage.isImportFromBundleLinkPresent());
        assertTrue(workflowsPage.isImportFromMarketplaceLinkPresent());
    }
}
