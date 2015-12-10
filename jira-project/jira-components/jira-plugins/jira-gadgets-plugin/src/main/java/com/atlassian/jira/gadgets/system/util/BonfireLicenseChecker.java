package com.atlassian.jira.gadgets.system.util;

/**
 * Figures out whether this instance has Bonfire (taking into account whether or not this is an OnDemand instance)
 *
 * @since v6.1
 */
public interface BonfireLicenseChecker
{
    /**
     *
     * @return true if this OnDemand instance has Bonfire running (either trial or paid), false otherwise
     */
    boolean bonfireIsActiveAndLicensed();
}
