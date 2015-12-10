package com.atlassian.jira.web.session;

import com.atlassian.jira.issue.pager.NextPreviousPager;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.SessionKeys;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides access to getting and setting {@link NextPreviousPager} objects in session.
 *
 * @see SessionSearchObjectManagerFactory#createNextPreviousPagerManager()
 * @see SessionSearchObjectManagerFactory#createNextPreviousPagerManager(javax.servlet.http.HttpServletRequest)
 * @see SessionSearchObjectManagerFactory#createNextPreviousPagerManager(com.atlassian.jira.util.velocity.VelocityRequestSession)
 * @since v4.2
 */
@NonInjectableComponent
public class DefaultSessionNextPreviousPagerManager extends AbstractSessionSearchObjectManager<NextPreviousPager>
        implements SessionNextPreviousPagerManager
{
    public DefaultSessionNextPreviousPagerManager(final HttpServletRequest request, final Session session)
    {
        super(request, session);
    }

    protected String getLastViewedSessionKey()
    {
        return SessionKeys.NEXT_PREV_PAGER;
    }
}
