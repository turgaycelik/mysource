package com.atlassian.jira.webtests.ztests.workflow;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.atlassian.jira.functest.framework.assertions.TableAssertions;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.table.AndCell;
import com.atlassian.jira.webtests.table.EmptyCell;
import com.atlassian.jira.webtests.table.LinkCell;
import com.atlassian.jira.webtests.table.StrictTextCell;
import com.atlassian.jira.webtests.table.TextCell;

/**
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowEditor extends JIRAWebTest
{
    private static final String WORKFLOW_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.8//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_8.dtd\">\n"
            + "<workflow>\n"
            + "  <meta name=\"jira.update.author.key\">admin</meta>\n"
            + "  <meta name=\"jira.description\"></meta>\n"
            + "  <meta name=\"jira.updated.date\">1196830052833</meta>\n"
            + "  <initial-actions>\n"
            + "    <action id=\"1\" name=\"Create\">\n"
            + "      <validators>\n"
            + "        <validator name=\"\" type=\"class\">\n"
            + "          <arg name=\"class.name\">com.atlassian.jira.workflow.validator.PermissionValidator</arg>\n"
            + "          <arg name=\"permission\">Create Issue</arg>\n"
            + "        </validator>\n"
            + "      </validators>\n"
            + "      <results>\n"
            + "        <unconditional-result old-status=\"null\" status=\"open\" step=\"1\">\n"
            + "          <post-functions>\n"
            + "            <function type=\"class\">\n"
            + "              <arg name=\"class.name\">com.atlassian.jira.workflow.function.issue.IssueCreateFunction</arg>\n"
            + "            </function>\n"
            + "            <function type=\"class\">\n"
            + "              <arg name=\"class.name\">com.atlassian.jira.workflow.function.issue.IssueReindexFunction</arg>\n"
            + "            </function>\n"
            + "            <function type=\"class\">\n"
            + "              <arg name=\"class.name\">com.atlassian.jira.workflow.function.event.FireIssueEventFunction</arg>\n"
            + "              <arg name=\"eventTypeId\">1</arg>\n"
            + "            </function>\n"
            + "          </post-functions>\n"
            + "        </unconditional-result>\n"
            + "      </results>\n"
            + "    </action>\n"
            + "  </initial-actions>\n"
            + "  <steps>\n"
            + "    <step id=\"1\" name=\"Open\">\n"
            + "      <meta name=\"jira.status.id\">1</meta>\n"
            + "    </step>\n"
            + "  </steps>\n"
            + "</workflow>";

    TableAssertions tableAssertions;

    public TestWorkflowEditor(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("TestWorkflowEditor.xml");
        tableAssertions = new TableAssertions(tester, environmentData);
    }

    public void testWorkflowListingEnterprise()
    {
        //assert pre-conditions
        administration.workflows().goTo();
        assertEquals(2, getWebTableWithID("active-workflows-table").getRowCount());
        assertTableRowEquals(getWebTableWithID("active-workflows-table"), 0,
                new Object[]
                        {
                                new TextCell("Name"),
                                new TextCell("Last modified"),
                                new TextCell("Assigned Schemes"),
                                new TextCell("Steps"),
                                new TextCell("Operations")
                        });

        assertTableHasMatchingRowFrom(getWebTableWithID("active-workflows-table"), 1,
                new Object[] {
                        "Workflow1",
                        new TextCell("05/Dec/07", "Administrator"),
                        new LinkCell("EditWorkflowScheme.jspa?schemeId=10010", "WorkflowScheme_Workflow1"),
                        new TextCell("1"),
                        new AndCell(
                                new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Workflow1", "View"),
                                new XsrfLinkCell("EditWorkflowDispatcher.jspa?wfName=Workflow1", "Edit"),
                                new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Workflow1", "Copy"))
                });

        assertTableHasMatchingRowFrom(getWebTableWithID("inactive-workflows-table"), 1,
                new Object[] {
                        new TextCell(new String[] { "jira", "Read-only System Workflow", "Default", "The default JIRA workflow." }),
                        new EmptyCell(),
                        new EmptyCell(),
                        new TextCell("5"),
                        new AndCell(
                                new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=jira", "View"),
                                new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=jira", "Copy"))
                });
        assertTableHasMatchingRowFrom(getWebTableWithID("inactive-workflows-table"), 1,
                new Object[] {
                        "Workflow2",
                        new TextCell("05/Dec/07", "Administrator"),
                        new EmptyCell(),
                        new TextCell("1"),
                        new AndCell(
                                new XsrfLinkCell("EditWorkflowDispatcher.jsp?wfName=Workflow2", "Edit"),
                                new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Workflow2", "Copy"),
                                new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=Workflow2", "Delete"))
                });
        assertTableHasMatchingRowFrom(getWebTableWithID("inactive-workflows-table"), 1,
                new Object[] {
                        "Workflow3",
                        new TextCell("05/Dec/07", "Administrator"),
                        new EmptyCell(),
                        new TextCell("1"),
                        new AndCell(
                                new XsrfLinkCell("EditWorkflowDispatcher.jsp?wfName=Workflow3", "Edit"),
                                new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Workflow3", "Copy"),
                                new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=Workflow3", "Delete"))
                });

        //try copying a workflow
        clickLink("copy_Workflow3");
        assertTextPresent("Copy Workflow: Workflow3");
        setFormElement("newWorkflowName", "XX Copy Of Workflow3");
        setFormElement("description", "Description of Workflow 3 copy");
        submit("Update");
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yy");

        administration.workflows().goTo();
        assertTextPresent("Workflows");
        assertTableHasMatchingRowFrom(getWebTableWithID("inactive-workflows-table"), 1,
                new Object[] {
                        "XX Copy Of Workflow3",
                        new TextCell(format.format(new Date()), "Administrator"),
                        new EmptyCell(),
                        new TextCell("1"),
                        new AndCell(
                                new XsrfLinkCell("EditWorkflowDispatcher.jsp?wfName=XX+Copy+Of+Workflow3", "Edit"),
                                new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=XX+Copy+Of+Workflow3", "Copy"),
                                new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=XX+Copy+Of+Workflow3", "Delete"))
                });

        //try deleting a workflow
        clickLink("del_Workflow2");
        assertTextPresent("Delete Workflow");
        assertTextPresent("Confirm that you want to delete the workflow <strong>Workflow2</strong>.");
        assertLinkPresentWithText("do a full backup");
        assertLinkPresentWithText("export");
        submit("Delete");
        assertTextNotPresent("Workflow2");

        //try XML view of a workflow
        clickLink("edit_live_Workflow3");
        clickLink("xml_Workflow3");
        //should really assert the XML is equal here, but since the ordering of elements is different depending on
        //JDK, its a bit tricky.  XMLUnit doesn't help either, since it's the meta elements (with attributes) that
        //are different in order.
        assertEquals(200, tester.getDialog().getResponse().getResponseCode());

        //try adding a workflow
        administration.workflows().goTo();
        clickLink("add-workflow");
        setFormElement("newWorkflowName", "ZZ This is a new Workflow!");
        setFormElement("description", "A new Workflow.");
        submit("Add");
        administration.workflows().goTo();
        assertTableHasMatchingRowFrom(getWebTableWithID("inactive-workflows-table"), 1,
                new Object[] {
                        "ZZ This is a new Workflow!",
                        new TextCell(format.format(new Date()), "Administrator"),
                        new EmptyCell(),
                        new TextCell("1"),
                        new AndCell(
                                new XsrfLinkCell("EditWorkflowDispatcher.jsp?wfName=ZZ+This+is+a+new+Workflow%21", "Edit"),
                                new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=ZZ+This+is+a+new+Workflow%21", "Copy"),
                                new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=ZZ+This+is+a+new+Workflow%21", "Delete"))
                });


        //try importing a workflow via XML.
        clickLinkWithText("Import XML");
        setFormElement("name", "ZZZ Imported workflow");
        setFormElement("description", "This was imported from XML!");
        setFormElement("workflowXML", "Invalid input");
        setFormElement("definition", "inline");
        submit("Import");
        assertTextPresent("Error parsing workflow XML");
        setFormElement("workflowXML", WORKFLOW_XML);
        submit("Import");
        assertTableHasMatchingRowFrom(getWebTableWithID("inactive-workflows-table"), 1,
                new Object[] {
                        new TextCell("ZZZ Imported workflow", "This was imported from XML!"),
                        new TextCell(format.format(new Date()), "Administrator"),
                        new EmptyCell(),
                        new TextCell("1"),
                        new AndCell(
                                new XsrfLinkCell("EditWorkflowDispatcher.jsp?wfName=ZZZ+Imported+workflow", "Edit"),
                                new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=ZZZ+Imported+workflow", "Copy"),
                                new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=ZZZ+Imported+workflow", "Delete"))
                });
    }

    public void testViewWorkflowSteps()
    {
        //lets got to Workflow3 and assert all the pre-conditions
        administration.workflows().goTo().workflowSteps("Workflow3");
        assertTextSequence(new String[] { "Edit Workflow", "Workflow3" });

        assertEquals(2, getWebTableWithID("steps_table").getRowCount());
        assertTableRowEquals(getWebTableWithID("steps_table"), 0,
                new Object[] { new TextCell("Step Name", "(id)"),
                        new TextCell("Linked Status"),
                        new TextCell("Transitions", "(id)"),
                        new TextCell("Operations"),
                });
        assertTableRowEquals(getWebTableWithID("steps_table"), 1,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=1", "Open"), new TextCell("(1)")),
                        new TextCell("Open"),
                        new StrictTextCell(""),
                        new AndCell(new XsrfLinkCell("AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=1", "Add Transition"),
                                new XsrfLinkCell("EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=1", "Edit"),
                                new XsrfLinkCell("ViewWorkflowStepMetaAttributes.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=1", "View Properties"))
                });

        //lets add a Step
        setFormElement("stepName", "");
        submit("Add");
        assertTextPresent("Step name must be specified.");

        setFormElement("stepName", "Resolved");
        selectOption("stepStatus", "Resolved");
        submit("Add");

        assertTableRowEquals(getWebTableWithID("steps_table"), 2,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Resolved"), new TextCell("(2)")),
                        new TextCell("Resolved"),
                        new StrictTextCell(""),
                        new AndCell(new XsrfLinkCell("AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Add Transition"),
                                new XsrfLinkCell("EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Edit"),
                                new XsrfLinkCell("ViewWorkflowStepMetaAttributes.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "View Properties"),
                                new XsrfLinkCell("DeleteWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Step"))
                });

        //Add a Transition
        clickLink("add_trans_2");
        setFormElement("transitionName", "Re-open");
        setFormElement("description", "This transition re-opens a resolved issue.");
        selectOption("view", "Default Screen");
        submit("Add");
        assertTextSequence(new String[] { "Edit Workflow", "Workflow3" });

        assertTableRowEquals(getWebTableWithID("steps_table"), 2,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Resolved"), new TextCell("(2)")),
                        new TextCell("Resolved"),
                        new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow3&workflowTransition=11&workflowStep=2", "Re-open"), new TextCell("(11)", "Open")),
                        new AndCell(new XsrfLinkCell("AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Add Transition"),
                                new XsrfLinkCell("DeleteWorkflowTransitions!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Transitions"),
                                new XsrfLinkCell("EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Edit"),
                                new XsrfLinkCell("ViewWorkflowStepMetaAttributes.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "View Properties"),
                                new XsrfLinkCell("DeleteWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Step"))
                });

        //edit the step.
        clickLink("edit_step_2");
        assertTextPresent("Update Workflow Step");
        assertTextPresent("This page allows you to update the <b>Resolved</b> step.");
        setFormElement("stepName", "Actually Closed");
        selectOption("stepStatus", "Closed");
        submit("Update");

        assertTableRowEquals(getWebTableWithID("steps_table"), 2,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Actually Closed"), new TextCell("(2)")),
                        new TextCell("Closed"),
                        new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow3&workflowTransition=11&workflowStep=2", "Re-open"), new TextCell("(11)", "Open")),
                        new AndCell(new XsrfLinkCell("AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Add Transition"),
                                new XsrfLinkCell("DeleteWorkflowTransitions!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Transitions"),
                                new XsrfLinkCell("EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Edit"),
                                new XsrfLinkCell("ViewWorkflowStepMetaAttributes.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "View Properties"),
                                new XsrfLinkCell("DeleteWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Step"))
                });

        //Add another transition
        clickLink("add_trans_2");
        assertTextPresent("Add Workflow Transition");
        assertTextPresent("Create a transition from <b>Actually Closed</b> to another step.");
        setFormElement("transitionName", "Closed it again");
        setFormElement("description", "");
        selectOption("destinationStep", "Actually Closed");
        submit("Add");

        assertTableRowEquals(getWebTableWithID("steps_table"), 2,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Actually Closed"), new TextCell("(2)")),
                        new TextCell("Closed"),
                        new AndCell(
                                new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow3&workflowTransition=11&workflowStep=2", "Re-open"), new TextCell("(11)", "Open"),
                                new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow3&workflowTransition=21&workflowStep=2", "Closed it again"), new TextCell("21", "Closed")),
                        new AndCell(
                                new XsrfLinkCell("AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Add Transition"),
                                new XsrfLinkCell("DeleteWorkflowTransitions!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Transitions"),
                                new XsrfLinkCell("EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Edit"),
                                new XsrfLinkCell("ViewWorkflowStepMetaAttributes.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "View Properties"))
                });

        //Now lets delete a transition
        clickLink("del_trans_2");
        assertTextPresent("Delete Workflow Transitions");
        assertTextPresent("Please select transitions to delete from the <b>Actually Closed</b> step.");
        selectOption("transitionIds", "Closed it again");
        submit("Delete");

        assertTextSequence(new String[] { "Edit Workflow", "Workflow3" });
        assertTextNotPresent("Closed it again");
    }

    public void testStepProperties()
    {
        administration.workflows().goTo().workflowSteps("Workflow2");
        tester.clickLinkWithText("View Properties");
        tester.assertTextPresent("View Workflow Step Properties: Open");
        tester.assertLinkPresentWithText("workflow steps");
        tester.assertTextPresent("There are currently no defined properties.");
        tester.assertTextPresent("Add New Property");

        //add without key
        tester.submit("Add");
        tester.assertTextPresent("Key must be set.");

        //add without value
        tester.setFormElement("attributeKey", "test.key");
        tester.setFormElement("attributeValue", "");
        tester.submit("Add");

        tester.assertTextNotPresent("There are currently no defined properties.");

        assertEquals(2, getWebTableWithID("metas_table").getRowCount());
        tableAssertions.assertTableRowEquals(getWebTableWithID("metas_table"), 0,
                new Object[] { new TextCell("Property Key"),
                        new TextCell("Property Value"),
                        new TextCell("Operations")
                });
        tableAssertions.assertTableRowEquals(getWebTableWithID("metas_table"), 1,
                new Object[] { new TextCell("test.key"),
                        new StrictTextCell(""),
                        new XsrfLinkCell("RemoveWorkflowStepMetaAttribute.jspa?workflowName=Workflow2&workflowMode=live&workflowStep=1&attributeKey=test.key", "Delete")
                });

        //add without value
        tester.setFormElement("attributeKey", "another.key");
        tester.setFormElement("attributeValue", "This is a value.");
        tester.submit("Add");

        tester.assertTextNotPresent("There are currently no defined properties.");

        assertEquals(3, getWebTableWithID("metas_table").getRowCount());
        assertTableHasMatchingRow(getWebTableWithID("metas_table"),
                new Object[] { new TextCell("another.key"),
                        new StrictTextCell("This is a value."),
                        new XsrfLinkCell("RemoveWorkflowStepMetaAttribute.jspa?workflowName=Workflow2&workflowMode=live&workflowStep=1&attributeKey=another.key", "Delete")
                });

        //try adding a duplicate.
        tester.setFormElement("attributeKey", "another.key");
        tester.submit("Add");
        assertions.getJiraFormAssertions().assertFieldErrMsg("Key 'another.key' already exists.");

        //Delete
        tester.clickLink("del_meta_another.key");
        assertEquals(2, getWebTableWithID("metas_table").getRowCount());
        tester.assertTextNotPresent("another.key");
        tester.assertTextNotPresent("This is a value.");
    }

    public void testEditSingleStepAndTransition()
    {
        administration.workflows().goTo().workflowSteps("Workflow2").add("Resolved", "Resolved");

        navigation.clickLinkWithExactText("Open");
        assertions.assertNodeHasText(new CssLocator(tester, ".aui-page-header-main h2"), "Step: Open");

        //assert the WorkflowBrowser is correct.
        assertions.assertNodeExists(new CssLocator(tester, "#orig_steps #view_transition_1"));
        assertLinkPresentWithSubString("view_transition_1", "ViewWorkflowTransition.jspa");
        assertions.assertNodeHasText(new CssLocator(tester, "#orig_steps #view_transition_1"), "Create");

        assertions.assertNodeExists(new CssLocator(tester, "#dest_steps .workflow-transition-not-found"));
        assertions.assertNodeHasText(new CssLocator(tester, "#dest_steps .workflow-transition-not-found"), "No transitions");

        //lets add an outgoing transition
        clickLink("add_transition");
        setFormElement("transitionName", "Resolve");
        selectOption("destinationStep", "Resolved");
        submit("Add");
        assertions.assertNodeHasText(new CssLocator(tester, ".aui-page-header-main h2"), "Step: Open");

        //assert the WorkflowBrowser is correct.
        assertions.assertNodeExists(new CssLocator(tester, "#orig_steps #view_transition_1"));
        assertions.assertNodeHasText(new CssLocator(tester, "#orig_steps #view_transition_1"), "Create");
        assertLinkPresentWithSubString("view_transition_1", "ViewWorkflowTransition.jspa");

        assertions.assertNodeExists(new CssLocator(tester, "#dest_steps #view_outbound_transition_11"));
        assertions.assertNodeHasText(new CssLocator(tester, "#dest_steps #view_outbound_transition_11"), "Resolve");
        assertLinkPresentWithSubString("view_outbound_transition_11", "ViewWorkflowTransition.jspa");

        //lets add another outgoing transition
        clickLink("add_transition");
        setFormElement("transitionName", "resolveitsomemore");
        selectOption("destinationStep", "Resolved");
        submit("Add");

        //assert the WorkflowBrowser is correct.
        assertions.assertNodeExists(new CssLocator(tester, "#orig_steps #view_transition_1"));
        assertions.assertNodeHasText(new CssLocator(tester, "#orig_steps #view_transition_1"), "Create");
        assertLinkPresentWithSubString("view_transition_1", "ViewWorkflowTransition.jspa");

        assertions.assertNodeExists(new CssLocator(tester, "#dest_steps #view_outbound_transition_11"));
        assertions.assertNodeHasText(new CssLocator(tester, "#dest_steps #view_outbound_transition_11"), "Resolve");
        assertLinkPresentWithSubString("view_outbound_transition_11", "ViewWorkflowTransition.jspa");

        assertions.assertNodeExists(new CssLocator(tester, "#dest_steps #view_outbound_transition_21"));
        assertions.assertNodeHasText(new CssLocator(tester, "#dest_steps #view_outbound_transition_21"), "resolveitsomemore");
        assertLinkPresentWithSubString("view_outbound_transition_21", "ViewWorkflowTransition.jspa");

        //add an incoming transition
        clickLink("add_transition");
        setFormElement("transitionName", "opensomemore");
        submit("Add");

        //assert the WorkflowBrowser is correct.
        assertions.assertNodeExists(new CssLocator(tester, "#orig_steps #view_transition_1"));
        assertions.assertNodeHasText(new CssLocator(tester, "#orig_steps #view_transition_1"), "Create");
        assertLinkPresentWithSubString("view_transition_1", "ViewWorkflowTransition.jspa");

        assertions.assertNodeExists(new CssLocator(tester, "#orig_steps #view_transition_31"));
        assertions.assertNodeHasText(new CssLocator(tester, "#orig_steps #view_transition_31"), "opensomemore");
        assertLinkPresentWithSubString("view_transition_31", "ViewWorkflowTransition.jspa");

        assertions.assertNodeExists(new CssLocator(tester, "#dest_steps #view_outbound_transition_11"));
        assertions.assertNodeHasText(new CssLocator(tester, "#dest_steps #view_outbound_transition_11"), "Resolve");
        assertLinkPresentWithSubString("view_outbound_transition_11", "ViewWorkflowTransition.jspa");

        assertions.assertNodeExists(new CssLocator(tester, "#dest_steps #view_outbound_transition_21"));
        assertions.assertNodeHasText(new CssLocator(tester, "#dest_steps #view_outbound_transition_21"), "resolveitsomemore");
        assertLinkPresentWithSubString("view_outbound_transition_21", "ViewWorkflowTransition.jspa");

        assertions.assertNodeExists(new CssLocator(tester, "#dest_steps #view_outbound_transition_31"));
        assertions.assertNodeHasText(new CssLocator(tester, "#dest_steps #view_outbound_transition_31"), "opensomemore");
        assertLinkPresentWithSubString("view_outbound_transition_31", "ViewWorkflowTransition.jspa");


        //lets view a transition
        clickLinkWithText("resolveitsomemore");
        assertTextPresent("Transition: resolveitsomemore");
        assertTextSequence(new String[] { "Screen", "None", "it will happen instantly" });

        //assert the WorkflowBrowser is correct.
        assertions.assertNodeExists(new CssLocator(tester, "#orig_steps #view_inbound_step_1"));
        assertions.assertNodeHasText(new CssLocator(tester, "#orig_steps #view_inbound_step_1"), "Open");
        assertLinkPresentWithSubString("view_inbound_step_1", "ViewWorkflowStep.jspa");

        assertions.assertNodeHasText(new CssLocator(tester, ".workflow-current-context .workflow-transition-lozenge"), "resolveitsomemore");

        assertions.assertNodeExists(new CssLocator(tester, "#dest_steps #view_outbound_step_2"));
        assertions.assertNodeHasText(new CssLocator(tester, "#dest_steps #view_outbound_step_2"), "Resolved");
        assertLinkPresentWithSubString("view_outbound_step_2", "ViewWorkflowStep.jspa");


        //edit the transition
        clickLink("edit_transition");
        setFormElement("transitionName", "resolveitalittleless");
        submit("Update");

        //assert the WorkflowBrowser is correct.
        assertions.assertNodeExists(new CssLocator(tester, "#orig_steps #view_inbound_step_1"));
        assertions.assertNodeHasText(new CssLocator(tester, "#orig_steps #view_inbound_step_1"), "Open");
        assertLinkPresentWithSubString("view_inbound_step_1", "ViewWorkflowStep.jspa");

        assertions.assertNodeHasText(new CssLocator(tester, ".workflow-current-context .workflow-transition-lozenge"), "resolveitalittleless");

        assertions.assertNodeExists(new CssLocator(tester, "#dest_steps #view_outbound_step_2"));
        assertions.assertNodeHasText(new CssLocator(tester, "#dest_steps #view_outbound_step_2"), "Resolved");
        assertLinkPresentWithSubString("view_outbound_step_2", "ViewWorkflowStep.jspa");

        //lets look at a 'loop-back transition'
        navigation.clickLinkWithExactText("Open");
        clickLinkWithText("opensomemore");

        //assert the WorkflowBrowser is correct.
        assertions.assertNodeExists(new CssLocator(tester, "#orig_steps #view_inbound_step_1"));
        assertions.assertNodeHasText(new CssLocator(tester, "#orig_steps #view_inbound_step_1"), "Open");
        assertLinkPresentWithSubString("view_inbound_step_1", "ViewWorkflowStep.jspa");

        assertions.assertNodeHasText(new CssLocator(tester, ".workflow-current-context .workflow-transition-lozenge"), "opensomemore");

        assertions.assertNodeExists(new CssLocator(tester, "#dest_steps #view_outbound_step_1"));
        assertions.assertNodeHasText(new CssLocator(tester, "#dest_steps #view_outbound_step_1"), "Open");
        assertLinkPresentWithSubString("view_outbound_step_1", "ViewWorkflowStep.jspa");

        //finally try to delete the transition
        clickLinkWithText("Delete");
        assertTextPresent("Delete Workflow Transitions");
        assertTextSequence(new String[] { "Confirm that you want to delete", "opensomemore", "transition(s)." });
        submit("Delete");

        //we should come back to the view workflows table.
        assertTextSequence(new String[] { "Edit Workflow", "Workflow2" });

        //just to be safe, make sure the transitions we added do/don't show up here.
        assertTextNotPresent("opensomemore");
        assertLinkPresentWithText("Resolve");
        assertLinkPresentWithText("resolveitalittleless");
    }

    public void testValidatorsConditionsAndPostFunctions()
    {
        administration.workflows().goTo().workflowSteps("Workflow2");
        clickLink("add_trans_1");
        setFormElement("transitionName", "testtransition");
        submit("Add");
        clickLinkWithText("testtransition");

        assertTextPresent("Transition: testtransition");

        // FIXME: unless scoped, these assertions are useless.
        assertLinkPresentWithText("Validators");
        assertLinkPresentWithText("Post Functions");
        assertLinkPresentWithText("Add");

        //lets add a condition.
        tester.clickLinkWithText("Add condition", 0);
        assertTextPresent("Add Condition To Transition");

        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Only Assignee Condition"), new TextCell("Condition to allow only the assignee to execute a transition.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Only Reporter Condition"), new TextCell("Condition to allow only the reporter to execute a transition.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Permission Condition"), new TextCell("Condition to allow only users with a certain permission to execute a transition.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Sub-Task Blocking Condition"), new TextCell("Condition to block parent issue transition depending on sub-task status.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("User Is In Group"), new TextCell("Condition to allow only users in a given group to execute a transition.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("User Is In Group Custom Field"), new TextCell("Condition to allow only users in a custom field-specified group to execute a transition.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("User Is In Project Role"), new TextCell("Condition to allow only users in a given project role to execute a transition.") });

        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:onlyassignee-condition");
        submit("Add");
        assertTextPresent("Only the <b>assignee</b> of the issue can execute this transition.");

        // add another condition
        tester.clickLinkWithText("Add condition", 0);
        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:isuserinprojectrole-condition");
        submit("Add");
        assertTextPresent("Add Parameters To Condition");
        assertTextPresent("Add required parameters to the Condition.");
        selectOption("jira.projectrole.id", "Developers");
        submit("Add");
        assertTextPresent("Only users in project role <b>Developers</b> can execute this transition.");

        assertTextSequence(new String[] { "Switch to OR",
                                          "Only the <b>assignee</b> of the issue can execute this transition.",
                                          "Only users in project role <b>Developers</b> can execute this transition."});
        clickLinkWithText("Switch to OR");

        assertTextSequence(new String[] { "Switch to AND",
                                          "Only the <b>assignee</b> of the issue can execute this transition.",
                                          "Only users in project role <b>Developers</b> can execute this transition."});

        //test the grouping functionality.  This isn't a great test, but better than nothing
        clickLinkWithText("Add grouped condition");
        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:onlyassignee-condition");
        submit("Add");

        assertTextSequence(new String[] { "Switch to AND",
                                          "Switch to OR",
                                          "Only the <b>assignee</b> of the issue can execute this transition.",
                                          "Only the <b>assignee</b> of the issue can execute this transition.",
                                          "Only users in project role <b>Developers</b> can execute this transition."});

        //lets add some validators
        clickLinkWithText("Validators");
        clickLinkWithText("Add validator");

        assertTextPresent("Add Validator To Transition");
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Permission Validator"), new TextCell("Validates that the user has a permission.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("User Permission Validator"), new TextCell("Validates that the user has a permission, where the OSWorkflow variable holding the username is configurable. Obsolete.") });

        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:permission-validator");
        submit("Add");
        assertTextPresent("Add Parameters To Validator");
        assertTextPresent("Add required parameters to the Validator.");
        selectOption("permissionKey", "Assignable User");
        submit("Add");

        assertTextSequence(new String[] {
                "Add validator", "The transition requires the following criteria to be valid",
                "Edit", "Delete",
                "Only users with <b>Assignable User</b> permission can execute this transition." });

        // Finally lets try some post functions
        clickLinkWithText("Post Functions");
        clickLinkWithText("Add post function");
        assertTextPresent("Add Post Function To Transition");
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Assign to Current User"), new TextCell("Assigns the issue to the current user if the current user has the 'Assignable User' permission.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Assign to Lead Developer"), new TextCell("Assigns the issue to the project/component lead developer") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Assign to Reporter"), new TextCell("Assigns the issue to the reporter") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Update Issue Field"), new TextCell("Updates a simple issue field to a given value.") });

        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:assigntocurrentuser-function");
        submit("Add");

        assertTextSequence(new String[] {
                "Add post function", "The following will be processed after the transition occurs",
                "Set issue status to the linked status of the destination workflow step.",
                "Assign the issue to the current user. Please note that the issue will only be assigned to the current user if the current user has the 'Assignable User' permission.",
                "Add a comment to an issue if one is entered during a transition.",
                "Update change history for an issue and store the issue in the database.",
                "Re-index an issue to keep indexes in sync with the database.",
                "Fire a", "Generic Event", "event that can be processed by the listeners."
        });

        //lets move it to the top
        clickLinkWithText("Move Up");
        assertLinkWithTextNotPresent("Can't move up from the top!", "Move Up");
        assertTextSequence(new String[] {
                "Add post function", "The following will be processed after the transition occurs",
                "Assign the issue to the current user. Please note that the issue will only be assigned to the current user if the current user has the 'Assignable User' permission.",
                "Set issue status to the linked status of the destination workflow step.",
                "Add a comment to an issue if one is entered during a transition.",
                "Update change history for an issue and store the issue in the database.",
                "Re-index an issue to keep indexes in sync with the database.",
                "Fire a", "Generic Event", "event that can be processed by the listeners."
        });

        //lets move it to the bottom
        while (tester.getDialog().isLinkPresentWithText("Move Down"))
        {
            clickLinkWithText("Move Down");
        }

        assertLinkNotPresentWithText("Move Down");
        assertTextSequence(new String[] {
                "Add post function", "The following will be processed after the transition occurs",
                "Set issue status to the linked status of the destination workflow step.",
                "Add a comment to an issue if one is entered during a transition.",
                "Update change history for an issue and store the issue in the database.",
                "Re-index an issue to keep indexes in sync with the database.",
                "Fire a", "Generic Event", "event that can be processed by the listeners.",
                "Assign the issue to the current user. Please note that the issue will only be assigned to the current user if the current user has the 'Assignable User' permission."
        });
    }

    private class XsrfLinkCell extends com.atlassian.jira.webtests.table.LinkCell
    {
        private XsrfLinkCell(final String url, final String label)
        {
            super(page.addXsrfToken(url), label);
        }
    }

}
