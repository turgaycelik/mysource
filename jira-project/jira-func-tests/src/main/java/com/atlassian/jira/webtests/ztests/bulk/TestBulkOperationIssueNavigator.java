package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestBulkOperationIssueNavigator extends JIRAWebTest
{
    public TestBulkOperationIssueNavigator(final String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestBulkOperationIssueNavigator.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void tearDown()
    {
        super.tearDown();
    }

    public void testColumns()
    {
        _testUnsavedFilter();
        _testCustomFilterCustomColumns();
        _testCustomFilterStandardColumns();
    }

    public void _testUnsavedFilter()
    {
        navigation.issueNavigator().displayAllIssues();
        assertColumnsInBulkWizard(new Object[] { "T", "Key", "Summary", "Assignee", "Reporter", "Status", "Resolution", "Created", "Updated" });
    }

    public void _testCustomFilterCustomColumns()
    {
        navigation.issueNavigator().loadFilter(10011, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        assertColumnsInBulkWizard(new Object[] {"T", "Key", "Summary"});
    }

    public void _testCustomFilterStandardColumns()
    {
        navigation.issueNavigator().loadFilter(10010, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        assertColumnsInBulkWizard(new Object[] {"T", "Key", "Summary", "Assignee", "Reporter", "Status", "Resolution", "Created", "Updated"});
    }

    private void assertColumnsInBulkWizard(Object[] headerRow)
    {
        // assert columns
        assertTableHasMatchingRow(getWebTableWithID("issuetable"), headerRow);

        navigation.issueNavigator().bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES);
        // assert same columns, prepended with extra column for checkboxes
        List list = new ArrayList(Arrays.asList(headerRow));
        list.add(0, null);
        assertTableHasMatchingRow(getWebTableWithID("issuetable"), list.toArray());

        checkCheckbox("bulkedit_10030", "on");
        submit("Next");

        checkCheckbox("operation", "bulk.edit.operation.name");
        submit("Next");

        checkCheckbox("actions", "comment");
        setFormElement("comment", "Test");
        submit("Next");

        // assert same columns as original
        assertTableHasMatchingRow(getWebTableWithID("issuetable"), headerRow);
    }

}
