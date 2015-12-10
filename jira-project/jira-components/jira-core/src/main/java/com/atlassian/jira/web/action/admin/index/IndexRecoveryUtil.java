package com.atlassian.jira.web.action.admin.index;

import java.util.Collection;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableList;

/**
 * Helper for Index recovery options.
 *
 * @since v6.2
 */
public class IndexRecoveryUtil
{
    static final Interval DEFAULT_INTERVAL = Interval.DAILY;

    enum Interval {

        HOURLY(DateUtils.HOUR_MILLIS),
        DAILY(DateUtils.DAY_MILLIS),
        WEEKLY(DateUtils.DAY_MILLIS * 7);

        private final long millis;

        private Interval(final long millis)
        {
            this.millis = millis;
        }

        long getMillis()
        {
            return millis;
        }

    }

    public static IndexRecoveryUtil.Interval intervalFromMillis(final long delay)
    {
        // Do a bit of fuzzy stuff here as for now we only support Hours/days/weeks
        if (delay < DateUtils.MINUTE_MILLIS)
        {
            // Take this as not set
            return IndexRecoveryUtil.DEFAULT_INTERVAL;
        }
        else if (delay < DateUtils.HOUR_MILLIS * 2)
        {
            return IndexRecoveryUtil.Interval.HOURLY;
        }
        else if (delay < DateUtils.DAY_MILLIS * 2)
        {
            return IndexRecoveryUtil.Interval.DAILY;
        }
        return IndexRecoveryUtil.Interval.WEEKLY;
    }

    public static Collection<TextOption> getIntervalOptions(I18nHelper i18n)
    {
        return ImmutableList.of
                (
                        new TextOption(IndexRecoveryUtil.Interval.HOURLY.name(), i18n.getText("admin.index.recovery.snapshot.interval.hour")),
                        new TextOption(IndexRecoveryUtil.Interval.DAILY.name(), i18n.getText("admin.index.recovery.snapshot.interval.day")),
                        new TextOption(IndexRecoveryUtil.Interval.WEEKLY.name(), i18n.getText("admin.index.recovery.snapshot.interval.week"))
                );
    }

   public static String getIntervalOption(Interval interval, I18nHelper i18n)
    {
        switch (interval)
        {
            case HOURLY:
            {
                return i18n.getText("admin.index.recovery.snapshot.interval.hour");
            }
            case DAILY:
            {
                return i18n.getText("admin.index.recovery.snapshot.interval.day");
            }
            default:
            {
                return i18n.getText("admin.index.recovery.snapshot.interval.week");
            }
        }
    }

}
