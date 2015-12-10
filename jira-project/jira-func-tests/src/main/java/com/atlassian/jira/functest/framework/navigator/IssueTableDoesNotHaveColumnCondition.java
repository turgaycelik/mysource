package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.WebTester;

import static junit.framework.Assert.fail;

/**
 * Checks that the passed column is not visible in the issue navigator.
 *
 * @since v4.0
 */
public class IssueTableDoesNotHaveColumnCondition implements SearchResultsCondition
{
    private final String columnName;
    private final IssueTableHasColumnCondition containsColumn;

    public IssueTableDoesNotHaveColumnCondition(final String columnName)
    {
        this.columnName = columnName;
        this.containsColumn = new IssueTableHasColumnCondition(columnName);
    }

    @Override
    public void assertCondition(final WebTester tester)
    {
        WebTable table = this.containsColumn.getIssueTable(tester);
        if (table == null)
        {
            return;
        }

        if (this.containsColumn.isColumnPresent(table))
        {
            fail("Found column " + columnName + " on issue table, when it was expected to not be visible");
        }
    }
}
