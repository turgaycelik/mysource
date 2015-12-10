package com.atlassian.jira.datetime;

import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

/**
 * Test for DateTimeFormatterImpl.
 *
 * @since 4.4
 */
public class DateTimeFormatterImplTest
{
    /**
     * Test date -> 2011-04-28T12:43:34.618+1000
     */
    private static final Date APR_28 = new Date(1303958614618L);

    private static final DateTimeZone JIRA_TZ = DateTimeZone.forID("Australia/Sydney");
    private static final Locale JIRA_LOCALE = Locale.ENGLISH;

    private static final DateTimeZone USER_TZ = DateTimeZone.forID("Europe/Lisbon");
    private static final Locale USER_LOCALE = new Locale("pt");

    private DateTimeFormatterFactoryStub dateTimeFormatterFactory;

    @Test
    public void callingForLoggedInUserClearsOverrideZoneAndLocale() throws Exception
    {
        final Date apr28 = APR_28;

        // make sure the overrides are not used for formatting, but rather the current user's tz/locale
        DateTimeFormatter formatter = dateTimeFormatterFactory.formatter()
                .withZone(JIRA_TZ.toTimeZone())
                .withLocale(JIRA_LOCALE)
                .forLoggedInUser();
        //Portuguese Locale used to return "Abr" for month before < Java 1.8, after returns "abr" (lower case)
        assertThat(formatter.format(apr28), equalToIgnoringCase("28/Abr/11 03:43 AM"));
    }

    @Test
    public void callingForUserClearsForLoggedInUserFlag() throws Exception
    {
        DateTimeFormatter formatter = dateTimeFormatterFactory.formatter().forLoggedInUser().forUser(null);
        assertThat(formatter.format(APR_28), equalTo("28/Apr/11 12:43 PM"));
    }

    @Before
    public void setUpFactoryStub() throws Exception
    {
        dateTimeFormatterFactory = new DateTimeFormatterFactoryStub();
        dateTimeFormatterFactory.relativeDates(false);
        dateTimeFormatterFactory.userTimeZone(USER_TZ);
        dateTimeFormatterFactory.jiraTimeZone(JIRA_TZ);
        dateTimeFormatterFactory.userLocale(USER_LOCALE);
        dateTimeFormatterFactory.jiraLocale(JIRA_LOCALE);
    }
}
