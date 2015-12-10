/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import org.apache.lucene.search.Query;

/**
 * Build a Lucene query for {@link SharedEntity shared entities}.
 * 
 * @since v3.13
 */
public interface QueryFactory
{
    /**
     * Get a query with permission checks for the specified user.
     * 
     * @param searchParameters to search for
     * @param user the user to limit the results for, null is anonymous
     * @return the query to search a lucene index
     */
    Query create(final SharedEntitySearchParameters searchParameters, User user);

    /**
     * Get a query without any permission checks.
     * 
     * @param searchParameters to search for
     * @return the query to search a lucene index
     */
    Query create(final SharedEntitySearchParameters searchParameters);
}
