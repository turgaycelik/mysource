package com.atlassian.jira.web.component.cron.generator;

import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.web.component.cron.parser.CronExpressionParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCronExpressionDescriptor
{
    private void assertPrettySchedule(String expectedPretty, String cronString)
    {
        CronExpressionParser parser = new CronExpressionParser(cronString);
        I18nBean i18n = new MockI18nBean(); // English locale
        CronExpressionDescriptor descriptor = new CronExpressionDescriptor(i18n);
        assertEquals(expectedPretty, descriptor.getPrettySchedule(parser.getCronEditorBean()));
    }

    @Test
    public void getPrettyScheduleInvalid()
    {
        String invalidString = "Invalid 2 3 4 5 6";
        assertPrettySchedule(invalidString, invalidString);
    }

    @Test
    public void getPrettyScheduleDailyAt10()
    {
        assertPrettySchedule("Daily at 10:00 am", "0 0 10 * * *");
    }

    @Test
    public void getPrettyScheduleDailyAt1035()
    {
        assertPrettySchedule("Daily at 10:35 am", "0 35 10 * * *");
    }

    @Test
    public void getPrettyScheduleDailyBetween10And3Every2Hours()
    {
        assertPrettySchedule("Daily every 2 hours from 10:00 am to 3:00 pm", "0 0 10-14/2 * * *");
    }

    @Test
    public void getPrettyScheduleDailyBetween9And2Every15Minutes()
    {
        assertPrettySchedule("Daily every 15 minutes from 9:00 am to 2:00 pm", "0 0/15 9-13 * * *");
    }

    @Test
    public void getPrettyScheduleDailyAllDayEvery15Minutes()
    {
        assertPrettySchedule("Daily every 15 minutes", "0 0/15 * * * *");
    }

    @Test
    public void getPrettyScheduleDailyAllDayEvery2Hours()
    {
        assertPrettySchedule("Daily every 2 hours", "0 0 */2 * * *");
    }

    @Test
    public void getPrettyScheduleWithHoursAndMinutesIncrementsRoundTrips()
    {
        final String expression = "0 0/15 */2 ? * *";
        assertPrettySchedule(expression, expression);
    }

    @Test
    public void getPrettyScheduleWithSingleHourAndMinutesIncrementsRoundTrips()
    {
        final String expression = "0 0/15 6 ? * *";
        assertPrettySchedule(expression, expression);
    }

    @Test
    public void getPrettyScheduleWithSingleHourRangeAndMinutesIncrementsRoundTrips()
    {
        assertPrettySchedule("Daily every 15 minutes from 6:00 am to 7:00 am", "0 0/15 6-6 ? * *");
    }

    @Test
    public void getPrettyScheduleDaysPerWeekMonday()
    {
        assertPrettySchedule("Each Monday at 10:00 am", "0 0 10 * * 2");
    }

    @Test
    public void getPrettyScheduleDaysPerWeekSunTueThu()
    {
        assertPrettySchedule("Each Sunday, Tuesday and Thursday at 10:00 am", "0 0 10 * * 3,1,5");
    }

    @Test
    public void getPrettyScheduleDaysPerWeekTueAndThurs()
    {
        assertPrettySchedule("Each Tuesday and Thursday at 10:00 am", "0 0 10 * * 3,5");
    }

    @Test
    public void getPrettyScheduleDaysPerWeekMondayTueAndThurs()
    {
        assertPrettySchedule("Each Monday, Tuesday and Thursday at 10:00 am", "0 0 10 * * 2,3,5");
    }

    @Test
    public void getPrettyScheduleDaysPerWeekMondayTueWithRange()
    {
        assertPrettySchedule("Each Monday and Tuesday every 30 minutes from 8:00 am to 11:00 am", "0 0/30 8-10 * * 2,3");
    }

    @Test
    public void getPrettyScheduleDaysPerMonthFirstOfMonth()
    {
        assertPrettySchedule("The 1st day of every month at 10:00 am", "0 0 10 1 * ?");
    }

    @Test
    public void getPrettyScheduleDaysPerMonthLastOfMonth()
    {
        assertPrettySchedule("The last day of every month at 1:25 pm", "0 25 13 L * ?");
    }

    @Test
    public void getPrettyScheduleDaysPerMonthSecondWednesday()
    {
        assertPrettySchedule("The second Wednesday of every month at 4:25 pm", "0 25 16 ? * 4#2");
    }

    @Test
    public void getPrettyScheduleDaysPerMonthLastFriday()
    {
        assertPrettySchedule("The last Friday of every month at 8:15 am", "0 15 8 ? * 6L");
    }

    @Test
    public void getPrettyScheduleAdvanced()
    {
        assertPrettySchedule("30 0 10 * 1 ?", "30 0 10 * 1 ?"); // seconds field and January month
    }

    @Test
    public void getPrettyScheduleDaily10AMNonPreferredGeneratorFormat()
    {
        assertPrettySchedule("Daily at 10:00 am", "0 0 10 * * ?");
    }
}
