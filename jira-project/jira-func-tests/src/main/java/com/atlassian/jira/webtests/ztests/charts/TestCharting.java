package com.atlassian.jira.webtests.ztests.charts;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.CHARTING })
public class TestCharting extends FuncTestCase
{

    public static final String MAIN_CONTENT_AREA = "#content";
    public static final String PAGE_HEADER = "#content header h1";
    public static final String REPORT_HEADER = ".reportHeading h3";

    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testCreatedVsResolvedReport()
    {
        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:createdvsresolved-report");
        tester.assertTextPresent("Report: Created vs Resolved Issues Report");

        //validation
        tester.submit("Next");
        tester.assertTextPresent("Please specify a project or filter");

        tester.setFormElement("daysprevious", "aaa");
        tester.submit("Next");
        tester.assertTextPresent("You must specify a whole number of days.");

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.setFormElement("daysprevious", "30");
        tester.submit("Next");

        CssLocator locator = new CssLocator(tester, MAIN_CONTENT_AREA);
        text.assertTextPresent(locator, "There are no matching issues to report on.");

        //now lets create an issue and do the report again.
        navigation.issue().createIssue("homosapien", "Bug", "My first bug");

        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:createdvsresolved-report");
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.setFormElement("daysprevious", "30");
        tester.submit("Next");

        locator = new CssLocator(tester, MAIN_CONTENT_AREA);
        text.assertTextPresent(new CssLocator(tester, PAGE_HEADER), "Created vs Resolved Issues Report");
        text.assertTextSequence(new CssLocator(tester, REPORT_HEADER), "homosapien");
        text.assertTextSequence(locator, "This chart shows the number of issues", "created",
                "vs the number of issues", "resolved", "in the last", "30", "days.");
        tester.assertTextPresent("Data Table");

        final TableLocator tableLocator = new TableLocator(tester, "createdvsresolved-report-datatable");
        text.assertTextPresent(tableLocator, "Period");
        text.assertTextPresent(tableLocator, "Created");
        text.assertTextPresent(tableLocator, "Resolved");
    }

    public void testResolutionTimeReport()
    {
        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:resolutiontime-report");
        tester.assertTextPresent("Report: Resolution Time Report");

        //validation
        tester.submit("Next");
        tester.assertTextPresent("Please specify a project or filter");

        tester.setFormElement("daysprevious", "aaa");
        tester.submit("Next");
        tester.assertTextPresent("You must specify a whole number of days.");

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.setFormElement("daysprevious", "30");
        tester.submit("Next");

        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextPresent(locator, "There are no matching issues to report on.");

        //can't actually create some data here since the resolution time will be too short.  The TestChartingData
        // test takes care of the data tests though.
    }

