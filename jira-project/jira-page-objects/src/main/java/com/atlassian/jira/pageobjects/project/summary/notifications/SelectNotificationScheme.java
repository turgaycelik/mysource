package com.atlassian.jira.pageobjects.project.summary.notifications;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

import javax.inject.Inject;

/**
 * @since v4.4
 */
public class SelectNotificationScheme
{
    @ElementBy (id = "schemeIds_select", timeoutType = TimeoutType.PAGE_LOAD)
    private SelectElement schemeSelect;

    @ElementBy (id = "associate_submit")
    private PageElement submit;

    @ElementBy (id = "cancelButton")
    private PageElement cancelButton;

    @Inject
    private PageBinder binder;

    public SelectNotificationScheme select(String name)
    {
        schemeSelect.select(Options.text(name));
        return this;
    }

    public <T extends Page> T submit(final Class<T> nextPage, String... arguments)
    {
        submit.click();
        return binder.navigateToAndBind(nextPage, arguments);
    }

    public SelectNotificationScheme cancel()
    {
        cancelButton.click();
        return this;
    }
}

