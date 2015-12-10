package com.atlassian.jira.pageobjects.pages.admin;

import java.util.List;

import javax.inject.Inject;

import org.openqa.selenium.By;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.collect.Iterables;

public class ViewNotificationSchemesPage extends AbstractJiraPage
{
    @ElementBy (cssSelector = "table#notification_schemes")
    protected PageElement schemesTable;

    @ElementBy (cssSelector = "tbody>tr", within = "schemesTable")
    protected Iterable<PageElement> schemeRows;

    @Override
    public TimedCondition isAt()
    {
        return schemesTable.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/jira/ViewNotificationSchemes.jspa";
    }

    public Iterable<SchemeRow> getSchemeRows()
    {
        return Iterables.transform(schemeRows, PageElements.bind(pageBinder, SchemeRow.class));
    }

    public static final class SchemeRow
    {
        @Inject private PageBinder pageBinder;

        private final String schemeName;
        private final PageElement notificationLink;

        public SchemeRow(final PageElement webElement)
        {
            final List<PageElement> cols = webElement.findAll(By.tagName("td"));
            final PageElement name = cols.get(0);
            final PageElement operations = cols.get(2);

            this.notificationLink = operations.find(By.linkText("Notifications"));
            this.schemeName = name.find(By.tagName("b")).getText().trim();
        }

        public String getSchemeName()
        {
            return schemeName;
        }

        public NotificationsNotificationSchemesPage clickNotifications()
        {
            notificationLink.click();
            return pageBinder.bind(NotificationsNotificationSchemesPage.class);
        }
    }

}
