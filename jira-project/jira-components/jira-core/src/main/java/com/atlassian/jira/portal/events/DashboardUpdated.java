package com.atlassian.jira.portal.events;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.portal.PortalPage;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @since v5.0
 */
public class DashboardUpdated
{
    public final PortalPage oldPage;
    public final PortalPage newPage;
    public final User loggedInUser;

    public DashboardUpdated(final PortalPage oldPage, final PortalPage newPage, final User user)
    {
        this.oldPage = oldPage;
        this.newPage = newPage;
        this.loggedInUser = user;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
