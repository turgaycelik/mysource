package com.atlassian.jira.jql.builder;

/**
 * Factory for creating new instances of JqlClauseBuilder.
 * You normally wouldn't call this directly, it is used inside {@link JqlQueryBuilder}.
 *
 * @since v4.4
 * @see JqlQueryBuilder
 */
public interface JqlClauseBuilderFactory
{
    JqlClauseBuilder newJqlClauseBuilder(JqlQueryBuilder parent);
}
