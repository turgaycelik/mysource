package com.atlassian.jira.web.component.cron.generator;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.component.cron.CronEditorBean;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes Cron Expressions in human readable text.
 */
public class CronExpressionDescriptor
{
    private static final Map<String, String> MINUTE_INCREMENT_TO_MESG_KEY = ImmutableMap.of(
            "15", "cron.editor.every.15.minutes",
            "30", "cron.editor.every.30.minutes",
            "60", "cron.editor.every.hour",
            "120", "cron.editor.every.2.hours",
            "180", "cron.editor.every.3.hours"
    );

    private static final Map<String, String> DAY_NUMBERS;
    static
    {
        Map<String, String> dayNumbers = new HashMap<String, String>();
        dayNumbers.put("1", "sunday");
        dayNumbers.put("2", "monday");
        dayNumbers.put("3", "tuesday");
        dayNumbers.put("4", "wednesday");
        dayNumbers.put("5", "thursday");
        dayNumbers.put("6", "friday");
        dayNumbers.put("7", "saturday");
        DAY_NUMBERS = Collections.unmodifiableMap(dayNumbers);
    }

    private static final String CRON_EDITOR_KEY_PREFIX = "cron.editor.";
    private static final int MINUTES_DIGITS = 2;
    private static final int DAYS_IN_WEEK = 7;
    private static final String LAST_COMMA_REGEX = ",([^,]*)$";

    private final I18nHelper i18n;


    /**
     * Creates a Descriptor for
     * @param i18n the I18nBean.
     */
    public CronExpressionDescriptor(I18nHelper i18n)
    {
        this.i18n = i18n;
    }

    /**
     * Returns the translated name of the day for the given day number using
     * our cron editor day numbering scheme.
     * @param number the day number.
     * @return the name of the day.
     */
    private String getDay(String number)
    {
        String keypart = DAY_NUMBERS.get(number);
        return i18n.getText(CRON_EDITOR_KEY_PREFIX + keypart);
    }

    /**
     * Renders a nice locale-specific and human readable description of the given
     * cronEditorBean's schedule or, if this can't be understood, the underlying
     * cron expression.
     *
     * @param bean the cronEditorBean.
     * @return the description.
     */
    public String getPrettySchedule(CronEditorBean bean)
    {
        if (bean.isAdvancedMode()) {
            return bean.getCronString();
        }
        StringBuilder desc = new StringBuilder();
        if (bean.isDailyMode())
        {
            desc.append(i18n.getText("cron.editor.daily")).append(" ");
            desc.append(getTimePart(bean));
        }
        else if (bean.isDayPerWeekMode())
        {
            desc.append(getDayPerWeekDescriptor(bean));
        }
        else if (bean.isDaysPerMonthMode())
        {
            desc.append(getDayPerMonthDescriptor(bean));
        }

        return desc.toString();
    }

    private String getDayPerWeekDescriptor(CronEditorBean bean)
    {
        StringBuilder desc = new StringBuilder();
        desc.append(i18n.getText("cron.editor.each"));
        desc.append(" ");

        String[] daysArray = bean.getSpecifiedDaysPerWeek().split(",");
        Arrays.sort(daysArray);
        String daysString = StringUtils.join(daysArray, ",");
        daysString = daysString.replaceAll(LAST_COMMA_REGEX, " and $1"); // replace last comma with "and"
        for (int i = 1; i <= DAYS_IN_WEEK; i++)
        {
            String dayNum = Integer.toString(i);
            daysString = daysString.replaceAll(dayNum, getDay(dayNum));
        }
        daysString = daysString.replaceAll(",", ", ");
        desc.append(daysString).append(" ");

        desc.append(getTimePart(bean));
        return desc.toString();
    }

    /**
     * Renders the day per month mode description either the numeric day
     * (e.g. 1st day of the month) or the ordinal day of week of month (e.g. last Monday of the month)
     * @param bean the bean whose state is rendered.
     * @return the rendered description.
     */
    private String getDayPerMonthDescriptor(CronEditorBean bean)
    {
        StringBuilder desc = new StringBuilder();
        if (bean.isDayOfWeekOfMonth())
        {
            String ordinal = i18n.getText("cron.editor.ordinal." + bean.getDayInMonthOrdinal());
            String ordinalWeekday = ordinal + " " + getDay(bean.getSpecifiedDaysPerWeek());
            desc.append(i18n.getText("cron.editor.the.of.every.month", ordinalWeekday));
            desc.append(" ");
            desc.append(getTimePart(bean));
        }
        else
        {
            // numeric day of month
            desc.append(i18n.getText("cron.editor.the.day.of.every.month", i18n.getText("cron.editor.nth." + bean.getDayOfMonth())));
            desc.append(" ");
            desc.append(getTimePart(bean));
        }
        return desc.toString();
    }

    /**
     * Renders the time as a run once (e.g. "at 5:25 pm") or a repetition in a range
     * (e.g. "every 3 hours from 1:00 pm to 10:00 pm")
     *
     * @param bean the bean containing the time state.
     * @return the rendered description.
     */
    private String getTimePart(CronEditorBean bean)
    {
        StringBuilder desc = new StringBuilder();
        if (!bean.isRange())
        {
            desc.append(getRunOnce(bean));
        }
        else
        {
            desc.append(getRepeatInRange(bean));
        }
        return desc.toString();
    }

    /**
     * Renders the run-once time like this "at 5:25 pm".
     *
     * @param bean contains the run once time.
     * @return a description of the run once time.
     */
    private String getRunOnce(CronEditorBean bean)
    {
        StringBuilder desc = new StringBuilder();
        desc.append(i18n.getText("cron.editor.at"));
        desc.append(" ");
        desc.append(bean.getHoursRunOnce()).append(":").append(getPaddedMinutes(bean.getMinutes())).append(" ").append(bean.getHoursRunOnceMeridian());
        return desc.toString();
    }

    /**
     * Renders the repetition in a time range like this "every 3 hours from 1:00 pm to 10:00 pm".
     *
     * @param bean containing the repeat and range.
     * @return the rendered description.
     */
    private String getRepeatInRange(CronEditorBean bean)
    {
        StringBuilder desc = new StringBuilder();
        String increment = bean.getIncrementInMinutes();
        if (increment.equals("0"))
        {
            // once per day, we leave this expression out
        }
        else
        {
            String key = MINUTE_INCREMENT_TO_MESG_KEY.get(increment);
            desc.append(i18n.getText(key));
        }

        if (!bean.is24HourRange())
        {
            desc.append(" ");
            desc.append(i18n.getText("cron.editor.from"));
            desc.append(" ");
            desc.append(bean.getHoursFrom()).append(":00 ").append(bean.getHoursFromMeridian());
            desc.append(" ");
            desc.append(i18n.getText("cron.editor.to"));
            desc.append(" ");
            desc.append(bean.getHoursTo()).append(":00 ").append(bean.getHoursToMeridian());
        }
        return desc.toString();
    }

    private String getPaddedMinutes(String minutes)
    {
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumIntegerDigits(MINUTES_DIGITS);
        return format.format(Integer.parseInt(minutes));
    }
}