    public void testPieReport()
    {
        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:pie-report");
        tester.assertTextPresent("Report: Pie Chart Report");

        //validation
        tester.submit("Next");
        tester.assertTextPresent("Please specify a project or filter");

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.selectOption("statistictype", "Issue Type");
        tester.submit("Next");

        CssLocator locator = new CssLocator(tester, MAIN_CONTENT_AREA);
        text.assertTextPresent(new CssLocator(tester, PAGE_HEADER), "Pie Chart Report");
        text.assertTextSequence(locator, "Project:", "homosapien", "(Issue Type)");
        text.assertTextPresent(locator, "There are no matching issues to report on.");

        //now lets create some issues
        navigation.issue().createIssue("homosapien", "Bug", "My first bug");
        navigation.issue().createIssue("homosapien", "Improvement", "My first improvement");

        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:pie-report");
        tester.assertTextPresent("Report: Pie Chart Report");

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.selectOption("statistictype", "Issue Type");
        tester.submit("Next");

        locator = new CssLocator(tester, MAIN_CONTENT_AREA);
        text.assertTextSequence(locator, "Pie Chart Report");
        text.assertTextSequence(locator, "Project:", "homosapien", "(Issue Type)");

        tester.assertTextPresent("Data Table");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 0, 1), "Issues");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 0, 2), "%");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 1, 0), "Improvement");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 1, 1), "1");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 1, 2), "50%");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 2, 0), "Bug");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 2, 1), "1");
        text.assertTextPresent(new TableCellLocator(tester, "singlefieldpie-report-datatable", 2, 2), "50%");

        // click on a pie slice, and check that we get to the issue navigator
        tester.clickLinkWithText("Bug: 1 issues (50%)");
        tester.assertTextPresent("My first bug");
    }

    public void testAverageAgeReport()
    {
        final String name = "Average Age Report";
        assertReportValidation("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:averageage-report", name, "There are no matching issues to report on.");

        text.assertTextPresent(new CssLocator(tester, PAGE_HEADER), name);
        text.assertTextSequence(new CssLocator(tester, REPORT_HEADER), "homosapien");

        tester.assertTextPresent("Data Table");
        text.assertTextPresent(new TableCellLocator(tester, "averageage-report-datatable", 0, 0), "Period");
        text.assertTextPresent(new TableCellLocator(tester, "averageage-report-datatable", 0, 1), "Issues Unresolved");
        text.assertTextPresent(new TableCellLocator(tester, "averageage-report-datatable", 0, 2), "Total Age");
        text.assertTextPresent(new TableCellLocator(tester, "averageage-report-datatable", 0, 3), "Avg. Age");
    }

    public void testRecentlyCreatedReport()
    {
        final String name = "Recently Created Issues Report";
        assertReportValidation("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:recentlycreated-report", name, null);

        text.assertTextPresent(new CssLocator(tester, PAGE_HEADER), name);
        text.assertTextSequence(new CssLocator(tester, REPORT_HEADER), "homosapien");

        tester.assertTextPresent("Data Table");
        text.assertTextPresent(new TableCellLocator(tester, "recentlycreated-report-datatable", 0, 0), "Period");
        text.assertTextPresent(new TableCellLocator(tester, "recentlycreated-report-datatable", 0, 1), "Created Issues (Unresolved)");
        text.assertTextPresent(new TableCellLocator(tester, "recentlycreated-report-datatable", 0, 2), "Created Issues (Resolved)");
    }

    public void testTimeSinceReport()
    {
        final String name = "Time Since Issues Report";
        assertReportValidation("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:timesince-report", name, null);

        text.assertTextPresent(new CssLocator(tester, PAGE_HEADER), name);
        text.assertTextSequence(new CssLocator(tester, REPORT_HEADER), "homosapien");

        tester.assertTextPresent("Data Table");
        text.assertTextPresent(new TableCellLocator(tester, "timesince-report-datatable", 0, 0), "Period");
        text.assertTextPresent(new TableCellLocator(tester, "timesince-report-datatable", 0, 1), "Created");
    }

    private void assertReportValidation(String url, String reportName, String noIssuesMsg)
    {
        tester.gotoPage(url);
        tester.assertTextPresent("Report: " + reportName);

        //validation
        tester.submit("Next");
        tester.assertTextPresent("Please specify a project or filter");

        tester.setFormElement("daysprevious", "aaa");
        tester.submit("Next");
        tester.assertTextPresent("You must specify a whole number of days.");

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.setFormElement("daysprevious", "30");
        tester.submit("Next");

        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextPresent(new CssLocator(tester, PAGE_HEADER), reportName);
        text.assertTextSequence(new CssLocator(tester, REPORT_HEADER), "homosapien");

        if (noIssuesMsg != null)
        {
            text.assertTextPresent(locator, noIssuesMsg);
        }

        //now lets create some issues
        navigation.issue().createIssue("homosapien", "Bug", "My first bug");
        navigation.issue().createIssue("homosapien", "Improvement", "My first improvement");

        tester.gotoPage(url);
        tester.assertTextPresent("Report: " + reportName);

        //now lets add the report
        tester.setFormElement("projectOrFilterId", "project-10000");
        tester.setFormElement("daysprevious", "30");
        tester.submit("Next");
    }
}
