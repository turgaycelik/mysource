/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

/**
 * Create the indexed field and then query terms to find entities that have permissions related to a particular {@link ShareType}.
 *
 * @since v3.13
 */
public interface ShareQueryFactory<S extends ShareTypeSearchParameter>
{
    /**
     * Get the search query for the shares that can be seen by the passed user for the passed parameter. Used when the user has specified something to
     * search for.
     *
     * @param parameter the parameters for the search.
     * @param user the user to perform the search on behalf of.
     * @return an array of Terms. It may be empty but never null.
     */
    Query getQuery(ShareTypeSearchParameter parameter, ApplicationUser user);

    /**
     * @deprecated Use {@link #getQuery(com.atlassian.jira.sharing.search.ShareTypeSearchParameter, com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     *
     * Get the search query for the shares that can be seen by the passed user for the passed parameter. Used when the user has specified something to
     * search for.
     *
     * @param parameter the parameters for the search.
     * @param user the user to perform the search on behalf of.
     * @return an array of Terms. It may be empty but never null.
     */
    Query getQuery(ShareTypeSearchParameter parameter, User user);

    /**
     * Get the search query for all shares shares that match the passed parameter. Used by the system to match all entities that match the parameter,
     * for instance when deleting Group, Project or Role.
     *
     * @param parameter the parameters for the search.
     * @return an array of Terms. It may be empty but never null.
     */
    Query getQuery(ShareTypeSearchParameter parameter);

    /**
     * Get the search terms for the shares that can be seen by the passed user.
     *
     * @param user the user to perform the search on behalf of.
     * @return an array of Terms. It may be empty but never null.
     */
    Term[] getTerms(ApplicationUser user);


    /**
     * @deprecated Use {@link #getTerms(com.atlassian.jira.user.ApplicationUser)} instead. Since v6.0.
     *
     * Get the search terms for the shares that can be seen by the passed user.
     *
     * @param user the user to perform the search on behalf of.
     * @return an array of Terms. It may be empty but never null.
     */
    Term[] getTerms(User user);

    /**
     * Get the Field we will later search for.
     *
     * @param permission
     * @return the field so we can add it to the index
     */
    Field getField(SharedEntity entity, SharePermission permission);
}
