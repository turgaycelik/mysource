package com.atlassian.jira.web.session;

import com.atlassian.jira.issue.pager.NextPreviousPager;
import com.atlassian.jira.util.NonInjectableComponent;

/**
 * Provides access to getting and setting {@link NextPreviousPager} objects in session.
 *
 * @since v4.2
 * @see SessionSearchObjectManagerFactory#createNextPreviousPagerManager()
 * @see SessionSearchObjectManagerFactory#createNextPreviousPagerManager(javax.servlet.http.HttpServletRequest)
 * @see SessionSearchObjectManagerFactory#createNextPreviousPagerManager(com.atlassian.jira.util.velocity.VelocityRequestSession)
 */
@NonInjectableComponent
public interface SessionNextPreviousPagerManager extends SessionSearchObjectManager<NextPreviousPager>
{
}
