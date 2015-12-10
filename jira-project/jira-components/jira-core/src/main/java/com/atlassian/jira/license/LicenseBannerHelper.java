package com.atlassian.jira.license;

import javax.annotation.Nonnull;

/**
 * Knows how to generate and control the license banner. A license warning is displayed on the UI
 * if the license is about to expire.
 *
 * @since v6.3
 */
public interface LicenseBannerHelper
{
    /**
     * Return the license banner to display in JIRA.
     *
     * @return the license banner to display in JIRA. {@code ""} is returned when no banner should be displayed.
     */
    @Nonnull
    String getBanner();

    /**
     * Hide the license banner for the calling user. The banner will re-appear later.
     */
    void remindMeLater();

    /**
     * Hide the license banner for the calling user. The banner will not re-appear.
     */
    void remindMeNever();

    /**
     * Reset the remind me state of the current user.
     */
    void clearRemindMe();
}
