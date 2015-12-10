package com.atlassian.jira.pageobjects.components;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementActions;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;

import javax.inject.Inject;

import java.util.List;

import static java.lang.String.format;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
* Represents the calendar popup on a page.
*
* @since v5.1
*/
public class CalendarPopup
{
    @Inject protected PageBinder pageBinder;
    @Inject protected PageElementFinder pageElementFinder;
    @Inject protected JiraTestedProduct jira;

    @Inject protected PageElementActions actions;

    @Inject protected AtlassianWebDriver driver;

    @Inject protected Timeouts timeouts;

    @ElementBy(cssSelector = "div.calendar.active")
    protected PageElement openCalendar;

    private static final String DAY_CELL_TEMPLATE = "day-%d";
    private static final String HOUR_CELL = "hour";



    /**
     * A cell in the calendar popup representing a certain day. Note that the cell may or may not exist on the page,
     * which may be queried by {@link com.atlassian.pageobjects.elements.PageElement#isPresent()} method of the
     * returned element.
     *
     * @param day day cell number
     * @return a page element representing the day cell in the calendar table
     */
    public PageElement getDayCell(int day)
    {
        return openCalendar.find(By.className(format(DAY_CELL_TEMPLATE, day)));
    }

    public Iterable<PageElement> getAllDayCells()
    {
        return openCalendar.findAll(By.className("day"));
    }

    public PageElement getSelectedDayCell()
    {
        PageElement selected = Iterables.find(getAllDayCells(), PageElements.hasClass("selected"), null);
        assertNotNull("Could not find selected day in " + getAllDayCells(), selected);
        return selected;
    }

    public boolean hasDay(int day)
    {
        return getDayCell(day).isPresent();
    }

    public TimedQuery<Integer> getSelectedDay()
    {
        return parseIntValue(getSelectedDayCell());
    }

    /**
     * A cell in the calendar popup representing the hour. Note that the cell may or may not exist on the page,
     * which may be queried by {@link com.atlassian.pageobjects.elements.PageElement#isPresent()} method of the
     * returned element.
     *
     * @return a page element representing the hour cell in the calendar table
     */
    public PageElement getHourCell()
    {
        return openCalendar.find(By.className(HOUR_CELL));
    }

    public boolean isDateTime()
    {
        return getHourCell().isPresent();
    }

    public TimedQuery<Integer> getHour()
    {
        assertTrue("Only date-time picker can have hour value", isDateTime());
        return parseIntValue(getHourCell());
    }

    private TimedQuery<Integer> parseIntValue(final PageElement cell)
    {
        return Queries.forSupplier(timeouts, new Supplier<Integer>()
        {
            @Override
            public Integer get()
            {
                return Integer.parseInt(cell.getText().trim());
            }
        });
    }

    public TimedCondition isOpen()
    {
        return Conditions.and(openCalendar.timed().isPresent(), openCalendar.timed().hasClass("active"));
    }

    public TimedQuery<Boolean> isClosed()
    {
        return Conditions.not(isOpen());

    }

    /**
     * <p/>
     * Clicks a day cell in this calendar. The popup must be open and the cell must exist in this calendar, or an
     * exception will be raised.
     *
     * <p/>
     * This will close the popup and insert the date into the associated picker. Query the picker for the result
     * of this action.
     *
     * @param day the day number to click
     */
    public void selectDay(int day)
    {
        final PageElement dayLocator = getDayCell(day);
        assertTrue("Cell " + day + " does not exist in this calendar", dayLocator.isPresent());
        dayLocator.click();
    }


    public CalendarPopup increaseHour()
    {
        assertTrue("It's not a date-time picker", isDateTime());
        final PageElement hourCell = getHourCell();
        TimedCondition hourChanged = hourChangedCondition();
        hourCell.click();
        Poller.waitUntilTrue("Hour did not change after clicking", hourChanged);
        return this;
    }

    private TimedCondition hourChangedCondition()
    {
        final int initialHour = getHour().now();
        return Conditions.forSupplier(new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                // we want the current hour to be different than initial hour
                return getHour().now() != initialHour;
            }
        });
    }

    public CalendarPopup increaseHour(int times)
    {
        for (int i=0; i<times; i++)
        {
            increaseHour();
        }
        return this;
    }

    public TimedQuery getCalendarTitle()
    {
        return openCalendar.find(By.className("title")).timed().getText();
    }

    public PageElement getCalendarToolTip()
    {
        return openCalendar.find(By.className("ttip"));
    }

    public PageElement getCalendarWeekLabel()
    {
        return openCalendar.find(By.cssSelector(".name.wn"));
    }

    public PageElement getCalendarAMPM()
    {
        return openCalendar.find(By.className("ampm"));
    }

    public List<PageElement> getDaysOfWeek()
    {
        return openCalendar.findAll(By.cssSelector(".day.name"));
    }

    public PageElement getTodayButton()
    {
        // NOTE: find a better way to do this given enough time. May need to change production code.
        PageElement headRow = openCalendar.find(By.className("headrow"));
        List<PageElement> navigatorButtons = headRow.findAll(By.className("button"));
        // << < TODAY > >>
        // index 2...
        return navigatorButtons.get(2);
    }


}
