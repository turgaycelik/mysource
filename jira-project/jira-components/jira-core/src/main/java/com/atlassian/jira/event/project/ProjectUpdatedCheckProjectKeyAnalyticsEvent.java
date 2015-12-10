package com.atlassian.jira.event.project;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Analytics Event to track how many Project Updates are done with/without ProjectKey change.
 * In case ProjectKey is changed during update then "administration.project.projectupdated.projectkey.changed" event is raised.
 * In case ProjectKey is not changed during update then "administration.project.projectupdated.projectkey.notchanged" event is raised.
 *
 * @since v6.2
 */

public class ProjectUpdatedCheckProjectKeyAnalyticsEvent
{
    private final String oldProjectKey;
    private final String newProjectKey;

    public ProjectUpdatedCheckProjectKeyAnalyticsEvent(String oldProjectKey, String newProjectKey)
    {
        this.oldProjectKey = oldProjectKey;
        this.newProjectKey = newProjectKey;
    }

    @EventName
    public String calculateEventName()
    {
        return "administration.project.projectupdated.projectkey." + (newProjectKey.equals(oldProjectKey) ? "notchanged" : "changed");
    }

    public String getOldProjectKey()
    {
        return oldProjectKey;
    }

    public String getNewProjectKey()
    {
        return newProjectKey;
    }
}
