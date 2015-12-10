package com.atlassian.jira.jql.query;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.MockSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.resolver.SavedFilterResolver;
import com.atlassian.jira.jql.validator.SavedFilterCycleDetector;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestSavedFilterClauseQueryFactory extends MockControllerTestCase
{
    private SavedFilterResolver savedFilterResolver;
    private JqlOperandResolver jqlOperandResolver;
    private SavedFilterCycleDetector savedFilterCycleDetector;
    private User theUser = null;
    private QueryCreationContext queryCreationContext;
    private boolean overrideSecurity = false;

    @Before
    public void setUp() throws Exception
    {
        savedFilterResolver = mockController.getMock(SavedFilterResolver.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        savedFilterCycleDetector = mockController.getMock(SavedFilterCycleDetector.class);
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testGetQueryBadOperators() throws Exception
    {
        replay();
        final SavedFilterClauseQueryFactory factory = new SavedFilterClauseQueryFactory(savedFilterResolver, null, jqlOperandResolver, savedFilterCycleDetector, null, null);

        final Operator[] operators = Operator.values();
        for (Operator operator : operators)
        {
            if (!OperatorClasses.EQUALITY_OPERATORS.contains(operator))
            {
                assertEquals(QueryFactoryResult.createFalseResult(), factory.getQuery(queryCreationContext, new TerminalClauseImpl("savedFilter", operator, "blah")));
            }
        }
    }

    @Test
    public void testGetQueryNoFiltersFound() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);
        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral("filter1"), createLiteral("filter2"), createLiteral(123L)).asList();

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        mockController.setReturnValue(Collections.emptyList());

        jqlOperandResolver.getValues(queryCreationContext, operand, clause);
        mockController.setReturnValue(queryLiterals);

        mockController.replay();

        final SavedFilterClauseQueryFactory filterClauseQueryFactory = new SavedFilterClauseQueryFactory(savedFilterResolver, null, jqlOperandResolver, savedFilterCycleDetector, null, null);
        final QueryFactoryResult query = filterClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testGetQueryNoFiltersFoundOverrideSecurity() throws Exception
    {
        overrideSecurity = true;
        queryCreationContext = new QueryCreationContextImpl(theUser, overrideSecurity);

        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);
        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral("filter1"), createLiteral("filter2"), createLiteral(123L)).asList();

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        EasyMock.expect(savedFilterResolver.getSearchRequestOverrideSecurity(queryLiterals))
                .andReturn(Collections.<SearchRequest>emptyList());

        EasyMock.expect(jqlOperandResolver.getValues(queryCreationContext, operand, clause))
                .andReturn(queryLiterals);

        replay();

        final SavedFilterClauseQueryFactory filterClauseQueryFactory = new SavedFilterClauseQueryFactory(savedFilterResolver, null, jqlOperandResolver, savedFilterCycleDetector, null, null);
        final QueryFactoryResult query = filterClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testGetQueryOneFilterFound() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);
        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral("filter1"), createLiteral("filter2"), createLiteral(123L)).asList();

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        final MockSearchRequest foundFilter = new MockSearchRequest("dude");
        mockController.setReturnValue(Collections.singletonList(foundFilter));

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter, null);
        mockController.setReturnValue(false);

        jqlOperandResolver.getValues(queryCreationContext, operand, clause);
        mockController.setReturnValue(queryLiterals);

        mockController.replay();

        final TermQuery termQuery = new TermQuery(new Term("blah", "blee"));
        final SavedFilterClauseQueryFactory filterClauseQueryFactory = new SavedFilterClauseQueryFactory(savedFilterResolver, null, jqlOperandResolver, savedFilterCycleDetector, null, null)
        {
            @Override
            Query getQueryFromSavedFilter(final QueryCreationContext queryCreationContext, final SearchRequest savedFilter)
            {
                assertEquals(foundFilter, savedFilter);
                return termQuery;
            }
        };
        final QueryFactoryResult query = filterClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(termQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testGetQueryMultipleFiltersFound() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);
        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral("filter1"), createLiteral("filter2"), createLiteral(123L)).asList();

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.IN, operand);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        final MockSearchRequest foundFilter1 = new MockSearchRequest("dude1");
        final MockSearchRequest foundFilter2 = new MockSearchRequest("dude2");
        final MockSearchRequest foundFilter3 = new MockSearchRequest("dude3");
        mockController.setReturnValue(CollectionBuilder.<SearchRequest>newBuilder(foundFilter1, foundFilter2, foundFilter3).asList());

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter2, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter3, null);
        mockController.setReturnValue(false);

        jqlOperandResolver.getValues(queryCreationContext, operand, clause);
        mockController.setReturnValue(queryLiterals);

        mockController.replay();

        final AtomicInteger callCount = new AtomicInteger(0);
        final TermQuery termQuery = new TermQuery(new Term("blah", "blee"));
        final SavedFilterClauseQueryFactory filterClauseQueryFactory = new SavedFilterClauseQueryFactory(savedFilterResolver, null, jqlOperandResolver, savedFilterCycleDetector, null, null)
        {
            @Override
            Query getQueryFromSavedFilter(final QueryCreationContext queryCreationContext, final SearchRequest savedFilter)
            {
                callCount.incrementAndGet();
                return termQuery;
            }
        };
        final QueryFactoryResult query = filterClauseQueryFactory.getQuery(queryCreationContext, clause);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(termQuery, BooleanClause.Occur.SHOULD);
        expectedQuery.add(termQuery, BooleanClause.Occur.SHOULD);
        expectedQuery.add(termQuery, BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
        assertEquals(3, callCount.get());

        mockController.verify();
    }

    @Test
    public void testGetQueryOneFilterFoundWithNot() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);
        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral("filter1"), createLiteral("filter2"), createLiteral(123L)).asList();

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.NOT_IN, operand);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        final MockSearchRequest foundFilter = new MockSearchRequest("dude");
        mockController.setReturnValue(Collections.singletonList(foundFilter));

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter, null);
        mockController.setReturnValue(false);

        jqlOperandResolver.getValues(queryCreationContext, operand, clause);
        mockController.setReturnValue(queryLiterals);

        mockController.replay();

        final TermQuery termQuery = new TermQuery(new Term("blah", "blee"));
        final SavedFilterClauseQueryFactory filterClauseQueryFactory = new SavedFilterClauseQueryFactory(savedFilterResolver, null, jqlOperandResolver, savedFilterCycleDetector, null, null)
        {
            @Override
            Query getQueryFromSavedFilter(final QueryCreationContext queryCreationContext, final SearchRequest savedFilter)
            {
                assertEquals(foundFilter, savedFilter);
                return termQuery;
            }
        };
        final QueryFactoryResult query = filterClauseQueryFactory.getQuery(queryCreationContext, clause);

        assertEquals(termQuery, query.getLuceneQuery());
        assertTrue(query.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testGetQueryMultipleFiltersFoundWithNot() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);
        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral("filter1"), createLiteral("filter2"), createLiteral(123L)).asList();

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.NOT_IN, operand);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        final MockSearchRequest foundFilter1 = new MockSearchRequest("dude1");
        final MockSearchRequest foundFilter2 = new MockSearchRequest("dude2");
        final MockSearchRequest foundFilter3 = new MockSearchRequest("dude3");
        mockController.setReturnValue(CollectionBuilder.<SearchRequest>newBuilder(foundFilter1, foundFilter2, foundFilter3).asList());

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter2, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter3, null);
        mockController.setReturnValue(false);

        jqlOperandResolver.getValues(queryCreationContext, operand, clause);
        mockController.setReturnValue(queryLiterals);

        mockController.replay();

        final AtomicInteger callCount = new AtomicInteger(0);
        final TermQuery termQuery = new TermQuery(new Term("blah", "blee"));
        final SavedFilterClauseQueryFactory filterClauseQueryFactory = new SavedFilterClauseQueryFactory(savedFilterResolver, null, jqlOperandResolver, savedFilterCycleDetector, null, null)
        {
            @Override
            Query getQueryFromSavedFilter(final QueryCreationContext queryCreationContext, final SearchRequest savedFilter)
            {
                callCount.incrementAndGet();
                return termQuery;
            }
        };
        final QueryFactoryResult query = filterClauseQueryFactory.getQuery(queryCreationContext, clause);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(termQuery, BooleanClause.Occur.SHOULD);
        expectedQuery.add(termQuery, BooleanClause.Occur.SHOULD);
        expectedQuery.add(termQuery, BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertTrue(query.mustNotOccur());
        assertEquals(3, callCount.get());

        mockController.verify();

    }

    @Test
    public void testGetQueryMultipleFiltersFoundWithNotOneCausesCycle() throws Exception
    {
        final SingleValueOperand filter1Operand = new SingleValueOperand("filter1");
        final SingleValueOperand filter2Operand = new SingleValueOperand("filter2");
        final SingleValueOperand filter3Operand = new SingleValueOperand(123L);
        final List<QueryLiteral> queryLiterals = CollectionBuilder.newBuilder(createLiteral("filter1"), createLiteral("filter2"), createLiteral(123L)).asList();

        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(filter1Operand, filter2Operand, filter3Operand).asList());
        TerminalClause clause = new TerminalClauseImpl("test", Operator.NOT_IN, operand);

        savedFilterResolver.getSearchRequest(theUser, queryLiterals);
        final MockSearchRequest foundFilter1 = new MockSearchRequest("dude1");
        final MockSearchRequest foundFilter2 = new MockSearchRequest("dude2");
        final MockSearchRequest foundFilter3 = new MockSearchRequest("dude3");
        mockController.setReturnValue(CollectionBuilder.<SearchRequest>newBuilder(foundFilter1, foundFilter2, foundFilter3).asList());

        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter1, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter2, null);
        mockController.setReturnValue(false);
        savedFilterCycleDetector.containsSavedFilterReference(theUser, overrideSecurity, foundFilter3, null);
        mockController.setReturnValue(true);

        jqlOperandResolver.getValues(queryCreationContext, operand, clause);
        mockController.setReturnValue(queryLiterals);

        mockController.replay();

        final AtomicInteger callCount = new AtomicInteger(0);
        final TermQuery termQuery = new TermQuery(new Term("blah", "blee"));
        final SavedFilterClauseQueryFactory filterClauseQueryFactory = new SavedFilterClauseQueryFactory(savedFilterResolver, null, jqlOperandResolver, savedFilterCycleDetector, null, null)
        {
            @Override
            Query getQueryFromSavedFilter(final QueryCreationContext queryCreationContext, final SearchRequest savedFilter)
            {
                callCount.incrementAndGet();
                return termQuery;
            }
        };
        final QueryFactoryResult query = filterClauseQueryFactory.getQuery(queryCreationContext, clause);

        BooleanQuery expectedQuery = new BooleanQuery();

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
        assertEquals(2, callCount.get());

        mockController.verify();

    }

    @Test
    public void testGetQueryFromSavedFilterNullWhereClause() throws Exception
    {
        mockController.replay();
        SavedFilterClauseQueryFactory queryFactory = new SavedFilterClauseQueryFactory(null, null, null, null, null, null);

        assertEquals(new MatchAllDocsQuery(), queryFactory.getQueryFromSavedFilter(queryCreationContext, new SearchRequest()));
    }
}
