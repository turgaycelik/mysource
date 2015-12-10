package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.search.ClauseTooComplexSearchException;
import com.atlassian.jira.issue.search.util.LuceneQueryModifier;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.search.BooleanQuery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestDefaultLuceneQueryBuilder extends MockControllerTestCase
{
    private QueryRegistry queryRegistry;
    private LuceneQueryModifier luceneQueryModifier;

    @Before
    public void setUp() throws Exception
    {
        queryRegistry = mockController.getMock(QueryRegistry.class);
        luceneQueryModifier = mockController.getMock(LuceneQueryModifier.class);
    }

    @Test
    public void testCreateLuceneQueryHappyPath() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        final QueryCreationContextImpl context = new QueryCreationContextImpl((ApplicationUser) null);
        final QueryFactoryResult falseResult = QueryFactoryResult.createFalseResult();

        final QueryVisitor queryVisitor = new MyQueryVisitor(queryRegistry, context, falseResult, null, null);
        expect(luceneQueryModifier.getModifiedQuery(falseResult.getLuceneQuery())).andReturn(falseResult.getLuceneQuery());

        mockController.replay();
        final DefaultLuceneQueryBuilder builder = new DefaultLuceneQueryBuilder(queryRegistry, luceneQueryModifier, null, null)
        {
            @Override
            QueryVisitor createQueryVisitor(final QueryCreationContext context)
            {
                return queryVisitor;
            }
        };

        assertEquals(falseResult.getLuceneQuery(), builder.createLuceneQuery(context, clause));
        mockController.verify();
    }

    @Test
    public void testCreateLuceneQueryVistorThrowsUp() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        final QueryCreationContextImpl context = new QueryCreationContextImpl((ApplicationUser) null);

        final QueryVisitor queryVisitor = new MyQueryVisitor(queryRegistry, context, null, null, null);

        mockController.replay();
        final DefaultLuceneQueryBuilder builder = new DefaultLuceneQueryBuilder(queryRegistry, luceneQueryModifier, null, null)
        {
            @Override
            QueryVisitor createQueryVisitor(final QueryCreationContext context)
            {
                return queryVisitor;
            }
        };

        try
        {
            builder.createLuceneQuery(context, clause);
            fail("Expected ClauseTooComplexSearchException to be thrown");
        }
        catch (ClauseTooComplexSearchException e) { }

        mockController.verify();
    }

    @Test
    public void testCreateLuceneQueryModifierThrowsUp() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        final QueryCreationContextImpl context = new QueryCreationContextImpl((ApplicationUser) null);
        final QueryFactoryResult falseResult = QueryFactoryResult.createFalseResult();

        final QueryVisitor queryVisitor = new MyQueryVisitor(queryRegistry, context, falseResult, null, null);
        expect(luceneQueryModifier.getModifiedQuery(falseResult.getLuceneQuery())).andThrow(new BooleanQuery.TooManyClauses());

        mockController.replay();
        final DefaultLuceneQueryBuilder builder = new DefaultLuceneQueryBuilder(queryRegistry, luceneQueryModifier, null, null)
        {
            @Override
            QueryVisitor createQueryVisitor(final QueryCreationContext context)
            {
                return queryVisitor;
            }
        };

        try
        {
            builder.createLuceneQuery(context, clause);
            fail("Expected ClauseTooComplexSearchException to be thrown");
        }
        catch (ClauseTooComplexSearchException e) { }

        mockController.verify();
    }

    private static class MyQueryVisitor extends QueryVisitor
    {
        private final QueryFactoryResult queryFactoryResult;

        MyQueryVisitor(QueryRegistry queryRegistry, QueryCreationContext queryCreationContext, QueryFactoryResult queryFactoryResult,
                WasClauseQueryFactory wasClauseQueryFactory, ChangedClauseQueryFactory changedClauseQueryFactory)
        {
            super(queryRegistry, queryCreationContext, wasClauseQueryFactory, changedClauseQueryFactory);
            this.queryFactoryResult = queryFactoryResult;
        }

        @Override
        public QueryFactoryResult visit(final TerminalClause terminalClause)
        {
            if (queryFactoryResult != null)
            {
                return queryFactoryResult;
            }
            else
            {
                throw new JqlTooComplex(terminalClause);
            }
        }
    }
}
