package com.atlassian.jira.webtests.table;

import com.atlassian.jira.functest.framework.util.url.URLUtil;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;

/**
 * This is used with the {@link com.atlassian.jira.webtests.JIRAWebTest#tableRowEquals(com.meterware.httpunit.WebTable,int,java.util.List)}
 * to identify which table cells should check for a link and not a text
 */
public class LinkCell extends AbstractSimpleCell
{
    private final String url;
    private final String label;

    public LinkCell(String url, String label)
    {
        this.url = url;
        this.label = label;
    }

    public String toString()
    {
        return "[url: '" + url + "', label: '" + label + "']";
    }


    public String getCellAsText(WebTable table, int row, int col)
    {
        TableCell tableCell = table.getTableCell(row, col);
        WebLink[] links = tableCell.getLinks();
        if (links != null && links.length > 0)
        {
            StringBuilder sb = new StringBuilder();
            for (WebLink webLink : links)
            {
                String urlString = webLink.getURLString();
                sb.append("[link: '").append(urlString).append("']");
            }
            return sb.toString();
        }
        return "No Links was found in " + table.getID() + "[" + row + ", " + col + "]";
    }

    public boolean equals(WebTable table, int row, int col)
    {
        return tableCellHasLinkThatContains(table, row, col, url) && tableCellHasStrictText(table, row, col, label);
    }

    /**
     * Checks if a particular table cell contains the link URL specified.
     *
     * @param table table to compare
     * @param row   row index of table
     * @param col   col index of table
     * @param link  URL
     * @return True if the table cell contains the link URL specified.
     */
    protected boolean tableCellHasLinkThatContains(WebTable table, int row, int col, String link)
    {
        if (link == null)
        {
            return tableCellHasNoLinks(table, row, col);
        }
        else
        {
//            log("Checking cell [" + row + ", " + col + "] for link [" + link + "]");
            TableCell tableCell = table.getTableCell(row, col);
            WebLink[] links = tableCell.getLinks();
            if (links != null && links.length > 0)
            {
                for (WebLink webLink : links)
                {
                    String urlString = webLink.getURLString();
                    if (urlString != null && URLUtil.compareURLStrings(link, urlString))
                    {
                        return true;
                    }
                }
            }
//            log("Expected '" + link + "' but was not found in '" + table.getCellAsText(row, col) + "'");
            return false;
        }
    }

    protected boolean tableCellHasNoLinks(WebTable table, int row, int col)
    {
//        log("Checking cell [" + row + ", " + col + "] for no links");
        TableCell tableCell = table.getTableCell(row, col);
        WebLink[] links = tableCell.getLinks();
        boolean result = links == null || links.length == 0;
        if (!result)
        {
//            log("Links were not expected but were found in '" + table.getTableCell(row, col).asText() + "'");
        }
        return result;
    }

    /**
     * todo - this should really be getting the anchor tag and asserting that the link has the text not the whole cell
     *
     * @param table table to compare
     * @param row   row index of table
     * @param col   col index of table
     * @param text  text to verify
     * @return Returns true if the text is contained in the table cell specified.
     */
    protected boolean tableCellHasStrictText(WebTable table, int row, int col, String text)
    {
//        log("Checking cell [" + row + ", " + col + "] for text [" + text + "]");
        String cellContent = table.getCellAsText(row, col);

        final boolean result;
        if ("".equals(text))
        {
            result = "".equals(cellContent.trim());
        }
        else
        {
            result = cellContent.indexOf(text) != -1;
        }
        if (!result)
        {
//            log("Expected '" + text + "' but was not found in '" + cellContent + "'");
        }
        return result;
    }
}