package com.atlassian.jira.jql.query;

import java.util.List;

import com.atlassian.jira.issue.vote.DefaultVoteManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.VotesIndexValueConverter;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.search.BooleanQuery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestVotesClauseQueryFactory extends MockControllerTestCase
{
    private DefaultVoteManager defaultVoteManager;
    private MockJqlOperandResolver jqlOperandResolver;
    private VotesIndexValueConverter votesIndexValueConverter;

    @Before
    public void setUp() throws Exception
    {
        defaultVoteManager = mockController.getMock(DefaultVoteManager.class);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        votesIndexValueConverter = mockController.getMock(VotesIndexValueConverter.class);
    }

    @Test
    public void testVotingDisabled() throws Exception
    {
        defaultVoteManager.isVotingEnabled();
        mockController.setReturnValue(false);

        mockController.replay();
        final VotesClauseQueryFactory factory = new VotesClauseQueryFactory(jqlOperandResolver, votesIndexValueConverter, defaultVoteManager);
        final TerminalClauseImpl clause = new TerminalClauseImpl("a", Operator.EQUALS, "clause");
        final QueryFactoryResult result = factory.getQuery(null, clause);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
        mockController.verify();
    }

    @Test
    public void testVotingEnabled() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("a", Operator.EQUALS, "clause");
        final GenericClauseQueryFactory genericClauseQueryFactory = mockController.getMock(GenericClauseQueryFactory.class);

        defaultVoteManager.isVotingEnabled();
        mockController.setReturnValue(true);

        genericClauseQueryFactory.getQuery(null, clause);
        mockController.setReturnValue(new QueryFactoryResult(new BooleanQuery()));

        mockController.replay();
        final VotesClauseQueryFactory factory = new VotesClauseQueryFactory(jqlOperandResolver, votesIndexValueConverter, defaultVoteManager)
        {
            @Override
            GenericClauseQueryFactory createGenericClauseFactory(final JqlOperandResolver operandResolver, final List<OperatorSpecificQueryFactory> operatorFactories)
            {
                return genericClauseQueryFactory;
            }
        };

        factory.getQuery(null, clause);        
        mockController.verify();
    }
}
