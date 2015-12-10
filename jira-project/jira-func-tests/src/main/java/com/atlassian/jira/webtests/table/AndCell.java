package com.atlassian.jira.webtests.table;

import com.atlassian.core.util.collection.EasyList;
import com.meterware.httpunit.WebTable;

import java.util.Collection;
import java.util.Iterator;

/**
 * A composite {@link com.atlassian.jira.webtests.table.SimpleCell} that
 * AND's all the simple cells together.
 *
 * @since v3.12
 */
public class AndCell extends AbstractSimpleCell
{
    private final Collection simpleCells;

    public AndCell(Collection simpleCells)
    {
        this.simpleCells = simpleCells;
    }

    public AndCell(SimpleCell cell1, SimpleCell cell2)
    {
        this.simpleCells = EasyList.build(cell1, cell2);
    }

    public AndCell(SimpleCell cell1, SimpleCell cell2, SimpleCell cell3)
    {
        this.simpleCells = EasyList.build(cell1, cell2, cell3);
    }

    public AndCell(SimpleCell cell1, SimpleCell cell2, SimpleCell cell3, SimpleCell cell4)
    {
        this.simpleCells = EasyList.build(cell1, cell2, cell3, cell4);
    }

    public AndCell(SimpleCell cell1, SimpleCell cell2, SimpleCell cell3, SimpleCell cell4, SimpleCell cell5)
    {
        this.simpleCells = EasyList.build(cell1, cell2, cell3, cell4, cell5);
    }

    public AndCell(SimpleCell cell1, SimpleCell cell2, SimpleCell cell3, SimpleCell cell4, SimpleCell cell5, SimpleCell cell6)
    {
        this.simpleCells = EasyList.build(cell1, cell2, cell3, cell4, cell5, cell6);
    }

    /**
     * Need to find which SimpleCell to get the cell as text from, since there could be a mix of different SimpleCell
     * types which want to display different information about the WebTable cell.
     * @param table table to lookup
     * @param row row index of table
     * @param col col index of table
     * @return interesting information about the cell for the cell that is not equal to this cell.
     */
    public String getCellAsText(WebTable table, int row, int col)
    {
        for (final Object simpleCell1 : simpleCells)
        {
            SimpleCell simpleCell = (SimpleCell) simpleCell1;
            //false if any cell is false
            if (!simpleCell.equals(table, row, col))
            {
                return simpleCell.getCellAsText(table, row, col);
            }
        }
        //default to super
        return super.getCellAsText(table, row, col);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder("[AND: ");
        for (Iterator iterator = simpleCells.iterator(); iterator.hasNext();)
        {
            SimpleCell simpleCell = (SimpleCell) iterator.next();
            sb.append(simpleCell.toString());
            if (iterator.hasNext())
            {
                sb.append(" && ");
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * AND's all the simple cells together for the specified table cell
     * @param table table to compare
     * @param row row index of table
     * @param col col index of table
     * @return true if ALL the simpleCells evaluate true for the given table cell
     */
    public boolean equals(WebTable table, int row, int col)
    {
        for (final Object simpleCell1 : simpleCells)
        {
            SimpleCell simpleCell = (SimpleCell) simpleCell1;
            //false if any cell is false
            if (!simpleCell.equals(table, row, col))
            {
//                log("Expected '" + simpleCell + "' but found " + table.getCellAsText(row, col) + " for " + table.getID() + "[" + row + ", " + col + "]");
                return false;
            }
        }
        //true if all simple cells are true
        return true;
    }
}
