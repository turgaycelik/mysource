package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.components.fields.MultiSelect;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class ShareDialog extends JiraDialog
{
    @Inject
    private PageBinder pageBinder;

    @ElementBy (id = "submitShare")
    private PageElement submit;

    @ElementBy (id = "cancelShare")
    private PageElement cancel;

    @ElementBy (id = "note")
    private PageElement note;

    @ElementBy (className = "progress-messages")
    private PageElement progressMessage;

    private MultiSelect recipients;

    @Init
    private void init()
    {
        recipients = pageBinder.bind(MultiSelect.class, "sharenames", new Function<String, By>(){
            @Override
            public By apply(@Nullable final String itemName)
            {
                //means find all items
                if(itemName == null)
                {
                    return By.cssSelector(".recipients li");
                }
                else
                {
                    return By.cssSelector(".recipients li[title=\"" + itemName + "\"]");
                }
            }
        });
    }

    public void addUserRecipient(final String userName)
    {
        addRecipient(userName);
    }

    private void addRecipient(final String recipient)
    {
        recipients.add(recipient);
    }

    public void addMailRecipient(final String email)
    {
        addRecipient(email);
    }

    public void setNote(final String note)
    {
        this.note.type(note);
    }

    public void submitAndWaitForSuccess()
    {
        Poller.waitUntilTrue(submit.timed().isEnabled());
        submit.click();
        final TimedCondition successCondition = progressMessage.timed().hasClass("success");
        Poller.waitUntilTrue("Expected success after submit share", successCondition);
    }

    public TimedCondition isSubmitEnabled()
    {
        return submit.timed().isEnabled();
    }

    public TimedCondition isPresent()
    {
        return submit.timed().isPresent();
    }

    public TimedCondition isVisible()
    {
        return submit.timed().isVisible();
    }
}
