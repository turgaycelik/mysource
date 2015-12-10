package com.atlassian.jira.issue.fields.rest.json.beans;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * This bean holds the time tracking information that is sent back to clients of the REST API.
 *
 * @since v5.0
 */
public class TimeTrackingJsonBean
{
    @JsonProperty
    private String originalEstimate;

    @JsonProperty
    private String remainingEstimate;

    @JsonProperty
    private String timeSpent;

    @JsonProperty
    private Long originalEstimateSeconds;

    @JsonProperty
    private Long remainingEstimateSeconds;

    @JsonProperty
    private Long timeSpentSeconds;

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

    /**
     *
     * @return null if the input is null
     */
    public static TimeTrackingJsonBean shortBean(final String originalEstimate, final String estimate, final String timeSpent, final Long originalEstimateSeconds, final Long estimateSeconds, final Long timeSpentSeconds)
    {
        final TimeTrackingJsonBean bean = new TimeTrackingJsonBean();
        bean.originalEstimate = originalEstimate;
        bean.remainingEstimate = estimate;
        bean.timeSpent = timeSpent;
        bean.originalEstimateSeconds = originalEstimateSeconds;
        bean.remainingEstimateSeconds = estimateSeconds;
        bean.timeSpentSeconds = timeSpentSeconds;

        return bean;
    }
}
