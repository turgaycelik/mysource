package com.atlassian.jira.issue;

import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;

/**
 * Locator to abstract how we obtain the TemporaryAttachmentsMonitor.  Implementations should store one of these
 * monitors per user.
 *
 * @since v4.2
 */
public interface TemporaryAttachmentsMonitorLocator
{
    /**
     * Returns the current temporary attachmentsMonitor.  Creates a new one if specified when none exists yet.
     *
     * @param create Should this call create a new monitor if none exists yet
     * @return The current monitor or null.
     */
    TemporaryAttachmentsMonitor get(boolean create);
}
