package com.atlassian.jira.web.bean;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;

import java.util.Locale;

/**
 * Factory that can create {@link com.atlassian.jira.web.bean.TimeTrackingGraphBean} instances.
 *
 * @since v4.4
 */
public interface TimeTrackingGraphBeanFactory
{
    TimeTrackingGraphBean createBean(Issue issue, Style style, I18nHelper helper);

    TimeTrackingGraphBean createBean(AggregateTimeTrackingBean aggregateBean, Style style,
            I18nHelper helper);

    /**
     * Represents a style of {@link TimeTrackingGraphBean} that this factory can create.
     */
    public interface Style
    {
        /**
         * Style that will create a {@link TimeTrackingGraphBean} that can be used to render
         * time tracking data in a tight space.
         */
        public final Style SHORT = new ShortStyle();

        /**
         * Style that will create a {@link TimeTrackingGraphBean} that can be used to render
         * time tracking. This is the style you should use if you do not know.
         */
        public final Style NORMAL = new LongStyle();

        String getDuration(Long duration, Locale locale, final JiraDurationUtils utils);
        String getTooltip(Long duration, Locale locale, final JiraDurationUtils utils);
    }

    public static final class ShortStyle implements Style
    {
        public String getDuration(final Long duration, final Locale locale, final JiraDurationUtils utils)
        {
            if (duration != null)
            {
                return utils.getShortFormattedDuration(duration, locale);
            }
            return null;
        }

        public String getTooltip(final Long duration, final Locale locale, final JiraDurationUtils utils)
        {
            if (duration != null)
            {
                return utils.getFormattedDuration(duration, locale);
            }
            return null;
        }
    }

    public static final class LongStyle implements Style
    {
        public String getDuration(final Long duration, final Locale locale, final JiraDurationUtils utils)
        {
            if (duration != null)
            {
                return utils.getFormattedDuration(duration, locale);
            }
            return null;
        }

        public String getTooltip(final Long duration, final Locale locale, final JiraDurationUtils utils)
        {
            if (duration != null)
            {
                return utils.getFormattedDuration(duration, locale);
            }
            return null;
        }
    }
}
