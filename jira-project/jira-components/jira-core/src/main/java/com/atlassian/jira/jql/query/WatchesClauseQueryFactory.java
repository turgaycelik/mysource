package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.WatchesIndexValueConverter;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for producing clauses for the {@link com.atlassian.jira.issue.fields.WatchesSystemField}.
 *
 * @since v4.4
 */
@InjectableComponent
public class WatchesClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(WatchesClauseQueryFactory.class);

    private final ClauseQueryFactory delegateClauseQueryFactory;
    private final WatcherManager watcherManager;

    public WatchesClauseQueryFactory(final JqlOperandResolver operandResolver, final WatchesIndexValueConverter watchesIndexValueConverter, final WatcherManager watcherManager)
    {
        this.watcherManager = watcherManager;
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new ActualValueEqualityQueryFactory(watchesIndexValueConverter));
        operatorFactories.add(new ActualValueRelationalQueryFactory(watchesIndexValueConverter));
        this.delegateClauseQueryFactory = createGenericClauseFactory(operandResolver, operatorFactories);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        if (watcherManager.isWatchingEnabled())
        {
            return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
        }
        else
        {
            log.debug("Attempt to search watches field when voting is disabled.");
            return QueryFactoryResult.createFalseResult();
        }
    }

    ///CLOVER:OFF

    GenericClauseQueryFactory createGenericClauseFactory(final JqlOperandResolver operandResolver, final List<OperatorSpecificQueryFactory> operatorFactories)
    {
        return new GenericClauseQueryFactory(SystemSearchConstants.forWatches().getIndexField(), operatorFactories, operandResolver);
    }

    ///CLOVER:ON
}
