package com.atlassian.jira.pageobjects.pages.project;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Browse projects page implementation.
 *
 * @since v6.3
 */
public class BrowseProjectsPage extends AbstractJiraPage
{
    public static final String ALL = "all";
    private final String selectedCategory;

    @ElementBy(cssSelector = "tbody.projects-list")
    private PageElement projectsList;

    public BrowseProjectsPage()
    {
        this(null);
    }

    public BrowseProjectsPage(final String selectedCategory)
    {
        this.selectedCategory = selectedCategory;
    }

    @Override
    public String getUrl()
    {
        return "/secure/BrowseProjects.jspa" + (selectedCategory == null ? "" : "?selectedCategory=" + selectedCategory);
    }

    @Override
    public TimedCondition isAt()
    {
        return projectsList.timed().isPresent();
    }

    public boolean isAllowed()
    {
        return !isForbidden();
    }

    public boolean isForbidden()
    {
        final PageElement errorMsgParagraphs = elementFinder.find(By.cssSelector("div.aui-message.error p"));
        // the error message is: You are not logged in and do not have the permissions required to browse projects as a guest.
        return errorMsgParagraphs.isPresent() && errorMsgParagraphs.getText().contains("do not have the permissions");
    }

    public List<String> getProjectKeys()
    {
        final List<PageElement> projectLinkElements = elementFinder.findAll(By.cssSelector("tbody.projects-list tr td:nth-child(2) a"));
        return Lists.transform(projectLinkElements, new Function<PageElement, String>()
        {
            @Override
            public String apply(final PageElement from)
            {
                final String href = from.getAttribute("href");
                return href.substring(href.lastIndexOf("/") + 1);
            }
        });
    }
}
