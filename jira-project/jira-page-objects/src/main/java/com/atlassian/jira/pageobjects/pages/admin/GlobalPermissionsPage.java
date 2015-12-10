package com.atlassian.jira.pageobjects.pages.admin;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class GlobalPermissionsPage extends AbstractJiraPage
{

    @ElementBy (id = "global_perms")
    private PageElement mainPermissionTable;

    private List<GlobalPermissionRow> globalPermissions;

    @Override
    public TimedCondition isAt()
    {
        return mainPermissionTable.timed().isPresent();
    }

    @Init
    private void loadGlobalPermissions()
    {
        List<WebElement> rows = driver.findElements(By.cssSelector("table#global_perms > tbody > tr"));
        this.globalPermissions = Lists.transform(rows, new Function<WebElement, GlobalPermissionRow>()
        {
            @Override
            public GlobalPermissionRow apply(final WebElement row)
            {
                return pageBinder.bind(GlobalPermissionRow.class, row);
            }
        });
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/jira/GlobalPermissions!default.jspa";
    }

    public List<GlobalPermissionRow> getGlobalPermissions()
    {
        return globalPermissions;
    }
    
    public List<String> getDeleteLinkIds()
    {
		final List<Iterable<String>> linkIds = Lists.transform(getGlobalPermissions(),
				new Function<GlobalPermissionRow, Iterable<String>>() {
					@Override
					public Iterable<String> apply(final GlobalPermissionRow globalPermissionRow) {
						return globalPermissionRow.getDeleteLinkId();
					}
				});
		return ImmutableList.copyOf(Iterables.concat(linkIds));
    }

    public static final class GlobalPermissionRow
    {
        private final WebElement webElement;
        private final String permissionName;
        private final String secondaryText;
        private final Iterable<String> groupsAndUsers;
        private Iterable<String> deleteLinkIds = ImmutableList.of();

        public GlobalPermissionRow(final WebElement webElement)
        {
            this.webElement = webElement;
            List<WebElement> cols = webElement.findElements(By.tagName("td"));

            WebElement name = cols.get(0);
            WebElement groups = cols.get(1);
            this.permissionName = name.findElement(By.tagName("strong")).getText();
            this.secondaryText = name.findElement(By.className("secondary-text")).getText();
            this.groupsAndUsers = Iterables.transform(groups.findElements(By.tagName("li")), new Function<WebElement, String>()
            {
                @Override
                public String apply(final WebElement webElement)
                {
                    return webElement.getText();
                }
            });
            try
            {
				final List<WebElement> deleteLinks = webElement.findElements(By.linkText("Delete"));
				deleteLinkIds = Iterables.transform(deleteLinks, new Function<WebElement, String>() {
					@Override
					public String apply(final WebElement webElement) {
						return webElement.getAttribute("id");
					}
				});
			} catch (NoSuchElementException e)
            {
                 // nop - probably no operations here
            }
        }

        public String getPermissionName()
        {
            return permissionName;
        }

        public String getSecondaryText()
        {
            return secondaryText;
        }

        public Iterable<String> getGroupsAndUsers()
        {
            return groupsAndUsers;
        }
        
        public Iterable<String> getDeleteLinkId()
        {
            return deleteLinkIds;
        }
    }

}
