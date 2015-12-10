package com.atlassian.jira.jql.query;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.comparator.PriorityComparator;
import com.atlassian.jira.issue.comparator.PriorityObjectComparator;
import com.atlassian.jira.issue.priority.MockPriority;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.PriorityResolver;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestRelationalOperatorIdIndexValueQueryFactory
{
    RelationalOperatorIdIndexValueQueryFactory relationOperatorQueryFactory;

    @Before
    public void setUp() throws Exception
    {
        final MockControl mockPriorityComparatorControl = MockClassControl.createStrictControl(PriorityComparator.class);
        final PriorityComparator mockPriorityComparator = (PriorityComparator) mockPriorityComparatorControl.getMock();

        mockPriorityComparatorControl.replay();

        final MockControl mockPriorityResolverControl = MockClassControl.createControl(PriorityResolver.class);
        final PriorityResolver mockPriorityResolver = (PriorityResolver) mockPriorityResolverControl.getMock();
        mockPriorityResolverControl.replay();

        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();

        mockIssueConstantInfoResolverControl.replay();
        relationOperatorQueryFactory = new RelationalOperatorIdIndexValueQueryFactory<Priority>(mockPriorityComparator, mockPriorityResolver, mockIssueConstantInfoResolver);

        mockIssueConstantInfoResolverControl.verify();
        mockPriorityResolverControl.verify();
        mockPriorityComparatorControl.verify();
    }

    @After
    public void tearDown() throws Exception
    {
        this.relationOperatorQueryFactory = null;
    }

    @Test
    public void testCreateQueryForSingleValueNullAndEmptyValuesList() throws Exception
    {
        QueryFactoryResult query = relationOperatorQueryFactory.createQueryForSingleValue("blah", Operator.LESS_THAN, null);
        assertFalse(query.mustNotOccur());
        assertTrue(query.getLuceneQuery() instanceof BooleanQuery);
        assertEquals("", query.getLuceneQuery().toString());

        query = relationOperatorQueryFactory.createQueryForSingleValue("blah", Operator.LESS_THAN, Collections.<QueryLiteral>emptyList());
        assertFalse(query.mustNotOccur());
        assertTrue(query.getLuceneQuery() instanceof BooleanQuery);
        assertEquals("", query.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForSingleValueOperatorSingleInstance() throws Exception
    {
        final MockControl mockPriorityComparatorControl = MockClassControl.createStrictControl(PriorityComparator.class);
        final PriorityComparator mockPriorityComparator = (PriorityComparator) mockPriorityComparatorControl.getMock();
        mockPriorityComparatorControl.replay();

        final MockControl mockPriorityResolverControl = MockClassControl.createControl(PriorityResolver.class);
        final PriorityResolver mockPriorityResolver = (PriorityResolver) mockPriorityResolverControl.getMock();
        mockPriorityResolverControl.replay();

        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();
        mockIssueConstantInfoResolver.getIndexedValues("123");
        mockIssueConstantInfoResolverControl.setReturnValue(EasyList.build("123"));
        mockIssueConstantInfoResolverControl.replay();

        final AtomicBoolean generateCalled = new AtomicBoolean(false);
        final BooleanQuery query = new BooleanQuery(true);
        relationOperatorQueryFactory = new RelationalOperatorIdIndexValueQueryFactory<Priority>(mockPriorityComparator, mockPriorityResolver, mockIssueConstantInfoResolver)
        {
            @Override
            Query generateQueryForValue(final String fieldName, final Operator operator, final String id)
            {
                generateCalled.set(true);
                return query;
            }
        };

        final QueryFactoryResult generatedQuery = relationOperatorQueryFactory.createQueryForSingleValue("blah", Operator.LESS_THAN, EasyList.build(createLiteral("123")));
        assertFalse(generatedQuery.mustNotOccur());
        assertTrue(generateCalled.get());
        assertEquals(query, generatedQuery.getLuceneQuery());
    }

    @Test
    public void testCreateQueryForSingleValueOperatorSingleInstanceNullQuery() throws Exception
    {
        final MockControl mockPriorityComparatorControl = MockClassControl.createStrictControl(PriorityComparator.class);
        final PriorityComparator mockPriorityComparator = (PriorityComparator) mockPriorityComparatorControl.getMock();
        mockPriorityComparatorControl.replay();

        final MockControl mockPriorityResolverControl = MockClassControl.createControl(PriorityResolver.class);
        final PriorityResolver mockPriorityResolver = (PriorityResolver) mockPriorityResolverControl.getMock();
        mockPriorityResolverControl.replay();

        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();
        mockIssueConstantInfoResolver.getIndexedValues("123");
        mockIssueConstantInfoResolverControl.setReturnValue(EasyList.build("123"));
        mockIssueConstantInfoResolverControl.replay();

        final AtomicBoolean generateCalled = new AtomicBoolean(false);
        relationOperatorQueryFactory = new RelationalOperatorIdIndexValueQueryFactory<Priority>(mockPriorityComparator, mockPriorityResolver, mockIssueConstantInfoResolver)
        {
            @Override
            Query generateQueryForValue(final String fieldName, final Operator operator, final String id)
            {
                generateCalled.set(true);
                return null;
            }
        };

        final QueryFactoryResult generatedQuery = relationOperatorQueryFactory.createQueryForSingleValue("blah", Operator.LESS_THAN, EasyList.build(createLiteral("123")));
        assertTrue(generateCalled.get());
        assertFalse(generatedQuery.mustNotOccur());
        assertEquals(new BooleanQuery(), generatedQuery.getLuceneQuery());
    }

    @Test
    public void testCreateQueryForSingleValueOperatorMultipleValues() throws Exception
    {
        final MockControl mockPriorityComparatorControl = MockClassControl.createStrictControl(PriorityComparator.class);
        final PriorityComparator mockPriorityComparator = (PriorityComparator) mockPriorityComparatorControl.getMock();
        mockPriorityComparatorControl.replay();

        final MockControl mockPriorityResolverControl = MockClassControl.createControl(PriorityResolver.class);
        final PriorityResolver mockPriorityResolver = (PriorityResolver) mockPriorityResolverControl.getMock();
        mockPriorityResolverControl.replay();

        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();
        mockIssueConstantInfoResolver.getIndexedValues("123");
        mockIssueConstantInfoResolverControl.setReturnValue(EasyList.build("123"));
        mockIssueConstantInfoResolver.getIndexedValues("234");
        mockIssueConstantInfoResolverControl.setReturnValue(EasyList.build("234"));
        mockIssueConstantInfoResolver.getIndexedValues("345");
        mockIssueConstantInfoResolverControl.setReturnValue(EasyList.build("345"));
        mockIssueConstantInfoResolverControl.replay();

        relationOperatorQueryFactory = new RelationalOperatorIdIndexValueQueryFactory<Priority>(mockPriorityComparator, mockPriorityResolver, mockIssueConstantInfoResolver)
        {
            @Override
            Query generateQueryForValue(final String fieldName, final Operator operator, final String id)
            {
                return new TermQuery(new Term(fieldName, id));
            }
        };

        final QueryFactoryResult generatedQuery = relationOperatorQueryFactory.createQueryForSingleValue("blah", Operator.LESS_THAN, EasyList.build(createLiteral("123"), createLiteral("234"), createLiteral("345")));
        assertFalse(generatedQuery.mustNotOccur());
        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term("blah", "123")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("blah", "234")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("blah", "345")), BooleanClause.Occur.SHOULD);
        assertEquals(expectedQuery, generatedQuery.getLuceneQuery());
    }

    @Test
    public void testCreateQueryForSingleValueOperatorMultipleValuesNullQueries() throws Exception
    {
        final MockControl mockPriorityComparatorControl = MockClassControl.createStrictControl(PriorityComparator.class);
        final PriorityComparator mockPriorityComparator = (PriorityComparator) mockPriorityComparatorControl.getMock();
        mockPriorityComparatorControl.replay();

        final MockControl mockPriorityResolverControl = MockClassControl.createControl(PriorityResolver.class);
        final PriorityResolver mockPriorityResolver = (PriorityResolver) mockPriorityResolverControl.getMock();
        mockPriorityResolverControl.replay();

        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();
        mockIssueConstantInfoResolver.getIndexedValues("123");
        mockIssueConstantInfoResolverControl.setReturnValue(EasyList.build("123"));
        mockIssueConstantInfoResolver.getIndexedValues("345");
        mockIssueConstantInfoResolverControl.setReturnValue(EasyList.build("345"));
        mockIssueConstantInfoResolverControl.replay();

        relationOperatorQueryFactory = new RelationalOperatorIdIndexValueQueryFactory<Priority>(mockPriorityComparator, mockPriorityResolver, mockIssueConstantInfoResolver)
        {
            @Override
            Query generateQueryForValue(final String fieldName, final Operator operator, final String id)
            {
                return null;
            }
        };

        final QueryFactoryResult generatedQuery = relationOperatorQueryFactory.createQueryForSingleValue("blah", Operator.LESS_THAN, EasyList.build(createLiteral("123"), null, createLiteral("345")));
        assertFalse(generatedQuery.mustNotOccur());
        assertEquals(new BooleanQuery(), generatedQuery.getLuceneQuery());
    }

    @Test
    public void testCheckQueryForEmpty() throws Exception
    {
        BooleanQuery query = new BooleanQuery();
        final QueryFactoryResult newQuery = relationOperatorQueryFactory.checkQueryForEmpty(query);
        assertFalse(newQuery.mustNotOccur());
        assertEquals(new BooleanQuery(), newQuery.getLuceneQuery());
    }

    @Test
    public void testCreateQueryForSingleValueUnsupportedOperator() throws Exception
    {
        final QueryFactoryResult result = relationOperatorQueryFactory.createQueryForSingleValue("blah", Operator.EQUALS, Collections.<QueryLiteral>emptyList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testHandlersOperator() throws Exception
    {
        assertTrue(relationOperatorQueryFactory.handlesOperator(Operator.GREATER_THAN));
        assertTrue(relationOperatorQueryFactory.handlesOperator(Operator.GREATER_THAN_EQUALS));
        assertTrue(relationOperatorQueryFactory.handlesOperator(Operator.LESS_THAN));
        assertTrue(relationOperatorQueryFactory.handlesOperator(Operator.LESS_THAN_EQUALS));
        assertFalse(relationOperatorQueryFactory.handlesOperator(Operator.EQUALS));
        assertFalse(relationOperatorQueryFactory.handlesOperator(Operator.NOT_EQUALS));
        assertFalse(relationOperatorQueryFactory.handlesOperator(Operator.IN));
    }

    @Test
    public void testMultipleValues() throws Exception
    {
        final QueryFactoryResult result = relationOperatorQueryFactory.createQueryForMultipleValues("blah", Operator.IN, Collections.<QueryLiteral>emptyList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testEmptyOperand() throws Exception
    {
        final QueryFactoryResult result = relationOperatorQueryFactory.createQueryForEmptyOperand("blah", Operator.EQUALS);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testGenerateQueryForValueNullId() throws Exception
    {
        assertNull(relationOperatorQueryFactory.generateQueryForValue("blah", Operator.LESS_THAN, null));
    }

    @Test
    public void testGenerateQueryForValueHappyPath() throws Exception
    {
        final MockPriority mockPriority = new MockPriority("123", "testPri");
        mockPriority.setSequence(1L);
        final MockPriority mockPriority2 = new MockPriority("666", "evilPri");
        mockPriority2.setSequence(2L);
        final MockPriority mockPriority3 = new MockPriority("999", "otherPri");
        mockPriority3.setSequence(3L);

        final MockControl mockPriorityResolverControl = MockClassControl.createControl(PriorityResolver.class);
        final PriorityResolver mockPriorityResolver = (PriorityResolver) mockPriorityResolverControl.getMock();
        mockPriorityResolver.get(123L);
        mockPriorityResolverControl.setReturnValue(mockPriority);
        mockPriorityResolver.getAll();
        mockPriorityResolverControl.setReturnValue(EasyList.build(mockPriority, mockPriority2, mockPriority3));
        mockPriorityResolverControl.replay();


        final MockControl mockIssueConstantInfoResolverControl = MockControl.createControl(IndexInfoResolver.class);
        final IndexInfoResolver<Priority> mockIssueConstantInfoResolver = (IndexInfoResolver<Priority>) mockIssueConstantInfoResolverControl.getMock();
        mockIssueConstantInfoResolver.getIndexedValue(mockPriority);
        mockIssueConstantInfoResolverControl.setReturnValue("123");
        mockIssueConstantInfoResolver.getIndexedValue(mockPriority2);
        mockIssueConstantInfoResolverControl.setReturnValue("666");
        mockIssueConstantInfoResolver.getIndexedValue(mockPriority3);
        mockIssueConstantInfoResolverControl.setReturnValue("999");
        mockIssueConstantInfoResolverControl.replay();

        relationOperatorQueryFactory = new RelationalOperatorIdIndexValueQueryFactory<Priority>(PriorityObjectComparator.PRIORITY_OBJECT_COMPARATOR, mockPriorityResolver, mockIssueConstantInfoResolver);

        final Query query = relationOperatorQueryFactory.generateQueryForValue("blah", Operator.LESS_THAN_EQUALS, "123");
        assertTrue(query instanceof BooleanQuery);
        BooleanQuery booleanQuery = (BooleanQuery)query;
        assertEquals(3, booleanQuery.clauses().size());
        final BooleanClause[] booleanClauses = booleanQuery.getClauses();

        final List expectedTerms = EasyList.build(new TermQuery(new Term("blah", "123")), new TermQuery(new Term("blah", "666")), new TermQuery(new Term("blah", "999")));
        for (BooleanClause booleanClause : booleanClauses)
        {
            assertTrue(BooleanClause.Occur.SHOULD.equals(booleanClause.getOccur()));
            assertTrue(expectedTerms.contains(booleanClause.getQuery()));
        }

        mockIssueConstantInfoResolverControl.verify();
        mockPriorityResolverControl.verify();
    }

    @Test
    public void testGetIndexValuesNullDoesNotGetAddedForUnresolved() throws Exception
    {
        final MockControl mockIndexInfoResolverControl = MockControl.createStrictControl(IndexInfoResolver.class);
        final IndexInfoResolver<Priority> mockIndexInfoResolver = (IndexInfoResolver<Priority>) mockIndexInfoResolverControl.getMock();
        mockIndexInfoResolver.getIndexedValues("testVal");
        mockIndexInfoResolverControl.setReturnValue(null);
        mockIndexInfoResolverControl.replay();

        final MockControl mockPriorityComparatorControl = MockClassControl.createStrictControl(PriorityComparator.class);
        final PriorityComparator mockPriorityComparator = (PriorityComparator) mockPriorityComparatorControl.getMock();
        mockPriorityComparatorControl.replay();

        final MockControl mockPriorityResolverControl = MockClassControl.createControl(PriorityResolver.class);
        final PriorityResolver mockPriorityResolver = (PriorityResolver) mockPriorityResolverControl.getMock();
        mockPriorityResolverControl.replay();

        relationOperatorQueryFactory = new RelationalOperatorIdIndexValueQueryFactory<Priority>(mockPriorityComparator, mockPriorityResolver, mockIndexInfoResolver);

        final List<String> indexVals = relationOperatorQueryFactory.getIndexValues(EasyList.build(createLiteral("testVal")));

        assertEquals(0, indexVals.size());
        mockIndexInfoResolverControl.verify();
    }

    @Test
    public void testGetIndexValuesNullDoNotGetAddedForUnresolved() throws Exception
    {
        final MockControl mockIndexInfoResolverControl = MockControl.createStrictControl(IndexInfoResolver.class);
        final IndexInfoResolver<Priority> mockIndexInfoResolver = (IndexInfoResolver<Priority>) mockIndexInfoResolverControl.getMock();
        mockIndexInfoResolver.getIndexedValues("testVal");
        mockIndexInfoResolverControl.setReturnValue(Collections.<Priority>emptyList());
        mockIndexInfoResolverControl.replay();

        final MockControl mockPriorityComparatorControl = MockClassControl.createStrictControl(PriorityComparator.class);
        final PriorityComparator mockPriorityComparator = (PriorityComparator) mockPriorityComparatorControl.getMock();
        mockPriorityComparatorControl.replay();

        final MockControl mockPriorityResolverControl = MockClassControl.createControl(PriorityResolver.class);
        final PriorityResolver mockPriorityResolver = (PriorityResolver) mockPriorityResolverControl.getMock();
        mockPriorityResolverControl.replay();

        relationOperatorQueryFactory = new RelationalOperatorIdIndexValueQueryFactory<Priority>(mockPriorityComparator, mockPriorityResolver, mockIndexInfoResolver);

        final List<String> indexVals = relationOperatorQueryFactory.getIndexValues(EasyList.build(createLiteral("testVal")));

        assertEquals(0, indexVals.size());
        mockIndexInfoResolverControl.verify();
    }

    @Test
    public void testGetIndexValuesHappyPathStringValue() throws Exception
    {
        final MockControl mockIndexInfoResolverControl = MockControl.createStrictControl(IndexInfoResolver.class);
        final IndexInfoResolver<Priority> mockIndexInfoResolver = (IndexInfoResolver<Priority>) mockIndexInfoResolverControl.getMock();
        mockIndexInfoResolver.getIndexedValues("testVal");
        mockIndexInfoResolverControl.setReturnValue(EasyList.build("123", "666", "999"));
        mockIndexInfoResolverControl.replay();

        final MockControl mockPriorityComparatorControl = MockClassControl.createStrictControl(PriorityComparator.class);
        final PriorityComparator mockPriorityComparator = (PriorityComparator) mockPriorityComparatorControl.getMock();
        mockPriorityComparatorControl.replay();

        final MockControl mockPriorityResolverControl = MockClassControl.createControl(PriorityResolver.class);
        final PriorityResolver mockPriorityResolver = (PriorityResolver) mockPriorityResolverControl.getMock();
        mockPriorityResolverControl.replay();

        relationOperatorQueryFactory = new RelationalOperatorIdIndexValueQueryFactory<Priority>(mockPriorityComparator, mockPriorityResolver, mockIndexInfoResolver);

        final List<String> indexVals = relationOperatorQueryFactory.getIndexValues(EasyList.build(createLiteral("testVal")));
        assertEquals(3, indexVals.size());
        assertEquals(EasyList.build("123", "666", "999"), indexVals);
        mockIndexInfoResolverControl.verify();
    }

    @Test
    public void testGetIndexValuesHappyPathLongValue() throws Exception
    {
        final MockControl mockIndexInfoResolverControl = MockControl.createStrictControl(IndexInfoResolver.class);
        final IndexInfoResolver<Priority> mockIndexInfoResolver = (IndexInfoResolver<Priority>) mockIndexInfoResolverControl.getMock();
        mockIndexInfoResolver.getIndexedValues(12345L);
        mockIndexInfoResolverControl.setReturnValue(EasyList.build("123", "666"));
        mockIndexInfoResolver.getIndexedValues(56789L);
        mockIndexInfoResolverControl.setReturnValue(EasyList.build("999"));
        mockIndexInfoResolverControl.replay();

        final MockControl mockPriorityComparatorControl = MockClassControl.createStrictControl(PriorityComparator.class);
        final PriorityComparator mockPriorityComparator = (PriorityComparator) mockPriorityComparatorControl.getMock();
        mockPriorityComparatorControl.replay();

        final MockControl mockPriorityResolverControl = MockClassControl.createControl(PriorityResolver.class);
        final PriorityResolver mockPriorityResolver = (PriorityResolver) mockPriorityResolverControl.getMock();
        mockPriorityResolverControl.replay();

        relationOperatorQueryFactory = new RelationalOperatorIdIndexValueQueryFactory<Priority>(mockPriorityComparator, mockPriorityResolver, mockIndexInfoResolver);

        final List<String> indexVals = relationOperatorQueryFactory.getIndexValues(EasyList.build(createLiteral(12345l), createLiteral(56789L)));
        assertEquals(3, indexVals.size());
        assertEquals(EasyList.build("123", "666", "999"), indexVals);
        mockIndexInfoResolverControl.verify();
    }

}
