package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.util.text.TextKit;
import com.atlassian.jira.webtests.table.SimpleCell;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.WebTable;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Used to make assertions about web Tables.
 *
 * @since v3.13.2
 */
public class TableAssertions extends AbstractFuncTestUtil
{
    private static ContainsChecker containsChecker = new ContainsChecker();
    private static EqualsChecker equalsChecker = new EqualsChecker();
    private static StrictWhitespaceContainsChecker strictWhitespaceContainsChecker = new StrictWhitespaceContainsChecker();

    public TableAssertions(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    /**
     * Gets a WebTable by summury or ID.
     *
     * @param tableSummaryOrId Table ID (or summary - whatever that is)
     * @return a WebTable by summury or ID.
     *
     * @see net.sourceforge.jwebunit.HttpUnitDialog#getWebTableBySummaryOrId(String)
     */
    public WebTable getWebTable(String tableSummaryOrId)
    {
        return tester.getDialog().getWebTableBySummaryOrId(tableSummaryOrId);
    }

    /**
     * This method asserts that the given table contains EXACTLY ONE copy of the given row.
     *
     * @param webTable The webTable under tests
     * @param expectedRow the row we expect to find exactly one copy of.
     */
    public void assertTableContainsRowOnce(final WebTable webTable, final Object[] expectedRow)
    {
        assertTableContainsRowOnce(webTable, expectedRow, false);
    }

    /**
     * This method asserts that the given table contains EXACTLY ONE copy of the given row.
     *
     * @param webTable The webTable under tests
     * @param expectedRow the row we expect to find exactly one copy of.
     * @param exactCellSearching set to true if cell contents should match exactly (equals); false for fuzzily (contains)
     */
    public void assertTableContainsRowOnce(final WebTable webTable, final Object[] expectedRow, boolean exactCellSearching)
    {
        final int numRows = countNumRows(webTable, expectedRow, exactCellSearching);
        if (numRows == 0)
        {
            Assert.fail("Did not find expected row in table. Expected row = " + rowToString(expectedRow));
        }
        if (numRows > 1)
        {
            Assert.fail("Expected to find one copy of the row in table, but found " + numRows + ". Expected row = " + rowToString(expectedRow));
        }
    }

    /**
     * This method asserts that the given table contains AT LEAST ONE copy of the given row.
     * For a more strict test, use {@link #assertTableContainsRowOnce(com.meterware.httpunit.WebTable, Object[])}.
     *
     * @param webTable The webTable under tests
     * @param expectedRow the row we expect to find.
     * @see #assertTableContainsRowOnce
     * @see #assertTableContainsRowCount
     */
    public void assertTableContainsRow(final WebTable webTable, final String[] expectedRow)
    {
        final int numRows = countNumRows(webTable, expectedRow);
        if (numRows == 0)
        {
            Assert.fail("Did not find expected row in table. Expected row = " + rowToString(expectedRow));
        }
    }

    /**
     * Asserts that the given table contains the expected number of copies of the given row. 
     *
     * @param webTable The webTable under tests
     * @param expectedRow the row we expect to find exactly one copy of.
     * @param expectedCount The number of copies of this row we expect to find.
     */
    public void assertTableContainsRowCount(final WebTable webTable, final String[] expectedRow, final int expectedCount)
    {
        final int numRows = countNumRows(webTable, expectedRow);
        if (numRows != expectedCount)
        {
            Assert.fail("Expected " + expectedCount + " row entries in table but found " + numRows +". Expected row = " + rowToString(expectedRow));
        }
    }

    /**
     * Assert that the specified row of the table is equal to the expectedRow. Equality for individual cells is determined
     * by {@link com.atlassian.jira.functest.framework.assertions.TableAssertions.StrictWhitespaceContainsChecker}
     *
     * @param table table to look up the row
     * @param row the row number of the table to compare
     * @param expectedRow the expected row to match
     */
    public void assertTableRowEquals(WebTable table, int row, Object[] expectedRow)
    {
        Assert.assertTrue("Expected row '" + rowToString(expectedRow) + "' does not match '" + getTableRowAsList(table, row) + "' (row '" + row + "' of table '" + table.getID() + "')",
                tableRowEquals(table, row, Arrays.asList(expectedRow), strictWhitespaceContainsChecker));
    }

    /**
     * Assert that the specified row of the table is equal to the expectedRow with whitespace collapsed.
     * Equality for individual cells is determined by {@link EqualsChecker}.
     *
     * @param table table to look up the row
     * @param row the row number of the table to compare
     * @param expectedRow the expected row to match with whitespace collapsed.
     */
    public void assertTableRowEqualsCollapsed(WebTable table, int row, Object[] expectedRow)
    {
        Assert.assertTrue("Expected row '" + rowToString(expectedRow) + "' does not match '" + getTableRowAsList(table, row) + "' (row '" + row + "' of table '" + table.getID() + "')",
                tableRowEquals(table, row, Arrays.asList(expectedRow), equalsChecker));
    }

    /**
     * Asserts that the particular table cell contains the text specified.
     * 
     * @param webTable the web table
     * @param row the row index (zero-based)
     * @param column the column index (zero-based)
     * @param text the text to find
     */
    public void assertTableCellHasText(final WebTable webTable, int row, int column, String text)
    {
        Assert.assertNotNull(webTable);
        boolean hasText = containsChecker.check(webTable, row, column, text);
        String actualText = webTable.getCellAsText(row, column);
        Assert.assertTrue("expected to find [" + text + "], somewhere in [" + actualText + "] but obviously couldn't.", hasText);
    }

    private int countNumRows(final WebTable webTable, final Object[] expectedRow)
    {
        return countNumRows(webTable, expectedRow, false);
    }

    private int countNumRows(final WebTable webTable, final Object[] expectedRow, final boolean exactCellSearching)
    {
        int count = 0;
        for (int i = 0; i < webTable.getRowCount(); i++)
        {
             if (tableRowEquals(webTable, i, Arrays.asList(expectedRow), exactCellSearching))
             {
                 count++;
             }
        }
        return count;
    }

    /**
     * Checks for equality in table rows.
     *
     * @param table the table
     * @param row the row index
     * @param expectedRow the expected row
     * @param strictCellCheck set to true if cell contents should match exactly (equals); false for fuzzily (contains)
     * @return true or false
     * @see #tableRowEquals(com.meterware.httpunit.WebTable, int, java.util.List, com.atlassian.jira.functest.framework.assertions.TableAssertions.TableCellChecker)
     */
    private boolean tableRowEquals(final WebTable table, final int row, final List expectedRow, final boolean strictCellCheck)
    {
        final TableCellChecker checker = strictCellCheck ? equalsChecker : containsChecker;
        return tableRowEquals(table, row, expectedRow, checker);
    }

    /**
     * Checks if the row at the specified row number on the table matches the expectedRow. The rows match if all the
     * corresponding columns match.
     * <p/>
     * A column match is determined as follows, if the column of the expectedRow: <li> is null, it will assume the
     * column is correct (ie. ignored) <li> is {@link com.atlassian.jira.webtests.table.LinkCell} then it will use
     * {@link com.atlassian.jira.webtests.table.LinkCell#tableCellHasLinkThatContains(com.meterware.httpunit.WebTable,int,int,String)}
     * <li> else it will use the {@link TableCellChecker} to determine equality.
     *
     * @param table table to check has the expectedRow on row number row
     * @param row row number of the table to compare to the expectedRow (starts from 0)
     * @param expectedRow the row to compare to the tables row
     * @param tableCellChecker the checker to use for determining cell equality
     * @return true if the row at the specified row number equals to the expectedRow
     */
    private boolean tableRowEquals(final WebTable table, final int row, final List expectedRow, final TableCellChecker tableCellChecker)
    {
        if (expectedRow.isEmpty())
        {
            log("expected row is empty");
            return false;
        }

        int maxCol = table.getColumnCount();
        for (int col = 0; col < expectedRow.size() && col < maxCol; col++)
        {
            Object expectedCell = expectedRow.get(col);
            if (expectedCell == null)
            {
                //if the expected cell is null, assume it's valid and ignore it
            }
            else if (expectedCell instanceof SimpleCell)
            {
                SimpleCell simpleCell = (SimpleCell) expectedCell;
                if (!simpleCell.equals(table, row, col))
                {
                    String cellContent = simpleCell.getCellAsText(table, row, col);
                    log("table '" + table.getID() + "' row '" + row + "' does not match expected row because cell [" + row + ", " + col + "] = [" + cellContent + "] does not match [" + expectedCell.toString() + "]");
                    return false;
                }
            }
            else if (!tableCellChecker.check(table, row, col, expectedCell.toString()))
            {
                String cellContent = table.getCellAsText(row, col).trim();
                log("table '" + table.getID() + "' row '" + row + "' does not match expected row because cell [" + row + ", " + col + "] = [" + cellContent + "] does not match [" + expectedCell.toString() + "]");
                return false;
            }
        }
        return true;
    }

    private String rowToString(final Object[] row)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++)
        {
            if (i > 0)
            {
                sb.append(", ");
            }
            sb.append('"');
            sb.append(row[i]);
            sb.append('"');
        }
        return sb.toString();
    }

    /**
     * Get the specified row from the table as a list of trimmed strings.
     *
     * @param table table to get the row from
     * @param row the row index starting from 0 to extract the row from
     * @return list of trimmed cell values from the table on specified row.
     */
    private List<String> getTableRowAsList(WebTable table, int row)
    {
        List<String> tableRow = new ArrayList<String>();
        int maxCol = table.getColumnCount();
        for (int col = 0; col < maxCol; col++)
        {
            tableRow.add(table.getCellAsText(row, col).trim());
        }
        return tableRow;
    }

    interface TableCellChecker
    {
        boolean check(WebTable table, int row, int col, String text);
    }

    /**
     * Collapses whitespace and checks for equality.
     */
    private static final class EqualsChecker implements TableCellChecker
    {
        public boolean check(final WebTable table, final int row, final int col, final String text)
        {
            return TextKit.equalsCollapseWhiteSpace(text, table.getCellAsText(row, col));
        }
    }

    /**
     * Does not collapse whitespace and also checks for empty strings.
     */
    private static final class StrictWhitespaceContainsChecker implements TableCellChecker
    {
        public boolean check(final WebTable table, final int row, final int col, final String text)
        {
            final String cellContent = table.getCellAsText(row, col);

            if ("".equals(text))
            {
                return "".equals(cellContent.trim());
            }
            else
            {
                return cellContent.contains(text);
            }
        }
    }

    /**
     * Collapses whitespace and checks for contains.
     */
    private static final class ContainsChecker implements TableCellChecker
    {
        public boolean check(final WebTable table, final int row, final int col, final String text)
        {
            return TextKit.containsCollapseWhiteSpace(text, table.getCellAsText(row, col));
        }
    }
}
