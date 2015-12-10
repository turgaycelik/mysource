package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.parameters.lucene.PermissionQueryFactory;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import org.apache.log4j.Logger;

import static com.atlassian.jira.issue.search.constants.SystemSearchConstants.forVoters;
import static com.atlassian.jira.jql.query.PermissionClauseQueryFactory.create;
import static com.atlassian.jira.jql.query.QueryFactoryResult.createFalseResult;

/**
 * Factory for producing clauses for the voters.
 *
 * @since v4.1
 */
@InjectableComponent
public class VoterClauseQueryFactory implements ClauseQueryFactory
{
    private static final Logger log = Logger.getLogger(VoterClauseQueryFactory.class);

    private final ClauseQueryFactory delegateClauseQueryFactory;
    private final VoteManager voteManager;

    public VoterClauseQueryFactory(final JqlOperandResolver operandResolver, final UserResolver userResolver, final VoteManager voteManager, final PermissionQueryFactory permissionQueryFactory)
    {
        this.voteManager = voteManager;
        delegateClauseQueryFactory = create(operandResolver, userResolver, permissionQueryFactory, forVoters().getIndexField());
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
            return createFalseResult();
        }
    }
}
