package com.atlassian.jira.jql.util;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.ConstantClock;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.util.JqlDateSupportImpl.Precision.DAYS;
import static com.atlassian.jira.jql.util.JqlDateSupportImpl.Precision.HOURS;
import static com.atlassian.jira.jql.util.JqlDateSupportImpl.Precision.MINUTES;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Simple test for {@link JqlDateSupportImpl}.
 *
 * @since v4.0
 */
public class TestJqlDateSupportImpl extends MockControllerTestCase
{
    private TimeZoneManager timeZoneManager;

    @Before
    public void setUp() throws Exception
    {
        timeZoneManager = createMock(TimeZoneManager.class);
    }

    @Test
    public void testNullConstructorArguments()
    {
        try
        {
            replay();
            new JqlDateSupportImpl(null, timeZoneManager);
            fail("Expecting an exception.");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    @Test
    public void testParseDateIllegalArgument() throws Exception
    {
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        try
        {
            support.convertToDate((Long) null);
            fail("Expecting an exception.");
        }
        catch (IllegalArgumentException ignored)
        {
        }

        try
        {
            support.convertToDate((String) null);
            fail("Expecting an exception.");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    @Test
    public void testParseDateDuration() throws Exception
    {
        final String validDuration = "-4w";

        final long removeMillis = TimeUnit.SECONDS.toMillis(DateUtils.getDurationWithNegative(validDuration));
        final Date currentDate = new Date();

        //We have to do this calculation like this and not using the Calendar class because the Calendar is timezone
        //sensitive while JIRA's calculation is not.
        final Date expectedDate = new Date(currentDate.getTime() + removeMillis);
        final JqlDateSupport support = new JqlDateSupportImpl(new ConstantClock(currentDate), timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertEquals(expectedDate, support.convertToDate(validDuration));
    }

    @Test
    public void testParseShortYear() throws Exception
    {
        final String yymmdd = "08/06/15";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertNull(support.convertToDate(yymmdd));
    }

    @Test
    public void testParseYyyyMmDd1() throws Exception
    {
        final String yyyymmdd = "2008/06/15";

        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertEquals(createDate(2008, 6, 15), support.convertToDate(yyyymmdd));
    }

    @Test
    public void testParseYyyyMmdd1UsingUserTimeZone() throws Exception
    {
        final String yyyymmdd = "2008/06/15";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getTimeZone("Etc/GMT+12"));
        replay();
        assertEquals(createDateInTimeZone(2008, 6, 15, 22, 0, TimeZone.getTimeZone("Australia/Sydney")), support.convertToDate(yyyymmdd));
    }

    @Test
    public void testParseYyyyMmDd2() throws Exception
    {
        final String yyyymmdd = "2008-07-21";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertEquals(createDate(2008, 7, 21), support.convertToDate(yyyymmdd));
    }

    @Test
    public void testParseYyyyMmDdHhMm1() throws Exception
    {
        final String yyyymmdd = "2008/06/15 15:23";

        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertEquals(createDate(2008, 6, 15, 15, 23), support.convertToDate(yyyymmdd));
    }

    @Test
    public void testParseYyyyMmDdHhMm2() throws Exception
    {
        final String yyyymmdd = "2008-07-21 9:09";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertEquals(createDate(2008, 7, 21, 9, 9), support.convertToDate(yyyymmdd));
    }

    @Test
    public void testParseInvalid() throws Exception
    {
        final String invalidDate = "Some random test";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone()).times(3);
        replay();
        assertNull(support.convertToDate(invalidDate));
        assertNull(support.convertToDate(""));
        assertNull(support.convertToDate("          "));
    }

    @Test
    public void testInvalidDates() throws Exception
    {
        final String invalidDate = "2009/01/45";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertNull(support.convertToDate(invalidDate));
    }

    @Test
    public void testParseHalfInvalid() throws Exception
    {
        final String invalidDate = "2008/12/1 sdhfdfsjkhdk";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertNull(support.convertToDate(invalidDate));
    }

    @Test
    public void testParseHalfInvalid2() throws Exception
    {
        final String yyyymmdd = "2008-07-21 9:09:";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertNull(support.convertToDate(yyyymmdd));
    }

    @Test
    public void testParseWithImpliedPrecision__UtterNonsense() throws Exception
    {
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone()).anyTimes();
        replay();

        assertNull(support.convertToDateRangeWithImpliedPrecision("FUNCTIONAL PROGRAMMING SILVER BULLETS"));
        assertNull(support.convertToDateRangeWithImpliedPrecision("2011-06-29 FUNCTIONAL PROGRAMMING SILVER BULLETS"));
        assertNull(support.convertToDateRangeWithImpliedPrecision("2011-06-29 10:24 FUNCTIONAL PROGRAMMING SILVER BULLETS"));
        assertNull(support.convertToDateRangeWithImpliedPrecision("2011-06-29 10:10:58"));
        assertNull(support.convertToDateRangeWithImpliedPrecision("2011-06-29 10:"));
        assertNull(support.convertToDateRangeWithImpliedPrecision("2011-06-29 10"));
        assertNull(support.convertToDateRangeWithImpliedPrecision("2011-06-"));
        assertNull(support.convertToDateRangeWithImpliedPrecision("2011-06"));
        assertNull(support.convertToDateRangeWithImpliedPrecision(""));
    }

    @Test
    public void testParseWithImpliedPrecision_YyyyMmDdHhMm1_MinuteBased() throws Exception
    {
        replay();
        runTestForPatten("yyyy-MM-dd HH:mm", Duration.MINS);
    }


    @Test
    public void testParseWithImpliedPrecision_YyyyMmDdHhMm2_MinuetBased() throws Exception
    {
        replay();
        runTestForPatten("yyyy/MM/dd HH:mm", Duration.MINS);

    }

    @Test
    public void testParseWithImpliedPrecision_YyyyMmDdHhMm1_DayBased() throws Exception
    {
        replay();
        runTestForPatten("yyyy-MM-dd", Duration.DAYS);
    }

    @Test
    public void testParseWithImpliedPrecision_YyyyMmDdHhMm2_DayBased() throws Exception
    {
        replay();
        runTestForPatten("yyyy/MM/dd", Duration.DAYS);
    }

    @Test
    public void testParseWithImpliedPrecision_AtlassianDuration_HourBased()
    {
        replay();
        
        DurationTestSuite suite = new DurationTestSuite();
        suite.durationTest().hours(0);
        suite.durationTest().hours(1);
        suite.durationTest().hours(24);

        suite.durationTest().days(1).hours(0);
        suite.durationTest().days(1).hours(1);
        suite.durationTest().weeks(1).hours(0);
        suite.durationTest().weeks(1).hours(3);
        suite.durationTest().weeks(1).days(1).hours(0);
        suite.durationTest().weeks(1).days(2).hours(3);

        suite.zone(getDefaultTimeZone()).setClock("2008-7-21").run();
        suite.zone("GMT").setClock("2008-7-21 10:00:56").run();
        suite.zone("GMT+6").setClock("2008-7-21 11:59:59").run();
        suite.zone("GMT+10").setClock("2008-7-21").run();
        suite.zone("GMT-10").run();
        suite.zone("GMT-6").run();
    }

    @Test
    public void testParseWithImpliedPrecision_AtlassianDuration_MinuteBased() throws Exception
    {
        replay();

        DurationTestSuite suite = new DurationTestSuite();
        //Default unit is minutes.
        suite.durationTest().add(1);
        suite.durationTest().minutes(0);
        suite.durationTest().minutes(1);
        suite.durationTest().minutes(60);

        suite.durationTest().hours(1).minutes(0);
        suite.durationTest().hours(3).minutes(3);
        suite.durationTest().hours(3).add(43);

        suite.durationTest().days(1).minutes(0);
        suite.durationTest().days(3).minutes(4);
        suite.durationTest().days(3).add(23);

        suite.durationTest().weeks(2).minutes(38);
        suite.durationTest().weeks(3).minutes(45);
        suite.durationTest().weeks(3).add(0);

        suite.durationTest().days(1).hours(1).minutes(0);
        suite.durationTest().days(1).hours(1).add(0);

        suite.durationTest().weeks(2).hours(3).minutes(3);
        suite.durationTest().weeks(2).hours(3).add(47);

        suite.durationTest().weeks(2).days(2).hours(3).add(47);
        suite.durationTest().weeks(2).days(45).hours(3).minutes(210);

        suite.zone(getDefaultTimeZone()).setClock("2008-7-21").run();
        suite.zone("GMT").run();
        suite.zone("GMT-10").run();
        suite.zone("GMT-6").run();
        suite.zone("GMT+6").run();
        suite.zone("GMT+10").run();
    }

    @Test
    public void testParseWithImpliedPrecision_AtlassianDuration_DayBased() throws Exception
    {
        replay();

        DurationTestSuite suite = new DurationTestSuite();
        suite.durationTest().days(0);
        suite.durationTest().days(1);
        suite.durationTest().days(7);
        suite.durationTest().weeks(2).days(0);
        suite.durationTest().weeks(3).days(4);

        suite.zone(getDefaultTimeZone()).setClock("2008-7-21").run();
        suite.zone("GMT").run();
        suite.zone("GMT-10").run();
        suite.zone("GMT-6").run();
        suite.zone("GMT+6").run();
        suite.zone("GMT+10").run();
    }

    @Test
    public void testParseWithImpliedPrecision_AtlassianDuration_WeekBased() throws Exception
    {
        replay();

        DurationTestSuite suite = new DurationTestSuite();
        suite.durationTest().weeks(0);
        suite.durationTest().weeks(1);
        suite.durationTest().weeks(2);
        suite.durationTest().weeks(4);

        suite.zone(getDefaultTimeZone()).setClock("2008-7-21").run();
        suite.zone("GMT").run();
        suite.zone("GMT-10").run();
        suite.zone("GMT-6").run();
        suite.zone("GMT+6").run();
        suite.zone("GMT+10").run();
    }

    @Test
    public void testConvertDateRange() throws Exception
    {
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        replay();

        final Date now = new Date();
        assertEquals(new DateRange(now, now), support.convertToDateRange(now.getTime()));
    }

    @Test
    public void testParseLong() throws Exception
    {
        final Date now = new Date();
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        replay();
        assertEquals(now, support.convertToDate(now.getTime()));
    }

    @Test
    public void testValidateDateIllegalArguments() throws Exception
    {
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        replay();

        try
        {
            support.validate(null);
            fail("Expecting an exception.");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    @Test
    public void testValidateDateDuration() throws Exception
    {
        final String validDuration = "-4w";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();

        assertTrue(support.validate(validDuration));
    }

    @Test
    public void testValidateYyyyMmDd1() throws Exception
    {
        final String yyyymmdd = "2008/06/15";

        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertTrue(support.validate(yyyymmdd));
    }

    @Test
    public void testValidateYyyyMmDd2() throws Exception
    {
        final String yyyymmdd = "2008-07-21";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertTrue(support.validate(yyyymmdd));
    }

    private static TimeZone getDefaultTimeZone()
    {
        return TimeZone.getDefault();
    }

    @Test
    public void testValidateYyyyMmDdHhMm1() throws Exception
    {
        final String yyyymmdd = "2008/06/15 15:23";

        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertTrue(support.validate(yyyymmdd));
    }

    @Test
    public void testValidateYyyyMmDdHhMm2() throws Exception
    {
        final String yyyymmdd = "2008-07-21 15:24";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertTrue(support.validate(yyyymmdd));
    }

    @Test
    public void testValidateInvalid() throws Exception
    {
        final String invalidDate = "Some random test";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        assertFalse(support.validate(invalidDate));
        assertFalse(support.validate(""));
        assertFalse(support.validate("        "));
    }

    @Test
    public void testValidateHalfInvalid() throws Exception
    {
        final String invalidDate = "2008/12/1 sdhfdfsjkhdk";
        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
        replay();
        final boolean result = support.validate(invalidDate);
        assertFalse(result);
    }

    @Test
    public void testGetIndexedValue() throws Exception
    {
        final Date now = new Date();

        final JqlDateSupport support = new JqlDateSupportImpl(timeZoneManager);
        replay();
        final String indexedValue = support.getIndexedValue(now);
        assertFalse(StringUtils.isBlank(indexedValue));
    }

    @Test
    public void testGetDateStringNullDate()
    {
        try
        {
            expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(getDefaultTimeZone());
            replay();
            new JqlDateSupportImpl(timeZoneManager).getDateString(null);
            fail("Expecting an exception on invalid argument.");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    @Test
    public void testGetDateStringMidnight() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        final Date currentDate = createDateInTimeZone(2007, 6, 14, 0, 0, TimeZone.getDefault());
        replay();
        assertEquals("2007-06-14", new JqlDateSupportImpl(timeZoneManager).getDateString(currentDate));
    }

    @Test
    public void testGetDateStringNotMidnight() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        final Calendar cal = Calendar.getInstance();
        cal.set(2007, 11, 23, 14, 6, 25);
        replay();
        assertEquals("2007-12-23 14:06", new JqlDateSupportImpl(timeZoneManager).getDateString(cal.getTime()));
    }

    @Test
    public void testGetDurationString() throws Exception
    {
        replay();
        assertEquals("3d", JqlDateSupportImpl.getDurationString(259200000));
    }

    @Test
    public void testGetDurationStringNegative() throws Exception
    {
        replay();
        assertEquals("-3d", JqlDateSupportImpl.getDurationString(-259200000));
    }

    @Test
    public void test_reverseParseAtlassianDuration() throws Exception
    {
        replay();
        JqlDateSupportImpl support = new JqlDateSupportImpl(timeZoneManager);

        assertEquals(DAYS, support.reverseParseAtlassianDuration("6w"));
        assertEquals(DAYS, support.reverseParseAtlassianDuration("6w 3d"));
        assertEquals(HOURS, support.reverseParseAtlassianDuration("6w 3d 1h"));
        assertEquals(MINUTES, support.reverseParseAtlassianDuration("6w 3d 1h 12m"));

        assertEquals(DAYS, support.reverseParseAtlassianDuration("3d"));
        assertEquals(HOURS, support.reverseParseAtlassianDuration("3d 1h"));
        assertEquals(MINUTES, support.reverseParseAtlassianDuration("3d 1h 12m"));

        assertEquals(MINUTES, support.reverseParseAtlassianDuration("2h 12m"));
        assertEquals(HOURS, support.reverseParseAtlassianDuration("2h"));

        assertEquals(MINUTES, support.reverseParseAtlassianDuration("12m"));

        assertEquals(MINUTES, support.reverseParseAtlassianDuration("12"));

        assertEquals(DAYS, support.reverseParseAtlassianDuration("6.5w"));
        assertEquals(DAYS, support.reverseParseAtlassianDuration("0.66w 3d"));
        assertEquals(HOURS, support.reverseParseAtlassianDuration("6w 3d 1h"));
        assertEquals(MINUTES, support.reverseParseAtlassianDuration("6w 3d 1h 12m"));

        assertEquals(DAYS, support.reverseParseAtlassianDuration("3d"));
        assertEquals(HOURS, support.reverseParseAtlassianDuration("3d 1h"));
        assertEquals(MINUTES, support.reverseParseAtlassianDuration("3d 1h 12m"));

        assertEquals(MINUTES, support.reverseParseAtlassianDuration("2h 12m"));
        assertEquals(HOURS, support.reverseParseAtlassianDuration("2h"));

        assertEquals(MINUTES, support.reverseParseAtlassianDuration("12m"));

        assertEquals(MINUTES, support.reverseParseAtlassianDuration("12"));
        assertEquals(MINUTES, support.reverseParseAtlassianDuration("12h 12"));
        assertEquals(MINUTES, support.reverseParseAtlassianDuration("12m 12"));
        assertEquals(MINUTES, support.reverseParseAtlassianDuration("12d 12"));

        assertEquals(MINUTES, support.reverseParseAtlassianDuration("12 12d"));
    }

    private static Date createDate(int year, int month, int day)
    {
        return createDate(year, month, day, 0, 0);
    }

    private static Date createDate(int year, int month, int day, int hour, int minute)
    {
        return createDateInTimeZone(year, month, day, hour, minute, 0, getDefaultTimeZone());
    }

    private static Date createDate(int year, int month, int day, int hour, int minute, int second)
    {
        return createDateInTimeZone(year, month, day, hour, minute, second, getDefaultTimeZone());
    }

    private static Date createDateInTimeZone(int year, int month, int day, int hour, int minute, TimeZone timeZone)
    {
        return createDateInTimeZone(year, month, day, hour, minute, 0, timeZone);
    }

    private static Date createDateInTimeZone(int year, int month, int day, int hour, int minute, int second, TimeZone timeZone)
    {
        final Calendar expectedCal = Calendar.getInstance();
        expectedCal.setTimeZone(timeZone);
        expectedCal.set(year, month - 1, day, hour, minute, second);
        expectedCal.set(Calendar.MILLISECOND, 0);
        return expectedCal.getTime();
    }

    private void runTestForPatten(final String pattern, final Duration duration)
    {
        DateTestSuite suite = new DateTestSuite().patten(pattern, duration);

        //Note the dates here are not parsed using the passed pattern a test independent pattern.
        suite.addTest("2008-07-21 9:09:00").addTest("2012-12-31 11:59:59");

        suite.zone(getDefaultTimeZone()).run();
        suite.zone("GMT").run();
        suite.zone("GMT-6").run();
        suite.zone("GMT-2").run();
        suite.zone("GMT+2").run();
        suite.zone("GMT+10").run();
    }

    private enum Duration
    {
        MINS(60, 'm')
        {
            @Override
            public Calendar startRange(Calendar calendar)
            {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            }

            @Override
            public Calendar endRange(Calendar calendar)
            {
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            }
        },
        HOURS(MINS.getSeconds() * 60, 'h')
        {
            @Override
            public Calendar startRange(Calendar calendar)
            {
                MINS.startRange(calendar).set(Calendar.MINUTE, 0);
                return calendar;
            }

            @Override
            public Calendar endRange(Calendar calendar)
            {
                MINS.endRange(calendar).set(Calendar.MINUTE, 59);
                return calendar;
            }
        },
        DAYS(HOURS.getSeconds() * 24, 'd')
        {
            @Override
            public Calendar endRange(Calendar calendar)
            {
                HOURS.endRange(calendar).set(Calendar.HOUR_OF_DAY, 23);
                return calendar;
            }

            @Override
            public Calendar startRange(Calendar calendar)
            {
                HOURS.startRange(calendar).set(Calendar.HOUR_OF_DAY, 0);
                return calendar;
            }
        },
        WEEKS(DAYS.getSeconds() * 7, 'w')
        {
            @Override
            public Calendar endRange(Calendar calendar)
            {
                return DAYS.endRange(calendar);
            }

            @Override
            public Calendar startRange(Calendar calendar)
            {
                return DAYS.startRange(calendar);
            }
        };

        private long seconds;
        private char unit;

        private Duration(long seconds, char unit)
        {
            this.seconds = seconds;
            this.unit = unit;
        }

        public long getSeconds()
        {
            return seconds;
        }

        public char getUnit()
        {
            return unit;
        }

        public abstract Calendar startRange(Calendar calendar);
        public abstract Calendar endRange(Calendar calendar);
    }

    private static abstract class ParseTestSuite<T extends ParseTestSuite>
    {
        private final SimpleDateFormat YMD = new SimpleDateFormat("yy-MM-dd");
        private final SimpleDateFormat YMDHMS = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        protected TimeZone zone = TimeZone.getDefault();

        public T zone(String zone)
        {
            this.zone = TimeZone.getTimeZone(zone);
            return getThis();
        }

        public T zone(TimeZone zone)
        {
            this.zone = zone;
            return getThis();
        }

        Date parseTestDate(String time)
        {
            Date d = parseDate(YMDHMS, time);
            if (d == null)
            {
                d = parseDate(YMD, time);
                if (d == null)
                {
                    throw new IllegalArgumentException("Unable to parse '" + time + "'.");
                }
            }
            return d;
        }

        private Date parseDate(DateFormat format, String str)
        {
            ParsePosition position = new ParsePosition(0);
            format.setTimeZone(zone);
            Date date = format.parse(str, position);
            if (date == null || position.getErrorIndex() >= 0 || position.getIndex() < str.length())
            {
                return null;
            }
            else
            {
                return date;
            }
        }

        TimeZoneManager createManagerForZone()
        {
            return new ConstantTimeZoneManager(zone);
        }

        abstract T getThis();
    }

    private static class DateTestSuite extends ParseTestSuite<DateTestSuite>
    {
        private final List<String> data = new ArrayList<String>();

        private Duration duration;
        private SimpleDateFormat format;
        private String pattern;

        public DateTestSuite patten(String pattern, Duration duration)
        {
            this.duration = duration;
            this.format = new SimpleDateFormat(pattern);
            this.pattern = pattern;

            return this;
        }

        public DateTestSuite addTest(String string)
        {
            data.add(string);
            return this;
        }

        public DateTestSuite run()
        {
            final List<String> errors = new ArrayList<String>();
            final JqlDateSupportImpl support = new JqlDateSupportImpl(new ConstantClock(10L), createManagerForZone());

            DateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            displayFormat.setTimeZone(zone);

            format.setTimeZone(zone);
            for (String s : data)
            {
                //Parse the date from the independent test form.
                Date testDate = parseTestDate(s);

                //Format the date into test dependent form.
                String testString = format.format(testDate);
                
                Calendar instance = Calendar.getInstance(zone);
                instance.setTime(testDate);
                Date expectedStart = duration.startRange(instance).getTime();
                Date expectedEnd = duration.endRange(instance).getTime();

                DateRange dateRange = support.convertToDateRangeWithImpliedPrecision(testString);
                if (!expectedStart.equals(dateRange.getLowerDate()) && !expectedEnd.equals(dateRange.getUpperDate()))
                {
                    errors.add(format("Expected range [%s, %s] but got [%s, %s] for %s (%s).",
                            displayFormat.format(expectedStart), displayFormat.format(expectedEnd),
                            displayFormat.format(dateRange.getLowerDate()), displayFormat.format(dateRange.getUpperDate()),
                            testString, pattern));
                }
            }

            if (!errors.isEmpty())
            {
                fail(StringUtils.join(errors, '\n'));
            }

            return this;
        }

        @Override
        DateTestSuite getThis()
        {
            return this;
        }
    }

    private static class DurationTestSuite extends ParseTestSuite<DurationTestSuite>
    {
        private final List<DurationTest> tests = new ArrayList<DurationTest>();
        private String clock;

        public DurationTest durationTest()
        {
            DurationTest newTest = new DurationTest();
            tests.add(newTest);
            return newTest;
        }

        public DurationTestSuite setClock(String clock)
        {
            this.clock = clock;
            return this;
        }

        @Override
        DurationTestSuite getThis()
        {
            return this;
        }

        public DurationTestSuite run()
        {
            Date now = parseTestDate(clock);

            final List<String> errors = new ArrayList<String>();
            final JqlDateSupportImpl support = new JqlDateSupportImpl(new ConstantClock(now), createManagerForZone());

            for (DurationTest test : tests)
            {
                errors.addAll(test.runTest(support, zone, now.getTime()));
            }

            if (!errors.isEmpty())
            {
                fail(StringUtils.join(errors, '\n'));
            }

            return this;
        }

        private static class DurationTest
        {
            private StringBuilder duration = new StringBuilder();
            private Duration currentDuration = null;
            private long diff = 0;

            private List<String> runTest(final JqlDateSupport support, final TimeZone zone, final long now)
            {
                List<String> errors = new ArrayList<String>();

                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                format.setTimeZone(zone);

                //Test positive duration.
                Calendar calendar = Calendar.getInstance(zone);
                calendar.setTimeInMillis(now + diff);

                runTest(support, errors, format, calendar, duration.toString());

                if (diff != 0)
                {
                    //Test negative duration.
                    calendar.setTimeInMillis(now - diff);
                    runTest(support, errors, format, calendar, "-" + duration.toString());
                }

                return errors;
            }

            private void runTest(JqlDateSupport support, List<String> errors, DateFormat format, Calendar time,
                    final String durationString)
            {
                Calendar cal = (Calendar) time.clone();
                Date expectedStart = currentDuration.startRange(cal).getTime();
                Date expectedEnd = currentDuration.endRange(cal).getTime();

                DateRange dateRange = support.convertToDateRangeWithImpliedPrecision(durationString);
                if (!expectedStart.equals(dateRange.getLowerDate()) && !expectedEnd.equals(dateRange.getUpperDate()))
                {
                    errors.add(format("Expected range [%s, %s] but got [%s, %s] for %s (@ %s).",
                            format.format(expectedStart), format.format(expectedEnd),
                            format.format(dateRange.getLowerDate()), format.format(dateRange.getUpperDate()),
                            durationString, format.format(time.getTime())));
                }
            }

            private DurationTest weeks(int weeks)
            {
                return addUnit(weeks, Duration.WEEKS);
            }

            public DurationTest days(int days)
            {
                return addUnit(days, Duration.DAYS);
            }

            public DurationTest hours(int hours)
            {
                return addUnit(hours, Duration.HOURS);
            }

            public DurationTest minutes(int minutes)
            {
                return addUnit(minutes, Duration.MINS);
            }

            public DurationTest add(int count)
            {
                return addUnit(count, null);
            }

            private DurationTest addUnit(int count, Duration unit)
            {
                if (duration.length() > 0)
                {
                    duration.append(" ");
                }
                duration.append(count);

                if (unit != null)
                {
                    duration.append(unit.getUnit());
                }

                if (unit == null)
                {
                    unit = Duration.MINS;
                }

                diff = diff + count * TimeUnit.SECONDS.toMillis(unit.getSeconds());

                if (currentDuration == null || (unit != Duration.WEEKS && currentDuration.compareTo(unit) > 0))
                {
                    currentDuration = unit;
                }

                return this;
            }

            @Override
            public String toString()
            {
                return format("%s [%d].", duration.toString(), diff);
            }
        }
    }

    private static class ConstantTimeZoneManager implements TimeZoneManager
    {
        private final TimeZone zone;

        public ConstantTimeZoneManager(TimeZone zone)
        {
            this.zone = zone;
        }

        @Override
        public TimeZone getLoggedInUserTimeZone()
        {
            return zone;
        }
        @Override
        public TimeZone getTimeZoneforUser(User user)
        {
            return zone;
        }

        @Override
        public TimeZone getDefaultTimezone()
        {
            return zone;
        }
    }
}
