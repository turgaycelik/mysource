package com.atlassian.jira.pageobjects.pages.admin.screen;

import com.atlassian.jira.pageobjects.components.ScreenEditor;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static java.lang.String.format;

public class EditScreenPage implements Page
{
    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder binder;

    private ScreenEditor screenEditor;
    private final long screenId;
    private PageElement table;

    public EditScreenPage(final long screenId)
    {
        this.screenId = screenId;
    }

    @Init
    private void bindElements()
    {
        screenEditor = binder.bind(ScreenEditor.class);
    }

    @Override
    public String getUrl()
    {
        return format("/secure/admin/ConfigureFieldScreen.jspa?id=%d", screenId);
    }

    @WaitUntil
    public void waitUntil()
    {
        table = elementFinder.find(By.className("fields-table"));
        Poller.waitUntilTrue(table.timed().isPresent());
    }

    public ScreenEditor getScreenEditor()
    {
        return screenEditor;
    }

}
