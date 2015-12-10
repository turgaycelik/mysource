package com.atlassian.jira.webtests.table;

import com.atlassian.core.util.collection.EasyList;
import com.meterware.httpunit.WebTable;

import java.util.Collection;

/**
 * A composite {@link SimpleCell} that
 * OR's all the simple cells together.
 *
 * @since v3.12
 */
public class OrCell extends AbstractSimpleCell
{
    private final Collection simpleCells;

    public OrCell(Collection simpleCells)
    {
        this.simpleCells = simpleCells;
    }

    public OrCell(SimpleCell cell1, SimpleCell cell2)
    {
        this.simpleCells = EasyList.build(cell1, cell2);
    }

    public OrCell(SimpleCell cell1, SimpleCell cell2, SimpleCell cell3)
    {
        this.simpleCells = EasyList.build(cell1, cell2, cell3);
    }

    /**
     * OR's all the simple cells together for the specified table cell
     * @param table table to compare
     * @param row row index of table
     * @param col col index of table
     * @return true if ANY of the simpleCells evaluate true for the given table cell
     */
    public boolean equals(WebTable table, int row, int col)
    {
        for (final Object simpleCell1 : simpleCells)
        {
            SimpleCell simpleCell = (SimpleCell) simpleCell1;
            //true if any single cell is true
            if (simpleCell.equals(table, row, col))
            {
                return true;
            }
        }
        //false if none of the simple cells are true
        return false;
    }
}
