package com.atlassian.jira.portal.events;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntity;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

/**
 * All you get is a SharedEntity identifier because by the time you receive this event,
 * the dashboard itself is actually gone.
 * @since v5.0
 */
public class DashboardDeleted
{
    public final SharedEntity dashboard;
    public final User loggedInUser;

    public DashboardDeleted(final SharedEntity deleted, final User user)
    {
        this.dashboard = deleted;
        this.loggedInUser = user;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
