/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.datetime;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.mail.internet.MailDateFormat;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneResolver;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;

import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import junit.framework.Assert;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class TestOutlookDate
{
    private final DateFormat dmyFormatter = new SimpleDateFormat("dd/MMM/yy", Locale.US);
    private final String str15jul2002 = "15/Jul/02";

    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private DateTimeFormatterFactory dateTimeFormatterFactory;
    @Mock
    TimeZoneResolver timeZoneInfoResolver;
    @Rule
    public TestRule initRule = MockitoMocksInContainer.forTest(this);

    private OutlookDate outlookDate;
    @Mock
    private DateTimeFormatter dateTimeFormatter;
    @Mock
    private I18nHelper.BeanFactory i18n;
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    private Date date15jul2002;

    @Before
    public void setUp() throws Exception
    {
        date15jul2002 = dmyFormatter.parse(str15jul2002);

        when(dateTimeFormatterFactory.formatter()).thenReturn(dateTimeFormatter);
        when(dateTimeFormatter.forLoggedInUser()).thenReturn(dateTimeFormatter);

        when(applicationProperties.getDefaultBackedString("jira.lf.date.complete")).thenReturn("dd/MMM/yy h:mm a");
        when(applicationProperties.getDefaultBackedString("jira.lf.date.dmy")).thenReturn("dd/MMM/yy");
        when(applicationProperties.getDefaultBackedString("jira.lf.date.time")).thenReturn("h:mm a");
        when(applicationProperties.getOption("jira.lf.date.relativize")).thenReturn(true);

        outlookDate = new OutlookDate(Locale.ENGLISH, applicationProperties, i18n, dateTimeFormatterFactory);

    }



    @Test
    public void testFormatDMY() throws ParseException
    {

        when(expectWithStyle(DateTimeStyle.DATE).format(date15jul2002)).thenReturn(str15jul2002);
        Assert.assertEquals(str15jul2002, outlookDate.formatDMY(date15jul2002));
        Assert.assertEquals(str15jul2002, outlookDate.formatDMY(new Timestamp(date15jul2002.getTime())));
    }


    @Test
    public void testParseDatePickerDate() throws Exception
    {

        when(expectWithStyle(DateTimeStyle.DATE_PICKER).parse("04/Jul/07 12:16 PM")).thenReturn(date15jul2002);
        assertThat(outlookDate.parseDatePicker("04/Jul/07 12:16 PM"), CoreMatchers.equalTo(date15jul2002));
    }

    @Test
    public void testParseDate() throws ParseException
    {
        final Date date = new DateTime(2000, 1, 1, 11, 23, 0, 0).toDate();
        final String dateStr = "1/Jan/00 11:23 AM";
        expectParse(DateTimeStyle.COMPLETE, dateStr, date);

        Assert.assertEquals(date, outlookDate.parseCompleteDateTime(dateStr));
    }

    /**
     * This method is used to test the performace of the testDaysAgo method. It is run
     * manually rather than part of the automated test suite.
     */
    public void _testDaysAgoPerf()
    {
        for (int i = 0; i < 100000; i++)
        {
            testDaysAgo();
        }
    }
    @Test
    public void testDaysAgo()
    {
        //-------- Todays Dates ----------//
        //create a date at 5am
        final GregorianCalendar todayFiveAm = new GregorianCalendar(2003, 7, 21, 5, 0);
        final Date todayFiveAmDate = todayFiveAm.getTime();

        //create a date at 11pm
        final GregorianCalendar todayElevenPm = new GregorianCalendar(2003, 7, 21, 23, 0);
        final Date todayElevenPmDate = todayElevenPm.getTime();

        //-------- Tomorrow's Dates ----------//
        //create a date tomorrow at 5am
        final GregorianCalendar tomorrowFiveAm = new GregorianCalendar(2003, 7, 22, 5, 0);
        final Date tomorrowFiveAmDate = tomorrowFiveAm.getTime();

        //create a date tomorrow at 11pm
        final GregorianCalendar tomorrowElevenPm = new GregorianCalendar(2003, 7, 22, 23, 0);
        final Date tomorrowElevenPmDate = tomorrowElevenPm.getTime();

        //-------- Yesterday's Dates ----------//
        //create a date yesterday at 10pm
        final GregorianCalendar yesterdayTenPm = new GregorianCalendar(2003, 7, 20, 22, 0);
        final Date yesterdayTenPmDate = yesterdayTenPm.getTime();

        //both test today vs yesterday, so should both return one day
        Assert.assertEquals(OutlookDate.TODAY, outlookDate.daysAgo(todayFiveAmDate, todayElevenPmDate));
        Assert.assertEquals(OutlookDate.TODAY, outlookDate.daysAgo(todayElevenPmDate, todayFiveAmDate));

        Assert.assertEquals(OutlookDate.YESTERDAY, outlookDate.daysAgo(yesterdayTenPmDate, todayElevenPmDate));
        Assert.assertEquals(OutlookDate.YESTERDAY, outlookDate.daysAgo(yesterdayTenPmDate, todayFiveAmDate));

        Assert.assertEquals(OutlookDate.THIS_WEEK, outlookDate.daysAgo(yesterdayTenPmDate, tomorrowElevenPmDate));
        Assert.assertEquals(OutlookDate.THIS_WEEK, outlookDate.daysAgo(yesterdayTenPmDate, tomorrowFiveAmDate));
    }

    @Test
    public void testSmartFormatter() throws Exception
    {
        final int year = 2008;
        final int month = 1;
        // centre point
        final Date today = new GregorianCalendar(year, month, 15, 3, 4, 5).getTime();

        assertSmartKeyNull(today, year, month, 1);
        assertSmartKeyNull(today, year, month, 2);
        assertSmartKeyNull(today, year, month, 3);
        assertSmartKeyNull(today, year, month, 4);
        assertSmartKeyNull(today, year, month, 5);
        assertSmartKeyNull(today, year, month, 6);
        assertSmartKeyNull(today, year, month, 7);
        assertSmartKeyNull(today, year, month, 8);
        assertSmartKeyNull(today, year, month, 9);
        assertSmartKeyNull(today, year, month, 10);
        assertSmartKeyNull(today, year, month, 11);
        assertSmartKeyNull(today, year, month, 12);
        assertSmartKeyNull(today, year, month, 13);
        assertSmartKeyEquals(today, year, month, 14, "common.concepts.yesterday");
        assertSmartKeyEquals(today, year, month, 15, "common.concepts.today");
        assertSmartKeyEquals(today, year, month, 16, "common.concepts.tomorrow");
        assertSmartKeyNull(today, year, month, 17);
        assertSmartKeyNull(today, year, month, 18);
        assertSmartKeyNull(today, year, month, 19);
        assertSmartKeyNull(today, year, month, 20);
        assertSmartKeyNull(today, year, month, 21);
        assertSmartKeyNull(today, year, month, 22);
        assertSmartKeyNull(today, year, month, 23);
        assertSmartKeyNull(today, year, month, 24);
        assertSmartKeyNull(today, year, month, 25);
        assertSmartKeyNull(today, year, month, 26);
        assertSmartKeyNull(today, year, month, 27);
        assertSmartKeyNull(today, year, month, 28);

        // this is in March, we are just smartasses
        assertSmartKeyNull(today, year, month, 29);
        assertSmartKeyNull(today, year, month, 30);
        assertSmartKeyNull(today, year, month, 31);
        assertSmartKeyNull(today, year, month, 32);
    }

    /**
     * In St Louis (where our builds run) Daylight Savings Time started on Sunday, March 9, 2008 at 2:00 AM local standard time
     * Note: in other time zones, this shouldn't fail - it just doesn't check DST.
     */
    @Test
    public void testSmartFormatterDST1()
    {
        final int year = 2008;
        final int month = 2; // 0-based months
        // centre point
        final Date today = new GregorianCalendar(year, month, 14, 3, 4, 5).getTime();

        assertSmartKeyNull(today, year, month, 1);
        assertSmartKeyNull(today, year, month, 2);
        assertSmartKeyNull(today, year, month, 3);
        assertSmartKeyNull(today, year, month, 4);
        assertSmartKeyNull(today, year, month, 5);
        assertSmartKeyNull(today, year, month, 6);
        assertSmartKeyNull(today, year, month, 7);
        assertSmartKeyNull(today, year, month, 8);
        assertSmartKeyNull(today, year, month, 9);
        assertSmartKeyNull(today, year, month, 10);
        assertSmartKeyNull(today, year, month, 11);
        assertSmartKeyNull(today, year, month, 12);
        assertSmartKeyEquals(today, year, month, 13, "common.concepts.yesterday");
        assertSmartKeyEquals(today, year, month, 14, "common.concepts.today");
        assertSmartKeyEquals(today, year, month, 15, "common.concepts.tomorrow");
        assertSmartKeyNull(today, year, month, 16);
        assertSmartKeyNull(today, year, month, 17);
        assertSmartKeyNull(today, year, month, 18);
        assertSmartKeyNull(today, year, month, 19);
        assertSmartKeyNull(today, year, month, 20);
        assertSmartKeyNull(today, year, month, 21);
        assertSmartKeyNull(today, year, month, 22);
        assertSmartKeyNull(today, year, month, 23);
        assertSmartKeyNull(today, year, month, 24);
        assertSmartKeyNull(today, year, month, 25);
        assertSmartKeyNull(today, year, month, 26);
        assertSmartKeyNull(today, year, month, 27);
        assertSmartKeyNull(today, year, month, 28);
        assertSmartKeyNull(today, year, month, 29);
        assertSmartKeyNull(today, year, month, 30);
        assertSmartKeyNull(today, year, month, 31);
    }

    /**
     * In St Louis (where our builds run) Daylight Savings Time started on Sunday, November 2, 2008 at 2:00 AM local daylight time
     * Note: in other time zones, this shouldn't fail - it just doesn't check DST.
     */
    @Test
    public void testSmartFormatterDST2()
    {
        final int year = 2008;
        final int month = 10; // 0-based months
        // centre point
        final Date today = new GregorianCalendar(year, month, 1, 3, 4, 5).getTime();

        assertSmartKeyNull(today, year, month - 1, 24);
        assertSmartKeyNull(today, year, month - 1, 25);
        assertSmartKeyNull(today, year, month - 1, 26);
        assertSmartKeyNull(today, year, month - 1, 27);
        assertSmartKeyNull(today, year, month - 1, 28);
        assertSmartKeyNull(today, year, month - 1, 29);
        assertSmartKeyNull(today, year, month - 1, 30);
        assertSmartKeyEquals(today, year, month - 1, 31, "common.concepts.yesterday");
        assertSmartKeyEquals(today, year, month, 1, "common.concepts.today");
        assertSmartKeyEquals(today, year, month, 2, "common.concepts.tomorrow");
        assertSmartKeyNull(today, year, month, 3);
        assertSmartKeyNull(today, year, month, 4);
        assertSmartKeyNull(today, year, month, 5);
        assertSmartKeyNull(today, year, month, 6);
        assertSmartKeyNull(today, year, month, 7);
        assertSmartKeyNull(today, year, month, 8);
        assertSmartKeyNull(today, year, month, 9);
    }

    @Test
    public void testFormatSmartShowTime() throws Exception
    {
        final JodaFormatterCache jodaFormatterCache = new JodaFormatterCache();

        final DateTimeFormatterServiceProvider dateTimeFormatterServiceProvider = createDateTimeFormatterServiceProvider(applicationProperties, jiraAuthenticationContext, i18n);

        final Date today = new GregorianCalendar(2008, 1, 15, 3, 4, 5).getTime();
        final DateTimeRelativeDatesAlwaysWithTime alwaysWithTime = new DateTimeRelativeDatesAlwaysWithTime(dateTimeFormatterServiceProvider, applicationProperties, timeZoneInfoResolver, jiraAuthenticationContext, jodaFormatterCache, new ConstantClock(today));

        for (int i = 1; i <= 28; i++)
        {
            assertTimePresent(alwaysWithTime, 2008, 1, i);
        }
    }

    @Test
    public void testFormatSmartHideTime() throws Exception
    {
        final JodaFormatterCache jodaFormatterCache = new JodaFormatterCache();

        final DateTimeFormatterServiceProvider dateTimeFormatterServiceProvider = createDateTimeFormatterServiceProvider(applicationProperties, jiraAuthenticationContext, i18n);
        final Date today = new GregorianCalendar(2008, 1, 15, 3, 4, 5).getTime();
        final DateTimeRelativeNoTimeFormatter noTimeFormatter = new DateTimeRelativeNoTimeFormatter(dateTimeFormatterServiceProvider, applicationProperties, timeZoneInfoResolver, jiraAuthenticationContext, jodaFormatterCache, new ConstantClock(today));
        for (int i = 1; i <= 28; i++)
        {
            assertTimeNotPresent(noTimeFormatter, 2008, 1, i);
        }
    }

    @Test
    public void testFormatSmartShowTimeOnlyOnDays() throws Exception
    {
        final JodaFormatterCache jodaFormatterCache = new JodaFormatterCache();

        final DateTimeFormatterServiceProvider dateTimeFormatterServiceProvider = createDateTimeFormatterServiceProvider(applicationProperties, jiraAuthenticationContext, i18n);

        final Date today = new GregorianCalendar(2008, 1, 15, 3, 4, 5).getTime();
        final DateTimeRelativeDatesWithTimeFormatter dateTimeRelativeDatesWithTimeFormatter = new DateTimeRelativeDatesWithTimeFormatter(dateTimeFormatterServiceProvider, applicationProperties, timeZoneInfoResolver, jiraAuthenticationContext, jodaFormatterCache, new ConstantClock(today));

        for (int i = 1; i <= 13; i++)
        {
            assertTimeNotPresent(dateTimeRelativeDatesWithTimeFormatter, 2008, 1, i);
        }
        for (int i = 14; i <= 16; i++)
        {
            assertTimePresent(dateTimeRelativeDatesWithTimeFormatter, 2008, 1, i);
        }
        for (int i = 17; i <= 28; i++)
        {
            assertTimeNotPresent(dateTimeRelativeDatesWithTimeFormatter, 2008, 1, i);
        }
        assertTimeNotPresent(dateTimeRelativeDatesWithTimeFormatter, 2008, 2, 1);
        assertTimeNotPresent(dateTimeRelativeDatesWithTimeFormatter, 2009, 1, 1);
    }

    private DateTimeFormatterServiceProvider createDateTimeFormatterServiceProvider(final ApplicationProperties applicationProperties, final JiraAuthenticationContext jiraAuthenticationContext, final I18nHelper.BeanFactory i18n)
    {
        return new DateTimeFormatterServiceProvider()
            {
                @Override
                public String getDefaultBackedString(String key)
                {
                    return applicationProperties.getDefaultBackedString(key);
                }

                @Override
                public String getUnescapedText(String key)
                {
                    return i18n.getInstance(jiraAuthenticationContext.getLoggedInUser()).getUnescapedText(key);
                }

                @Override
                public String getText(String key, Object... parameters)
                {
                   return "key" + Arrays.toString(parameters);
                }
            };
    }

    private void assertTimePresent(DateTimeFormatStrategy dateTimeFormatStrategy, final int year, final int month, final int day)
    {
        final Date dayStart = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        assertThat(dateTimeFormatStrategy.format(new DateTime(dayStart), Locale.getDefault()),CoreMatchers.containsString("12:00 AM"));
        final Date dayEnd = new GregorianCalendar(year, month, day, 23, 59, 59).getTime();
        assertThat(dateTimeFormatStrategy.format(new DateTime(dayEnd), Locale.getDefault()),CoreMatchers.containsString("11:59 PM"));
    }

    private void assertTimeNotPresent(DateTimeFormatStrategy dateTimeFormatStrategy, final int year, final int month, final int day)
    {
        final Date dayStart = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        assertThat(dateTimeFormatStrategy.format(new DateTime(dayStart), Locale.getDefault()),
                CoreMatchers.not(CoreMatchers.containsString("12:00 AM")));
        final Date dayEnd = new GregorianCalendar(year, month, day, 23, 59, 59).getTime();
        assertThat(dateTimeFormatStrategy.format(new DateTime(dayEnd), Locale.getDefault()),
                CoreMatchers.not(CoreMatchers.containsString("11:59 PM")));

    }

    private void assertSmartKeyNull(final Date today, final int year, final int month, final int day)
    {
        // note: time of day should not make a difference
        final Date dayStart = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        Assert.assertNull(AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayStart), new DateTime(today)));
        final Date dayEnd = new GregorianCalendar(year, month, day, 23, 59, 59).getTime();
        Assert.assertNull(AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayEnd), new DateTime(today)));
    }

    private void assertSmartKeyEquals(final Date today, final int year, final int month, final int day, final String key)
    {
        // note: time of day should not make a difference
        final Date dayStart = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        Assert.assertEquals(key, AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayStart), new DateTime(today)));
        final Date dayEnd = new GregorianCalendar(year, month, day, 23, 59, 59).getTime();
        Assert.assertEquals(key, AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayEnd), new DateTime(today)));
    }

    @Test
    public void testDaysAgoAcrossNewYear()
    {

        //create a date at 1am
        final GregorianCalendar todayOneAm = new GregorianCalendar(2003, 1, 1, 1, 0);
        final Date todayFiveAmDate = todayOneAm.getTime();

        //create a date yesterday at 11pm
        final GregorianCalendar yesterdayTenPm = new GregorianCalendar(2002, 12, 31, 23, 0);
        final Date yesterdayTenPmDate = yesterdayTenPm.getTime();

        //test today vs yesterday, so should return one day
        Assert.assertEquals(1, outlookDate.daysAgo(yesterdayTenPmDate, todayFiveAmDate));
    }

    @Test
    public void testIsDatePickerDateDateWhenParseSucceeded()
    {
        final String date = "15/May/2000";
        expectParse(DateTimeStyle.DATE_PICKER, date, new Date(123));
        Assert.assertTrue("Expected date to be in date picker format",outlookDate.isDatePickerDate(date));
    }
    @Test
    public void testIsDatePickerFailsDateDateWhenNullArgument()
    {
        Assert.assertFalse("Expected that null is not date picker format",outlookDate.isDatePickerDate(null));
    }

    @Test
    public void testIsDatePickerFailsWhenExceptionIsThrown()
    {

        final String date = "15/May/2000";
        when(expectWithStyle(DateTimeStyle.DATE_PICKER).parse(date)).thenThrow(new IllegalArgumentException("Error"));
        final DateTimeFormatter mock = expectWithStyle(DateTimeStyle.DATE_TIME_PICKER);
        //we have to have this fancy invocations because in other ways exception will be thrown here from previous mock
        doThrow(new IllegalArgumentException("Error")).when(mock).parse(date);

        Assert.assertFalse("Expected date not to be in date picker format", outlookDate.isDatePickerDate(date));
    }

    @Test
    public void testFormatIso8601() throws ParseException
    {
        when(expectWithStyle(DateTimeStyle.ISO_8601_DATE_TIME).format(date15jul2002)).thenReturn("2002-07-15T00-00");
        Assert.assertEquals("2002-07-15T00-00", outlookDate.formatIso8601(date15jul2002));
        Assert.assertEquals("2002-07-15T00-00", outlookDate.formatIso8601(new Timestamp(date15jul2002.getTime())));
    }

    @Test
    public void testFormatIso8601Date() throws ParseException
    {
        when(expectWithStyle(DateTimeStyle.ISO_8601_DATE).format(date15jul2002)).thenReturn("2002-07-15");
        Assert.assertEquals("2002-07-15", outlookDate.formatIso8601Date(date15jul2002));
        Assert.assertEquals("2002-07-15", outlookDate.formatIso8601Date(new Timestamp(date15jul2002.getTime())));
    }

    @Test
    public void testFormatRss() throws ParseException
    {
        final String expected = "Mon, 15 Jul 2002 00:00:00 who cares" ;
        when(expectWithStyle(DateTimeStyle.RSS_RFC822_DATE_TIME).format(date15jul2002)).thenReturn(expected);
        Assert.assertEquals(expected, outlookDate.formatRss(date15jul2002));
        Assert.assertEquals(expected, outlookDate.formatRss(new Timestamp(date15jul2002.getTime())));
    }

    @Test
    public void testFormatRssRfc822() throws ParseException
    {
        final String expected = "Mon, 15 Jul 2002 00:00:00 whatever" ;
        when(expectWithStyle(DateTimeStyle.RSS_RFC822_DATE_TIME).format(date15jul2002)).thenReturn(expected);
        Assert.assertEquals(expected, outlookDate.formatRssRfc822(date15jul2002));
        Assert.assertEquals(expected, outlookDate.formatRssRfc822(new Timestamp(date15jul2002.getTime())));
    }

    /*
     Test the MailDateFormat to see if it can parse differing formats.  This is used by OutlookDate
     */
    @Test
    public void testMailDateFormat()
    {
        Date dt;
        ParsePosition pp;
        final MailDateFormat mailDateFormat = new MailDateFormat();

        pp = new ParsePosition(0);
        dt = mailDateFormat.parse("Wed, 22 Aug 2007 10:00:10 +1000 (GMT+10:00)", pp);
        Assert.assertEquals(-1, pp.getErrorIndex());
        Assert.assertNotNull(dt);

        pp = new ParsePosition(0);
        dt = mailDateFormat.parse("Wed, 22 Aug 2007 10:00:10 +1000", pp);
        Assert.assertEquals(-1, pp.getErrorIndex());
        Assert.assertNotNull(dt);

        // ok it looks good can it parse a SimpleDate equivalent
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        final String dateStr = sdf.format(dt);

        pp = new ParsePosition(0);
        dt = mailDateFormat.parse(dateStr, pp);
        Assert.assertEquals(-1, pp.getErrorIndex());
        Assert.assertNotNull(dt);

    }

    private DateTimeFormatter expectParse(final DateTimeStyle style, final String dateStr, final Date date)
    {
        when(dateTimeFormatter.withStyle(style)).thenReturn(dateTimeFormatter);
        when(dateTimeFormatter.parse(dateStr)).thenReturn(date);
        return dateTimeFormatter;
    }
    private DateTimeFormatter expectWithStyle(final DateTimeStyle style)
    {
        when(dateTimeFormatter.withStyle(style)).thenReturn(dateTimeFormatter);
        return dateTimeFormatter;
    }
}
