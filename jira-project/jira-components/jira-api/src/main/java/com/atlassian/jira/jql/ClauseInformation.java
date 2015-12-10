package com.atlassian.jira.jql;

import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.query.operator.Operator;

import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Used to tie together field names, JQL clause names, and document constant names.
 *
 * @since v4.0
 */
@Immutable
public interface ClauseInformation
{
    /**
     * @return the allowed JQL clause names.
     */
    ClauseNames getJqlClauseNames();

    /**
     * @return the string that represents the field id in the lucene index; may be null if the clause does not search
     * directly on the index e.g. "saved filter" or "all text" clause.
     */
    @Nullable
    String getIndexField();

    /**
     * @return the system or custom field id that this clause is associated with; may be null if the clause does not
     * have a corresponding field e.g. "parent issue" or "saved filter" clause.
     */
    @Nullable
    String getFieldId();

    /**
     * Provides a set of the supported {@link com.atlassian.query.operator.Operator}'s that this custom field searcher
     * can handle for its searching.
     *
     * @return a set of supported operators.
     */
    Set<Operator> getSupportedOperators();

    /**
     * Provides the {@link com.atlassian.jira.JiraDataType} that this clause handles and searches on. This allows us
     * to infer some information about how the search will behave and how it will interact with other elements in
     * the system.
     *
     * For example, if this returns {@link com.atlassian.jira.JiraDataTypes#DATE} then we know that we could provide
     * users with a date picker for an input field, and we know that this clause should only be used by functions
     * that also specify dates.
     *
     * @see com.atlassian.jira.JiraDataTypes
     *
     * @return the JiraDataType that this clause can handle.
     */
    JiraDataType getDataType();
}
