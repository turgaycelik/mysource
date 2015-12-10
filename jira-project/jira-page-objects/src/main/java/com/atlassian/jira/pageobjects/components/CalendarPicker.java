package com.atlassian.jira.pageobjects.components;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.joda.time.DateTime;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a calender picker control, with a date input and access to calendar popup.
 *
 * @since 5.2
 */
public class CalendarPicker
{
    private static final String ENGLISH_FORMAT = "d/MMM/yy hh:mm a";


    @Inject PageBinder pageBinder;
    @Inject PageElementFinder pageElementFinder;

    protected PageElement input;
    protected PageElement trigger;

    protected final PageElement parent;
    protected final By inputLocator;
    protected final By triggerLocator;

    public CalendarPicker(String inputId)
    {
        this.parent = null;
        this.inputLocator = By.id(inputId);
        this.triggerLocator = By.id(inputId + "-trigger");
    }

    public CalendarPicker(PageElement parent, String inputId)
    {
        this.parent = parent;
        this.inputLocator = By.id(inputId);
        this.triggerLocator = By.id(inputId + "-trigger");
    }

    public CalendarPicker(By inputLocator, By triggerLocator)
    {
        this.parent = null;
        this.inputLocator = inputLocator;
        this.triggerLocator = triggerLocator;
    }

    public CalendarPicker(PageElement parent, By inputLocator, By triggerLocator)
    {
        this.parent = parent;
        this.inputLocator = inputLocator;
        this.triggerLocator = triggerLocator;
    }

    @Init
    public void init()
    {
        final PageElementFinder finder = parent != null ? parent : pageElementFinder;
        trigger = finder.find(triggerLocator);
        input = finder.find(inputLocator);
    }

    public PageElement input()
    {
        return input;
    }

    public PageElement trigger()
    {
        return trigger;
    }

    public CalendarPicker setDate(String dateValue)
    {
        input().clear();
        input.type(dateValue);
        return this;
    }

    public TimedQuery<String> getDateValue()
    {
        return input.timed().getValue();
    }

    public Date getDateValueInEnglishFormat()
    {
        final String dateString = getDateValue().now();
        try
        {
            return new SimpleDateFormat(ENGLISH_FORMAT).parse(dateString);
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Unable to parse date '" + dateString + "'.", e);
        }
    }

    public CalendarPopup openCalendarPopup()
    {
        trigger.click();
        final CalendarPopup popup = pageBinder.bind(CalendarPopup.class);
        Poller.waitUntilTrue(popup.isOpen());
        return popup;
    }

}
