package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.util.UriQueryParser;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.openqa.selenium.By;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Author: Geoffrey Wong
 * JIRA Administration page to browse all groups in JIRA instance
 */
public class GroupBrowserPage extends AbstractJiraPage
{
    @ElementBy (name = "max")
    SelectElement maxGroupsPerPage;

    @ElementBy (name = "nameFilter")
    PageElement nameFilter;

    @ElementBy (className = "aui-button")
    PageElement filterGroupsButton;
    
    @ElementBy (linkText = "Reset Filter")
    PageElement resetFilter;

    @ElementBy (id = "group_browser_table")
    PageElement groupBrowserTable;

    @ElementBy (cssSelector = "tbody tr", within = "groupBrowserTable")
    protected Iterable<PageElement> groupRows;

    @ElementBy (id = "add-group")
    private PageElement addGroupForm;

    @ElementBy (name = "addName", within = "addGroupForm")
    private PageElement addGroupNameField;

    @ElementBy (name = "add_group", within = "addGroupForm")
    private PageElement addGroupSubmit;

    @Inject
    private PageElementFinder pageElementFinder;

    public String getUrl()
    {
        return "/secure/admin/user/GroupBrowser.jspa";
    }

    @Override
    public TimedCondition isAt()
    {
        return groupBrowserTable.timed().isPresent();
    }

    public GroupBrowserPage setMaxGroupsPerPage(final String option)
    {
        maxGroupsPerPage.select(Options.text(option));
        return this;
    }

    public GroupBrowserPage setGroupNameFilter(final String groupName)
    {
        nameFilter.clear().type(groupName);
        return this;
    }
    
    public GroupBrowserPage filterGroups()
    {
        filterGroupsButton.click();
        return pageBinder.bind(GroupBrowserPage.class);
    }

    public GroupBrowserPage resetFilter()
    {
        resetFilter.click();
        return this;
    }

    public BulkEditGroupMembersPage editMembersOfGroup(final String group)
    {
        pageElementFinder.find(By.id("edit_members_of_" + group)).click();
        return pageBinder.bind(BulkEditGroupMembersPage.class, group);
    }

    public Iterable<GroupListRow> getDisplayedGroups()
    {
        return Iterables.transform(groupRows, PageElements.bind(pageBinder, GroupListRow.class));
    }

    public GroupListRow findGroupByName(final String name)
    {
        return Iterables.find(getDisplayedGroups(), new Predicate<GroupListRow>()
        {
            @Override
            public boolean apply(final GroupListRow input)
            {
                return name.equals(input.getGroupName());
            }
        });
    }

    public void addGroup(final String name)
    {
        addGroupNameField.clear().type(name);
        addGroupSubmit.click();
    }

    public static class GroupListRow
    {
        private static final UriQueryParser URI_QUERY_PARSER = new UriQueryParser();

        @Inject
        private PageBinder binder;

        @Nullable
        private final PageElement deleteLink;

        private final String groupName;

        public GroupListRow(final PageElement row)
        {
            groupName = row.find(By.cssSelector("td:first-child a")).getText();
            deleteLink = findDeleteLink(row);
        }

        private PageElement findDeleteLink(final PageElement row)
        {
            return Iterables.find(row.findAll(By.cssSelector(".operations-list a")), new Predicate<PageElement>()
            {
                @Override
                public boolean apply(final PageElement link)
                {
                   return "Delete".equals(link.getText().trim());
                }
            }, null);
        }

        public String getGroupName()
        {
            return groupName;
        }

        public DeleteGroupPage delete() throws URISyntaxException
        {
            if (deleteLink == null)
            {
                throw new IllegalStateException(String.format("Cannot delete the group '%s'.", groupName));
            }
            else
            {
                final Map<String, String> params = URI_QUERY_PARSER.parse(URI.create(deleteLink.getAttribute("href")));
                deleteLink.click();
                return binder.bind(DeleteGroupPage.class, params.get("name"), params.get("atl_token"));
            }
        }
    }
}
