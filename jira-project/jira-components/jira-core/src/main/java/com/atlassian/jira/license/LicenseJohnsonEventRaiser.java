package com.atlassian.jira.license;

import javax.servlet.ServletContext;

/**
 * This can raise its Johnson with events related to licensing
 *
 * @since v4.0
 */
public interface LicenseJohnsonEventRaiser
{
    /**
     * This is the key used to indicate to Johnson that the license is too old
     */
    String LICENSE_TOO_OLD = "license-too-old";

    /**
     * This is the key used to indicate to Johnson that we've encountered a clustering license issue.
     */
    String CLUSTERING_UNLICENSED = "clustering-unlicensed";

    /**
     * Indicates to Johnson that the license is too old AND we're a subscription license
     */
    String SUBSCRIPTION_EXPIRED = "subscription-expired";

    /**
     * This is the key used to indicate to Johnson that we've encountered a general clustering issue.
     */
    String CLUSTERING = "clustering";

    /**
     * Checks whether the license is too old for this JIRA instance, and raise a Johnson {@link #LICENSE_TOO_OLD} event if it is the case.
     * @param servletContext the current servlet context
     * @param licenseDetails the current license details
     * @return {@code true} if the license is too old and the Johnson event has been raised.
     */
    boolean checkLicenseIsTooOldForBuild(ServletContext servletContext, LicenseDetails licenseDetails);

}
