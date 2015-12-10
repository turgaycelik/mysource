package com.atlassian.jira.webtests.table;

import com.meterware.httpunit.MockWebTable;
import junit.framework.TestCase;

/**
 * Unit test for StrictTextCell.
 *
 * @since v3.13
 */
public class StrictTextCellTest extends TestCase
{
    public void test()
    {
        StrictTextCell strictTextCell = new StrictTextCell("Hello World");

        MockWebTable table = new MockWebTable();
        table.setCellText(0, 0, "Hello World");
        assertTrue(strictTextCell.equals(table, 0, 0));
        table.setCellText(0, 0, " \t\r\nHello World \t\r\n");
        assertTrue(strictTextCell.equals(table, 0, 0));
        table.setCellText(0, 0, "Hello\tWorld");
        assertFalse(strictTextCell.equals(table, 0, 0));
        // 2 spaces
        table.setCellText(0, 0, "Hello  World");
        assertFalse(strictTextCell.equals(table, 0, 0));
    }

}
