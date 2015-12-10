package com.atlassian.jira.pageobjects.pages.admin;

import java.util.List;

import org.openqa.selenium.By;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class NotificationsNotificationSchemesPage extends AbstractJiraPage
{
    @ElementBy (cssSelector = "table#notificationSchemeTable")
    protected PageElement schemesTable;

    @ElementBy (cssSelector = "tbody>tr", within = "schemesTable")
    protected Iterable<PageElement> eventRows;

    @Override
    public TimedCondition isAt()
    {
        return schemesTable.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/jira/EditNotifications!default.jspa";
    }

    public Iterable<EventRow> getEventRows()
    {
        return Iterables.transform(eventRows, PageElements.bind(pageBinder, EventRow.class));
    }

    public static final class EventRow
    {
        private final String eventName;
        private final List<String> notifications;

        public EventRow(final PageElement webElement)
        {
            final List<PageElement> cols = webElement.findAll(By.tagName("td"));
            final PageElement name = cols.get(0);
            final PageElement notifications = cols.get(1);

            this.notifications = Lists.transform(notifications.findAll(By.cssSelector("ul>li>span")), new Function<PageElement, String>()
            {
                @Override
                public String apply(PageElement element)
                {
                    return element.getText().trim();
                }
            });
            this.eventName = name.find(By.tagName("b")).getText().trim();
        }

        public String getEventName()
        {
            return eventName;
        }

        public List<String> getNotifications()
        {
            return notifications;
        }
        
    }

}
