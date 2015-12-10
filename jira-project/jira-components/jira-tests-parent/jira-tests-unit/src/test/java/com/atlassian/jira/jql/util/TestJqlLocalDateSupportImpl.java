package com.atlassian.jira.jql.util;

import java.util.Calendar;
import java.util.TimeZone;

import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.ConstantClock;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 *
 * @since v4.4
 */
public class TestJqlLocalDateSupportImpl extends MockControllerTestCase
{
    private TimeZoneManager timeZoneManager;

    @Before
    public void setUp() throws Exception
    {
        timeZoneManager = createMock(TimeZoneManager.class);
    }

    @Test
    public void testNullConstructorArguments() throws Exception
    {
       try
        {
            replay();
            new JqlLocalDateSupportImpl(null, timeZoneManager);
            fail("Expecting an exception.");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    @Test
    public void testParseDateIllegalArgument() throws Exception
    {
        final JqlLocalDateSupport support = new JqlLocalDateSupportImpl(timeZoneManager);
        replay();
        try
        {
            support.convertToLocalDate((Long) null);
            fail("Expecting an exception.");
        }
        catch (IllegalArgumentException ignored)
        {
        }

        try
        {
            support.convertToLocalDate((String) null);
            fail("Expecting an exception.");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    @Test
    public void testConvertDurationToLocalDate() throws Exception
    {
        final String validDuration = "-2d";
        expect(timeZoneManager.getLoggedInUserTimeZone()).andStubReturn(TimeZone.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2011);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DAY_OF_MONTH, 11);

        final LocalDate expectedLocalDate = new LocalDate(2011, 5, 9);
        JqlLocalDateSupportImpl jqlLocalDateSupport = new JqlLocalDateSupportImpl(new ConstantClock(calendar.getTime()), timeZoneManager);
        replay();
        assertEquals(expectedLocalDate, jqlLocalDateSupport.convertToLocalDate(validDuration));
    }

    @Test
    public void testConvertDateStringToLocalDate() throws Exception
    {
        final String dateString = "2011/5/1";
        expect(timeZoneManager.getLoggedInUserTimeZone()).andStubReturn(TimeZone.getDefault());

        final LocalDate expectedLocalDate = new LocalDate(2011, 5, 1);
        JqlLocalDateSupportImpl jqlLocalDateSupport = new JqlLocalDateSupportImpl(timeZoneManager);
        replay();
        assertEquals(expectedLocalDate, jqlLocalDateSupport.convertToLocalDate(dateString));
    }

    @Test
    public void testConvertLongToLocalDate() throws Exception
    {
       expect(timeZoneManager.getLoggedInUserTimeZone()).andStubReturn(TimeZone.getDefault());
       Calendar calendar = Calendar.getInstance();
       calendar.set(Calendar.YEAR, 2011);
       calendar.set(Calendar.MONTH, 4);
       calendar.set(Calendar.DAY_OF_MONTH, 11);
       final LocalDate expectedLocalDate = new LocalDate(2011, 5, 11);
       JqlLocalDateSupport jqlLocalDateSupport = new JqlLocalDateSupportImpl(timeZoneManager);
       replay();
       assertEquals(expectedLocalDate, jqlLocalDateSupport.convertToLocalDate(calendar.getTimeInMillis()));
    }

    @Test
    public void testGetIndexedLocalDateValue() throws Exception
    {
       LocalDate localDate = new LocalDate(2011, 5, 11);

       JqlLocalDateSupport jqlLocalDateSupport = new JqlLocalDateSupportImpl(timeZoneManager);
       replay();
       assertEquals("20110511", jqlLocalDateSupport.getIndexedValue(localDate));
    }

    @Test
    public void testValidateDurationString() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andStubReturn(TimeZone.getDefault());
        JqlLocalDateSupport jqlLocalDateSupport = new JqlLocalDateSupportImpl(timeZoneManager);
        replay();
        assertTrue(jqlLocalDateSupport.validate("-2d"));
    }

    public void testValidateStringIsNull() throws Exception
    {
       JqlLocalDateSupport jqlLocalDateSupport = new JqlLocalDateSupportImpl(timeZoneManager);
       replay();
       try
       {
          jqlLocalDateSupport.validate(null);
          fail("Expecting an exception.");
       }
       catch (IllegalArgumentException e)
       {

       }
    }

    @Test
    public void testValidateDateString() throws Exception
    {
        JqlLocalDateSupport jqlLocalDateSupport = new JqlLocalDateSupportImpl(timeZoneManager);
        replay();
        assertTrue(jqlLocalDateSupport.validate("2011/1/1"));
    }

    @Test
    public void testValidationDurationStringFails() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andStubReturn(TimeZone.getDefault());
        JqlLocalDateSupport jqlLocalDateSupport = new JqlLocalDateSupportImpl(timeZoneManager);
        replay();
        assertFalse(jqlLocalDateSupport.validate("-21312312wefsf"));
    }

    @Test
    public void testValidationDateStringFails() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andStubReturn(TimeZone.getDefault());
        JqlLocalDateSupport jqlLocalDateSupport = new JqlLocalDateSupportImpl(timeZoneManager);
        replay();
        assertFalse(jqlLocalDateSupport.validate("2011/20398329482/12"));
    }

    @Test
    public void testGetLocalDateString() throws Exception
    {
        LocalDate localDate = new LocalDate(1989, 11, 3);
        JqlLocalDateSupport jqlLocalDateSupport = new JqlLocalDateSupportImpl(timeZoneManager);
        replay();
        assertEquals("1989-11-3", jqlLocalDateSupport.getLocalDateString(localDate));
    }

    @Test
    public void testGetLocalDateStringIsNull() throws Exception
    {
       JqlLocalDateSupport jqlLocalDateSupport = new JqlLocalDateSupportImpl(timeZoneManager);
       replay();
       try
       {
          jqlLocalDateSupport.getLocalDateString(null);
          fail("Expecting an exception.");
       }
       catch (IllegalArgumentException e)
       {

       }
    }

}
