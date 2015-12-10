package com.atlassian.jira.pageobjects.components;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Minimal implementation of an inline dialog. Implementors extend this class and provide
 * the trigger element for the dialog, as well as the id for the container that will house
 * the actual visible dialog.
 *
 * @since v4.4
 */
public class InlineDialog
{
    private final String contentsId;

    private PageElement trigger;
    private PageElement dialogContents;

    @Inject
    private PageElementFinder elementFinder;

    @Init
    public void initialize()
    {
        this.dialogContents = elementFinder.find(By.id("inline-dialog-" + contentsId));
    }

    public InlineDialog(final PageElement trigger, final String contentsId)
    {
        this.trigger = trigger;
        this.contentsId = contentsId;
    }

    public InlineDialog open()
    {
        trigger.click();
        waitUntilTrue(isOpen());
        return this;
    }

    public InlineDialog close()
    {
        if(isOpen().byDefaultTimeout())
        {
            trigger.click();
        }
        return this;
    }

    public TimedQuery<Boolean> isOpen()
    {
        return dialogContents.timed().isVisible();
    }

    protected PageElement getDialogContents()
    {
        return dialogContents;
    }

}
