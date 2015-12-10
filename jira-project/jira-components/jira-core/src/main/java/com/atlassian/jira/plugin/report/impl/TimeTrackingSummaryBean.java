package com.atlassian.jira.plugin.report.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;

import java.util.Collection;

/**
 * TimeTrackingSummary bean used by the view.
 */
public class TimeTrackingSummaryBean
{
    private static final int MAX_GRAPH_WIDTH = 400;
    private final long originalEstimate;
    private final long timeSpent;
    private final long remainingEstimate;
    private final long aggregateOriginalEstimate;
    private final long aggregateTimeSpent;
    private final long aggregateRemainingEstimate;

    TimeTrackingSummaryBean(long totOriginalEst, long totTimeSpent, long totRemainEst)
    {
        this.originalEstimate = totOriginalEst;
        this.timeSpent = totTimeSpent;
        this.remainingEstimate = totRemainEst;
        this.aggregateOriginalEstimate = totOriginalEst;
        this.aggregateTimeSpent = totTimeSpent;
        this.aggregateRemainingEstimate = totRemainEst;
    }

    TimeTrackingSummaryBean(Collection /* <ReportIssue> */ issues)
    {
        long totOriginalEst = 0;
        long totTimeSpent = 0;
        long totRemainEst = 0;
        long totAggOriginalEst = 0;
        long totAggTimeSpent = 0;
        long totAggRemainEst = 0;

        for (final Object issue1 : issues)
        {
            final ReportIssue reportIssue = ((ReportIssue) issue1);
            final Issue issue = reportIssue.getIssue();

            totOriginalEst += getLongValue(issue.getOriginalEstimate());
            totTimeSpent += getLongValue(issue.getTimeSpent());
            totRemainEst += getLongValue(issue.getEstimate());
            for (final Object o : reportIssue.getSubTasks())
            {
                Issue subTask = ((ReportIssue) o).getIssue();
                totOriginalEst += getLongValue(subTask.getOriginalEstimate());
                totTimeSpent += getLongValue(subTask.getTimeSpent());
                totRemainEst += getLongValue(subTask.getEstimate());
            }
            final AggregateTimeTrackingBean aggregates = reportIssue.getAggregateBean();
            totAggOriginalEst += getLongValue(aggregates.getOriginalEstimate());
            totAggTimeSpent += getLongValue(aggregates.getTimeSpent());
            totAggRemainEst += getLongValue(aggregates.getRemainingEstimate());
        }
        this.originalEstimate = totOriginalEst;
        this.timeSpent = totTimeSpent;
        this.remainingEstimate = totRemainEst;
        this.aggregateOriginalEstimate = totAggOriginalEst;
        this.aggregateTimeSpent = totAggTimeSpent;
        this.aggregateRemainingEstimate = totAggRemainEst;
    }

    private long getLongValue(Long input)
    {
        return (input == null) ? 0 : input.longValue();
    }

    public long getOriginalEstimate()
    {
        return originalEstimate;
    }

    public long getTimeSpent()
    {
        return timeSpent;
    }

    public long getRemainingEstimate()
    {
        return remainingEstimate;
    }

    public long getAggregateOriginalEstimate()
    {
        return aggregateOriginalEstimate;
    }

    public long getAggregateRemainingEstimate()
    {
        return aggregateRemainingEstimate;
    }

    public long getAggregateTimeSpent()
    {
        return aggregateTimeSpent;
    }

    public int getCompletionTotalWidth()
    {
        if (timeSpent + remainingEstimate > originalEstimate)
        {
            return MAX_GRAPH_WIDTH;
        }

        else
        {
            return (int) (((float) (timeSpent + remainingEstimate) / (float) originalEstimate) * MAX_GRAPH_WIDTH);
        }
    }

    public int getEstimationTotalWidth()
    {
        return MAX_GRAPH_WIDTH;
    }

    public int getCompletedWidth()
    {
        return (int) (((float) timeSpent / (float) (timeSpent + remainingEstimate)) * getCompletionTotalWidth());
    }

    public int getIncompleteWidth()
    {
        return getCompletionTotalWidth() - getCompletedWidth();
    }

    public int getEstimateWidth()
    {
        if (originalEstimate > timeSpent + remainingEstimate)
        {
            return getEstimationTotalWidth();
        }
        else
        {
            return (int) (((float) originalEstimate / (float) (remainingEstimate + timeSpent)) * getEstimationTotalWidth());
        }
    }

    public int getUnderEstimateWidth()
    {
        if (originalEstimate > timeSpent + remainingEstimate)
        {
            return 0;
        }
        else
        {
            return getEstimationTotalWidth() - getEstimateWidth();
        }
    }

    public int getOverEstimateWidth()
    {
        if (originalEstimate > timeSpent + remainingEstimate)
        {
            return getEstimationTotalWidth() - (getCompletedWidth() + getIncompleteWidth());
        }
        else
        {
            return 0;
        }
    }
}
