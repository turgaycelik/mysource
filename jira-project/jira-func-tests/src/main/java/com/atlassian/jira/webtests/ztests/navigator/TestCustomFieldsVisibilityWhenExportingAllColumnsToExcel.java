package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigator.IssueTableDoesNotHaveColumnCondition;
import com.atlassian.jira.functest.framework.navigator.IssueTableHasColumnCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebResponseUtil;

import java.util.Arrays;

/**
 * Tests how custom fields are handled when using the "Export all columns to Excel" functionality of the issue navigator.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestCustomFieldsVisibilityWhenExportingAllColumnsToExcel extends FuncTestCase
{
    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllIssuesInAllProjectsAndWeSearchForAllIssuesInAllProjects()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInAllProjects();

        String allIssuesInAllProjects = "";
        exportJqlToExcel(allIssuesInAllProjects);

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllIssuesInAllProjectsAndWeSearchForASpecificProject()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInAllProjects();

        exportJqlToExcel("project = 'project 1'");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllIssuesInAllProjectsAndWeSearchForASpecificIssueType()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInAllProjects();

        exportJqlToExcel("issuetype = Bug");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllIssuesInAllProjectsAndWeSearchForASpecificProjectAndIssueType()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInAllProjects();

        exportJqlToExcel("project = 'project 1' AND issuetype = Bug");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllProjectsAndASpecificIssueTypeAndWeSearchForAllIssuesInAllProjects()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInAllProjects();

        String allIssuesInAllProjects = "";
        exportJqlToExcel(allIssuesInAllProjects);

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllProjectsAndASpecificIssueTypeAndWeSearchForAllIssuesInASpecificProject()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInAllProjects();

        exportJqlToExcel("project = 'project 1'");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllProjectsAndASpecificIssueTypeAndWeSearchForAllIssuesOfThatSpecificType()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInAllProjects();

        exportJqlToExcel("issuetype = Bug");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllProjectsAndASpecificIssueTypeAndWeSearchForAllIssuesOfThatSpecificTypeWithinAProject()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInAllProjects();

        exportJqlToExcel("project = 'project 1' AND issuetype = Bug");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsNotExportedToExcelWhenCustomFieldAppliesToAllProjectsAndASpecificIssueTypeAndWeSearchForAllIssuesOfADifferentIssueType()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInAllProjects();

        exportJqlToExcel("issuetype = Task");

        assertColumnIsNotExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllIssuesInAProjectAndWeSearchForAllIssuesInAllProjects()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInProject1();

        String allIssuesInAllProjects = "";
        exportJqlToExcel(allIssuesInAllProjects);

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllIssuesInAProjectAndWeSearchForAllIssuesInThatSpecificProject()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInProject1();

        exportJqlToExcel("project = 'project 1'");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllIssuesInAProjectAndWeSearchForIssuesOfASpecificTypeOnAnyProject()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInProject1();

        exportJqlToExcel("issuetype = Bug");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToAllIssuesInAProjectAndWeSearchForIssuesOfASpecificTypeInThatSpecificProject()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInProject1();

        exportJqlToExcel("project = 'project 1' AND issuetype = Bug");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsNotExportedToExcelWhenCustomFieldAppliesToAllIssuesInAProjectAndWeSearchForAllIssuesInADifferentProject()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInProject1();

        exportJqlToExcel("project = 'project 2'");

        assertColumnIsNotExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToIssuesOfASpecificTypeInAProjectAndWeSearchForAllIssuesInAllProjects()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInProject1();

        String allIssuesInAllProjects = "";
        exportJqlToExcel(allIssuesInAllProjects);

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToIssuesOfASpecificTypeInAProjectAndWeSearchForAllIssuesInThatProject()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInProject1();

        exportJqlToExcel("project = 'project 1'");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToIssuesOfASpecificTypeInAProjectAndWeSearchForAllIssuesOfThatType()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInProject1();

        exportJqlToExcel("issuetype = Bug");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsExportedToExcelWhenCustomFieldAppliesToIssuesOfASpecificTypeInAProjectAndWeSearchForAllIssuesOfThatTypeAndInThatProject()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInProject1();

        exportJqlToExcel("project = 'project 1' AND issuetype = Bug");

        assertColumnIsExported("Custom Field");
    }

    public void testCustomFieldIsNotExportedToExcelWhenCustomFieldAppliesToIssuesOfASpecificTypeInAProjectAndWeSearchForAllIssuesInADifferentProject()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInProject1();

        exportJqlToExcel("project = 'project 2'");

        assertColumnIsNotExported("Custom Field");
    }

    public void testCustomFieldIsNotExportedToExcelWhenCustomFieldAppliesToIssuesOfASpecificTypeInAProjectAndWeSearchForAllIssuesWithDifferentType()
    {
        restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInProject1();

        exportJqlToExcel("issuetype = Task");

        assertColumnIsNotExported("Custom Field");
    }

    public void testCustomFieldIsNotExportedToExcelWhenCustomFieldDoesNotHaveConfigurationSchemesAndWeSearchForAllIssuesInAllProjects()
    {
        restoreJiraWithOneProjectAndACustomFieldWithNoConfigurationSchemes();

        String allIssuesInAllProjects = "";
        exportJqlToExcel(allIssuesInAllProjects);

        assertColumnIsNotExported("Custom Field");
    }

    private void restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInAllProjects()
    {
        // Scenario:
        // Two projects with keys PRONE and PRTWO
        // PRONE has two issues with keys PRONE-1 (Bug) and PRONE-2 (Task)
        // PRTWO has two issues with keys PRTWO-1 (Bug) and PRTWO-2 (Task)
        // One Custom Field that applies to all issues in all projects
        administration.restoreDataAndLogin("TestExportAllColumnsExcel/CustomFieldAppliesToAllIssuesInAllProjects.xml", "admin");
    }

    private void restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInAllProjects()
    {
        // Scenario:
        // Two projects with keys PRONE and PRTWO
        // PRONE has two issues with keys PRONE-1 (Bug) and PRONE-2 (Task)
        // PRTWO has two issues with keys PRTWO-1 (Bug) and PRTWO-2 (Task)
        // One Custom Field that applies to all issues of type Bug in all projects
        administration.restoreDataAndLogin("TestExportAllColumnsExcel/CustomFieldAppliesToAllBugsInAllProjects.xml", "admin");
    }

    private void restoreJiraWithTwoProjectsAndACustomFieldForAllIssuesInProject1()
    {
        // Scenario:
        // Two projects with keys PRONE and PRTWO
        // PRONE has two issues with keys PRONE-1 (Bug) and PRONE-2 (Task)
        // PRTWO has two issues with keys PRTWO-1 (Bug) and PRTWO-2 (Task)
        // One Custom Field that applies to all issues in project PRONE
        administration.restoreDataAndLogin("TestExportAllColumnsExcel/CustomFieldAppliesToAllIssuesInProject1.xml", "admin");
    }

    private void restoreJiraWithTwoProjectsAndACustomFieldForAllBugsInProject1()
    {
        // Scenario:
        // Two projects with keys PRONE and PRTWO
        // PRONE has two issues with keys PRONE-1 (Bug) and PRONE-2 (Task)
        // PRTWO has two issues with keys PRTWO-1 (Bug) and PRTWO-2 (Task)
        // One Custom Field that applies to all issues of type Bug in project PRONE
        administration.restoreDataAndLogin("TestExportAllColumnsExcel/CustomFieldAppliesToAllBugsInProject1.xml", "admin");
    }

    private void restoreJiraWithOneProjectAndACustomFieldWithNoConfigurationSchemes()
    {
        // Scenario:
        // One project with key PRONE
        // PRONE has one issue with key PRONE-1
        // One Custom Field that has no configuration schemes, so it shouldn't apply to the project
        administration.restoreDataAndLogin("TestExportAllColumnsExcel/CustomFieldHasNoConfigurationSchemes.xml", "admin");
    }

    private void exportJqlToExcel(String jql)
    {
        String filterId = backdoor.filters().createFilter(jql, "filterName");

        tester.gotoPage("/sr/jira.issueviews:searchrequest-excel-all-fields/" + filterId + "/SearchRequest-" + filterId + ".xls?tempMax=1000");

        if (!WebResponseUtil.replaceResponseContentType(tester.getDialog().getResponse(), "text/html"))
        {
            fail("Failed to replace response content type with 'text/html'");
        }
    }

    private void assertColumnIsExported(String columnName)
    {
        assertions.getIssueNavigatorAssertions().assertSearchResults(Arrays.asList(new IssueTableHasColumnCondition(columnName)));
    }

    private void assertColumnIsNotExported(final String columnName)
    {
        assertions.getIssueNavigatorAssertions().assertSearchResults(Arrays.asList(new IssueTableDoesNotHaveColumnCondition(columnName)));
    }
}
