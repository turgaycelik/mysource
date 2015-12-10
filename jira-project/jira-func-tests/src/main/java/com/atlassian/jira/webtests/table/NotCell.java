package com.atlassian.jira.webtests.table;

import com.meterware.httpunit.WebTable;

/**
 * Negate a cell. Eg. NotCell(TextCell) will check that the text is not present in cell.
 *
 * @since v3.12
 */
public class NotCell extends AbstractSimpleCell
{
    private final SimpleCell cell;

    public NotCell(SimpleCell cell)
    {
        this.cell = cell;
    }

    public String toString()
    {
        return "[Not: " + cell + "]";
    }

    /**
     * NOT's the simple cell
     * @param table table to compare
     * @param row row index of table
     * @param col col index of table
     * @return negation of the cell
     */
    public boolean equals(WebTable table, int row, int col)
    {
        return !cell.equals(table, row, col);
    }
}
