package com.atlassian.jira.webtests.table;

import com.meterware.httpunit.WebTable;

/**
 * This is interface should be implemented for simple conditional checks to be used
 * in {@link com.atlassian.jira.webtests.JIRAWebTest#tableRowEquals(com.meterware.httpunit.WebTable, int, java.util.List)}.
 */
public interface SimpleCell
{
    /**
     * {@link WebTable#getCellAsText(int, int)} returns the text of the cell only. Using this in log statements is often
     * useless because it does not give interesting information. Eg. {@link com.atlassian.jira.webtests.table.ImageCell}
     * will return the cells information about the image rather than the text.
     * @param table table
     * @param row row index (starting from 0)
     * @param col col index (starting from 0)
     * @return String representing the WebTable cell that is interesting to this SimpleCell
     */
    String getCellAsText(WebTable table, int row, int col);

    /**
     * Checks if the cell table[row][col] is equal to this cell
     * @param table table
     * @param row row index (starting from 0)
     * @param col col index (starting from 0)
     * @return true if the referenced cell is equal to this cell, false otherwise
     */
    boolean equals(WebTable table, int row, int col);
}
