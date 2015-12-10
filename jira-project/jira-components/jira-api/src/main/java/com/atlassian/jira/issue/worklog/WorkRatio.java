/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.worklog;

import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

public class WorkRatio
{
    // Returns a string representing the ratio of actual time spent vs original time estimate.
    public static long getWorkRatio(Issue issue)
    {
        return getWorkRatio(issue.getOriginalEstimate(), issue.getTimeSpent());
    }

    // Returns a string representing the ratio of actual time spent vs original time estimate.
    public static long getWorkRatio(GenericValue issue)
    {
        Long originalEstimate = (Long) issue.get("timeoriginalestimate");
        Long timespent = (Long) issue.get("timespent");
        return getWorkRatio(originalEstimate, timespent);
    }

    /**
     * Returns the work ratio as a whole number percentage, rounded down.
     * Note that we round down to be consistent with {@link com.atlassian.jira.web.bean.PercentageGraphModel.RowPercentageCalculator#getPercentage(long)}
     *
     * @param originalEstimate the original estimate of the issue
     * @param timespent the time spent on the issue
     * @return the work ratio percentage
     */
    public static long getWorkRatio(Long originalEstimate, Long timespent)
    {
        float timeSpent;
        float timeOriginalEstimate;
        float timeRatio;

        if (originalEstimate != null)
        {
            if (timespent != null)
            {
                timeOriginalEstimate = originalEstimate;
                timeSpent = timespent;
                timeRatio = (timeSpent / timeOriginalEstimate);
                return (long) Math.floor(timeRatio * 100);
            }
            else
            {
                return 0;
            }
        }
        // A work ratio does not exist for this issue - set to '-1' so as this issue appears correctly when ordered in navigator columns.
        return -1;
    }

    // Returns a 'padded' string representing the ratio of actual time spent vs original time estimate.
    // The string is padded so that it can be used in a lucene range query.
    public static String getPaddedWorkRatio(GenericValue issue)
    {
        String ratio = String.valueOf(getWorkRatio(issue));
        return getPaddedWorkRatioString(ratio);
    }

    public static String getPaddedWorkRatioString(String ratio)
    {
        int length = ratio.length();

        // Ratios of -1 indeicate that no ratio exists - do not pad these
        if (WorklogKeys.PADLENGTH > length && !("-1").equals(ratio))
        {
            for (int i = 0; i < (WorklogKeys.PADLENGTH - length); i++)
            {
                ratio = "0" + ratio;
            }
        }

        return ratio;
    }
}
