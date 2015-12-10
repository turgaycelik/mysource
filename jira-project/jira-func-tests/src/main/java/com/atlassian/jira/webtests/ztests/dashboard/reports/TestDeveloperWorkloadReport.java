package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FuncTestHelperFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.assertions.TableAssertions;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 * Tests DeveloperWorkloadReport (aka User Workload Report on the UI).
 *
 */
@WebTest({ Category.FUNC_TEST, Category.REPORTS })
public class TestDeveloperWorkloadReport extends FuncTestCase
{
    protected Navigation navigation;
    protected TableAssertions tableAssertions;

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestDeveloperWorkloadReport.xml");
        final FuncTestHelperFactory funcTestHelperFactory = new FuncTestHelperFactory(tester, getEnvironmentData());
        navigation = funcTestHelperFactory.getNavigation();
        tableAssertions = funcTestHelperFactory.getAssertions().getTableAssertions();
    }

    public void testDeveloperWorkloadReportBasic()
    {
        navigation.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        tester.setFormElement("developer", ADMIN_USERNAME);
        tester.submit("Next");

        assertBasicTestReport();
    }

    private void assertBasicTestReport()
    {
        tester.assertTextPresent("User Workload Report");
        final WebTable table = getTable();
        tableAssertions.assertTableContainsRow(table, new String[] { "homosapien", "3", "1 week, 1 day, 1 hour" });
        tableAssertions.assertTableContainsRow(table, new String[] { "monkey", "3", "18 minutes" });
        tableAssertions.assertTableContainsRow(table, new String[] { "Total", "6", "1 week, 1 day, 1 hour, 18 minutes" });
    }

    public void testPreSubtaskInclusionUrls()
    {
        // subtasks and unassigned issues make the precondition for the display of subtask inclusion report option
        administration.subtasks().enable();
        administration.generalConfiguration().setAllowUnassignedIssues(true);

        navigation.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        // subtask inclusion options should be present now
        tester.assertTextPresent("Sub-task Inclusion");
        tester.assertTextPresent("Only including sub-tasks assigned to the selected user");
        tester.assertTextPresent("Also including unassigned sub-tasks");

        // use a legacy url which doesn't specify subtask inclusion options
        navigation.gotoPage("/secure/ConfigureReport.jspa?developer=admin&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Adeveloper-workload&Next=Next");

        // check the report is the same as it was
        assertBasicTestReport();
    }

    public void testSubtaskInclusionOnlyAssignee()
    {
        administration.subtasks().enable();
        administration.generalConfiguration().setAllowUnassignedIssues(true);

        // select options like legacy behaviour: only subtasks assigned to selected user are included
        navigation.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        tester.setFormElement("developer", ADMIN_USERNAME);
        tester.selectOption("subtaskInclusion", "Only including sub-tasks assigned to the selected user");
        tester.submit("Next");

        assertBasicTestReport();
    }

    public void testSubtaskInclusionOnlyAssigneeWithSubtasks()
    {
        administration.subtasks().enable();
        administration.generalConfiguration().setAllowUnassignedIssues(true);

        final String subtask = navigation.issue().createSubTask("MKY-1", ISSUE_TYPE_SUB_TASK,
                "curious george is a monkey", "he is always getting into trouble", "2h");
        navigation.issue().logWorkWithComment(subtask, "1h", "work it george"); // burn one hour leaving 1h
        navigation.issue().unassignIssue(subtask, "unassigning"); // TODO test this worked

        navigation.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        tester.setFormElement("developer", ADMIN_USERNAME);
        tester.selectOption("subtaskInclusion", "Only including sub-tasks assigned to the selected user");
        tester.submit("Next");

        assertBasicTestReport();
    }

    public void testSubtaskInclusionOnlyAssigneeWithSubtasksOnIssues()
    {
        administration.subtasks().enable();
        administration.generalConfiguration().setAllowUnassignedIssues(true);

        final String subtask = navigation.issue().createSubTask("MKY-1", ISSUE_TYPE_SUB_TASK,
                "curious george is a monkey", "he is always getting into trouble", "2h");
        navigation.issue().logWork(subtask, "1h"); // burn one hour leaving 1h
        navigation.issue().unassignIssue(subtask, null);

        // the subtask isn't assigned to admin, so it shouldn't show up using the only "assigned option"
        navigation.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        tester.setFormElement("developer", ADMIN_USERNAME);
        tester.selectOption("subtaskInclusion", "Only including sub-tasks assigned to the selected user");
        tester.submit("Next");

        assertBasicTestReport();
    }

    public void testSubtaskInclusionAlsoUnassigned()
    {
        administration.subtasks().enable();
        administration.generalConfiguration().setAllowUnassignedIssues(true);

        final String subtask = navigation.issue().createSubTask("MKY-1", ISSUE_TYPE_SUB_TASK,
                "curious george is a monkey", "he is always getting into trouble", "2h");
        navigation.issue().logWork(subtask, "1h"); // burn one hour leaving 1h
        navigation.issue().unassignIssue(subtask, null);

        navigation.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        tester.setFormElement("developer", ADMIN_USERNAME);
        tester.selectOption("subtaskInclusion", "Also including unassigned sub-tasks");
        tester.submit("Next");

        // now assert we have the extra time from our subtask
        tester.assertTextPresent("User Workload Report");
        final WebTable table = getTable();
        tableAssertions.assertTableContainsRow(table, new String[] { "homosapien", "3", "1 week, 1 day, 1 hour" });
        tableAssertions.assertTableContainsRow(table, new String[] { "monkey", "4", "1 hour, 18 minutes" });
        tableAssertions.assertTableContainsRow(table, new String[] { "Total", "7", "1 week, 1 day, 2 hours, 18 minutes" });
    }

    private WebTable getTable()
    {
        try
        {
            return tester.getDialog().getResponse().getTableWithID("dwreport");
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }


}
