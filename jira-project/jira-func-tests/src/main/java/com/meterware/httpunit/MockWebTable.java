package com.meterware.httpunit;

/**
 * Mock WebTable for HTTP Unit.
 *
 * @since v3.13
 */
public class MockWebTable extends WebTable
{
    String[][] cellText;

    public MockWebTable()
    {
        this(10, 10);
    }

    public MockWebTable(int rowSize, int colSize)
    {
        super(null, null, null, null, null, null);
        cellText = new String[rowSize][colSize];
    }

    public void setCellText(int row, int col, String text)
    {
        cellText[row][col] = text;
    }

    public String getCellAsText(int row, int col)
    {
        return cellText[row][col];
    }
}
