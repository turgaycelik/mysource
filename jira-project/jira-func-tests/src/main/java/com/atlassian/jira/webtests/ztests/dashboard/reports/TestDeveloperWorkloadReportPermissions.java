package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Tests DeveloperWorkloadReport (aka User Workload Report on the UI)
 */
@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS, Category.REPORTS })
public class TestDeveloperWorkloadReportPermissions extends FuncTestCase
{
    private final Report report = new Report();

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestDeveloperWorkloadReportPermissions.xml");
    }

    public void test()
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        {
            _testAdminUserDeveloperWorkloadReportAdminLogin();
            AdminLoginTests tests = new AdminLoginTests();

            tests.testFredUser();
            tests.testFredUserWithSubTasks();
            tests.testBobUser();
            tests.testBobUserWithSubTasks();
        }

        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        {
            _testAdminUserDeveloperWorkloadReportAdminLogin();
            FredLoginTests tests = new FredLoginTests();

            tests.testFredUser();
            tests.testFredUserWithSubTasks();
            tests.testBobUser();
            tests.testBobUserWithSubTasks();
        }

        navigation.login(BOB_USERNAME, BOB_USERNAME);
        {
            _testAdminUserDeveloperWorkloadReportAdminLogin();
            BobLoginTests tests = new BobLoginTests();

            tests.testFredUser();
            tests.testFredUserWithSubTasks();
            tests.testBobUser();
            tests.testBobUserWithSubTasks();
        }
    }

    private void _testAdminUserDeveloperWorkloadReportAdminLogin()
    {
        report.generateWithoutSubtasks(ADMIN_USERNAME);

        tester.assertTextPresent("User Workload Report");
        tester.assertTextPresent("There are no unresolved issues assigned to the specified user");
    }

    class AdminLoginTests
    {
        private void testFredUser()
        {
            report.generateWithoutSubtasks(FRED_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(FRED_FULLNAME);
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "8", "1 week, 4 days, 6 hours");
            assertions.text().assertTextSequence(response, "Total", "8", "1 week, 4 days, 6 hours");
        }

        private void testFredUserWithSubTasks()
        {
            report.generateWithUnassignedSubtasks(FRED_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(FRED_FULLNAME);
            // no unassigned subtasks for fred
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "8", "1 week, 4 days, 6 hours");
            assertions.text().assertTextSequence(response, "Total", "8", "1 week, 4 days, 6 hours");
        }

        private void testBobUser()
        {
            report.generateWithoutSubtasks(BOB_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(BOB_FULLNAME);
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "9", "4 days, 5 hours");
            assertions.text().assertTextSequence(response, "Total", "9", "4 days, 5 hours");
        }

        private void testBobUserWithSubTasks()
        {
            report.generateWithUnassignedSubtasks(BOB_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(BOB_FULLNAME);
            // no unassigned subtasks for fred
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "10", "1 week, 4 days, 1 hour");
            assertions.text().assertTextSequence(response, "Total", "10", "1 week, 4 days, 1 hour");
        }
    }

    class FredLoginTests
    {
        private void testFredUser()
        {
            report.generateWithoutSubtasks(FRED_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(FRED_FULLNAME);
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "8", "1 week, 4 days, 6 hours");
            assertions.text().assertTextSequence(response, "Total", "8", "1 week, 4 days, 6 hours");
        }

        private void testFredUserWithSubTasks()
        {
            report.generateWithUnassignedSubtasks(FRED_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(FRED_FULLNAME);
            final String response = tester.getDialog().getResponseText();
            // no unassigned subtasks for fred
            assertions.text().assertTextSequence(response, "homosapien", "8", "1 week, 4 days, 6 hours");
            assertions.text().assertTextSequence(response, "Total", "8", "1 week, 4 days, 6 hours");
        }

        private void testBobUser()
        {
            report.generateWithoutSubtasks(BOB_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(BOB_FULLNAME);
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "3", "3 days, 7 hours");
            assertions.text().assertTextSequence(response, "Total", "3", "3 days, 7 hours");
        }

        private void testBobUserWithSubTasks()
        {
            report.generateWithUnassignedSubtasks(BOB_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(BOB_FULLNAME);
            // no unassigned subtasks for fred
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "4", "1 week, 3 days, 3 hours");
            assertions.text().assertTextSequence(response, "Total", "4", "1 week, 3 days, 3 hours");
        }
    }

    class BobLoginTests
    {
        private void testFredUser()
        {
            report.generateWithoutSubtasks(FRED_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(FRED_FULLNAME);
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "2", "3 days, 6 hours");
            assertions.text().assertTextSequence(response, "Total", "2", "3 days, 6 hours");
        }

        private void testFredUserWithSubTasks()
        {
            report.generateWithUnassignedSubtasks(FRED_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(FRED_FULLNAME);
            // no unassigned subtasks for fred
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "2", "3 days, 6 hours");
            assertions.text().assertTextSequence(response, "Total", "2", "3 days, 6 hours");
        }

        private void testBobUser()
        {
            report.generateWithoutSubtasks(BOB_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(BOB_FULLNAME);
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "9", "4 days, 5 hours");
            assertions.text().assertTextSequence(response, "Total", "9", "4 days, 5 hours");
        }

        private void testBobUserWithSubTasks()
        {
            report.generateWithUnassignedSubtasks(BOB_USERNAME);

            tester.assertTextPresent("User Workload Report");
            tester.assertTextPresent("Workload report for user");
            tester.assertTextPresent(BOB_FULLNAME);
            // no unassigned subtasks for fred
            final String response = tester.getDialog().getResponseText();
            assertions.text().assertTextSequence(response, "homosapien", "10", "1 week, 4 days, 1 hour");
            assertions.text().assertTextSequence(response, "Total", "10", "1 week, 4 days, 1 hour");
        }
    }

    private class Report
    {
        private static final String REPORT = "secure/ConfigureReport.jspa?reportKey=com.atlassian.jira.plugin.system.reports%3Adeveloper-workload&Next=Next";
        private static final String CONFIG = "/secure/ConfigureReport!default.jspa?reportKey=com.atlassian.jira.plugin.system.reports:developer-workload";

        void configure()
        {
            navigation.gotoPage(CONFIG);
        }

        void generate(String user, String subtaskInclusion)
        {
            navigation.gotoPage(REPORT + "&developer=" + user + "&subtaskInclusion=" + subtaskInclusion);
        }

        void generateWithoutSubtasks(String user)
        {
            generate(user, "onlyAssigned");
        }

        void generateWithUnassignedSubtasks(String user)
        {
            generate(user, "assignedAndUnassigned");
        }
    }
}