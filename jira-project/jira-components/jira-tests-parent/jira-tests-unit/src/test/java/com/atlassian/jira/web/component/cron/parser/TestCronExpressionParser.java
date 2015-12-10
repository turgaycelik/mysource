package com.atlassian.jira.web.component.cron.parser;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.web.component.cron.CronEditorBean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestCronExpressionParser
{

    @Test
    public void testDailySpecOncePerDay()
    {
        String twoAmOncePerDay = "0 0 2 * * *";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(twoAmOncePerDay);
        assertTrue(cronExpressionParser.isDailyMode());
        assertFalse(cronExpressionParser.isDayPerWeekMode());
        assertFalse(cronExpressionParser.isDaysPerMonthMode());
        assertTrue((cronExpressionParser.isValidForEditor()));
        assertEquals(2, cronExpressionParser.getHoursEntry().getRunOnce());
        assertEquals(JiraUtils.AM, cronExpressionParser.getHoursEntry().getRunOnceMeridian());
        assertEquals(-1, cronExpressionParser.getHoursEntry().getFrom());
        assertNull(cronExpressionParser.getHoursEntry().getFromMeridian());
        assertEquals(-1, cronExpressionParser.getHoursEntry().getTo());
        assertNull(cronExpressionParser.getHoursEntry().getToMeridian());
        assertEquals(0, cronExpressionParser.getIncrementInMinutes());
    }

    @Test
    public void testDailySpecEvery15MinutesForOneHourOnly()
    {
        String twoAmEvery15Minutes = "0 0/15 2 * * *";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(twoAmEvery15Minutes);
        assertFalse(cronExpressionParser.isDailyMode());
        assertFalse(cronExpressionParser.isDayPerWeekMode());
        assertFalse(cronExpressionParser.isDaysPerMonthMode());
        assertTrue(cronExpressionParser.isAdvancedMode());

        // because the cron expression is too complex we dont need to know if its a range because the user can only edit
        // via the advanced tab in the ui.
        assertFalse(cronExpressionParser.isValidForEditor());

        final CronEditorBean cronEditorBean = cronExpressionParser.getCronEditorBean();
        assertTrue(cronEditorBean.isAdvancedMode());
        assertFalse(cronEditorBean.isDailyMode());
        assertFalse(cronEditorBean.isDayOfWeekOfMonth());
        assertFalse(cronEditorBean.isDayPerWeekMode());
        assertFalse(cronEditorBean.isDaysPerMonthMode());
    }

    @Test
    public void testDailySpecEvery15MinutesForOneHourOnlyUsingRange()
    {
        String twoAmEvery15Minutes = "0 0/15 8-8 * * *";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(twoAmEvery15Minutes);
        assertTrue(cronExpressionParser.isDailyMode());
        assertFalse(cronExpressionParser.isDayPerWeekMode());
        assertFalse(cronExpressionParser.isDaysPerMonthMode());
        assertFalse(cronExpressionParser.isAdvancedMode());
        assertTrue(cronExpressionParser.isValidForEditor());

        assertEquals(-1, cronExpressionParser.getHoursEntry().getRunOnce());
        assertNull(cronExpressionParser.getHoursEntry().getRunOnceMeridian());
        assertEquals(8, cronExpressionParser.getHoursEntry().getFrom());
        assertEquals(JiraUtils.AM, cronExpressionParser.getHoursEntry().getFromMeridian());
        assertEquals(9, cronExpressionParser.getHoursEntry().getTo());
        assertEquals(JiraUtils.AM, cronExpressionParser.getHoursEntry().getToMeridian());
        assertEquals(15, cronExpressionParser.getIncrementInMinutes());

        final CronEditorBean cronEditorBean = cronExpressionParser.getCronEditorBean();
        assertFalse(cronEditorBean.isAdvancedMode());
        assertTrue(cronEditorBean.isDailyMode());
        assertFalse(cronEditorBean.isDayOfWeekOfMonth());
        assertFalse(cronEditorBean.isDayPerWeekMode());
        assertFalse(cronEditorBean.isDaysPerMonthMode());
    }

    @Test
    public void testDailySpecEveryTwoHoursInRange()
    {
        String oneToFourEveryTwoHours = "0 0 1-4/2 ? * *";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(oneToFourEveryTwoHours);
        assertTrue(cronExpressionParser.isDailyMode());
        assertFalse(cronExpressionParser.isDayPerWeekMode());
        assertFalse(cronExpressionParser.isDaysPerMonthMode());
        assertEquals(120, cronExpressionParser.getIncrementInMinutes());
        assertEquals(-1, cronExpressionParser.getHoursEntry().getRunOnce());
        assertNull(cronExpressionParser.getHoursEntry().getRunOnceMeridian());
        assertEquals(1, cronExpressionParser.getHoursEntry().getFrom());
        assertEquals(JiraUtils.AM, cronExpressionParser.getHoursEntry().getFromMeridian());
        assertEquals(5, cronExpressionParser.getHoursEntry().getTo());
        assertEquals(JiraUtils.AM, cronExpressionParser.getHoursEntry().getToMeridian());
        assertTrue(cronExpressionParser.isValidForEditor());
        assertFalse(cronExpressionParser.getCronEditorBean().is24HourRange());
    }

    @Test
    public void testDailySpecEveryFifteenMinutesOfEverySecondHourAllDay()
    {
        String everyFiftenMinutesEverySecondHour = "0 0/15 */2 ? * *";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(everyFiftenMinutesEverySecondHour);
        assertFalse(cronExpressionParser.isDailyMode());
        assertFalse(cronExpressionParser.isDayPerWeekMode());
        assertFalse(cronExpressionParser.isDaysPerMonthMode());
        assertTrue(cronExpressionParser.isAdvancedMode());

        assertEquals(0, cronExpressionParser.getIncrementInMinutes());

        // because the cron expression is too complex we dont need to know if its a range because the user can only edit
        // via the advanced tab in the ui.
        assertFalse(cronExpressionParser.isValidForEditor());

        final CronEditorBean cronEditorBean = cronExpressionParser.getCronEditorBean();
        assertTrue(cronEditorBean.isAdvancedMode());
        assertFalse(cronEditorBean.isDailyMode());
        assertFalse(cronEditorBean.isDayOfWeekOfMonth());
        assertFalse(cronEditorBean.isDayPerWeekMode());
        assertFalse(cronEditorBean.isDaysPerMonthMode());
    }

    @Test
    public void testDailySpecEveryTwoHoursAllDay()
    {
        String oneToFourEveryTwoHours = "0 0 */2 ? * *";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(oneToFourEveryTwoHours);
        assertTrue(cronExpressionParser.isDailyMode());
        assertFalse(cronExpressionParser.isDayPerWeekMode());
        assertFalse(cronExpressionParser.isDaysPerMonthMode());
        assertEquals(120, cronExpressionParser.getIncrementInMinutes());
        assertEquals(-1, cronExpressionParser.getHoursEntry().getRunOnce());
        assertNull(cronExpressionParser.getHoursEntry().getRunOnceMeridian());
        assertEquals(12, cronExpressionParser.getHoursEntry().getFrom());
        assertEquals(JiraUtils.AM, cronExpressionParser.getHoursEntry().getFromMeridian());
        assertEquals(12, cronExpressionParser.getHoursEntry().getTo());
        assertEquals(JiraUtils.AM, cronExpressionParser.getHoursEntry().getToMeridian());
        assertTrue(cronExpressionParser.isValidForEditor());
        assertTrue(cronExpressionParser.getCronEditorBean().is24HourRange());
    }

    @Test
    public void testDailySpecWithInvalidFrequency()
    {
        String oneToFourEveryFiveHours = "0 0 1-4/5 * * *";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(oneToFourEveryFiveHours);
        assertFalse(cronExpressionParser.isValidForEditor());
    }

    @Test
    public void testMonthEntryIsInvalid()
    {
        String everyTwoMonths = "0 10 6 * */2 ?";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(everyTwoMonths);
        assertFalse(cronExpressionParser.isValidForEditor());
    }

    @Test
    public void testDayOfWeekOfMonth()
    {
        String secondMondayOfMonth = "0 30 20 ? * 2#2";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(secondMondayOfMonth);
        assertTrue(cronExpressionParser.isValidForEditor());
        assertTrue(cronExpressionParser.isDaysPerMonthMode());
        assertTrue(cronExpressionParser.isDayOfWeekOfMonth()); // sub mode
        assertFalse(cronExpressionParser.isDayPerWeekMode());
    }

    @Test
    public void testDaysOfWeekIsValid()
    {
        String everySecondMondayOfTheMonth = "0 0 1 ? * 2#2";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(everySecondMondayOfTheMonth);
        assertTrue(cronExpressionParser.isValidForEditor());
        assertTrue(cronExpressionParser.isDaysPerMonthMode());
        assertTrue(cronExpressionParser.isDayOfWeekOfMonth()); // sub mode
        assertFalse(cronExpressionParser.isDayPerWeekMode());
    }

    @Test
    public void testInvalidCronStringPassesBackDefaultCronStrings()
    {
        String oneToFourEveryFiveHours = "0 0 1-4/5 * * *";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(oneToFourEveryFiveHours);
        assertFalse(cronExpressionParser.isValidForEditor());
        assertEquals(oneToFourEveryFiveHours, cronExpressionParser.getCronString());

        assertTrue(cronExpressionParser.getDaysOfWeekEntry().isValid());
    }

    @Test
    public void testInvalidCronStringGoesToAdvancedMode()
    {
        String oneToFourEveryFiveHours = "0 0 1-4/5 * * *";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(oneToFourEveryFiveHours);
        assertFalse(cronExpressionParser.isValidForEditor());
        assertTrue(cronExpressionParser.isAdvancedMode());
    }

    @Test
    public void testInvalidCronStringHourContainsComma()
    {
        String atOneTwoAndThree = "0 0 1,2,3 * * *";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(atOneTwoAndThree);
        assertFalse(cronExpressionParser.isValidForEditor());
        assertTrue(cronExpressionParser.isAdvancedMode());
    }

    @Test
    public void testDayOfMonthIsValid()
    {
        String thirtiethOfEachMonth = "0 0 1 30 * ?";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(thirtiethOfEachMonth);
        assertTrue(cronExpressionParser.isValidForEditor());
        assertTrue(cronExpressionParser.isDaysPerMonthMode());
        assertFalse(cronExpressionParser.isDayOfWeekOfMonth());
        assertFalse(cronExpressionParser.isDayPerWeekMode());
    }

    @Test
    public void testSingleDayOfWeek()
    {
        String everySecondMondayOfTheMonth = "0 0 1 ? * 2";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(everySecondMondayOfTheMonth);
        assertTrue(cronExpressionParser.isValidForEditor());
        assertFalse(cronExpressionParser.isDaysPerMonthMode());
        assertTrue(cronExpressionParser.isDayPerWeekMode());
        assertEquals(1, cronExpressionParser.getHoursEntry().getRunOnce());
        assertNull(cronExpressionParser.getDaysOfWeekEntry().getDayInMonthOrdinal());
        assertEquals("2", cronExpressionParser.getDaysOfWeekEntry().getDaysAsNumbers());
    }

    @Test
    public void testDefaultCronString()
    {
        CronExpressionParser cronExpressionParser = new CronExpressionParser();
        CronEditorBean cronEditorBean = cronExpressionParser.getCronEditorBean();
        assertTrue(cronEditorBean.isDailyMode());
    }

    @Test
    public void testEvery3Days()
    {
        CronExpressionParser cronExpressionParser = new CronExpressionParser("0 0 10 1/3 * ?");
        CronEditorBean cronEditorBean = cronExpressionParser.getCronEditorBean();
        assertFalse(cronEditorBean.isDailyMode());
        assertFalse(cronEditorBean.isDayPerWeekMode());
        assertFalse(cronEditorBean.isDaysPerMonthMode());
        assertTrue(cronEditorBean.isAdvancedMode());
    }

    @Test
    public void testDaily10am()
    {
        //Note that 0 0 10 * * ? is not the preferred daily form of the generator.
        //The parser should still be able to interpret it as daily however.
        CronExpressionParser cronExpressionParser = new CronExpressionParser("0 0 10 * * ?");
        CronEditorBean cronEditorBean = cronExpressionParser.getCronEditorBean();
        assertTrue(cronEditorBean.isDailyMode());
        assertFalse(cronEditorBean.isDayPerWeekMode());
        assertFalse(cronEditorBean.isDaysPerMonthMode());
        assertFalse(cronEditorBean.isAdvancedMode());
    }

    @Test
    public void testSecondsImpliesAdvanced()
    {

        String fourtyFourSecsAfterMidnight = "44 0 0 * * ?";
        CronExpressionParser cronExpressionParser = new CronExpressionParser(fourtyFourSecsAfterMidnight);
        CronEditorBean cronEditorBean = cronExpressionParser.getCronEditorBean();
        assertFalse(cronEditorBean.isDailyMode());
        assertFalse(cronEditorBean.isDayPerWeekMode());
        assertFalse(cronEditorBean.isDaysPerMonthMode());
        assertTrue(cronEditorBean.isAdvancedMode());
    }

    @Test
    public void testYearIsAdvanced()
    {
        CronExpressionParser cronExpressionParser = new CronExpressionParser("0 0 1 * * ? 2007");
        CronEditorBean cronEditorBean = cronExpressionParser.getCronEditorBean();
        assertFalse(cronEditorBean.isDailyMode());
        assertFalse(cronEditorBean.isDayPerWeekMode());
        assertFalse(cronEditorBean.isDaysPerMonthMode());
        assertTrue(cronEditorBean.isAdvancedMode());
    }

}
