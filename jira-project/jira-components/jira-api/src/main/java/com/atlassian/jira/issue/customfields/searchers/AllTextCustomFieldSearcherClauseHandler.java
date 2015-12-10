package com.atlassian.jira.issue.customfields.searchers;

/**
 * The "all text" clause aggregates all the free text system fields in the system. To include custom fields in the
 * "all text" clause use this marker interface. This interface has to be implemented by an implementation of the
 * {@link CustomFieldSearcherClauseHandler}, and the set of supported operators must include the LIKE operator. 
 *
 * For an example usage of this interface see the {@link SimpleAllTextCustomFieldSearcherClauseHandler}.
 *
 * @since v4.0
 */
public interface AllTextCustomFieldSearcherClauseHandler
{
}
