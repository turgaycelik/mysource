package com.atlassian.jira.webtests.table;

import com.meterware.httpunit.WebTable;

/**
 * An implementation of SimpleCell which will do a strict check for the expected text.
 * <p>
 * Compare this to TextCell which only checks that the String is <em>contained</em> anywhere in the actual text.
 * This stricter test is sometimes required, eg to assert that one of the Cells is empty.
 * </p>
 * <p>
 * The comparison will ignore leading and trailing whitespace in the actual table.
 * </p>
 *
 * @since v3.13
 */
public class StrictTextCell extends AbstractSimpleCell
{
    private final String expectedText;

    public StrictTextCell(String expectedText)
    {
        this.expectedText = expectedText;
    }

    public boolean equals(WebTable table, int row, int col)
    {
        String targetCellText = table.getCellAsText(row, col).trim();
        return targetCellText.equals(expectedText);
    }

    public String toString()
    {
        // this method is called for user feedback when we log assertion failures, so make it something useful.
        return expectedText;
    }
}
