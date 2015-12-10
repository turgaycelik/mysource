package com.atlassian.jira.web.session;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.util.NonInjectableComponent;

/**
 * Provides access to getting and setting {@link SearchRequest} objects in session.
 *
 * @since v4.2
 * @see SessionSearchObjectManagerFactory#createSearchRequestManager()
 * @see SessionSearchObjectManagerFactory#createSearchRequestManager(javax.servlet.http.HttpServletRequest)
 * @see SessionSearchObjectManagerFactory#createSearchRequestManager(com.atlassian.jira.util.velocity.VelocityRequestSession)
 */
@NonInjectableComponent
public interface SessionSearchRequestManager extends SessionSearchObjectManager<SearchRequest>
{
}
