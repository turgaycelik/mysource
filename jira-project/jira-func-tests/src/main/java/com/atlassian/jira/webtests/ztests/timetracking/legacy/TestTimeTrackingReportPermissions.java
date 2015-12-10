package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;
import junit.framework.Assert;
import org.xml.sax.SAXException;

import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS, Category.REPORTS, Category.TIME_TRACKING })
public class TestTimeTrackingReportPermissions extends FuncTestCase
{
    /**
     * Dash character used to represent negative, won't let a line break in between it and the following character. in
     * html we use &#8209;
     */
    private static final char NON_BREAKING_NEGATIVE = 8209;

    /**
     * Captial sigma as defined in JiraWebActionSupport.properties: common.concepts.sum
     */
    private static final String SIGMA = "&Sigma;";
    private final String fileName = "TestTimeTrackingReportPermissions.xml";

    private static class Urls
    {
        private static final String TIMETRACKING_REPORT_CONFIG = "/secure/ConfigureReport!default.jspa?reportKey=com.atlassian.jira.plugin.system.reports:time-tracking";
    }

    public static final String VERSION_NAME_FOUR = "New Version 4 {b}";

    public void test() throws SAXException
    {
        administration.restoreData(fileName);
        _testAdminNoVersion();
        {
            navigation.logout();
            // this dataset has the username as the password
            navigation.login(BOB_USERNAME, BOB_USERNAME);
            _testBobNoVersion();
            _testBobVersion1();
            _testBobVersion4();
        }
        {
            navigation.logout();
            // this dataset has the username as the password
            navigation.login(FRED_USERNAME, FRED_PASSWORD);
            _testFredNoVersion();
            _testFredVersion1();
            _testFredVersion4();
        }
    }

    private void _testBobVersion1()
    {
        gotoReportConfig(10000);
        // defaults - no version
        selectMultiOptionByValue("versionId", "10000");
        selectMultiOptionByValue("subtaskInclusion", "all");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "all");
        tester.submit("Next");
        tester.assertTextPresent("Including all sub-tasks");
        tester.assertTextPresent(SIGMA);

        assertSummaryPercentages(91, 14);

