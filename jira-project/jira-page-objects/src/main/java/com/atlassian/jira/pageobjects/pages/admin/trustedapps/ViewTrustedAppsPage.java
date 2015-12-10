package com.atlassian.jira.pageobjects.pages.admin.trustedapps;

import javax.inject.Inject;

import org.openqa.selenium.By;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ViewTrustedAppsPage extends AbstractJiraPage
{
    private PageElement table;

    @Init
    public void init()
    {
        table = elementFinder.find(By.cssSelector("table[bgcolor=\"#bbbbbb\"]"));
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/trustedapps/ViewTrustedApplications.jspa";
    }

    public String getFormTitle()
    {
        return elementFinder.find(By.cssSelector("table.jiraform h3.formtitle")).getText().trim();
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.tagName("title")).timed().hasText("View Trusted Applications");
    }

    public Iterable<TrustedAppRow> getAllApps()
    {
        return Iterables.transform(table.findAll(By.cssSelector("tr[class=\"rowNormal\"]")),
                PageElements.bind(pageBinder, TrustedAppRow.class));
    }

    public TrustedAppRow getTrustedAppByName(final String name)
    {
        return Iterables.find(getAllApps(), new Predicate<TrustedAppRow>()
        {
            @Override
            public boolean apply(final TrustedAppRow input)
            {
                return input.getAppName().equals(name);
            }
        });
    }

    public static class TrustedAppRow
    {
        @Inject
        PageBinder pageBinder;

        private final String appName;
        private final PageElement editLink;

        public TrustedAppRow(final PageElement element)
        {
            this.appName = element.find(By.tagName("td")).getText().trim();
            this.editLink = element.find(By.partialLinkText("Edit"));
        }

        public String getAppName()
        {
            return appName;
        }

        public EditTrustedAppPage edit()
        {
            editLink.click();
            return pageBinder.bind(EditTrustedAppPage.class);
        }
    }

}