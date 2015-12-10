package com.atlassian.jira.web.session;

import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.PagerFilter;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides access to getting and setting {@link PagerFilter} objects in session.
 *
 * @see SessionSearchObjectManagerFactory#createPagerFilterManager()
 * @see SessionSearchObjectManagerFactory#createPagerFilterManager(javax.servlet.http.HttpServletRequest)
 * @see SessionSearchObjectManagerFactory#createPagerFilterManager(com.atlassian.jira.util.velocity.VelocityRequestSession)
 * @since v4.2
 */
@NonInjectableComponent
public class DefaultSessionPagerFilterManager extends AbstractSessionSearchObjectManager<PagerFilter>
        implements SessionPagerFilterManager
{
    public DefaultSessionPagerFilterManager(final HttpServletRequest request, final Session session)
    {
        super(request, session);
    }

    protected String getLastViewedSessionKey()
    {
        return SessionKeys.SEARCH_PAGER;
    }
}
