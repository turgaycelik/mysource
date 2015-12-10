package com.atlassian.jira.plugin.report.impl;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.bean.I18nBean;

/**
 * Duration formatter used by reports.
 *
 * @since v3.11
 */
class DurationFormatterImpl implements DurationFormatter
{
    private final JiraDurationUtils jiraDurationUtils;
    private final I18nHelper i18nHelper;

    DurationFormatterImpl(I18nHelper i18nHelper, JiraDurationUtils jiraDurationUtils)
    {
        this.i18nHelper = i18nHelper;
        this.jiraDurationUtils = jiraDurationUtils;
    }

    /**
     * Formats the duration. If duration is null, returns a dash..
     *
     * @param duration duration
     * @return formatted duration String or i18ned "unknown" if null.
     */
    public String format(Long duration)
    {
        if (duration == null)
        {
            return "-";
        }
        duration = Math.abs(duration.longValue());
        return jiraDurationUtils.getFormattedDuration(duration, i18nHelper.getLocale());
    }

    public String shortFormat(Long duration)
    {
        if (duration == null)
        {
            return "-";
        }
        duration = Math.abs(duration.longValue());
        return jiraDurationUtils.getShortFormattedDuration(duration);
    }
}