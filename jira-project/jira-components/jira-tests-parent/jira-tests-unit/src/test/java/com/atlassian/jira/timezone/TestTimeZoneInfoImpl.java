package com.atlassian.jira.timezone;

import java.util.TimeZone;

import com.atlassian.jira.mock.i18n.MockI18nHelper;

import junit.framework.TestCase;

public class TestTimeZoneInfoImpl extends TestCase
{
    public void testGetCityForDefaultTimeZone() throws Exception
    {
        TimeZone actualTimeZone = TimeZone.getTimeZone("Australia/Sydney");
        TimeZoneInfoImpl timeZoneInfo = new TimeZoneInfoImpl(TimeZoneService.JIRA, "Eastern Standard Time", actualTimeZone, new MockI18nHelper(), TimeZoneService.JIRA);

        assertEquals("timezone.zone.australia.sydney", timeZoneInfo.getCity());
    }
}
