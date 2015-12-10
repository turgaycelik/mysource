package com.atlassian.jira.issue.search.parameters.lucene;

import com.atlassian.crowd.embedded.api.User;
import org.apache.lucene.search.Query;

/**
 * @since v4.0
 */
public interface PermissionsFilterGenerator
{
    /**
     * Generates a lucene {@link Query} that is the canonical set of permissions for viewable issues for the given user.
     * This query can then be used to filter out impermissible documents from a lucene search.
     *
     * @param searcher the user performing the search
     * @return the query; could be null if an error occurred.
     */
    Query getQuery(User searcher);
}
