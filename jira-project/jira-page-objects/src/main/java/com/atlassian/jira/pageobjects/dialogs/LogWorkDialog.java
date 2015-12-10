package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.components.CalendarPicker;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;


/**
 * Represents the "Log Work" dialog.
 *
 * @since v5.2
 */
public class LogWorkDialog extends FormDialog
{
    private static final String TIME_SPENT = "log-work-time-logged";
    private static final String CALENDAR_INPUT = "log-work-date-logged-date-picker";
    private static final String CALENDAR_TRIGGER = "log-work-date-logged-icon";

    @ElementBy (id = "log-work-submit")
    protected PageElement submitLogWorkButton;

    protected CalendarPicker dateStarted;

    public LogWorkDialog()
    {
        super("log-work-dialog");
    }

    @Init
    public void init()
    {
        dateStarted = binder.bind(CalendarPicker.class, By.id(CALENDAR_INPUT), By.id(CALENDAR_TRIGGER));
    }


    public CalendarPicker getDateStarted()
    {
        return dateStarted;
    }


    public void setTimeSpent(String timeSpent)
    {
        getDialogElement().find(By.id(TIME_SPENT)).type(timeSpent);
    }

    public boolean submit()
    {
        return submit(submitLogWorkButton);
    }
}
