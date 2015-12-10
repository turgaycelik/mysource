package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebTable;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.REPORTS, Category.TIME_TRACKING })
public class TestTimeTrackingReport extends JIRAWebTest
{
    /**
     * Dash character used to represent negative, won't let a line break in between it and the following character.
     * in html we use &#8209;
     */
    private static final char NEG = 8209;

    /**
     * Captial sigma as defined in JiraWebActionSupport.properties: common.concepts.sum
     */
    private static final String SIGMA = "&Sigma;";
    private static final int PID_MONOTREME = 10010;
    private static final int PID_HOMOSAPIEN = 10000;
    private static final Long PROJECT_MONKEY_ID = (long)10001;

    private static class Urls
    {
        private static final String TIMETRACKING_REPORT_DEFAULTS = "/secure/ConfigureReport.jspa?versionId=-1&sortingOrder=least&completedFilter=all&reportKey=com.atlassian.jira.plugin.system.reports%3Atime-tracking&Next=Next";
        private static final String TIMETRACKING_REPORT_CONFIG = "/secure/ConfigureReport!default.jspa?reportKey=com.atlassian.jira.plugin.system.reports:time-tracking";
    }

    public static final String VERSION_NAME_FOUR = "New Version 4 {b}";

    public TestTimeTrackingReport(String name)
    {
        super(name);
    }

    public void testTimeTrackingReportAvailable()
    {
        restoreData("TestTimeTrackingReport.xml");
        //go directly to the report URL without specifying a project id (and nothing in session) should fail
        gotoPage(Urls.TIMETRACKING_REPORT_DEFAULTS);
        assertTextPresent("The selected project does not exist, or you do not have permission to view it.");

        //go directly to the report URL with a specified project id (and nothing in session)
        //it should show the report and add the id to the session
        gotoPage(Urls.TIMETRACKING_REPORT_DEFAULTS + "&selectedProjectId=10000");
        assertTextNotPresent("The selected project does not exist, or you do not have permission to view it.");
        assertTextPresent("Time Tracking Report for&nbsp;" + PROJECT_HOMOSAP);
        //browse to the report (project id should have been set in the session already)
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + 10000 + "&reportKey=com.atlassian.jira.plugin.system.reports:time-tracking");
        submit("Next");
        assertTextPresent("Time Tracking Report for&nbsp;" + PROJECT_HOMOSAP);

