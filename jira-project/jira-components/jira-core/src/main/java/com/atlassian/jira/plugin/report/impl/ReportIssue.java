package com.atlassian.jira.plugin.report.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Issue wrapper that is used by the TimeTracking report.
 * <p/>
 * This class provides all data that the view (UI) needs, including formatting support.
 *
 * @since v3.11
 */
public class ReportIssue
{
    private static final Long ZERO = new Long(0);

    private static List transformSubTasks(Issue issue, final AggregateTimeTrackingCalculator aggregateCalculator, final DurationFormatter durationFormatter, final AccuracyCalculator accuracyCalculator, Comparator comparator, Predicate issueInclusionPredicate)
    {
        if (issue.getSubTaskObjects() == null || issue.getSubTaskObjects().isEmpty())
        {
            return Collections.EMPTY_LIST;
        }

        List reportIssueSubTasks = new ArrayList(issue.getSubTaskObjects());
        CollectionUtils.transform(reportIssueSubTasks, new Transformer()
        {
            public Object transform(Object object)
            {
                Issue issue = (Issue) object;
                return new ReportIssue(issue, aggregateCalculator.getAggregates(issue), durationFormatter, accuracyCalculator, new ArrayList(), false);
            }
        });
        Collections.sort(reportIssueSubTasks, comparator);
        CollectionUtils.filter(reportIssueSubTasks, issueInclusionPredicate);
        return reportIssueSubTasks;
    }

    private final Issue issue;
    private final AggregateTimeTrackingBean aggregateBean;
    private final DurationFormatter durationFormatter;
    private final AccuracyCalculator accuracyCalculator;
    private final Collection subTasks;
    private final boolean isOrphan;

    private ReportIssue(Issue issue, AggregateTimeTrackingBean aggregateBean, final DurationFormatter durationFormatter, final AccuracyCalculator accuracyCalculator, List subTasks, boolean isOrphan)
    {
        this.issue = issue;
        this.aggregateBean = aggregateBean;
        this.durationFormatter = durationFormatter;
        this.accuracyCalculator = accuracyCalculator;
        this.subTasks = Collections.unmodifiableList(subTasks);
        this.isOrphan = isOrphan;
    }

    public ReportIssue(Issue issue, AggregateTimeTrackingCalculator aggregateCalculator, final DurationFormatter durationFormatter, final AccuracyCalculator accuracyCalculator, Comparator comparator, Predicate issueInclusionPredicate)
    {
        // all externally created ReportIssues are orphans, whether they are subtasks or not.
        this(issue, aggregateCalculator.getAggregates(issue), durationFormatter, accuracyCalculator, transformSubTasks(issue, aggregateCalculator, durationFormatter, accuracyCalculator, comparator, issueInclusionPredicate), true);
    }

    public String getKey()
    {
        return issue.getKey();
    }

    public String getSummary()
    {
        return issue.getSummary();
    }

    public IssueType getIssueType()
    {
        return issue.getIssueTypeObject();
    }

    public Priority getPriority()
    {
        return issue.getPriorityObject();
    }

    public Status getStatus()
    {
        return issue.getStatusObject();
    }

    public String getOriginalEstimate()
    {
        return durationFormatter.shortFormat(issue.getOriginalEstimate());
    }

    public String getAggregateOriginalEstimate()
    {
        if (!isOrphan && isSubTask())
        {
            return "";
        }
        return durationFormatter.shortFormat(aggregateBean.getOriginalEstimate());
    }

    public String getRemainingEstimate()
    {
        return durationFormatter.shortFormat(issue.getEstimate());
    }

    public String getAggregateRemainingEstimate()
    {
        return !isOrphan && isSubTask() ? "" : durationFormatter.shortFormat(aggregateBean.getRemainingEstimate());
    }

    Long getAggregateRemainingEstimateLong(Long defaultValue)
    {
        return getNotNull(aggregateBean.getRemainingEstimate(), defaultValue);
    }

    Long getAggregateOriginalEstimateLong(Long defaultValue)
    {
        return getNotNull(aggregateBean.getOriginalEstimate(), defaultValue);
    }
    
    static Long getNotNull(Long num, Long defaultValue)
    {
        return (num == null) ? defaultValue : num;
    }

    public String getTimeSpent()
    {
        return durationFormatter.shortFormat(issue.getTimeSpent());
    }

