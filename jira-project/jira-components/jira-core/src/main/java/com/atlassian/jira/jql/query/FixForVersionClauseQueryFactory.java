package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.comparator.VersionComparator;
import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.VersionIndexInfoResolver;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;

import java.util.List;

/**
 * Creates queries for affected version clauses.
 *
 * @since v4.0
 */
public class FixForVersionClauseQueryFactory implements ClauseQueryFactory
{
    private final ClauseQueryFactory delegateClauseQueryFactory;

    public FixForVersionClauseQueryFactory(VersionResolver versionResolver, JqlOperandResolver operandResolver)
    {
        final VersionIndexInfoResolver versionIndexInfoResolver = new VersionIndexInfoResolver(versionResolver);

        // need to create a version-specific relational query factory that uses a predicate which filters out versions of other projects
        final VersionSpecificRelationalOperatorQueryFactory relationQueryFactory = new VersionSpecificRelationalOperatorQueryFactory(VersionComparator.COMPARATOR, versionResolver, versionIndexInfoResolver);
        final List<OperatorSpecificQueryFactory> operatorFactories =
                CollectionBuilder.<OperatorSpecificQueryFactory>newBuilder(
                    new EqualityWithSpecifiedEmptyValueQueryFactory<Version>(versionIndexInfoResolver, BaseFieldIndexer.NO_VALUE_INDEX_VALUE),
                    relationQueryFactory
                ).asList();

        delegateClauseQueryFactory = new GenericClauseQueryFactory(SystemSearchConstants.forFixForVersion().getIndexField(), operatorFactories, operandResolver);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
    }

}