        //go directly to a report with no project id (so it retrieves from session)
        gotoPage(Urls.TIMETRACKING_REPORT_DEFAULTS);
        assertTextPresent("Time Tracking Report for&nbsp;" + PROJECT_HOMOSAP);
    }

    public void testTimeTrackingReportSwitchingBetweenProjects()
    {
        restoreData("TestTimeTrackingReport.xml");
        gotoPage(Urls.TIMETRACKING_REPORT_DEFAULTS + "&selectedProjectId=10000");
        assertTextPresent("Time Tracking Report for&nbsp;" + PROJECT_HOMOSAP);

        //go directly to a report for another project
        gotoPage(Urls.TIMETRACKING_REPORT_DEFAULTS + "&selectedProjectId=10001");
        assertTextPresent("Time Tracking Report for&nbsp;" + PROJECT_MONKEY);
    }

    public void testTimeTrackingReportShowsSubTaskSelector()
    {
        restoreData("TestTimeTrackingReport.xml");
        gotoPage(Urls.TIMETRACKING_REPORT_DEFAULTS + "&selectedProjectId=10000");
        assertTextNotPresent("Sub-task Inclusion");

        activateSubTasks();
        gotoReportConfig(PID_HOMOSAPIEN);
        assertTextPresent("Sub-task Inclusion");
        selectOption("subtaskInclusion", "Only including sub-tasks with the selected version");
        selectOption("subtaskInclusion", "Also including sub-tasks without a version set");
        selectOption("subtaskInclusion", "Including all sub-tasks");
        assertOptionValueNotPresent("subtaskInclusion", "Sub-tasks are not enabled");
    }

    public void testSubTaskAggregatesWithNoSubtasksDisplayed() throws SAXException
    {
        restoreData("TestTimeTrackingReport.xml");

        activateSubTasks();
        gotoReportConfig(PID_HOMOSAPIEN);
        // defaults
        selectMultiOptionByValue("subtaskInclusion", "all");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "all");
        submit("Next");
        assertTextPresent("Including all sub-tasks");
        assertTextPresent(SIGMA);

        assertTableCellHasText("bars-summary", 0, 0, "Progress: 39%");
        assertTableCellHasText("bars-summary", 1, 0, "Accuracy: 0%");

        WebTable table = getDialog().getResponse().getTableWithID("timeReport");
        List rowList = getTableRowAsList(table, 1);
        String[] row = (String[]) rowList.toArray(new String[rowList.size()]);

        assertTrue(row[3].indexOf("massive bug") != -1);
        assertEquals("1w 3d", row[4]);
        assertEquals("1w 3d", row[5]);
        assertEquals("5d 19h 30m", row[6]);
        assertEquals("5d 19h 30m", row[7]);
        assertEquals("4d 4h 30m", row[8]);
        assertEquals("4d 4h 30m", row[9]);
        assertEquals("on track", row[10]);
        assertEquals("on track", row[11]);

        rowList = getTableRowAsList(table, 2);
        row = (String[]) rowList.toArray(new String[rowList.size()]);
        assertTrue(row[3].indexOf("bug2") != -1);
        assertEquals("1d", row[4]);
        assertEquals("1d", row[5]);
        assertEquals("20h 30m", row[6]);
        assertEquals("20h 30m", row[7]);
        assertEquals("3h 30m", row[8]);
        assertEquals("3h 30m", row[9]);
        assertEquals("on track", row[10]);
        assertEquals("on track", row[11]);

        // TOTALS ROW
        rowList = getTableRowAsList(table, 3);
        row = (String[]) rowList.toArray(new String[rowList.size()]);
        assertEquals("Total", row[3]);
        assertEquals("1w 4d", row[4]);
        assertEquals("6d 16h", row[6]);
        assertEquals("4d 8h", row[8]);
        assertEquals("on track", row[10]);
    }

    public void testSubTaskAggregates() throws SAXException
    {
        restoreData("TestTimeTrackingReportWithSubtasksEnterprise.xml");

        gotoReportConfig(PID_MONOTREME);
        selectMultiOptionByValue("versionId", "10010"); // v8
        selectMultiOptionByValue("subtaskInclusion", "all");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "all");
        submit("Next");

        assertTextPresent("Time Tracking Report for&nbsp;Monotreme");
        assertTextPresent("(v8)");
        assertTextPresent("Including all sub-tasks");

        WebTable table = getDialog().getResponse().getTableWithID("timeReport");
        List rowList = getTableRowAsList(table, 1);
        String[] row = (String[]) rowList.toArray(new String[rowList.size()]);
        assertTrue(row[3].indexOf("monotreme parent with fixfor v8") != -1);
        assertEquals("-", row[4]);
        assertEquals("7h 10m", row[5]);
        assertEquals("-", row[6]);
        assertEquals("6h 10m", row[7]);
        assertEquals("-", row[8]);
        assertEquals("5h", row[9]);
        assertEquals("-", row[10]);
        assertEquals(NEG + "4h", row[11]);

        rowList = getTableRowAsList(table, 2);
        row = (String[]) rowList.toArray(new String[rowList.size()]);
        assertTrue(row[3].indexOf("subtask with no fixfor and parent fixfor v8") != -1);
        assertEquals("6h", row[4]);
        assertEquals("-", row[5]);
        assertEquals("6h", row[6]);
        assertEquals("-", row[7]);
        assertEquals("4h", row[8]);
        assertEquals("-", row[9]);
        assertEquals(NEG + "4h", row[10]);
        assertEquals("-", row[11]);

        rowList = getTableRowAsList(table, 3);
        row = (String[]) rowList.toArray(new String[rowList.size()]);
        assertTrue(row[3].indexOf("subtask with fixfor v8 same as parent") != -1);
        assertEquals("10m", row[4]);
        assertEquals("-", row[5]);
        assertEquals("10m", row[6]);
        assertEquals("-", row[7]);
        assertEquals("-", row[8]);
        assertEquals("-", row[9]);
        assertEquals("on track", row[10]);
        assertEquals("-", row[11]);

        rowList = getTableRowAsList(table, 4);
        row = (String[]) rowList.toArray(new String[rowList.size()]);
        assertTrue(row[3].indexOf("subtask complete same fixfor as parent") != -1);
        assertEquals("1h", row[4]);
        assertEquals("-", row[5]);
        assertEquals("0m", row[6]);
        assertEquals("-", row[7]);
        assertEquals("1h", row[8]);
        assertEquals("-", row[9]);
        assertEquals("on track", row[10]);
        assertEquals("-", row[11]);

        //orphan row
        rowList = getTableRowAsList(table, 5);
        row = (String[]) rowList.toArray(new String[rowList.size()]);
        assertTrue(row[3].indexOf("MON-5") != -1); //  unshown parent key
        assertTrue(row[3].indexOf("v12 subtask with v8 fixfor") != -1);
        assertEquals("44m", row[4]);
        assertEquals("44m", row[5]);
        assertEquals("23m", row[6]);
        assertEquals("23m", row[7]);
        assertEquals("21m", row[8]);
        assertEquals("21m", row[9]);
        assertEquals("on track", row[10]);
        assertEquals("on track", row[11]);

        // TOTALS ROW
        rowList = getTableRowAsList(table, 6);
        row = (String[]) rowList.toArray(new String[rowList.size()]);
        assertEquals("Total", row[3]);
        assertEquals("7h 54m", row[4]);
        assertEquals("6h 33m", row[6]);
        assertEquals("5h 21m", row[8]);
        assertEquals(NEG + "4h", row[10]);

        assertSummaryPercentages(44, -50);
        assertTableCellHasText("bars-summary", 0, 2, "5h 21m completed from current total estimate of 11h 54m");
        assertTableCellHasText("bars-summary", 1, 2, "Issues in this version are behind the original estimate of 7h 54m by 4 hours.");
    }

    public void testSubTaskAggregatesVersionSpecific() throws SAXException
    {
        restoreData("TestTimeTrackingReportWithSubtasksEnterprise.xml");

        gotoReportConfig(PID_MONOTREME);

        selectMultiOptionByValue("versionId", "10010"); // v8
        selectMultiOptionByValue("subtaskInclusion", "onlySelected");
        selectMultiOptionByValue("sortingOrder", "most");
        selectMultiOptionByValue("completedFilter", "all");
        submit("Next");

        assertTextPresent("Time Tracking Report for&nbsp;Monotreme");
        assertTextPresent("(v8)");
        assertTextPresent("Only including sub-tasks with the selected version");

        assertTimeReportCell(1, 3, "monotreme parent with fixfor v8");
        assertTimeReportCell(1, 4, "-");
        assertTimeReportCell(1, 5, "1h 10m");
        assertTimeReportCell(1, 6, "-");
        assertTimeReportCell(1, 7, "10m");
        assertTimeReportCell(1, 8, "-");
        assertTimeReportCell(1, 9, "1h");
        assertTimeReportCell(1, 10, "-");
        assertTimeReportCell(1, 11, "on track");

        assertTimeReportCell(2, 3, "subtask complete same fixfor as parent");
        assertTimeReportCell(2, 4, "1h");
        assertTimeReportCell(2, 5, "-");
        assertTimeReportCell(2, 6, "0m");
        assertTimeReportCell(2, 7, "-");
        assertTimeReportCell(2, 8, "1h");
        assertTimeReportCell(2, 9, "-");
        assertTimeReportCell(2, 10, "on track");
        assertTimeReportCell(2, 11, "-");

        assertTimeReportCell(3, 3, "subtask with fixfor v8 same as parent");
        assertTimeReportCell(3, 4, "10m");
        assertTimeReportCell(3, 5, "-");
        assertTimeReportCell(3, 6, "10m");
        assertTimeReportCell(3, 7, "-");
        assertTimeReportCell(3, 8, "-");
        assertTimeReportCell(3, 9, "-");
        assertTimeReportCell(3, 10, "on track");
        assertTimeReportCell(3, 11, "-");

        // orphan row
        assertTimeReportCell(4, 3, "v12 subtask with v8 fixfor");
        assertTimeReportCell(4, 4, "44m");
        assertTimeReportCell(4, 5, "44m");
        assertTimeReportCell(4, 6, "23m");
        assertTimeReportCell(4, 7, "23m");
        assertTimeReportCell(4, 8, "21m");
        assertTimeReportCell(4, 9, "21m");
        assertTimeReportCell(4, 10, "on track");
        assertTimeReportCell(4, 11, "on track");

        assertTimeReportCell(5, 3, "Total");
        assertTimeReportCell(5, 4, "1h 54m");
        assertTimeReportCell(5, 6, "33m");
        assertTimeReportCell(5, 8, "1h 21");
        assertTimeReportCell(5, 10, "on track");

        assertTextNotPresent("subtask with no fixfor and parent fixfor v8");

        assertSummaryPercentages(71, 0);
    }

    /**
     * Tests subtasks that sum with a parent that also has time tracking data.
     *
     * @throws SAXException in the event of malformed html
     */
    public void testSubTaskAggregatesWithVersionAndBlank() throws SAXException
    {
        restoreData("TestTimeTrackingReportWithSubtasksEnterprise.xml");
        assertSubtaskAggregatesWithVersionAndBlank();
    }


    /**
     * Tests subtasks that sum with a parent that also has time tracking data, also, add a
     * huge complete issue to the version which should not affect the report. Make the same
     * assertions as {@link #testSubTaskAggregatesWithVersionAndBlank()}.
     *
     * @throws SAXException in the event of malformed html
     */
    public void testSubTaskAggregatesWithVersionAndNoVersionSubtasksAddingHugeCompleteIssue() throws SAXException
    {
        restoreData("TestTimeTrackingReportWithSubtasksEnterprise.xml");
        getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");

        // now create a huge issue on the version which is complete.
        String hugeIssueKey = addIssue("Monotreme", "MON", "Bug", "huge complete issue", "Critical", null, null, new String[] { "v8" }, ADMIN_FULLNAME, "env", "huge estimates", "20w", null, null);
        activateTimeTracking();
        logWorkOnIssue(hugeIssueKey, "22w");

        assertSubtaskAggregatesWithVersionAndBlank();
    }

    public void testSubTasksBothResolvedAndUnresolvedIncluded() throws Exception
    {
        restoreData("TestTimeTrackingReportSubTasksResolvedAndUnresolved.xml");

        gotoReportConfig(10000);

        selectMultiOptionByValue("versionId", "10000");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "all");
        selectMultiOptionByValue("subtaskInclusion", "all");
        submit("Next");

        // assert all 10 issues are present
        for (int i = 1; i < 11; i++)
        {
            assertTextPresent("HSP-" + i);
        }
    }

    /**
     * Main body of assertions for tests of subtask aggregates for specified and blank fixfor versions on subtasks.
     */
    private void assertSubtaskAggregatesWithVersionAndBlank()
    {
        gotoReportConfig(PID_MONOTREME);

        selectMultiOptionByValue("versionId", "10011"); // v8
        selectMultiOptionByValue("subtaskInclusion", "selectedAndBlank");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "incomplete");
        submit("Next");

        assertTextPresent("Time Tracking Report for&nbsp;Monotreme");
        assertTextPresent("(v12)");
        assertTextPresent("Also including sub-tasks without a version set");

        assertTimeReportCell(1, 3, "v12 issue");
        assertTimeReportCell(1, 4, "1d 6h");
        assertTimeReportCell(1, 5, "1d 10h 2m");
        assertTimeReportCell(1, 6, "1d 6h");
        assertTimeReportCell(1, 7, "1d 10h 1m");
        assertTimeReportCell(1, 8, "-");
        assertTimeReportCell(1, 9, "1h 3m");
        assertTimeReportCell(1, 10, "on track");
        assertTimeReportCell(1, 11, NEG + "1h 2m");

        assertTimeReportCell(2, 3, "subtask v12");
        assertTimeReportCell(2, 4, "4h 1m");
        assertTimeReportCell(2, 5, "-");
        assertTimeReportCell(2, 6, "3h 59m");
        assertTimeReportCell(2, 7, "-");
        assertTimeReportCell(2, 8, "3m");
        assertTimeReportCell(2, 9, "-");
        assertTimeReportCell(2, 10, NEG + "1m");
        assertTimeReportCell(2, 11, "-");

        assertTimeReportCell(3, 3, "no fixfor subtask of a v12 issue");
        assertTimeReportCell(3, 4, "1m");
        assertTimeReportCell(3, 5, "-");
        assertTimeReportCell(3, 6, "2m");
        assertTimeReportCell(3, 7, "-");
        assertTimeReportCell(3, 8, "1h");
        assertTimeReportCell(3, 9, "-");
        assertTimeReportCell(3, 10, NEG + "1h 1m");
        assertTimeReportCell(3, 11, "-");

        assertTimeReportCell(4, 3, "Total");
        assertTimeReportCell(4, 4, "1d 10h 2m");
        assertTimeReportCell(4, 6, "1d 10h 1m");
        assertTimeReportCell(4, 8, "1h 3m");
        assertTimeReportCell(4, 10, NEG + "1h 2m");

        assertSummaryPercentages(2, -3);
        assertTableCellHasText("bars-summary", 0, 2, "1h 3m completed from current total estimate of 1d 11h 4m");
        assertTableCellHasText("bars-summary", 1, 2, "Issues in this version are behind the original estimate of 1d 10h 2m by 1 hour, 2 minutes.");
    }

    public void testPrintFormatInTimeTrackingReport()
    {
        restoreData("TestTimeTrackingReport.xml");
        //check time tracking report of the monkey project
        reconfigureTimetracking(FORMAT_DAYS);
        generateTimeTrackingReport(PROJECT_MONKEY_ID);
        assertTextPresent("<b>2d 16h</b> completed from current total estimate of <b>2d 19h</b>");

        reconfigureTimetracking(FORMAT_HOURS);
        generateTimeTrackingReport(PROJECT_MONKEY_ID);
        assertTextPresent("<b>64h</b> completed from current total estimate of <b>67h</b>");

        reconfigureTimetracking(FORMAT_PRETTY);
        generateTimeTrackingReport(PROJECT_MONKEY_ID);
        assertTextPresent("<b>2d 16h</b> completed from current total estimate of <b>2d 19h</b>");
    }

    public void testPrettyPrintTimeTrackingReports()
    {
        restoreData("TestTimeTrackingReport.xml");
        getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
        
        reconfigureTimetracking(FORMAT_PRETTY);
        generateUserWorkloadReport((long) PID_HOMOSAPIEN, ADMIN_USERNAME);
        assertTextPresentAfterText(PROJECT_HOMOSAP, "1");
        assertTextPresentBeforeText("1", "5 days, 19 hours, 30 minutes");
        assertTextPresentAfterText(PROJECT_MONKEY, "2");
        assertTextPresentBeforeText("2", "3 hours");

        //increase the workload and see if the report is adjusted
        String issueKey1 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, "increase HSP workload by 4 days");
        setOriginalEstimate(issueKey1, "4d");
        assignIssue(issueKey1, "assigning to admin", ADMIN_FULLNAME);

        generateUserWorkloadReport((long) PID_HOMOSAPIEN, ADMIN_USERNAME);
        assertTextPresentAfterText(PROJECT_HOMOSAP, "2"); //there are 2 assigned issues
        assertTextPresentBeforeText("2", "1 week, 2 days, 19 hours, 30 minutes"); //workload is increased by 4days
        assertTextPresentAfterText(PROJECT_MONKEY, "2"); //monkey should be the same
        assertTextPresentBeforeText("2", "3 hours");

        logWorkOnIssueWithComment(issueKey1, "9d", "huge amount of work being done here");//original estimate is 4 days so is can only reduce workload by 4 days
        generateUserWorkloadReport((long) PID_HOMOSAPIEN, ADMIN_USERNAME);
        assertTextPresent("5 days, 19 hours, 30 minutes");
        assertTextPresent("5 days, 22 hours, 30 minutes"); //check total

        reconfigureTimetracking(FORMAT_DAYS);
        generateUserWorkloadReport((long) PID_HOMOSAPIEN, ADMIN_USERNAME);
        assertTextPresent("5d 19.5h");//assert total homposaien workload
        assertTextPresent("3h");//assert total monkey workload
        assertTextPresent("5d 22.5h");//assert total workload

        //increase workload by 10 hours
        String issueKey = addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_BUG, ISSUE_BUG);
        setOriginalEstimate(issueKey, "10d");
        generateUserWorkloadReport(PROJECT_MONKEY_ID, ADMIN_USERNAME);
        assertTextPresent("5d 19.5h");//assert total homosapien workload
        assertTextPresent("10d 3h");//assert total monkey workload
        //this number does not look like it adds up because the dates are rounded but it adds up when looking in pretty format
        assertTextPresent("15d 22.5h");//assert total workload

    }

    public void testPrintHoursTimeTracking()
    {
        restoreData("TestTimeTrackingReport.xml");
        reconfigureTimetracking(FORMAT_HOURS);
        generateUserWorkloadReport((long) PID_HOMOSAPIEN, ADMIN_USERNAME);

        assertTextPresent("139.5h"); //assert homosapien workload
        assertTextPresent("3h"); //asert monkey workload
        assertTextPresent("142.5h"); //assert total workload

        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, ISSUE_TYPE_BUG);
        setOriginalEstimate(issueKey, "2h 30m");
        generateUserWorkloadReport(PROJECT_MONKEY_ID, ADMIN_USERNAME);
        assertTextPresent("145h"); //assert total workload increased to by 2.5h

        issueKey = addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_BUG, ISSUE_TYPE_BUG);
        setOriginalEstimate(issueKey, "5h");
        generateUserWorkloadReport(PROJECT_MONKEY_ID, ADMIN_USERNAME);
        assertTextPresent("150h"); //assert workload increased by 5h

        //log 5 hours of work and see if workload has been decreased
        logWorkOnIssueWithComment(issueKey, "5h", "more work");
        generateUserWorkloadReport(PROJECT_MONKEY_ID, ADMIN_USERNAME);
        assertTextPresent("145h"); //total workload decreased by 5h
    }

    public void testTimeTrackingReport()
    {
        restoreData("TestTimeTrackingReport.xml");
        reconfigureTimetracking(FORMAT_DAYS);
        generateTimeTrackingReport((long)PID_HOMOSAPIEN);

        //check time spent
        assertTextPresent("4d 8h");
        String issuekey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, ISSUE_TYPE_BUG);
        setOriginalEstimate(issuekey, "3d");
        logWorkOnIssueWithComment(issuekey, "3d", "three more days");
        generateTimeTrackingReport((long)PID_HOMOSAPIEN);
        assertTextPresent("7d 8h"); //assert 3 hours more spent on issues

        reconfigureTimetracking(FORMAT_HOURS);
        generateTimeTrackingReport((long)PID_HOMOSAPIEN);
        assertTextPresent("176h"); //time spent: 7.33 days = 176h
        assertTextPresent("160h");  //time remaining

        issuekey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ISSUE_TYPE_BUG, ISSUE_TYPE_BUG);
        setOriginalEstimate(issuekey, "1d"); //24 hours
        generateTimeTrackingReport((long)PID_HOMOSAPIEN);
        assertTextPresent("184h");  //time remaining increased by 24 hours

        logWorkOnIssueWithComment(issuekey, "1d", "24 hours completed");
        generateTimeTrackingReport((long)PID_HOMOSAPIEN);
        assertTextPresent("160h"); //worked on time for 24 hours

        reconfigureTimetracking(FORMAT_PRETTY);
        generateTimeTrackingReport((long)PID_HOMOSAPIEN);
        assertTextPresent("1w 1d 8h"); //assert time spent
        assertTextPresent("6d 16h"); //assert time remaining: 160h / 24 = 6.66667days
    }

    public void testSingleLevelGroupByReport()
    {
        restoreData("TestTimeTrackingReport.xml");
        // this only tests that the following page renders
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:singlelevelgroupby");
        setFormElement("filterid", "10000");
        submit("Next");
        assertTextPresent("Issues {all}");
    }

    public void testVersionIsEncoded()
    {
        restoreData("TestVersionAndComponentsWithHTMLNames.xml");
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:time-tracking");
        try
        {
            selectOption("versionId", "- \"version<input >");
            fail();
        }
        catch (RuntimeException e)
        {
            assertEquals("Unable to find option - \"version<input > for versionId", e.getMessage());
        }
        selectOption("versionId","- &quot;version&lt;input &gt;");
        submit("Next");
        assertTextPresent("&quot;version&lt;input &gt;");
        assertTextNotPresent("\"version<input >");
    }

    public void testTimeTrackingReportNotCacheable()
    {
        restoreData("TestTimeTrackingReport.xml");

        //go directly to the report URL with a specified project id (and nothing in session)
        //it should show the report and add the id to the session
        gotoPage(Urls.TIMETRACKING_REPORT_DEFAULTS + "&selectedProjectId=10000");
        assertResponseCannotBeCached();
    }

    public void testTimeTrackingReportSorting()
    {
        restoreData("TestTimeTrackingReportSorting.xml");
        gotoPage("/browse/AA?selectedTab=com.atlassian.jira.jira-projects-plugin%3Areports-panel");
        tester.clickLinkWithText("Time Tracking Report");
        tester.selectOption("sortingOrder", "Least completed issues first");
        tester.submit("Next");

        assertIssueOrder(new String[] { "AA-11", "AA-9", "AA-8", "AA-7", "AA-6", "AA-5", "AA-4", "AA-3", "AA-2", "AA-1", "AA-10" });

        gotoPage("/browse/AA?selectedTab=com.atlassian.jira.jira-projects-plugin%3Areports-panel");
        tester.clickLinkWithText("Time Tracking Report");
        tester.selectOption("sortingOrder", "Most completed issues first");
        tester.submit("Next");

        assertIssueOrder(new String[] { "AA-10", "AA-1", "AA-2", "AA-3", "AA-4", "AA-5", "AA-6", "AA-7", "AA-8", "AA-9", "AA-11" });

    }

    private void assertIssueOrder(String[] issueKeys)
    {
        XPathLocator xPathLocator = new XPathLocator(tester, "//table[@id='timeReport']/tbody/tr/td[@class='issue-key']/a");
        final Node[] nodes = xPathLocator.getNodes();
        assertEquals(issueKeys.length, nodes.length);
        for (int i = 0; i < nodes.length; i++)
        {
            String issueKey = issueKeys[i];
            assertEquals("The " + i + "th row was expected to be " + issueKey, issueKey, xPathLocator.getText(nodes[i]));
        }
    }


    //-- helpers --------------------------------------------------------------------------------------

    private void gotoReportConfig(int projectId)
    {
        gotoPage(Urls.TIMETRACKING_REPORT_CONFIG + "&selectedProjectId=" + projectId); // monotremes project
    }

    private void assertTimeReportCell(int row, int col, String expectedText)
    {
        assertTableCellHasText("timeReport", row, col, expectedText);
    }

    private void assertSummaryPercentages(int progressPercentage, int accuracyPercentage)
    {
        assertTableCellHasText("bars-summary", 0, 0, "Progress: " + progressPercentage + "%");
        assertTableCellHasText("bars-summary", 1, 0, "Accuracy: " + accuracyPercentage + "%");
    }

    //-- generation of reports
    private void generateTimeTrackingReport(Long projectId)
    {
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + projectId + "&reportKey=com.atlassian.jira.plugin.system.reports:time-tracking");
         selectOption("sortingOrder", "Most completed issues first");
        selectOption("completedFilter", "All");
        selectOption("versionId", "No Fix Version");
        submit("Next");
    }

    private void generateUserWorkloadReport(long projectId, String user)
    {
        gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=" + projectId + "&reportKey=com.atlassian.jira.plugin.system.reports:developer-workload");
        setFormElement("developer", user);
        submit("Next");
    }

    private void setOriginalEstimate(String issueKey, String originalEstimate)
    {
        gotoIssue(issueKey);
        clickLink("edit-issue");
        setFormElement("timetracking", originalEstimate);
        submit("Update");
    }

}
