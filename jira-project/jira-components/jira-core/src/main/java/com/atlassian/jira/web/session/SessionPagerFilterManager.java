package com.atlassian.jira.web.session;

import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.bean.PagerFilter;

/**
 * Provides access to getting and setting {@link PagerFilter} objects in session.
 *
 * @since v4.2
 * @see SessionSearchObjectManagerFactory#createPagerFilterManager()
 * @see SessionSearchObjectManagerFactory#createPagerFilterManager(javax.servlet.http.HttpServletRequest)
 * @see SessionSearchObjectManagerFactory#createPagerFilterManager(com.atlassian.jira.util.velocity.VelocityRequestSession)
 */
@NonInjectableComponent
public interface SessionPagerFilterManager extends SessionSearchObjectManager<PagerFilter>
{
}
