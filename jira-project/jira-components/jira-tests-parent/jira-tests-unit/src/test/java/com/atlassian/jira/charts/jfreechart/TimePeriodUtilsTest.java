package com.atlassian.jira.charts.jfreechart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.timezone.TimeZoneManager;

import com.google.common.collect.ImmutableList;

import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.time.Week;
import org.junit.Before;
import org.junit.Test;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test for TimePeriodUtils.
 *
 * @since v4.4
 */
public class TimePeriodUtilsTest extends MockControllerTestCase
{
    final static TimeZone PERTH_TZ = TimeZone.getTimeZone("Australia/Perth");

    final static TimeZone LISBON_TZ = TimeZone.getTimeZone("Europe/Lisbon");

    @Mock
    TimeZoneManager timeZoneManager;

    @Test
    public void prettyPrintHoursRespectsTimeZone() throws Exception
    {
        final TimePeriod lisbon3am = RegularTimePeriod.createInstance(Hour.class, date("2011.04.20 03:00", LISBON_TZ), LISBON_TZ);

        TimePeriodUtils timePeriod = instantiate(TimePeriodUtils.class);
        assertThat("3am in Lisbon is 12pm in Perth", timePeriod.prettyPrint(lisbon3am), equalTo("Wed Apr 20 10:00:00 WST 2011"));
    }

    @Test
    public void prettyPrintAnythingOtherThanHoursUsesToString() throws Exception
    {
        List<RegularTimePeriod> timePeriods = listOf(Quarter.class, Month.class, Week.class, Day.class);

        TimePeriodUtils utils = instantiate(TimePeriodUtils.class);
        for (TimePeriod timePeriod : timePeriods)
        {
            assertThat(format("Unexpected format for %s", timePeriod.getClass().getName()), utils.prettyPrint(timePeriod), equalTo(timePeriod.toString()));
        }
    }

    @Before
    public void setUpTimeZoneManager() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andStubReturn(PERTH_TZ);
    }

    private static ImmutableList<RegularTimePeriod> listOf(Class<?>... classes)
    {
        final Date now = new Date();
        final TimeZone timeZone = TimeZone.getDefault();

        ImmutableList.Builder<RegularTimePeriod> builder = ImmutableList.builder();
        for (Class<?> cls : classes)
        {
            builder.add(RegularTimePeriod.createInstance(cls, now, timeZone));
        }

        return builder.build();
    }

    private Date date(String dateString, TimeZone timeZone)
    {
        try
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            format.setTimeZone(timeZone);

            return format.parse(dateString);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }
}
