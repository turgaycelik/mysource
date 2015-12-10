package com.atlassian.jira.webtests.ztests.workflow;

import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.atlassian.jira.webtests.table.TextCell;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.meterware.httpunit.WebTable;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests the workflow editor, bulk and normal workflow transitions with workflows that contain common workflow
 * transition to that transitions back to the original step (step id of -1). JRA-12017
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowWithOriginalStepTransitions extends FuncTestCase
{
    public void setUpTest()
    {
        administration.restoreData("TestWorkflowWithOriginalStepTransitions.xml");
    }

    public void testWorkflowEditor() throws SAXException
    {
        navigation.gotoWorkflows();
        tester.assertTextPresent("workflow with originating_step transition");
        tester.clickLink("steps_live_workflow with originating_step transition");

        //now assert the steps are all present and that the Close transition always goes back to the same status as
        //the current step.
        WebTable stepsTable = new TableLocator(tester, "steps_table").getTable();
        assertions.getTableAssertions().assertTableRowEquals(stepsTable, 1,
                new Object[] { new TextCell("Open", "(1)"),
                               new TextCell("Open"),
                               new TextCell(new String[] {
                                       "Start Progress", "(4)", ">>", "In Progress",
                                       "Resolve Issue", "(5)", ">>", "Resolved",
                                       "Close Issue", "(2)", ">>", "Open" }),
                               //don't really care
                               null
                });
        text.assertTextNotPresent(new TableCellLocator(tester, "steps_table", 1, 2), "Closed");

        assertions.getTableAssertions().assertTableRowEquals(stepsTable, 2,
                new Object[] { new TextCell("In Progress", "(3)"),
                               new TextCell("In Progress"),
                               new TextCell(new String[] {
                                       "Stop Progress", "(301)", ">>", "Open",
                                       "Resolve Issue", "(5)", ">>", "Resolved",
                                       "Close Issue", "(2)", ">>", "In Progress" }),
                               //don't really care
                               null
                });
        text.assertTextNotPresent(new TableCellLocator(tester, "steps_table", 2, 2), "Closed");

        assertions.getTableAssertions().assertTableRowEquals(stepsTable, 3,
                new Object[] { new TextCell("Resolved", "(4)"),
                               new TextCell("Resolved"),
                               new TextCell(new String[] {
                                       "Close Issue", "(701)", ">>", "Closed",
                                       "Reopen Issue", "(3)", ">>", "Reopened" }),
                               //don't really care
                               null
                });
        text.assertTextNotPresent(new TableCellLocator(tester, "steps_table", 3, 2), "Resolved");

        assertions.getTableAssertions().assertTableRowEquals(stepsTable, 4,
                new Object[] { new TextCell("Reopened", "(5)"),
                               new TextCell("Reopened"),
                               new TextCell(new String[] {
                                       "Resolve Issue", "(5)", ">>", "Resolved",
                                       "Close Issue", "(2)", ">>", "Reopened",
                                       "Start Progress", "(4)", ">>", "In Progress" }),
                               //don't really care
                               null
                });
        text.assertTextNotPresent(new TableCellLocator(tester, "steps_table", 4, 2), "Closed");

        assertions.getTableAssertions().assertTableRowEquals(stepsTable, 5,
                new Object[] { new TextCell("Closed", "(6)"),
                               new TextCell("Closed"),
                               new TextCell(new String[] {
                                       "Reopen Issue", "(3)", ">>", "Reopened" }),
                               //don't really care
                               null
                });
        text.assertTextNotPresent(new TableCellLocator(tester, "steps_table", 5, 2), "Closed");

        //now lets check the 'close' transition
        tester.clickLinkWithText("Close Issue");

        WorkflowBrowser workflowBrowser = new WorkflowBrowser();

        //make sure the workflow browser is in the correct state
        assertThat(workflowBrowser.getOrigins(), Matchers.contains("Open", "In Progress", "Reopened"));
        assertThat(workflowBrowser.getCurrent(), Matchers.is("Close Issue"));
        assertThat(workflowBrowser.getDestinations(), Matchers.contains("Open", "In Progress", "Reopened"));


        //let's create a draft and try to edit the transition to ensure it behaves as expected.
        tester.clickLink("create_draft_workflow");
        tester.clickLinkWithText("Close Issue");

        //make sure the workflow browser is in the correct state
        assertThat(workflowBrowser.getOrigins(), Matchers.contains("Open", "In Progress", "Reopened"));
        assertThat(workflowBrowser.getCurrent(), Matchers.is("Close Issue"));
        assertThat(workflowBrowser.getDestinations(), Matchers.contains("Open", "In Progress", "Reopened"));


        tester.clickLink("edit_transition");
        //transitions like these should have a special dest step linking back to the original (-1)
        tester.assertOptionValuesEqual("destinationStep", new String[] { "1", "3", "4", "5", "6", "-1" });
        tester.setFormElement("description", "Adding a description");
        tester.submit("Update");
        tester.assertTextPresent("Transition: Close Issue");
        tester.assertTextPresent("Adding a description");

        //make sure the workflow browser is in the correct state
        assertThat(workflowBrowser.getOrigins(), Matchers.contains("Open", "In Progress", "Reopened"));
        assertThat(workflowBrowser.getCurrent(), Matchers.is("Close Issue"));
        assertThat(workflowBrowser.getDestinations(), Matchers.contains("Open", "In Progress", "Reopened"));

        //now lets link the transition back to the closed step.  Should now only have one destination step.
        tester.clickLink("edit_transition");
        tester.selectOption("destinationStep", "Closed");
        tester.submit("Update");

        //make sure the workflow browser is in the correct state
        assertThat(workflowBrowser.getOrigins(), Matchers.contains("Open", "In Progress", "Reopened"));
        assertThat(workflowBrowser.getCurrent(), Matchers.is("Close Issue"));
        assertThat(workflowBrowser.getDestinations(), Matchers.contains("Closed"));

        //finally, go back to the edit screen and make sure the -1 option doesn't show up any longer.  The only
        //way you can get into this state is via an XML import.
        tester.clickLink("edit_transition");
        tester.assertOptionValuesEqual("destinationStep", new String[] { "1", "3", "4", "5", "6" });
        tester.assertOptionValuesNotEqual("destinationStep", new String[] { "1", "3", "4", "5", "6", "-1" });
        tester.assertTextNotPresent("The originating step");
    }

    public void testTransitionSingleIssue() throws Exception
    {
        boolean isOracle = new EnvironmentUtils(tester, getEnvironmentData(), navigation).isOracle();

        navigation.issue().viewIssue("HSP-1");
        tester.assertTextPresent("Details");
        tester.assertTextPresent("Test1");
        //the issue is currently 'Open'
        text.assertTextPresent(new IdLocator(tester, "status-val"), "Open");
        tester.clickLink("action_id_2");
        tester.setWorkingForm("issue-workflow-transition");
        tester.assertTextPresent("Close Issue");
        //now lets close the issue.  The issue's status should not be changed!
        tester.submit("Transition");

        //check the status is still open
        text.assertTextPresent(new IdLocator(tester, "status-val"), "Open");
        //also check that an appropriate entry has been made in the change history.
        assertions.assertLastChangeHistoryRecords("HSP-1",
                new ExpectedChangeHistoryRecord(
                        EasyList.build(
                                new ExpectedChangeHistoryItem("Resolution", null, "Fixed"),
                                new ExpectedChangeHistoryItem("Status", "Open", "Open"))));

        if(isOracle) {
            //TODO: Remove once http://jira.atlassian.com/browse/JRA-20274 has been resolved
            Thread.sleep(2000);
        }

        //now lets do some other transition.
        tester.clickLink("action_id_4");
        text.assertTextPresent(new IdLocator(tester, "status-val"), "In Progress");
        assertions.assertLastChangeHistoryRecords("HSP-1",
                new ExpectedChangeHistoryRecord(
                        EasyList.build(
                                new ExpectedChangeHistoryItem("Resolution", "Fixed", null),
                                new ExpectedChangeHistoryItem("Status", "Open", "In Progress"))));

        if(isOracle) {
            //TODO: Remove once http://jira.atlassian.com/browse/JRA-20274 has been resolved
            Thread.sleep(2000);
        }
        
        //just for (in)sanity let's do another 'Close' transition
        tester.clickLink("action_id_2");
        tester.setWorkingForm("issue-workflow-transition");
        tester.assertTextPresent("Close Issue");
        //now lets close the issue.  The issue's status should not be changed!
        tester.submit("Transition");

        //check the status is still open
        text.assertTextPresent(new IdLocator(tester, "status-val"), "In Progress");
        //also check that an appropriate entry has been made in the change history.
        assertions.assertLastChangeHistoryRecords("HSP-1",
                new ExpectedChangeHistoryRecord(
                        EasyList.build(
                                new ExpectedChangeHistoryItem("Resolution", null, "Fixed"),
                                new ExpectedChangeHistoryItem("Status", "In Progress", "In Progress"))));
    }

    public void testBulkTransition()
    {
        //assert some pre-conditions
        navigation.issue().viewIssue("HSP-3");
        text.assertTextPresent(new IdLocator(tester, "status-val"), "In Progress");
        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(new IdLocator(tester, "status-val"), "Open");
        navigation.issue().viewIssue("HSP-2");
        text.assertTextPresent(new IdLocator(tester, "status-val"), "Open");

        navigation.issueNavigator().displayAllIssues();

        //lets select two issues with different originating statuses.  This should result in two separate
        //rows for the Close transition.  Selecting HSP-2 & HSP-3
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10002", "on");
        tester.checkCheckbox("bulkedit_10001", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.submit("Next");

        tester.assertTextPresent("Workflow: workflow with originating_step transition");
        WebTable workflowTable = new TableLocator(tester, "workflow_0").getTable();
        assertions.getTableAssertions().assertTableRowEquals(workflowTable, 0, new Object[] {
                new TextCell("Available Workflow Actions"),
                new TextCell("Status Transition"), null, null,
                new TextCell("Affected Issues") });
        final Object[] closeInProgressHsp3 = {
                new TextCell("Close Issue"),
                new TextCell("In Progress"), null, new TextCell("In Progress"),
                new TextCell("HSP-3") };
        assertions.getTableAssertions().assertTableContainsRowOnce(workflowTable, closeInProgressHsp3);
        final Object[] closeOpenHsp2 = {
                new TextCell("Close Issue"),
                new TextCell("Open"), null, new TextCell("Open"),
                new TextCell("HSP-2") };
        assertions.getTableAssertions().assertTableContainsRowOnce(workflowTable, closeOpenHsp2);
        assertions.getTableAssertions().assertTableRowEquals(workflowTable, 3, new Object[] {
                new TextCell("Stop Progress"),
                null, null, new TextCell("Open"), //unfortunately the originating status is currently a bit random so can't assert it here
                new TextCell("HSP-3") });
        assertions.getTableAssertions().assertTableRowEquals(workflowTable, 4, new Object[] {
                new TextCell("Start Progress"),
                null, null, new TextCell("In Progress"),//unfortunately the originating status is currently a bit random so can't assert it here
                new TextCell("HSP-2") });
        assertions.getTableAssertions().assertTableRowEquals(workflowTable, 5, new Object[] {
                new TextCell("Resolve Issue"),
                null, null, new TextCell("Resolved"),//unfortunately the originating status is currently a bit random so can't assert it here
                new TextCell("HSP-2", "HSP-3") });

        tester.checkCheckbox("wftransition", "workflow with originating_step transition_2_3");
        tester.submit("Next");
        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, new String[] { "Workflow", "workflow with originating_step transition" });
        text.assertTextSequence(locator, new String[] { "Selected Transition", "Close Issue" });
        text.assertTextSequence(locator, new String[] { "Status Transition", "In Progress", "In Progress" });
        text.assertTextSequence(locator, new String[] { "This change will affect", "1", "issues." });

        tester.selectOption("resolution", "Fixed");
        tester.submit("Next");

        //assert some things on the confirmation screen.
        locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, new String[] { "Workflow", "workflow with originating_step transition" });
        text.assertTextSequence(locator, new String[] { "Selected Transition", "Close Issue" });
        text.assertTextSequence(locator, new String[] { "Status Transition", "In Progress", "In Progress" });
        text.assertTextSequence(locator, new String[] { "This change will affect", "1", "issues." });
        text.assertTextSequence(new TableLocator(tester, "updatedfields"), new String[] { "Resolution", "Fixed" });
        text.assertTextPresent("HSP-3");
        text.assertTextNotPresent("HSP-1");
        text.assertTextNotPresent("HSP-2");

        tester.submit("Next");

        navigation.issue().viewIssue("HSP-3");
        text.assertTextPresent(new IdLocator(tester, "status-val"), "In Progress");
        //also check that an appropriate entry has been made in the change history.
        assertions.assertLastChangeHistoryRecords("HSP-3",
                new ExpectedChangeHistoryRecord(
                        EasyList.build(
                                new ExpectedChangeHistoryItem("Resolution", null, "Fixed"),
                                new ExpectedChangeHistoryItem("Status", "In Progress", "In Progress"))));

        //now lets try to bulk edit two issues with the same original status.  Should result in a single line. Select
        //HSP-1 & HSP-2
        navigation.issueNavigator().displayAllIssues();
        navigation.issueNavigator().bulkEditAllIssues();
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.checkCheckbox("bulkedit_10001", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.submit("Next");

        tester.assertTextPresent("Workflow: workflow with originating_step transition");
        workflowTable = new TableLocator(tester, "workflow_0").getTable();
        assertions.getTableAssertions().assertTableRowEquals(workflowTable, 0, new Object[] {
                new TextCell("Available Workflow Actions"),
                new TextCell("Status Transition"), null, null,
                new TextCell("Affected Issues") });
        assertions.getTableAssertions().assertTableRowEquals(workflowTable, 1, new Object[] {
                new TextCell("Close Issue"),
                new TextCell("Open"), null, new TextCell("Open"),
                new TextCell("HSP-1", "HSP-2") });
        assertions.getTableAssertions().assertTableRowEquals(workflowTable, 2, new Object[] {
                new TextCell("Start Progress"),
                null, null, new TextCell("In Progress"),//unfortunately the originating status is currently a bit random so can't assert it here
                new TextCell("HSP-1", "HSP-2") });
        assertions.getTableAssertions().assertTableRowEquals(workflowTable, 3, new Object[] {
                new TextCell("Resolve Issue"),
                null, null, new TextCell("Resolved"),//unfortunately the originating status is currently a bit random so can't assert it here
                new TextCell("HSP-1", "HSP-2") });

        tester.checkCheckbox("wftransition", "workflow with originating_step transition_2_1");
        tester.submit("Next");

        locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, new String[] { "Workflow", "workflow with originating_step transition" });
        text.assertTextSequence(locator, new String[] { "Selected Transition", "Close Issue" });
        text.assertTextSequence(locator, new String[] { "Status Transition", "Open", "Open" });
        text.assertTextSequence(locator, new String[] { "This change will affect", "2", "issues." });

        tester.selectOption("resolution", "Fixed");
        tester.submit("Next");

        //assert some things on the confirmation screen.
        locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, new String[] { "Workflow", "workflow with originating_step transition" });
        text.assertTextSequence(locator, new String[] { "Selected Transition", "Close Issue" });
        text.assertTextSequence(locator, new String[] { "Status Transition", "Open", "Open" });
        text.assertTextSequence(locator, new String[] { "This change will affect", "2", "issues." });
        text.assertTextSequence(new TableLocator(tester, "updatedfields"), new String[] { "Resolution", "Fixed" });
        text.assertTextNotPresent("HSP-3");
        text.assertTextPresent("HSP-1");
        text.assertTextPresent("HSP-2");
        tester.submit("Next");

        //now lets check the change history items for HSP-1 & HSP-2
        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(new IdLocator(tester, "status-val"), "Open");
        //also check that an appropriate entry has been made in the change history.
        assertions.assertLastChangeHistoryRecords("HSP-1",
                new ExpectedChangeHistoryRecord(
                        EasyList.build(
                                new ExpectedChangeHistoryItem("Resolution", null, "Fixed"),
                                new ExpectedChangeHistoryItem("Status", "Open", "Open"))));
        navigation.issue().viewIssue("HSP-2");
        text.assertTextPresent(new IdLocator(tester, "status-val"), "Open");
        //also check that an appropriate entry has been made in the change history.
        assertions.assertLastChangeHistoryRecords("HSP-2",
                new ExpectedChangeHistoryRecord(
                        EasyList.build(
                                new ExpectedChangeHistoryItem("Resolution", null, "Fixed"),
                                new ExpectedChangeHistoryItem("Status", "Open", "Open"))));
    }

    private class WorkflowBrowser
    {
        public List<String> getOrigins()
        {
            return getItemsAsList("#orig_steps li");
        }

        public boolean isInitial()
        {
            return css("#orig_steps .workflow-transition-initial").exists();
        }

        private CssLocator css(final String cssSelector)
        {
            return new CssLocator(getTester(), cssSelector);
        }

        public String getCurrent()
        {
            return Iterables.getOnlyElement(getItemsAsList(".workflow-current-context li"));
        }

        public List<String> getDestinations()
        {
            return getItemsAsList("#dest_steps li");
        }

        private List<String> getItemsAsList(final String cssSelector)
        {
            CssLocator cssLocator = css(cssSelector);
            List<String> result = Lists.newArrayList();
            for (Node node : cssLocator.getNodes())
            {
                String s = StringUtils.stripToNull(cssLocator.getText(node));
                if (s != null)
                {
                    result.add(s);
                }
            }
            return result;
        }
    }
}
