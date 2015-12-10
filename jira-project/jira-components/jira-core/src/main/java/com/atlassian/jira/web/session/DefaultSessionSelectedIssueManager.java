package com.atlassian.jira.web.session;

import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.SessionKeys;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides access to getting and setting the selected issue (stored as a {@link Long}) in session.
 *
 * @see SessionSearchObjectManagerFactory#createSelectedIssueManager()
 * @see SessionSearchObjectManagerFactory#createSelectedIssueManager(javax.servlet.http.HttpServletRequest)
 * @see SessionSearchObjectManagerFactory#createSelectedIssueManager(com.atlassian.jira.util.velocity.VelocityRequestSession)
 * @since v4.2
 */
@NonInjectableComponent
public class DefaultSessionSelectedIssueManager extends AbstractSessionSearchObjectManager<SessionSelectedIssueManager.SelectedIssueData>
        implements SessionSelectedIssueManager
{
    public DefaultSessionSelectedIssueManager(final HttpServletRequest request, final Session session)
    {
        super(request, session);
    }

    protected String getLastViewedSessionKey()
    {
        return SessionKeys.SEARCH_CURRENT_ISSUE;
    }
}
