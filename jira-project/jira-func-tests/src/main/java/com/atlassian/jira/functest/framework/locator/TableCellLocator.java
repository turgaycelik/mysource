package com.atlassian.jira.functest.framework.locator;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebTable;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Locator to run test assertions on a single table cell.
 */
public class TableCellLocator extends AbstractLocator
{
    private final int row;
    private final int col;
    private TableCell tableCell;

    public TableCellLocator(WebTester tester, String tableId, int row, int col)
    {
        super(tester);
        if (tableId == null)
        {
            throw new IllegalArgumentException("The tableId must not be null");
        }
        this.row = row;
        this.col = col;

        final WebTable table = new TableLocator(tester, tableId).getTable();
        if (table == null)
        {
            Assert.fail("No table with id '" + tableId + "' could be found!");
        }

        //select either td or ths
        final XPathLocator delegateXpathLocator = new XPathLocator(tester, "//table[@id='" + tableId + "']//tr");
        final Node[] rowNodes = delegateXpathLocator.getNodes();
        final NodeList tdList = rowNodes[row].getChildNodes();
        int colCount = 0;
        for (int i = 0; i < tdList.getLength(); i++)
        {
            final Node tdNode = tdList.item(i);
            if(tdNode != null && ("th".equals(tdNode.getNodeName()) || "td".equals(tdNode.getNodeName())))
            {
                colCount++;
            }

            if(colCount == col + 1)
            {
                nodes = new Node[] {tdNode};
                break;
            }
        }

        if(nodes == null || nodes.length == 0)
        {
            Assert.fail("Table with id '" + tableId + "' does not have a cell at row '" + row + "' and column '" + col + "'");
        }

        tableCell = table.getTableCell(row, col);
    }

    public TableCell getTableCell()
    {
        return tableCell;
    }

    public Node[] getNodes()
    {
        return nodes;
    }

    public int getRow()
    {
        return row;
    }

    public int getCol()
    {
        return col;
    }

    @Override
    public String toString()
    {
        return "TableCellLocator{col=" + col + ", row=" + row + '}';
    }
}
