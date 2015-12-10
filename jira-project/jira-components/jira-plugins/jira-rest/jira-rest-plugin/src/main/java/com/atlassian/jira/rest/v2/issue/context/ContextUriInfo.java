package com.atlassian.jira.rest.v2.issue.context;

import javax.ws.rs.core.UriInfo;

/**
 * Scoped instances of UriInfo that implement this interface are scoped to a REST method invocation.
 *
 * @since v4.2
 * @see javax.ws.rs.core.UriInfo
 */
public interface ContextUriInfo extends UriInfo
{
    // empty
}
