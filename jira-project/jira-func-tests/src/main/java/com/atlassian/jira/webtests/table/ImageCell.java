package com.atlassian.jira.webtests.table;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebTable;

/**
 * This is used with the {@link com.atlassian.jira.webtests.JIRAWebTest#tableRowEquals(com.meterware.httpunit.WebTable, int, java.util.List)}
 * to check if an image with given url exists in a specified table cell.
 */
public class ImageCell extends AbstractSimpleCell
{
    private String url;

    public ImageCell(String url)
    {
        this.url = url;
    }

    public String toString()
    {
        return getUrl();
    }

    public String getUrl()
    {
        return url;
    }

    public String getCellAsText(WebTable table, int row, int col)
    {
        TableCell tableCell = table.getTableCell(row, col);
        WebImage[] images = tableCell.getImages();
        if (images != null && images.length > 0)
        {
            StringBuilder sb = new StringBuilder();
            for (WebImage webImage : images)
            {
                String source = webImage.getSource();
                sb.append("[img: '").append(source).append("']");
            }
            return sb.toString();
        }
        return "No Images was found in " + table.getID() + "[" + row + ", " + col + "]";
    }

    /**
     * Checks if there are any image (&lt;img src="url"&gt;) tag in the specified row and col (cell) of the
     * {@link com.meterware.httpunit.WebTable table} which contains the url. If the url is null, then it checks that
     * there are no image tags at all in the table cell.
     *
     * @param table table to compare
     * @param row row index of table
     * @param col column index of table
     * @return true if there is an image with given url in the table cell (row, col)
     */
    public boolean equals(WebTable table, int row, int col)
    {
        return tableCellHasImageThatContains(table, row, col, getUrl());
    }

    /**
     * Checks if a table cell contains an image URL
     *
     * @param table table to compare to
     * @param row row index to compare
     * @param col col index to compare
     * @param stringInImageSource image filename, e.g. photo.jpg
     * @return True if the cell contains the image URL specified.
     */
    private boolean tableCellHasImageThatContains(WebTable table, int row, int col, String stringInImageSource)
    {
        if (stringInImageSource == null)
        {
            return tableCellHasNoImages(table, row, col);
        }
        else
        {
            log("Checking cell on row [" + row + "] col [" + col + "] for image [" + stringInImageSource + "]");
            TableCell tableCell = table.getTableCell(row, col);
            WebImage[] images = tableCell.getImages();
            if (images != null && images.length > 0)
            {
                String source = null;
                for (WebImage webImage : images)
                {
                    source = webImage.getSource();
                    if (source != null && source.indexOf(stringInImageSource) >= 0)
                    {
                        return true;
                    }
                }
                // Failed to find the expected image.
                // If there was only one found, we will log it.
                if (images.length == 1)
                {
                    log("Expected image with URL '" + stringInImageSource + "' did not match the single image in " +
                        "the cell '" + source + "'");
                }
                else
                {
                    log("Expected image with Url '" + stringInImageSource + "' was not found in table cell [" + row +
                        ", " + col + "] with '" + images.length + "' image(s)");
                }
            }
            else
            {
                log("Expected image with Url '" + stringInImageSource + "' but there was NO images in table cell [" + row + ", " + col + "]");
            }
            return false;
        }
    }

    private boolean tableCellHasNoImages(WebTable table, int row, int col)
    {
        log("Checking cell on row [" + row + "] col [" + col + "] for no images");
        TableCell tableCell = table.getTableCell(row, col);
        WebImage[] images = tableCell.getImages();
        boolean result = images == null || images.length == 0;
        if (!result)
        {
            log("Images were not expected but were found in '" + table.getTableCell(row, col).asText() + "'");
        }
        return result;
    }
}