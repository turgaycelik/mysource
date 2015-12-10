package com.atlassian.jira.web.bean;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation
 *
 * @since v4.4
 */
public class TimeTrackingGraphBeanFactoryImpl implements TimeTrackingGraphBeanFactory
{
    private final JiraDurationUtils utils;

    public TimeTrackingGraphBeanFactoryImpl(final JiraDurationUtils utils)
    {
        this.utils = notNull("utils", utils);
    }

    @Override
    public TimeTrackingGraphBean createBean(final Issue issue, final Style style, final I18nHelper helper)
    {
        notNull("helper", helper);
        notNull("issue", issue);
        notNull("style", style);

        final Long orig = issue.getOriginalEstimate();
        final Long estimate = issue.getEstimate();
        final Long spent = issue.getTimeSpent();

        return createBean(style, helper, orig, spent, estimate);
    }

    @Override
    public TimeTrackingGraphBean createBean(final AggregateTimeTrackingBean aggregateBean, final Style style,
            final I18nHelper helper)
    {
        notNull("helper", helper);
        notNull("aggregateBean", aggregateBean);
        notNull("style", style);

        final Long spent = aggregateBean.getTimeSpent();
        final Long estimate = aggregateBean.getRemainingEstimate();
        final Long orig = aggregateBean.getOriginalEstimate();

        return createBean(style, helper, orig, spent, estimate);
    }

    private TimeTrackingGraphBean createBean(final Style style, final I18nHelper helper, final Long originalEstimate,
            final Long timeSpent, final Long remainingEstimate)
    {
        final TimeTrackingGraphBean.Parameters params = new TimeTrackingGraphBean.Parameters(helper);
        params.setOriginalEstimate(originalEstimate);
        params.setRemainingEstimate(remainingEstimate);
        params.setTimeSpent(timeSpent);

        final Locale locale = helper.getLocale();
        params.setTimeSpentStr(style.getDuration(timeSpent, locale, utils));
        params.setOriginalEstimateStr(style.getDuration(originalEstimate, locale, utils));
        params.setRemainingEstimateStr(style.getDuration(remainingEstimate, locale, utils));

        params.setTimeSpentTooltip(style.getTooltip(timeSpent, locale, utils));
        params.setOriginalEstimateTooltip(style.getTooltip(originalEstimate, locale, utils));
        params.setRemainingEstimateTooltip(style.getTooltip(remainingEstimate, locale, utils));

        return new TimeTrackingGraphBean(params);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
