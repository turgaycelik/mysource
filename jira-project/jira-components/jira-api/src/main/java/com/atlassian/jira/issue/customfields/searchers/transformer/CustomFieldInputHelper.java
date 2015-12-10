package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;

/**
 * Provides help for constructing clauses for custom fields from Search Input Transformers.
 *
 * @since v4.0
 */
public interface CustomFieldInputHelper
{
    /**
     * Given the primary clause name and the field name, returns the "unique" clause name that should be used when
     * constructing terminal clauses for this clause name. Uniqueness is calculated per user; a name could be unique
     * for one user since he only has limited view of fields, but for another user it could be non-unique.
     *
     * @param user the user performing the search
     * @param primaryName the primary name of a clause, e.g. <code>cf[10000]</code> or <code>project</code>
     * @param fieldName the name of the field associated to the clause, e.g. <code>My Custom Field</code> or <code>project</code>
     * @return the clause name which should be used in construction of terminal clauses, to guarantee that this clause
     * refers only to the one specific field.
     */
    String getUniqueClauseName(User user, String primaryName, String fieldName);
}