    public String getAggregateTimeSpent()
    {
        return !isOrphan && isSubTask() ? "" : durationFormatter.shortFormat(aggregateBean.getTimeSpent());
    }

    public String getAccuracy()
    {
        Long accuracy = accuracyCalculator.calculateAccuracy(issue.getOriginalEstimate(), issue.getEstimate(), issue.getTimeSpent());
        return durationFormatter.format(accuracy);
    }

    public String getAccuracyNice()
    {
        return accuracyCalculator.calculateAndFormatAccuracy(issue.getOriginalEstimate(), issue.getEstimate(), issue.getTimeSpent());
    }

    public String getAccuracyPercentage()
    {
        return getAccuracyPercentage(issue.getOriginalEstimate(), issue.getEstimate(), issue.getTimeSpent());
    }

    public String getAggregateAccuracyPercentage()
    {
        return getAccuracyPercentage(aggregateBean.getOriginalEstimate(), aggregateBean.getRemainingEstimate(), aggregateBean.getTimeSpent());
    }

    private String getAccuracyPercentage(Long originalEstLong, Long timeEstLong, Long timeSpentLong)
    {
        long originalEst = getLongNullSafe(originalEstLong);
        if (originalEst == 0)
        {
            return "";
        }
        long timeEst = getLongNullSafe(timeEstLong);
        long timeSpent = getLongNullSafe(timeSpentLong);

        return "" + AccuracyCalculator.Percentage.calculate(originalEst, timeSpent, timeEst);
    }

    /**
     * Are there are any values against this issue at all.
     *
     * @return true if any times have been logged or estimated.
     */
    public boolean isTimeTracked()
    {
        return isTracked(issue.getOriginalEstimate(), issue.getEstimate(), issue.getTimeSpent());
    }

    /**
     * Are there are any aggregate values against this issue at all.
     *
     * @return true if any times have been logged or estimated in aggregate.
     */
    public boolean isAggregateTimeTracked()
    {
        return isTracked(aggregateBean.getOriginalEstimate(), aggregateBean.getRemainingEstimate(), aggregateBean.getTimeSpent());
    }

    static boolean isTracked(Long original, Long remaining, Long timeSpent)
    {
        return getLongNullSafe(original) != 0 || getLongNullSafe(remaining) != 0 || getLongNullSafe(timeSpent) != 0;
    }

    public boolean hasOriginalEstimate()
    {
        return issue.getOriginalEstimate() != null;
    }

    public boolean hasAggregateOriginalEstimate()
    {
        return aggregateBean.getOriginalEstimate() != null;
    }

    public String getAggregateAccuracy()
    {
        Long accuracy = accuracyCalculator.calculateAccuracy(aggregateBean.getOriginalEstimate(), aggregateBean.getRemainingEstimate(), aggregateBean.getTimeSpent());
        return durationFormatter.format(accuracy);
    }

    public String getAggregateAccuracyNice()
    {
        if (!isOrphan && isSubTask())
        {
            return "";
        }
        return accuracyCalculator.calculateAndFormatAccuracy(aggregateBean.getOriginalEstimate(), aggregateBean.getRemainingEstimate(), aggregateBean.getTimeSpent());
    }

    public int onSchedule()
    {
        return accuracyCalculator.onSchedule(issue.getOriginalEstimate(), issue.getEstimate(), issue.getTimeSpent());
    }

    public int onScheduleAggregate()
    {
        return accuracyCalculator.onSchedule(aggregateBean.getOriginalEstimate(), aggregateBean.getRemainingEstimate(), aggregateBean.getTimeSpent());
    }

    public Collection getSubTasks()
    {
        return subTasks;
    }

    public Issue getIssue()
    {
        return issue;
    }

    public boolean isOrphan()
    {
        return isOrphan;
    }

    public boolean isSubTask()
    {
        return issue.getParentObject() != null;
    }

    public Issue getParent()
    {
        return issue.getParentObject();
    }

    AggregateTimeTrackingBean getAggregateBean()
    {
        return aggregateBean;
    }

    boolean isAggregateComplete()
    {
        return !(getAggregateRemainingEstimateLong(ZERO).longValue() > 0);
    }

    /**
     * Converts the Long object to primitive long. Returns zero for null.
     *
     * @param value value to convert
     * @return primitive long value or zero if null
     */
    private static long getLongNullSafe(Long value)
    {
        return value == null ? 0 : value.longValue();
    }
}