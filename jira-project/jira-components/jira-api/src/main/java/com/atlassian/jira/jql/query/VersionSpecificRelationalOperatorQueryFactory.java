package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.util.JqlVersionPredicate;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.Predicate;
import com.atlassian.query.operator.Operator;

import java.util.Comparator;

/**
 * A relation query factory for version which overrides the the createPredicate method to return a predicate that
 * checks the project is the same.
 *
 * @since v4.0
 */
public class VersionSpecificRelationalOperatorQueryFactory extends RelationalOperatorIdIndexValueQueryFactory<Version>
{
///CLOVER:OFF
    public VersionSpecificRelationalOperatorQueryFactory(final Comparator<? super Version> comparator, final NameResolver<Version> versionNameResolver, final IndexInfoResolver<Version> versionIndexInfoResolver)
    {
        super(comparator, versionNameResolver, versionIndexInfoResolver);
    }

    @Override
    Predicate<Version> createPredicate(final Operator operator, final Version domainObject)
    {
        return new JqlVersionPredicate(operator, domainObject);
    }
///CLOVER:ON
}
