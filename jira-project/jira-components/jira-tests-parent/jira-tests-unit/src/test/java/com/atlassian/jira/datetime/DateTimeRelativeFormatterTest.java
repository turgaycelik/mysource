package com.atlassian.jira.datetime;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;

import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for TZConverter.
 */
public class DateTimeRelativeFormatterTest
{
    /**
     * Sydney time zone.
     */
    private static final DateTimeZone SYDNEY_TZ = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Australia/Sydney"));

    /**
     * Amsterdam time zone.
     */
    private static final DateTimeZone AMSTERDAM_TZ = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));

    /**
     * Sao Paulo time zone.
     */
    private static final DateTimeZone SAO_PAULO_TZ = DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

    /**
     * Locale used in test.
     */
    private static final Locale userLocale = Locale.UK;

    /**
     * This is the time that we use in most tests. It has time zone Sydney, because that is the time zone of the
     * server.
     */
    private final DateTime twoFiftyThreeAM_Sydney = new DateTime(2010, 11, 23, 2, 53, 0, 0, SYDNEY_TZ);

    /**
     * This is the same time as #twoFiftyThreeAM_Sydney, but shifted into the Amsterdam time zone.
     */
    private final DateTime twoFiftyThreeAM_Sydney_in_Amsterdam_TZ = new DateTime(twoFiftyThreeAM_Sydney, AMSTERDAM_TZ);

    /**
     * The class being tested.
     */
    private DateTimeRelativeFormatter formatterUnderTest;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private DateTimeFormatterServiceProvider serviceProvider;

    @Mock
    private Clock clock;

    @Test
    public void issueCreatedAMinuteAgoShouldDisplayAsToday() throws Exception
    {
        final DateTime nov22InAmsterdam = new DateTime(2010, 11, 22, 16, 54, 0, 0, AMSTERDAM_TZ);
        setClockTo(nov22InAmsterdam);

        // now = Nov 22 in amsterdam, so Nov 22 is today
        String formatted = formatterUnderTest.format(twoFiftyThreeAM_Sydney_in_Amsterdam_TZ, userLocale);
        assertThat(formatted, equalTo("Today 04:53 PM"));
    }

    @Test
    public void issueCreatedYesterdayButLessThan24HoursAgoShouldDisplayAsYesterday() throws Exception
    {
        final DateTime nov23InAmsterdam = new DateTime(2010, 11, 23, 2, 1, 0, 0, AMSTERDAM_TZ);
        setClockTo(nov23InAmsterdam);

        // now = Nov 23 in amsterdam, so Nov 22 was yesterday
        String formatted = formatterUnderTest.format(twoFiftyThreeAM_Sydney_in_Amsterdam_TZ, userLocale);
        assertThat(formatted, equalTo("Yesterday 04:53 PM"));
    }

    @Test
    public void issueCreatedYesterdayAndOver24HoursAgoShouldDisplayAsYesterday() throws Exception
    {
        final DateTime nov23InAmsterdam = new DateTime(2010, 11, 23, 17, 1, 2, 3, AMSTERDAM_TZ);
        setClockTo(nov23InAmsterdam);

        // now = Nov 23 in amsterdam, so Nov 22 was yesterday
        String formatted = formatterUnderTest.format(twoFiftyThreeAM_Sydney_in_Amsterdam_TZ, userLocale);
        assertThat(formatted, equalTo("Yesterday 04:53 PM"));
    }

    @Test
    public void dstGapShouldBeHandledCorrectly() throws Exception
    {
        final DateTime sydneyTime = new DateTime(2010, 10, 17, 1, 0, 0, 0, SYDNEY_TZ);
        final DateTime sampaTime = new DateTime(sydneyTime, SAO_PAULO_TZ);

        // this is the day before a DST interval change thing
        final DateTime oct16InSaoPaulo = new DateTime(2010, 10, 16, 13, 0, 0, 0, SAO_PAULO_TZ);
        setClockTo(oct16InSaoPaulo);

        String formatted = formatterUnderTest.format(sampaTime, userLocale);
        assertThat(formatted, equalTo("Today 11:00 AM"));
    }

    @Before
    public void setUp()
    {
        EasyMockAnnotations.initMocks(this);

        prepareServiceProviderMock();
        prepareApplicationPropertiesMock();
        formatterUnderTest = new DateTimeRelativeFormatter(serviceProvider, new JodaFormatterSupplierStub(), applicationProperties, clock);
    }

    private void setClockTo(DateTime currentDate)
    {
        expect(clock.getCurrentDate()).andReturn(currentDate.toDate()).once();
        replay(clock);
    }

    private void prepareServiceProviderMock()
    {
        // the supported date formats
        final ImmutableMap<String, String> dateFormats = ImmutableMap.of(
                APKeys.JIRA_LF_DATE_COMPLETE, "dd/MMM/yy hh:mm a", // complete
                APKeys.JIRA_LF_DATE_DAY, "EEEE hh:mm a",           // day of week + time
                APKeys.JIRA_LF_DATE_TIME, "hh:mm a"                // just time
        );

        expect(serviceProvider.getUnescapedText("common.concepts.today")).andStubReturn("Today {0}");
        expect(serviceProvider.getUnescapedText("common.concepts.yesterday")).andStubReturn("Yesterday {0}");
        for (Map.Entry<String, String> dateFormat : dateFormats.entrySet())
        {
            String propertyKey = dateFormat.getKey();
            String pattern = dateFormat.getValue();

            expect(serviceProvider.getDefaultBackedString(propertyKey)).andStubReturn(pattern);
        }

        replay(serviceProvider);
    }

    private void prepareApplicationPropertiesMock()
    {
        expect(applicationProperties.getOption(APKeys.JIRA_LF_DATE_RELATIVE)).andReturn(true);
        replay(applicationProperties);
    }
}
