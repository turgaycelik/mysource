package com.atlassian.jira.plugin.studio;

import javax.annotation.Nonnull;

/**
 * Provides an series of callbacks that studio needs to be able to integrate with JIRA.
 *
 * All of these methods will need to be thread safe. These method can be called by multiple threads at the same time.
 * The same method can be called by my multiple threads at the same time.
 *
 * All of these methods will need to be performant. JIRA will not do any caching of the results and will probably
 * call a method with the same arguments multiple times (possibly at the same time).
 *
 * @since v4.4.2
 */
public interface StudioHooks
{
    /**
     * Called by JIRA to get an instance of StudioLicenseHooks that it will callback when making some licensing
     * decisions. See {@link StudioLicenseHooks} for more details.
     *
     * @return an instance of StudioLicenseHooks that JIRA will use to make some licensing decisions.
     */
    @Nonnull
    public StudioLicenseHooks getLicenseHooks();
}
