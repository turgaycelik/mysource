package com.atlassian.jira.portal.events;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Event fired when a portal page gets created.
 * @since 5.0
 */
public final class DashboardCreated
{
    public final PortalPage page;
    public final User loggedInUser;

    public DashboardCreated(final PortalPage page, final User user)
    {
        this.page = page;
        this.loggedInUser = user;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
