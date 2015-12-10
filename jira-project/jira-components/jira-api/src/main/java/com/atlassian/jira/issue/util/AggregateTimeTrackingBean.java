package com.atlassian.jira.issue.util;

/**
 * Bean to store values of Time Tracking Aggregate values.
 * It is more efficient to calculate them all at the same time (looping only once), and an instance of this is returned.
 */
public class AggregateTimeTrackingBean
{
    public static final String AGG_TIMETRACKING = "atl.jira.timetracking.aggregate.bean.";    
    
    private Long timeSpent;
    private Long originalEstimate;
    private Long remainingEstimate;

    private int subtaskCount = 0;
    private Long greastestSubTaskEstimate = null;

    public AggregateTimeTrackingBean(Long originalEstimate, Long remainingEstimate, Long timeSpent, int subtaskCount)
    {
        this.timeSpent = timeSpent;
        this.originalEstimate = originalEstimate;
        this.remainingEstimate = remainingEstimate;
        this.subtaskCount = subtaskCount;
    }

    public Long getTimeSpent()
    {
        return timeSpent;
    }

    public void setTimeSpent(Long timeSpent)
    {
        this.timeSpent = timeSpent;
    }

    public Long getOriginalEstimate()
    {
        return originalEstimate;
    }

    public void setOriginalEstimate(Long originalEstimate)
    {
        this.originalEstimate = originalEstimate;
    }

    public Long getRemainingEstimate()
    {
        return remainingEstimate;
    }

    public void setRemainingEstimate(Long remainingEstimate)
    {
        this.remainingEstimate = remainingEstimate;
    }

    public int getSubTaskCount()
    {
        return subtaskCount;
    }

    public void setSubTaskCount(int subtaskCount)
    {
        this.subtaskCount = subtaskCount;
    }

    /**
     * This property is used to track the largest sub task estimate encountered so far.  Need to help the UI
     * work out how big things should be when it is called piece meal to render the UI.  It
     *
     * @return a Long value which is the greater of the original estimate OR the remaining estimate plus the time spent.
     */
    public Long getGreastestSubTaskEstimate()
    {
        return greastestSubTaskEstimate;
    }

    public void setGreastestSubTaskEstimate(Long greastestSubTaskEstimate)
    {
        this.greastestSubTaskEstimate = greastestSubTaskEstimate;
    }

    /**
     * When this is called, the current values of the bean are used to work out the the greatest sub task estimate.
     * TimeSpent is added to remainingEstimate and comapred to teh originalEstimate.  The larger is then compared with
     * the beans current greatest estimate and replaces it if greater.
     *
     * @param originalEstimate  an issue's original estimate
     * @param remainingEstimate an issue's remaining estimate
     * @param timeSpent         an issue time spent
     */
    public void bumpGreatestSubTaskEstimate(Long originalEstimate, Long remainingEstimate, Long timeSpent)
    {
        Long estimate = getTheGreaterOfEstimates(originalEstimate, remainingEstimate, timeSpent);
        if (estimate != null)
        {
            final Long singleEstimate = this.getGreastestSubTaskEstimate();
            if (singleEstimate == null)
            {
                this.setGreastestSubTaskEstimate(estimate);
            }
            else
            {
                if (estimate.longValue() > singleEstimate.longValue())
                {
                    this.setGreastestSubTaskEstimate(estimate);
                }
            }
        }
    }

    /**
     * Finds the greater of the original estimate OR the remaining estimate plus the time spent.
     *
     * @param originalEstimate  original estimate
     * @param remainingEstimate remaining estimate
     * @param timeSpent         the time spent
     * @return the greater of the original estimate OR the remaining estimate plus the time spent.
     */
    public static Long getTheGreaterOfEstimates(Long originalEstimate, Long remainingEstimate, Long timeSpent)
    {
        final Long totalTime = addAndPreserveNull(remainingEstimate, timeSpent);
        if (originalEstimate == null)
        {
            return totalTime;
        }
        else
        {
            return totalTime == null ? originalEstimate : new Long(Math.max(originalEstimate.longValue(), totalTime.longValue()));
        }
    }

    /**
     * Method to calculate the addition of two Longs, while preserving null if they are both null.
     *
     * @param estValue  value from issue
     * @param origValue value from bean
     * @return new Long based on the addition of the params.
     */
    public static Long addAndPreserveNull(Long estValue, Long origValue)
    {
        final Long newValue;
        if (estValue != null)
        {
            if (origValue == null)
            {
                newValue = estValue;
            }
            else
            {
                newValue = new Long(estValue.longValue() + origValue.longValue());
            }
        }
        else
        {
            newValue = origValue;
        }
        return newValue;
    }
}