        Table table = getTable("timeReport");
        table.assertRows(4);
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version One - Bob Security - No Time");
            row.isOrphan();
            row.hasNoTimeTrackingAtAll();
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version One - Bob Security - Estimate 2 Hours");
            row.originalEstimate("2h");
            row.originalEstimateAggregate("2h");
            row.remainingEstimate("2h");
            row.remainingEstimateAggregate("2h");
            row.timeSpent("-");
            row.timeSpentAggregate("-");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub Two - Bob Security - Version One - Overestimate");
            row.isOrphan();
            row.originalEstimate("1d");
            row.originalEstimateAggregate("1d");
            row.isComplete();
            row.isCompleteAggregate();
            row.timeSpent("4h");
            row.timeSpentAggregate("4h");
            row.accuracy("4h");
            row.accuracyAggregate("4h");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version One - Bob Security - EstAct 2 days 2 hours");
            row.isOrphan();
            row.originalEstimate("2d 2h");
            row.originalEstimateAggregate("2d 2h");
            row.isComplete();
            row.isCompleteAggregate();
            row.timeSpent("2d 2h");
            row.timeSpentAggregate("2d 2h");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            // TOTALS ROW
            Row row = table.nextRow();
            row.summary("Total");
            row.originalEstimate("3d 4h");
            row.originalEstimateAggregate("3d 4h");
            row.remainingEstimate("2h");
            row.remainingEstimateAggregate("2h");
            row.timeSpent("2d 6h");
            row.timeSpentAggregate("2d 6h");
            row.accuracy("4h");
            row.accuracyAggregate("4h");
        }
    }

    private void _testBobVersion4() throws SAXException
    {
        gotoReportConfig(10000);
        selectMultiOptionByValue("versionId", "10001");
        selectMultiOptionByValue("subtaskInclusion", "all");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "incomplete");
        tester.submit("Next");
        tester.assertTextPresent("Including all sub-tasks");
        tester.assertTextPresent(SIGMA);

        assertSummaryPercentages(0, 0);

        Table table = getTable("timeReport");
        table.assertRows(4);
        {
            Row row = table.nextRow();
            row.summary("Parent One - Version Four - Bob Security");
            row.isNotOrphan();
            row.hasNoTimeTracking();
            row.originalEstimateAggregate("6h");
            row.remainingEstimateAggregate("6h");
            row.timeSpentAggregate("-");
            row.accuracyAggregate("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version Four - Bob Security - Estimate 3 Hours");
            row.isNotOrphan();
            row.hasNoTimeTrackingAggregate();
            row.originalEstimate("3h");
            row.remainingEstimate("3h");
            row.timeSpent("-");
            row.accuracy("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version One - Bob Security - Estimate 2 Hours");
            row.isNotOrphan();
            row.hasNoTimeTrackingAggregate();
            row.originalEstimate("2h");
            row.remainingEstimate("2h");
            row.timeSpent("-");
            row.accuracy("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Bob Security - Estimate 1 Hour");
            row.isNotOrphan();
            row.hasNoTimeTrackingAggregate();
            row.originalEstimate("1h");
            row.remainingEstimate("1h");
            row.timeSpent("-");
            row.accuracy("on track");
        }
        {
            // TOTALS ROW
            Row row = table.nextRow();
            row.summary("Total");
            row.originalEstimate("6h");
            row.originalEstimateAggregate("6h");
            row.remainingEstimate("6h");
            row.remainingEstimateAggregate("6h");
            row.timeSpent("0m");
            row.timeSpentAggregate("0m");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
    }

    private void _testBobNoVersion() throws SAXException
    {
        gotoReportConfig(10000);
        // defaults - no version
        selectMultiOptionByValue("versionId", "-1");
        selectMultiOptionByValue("subtaskInclusion", "all");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "all");
        tester.submit("Next");
        tester.assertTextPresent("Including all sub-tasks");
        tester.assertTextPresent(SIGMA);

        assertSummaryPercentages(90, 0);

        Table table = getTable("timeReport");
        table.assertRows(4);
        {
            Row row = table.nextRow();
            row.summary("Parent Three - No Version - Bob Security");
            row.isNotOrphan();
            row.hasNoTimeTrackingAtAll();
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Bob Security - No Time");
            row.isOrphan();
            row.hasNoTimeTrackingAtAll();
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Bob Security - Estimate 1 Hour");
            row.isOrphan();
            row.originalEstimate("1h");
            row.originalEstimateAggregate("1h");
            row.remainingEstimate("1h");
            row.remainingEstimateAggregate("1h");
            row.timeSpent("-");
            row.timeSpentAggregate("-");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Bob Security - EstAct 1 day 1 hour");
            row.isOrphan();
            row.originalEstimate("1d 1h");
            row.originalEstimateAggregate("1d 1h");
            row.isComplete();
            row.isCompleteAggregate();
            row.timeSpent("1d 1h");
            row.timeSpentAggregate("1d 1h");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            // TOTALS ROW
            Row row = table.nextRow();
            row.summary("Total");
            row.isNotOrphan();
            row.originalEstimate("1d 2h");
            row.originalEstimateAggregate("1d 2h");
            row.remainingEstimate("1h");
            row.remainingEstimateAggregate("1h");
            row.timeSpent("1d 1h");
            row.timeSpentAggregate("1d 1h");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
    }

    private void _testFredVersion4() throws SAXException
    {
        gotoReportConfig(10000);
        // defaults - no version
        selectMultiOptionByValue("versionId", "10001");
        selectMultiOptionByValue("subtaskInclusion", "all");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "all");
        tester.submit("Next");
        tester.assertTextPresent("Including all sub-tasks");
        tester.assertTextPresent(SIGMA);

        assertSummaryPercentages(83, 0);

        Table table = getTable("timeReport");
        table.assertRows(3);
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version Four - Fred Security - No Time");
            row.isOrphan();
            row.hasNoTimeTrackingAtAll();
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version Four - Fred Security - Estimate Three Days");
            row.isOrphan();
            row.originalEstimate("3d");
            row.originalEstimateAggregate("3d");
            row.remainingEstimate("3d");
            row.remainingEstimateAggregate("3d");
            row.timeSpent("-");
            row.timeSpentAggregate("-");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version Four - Fred Security - EstAct 3 Weeks");
            row.isOrphan();
            row.originalEstimate("3w");
            row.originalEstimateAggregate("3w");
            row.isComplete();
            row.isCompleteAggregate();
            row.timeSpent("3w");
            row.timeSpentAggregate("3w");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            // TOTALS ROW
            Row row = table.nextRow();
            row.summary("Total");
            row.originalEstimate("3w 3d");
            row.originalEstimateAggregate("3w 3d");
            row.remainingEstimate("3d");
            row.remainingEstimateAggregate("3d");
            row.timeSpent("3w");
            row.timeSpentAggregate("3w");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
    }

    private void _testFredVersion1() throws SAXException
    {
        gotoReportConfig(10000);
        // defaults - no version
        selectMultiOptionByValue("versionId", "10000");
        selectMultiOptionByValue("subtaskInclusion", "all");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "all");
        tester.submit("Next");
        tester.assertTextPresent("Including all sub-tasks");
        tester.assertTextPresent(SIGMA);

        assertSummaryPercentages(84, -4);

        Table table = getTable("timeReport");
        table.assertRows(4);
        String minusFour = NON_BREAKING_NEGATIVE + "4h";
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version One - Fred Security - No Time");
            row.isOrphan();
            row.hasNoTimeTrackingAtAll();
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version One - Fred Security - Estimate Two Days");
            row.isOrphan();
            row.originalEstimate("2d");
            row.originalEstimateAggregate("2d");
            row.remainingEstimate("2d");
            row.remainingEstimateAggregate("2d");
            row.timeSpent("-");
            row.timeSpentAggregate("-");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub Two - Fred Security - Version One - Under Estimate");
            row.isOrphan();
            row.originalEstimate("4h");
            row.originalEstimateAggregate("4h");
            row.isComplete();
            row.isCompleteAggregate();
            row.timeSpent("1d");
            row.timeSpentAggregate("1d");
            row.accuracy(minusFour);
            row.accuracyAggregate(minusFour);
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - Version One - Fred Security - EstAct 2 Weeks");
            row.isOrphan();
            row.originalEstimate("2w");
            row.originalEstimateAggregate("2w");
            row.isComplete();
            row.isCompleteAggregate();
            row.timeSpent("2w");
            row.timeSpentAggregate("2w");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            // TOTALS ROW
            Row row = table.nextRow();
            row.summary("Total");
            row.originalEstimate("2w 2d 4h");
            row.originalEstimateAggregate("2w 2d 4h");
            row.remainingEstimate("2d");
            row.remainingEstimateAggregate("2d");
            row.timeSpent("2w 1d");
            row.timeSpentAggregate("2w 1d");
            row.accuracy(minusFour);
            row.accuracyAggregate(minusFour);
        }
    }

    private void _testFredNoVersion() throws SAXException
    {
        gotoReportConfig(10000);
        // defaults - no version
        selectMultiOptionByValue("versionId", "-1");
        selectMultiOptionByValue("subtaskInclusion", "all");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "all");
        tester.submit("Next");
        tester.assertTextPresent("Including all sub-tasks");
        tester.assertTextPresent(SIGMA);

        assertSummaryPercentages(83, 0);

        Table table = getTable("timeReport");
        table.assertRows(3);
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Fred Security - No Time");
            row.isOrphan();
            row.hasNoTimeTrackingAtAll();
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Fred Security - Estimate One Day");
            row.isOrphan();
            row.originalEstimate("1d");
            row.originalEstimateAggregate("1d");
            row.remainingEstimate("1d");
            row.remainingEstimateAggregate("1d");
            row.timeSpent("-");
            row.timeSpentAggregate("-");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Fred Security - EstAct 1 Week");
            row.isOrphan();
            row.originalEstimate("1w");
            row.originalEstimateAggregate("1w");
            row.isComplete();
            row.isCompleteAggregate();
            row.timeSpent("1w");
            row.timeSpentAggregate("1w");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            // TOTALS ROW
            Row row = table.nextRow();
            row.summary("Total");
            row.isNotOrphan();
            row.originalEstimate("1w 1d");
            row.originalEstimateAggregate("1w 1d");
            row.remainingEstimate("1d");
            row.remainingEstimateAggregate("1d");
            row.timeSpent("1w");
            row.timeSpentAggregate("1w");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
    }

    private void _testAdminNoVersion() throws SAXException
    {
        gotoReportConfig(10000);
        // defaults - no version
        selectMultiOptionByValue("versionId", "-1");
        selectMultiOptionByValue("subtaskInclusion", "all");
        selectMultiOptionByValue("sortingOrder", "least");
        selectMultiOptionByValue("completedFilter", "all");
        tester.submit("Next");
        tester.assertTextPresent("Including all sub-tasks");
        tester.assertTextPresent(SIGMA);

        assertSummaryPercentages(84, 0);

        Table table = getTable("timeReport");
        table.assertRows(7);

        {
            Row row = table.nextRow();
            row.summary("Parent Three - No Version - Bob Security");
            row.isNotOrphan();
            row.hasNoTimeTrackingAtAll();
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Bob Security - No Time");
            row.isOrphan();
            row.hasNoTimeTrackingAtAll();
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Fred Security - No Time");
            row.isOrphan();
            row.hasNoTimeTrackingAtAll();
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Fred Security - Estimate One Day");
            row.isOrphan();
            row.originalEstimate("1d");
            row.originalEstimateAggregate("1d");
            row.remainingEstimate("1d");
            row.remainingEstimateAggregate("1d");
            row.timeSpent("-");
            row.timeSpentAggregate("-");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Bob Security - Estimate 1 Hour");
            row.isOrphan();
            row.originalEstimate("1h");
            row.originalEstimateAggregate("1h");
            row.remainingEstimate("1h");
            row.remainingEstimateAggregate("1h");
            row.timeSpent("-");
            row.timeSpentAggregate("-");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Bob Security - EstAct 1 day 1 hour");
            row.isOrphan();
            row.originalEstimate("1d 1h");
            row.originalEstimateAggregate("1d 1h");
            row.isComplete();
            row.isCompleteAggregate();
            row.timeSpent("1d 1h");
            row.timeSpentAggregate("1d 1h");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
        {
            Row row = table.nextRow();
            row.summary("Sub One - No Version - Fred Security - EstAct 1 Week");
            row.isOrphan();
            row.originalEstimate("1w");
            row.originalEstimateAggregate("1w");
            row.isComplete();
            row.isCompleteAggregate();
            row.timeSpent("1w");
            row.timeSpentAggregate("1w");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }

        {
            // TOTALS ROW
            Row row = table.nextRow();
            row.summary("Total");
            row.isNotOrphan();
            row.originalEstimate("1w 2d 2h");
            row.originalEstimateAggregate("1w 2d 2h");
            row.remainingEstimate("1d 1h");
            row.remainingEstimateAggregate("1d 1h");
            row.timeSpent("1w 1d 1h");
            row.timeSpentAggregate("1w 1d 1h");
            row.accuracy("on track");
            row.accuracyAggregate("on track");
        }
    }

    //-- helpers --------------------------------------------------------------------------------------

    private void selectMultiOptionByValue(String fieldId, String value)
    {
        tester.checkCheckbox(fieldId, value);
    }

    private void gotoReportConfig(int projectId)
    {
        navigation.gotoPage(Urls.TIMETRACKING_REPORT_CONFIG + "&selectedProjectId=" + projectId); // monotremes project
    }

    private void assertSummaryPercentages(int progressPercentage, int accuracyPercentage)
    {
        oldway_consider_porting.assertTableCellHasText("bars-summary", 0, 0, "Progress: " + progressPercentage + "%");
        oldway_consider_porting.assertTableCellHasText("bars-summary", 1, 0, "Accuracy: " + accuracyPercentage + "%");
    }

    Table getTable(String id)
    {
        try
        {
            return new Table(tester.getDialog().getResponse().getTableWithID(id));
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Could not get WebTable", e);
        }
    }

    private class Table
    {
        private final WebTable table;
        private int nextRow = 1;

        Table(WebTable table)
        {
            this.table = table;
        }

        Row nextRow()
        {
            return new Row(oldway_consider_porting.getTableRowAsList(table, nextRow++));
        }

        void assertRows(int rows)
        {
            // table has header and total rows
            rows = rows + 2;
            assertEquals(rows, table.getRowCount());
        }
    }

    private class Row extends Assert
    {
        private final String[] row;

        Row(String[] row)
        {
            this.row = row;
        }

        Row(List rowList)
        {
            this((String[]) rowList.toArray(new String[rowList.size()]));
        }

        Row(WebTable table, int rowNum)
        {
            this(oldway_consider_porting.getTableRowAsList(table, rowNum));
        }

        void hasNoTimeTrackingAtAll()
        {
            hasNoTimeTracking();
            hasNoTimeTrackingAggregate();
        }

        void hasNoTimeTracking()
        {
            originalEstimate("-");
            remainingEstimate("-");
            timeSpent("-");
            accuracy("-");
        }

        void hasNoTimeTrackingAggregate()
        {
            originalEstimateAggregate("-");
            remainingEstimateAggregate("-");
            timeSpentAggregate("-");
            accuracyAggregate("-");
        }

        void isOrphan()
        {
            summary("HSP-");
        }

        void isTotalsRow()
        {
            summary("Totals");
        }

        void isNotOrphan()
        {
            assertTrue(row[3], row[3].indexOf("HSP-") == -1);
        }

        void isComplete()
        {
            remainingEstimate("0m");
        }

        void isCompleteAggregate()
        {
            remainingEstimateAggregate("0m");
        }

        void summary(String str)
        {
            assertTrue(row[3], row[3].indexOf(str) != -1);
        }

        void originalEstimate(String str)
        {
            assertEquals(str, row[4]);
        }

        void originalEstimateAggregate(String str)
        {
            assertEquals(str, row[5]);
        }

        void remainingEstimate(String str)
        {
            assertEquals(str, row[6]);
        }

        void remainingEstimateAggregate(String str)
        {
            assertEquals(str, row[7]);
        }

        void timeSpent(String str)
        {
            assertEquals(str, row[8]);
        }

        void timeSpentAggregate(String str)
        {
            assertEquals(str, row[9]);
        }

        void accuracy(String str)
        {
            assertEquals(str, row[10]);
        }

        void accuracyAggregate(String str)
        {
            assertEquals(str, row[11]);
        }
    }
}