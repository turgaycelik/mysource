package com.atlassian.jira.plugin.report.impl;

/**
 * Responsible for calculating the accuracy of estimates.
 * <p/>
 * Implementations should be re-entrant and therefore thread-safe.
 *
 * @since v3.11
 */
interface AccuracyCalculator
{
    /**
     * Calculates, formats and returns accuracy based on the values of estimates passed in.
     *
     * @param originalEstimate  original estimate
     * @param remainingEstimate remaining estimate
     * @param timeSpent         time spent
     * @return accuracy, null if not possible to calclate (ie. original estimate is null)
     */
    Long calculateAccuracy(Long originalEstimate, Long remainingEstimate, Long timeSpent);

    /**
     * Calculates, formats and returns accuracy based on the values of estimates passed in.
     *
     * @param originalEstimate  original estimate
     * @param remainingEstimate remaining estimate
     * @param timeSpent         time spent
     * @return accuracy, null if not possible to calclate (ie. original estimate is null)
     */
    String calculateAndFormatAccuracy(Long originalEstimate, Long remainingEstimate, Long timeSpent);

    /**
     * Calculates and returns zero if on schedule, positive if ahead of schedule, negative if behind schedule.
     *
     * @param originalEstimate  original estimate
     * @param remainingEstimate remaining estimate
     * @param timeSpent         time spent
     * @return zero if on schedule, positive if ahead of schedule, negative if behind schedule
     */
    int onSchedule(Long originalEstimate, Long remainingEstimate, Long timeSpent);

    static class Percentage
    {
        static int calculate(long originalEstimate,long timeSpent, long remainingEstimate)
        {
            return (originalEstimate == 0) ? 0 : (int) (((float) (originalEstimate - timeSpent - remainingEstimate) / (float) originalEstimate) * 100F);
        }
    }
}
