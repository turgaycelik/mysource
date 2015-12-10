package com.atlassian.jira.web.component.cron.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestCronDayOfWeekEntry
{
    @Test
    public void testOrdinal()
    {
        CronDayOfWeekEntry cronDayOfWeekEntry = new CronDayOfWeekEntry("2#2");
        assertTrue(cronDayOfWeekEntry.isValid());
        assertEquals("2", cronDayOfWeekEntry.getDayInMonthOrdinal());
    }

    @Test
    public void testDayOfWeekWithOrdinal()
    {
        CronDayOfWeekEntry cronDayOfWeekEntry = new CronDayOfWeekEntry("MON#2");
        assertTrue(cronDayOfWeekEntry.isValid());
        assertEquals("2", cronDayOfWeekEntry.getDayInMonthOrdinal());
    }

    @Test
    public void testDayOfWeekWithOrdinalLast()
    {
        CronDayOfWeekEntry cronDayOfWeekEntry = new CronDayOfWeekEntry("MONL");
        assertTrue(cronDayOfWeekEntry.isValid());
        assertEquals("L", cronDayOfWeekEntry.getDayInMonthOrdinal());

        cronDayOfWeekEntry = new CronDayOfWeekEntry("MONl");
        assertTrue(cronDayOfWeekEntry.isValid());
        assertEquals("L", cronDayOfWeekEntry.getDayInMonthOrdinal());

        cronDayOfWeekEntry = new CronDayOfWeekEntry("2L");
        assertTrue(cronDayOfWeekEntry.isValid());
        assertEquals("L", cronDayOfWeekEntry.getDayInMonthOrdinal());

        cronDayOfWeekEntry = new CronDayOfWeekEntry("2l");
        assertTrue(cronDayOfWeekEntry.isValid());
        assertEquals("L", cronDayOfWeekEntry.getDayInMonthOrdinal());
    }

    @Test
    public void testDayOfWeekWithInvalidOrdinals()
    {
        assertFalse(new CronDayOfWeekEntry("MON#5").isValid());
        assertFalse(new CronDayOfWeekEntry("MON#6").isValid());
        assertFalse(new CronDayOfWeekEntry("MON#7").isValid());
    }

    @Test
    public void testDayOfWeekSpecifed()
    {
        CronDayOfWeekEntry cronDayOfWeekEntry = new CronDayOfWeekEntry("MON,WED,FRI");
        assertTrue(cronDayOfWeekEntry.isValid());
        assertTrue(cronDayOfWeekEntry.isDaySpecified("MON"));
        assertTrue(cronDayOfWeekEntry.isDaySpecified("2"));
        assertFalse(cronDayOfWeekEntry.isDaySpecified("TUE"));
        assertFalse(cronDayOfWeekEntry.isDaySpecified("3"));
        assertTrue(cronDayOfWeekEntry.isDaySpecified("WED"));
        assertTrue(cronDayOfWeekEntry.isDaySpecified("4"));
        assertFalse(cronDayOfWeekEntry.isDaySpecified("THU"));
        assertFalse(cronDayOfWeekEntry.isDaySpecified("5"));
        assertTrue(cronDayOfWeekEntry.isDaySpecified("FRI"));
        assertTrue(cronDayOfWeekEntry.isDaySpecified("6"));
        assertFalse(cronDayOfWeekEntry.isDaySpecified("SAT"));
        assertFalse(cronDayOfWeekEntry.isDaySpecified("7"));
        assertFalse(cronDayOfWeekEntry.isDaySpecified("SUN"));
        assertFalse(cronDayOfWeekEntry.isDaySpecified("1"));
        assertNull(cronDayOfWeekEntry.getDayInMonthOrdinal());
    }

    @Test
    public void testValids()
    {
        assertTrue(new CronDayOfWeekEntry("?").isValid());
        assertTrue(new CronDayOfWeekEntry("*").isValid());
    }

    @Test
    public void testInvalids()
    {
        assertFalse(new CronDayOfWeekEntry("MON#MON").isValid());
        assertFalse(new CronDayOfWeekEntry("4#MON").isValid());
        assertFalse(new CronDayOfWeekEntry("9#1").isValid());
        assertFalse(new CronDayOfWeekEntry("#").isValid());
        assertFalse(new CronDayOfWeekEntry("##").isValid());
        assertFalse(new CronDayOfWeekEntry("0#1").isValid());
        assertFalse(new CronDayOfWeekEntry("#,MON").isValid());
        assertFalse(new CronDayOfWeekEntry("    ").isValid());
        assertFalse(new CronDayOfWeekEntry(null).isValid());
        assertFalse(new CronDayOfWeekEntry("").isValid());
        assertFalse(new CronDayOfWeekEntry("#?").isValid());
        assertFalse(new CronDayOfWeekEntry("MON-WED").isValid()); // We don't support this in the editor
        assertFalse(new CronDayOfWeekEntry("1-6").isValid()); // We don't support this in the editor
        assertFalse(new CronDayOfWeekEntry("%").isValid());
        assertFalse(new CronDayOfWeekEntry("MON#L").isValid());
        assertFalse(new CronDayOfWeekEntry("MON#l").isValid());
        assertFalse(new CronDayOfWeekEntry("2#l").isValid());
    }

    @Test
    public void testGetDaysAsNumbers()
    {
        String monTueWed = "2,3,4";
        CronDayOfWeekEntry cronDayOfWeekEntry = new CronDayOfWeekEntry(monTueWed);
        assertEquals(monTueWed, cronDayOfWeekEntry.getDaysAsNumbers());
        String monTueWedWords = "MON,TUE,WED";
        cronDayOfWeekEntry = new CronDayOfWeekEntry(monTueWedWords);
        assertEquals(monTueWed, cronDayOfWeekEntry.getDaysAsNumbers());
    }

}
