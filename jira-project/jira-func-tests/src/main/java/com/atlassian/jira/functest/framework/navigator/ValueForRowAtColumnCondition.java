package com.atlassian.jira.functest.framework.navigator;

import com.meterware.httpunit.WebTable;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Checks that the value for the given row on the given column is the expected one.
 *
 * @since v4.0
 */
public class ValueForRowAtColumnCondition implements SearchResultsCondition
{
    private final int row;
    private final String columnName;
    private final String expectedValue;
    private final IssueTableHasColumnCondition containsColumn;

    public ValueForRowAtColumnCondition(final int row, final String columnName, final String expectedValue)
    {
        this.row = row;
        this.columnName = columnName;
        this.expectedValue = expectedValue;
        this.containsColumn = new IssueTableHasColumnCondition(columnName);
    }

    @Override
    public void assertCondition(final WebTester tester)
    {
        WebTable table = containsColumn.getIssueTable(tester);
        if (table == null)
        {
            fail("Unable to find issue table");
        }

        Integer columnPosition = getColumnPosition(table);
        if (columnPosition == null)
        {
            fail("Unable to find the column " + columnName + " on the issue table");
        }

        assertThat(textAt(table, row, columnPosition), is(expectedValue));
    }

    private Integer getColumnPosition(final WebTable table)
    {
        for (int i = 0; i < table.getColumnCount(); i++)
        {
            String cellText = textAt(table, 0, i);
            if (columnName.equals(cellText))
            {
                return i;
            }
        }
        return null;
    }

    private String textAt(final WebTable table, final int row, final int column)
    {
        return StringUtils.trimToNull(table.getCellAsText(row, column));
    }
}
