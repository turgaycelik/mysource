package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.VotesIndexValueConverter;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for producing clauses for the {@link com.atlassian.jira.issue.fields.VotesSystemField}.
 *
 * @since v4.0
 */
@InjectableComponent
public class VotesClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(VotesClauseQueryFactory.class);

    private final ClauseQueryFactory delegateClauseQueryFactory;
    private final VoteManager voteManager;

    public VotesClauseQueryFactory(final JqlOperandResolver operandResolver, final VotesIndexValueConverter votesIndexValueConverter, final VoteManager voteManager)
    {
        this.voteManager = voteManager;
        List<OperatorSpecificQueryFactory> operatorFactories = new ArrayList<OperatorSpecificQueryFactory>();
        operatorFactories.add(new ActualValueEqualityQueryFactory(votesIndexValueConverter));
        operatorFactories.add(new ActualValueRelationalQueryFactory(votesIndexValueConverter));
        this.delegateClauseQueryFactory = createGenericClauseFactory(operandResolver, operatorFactories);
    }

    public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause)
    {
        if (voteManager.isVotingEnabled())
        {
            return delegateClauseQueryFactory.getQuery(queryCreationContext, terminalClause);
        }
        else
        {
            log.debug("Attempt to search votes field when voting is disabled.");
            return QueryFactoryResult.createFalseResult();
        }
    }

    ///CLOVER:OFF

    GenericClauseQueryFactory createGenericClauseFactory(final JqlOperandResolver operandResolver, final List<OperatorSpecificQueryFactory> operatorFactories)
    {
        return new GenericClauseQueryFactory(SystemSearchConstants.forVotes().getIndexField(), operatorFactories, operandResolver);
    }

    ///CLOVER:ON
}
