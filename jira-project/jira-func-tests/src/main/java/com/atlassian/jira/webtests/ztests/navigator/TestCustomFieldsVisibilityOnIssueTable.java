package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigator.ValueForRowAtColumnCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.Arrays;

/**
 * Test cases for the visibility of custom fields on the issue table.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestCustomFieldsVisibilityOnIssueTable extends FuncTestCase
{
    private static final String ALL_ISSUES_IN_ALL_PROJECTS_JQL = "";

    public void testOldFieldValuesForCustomFieldsAreNotDisplayedOnIssueTableWhenCustomFieldIsNotVisibleAccordingToTheProjectFieldScheme()
    {
        restoreJiraWithAnIssueWithAValueOnACustomFieldThatItsHiddenAccordingToTheProjectFieldScheme();

        searchForAllIssues();

        assertCustomFieldColumnDoesNotShowOldValueForTheIssue();
    }

    private void restoreJiraWithAnIssueWithAValueOnACustomFieldThatItsHiddenAccordingToTheProjectFieldScheme()
    {
        // The xml describes the same scenario as https://jira.atlassian.com/browse/JRA-39336:
        // Two projects, PRONE and PRTWO
        // One Custom Field initially applying to all issues on all projects
        // An issue for PRONE that has a value for the Custom Field stored on the database
        // The context of the Custom Field changed to apply only to PRTWO
        // The Field Scheme of PRONE changed to a field configuration that hides the Custom Field
        administration.restoreDataAndLogin("CustomFieldHiddenOnProjectByFieldScheme.xml", "admin");
    }

    private void searchForAllIssues()
    {
        navigation.issueNavigator().createSearch(ALL_ISSUES_IN_ALL_PROJECTS_JQL);
    }

    private void assertCustomFieldColumnDoesNotShowOldValueForTheIssue()
    {
        assertions.getIssueNavigatorAssertions().assertSearchResults(Arrays.asList(
                new ValueForRowAtColumnCondition(1, "Custom Field", null)
        ));
    }
}
