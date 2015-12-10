package com.atlassian.jira.gadgets.system.util;

/**
 * Figures out whether this instance has Greenhopper (taking into account whether or not this is an OnDemand instance)
 *
 * @since v6.1
 */
public interface GreenhopperLicenseChecker
{
    /**
     *
     * @return true if this OnDemand instance has Greenhopper running (either trial or paid), false otherwise
     */
    boolean greenhopperIsActiveAndLicensed();
}
