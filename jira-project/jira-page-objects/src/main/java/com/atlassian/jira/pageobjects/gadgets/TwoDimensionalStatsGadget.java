package com.atlassian.jira.pageobjects.gadgets;

import com.atlassian.pageobjects.elements.PageElement;
import it.com.atlassian.gadgets.pages.ConfigurableAJSGadget;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Page object representing Two Dimensional Stats Gadget
 *
 * @since v5.1
 */
public class TwoDimensionalStatsGadget extends ConfigurableAJSGadget
{
    public TwoDimensionalStatsGadget(String id)
    {
        super(id);
    }

    public PageElement getCell(int column, int row)
    {
        final PageElement tableRow = getRows().get(row);
        final List<PageElement> cells = tableRow.findAll(By.cssSelector("td"));
        return cells.get(column);
    }

    public PageElement getHeader(int column, int row)
    {
        final PageElement tableRow = getRows().get(row);
        final List<PageElement> cells = tableRow.findAll(By.cssSelector("th"));
        return cells.get(column);
    }

    private PageElement getTable()
    {
        return find(By.id("twodstatstable"));
    }

    private List<PageElement> getRows()
    {
        return getTable().findAll(By.cssSelector("tr"));
    }

    public void clickFilter()
    {
        final PageElement link = find(By.cssSelector(".table-footer a"));
        link.click();
    }

    public void showMore()
    {
        final PageElement showMoreLink = find(By.cssSelector("p.more a"));
        showMoreLink.click();
    }
}
