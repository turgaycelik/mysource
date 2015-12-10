package com.atlassian.jira.web.component.cron;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestTimeZoneHelper
{

    @Test
    public void testUseDaylight() throws Exception
    {
        final TimeZone tz = TimeZone.getTimeZone("America/New_York");
        final CronEditorWebComponent.TimeZoneHelper helper = new CronEditorWebComponent.TimeZoneHelper(tz);
        assertFalse(helper.useDaylight(createDate(2008, 2, 1)));
        assertTrue(helper.useDaylight(createDate(2008, 7, 1)));
    }
    
    @Test
    public void testDisplayNameOnWinterDay() throws Exception
    {
        final TimeZone tz = TimeZone.getTimeZone("America/New_York");
        final CronEditorWebComponent.TimeZoneHelper helper = new CronEditorWebComponent.TimeZoneHelper(tz)
        {
            boolean useDaylight(final Date date) // ignore date
            {
                final boolean useDaylight = super.useDaylight(createDate(2008, 2, 1));
                assertFalse(useDaylight);
                return useDaylight;
            }
        };

        assertEquals("Eastern Standard Time", helper.getDisplayName(Locale.US));
    }

    @Test
    public void testDisplayNameOnSummerDay() throws Exception
    {
        final TimeZone tz = TimeZone.getTimeZone("America/New_York");
        final CronEditorWebComponent.TimeZoneHelper helper = new CronEditorWebComponent.TimeZoneHelper(tz)
        {
            boolean useDaylight(final Date date) // ignore date
            {
                final boolean useDaylight = super.useDaylight(createDate(2008, 7, 1));
                assertTrue(useDaylight);
                return useDaylight;
            }
        };

        assertEquals("Eastern Daylight Time", helper.getDisplayName(Locale.US));
    }

    private Date createDate(final int year, final int month, final int date)
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, date);
        return calendar.getTime();
    }

}
