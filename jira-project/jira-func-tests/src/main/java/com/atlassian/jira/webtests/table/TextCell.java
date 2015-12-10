package com.atlassian.jira.webtests.table;

import com.meterware.httpunit.WebTable;

/**
 * Checks if the cell has each string in the texts array using indexOf.
 */
public class TextCell extends AbstractSimpleCell
{
    private final String[] texts;

    public TextCell(String text)
    {
        this.texts = new String[] {text};
    }

    public TextCell(String text1, String text2)
    {
        this(new String[] {text1, text2});
    }

    public TextCell(String[] texts)
    {
        this.texts = texts;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder("[texts: ");

        for (int i = 0; i < texts.length; i++)
        {
            sb.append("'").append(texts[i]).append("'");
            if (i < texts.length - 1)
            {
                sb.append(", ");
            }
        }
        sb.append("]");

        return sb.toString();
    }

    //todo - should make it check text in sequence of its occurence
    public boolean equals(WebTable table, int row, int col)
    {
        String targetCell = table.getCellAsText(row, col);
        for (final String text : texts)
        {
            if (targetCell.indexOf(text) == -1)
            {
                return false;
            }
        }
        return true;
    }
}