package com.atlassian.jira.pageobjects.pages.viewissue.link;

import com.atlassian.jira.pageobjects.pages.viewissue.DeleteLinkConfirmationDialog;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents the issue links section.
 *
 * @since v5.0
 */
public class IssueLinkSection
{
    public enum LinkSourceType { ALL, INTERNAL, REMOTE }

    @Inject
    private PageBinder pageBinder;
    
    @Inject
    private PageElementFinder locator;

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private Timeouts timeouts;

    @ElementBy(id = "show-more-links-link")
    private PageElement showMoreLinksLink;

    private final String issueKey;

    public IssueLinkSection(String issueKey)
    {
        this.issueKey = issueKey;
    }

    @WaitUntil
    public void waitUntilLoaded()
    {
        //Basically, we are waiting for links to stop loading. Why don't we use !timed().isPresent()?
        //Well basically, the "link-loading" element is not removed from the page after an error it detected, infact
        //it is used to display the error message. The timed() calls cache the webdriver element the first time
        //it is looked up. This means that timed().isPresent() will continue to return true even after the "link-loading"
        //class has been removed. We would need to write a condition like !and(timed().isPresent(), timed().hasClass("link-loading"))
        //for each loading element. It just seemed simpler to do it this way.
        waitUntilTrue(Conditions.forSupplier(timeouts.timeoutFor(TimeoutType.PAGE_LOAD), new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return locator.findAll(By.className("link-loading")).isEmpty();
            }
        }));
    }

    /**
     * Returns the relationships. E.g. "blocked by" or "relates to".
     *
     * @return list of relationships
     */
    public List<String> getRelationships()
    {
        ImmutableList.Builder<String> relationshipsListBuilder = ImmutableList.builder();
        for (PageElement linkTypeElement : getAllLinkTypeElements())
        {
            relationshipsListBuilder.add(extractRelationshipFromLinkTypeElement(linkTypeElement));
        }
        return relationshipsListBuilder.build();
    }

    public List<IssueLink> getLinks()
    {
        List<IssueLink> issueLinks = Lists.newArrayList();
        for (PageElement linkTypeElement : getAllLinkTypeElements())
        {
            String relationship = extractRelationshipFromLinkTypeElement(linkTypeElement);
            issueLinks.addAll(getLinks(LinkSourceType.ALL, relationship, linkTypeElement));
        }
        return issueLinks;
    }

    /**
     * Returns the issue link with the given title. If there are multiple issue links with the same title, the first
     * one is returned. If no issue link is found, null is returned.
     *
     * @param title the issue link title
     * @return the issue link with the given title
     */
    public IssueLink getLinkByTitle(final String title)
    {
        for (final IssueLink issueLink : getLinks())
        {
            if (title.equals(issueLink.getTitle()))
            {
                return issueLink;
            }
        }

        return null;
    }

    /**
     * Returns the links for the source. Returns empty list if the source could not be found.
     *
     * @param source source of the links
     * @return links for the source
     */
    public List<IssueLink> getLinksForSource(LinkSourceType source)
    {
        List<IssueLink> issueLinks = Lists.newArrayList();
        for (String relationship : getRelationships())
        {
            issueLinks.addAll(getLinks(source, relationship));
        }
        return issueLinks;
    }

    /**
     * Returns the links for the source and relationship. Returns empty list if the source or relationship could not be
     * found.
     *
     * @param sourceType source type of the links
     * @param relationship E.g. "blocked by"
     * @return links for the source and relationship
     */
    public List<IssueLink> getLinks(LinkSourceType sourceType, String relationship)
    {
        PageElement linkTypeElement = findLinkTypeElement(relationship);
        return getLinks(sourceType, relationship, linkTypeElement);
    }

    public DeleteLinkConfirmationDialog deleteLink(IssueLink issueLink)
    {
        PageElement linkElement = locator.find(By.id(issueLink.getElementId()));
        driver.executeScript(String.format("AJS.$('#delete_%s').css('visibility', 'visible');", issueLink.getElementId()));
        PageElement deleteLinkElement = linkElement.find(By.className("icon-delete"));
        Poller.waitUntilTrue(deleteLinkElement.timed().isVisible());
        deleteLinkElement.click();
        return pageBinder.bind(DeleteLinkConfirmationDialog.class, issueKey);
    }

    public IssueLinkSection expandLinks()
    {
        showMoreLinksLink.click();

        // Wait for newly visible links to load
        waitUntilFalse(locator.find(By.className("link-loading")).timed().isVisible());

        return this;
    }

    /**
     * Click the "Authenticate" link for a remote issue that requires an OAuth token.
     *
     * @param issueLink the issue link to authenticate
     */
    public void authenticateLink(IssueLink issueLink)
    {
        final PageElement link = locator.find(By.id(issueLink.getElementId()));
        final PageElement authenticateLink = link.find(By.className("applink-authenticate"));
        authenticateLink.click();
    }

    public boolean warningsPresent()
    {
        return locator.find(By.cssSelector("#linkingmodule .warning")).isPresent();
    }

    private List<IssueLink> getLinks(LinkSourceType sourceType, String relationship, PageElement linkTypeElement)
    {
        if (linkTypeElement == null)
        {
            return Collections.emptyList();
        }

        List<IssueLink> issueLinks = Lists.newArrayList();
        List<PageElement> linkElements = linkTypeElement.findAll(By.tagName("dd"));
        for (PageElement linkElement : linkElements)
        {
            if ((sourceType == LinkSourceType.ALL || getLinkSourceType(linkElement) == sourceType) && linkElement.isVisible())
            {
                issueLinks.add(convertToIssueLink(relationship, linkElement));
            }
        }

        return issueLinks;
    }

    private LinkSourceType getLinkSourceType(PageElement linkElement)
    {
        return linkElement.hasClass("remote-link") ? LinkSourceType.REMOTE : LinkSourceType.INTERNAL;
    }

    private String extractRelationshipFromLinkTypeElement(PageElement linkTypeElement)
    {
        return linkTypeElement.find(By.tagName("dt")).getText();
    }

    private List<PageElement> getAllLinkTypeElements()
    {
        return locator.findAll(By.className("links-list"));
    }

    /**
     * Finds the link type element. Returns <tt>null</tt> if the element could not be found.
     *
     * @param relationship E.g. "blocked by"
     * @return LinkType element if found, otherwise <tt>null</tt>
     */
    private PageElement findLinkTypeElement(String relationship)
    {
        for (PageElement linkTypeElement : getAllLinkTypeElements())
        {
            if (extractRelationshipFromLinkTypeElement(linkTypeElement).equals(relationship))
            {
                return linkTypeElement;
            }
        }
        return null;
    }

    private IssueLink convertToIssueLink(String relationship, PageElement linkElement)
    {
        String elementId = linkElement.getAttribute("id");
        PageElement titleElement = linkElement.find(By.className("link-title"));
        String title = titleElement.getText();
        String url = null;
        if ("a".equals(titleElement.getTagName()))
        {
            url = titleElement.getAttribute("href");
        }

        boolean resolved = titleElement.hasClass("resolution");

        PageElement summaryElement = linkElement.find(By.className("link-summary"));
        String summary = null;
        if (summaryElement.isPresent())
        {
            summary = summaryElement.getText();
        }

        PageElement iconElement = linkElement.find(By.tagName("p")).find(By.tagName("img"));
        String iconUrl = null;
        if (iconElement.isPresent())
        {
            iconUrl = iconElement.getAttribute("src");
        }

        String deleteUrl = linkElement.find(By.className("delete-link")).find(By.className("icon-delete")).getAttribute("href");

        PageElement priorityElement = linkElement.find(By.className("priority"));
        String priorityIconUrl = null;
        if (priorityElement.isPresent())
        {
            priorityIconUrl = priorityElement.find(By.tagName("img")).getAttribute("src");
        }

        PageElement statusElement = linkElement.find(By.className("status"));
        String status = null;
        if (statusElement.isPresent())
        {
            status = statusElement.find(By.className("jira-issue-status-lozenge")).getText();
        }

        return IssueLink.builder()
                .elementId(elementId)
                .relationship(relationship)
                .title(title)
                .url(url)
                .summary(summary)
                .iconUrl(iconUrl)
                .deleteUrl(deleteUrl)
                .priorityIconUrl(priorityIconUrl)
                .status(status)
                .resolved(resolved)
                .build();
    }

}
