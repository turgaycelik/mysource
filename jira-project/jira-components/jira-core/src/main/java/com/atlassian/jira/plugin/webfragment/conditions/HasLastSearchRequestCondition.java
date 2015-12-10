package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchRequestManager;

/**
 * Condition that checks if a user has a current search.
 *
 * @since v4.0
 */
public class HasLastSearchRequestCondition extends AbstractJiraCondition
{
    private final VelocityRequestContextFactory requestContextFactory;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;

    public HasLastSearchRequestCondition(VelocityRequestContextFactory requestContextFactory, final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory)
    {
        this.requestContextFactory = requestContextFactory;
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
    }

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        final VelocityRequestContext velocityRequestContext = requestContextFactory.getJiraVelocityRequestContext();
        if (velocityRequestContext != null)
        {
            final VelocityRequestSession session = velocityRequestContext.getSession();
            if (session != null)
            {
                SessionSearchRequestManager sessionSearchRequestManager = sessionSearchObjectManagerFactory.createSearchRequestManager(session);
                return sessionSearchRequestManager.getCurrentObject() != null;
            }
        }
        return false;
    }
}

