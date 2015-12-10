package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.REPORTS })
public class TestPieChartReport extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestPieChartWithCustomField.xml");
    }

    public void testPieChartXSS() throws Exception
    {
        administration.backdoor().usersAndGroups().addUser("testuser", "testuser", "\"><img src=x onerror=alert(1)>", "testuser@example.com");
        navigation.gotoPage("secure/ConfigureReport.jspa?projectOrFilterId=project-10000&statistictype=assignees&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Apie-report&Next=Next");
    }

    public void testPieChartReportWithGroupCustomField()
    {
        navigation.gotoPage("/secure/ConfigureReport.jspa?projectOrFilterId=project-10000&statistictype=customfield_10010&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Apie-report&Next=Next");

        text.assertTextPresent(new WebPageLocator(tester), "Pie Chart Report");
        text.assertTextSequence(new TableLocator(tester, "singlefieldpie-report-datatable"), "None", "4", "100%");
    }

    public void testPieChartReportWithMultiSelectCustomField()
    {
        navigation.gotoPage("/secure/ConfigureReport.jspa?projectOrFilterId=project-10000&statistictype=customfield_10011&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Apie-report&Next=Next");

        text.assertTextPresent(new WebPageLocator(tester), "Pie Chart Report");
        text.assertTextSequence(new TableLocator(tester, "singlefieldpie-report-datatable"), "None", "4", "100%");
    }

    //JRA-19121
    public void testPieChartReportWithProjectCustomField()
    {
        navigation.gotoPage("/secure/ConfigureReport.jspa?projectOrFilterId=project-10000&statistictype=customfield_10012&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Apie-report&Next=Next");

        text.assertTextPresent(new WebPageLocator(tester), "Pie Chart Report");
        text.assertTextSequence(new TableLocator(tester, "singlefieldpie-report-datatable"), "None", "4", "100%");
    }

    public void testPieChartReportWithSelectCustomField()
    {
        navigation.gotoPage("/secure/ConfigureReport.jspa?projectOrFilterId=project-10000&statistictype=customfield_10013&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Apie-report&Next=Next");

        text.assertTextPresent(new WebPageLocator(tester), "Pie Chart Report");
        text.assertTextSequence(new TableLocator(tester, "singlefieldpie-report-datatable"), "None", "4", "100%");
    }

    public void testPieChartReportWithUserCustomField()
    {
        navigation.gotoPage("/secure/ConfigureReport.jspa?projectOrFilterId=project-10000&statistictype=customfield_10014&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Apie-report&Next=Next");

        text.assertTextPresent(new WebPageLocator(tester), "Pie Chart Report");
        text.assertTextSequence(new TableLocator(tester, "singlefieldpie-report-datatable"), "None", "4", "100%");
    }

    //JRA-19118
    public void testPieChartReportWithVersionCustomField()
    {
        navigation.gotoPage("/secure/ConfigureReport.jspa?projectOrFilterId=project-10000&statistictype=customfield_10000&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Apie-report&Next=Next");

        text.assertTextPresent(new WebPageLocator(tester), "Pie Chart Report");
        text.assertTextSequence(new TableLocator(tester, "singlefieldpie-report-datatable"), "None", "4", "100%");
    }
}
