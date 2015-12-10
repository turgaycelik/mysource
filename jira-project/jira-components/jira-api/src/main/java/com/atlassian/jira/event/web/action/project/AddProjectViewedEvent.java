package com.atlassian.jira.event.web.action.project;

import com.atlassian.analytics.api.annotations.Analytics;

/**
 * Fired when the AddProject action is loaded.
 *
 * @since v5.2
 */
@Analytics ("addproject.viewed")
public class AddProjectViewedEvent
{
    private final String src;

    /**
     * @param src a String to identify the source of the trigger, e.g. adminsummary, browseprojects, etc.
     */
    public AddProjectViewedEvent(String src)
    {
        this.src = src;
    }

    public String getSrc()
    {
        return src;
    }
}
