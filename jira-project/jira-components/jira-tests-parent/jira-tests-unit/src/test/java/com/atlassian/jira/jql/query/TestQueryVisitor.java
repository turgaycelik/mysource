package com.atlassian.jira.jql.query;

import java.util.Collections;

import com.atlassian.jira.jql.clause.DeMorgansVisitor;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestQueryVisitor
{
    @Test
    public void testVisitAndClauseMustNotOccur() throws Exception
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        QueryVisitor queryVisitor = new QueryVisitor(null, null, deMorgansVisitor, null, null);

        final MockControl mockClauseControl = MockControl.createStrictControl(Clause.class);
        final Clause mockClause = (Clause) mockClauseControl.getMock();
        mockClause.accept(deMorgansVisitor);
        mockClauseControl.setReturnValue(mockClause);
        mockClause.accept(queryVisitor);
        mockClauseControl.setReturnValue(new QueryFactoryResult(new BooleanQuery(), true));
        mockClauseControl.replay();

        final MockControl mockClauseControl2 = MockControl.createStrictControl(Clause.class);
        final Clause mockClause2 = (Clause) mockClauseControl2.getMock();
        mockClause2.accept(deMorgansVisitor);
        mockClauseControl2.setReturnValue(mockClause2);
        mockClause2.accept(queryVisitor);
        mockClauseControl2.setReturnValue(new QueryFactoryResult(new BooleanQuery(), true));
        mockClauseControl2.replay();

        AndClause andClause = new AndClause(mockClause, mockClause2);

        final QueryFactoryResult query = queryVisitor.visit(andClause);

        assertFalse(query.mustNotOccur());
        assertTrue(query.getLuceneQuery() instanceof BooleanQuery);
        BooleanQuery booleanQuery = (BooleanQuery)query.getLuceneQuery();
        final BooleanClause[] booleanClauses = booleanQuery.getClauses();
        assertEquals(2, booleanClauses.length);
        assertEquals(BooleanClause.Occur.MUST_NOT, booleanClauses[0].getOccur());
        assertEquals(BooleanClause.Occur.MUST_NOT, booleanClauses[1].getOccur());

        mockClauseControl.verify();
        mockClauseControl2.verify();
    }

    @Test
    public void testVisitAndClauseHappyPath() throws Exception
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        QueryVisitor queryVisitor = new QueryVisitor(null, null, deMorgansVisitor, null, null);

        final MockControl mockClauseControl = MockControl.createStrictControl(Clause.class);
        final Clause mockClause = (Clause) mockClauseControl.getMock();
        mockClause.accept(deMorgansVisitor);
        mockClauseControl.setReturnValue(mockClause);
        mockClause.accept(queryVisitor);
        mockClauseControl.setReturnValue(new QueryFactoryResult(new BooleanQuery(), true));
        mockClauseControl.replay();

        final MockControl mockClauseControl2 = MockControl.createStrictControl(Clause.class);
        final Clause mockClause2 = (Clause) mockClauseControl2.getMock();
        mockClause2.accept(deMorgansVisitor);
        mockClauseControl2.setReturnValue(mockClause2);
        mockClause2.accept(queryVisitor);
        mockClauseControl2.setReturnValue(QueryFactoryResult.createFalseResult());
        mockClauseControl2.replay();

        AndClause andClause = new AndClause(mockClause, mockClause2);

        final QueryFactoryResult query = queryVisitor.visit(andClause);

        assertFalse(query.mustNotOccur());
        assertTrue(query.getLuceneQuery() instanceof BooleanQuery);
        BooleanQuery booleanQuery = (BooleanQuery)query.getLuceneQuery();
        final BooleanClause[] booleanClauses = booleanQuery.getClauses();
        assertEquals(2, booleanClauses.length);
        assertEquals(BooleanClause.Occur.MUST_NOT, booleanClauses[0].getOccur());
        assertEquals(BooleanClause.Occur.MUST, booleanClauses[1].getOccur());

        mockClauseControl.verify();
        mockClauseControl2.verify();
    }

    @Test
    public void testVisitOrClauseContainsMustNotOccur() throws Exception
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        QueryVisitor queryVisitor = new QueryVisitor(null, null, deMorgansVisitor, null, null);

        final MockControl mockClauseControl = MockControl.createStrictControl(Clause.class);
        final Clause mockClause = (Clause) mockClauseControl.getMock();
        mockClause.accept(deMorgansVisitor);
        mockClauseControl.setReturnValue(mockClause);
        mockClause.accept(queryVisitor);
        mockClauseControl.setReturnValue(new QueryFactoryResult(new BooleanQuery(), true));
        mockClauseControl.replay();

        final MockControl mockClauseControl2 = MockControl.createStrictControl(Clause.class);
        final Clause mockClause2 = (Clause) mockClauseControl2.getMock();
        mockClause2.accept(deMorgansVisitor);
        mockClauseControl2.setReturnValue(mockClause2);
        mockClause2.accept(queryVisitor);
        mockClauseControl2.setReturnValue(QueryFactoryResult.createFalseResult());
        mockClauseControl2.replay();

        OrClause orClause = new OrClause(mockClause, mockClause2);

        final QueryFactoryResult query = queryVisitor.visit(orClause);

        assertFalse(query.mustNotOccur());
        assertTrue(query.getLuceneQuery() instanceof BooleanQuery);
        BooleanQuery booleanQuery = (BooleanQuery)query.getLuceneQuery();
        final BooleanClause[] booleanClauses = booleanQuery.getClauses();
        assertEquals(2, booleanClauses.length);
        assertEquals(BooleanClause.Occur.SHOULD, booleanClauses[0].getOccur());

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new BooleanQuery(), BooleanClause.Occur.MUST_NOT);
        assertEquals(expectedQuery, booleanClauses[0].getQuery());
        assertEquals(BooleanClause.Occur.SHOULD, booleanClauses[1].getOccur());
        assertEquals(new BooleanQuery(), booleanClauses[1].getQuery());

        mockClauseControl.verify();
        mockClauseControl2.verify();
    }

    @Test
    public void testVisitOrClauseHappyPath() throws Exception
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        QueryVisitor queryVisitor = new QueryVisitor(null, null, deMorgansVisitor, null, null);

        final MockControl mockClauseControl = MockControl.createStrictControl(Clause.class);
        final Clause mockClause = (Clause) mockClauseControl.getMock();
        mockClause.accept(deMorgansVisitor);
        mockClauseControl.setReturnValue(mockClause);
        mockClause.accept(queryVisitor);
        mockClauseControl.setReturnValue(QueryFactoryResult.createFalseResult());
        mockClauseControl.replay();

        final MockControl mockClauseControl2 = MockControl.createStrictControl(Clause.class);
        final Clause mockClause2 = (Clause) mockClauseControl2.getMock();
        mockClause2.accept(deMorgansVisitor);
        mockClauseControl2.setReturnValue(mockClause2);
        mockClause2.accept(queryVisitor);
        mockClauseControl2.setReturnValue(QueryFactoryResult.createFalseResult());
        mockClauseControl2.replay();

        OrClause orClause = new OrClause(mockClause, mockClause2);

        final QueryFactoryResult query = queryVisitor.visit(orClause);

        assertFalse(query.mustNotOccur());
        assertTrue(query.getLuceneQuery() instanceof BooleanQuery);
        BooleanQuery booleanQuery = (BooleanQuery)query.getLuceneQuery();
        final BooleanClause[] booleanClauses = booleanQuery.getClauses();
        assertEquals(2, booleanClauses.length);
        assertEquals(BooleanClause.Occur.SHOULD, booleanClauses[0].getOccur());
        assertEquals(BooleanClause.Occur.SHOULD, booleanClauses[1].getOccur());

        mockClauseControl.verify();
        mockClauseControl2.verify();
    }

    @Test
    public void testVisitTerminalClauseHappyPath() throws Exception
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();

        TerminalClause terminalClause = new TerminalClauseImpl("blah", Operator.EQUALS, "blee");

        final MockControl mockClauseQueryFactoryControl = MockControl.createStrictControl(ClauseQueryFactory.class);
        final ClauseQueryFactory mockClauseQueryFactory = (ClauseQueryFactory) mockClauseQueryFactoryControl.getMock();
        mockClauseQueryFactory.getQuery(null, terminalClause);
        final BooleanQuery returnQuery = new BooleanQuery();
        mockClauseQueryFactoryControl.setReturnValue(new QueryFactoryResult(returnQuery));
        mockClauseQueryFactoryControl.replay();

        final MockControl mockQueryRegistryControl = MockControl.createStrictControl(QueryRegistry.class);
        final QueryRegistry mockQueryRegistry = (QueryRegistry) mockQueryRegistryControl.getMock();
        mockQueryRegistry.getClauseQueryFactory(null, terminalClause);
        mockQueryRegistryControl.setReturnValue(Collections.singletonList(mockClauseQueryFactory));
        mockQueryRegistryControl.replay();

        QueryVisitor queryVisitor = new QueryVisitor(mockQueryRegistry, null, deMorgansVisitor, null, null);

        final QueryFactoryResult query = queryVisitor.visit(terminalClause);

        assertFalse(query.mustNotOccur());
        assertEquals(returnQuery, query.getLuceneQuery());

        mockClauseQueryFactoryControl.verify();
        mockQueryRegistryControl.verify();
    }


    @Test
    public void testVisitTerminalClauseMultipleFactoriesHappyPath() throws Exception
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();

        TerminalClause terminalClause = new TerminalClauseImpl("blah", Operator.EQUALS, "blee");

        final MockControl mockClauseQueryFactoryControl = MockControl.createStrictControl(ClauseQueryFactory.class);
        final ClauseQueryFactory mockClauseQueryFactory = (ClauseQueryFactory) mockClauseQueryFactoryControl.getMock();
        mockClauseQueryFactory.getQuery(null, terminalClause);
        final BooleanQuery returnQuery = new BooleanQuery();
        mockClauseQueryFactoryControl.setReturnValue(new QueryFactoryResult(returnQuery));
        mockClauseQueryFactoryControl.replay();

        final MockControl mockClauseQueryFactoryControl2 = MockControl.createStrictControl(ClauseQueryFactory.class);
        final ClauseQueryFactory mockClauseQueryFactory2 = (ClauseQueryFactory) mockClauseQueryFactoryControl2.getMock();
        final BooleanQuery returnQuery2 = new BooleanQuery();
        returnQuery2.add(new TermQuery(new Term("bad", "query")), BooleanClause.Occur.MUST);
        mockClauseQueryFactory2.getQuery(null, terminalClause);
        mockClauseQueryFactoryControl2.setReturnValue(new QueryFactoryResult(returnQuery2, true));
        mockClauseQueryFactoryControl2.replay();


        final MockControl mockQueryRegistryControl = MockControl.createStrictControl(QueryRegistry.class);
        final QueryRegistry mockQueryRegistry = (QueryRegistry) mockQueryRegistryControl.getMock();
        mockQueryRegistry.getClauseQueryFactory(null, terminalClause);
        mockQueryRegistryControl.setReturnValue(CollectionBuilder.newBuilder(mockClauseQueryFactory, mockClauseQueryFactory2).asList());
        mockQueryRegistryControl.replay();

        QueryVisitor queryVisitor = new QueryVisitor(mockQueryRegistry, null, deMorgansVisitor, null, null);

        final QueryFactoryResult query = queryVisitor.visit(terminalClause);

        //The expected query.
        BooleanQuery expectedSubQuery = new BooleanQuery();
        expectedSubQuery.add(returnQuery2, BooleanClause.Occur.MUST_NOT);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(returnQuery, BooleanClause.Occur.SHOULD);
        expectedQuery.add(expectedSubQuery, BooleanClause.Occur.SHOULD);

        assertFalse(query.mustNotOccur());
        assertEquals(expectedQuery, query.getLuceneQuery());

        mockClauseQueryFactoryControl.verify();
        mockQueryRegistryControl.verify();
    }

    @Test
    public void testVisitNotClauseHappyPath() throws Exception
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        QueryVisitor queryVisitor = new QueryVisitor(null, null, deMorgansVisitor, null, null);

        NotClause badNot = new NotClause(new TerminalClauseImpl("blah", Operator.EQUALS, "blee"));
        final MockControl mockNotClauseControl = MockClassControl.createControl(NotClause.class);
        final NotClause mockNotClause = (NotClause) mockNotClauseControl.getMock();
        mockNotClause.accept(deMorgansVisitor);
        mockNotClauseControl.setReturnValue(badNot);
        mockNotClauseControl.replay();

        try
        {
            queryVisitor.visit(mockNotClause);
            fail("Should throw an exception.");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
        mockNotClauseControl.verify();
    }

    @Test
    public void testVisitTerminalClauseNoQueryFactory() throws Exception
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();

        TerminalClause terminalClause = new TerminalClauseImpl("blah", Operator.EQUALS, "blee");

        final MockControl mockQueryRegistryControl = MockControl.createStrictControl(QueryRegistry.class);
        final QueryRegistry mockQueryRegistry = (QueryRegistry) mockQueryRegistryControl.getMock();
        mockQueryRegistry.getClauseQueryFactory(null, terminalClause);
        mockQueryRegistryControl.setReturnValue(Collections.emptyList());
        mockQueryRegistryControl.replay();

        QueryVisitor queryVisitor = new QueryVisitor(mockQueryRegistry, null, deMorgansVisitor, null, null);

        final QueryFactoryResult queryFactoryResult = queryVisitor.visit(terminalClause);

        assertFalse(queryFactoryResult.mustNotOccur());
        assertEquals(new BooleanQuery(), queryFactoryResult.getLuceneQuery());

        mockQueryRegistryControl.verify();
    }

    /*
     * Make sure the Query returned from the visit will be not be negated when not necessary.
     */
    @Test
    public void testCreateQueryNotNegated()
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final QueryVisitor queryVisitor = new QueryVisitor(null, null, deMorgansVisitor, null, null);
        final BooleanQuery expectedQuery = new BooleanQuery();

        final MockControl mockNotClauseControl = MockClassControl.createControl(NotClause.class);
        final NotClause mockNotClause = (NotClause) mockNotClauseControl.getMock();
        mockNotClause.accept(deMorgansVisitor);
        mockNotClauseControl.setReturnValue(mockNotClause);
        mockNotClause.accept(queryVisitor);
        mockNotClauseControl.setReturnValue(new QueryFactoryResult(expectedQuery));
        mockNotClauseControl.replay();

        final Query actualQuery = queryVisitor.createQuery(mockNotClause);
        assertEquals(expectedQuery, actualQuery);

        mockNotClauseControl.verify();
    }

    /*
     * Make sure the Query returned from the visit will be negated if the result of the visit says
     * that it should be.
     */
    @Test
    public void testCreateQueryNegated()
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final QueryVisitor queryVisitor = new QueryVisitor(null, null, deMorgansVisitor, null, null);
        final Query notQuery = new TermQuery(new Term("qwerty", "dddd"));
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(notQuery, BooleanClause.Occur.MUST_NOT);

        final MockControl mockNotClauseControl = MockClassControl.createControl(NotClause.class);
        final NotClause mockNotClause = (NotClause) mockNotClauseControl.getMock();
        mockNotClause.accept(deMorgansVisitor);
        mockNotClauseControl.setReturnValue(mockNotClause);
        mockNotClause.accept(queryVisitor);
        mockNotClauseControl.setReturnValue(new QueryFactoryResult(notQuery, true));
        mockNotClauseControl.replay();

        final Query actualQuery = queryVisitor.createQuery(mockNotClause);
        assertEquals(expectedQuery, actualQuery);

        mockNotClauseControl.verify();
    }

    @Test
    public void testQueryCreatedOnNormalisedClause() throws Exception
    {
        final DeMorgansVisitor deMorgansVisitor = new DeMorgansVisitor();
        final QueryVisitor queryVisitor = new QueryVisitor(null, null, deMorgansVisitor, null, null);
        final BooleanQuery expectedQuery = new BooleanQuery();

        final MockControl mockNormalisedClauseControl = MockClassControl.createControl(Clause.class);
        final Clause mockNormalisedClause = (Clause) mockNormalisedClauseControl.getMock();
        mockNormalisedClause.accept(queryVisitor);
        mockNormalisedClauseControl.setReturnValue(new QueryFactoryResult(expectedQuery));
        mockNormalisedClauseControl.replay();

        final MockControl mockNotClauseControl = MockClassControl.createControl(NotClause.class);
        final NotClause mockNotClause = (NotClause) mockNotClauseControl.getMock();
        mockNotClause.accept(deMorgansVisitor);
        mockNotClauseControl.setReturnValue(mockNormalisedClause);
        mockNotClauseControl.replay();

        final Query actualQuery = queryVisitor.createQuery(mockNotClause);
        assertEquals(expectedQuery, actualQuery);

        mockNotClauseControl.verify();
        mockNormalisedClauseControl.verify();
    }
}
