package com.atlassian.jira.webtests.table;

import com.meterware.httpunit.WebTable;
import org.apache.commons.lang.StringUtils;

/**
 * Checks if the cell has no text
 */
public class EmptyCell extends AbstractSimpleCell
{

    public String toString()
    {
        return "[Empty Cell]";
    }

    public boolean equals(WebTable table, int row, int col)
    {
        String targetCell = getCellAsText(table, row, col);
        return StringUtils.isBlank(targetCell);
    }
}