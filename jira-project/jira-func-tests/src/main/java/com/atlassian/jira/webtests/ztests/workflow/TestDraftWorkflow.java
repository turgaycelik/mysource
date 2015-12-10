package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebResponse;

import static com.atlassian.jira.functest.framework.admin.ViewWorkflows.WorkflowItemsList.Predicates.byDescription;
import static com.atlassian.jira.functest.framework.admin.ViewWorkflows.WorkflowItemsList.Predicates.byName;
import static com.atlassian.jira.functest.framework.admin.ViewWorkflows.WorkflowItemsList.Predicates.schemesEqual;
import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;

/**
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestDraftWorkflow extends JIRAWebTest
{
    public TestDraftWorkflow(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("TestDraftWorkflow.xml");
        HttpUnitOptions.setScriptingEnabled(true);
    }

    public void tearDown()
    {
        HttpUnitOptions.setScriptingEnabled(false);
        super.tearDown();
    }

    public void testPublishDraftWorkflowWithFunnyNames()
    {
        administration.restoreData("TestDraftWorkflowFunnyNames.xml");

        administration.workflows().goTo().createDraft("Funny&Name").add("In Progress", "In Progress");

        assertTrue(administration.workflows().goTo().active().drafts().contains("Funny&Name"));

        //check that the active workflow does not have the new step.
        administration.workflows().view("Funny&Name");
        tester.assertTextNotPresent("In Progress");

        //check that the draft has the step.
        administration.workflows().goTo().workflowSteps("Funny&Name");
        tester.assertTextPresent("In Progress");

        //ensure cancel publish goes back to the workflow listing
        administration.workflows().goTo().publishDraft("Funny&Name").cancel();
        assertTrue(administration.workflows().active().drafts().contains("Funny&Name"));

        //now lets publish.
        administration.workflows().goTo().
                publishDraft("Funny&Name").
                backupOriginalWorkflowAs("overwritten workflowDude").
                publish();

        administration.workflows().goTo();

        assertFalse(administration.workflows().active().drafts().contains("Funny&Name"));
        assertTrue(administration.workflows().inactive().contains("overwritten workflowDude"));

        //check that the active workflow does now have the new step.
        tester.clickLink("steps_live_Funny&Name");
        tester.assertTextPresent("In Progress");

        //check the copy that was created doesn't have the new step.
        administration.workflows().goTo().
                edit("overwritten workflowDude").
                textView().
                goTo();
        tester.assertTextNotInTable("steps_table", "In Progress");
    }

    public void testPublishDraftWorkflow()
    {
        //lets create draft workflow first.
        administration.workflows().goTo().createDraft("Workflow1").
                add("In Progress", "In Progress");

        assertTrue(administration.workflows().goTo().active().drafts().contains("Workflow1"));

        //check that the active workflow does not have the new step.
        administration.workflows().view("Workflow1");
        tester.assertTextNotPresent("In Progress");

        //check that the draft has the step.
        administration.workflows().goTo().workflowSteps("Workflow1");
        tester.assertTextPresent("In Progress");

        //ensure cancel publish goes back to the workflow listing
        administration.workflows().goTo().publishDraft("Workflow1").cancel();
        assertTrue(administration.workflows().active().drafts().contains("Workflow1"));

        //now lets publish.
        administration.workflows().goTo().
                publishDraft("Workflow1").
                backupOriginalWorkflowAs("overwritten workflowDude").
                publish();

        administration.workflows().goTo();
        assertFalse(administration.workflows().active().drafts().contains("Workflow1"));
        assertTrue(administration.workflows().inactive().contains("overwritten workflowDude"));

        //check that the active workflow does now have the new step.
        tester.clickLink("steps_live_Workflow1");
        tester.assertTextPresent("In Progress");

        //check the copy that was created doesn't have the new step.
        administration.workflows().goTo().
                edit("overwritten workflowDude").
                textView().
                goTo();
        tester.assertTextNotInTable("steps_table", "In Progress");
    }

    public void testPublishDraftWithoutBackup()
    {
        //lets create draft workflow first.
        administration.workflows().goTo().createDraft("Workflow1").
                add("In Progress", "In Progress");
        assertTrue(administration.workflows().goTo().active().drafts().contains("Workflow1"));

        //publish the draft without creating a backup.
        administration.workflows().goTo().publishDraft("Workflow1").publish();

        administration.workflows().goTo();
        assertFalse(administration.workflows().active().drafts().contains("Workflow1"));
        assertFalse(administration.workflows().inactive().contains("Copy of Workflow1"));
    }

    public void testHideDeleteLink()
    {
        administration.workflows().goTo();

        //lets create a draft.
        administration.workflows().createDraft("Workflow1");

        //shouldn't be able to delete an existing step
        tester.assertLinkNotPresent("delete_step_1");

        //also check the single step view
        navigation.clickLinkWithExactText("Open");
        tester.assertLinkNotPresent("del_step");

        //lets add a new step. This should now display a delete link.
        administration.workflows().goTo().workflowSteps("Workflow1").add("ClosedStep", "Closed");
        tester.assertTextInTable("steps_table", "ClosedStep");
        tester.assertLinkPresent("delete_step_2");

        //also check the single step view
        tester.clickLinkWithText("ClosedStep");
        tester.assertLinkPresent("del_step");

        //lets delete the new step.
        administration.workflows().goTo().workflowSteps("Workflow1");
        tester.clickLink("delete_step_2");
        tester.assertTextPresent("Delete Workflow Step: ClosedStep");
        tester.submit("Delete");

        tester.assertTextNotInTable("steps_table", "ClosedStep");
    }

    public void testEditStatusDisabled()
    {
        administration.workflows().goTo();
        //lets create a draft
        administration.workflows().createDraft("Workflow1");
        tester.assertTextInTable("steps_table", "Open");
        tester.assertTextNotInTable("steps_table", "EditedStep");

        //lets edit the existing step.  Shouldn't be able to modify the status.
        tester.clickLink("edit_step_1");
        assertions.assertNodeExists(new CssLocator(tester, "select[name=stepStatus][disabled]"));
        tester.setFormElement("stepName", "EditedStep");
        tester.submit("Update");
        tester.assertTextInTable("steps_table", "EditedStep");

        //lets create a new step and edit its status.
        tester.setFormElement("stepName", "ClosedStep");
        tester.selectOption("stepStatus", "Closed");
        tester.submit("Add");
        tester.assertTextNotInTable("steps_table", "Reopened");
        tester.assertTextNotInTable("steps_table", "ClosedStepEdit");

        tester.clickLink("edit_step_2");
        //this is a new step, so we should be able to edit the status.
        assertions.assertNodeDoesNotExist(new CssLocator(tester, "select[name=stepStatus][disabled]"));
        tester.setFormElement("stepName", "ClosedStepEdit");
        tester.selectOption("stepStatus", "Reopened");
        tester.submit("Update");

        tester.assertTextInTable("steps_table", "Reopened");
        tester.assertTextInTable("steps_table", "ClosedStepEdit");
    }

    public void testEditValidation()
    {
        administration.workflows().goTo().createDraft("Workflow1");

        //try to hack a URL where we're changing the stepStatus of an existing step.
        tester.gotoPage(page.addXsrfToken("secure/admin/workflows/EditWorkflowStep.jspa?workflowMode=draft&workflowName=Workflow1&workflowStep=1&stepStatus=Closed&stepName=sweet"));

        tester.assertTextPresent("Cannot change the status of an existing step on a draft workflow");
    }

    public void testDeleteDraftValidation()
    {
        administration.workflows().goTo().createDraft("Workflow1");

        //try to hack a URL where we're changing the stepStatus of an existing step.
        tester.gotoPage("secure/admin/workflows/DeleteWorkflowStep!default.jspa?workflowMode=draft&workflowName=Workflow1&workflowStep=1");
        tester.submit("Delete");

        tester.assertTextPresent("Cannot delete an existing step on a draft workflow.");
    }

    //TODO: this is not testing a draft workflow.  We might want to move this.
    public void testDeleteValidation()
    {
        administration.workflows().goTo();

        //try to hack a URL where we're changing the stepStatus of an existing step.
        tester.gotoPage("secure/admin/workflows/DeleteWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow1&workflowStep=1");
        tester.submit("Delete");

        tester.assertTextPresent("Cannot delete step. This workflow is not editable.");
    }

    public void testWorkflowDraftName()
    {
        administration.workflows().goTo().createDraft("Workflow1");

        //the workflow name should have (Draft appended).
        assertTextSequence(new String[] { "Workflow1", "Draft" });

        //lets add a step
        tester.setFormElement("stepName", "Dude");
        tester.submit("Add");

        //lets add a transition
        tester.clickLinkWithText("Add Transition");
        tester.setFormElement("transitionName", "testTransition");
        tester.submit("Add");

        //look at the single step view workflow name should have (Draft appended).
        navigation.clickLinkWithExactText("Open");
        assertTextSequence(new String[] { "Workflows", "Workflow1 (Draft)", "Step: Open" });

        //lets look at the testTransition workflow name should have (Draft appended).
        tester.clickLinkWithText("testTransition");
        tester.assertTextPresent("Transition: testTransition");
        assertTextSequence(new String[] { "Workflows", "Workflow1 (Draft)", "Transition: testTransition" });

        //finally lets look at the step's properties
        navigation.clickLinkWithExactText("Open");
        tester.clickLink("view_properties_1");
        assertTextSequence(new String[] { "View", "workflow steps", "of", "Workflow1 (Draft)" });

        //NOW lets check the original workflow.  There shouldn't be (Draft) anywhere.
        tester.clickLink("workflows");
        tester.clickLink("steps_live_Workflow1");
        tester.assertTextNotPresent("(Draft)");
        navigation.clickLinkWithExactText("Open");
        tester.assertTextNotPresent("(Draft)");
        tester.clickLink("view_properties_1");
        tester.assertTextNotPresent("(Draft)");
        tester.clickLinkWithText("workflow steps");
        navigation.clickLinkWithExactText("Open");
        tester.clickLink("view_transition_1");
        tester.assertTextNotPresent("(Draft)");
    }

    public void testDeleteLinkNotShownForInitialTransition()
    {
        administration.workflows().goTo();

        //lets create a copy of the default workflow
        administration.workflows().goTo().copyWorkflow("jira", "Copy of jira");

        administration.workflows().goTo().
                edit("Copy of jira").
                textView().
                goTo();

        navigation.clickLinkWithExactText("Open");
        tester.clickLinkWithText("Start Progress");
        tester.assertTextPresent("Transition: Start Progress");
        //check a normal transition has the delete link.
        tester.assertLinkPresent("delete_transition");

        //an initial transition should NOT have the delete link.
        navigation.clickLinkWithExactText("Open");
        tester.clickLink("view_transition_1");
        tester.assertTextPresent("Transition: Create Issue");
        tester.assertLinkNotPresent("delete_transition");
    }

    public void testAddTransitionForStepWithNoTransitions()
    {
        administration.workflows().goTo().createDraft("Workflow1");
        //check the link isn't present.
        assertLinkWithTextNotPresent("Haven't opened the step yet", "Add Transition");
        navigation.clickLinkWithExactText("Open");
        tester.assertTextPresent("You can't add an outgoing transition to this step unless one already exists on the active workflow.");
        assertLinkWithTextNotPresent("Shouldn't be able to add a transition for this draft", "Add Transition");

        //check that if the URL is hacked, the correct error is shown.
        tester.gotoPage("/secure/admin/workflows/AddWorkflowTransition!default.jspa?workflowMode=draft&workflowName=Workflow1&workflowStep=1");
        assertTextPresent("Add Workflow Transition");
        assertions.getJiraFormAssertions().assertFormErrMsg("You are editing a draft workflow. The step 'Open' has no "
                + "outgoing transitions in the Active workflow, so you cannot add any outgoing transitions in the Draft workflow.");
        tester.submit("Add");
        tester.assertTextPresent("Add Workflow Transition");
        assertions.getJiraFormAssertions().assertFormErrMsg("You are editing a draft workflow. The step 'Open' has no "
                + "outgoing transitions in the Active workflow, so you cannot add any outgoing transitions in the Draft workflow.");

        //Finally lets try adding some transitions for a new step.
        administration.workflows().goTo().createDraft("Workflow1");
        tester.setFormElement("stepName", "Dude");
        tester.submit("Add");

        //add the transition
        tester.clickLinkWithText("Add Transition");
        tester.setFormElement("transitionName", "New Transition");
        tester.submit("Add");

        assertTextSequence(new String[] { "Edit Workflow", "Workflow1" });
        tester.assertTextPresent("New Transition");
    }

    public void testOverWriteWorkflowWithInvalidTransition()
    {
        administration.restoreData("TestOverwriteInvalidWorkflow.xml");

        administration.workflows().goTo().publishDraft("Workflow1").publish();
        assertions.getJiraFormAssertions().assertFormErrMsg("You are editing a draft workflow. The step 'Open' has no"
                + " outgoing transitions in the Active workflow, so you cannot add any outgoing transitions in the"
                + " Draft workflow");
    }

    public void testCreateAndDeleteDraftWorkflow()
    {
        administration.workflows().goTo().createDraft("Workflow1");
        administration.workflows().goTo();

        //check that the workflows table now has a draft workflow.
        assertTrue(administration.workflows().active().contains("Workflow1"));
        assertTrue(administration.workflows().active().drafts().contains("Workflow1"));

        //now lets try to discard the draft workflow!
        administration.workflows().goTo().edit("Workflow1");
        tester.clickLinkWithText("Discard Draft");
        tester.assertTextPresent("Discard Draft Workflow");
        tester.assertTextPresent("Confirm that you want to discard the draft workflow <strong>Workflow1</strong>.");
        tester.submit("Delete");

        //check that the draft workflow is gone.
        administration.workflows().goTo();
        assertTrue(administration.workflows().active().contains("Workflow1"));
        assertFalse(administration.workflows().active().drafts().contains("Workflow1"));
    }

    public void testWorkflowCachingHeaders()
    {
        administration.workflows().goTo();
        tester.clickLink("steps_live_jira");
        tester.clickLink("xml_jira");
        final WebResponse response = tester.getDialog().getResponse();
        // JRA-17446: check that headers are compatible with IE over SSL
        assertEquals("", response.getHeaderField("Pragma"));
        final String cacheControl = response.getHeaderField("Cache-Control");
        assertTrue(cacheControl.contains("private"));
        assertTrue(cacheControl.contains("must-revalidate"));
        assertTrue(cacheControl.contains("max-age"));
        assertResponseCanBeCached();
    }

    public void testDeactivateDraftWorkflow()
    {
        administration.restoreData("TestDeactivateDraftWorkflowEnterprise.xml");

        administration.workflows().goTo();

        //assert pre-conditions
        assertTrue(administration.workflows().active().contains("Workflow1"));
        assertTrue(administration.workflows().active().drafts().contains("Workflow1"));

        assertTrue(all
                (
                        filter(administration.workflows().active(), byName("Workflow1")),
                        schemesEqual(ImmutableMultiset.of("WorkflowScheme_Workflow1", "A second scheme"))
                )
        );

        assertTrue(administration.workflows().active().contains("Workflow3"));
        assertTrue(administration.workflows().active().drafts().contains("Workflow3"));

        assertTrue(all
                (
                        filter(administration.workflows().active(), byName("Workflow3")),
                        schemesEqual(ImmutableMultiset.of("A second scheme"))
                )
        );

        administration.workflows().goTo().view("Workflow1");
        tester.assertTextNotPresent("NewDraftStep");

        administration.workflows().goTo().workflowSteps("Workflow1");
        tester.assertTextPresent("NewDraftStep");

        administration.workflows().goTo().view("Workflow3");
        tester.assertTextNotPresent("AnotherNewDraftStep");

        administration.workflows().goTo().workflowSteps("Workflow3");
        tester.assertTextPresent("AnotherNewDraftStep");

        //now lets migrate project monkey to the default scheme.  This should cause one of the drafts to be copied and deleted
        administration.project().associateWorkflowScheme("monkey", "Default");

        administration.workflows().goTo();
        //assert a copy was created for the draft of the deactivated workflow
        assertEquals(size(filter(administration.workflows().inactive(), byName("Copy of Workflow3"))), 1);
        assertTrue(all
                (
                        filter(administration.workflows().inactive(), byName("Copy of Workflow3")),
                        and(
                                byDescription
                                        (
                                                "(This copy was automatically generated from a draft, when workflow "
                                                        + "'Workflow3' was made inactive.)"
                                        ),
                                schemesEqual(ImmutableMultiset.<String>of())
                        )
                )
        );

        administration.workflows().workflowSteps("Copy of Workflow3");
        tester.assertTextPresent("AnotherNewDraftStep");

        administration.workflows().goTo();
        assertTrue(administration.workflows().inactive().contains("Workflow3"));
        assertTrue(all
                (
                        filter(administration.workflows().active(), byName("Workflow3")),
                        schemesEqual(ImmutableMultiset.of("A second scheme"))
                )
        );

        administration.workflows().workflowSteps("Workflow3");
        tester.assertTextNotPresent("AnotherNewDraftStep");

        // now lets migrate project homo to the default scheme.  This should cause the remaining draft to be copied and deleted.
        // Try to migrate the project again
        administration.project().associateWorkflowScheme("homosapien", "Default", ImmutableMap.<String, String>of(), true);

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration("homosapien", "Default");

        //assert no drafts left, and two copies present.
        administration.workflows().goTo();
        assertEquals(size(filter(administration.workflows().inactive(), byName("Copy of Workflow1"))), 1);
        assertTrue(all
                (
                        filter(administration.workflows().inactive(), byName("Copy of Workflow1")),
                        and(
                                byDescription
                                        (
                                                "(This copy was automatically generated from a draft, when workflow "
                                                        + "'Workflow1' was made inactive.)"
                                        ),
                                schemesEqual(ImmutableMultiset.<String>of())
                        )
                )
        );

        administration.workflows().workflowSteps("Copy of Workflow1");
        tester.assertTextPresent("NewDraftStep");

        administration.workflows().goTo();
        assertTrue(administration.workflows().inactive().contains("Workflow1"));
        assertTrue(all
                (
                        filter(administration.workflows().active(), byName("Workflow1")),
                        schemesEqual(ImmutableMultiset.of("WorkflowScheme_Workflow1", "A second scheme"))
                )
        );

        administration.workflows().workflowSteps("Workflow1");
        tester.assertTextNotPresent("NewDraftStep");
    }
}
