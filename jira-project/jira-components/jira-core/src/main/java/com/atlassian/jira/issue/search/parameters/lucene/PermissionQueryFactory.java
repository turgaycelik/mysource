package com.atlassian.jira.issue.search.parameters.lucene;

import com.atlassian.jira.user.ApplicationUser;
import org.apache.lucene.search.Query;

/**
 * Factory for generating a permission query based on a specific permission.
 * 
 * @since 4.1
 */
public interface PermissionQueryFactory
{
    /**
     * Generate a permission query for a specific permission.
     * 
     * @param searcher the user who is doing the searching
     * @param permissionId the specific permission
     * @return a permission query for that user
     */
    Query getQuery(final ApplicationUser searcher, final int permissionId);
}
