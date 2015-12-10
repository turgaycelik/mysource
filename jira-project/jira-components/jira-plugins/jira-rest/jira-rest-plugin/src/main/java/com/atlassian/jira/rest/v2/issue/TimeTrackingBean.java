package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.component.ComponentAccessor;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.atlassian.jira.issue.IssueFieldConstants.TIMETRACKING;
import static com.atlassian.jira.issue.IssueFieldConstants.TIME_ESTIMATE;
import static com.atlassian.jira.issue.IssueFieldConstants.TIME_ORIGINAL_ESTIMATE;
import static com.atlassian.jira.issue.IssueFieldConstants.TIME_SPENT;

/**
 * This bean holds the time tracking information that is sent back to clients of the REST API.
 * This is for documentation only
 *
 * @since v4.2
 */
@XmlRootElement
public class TimeTrackingBean
{
    @XmlElement
    private String originalEstimate;

    @XmlElement
    private String remainingEstimate;

    @XmlElement
    private String timeSpent;

    @XmlElement
    private Long originalEstimateSeconds;

    @XmlElement
    private Long remainingEstimateSeconds;

    @XmlElement
    private Long timeSpentSeconds;

    /**
     * Creates a new TimeTrackingBean.
     *
     * @param originalEstimateSeconds the original estimateMinutes
     * @param estimateSeconds the remaining estimateMinutes
     * @param timeSpentSeconds the time spent
     */
    public TimeTrackingBean(Long originalEstimateSeconds, Long estimateSeconds, Long timeSpentSeconds)
    {
        this.originalEstimateSeconds = originalEstimateSeconds;
        this.remainingEstimateSeconds = estimateSeconds;
        this.timeSpentSeconds = timeSpentSeconds;
        this.originalEstimate = secondsToFormattedMinutes(originalEstimateSeconds);
        this.remainingEstimate = secondsToFormattedMinutes(estimateSeconds);
        this.timeSpent  = secondsToFormattedMinutes(timeSpentSeconds);
    }

    /**
     * Returns true iff at least one of this bean's properties is non-null.
     *
     * @return a boolean indicating whether at least one of this bean's properties is non-null
     */
    public boolean hasValues()
    {
        return originalEstimateSeconds != null || remainingEstimateSeconds != null || timeSpentSeconds != null;
    }

    public String getOriginalEstimate()
    {
        return originalEstimate;
    }

    public void setOriginalEstimate(String originalEstimate)
    {
        this.originalEstimate = originalEstimate;
    }

    public String getRemainingEstimate()
    {
        return remainingEstimate;
    }

    public void setRemainingEstimate(String remainingEstimate)
    {
        this.remainingEstimate = remainingEstimate;
    }

    public String getTimeSpent()
    {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent)
    {
        this.timeSpent = timeSpent;
    }

    public Long getOriginalEstimateSeconds()
    {
        return originalEstimateSeconds;
    }

    public void setOriginalEstimateSeconds(Long originalEstimateSeconds)
    {
        this.originalEstimateSeconds = originalEstimateSeconds;
    }

    public Long getRemainingEstimateSeconds()
    {
        return remainingEstimateSeconds;
    }

    public void setRemainingEstimateSeconds(Long remainingEstimateSeconds)
    {
        this.remainingEstimateSeconds = remainingEstimateSeconds;
    }

    public Long getTimeSpentSeconds()
    {
        return timeSpentSeconds;
    }

    public void setTimeSpentSeconds(Long timeSpentSeconds)
    {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    private String secondsToFormattedMinutes(Long seconds)
    {
        return seconds != null ? String.format("%dm", seconds / 60) : null;
    }

}
