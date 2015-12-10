package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test how the statistics break down when you have fields hidden
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REPORTS })
public class TestSingleLevelGroupByReportIrrelevantIssues extends FuncTestCase
{
    private static final String SELECT_FIELD_CONFIGURATION_NAME = "select cf";

    public void testDifferentFieldConfigurations()
    {
        administration.restoreData("TestSingleLevelGroupByReportIrrelevantIssues.xml");

        runReport();
        assertIssueStatBreakdown(
                "Optoin 1", "MKY-3", "ANA-1",
                "opt1", "ANA-4",
                "None", "MKY-2", "MKY-1", "ANA-3", "ANA-2");

        // hide Select CF in default config and reload dashboard
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields(SELECT_FIELD_CONFIGURATION_NAME);
        administration.reIndex();

        runReport();
        assertIssueStatBreakdown(
                "Optoin 1", "MKY-3",
                "None", "MKY-2", "MKY-1",
                "Irrelevant", "ANA-4", "ANA-3", "ANA-2", "ANA-1");

        // hide Select CF in Copy config too and reload dashboard
        administration.fieldConfigurations().fieldConfiguration("Copy of Default Field Configuration").hideFields(SELECT_FIELD_CONFIGURATION_NAME);
        administration.reIndex();

        runReport();
        assertIssueStatBreakdown(
                "Irrelevant", "MKY-3", "MKY-2", "MKY-1", "ANA-4", "ANA-3", "ANA-2", "ANA-1");

        // show Select CF in default config (still hidden in Copy) and reload dashboard
        administration.fieldConfigurations().defaultFieldConfiguration().showFields(SELECT_FIELD_CONFIGURATION_NAME);
        administration.reIndex();

        runReport();
        assertIssueStatBreakdown(
                "Optoin 1", "ANA-1",
                "opt1", "ANA-4",
                "None", "ANA-3", "ANA-2",
                "Irrelevant", "MKY-3", "MKY-2", "MKY-1");

    }

    private void assertIssueStatBreakdown(String... breakdown)
    {
        // assert the issues broken down into the different groups
        text.assertTextSequence(new IdLocator(tester, "single_groupby_report_table"), breakdown);
    }

    private void runReport()
    {
        tester.gotoPage("/secure/ConfigureReport.jspa?filterid=10010&mapper=customfield_10000&selectedProjectId=10010&reportKey=com.atlassian.jira.plugin.system.reports%3Asinglelevelgroupby&Next=Next");
    }
}
