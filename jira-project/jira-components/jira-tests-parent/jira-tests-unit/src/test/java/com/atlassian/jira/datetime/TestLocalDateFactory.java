package com.atlassian.jira.datetime;

import java.util.Date;

import junit.framework.TestCase;

public class TestLocalDateFactory extends TestCase
{
    public void testFromDate()
    {
        assertEquals(new LocalDate(1970, 1, 1), LocalDateFactory.from(new Date(0)));
        assertEquals(new LocalDate(2010, 5, 30), LocalDateFactory.from(new Date(1275196032735L)));
        assertEquals(new LocalDate(1969, 8, 15), LocalDateFactory.from(new Date(-11991058346L)));
        assertEquals(new LocalDate(1, 1, 1), LocalDateFactory.from(new Date(-62135769600000L)));
        try
        {
            // 31/12/0001 BC
            LocalDateFactory.from(new Date(-62135856000000L));
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("LocalDate only handles the Common Era - no BC dates are allowed.", ex.getMessage());
        }
    }
}
