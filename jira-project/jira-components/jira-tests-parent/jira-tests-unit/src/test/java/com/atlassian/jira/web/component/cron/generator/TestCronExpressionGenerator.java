package com.atlassian.jira.web.component.cron.generator;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.web.component.cron.CronEditorBean;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class TestCronExpressionGenerator
{
    @Test
    public void testDailyCronWithRunOnce()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursRunOnce("9");
        cronEditorBean.setHoursRunOnceMeridian(JiraUtils.AM);
        cronEditorBean.setMinutes("35");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 35 9 ? * *", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    @Test
    public void testDailyCronWithFromAndTo()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);

        cronEditorBean.setHoursFrom("1");
        cronEditorBean.setHoursFromMeridian(JiraUtils.AM);
        cronEditorBean.setHoursTo("1");
        cronEditorBean.setHoursToMeridian(JiraUtils.PM);

        cronEditorBean.setIncrementInMinutes("180");

        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 0 1-12/3 ? * *", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    @Test
    public void testDayOfWeekWithRunOnce()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAYS_OF_WEEK_SPEC_MODE);

        cronEditorBean.setHoursRunOnce("9");
        cronEditorBean.setHoursRunOnceMeridian(JiraUtils.AM);
        cronEditorBean.setMinutes("35");

        cronEditorBean.setSpecifiedDaysOfWeek("2,3,4");

        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 35 9 ? * 2,3,4", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    @Test
    public void testDayOfWeekWithFromTo()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAYS_OF_WEEK_SPEC_MODE);

        cronEditorBean.setHoursFrom("1");
        cronEditorBean.setHoursFromMeridian(JiraUtils.AM);
        cronEditorBean.setHoursTo("1");
        cronEditorBean.setHoursToMeridian(JiraUtils.PM);

        cronEditorBean.setIncrementInMinutes("180");

        cronEditorBean.setSpecifiedDaysOfWeek("2,3,4");

        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 0 1-12/3 ? * 2,3,4", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    @Test
    public void testDayOfMonth()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAYS_OF_MONTH_SPEC_MODE);

        cronEditorBean.setMinutes("59");
        cronEditorBean.setHoursRunOnce("12");
        cronEditorBean.setHoursRunOnceMeridian(JiraUtils.PM);

        cronEditorBean.setDayOfMonth("13");
        cronEditorBean.setDayOfWeekOfMonth(false);

        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 59 12 13 * ?", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    @Test
    public void testDayOfWeekOfMonth()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAYS_OF_MONTH_SPEC_MODE);

        cronEditorBean.setMinutes("59");
        cronEditorBean.setHoursRunOnce("12");
        cronEditorBean.setHoursRunOnceMeridian(JiraUtils.AM);

        cronEditorBean.setSpecifiedDaysOfWeek("2");
        cronEditorBean.setDayInMonthOrdinal("3");
        cronEditorBean.setDayOfWeekOfMonth(true);

        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 59 0 ? * 2#3", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    @Test
    public void testGenerateDailySpecWithIntervalAndRunOnce()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setIncrementInMinutes("60");
        cronEditorBean.setHoursRunOnce("9");
        cronEditorBean.setHoursRunOnceMeridian(JiraUtils.AM);
        cronEditorBean.setMinutes("35");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        try
        {
            cronExpressionGenerator.generateDailySpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException expected)
        {
            // ayay
        }
    }

    @Test
    public void testGenerateDailySpecWithNoIntervalAndNoRunOnce()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        try
        {
            cronExpressionGenerator.generateDailySpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException expected)
        {
            // ayay
        }
    }

    @Test
    public void testGenerateDailySpecRunOnce()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursRunOnce("4");
        cronEditorBean.setMinutes("25");
        cronEditorBean.setIncrementInMinutes("0");
        cronEditorBean.setHoursRunOnceMeridian(JiraUtils.PM);
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 25 16", dailySpec);
    }

    @Test
    public void testGenerateDailySpecRunOnceInMonthMode()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAYS_OF_MONTH_SPEC_MODE);
        cronEditorBean.setHoursRunOnce("4");
        cronEditorBean.setMinutes("25");
        // Set this to something other than one, this should be ignored because of the mode
        // This is a special case.
        cronEditorBean.setIncrementInMinutes("120");
        cronEditorBean.setHoursRunOnceMeridian(JiraUtils.PM);
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 25 16", dailySpec);
    }

    @Test
    public void testGenerateDailySpecFromToWithHourInterval()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursFrom("4");
        cronEditorBean.setHoursFromMeridian(JiraUtils.AM);
        cronEditorBean.setHoursTo("4");
        cronEditorBean.setHoursToMeridian(JiraUtils.PM);
        cronEditorBean.setIncrementInMinutes("120");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 0 4-15/2", dailySpec);
    }

    @Test
    public void testGenerateDailySpecFromToWithMinuteInterval()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursFrom("4");
        cronEditorBean.setHoursFromMeridian(JiraUtils.AM);
        cronEditorBean.setHoursTo("4");
        cronEditorBean.setHoursToMeridian(JiraUtils.PM);
        cronEditorBean.setIncrementInMinutes("15");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 0/15 4-15", dailySpec);
    }

    @Test
    public void testGenerateDailySpecWithEqualFromToMinuteInterval()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursFrom("4");
        cronEditorBean.setHoursFromMeridian(JiraUtils.AM);
        cronEditorBean.setHoursTo("4");
        cronEditorBean.setHoursToMeridian(JiraUtils.AM);
        cronEditorBean.setIncrementInMinutes("15");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 0/15 *", dailySpec);
    }

    @Test
    public void testGenerateDailySpecWithEqualFromToHourInterval()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursFrom("4");
        cronEditorBean.setHoursFromMeridian(JiraUtils.AM);
        cronEditorBean.setHoursTo("4");
        cronEditorBean.setHoursToMeridian(JiraUtils.AM);
        cronEditorBean.setIncrementInMinutes("120");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 0 */2", dailySpec);
    }

    @Test
    public void testGenerateDaysOfWeekSpecWithDaysSet()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        String specifiedDaysOfWeek = "2,3,4";
        cronEditorBean.setSpecifiedDaysOfWeek(specifiedDaysOfWeek);
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals(specifiedDaysOfWeek, cronExpressionGenerator.generateDaysOfWeekSpec(cronEditorBean));
    }

    @Test
    public void testGenerateDaysOfWeekSpecWithDaysNotSet()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        try
        {
            cronExpressionGenerator.generateDaysOfWeekSpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException e)
        {
            // Woo Hoo
        }
    }

    @Test
    public void testGenerateDayOfMonthSpecWithDayOfMonthSet()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        String dayOfMonth = "20";
        cronEditorBean.setDayOfMonth(dayOfMonth);
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals(dayOfMonth + " * ?", cronExpressionGenerator.generateDayOfMonthSpec(cronEditorBean));
    }

    @Test
    public void testGenerateDayOfMonthSpecWithDayOfMonthNotSet()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        try
        {
            cronExpressionGenerator.generateDaysOfWeekSpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException ise)
        {
            // WQooo hoo
        }
    }

    @Test
    public void testGenerateDayOfWeekOfMonthSpecLastMonday()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setDayInMonthOrdinal("L");
        cronEditorBean.setSpecifiedDaysOfWeek("2");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("? * 2L", cronExpressionGenerator.generateDayOfWeekOfMonthSpec(cronEditorBean));
    }

    @Test
    public void testGenerateDayOfWeekOfMonthSpecSecondTuesday()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setDayInMonthOrdinal("2");
        cronEditorBean.setSpecifiedDaysOfWeek("3");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("? * 3#2", cronExpressionGenerator.generateDayOfWeekOfMonthSpec(cronEditorBean));
    }

    @Test
    public void testGenerateDayOfWeekOfMonthWithoutOrdinal()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setSpecifiedDaysOfWeek("3");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();

        try
        {
            cronExpressionGenerator.generateDayOfWeekOfMonthSpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException e)
        {
            // Wook hoo
        }
    }

    @Test
    public void testGenerateDayOfWeekOfMonthWithoutSpecifiedDaysPerWeek()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setDayInMonthOrdinal("2");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();

        try
        {
            cronExpressionGenerator.generateDayOfWeekOfMonthSpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException e)
        {
            // Wook hoo
        }
    }


}
