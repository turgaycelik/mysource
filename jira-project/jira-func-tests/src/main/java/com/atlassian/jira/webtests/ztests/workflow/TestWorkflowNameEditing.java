package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.xml.sax.SAXException;

import static com.atlassian.jira.functest.framework.admin.ViewWorkflows.WorkflowItemsList.Predicates.byDescription;
import static com.atlassian.jira.functest.framework.admin.ViewWorkflows.WorkflowItemsList.Predicates.byName;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;

/**
 * Test that editing workflow name and description works in JIRA.
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowNameEditing extends JIRAWebTest
{
    public TestWorkflowNameEditing(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("TestWorkflowNameEditing.xml");
    }

    public void tearDown()
    {
        administration.restoreBlankInstance();
        super.tearDown();
    }

    public void testSystemWorkflowIsNotEditable()
    {
        assertFalse(administration.workflows().goTo().isEditable("jira"));
    }

    public void testAnActiveWorkflowIsEditableGivenADraftHasNotBeenCreatedForIt()
    {
        assertTrue(administration.workflows().goTo().isEditable("Active workflow"));
    }

    public void testAnActiveWorkflowIsEditableGivenADraftHasBeenCreatedForIt()
    {
        administration.workflows().goTo().createDraft("Active workflow");
        assertTrue(administration.workflows().goTo().isEditable("Active workflow"));
    }

    public void testAnInactiveWorkflowIsEditableGivenItHasNoAssignedSchemes()
    {
        assertTrue(administration.workflows().goTo().isEditable("editable with no scheme"));
    }

    public void testAnInactiveWorkflowIsEditableGivenItHasAssignedSchemes()
    {
        assertTrue(administration.workflows().goTo().isEditable("editable workflow"));
    }

    public void testEditInactiveWorkflowWithScheme() throws SAXException
    {
        administration.workflows().goTo().
                edit("editable workflow").
                rename().
                setNameTo("edited with a scheme").setDescriptionTo("edited with a scheme").
                submit();

        //goto the associated schemes and verify that the workflow name has changed
        administration.workflows().goTo();
        tester.assertTextPresent("edited with a scheme");
        tester.assertTextNotPresent("editable workflow");

        //verify that the workflow name edit is still valid by associating the schemes with projects and verify a single transition works.
        associateWorkFlowSchemeToProject(PROJECT_MONKEY, "scheme with editable flow");
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_MONKEY, "scheme with editable flow");
        associateWorkFlowSchemeToProject(PROJECT_NEO, "scheme with renamed workflow");
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_NEO, "scheme with renamed workflow");

        //for project monkey, check that the renamed workflow only effects unassigned issue types (ie. task and new feature)
        assertAddingIssueForModifiedWorkflow(PROJECT_MONKEY, ISSUE_TYPE_NEWFEATURE, "MONKEY new feature issue of the workflow that was renamed");
        assertAddingIssueForModifiedWorkflow(PROJECT_MONKEY, ISSUE_TYPE_TASK, "MONKEY task issue of the workflow that was renamed");
        //also check that the other assigned issue types are not affected (ie. improvement and bug)
        assertAddingIssueForUnModifiedWorkflow(PROJECT_MONKEY, ISSUE_TYPE_BUG, "MONKEY bug issue of the workflow that was NOT renamed");
        assertAddingIssueForUnModifiedWorkflow(PROJECT_MONKEY, ISSUE_TYPE_IMPROVEMENT, "MONKEY improvement issue of the workflow that was NOT renamed");

        //for project neanderthal, check that the renamed workflow affects all unassigned issue types (ie. all the issues types)
        assertAddingIssueForModifiedWorkflow(PROJECT_NEO, ISSUE_TYPE_BUG, "NEO bug issue of the workflow that was renamed");
        assertAddingIssueForModifiedWorkflow(PROJECT_NEO, ISSUE_TYPE_IMPROVEMENT, "NEO improvement issue of the workflow that was renamed");
        assertAddingIssueForModifiedWorkflow(PROJECT_NEO, ISSUE_TYPE_NEWFEATURE, "NEO new feature issue of the workflow that was renamed");
        assertAddingIssueForModifiedWorkflow(PROJECT_NEO, ISSUE_TYPE_TASK, "NEO task issue of the workflow that was renamed");
    }

    private void assertAddingIssueForUnModifiedWorkflow(String project, String issueType, String summary)
    {
        navigation.issue().createIssue(project, issueType, summary);
        tester.assertTextNotPresent("RESOLVE WITH EDITED WO...");
        tester.assertLinkPresentWithText("Start Progress");
        tester.assertLinkPresentWithText("Resolve Issue");
        tester.assertLinkPresentWithText("Close Issue");
        tester.assertTextPresent("Open");
        tester.assertTextNotPresent("In Progress");
        tester.clickLink("action_id_4");//start progress
        tester.assertTextPresent("In Progress");
        tester.assertTextNotPresent("Open");
    }

    private void assertAddingIssueForModifiedWorkflow(String project, String issueType, String summary)
    {
        navigation.issue().createIssue(project, issueType, summary);
        tester.assertLinkPresentWithText("RESOLVE WITH EDITED WO...");
        tester.assertTextPresent("Open");
        tester.assertTextNotPresent("Resolved");
        tester.clickLink("action_id_11");//resolve the issue by clicking on the workflow transition
        tester.assertTextPresent("Resolved");
        tester.assertTextNotPresent("Open");
    }

    public void testDefaultSystemWorkflowIsNotEditableViaUrlAccess()
    {
        //goto the edit page for jira workflow directly
        tester.gotoPage(page.addXsrfToken("/secure/admin/workflows/EditWorkflow.jspa?workflowMode=live&workflowName=jira"));
        //assert that the name or description cannot be edited
        assertWorkflowIsNotEditable();
    }

    public void testActiveWorkflowsAreNotEditable()
    {
        //goto the edit page for the active workflow
        tester.gotoPage(page.addXsrfToken("/secure/admin/workflows/EditWorkflow.jspa?workflowMode=live&workflowName=Active+workflow"));
        //assert that the name or description cannot be edited
        assertWorkflowIsNotEditable();
    }

    public void testEditWorkflowNameValidation()
    {
        //check name is not null/empty
        administration.workflows().goTo().
                edit("editable workflow").rename().setNameTo("").submit();
        tester.assertTextPresent("You must specify a workflow name.");

        //check for duplicate name
        administration.workflows().goTo().
                edit("editable workflow").rename().setNameTo("Active workflow").submit();
        tester.assertTextPresent("A workflow with this name already exists.");

        //check for non-ascii characters
        administration.workflows().goTo().
                edit("editable workflow").rename().setNameTo("non-ascii char: \u1234").submit();
        tester.assertTextPresent("Please use only ASCII characters for the workflow name.");
    }

    private void assertWorkflowIsNotEditable()
    {
        tester.assertTextPresent("Workflow cannot be edited as it is not editable.");
        tester.submit("Update");
        tester.assertTextPresent("Edit Workflow");
        tester.assertTextPresent("Workflow cannot be edited as it is not editable.");
        tester.setFormElement("newWorkflowName", "name change");
        tester.setFormElement("description", "desc change");
        tester.submit("Update");
        tester.assertTextPresent("Edit Workflow");
        tester.assertTextPresent("Workflow cannot be edited as it is not editable.");
    }

    public void testEditDraftWorkflow()
     {
         //first lets create a draft.
         administration.workflows().goTo().createDraft("Active workflow");

         //lets edit it.
         //shouldn't be possible to change the name for a draft!
         assertFalse(administration.workflows().goTo().edit("Active workflow").rename().isNameEditable());

         administration.workflows().goTo().edit("Active workflow").
                 rename().setDescriptionTo("well not really since its a draft").submit();

         assertTrue(any
                 (
                         filter(administration.workflows().goTo().active(), byName("Active workflow")),
                         byDescription("This workflow is active")
                 )
         );

         administration.workflows().goTo().edit("Active workflow");
         tester.assertTextPresent("well not really since its a draft");
     }
}
