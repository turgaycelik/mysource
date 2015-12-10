package com.atlassian.jira.pageobjects.pages.viewissue.linkissue;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import org.openqa.selenium.By;

import javax.inject.Inject;

import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents the search confluence page dialog that is opened
 * from the "Wiki Page" tab (LinkConfluenceSection)
 *
 * @since v5.0
 */
public class SearchConfluenceDialog
{
    @Inject
    private PageElementFinder locator;

    @Inject
    private PageBinder pageBinder;

    @ElementBy (id = "confluence-page-search-form")
    private PageElement form;

    @ElementBy (id = "confluence-app-link")
    private SelectElement server;

    @ElementBy (id = "link-search-text")
    private PageElement searchText;

    @ElementBy (id = "search-panel-space")
    private SelectElement spaces;

    @ElementBy (id = "search-panel-button")
    private PageElement searchButton;

    @ElementBy (id = "confluence-link-cancel")
    private PageElement cancelLink;

    private final LinkConfluenceSection linkConfluenceSection;

    public SearchConfluenceDialog(final LinkConfluenceSection linkConfluenceSection)
    {
        this.linkConfluenceSection = linkConfluenceSection;
    }

    @WaitUntil
    final public void waitUntil()
    {
         waitUntilTrue(form.timed().isPresent());
    }

    public SearchConfluenceDialog searchText(final String searchText)
    {
        this.searchText.clear().type(searchText);
        return this;
    }

    public SearchConfluenceDialog search()
    {
        this.searchButton.click();
        return this;
    }

    public LinkConfluenceSection selectResult(final String pageTitle)
    {
        waitUntilTrue(locator.find(By.id("confluence-searchresult")).timed().isPresent());

        final List<PageElement> elements = locator.findAll(By.className("title"));
        for (final PageElement element : elements)
        {
            if (pageTitle.equals(element.getText()))
            {
                element.click();
                return pageBinder.bind(LinkConfluenceSection.class, linkConfluenceSection.getLinkIssueDialog());
            }
        }

        throw new RuntimeException("Search result '" + pageTitle + "' not found");
    }

    public boolean errorsPresent()
    {
        return locator.find(By.className("error")).isPresent();
    }

    public void waitForInfoPresent()
    {
        // Only search inside the results table in case the styling changes and there is a static info message added
        // at the top of the page
        waitUntilTrue(locator.find(By.cssSelector("#search-results-table .info")).timed().isPresent());
    }

    public SearchConfluenceDialog selectServer(final String appId)
    {
        final Option option = Options.value(appId);
        this.server.select(option);
        return this;
    }
}
