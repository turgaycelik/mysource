package com.atlassian.jira.jql.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.easymock.MockControl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.MockClassControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestAbstractEqualityQueryFactory
{
    AbstractEqualityQueryFactory equalityQueryFactory;
    IssueConstantInfoResolver<Priority> resolver;

    @Before
    public void setUp() throws Exception
    {
        resolver = EasyMock.createMock(IssueConstantInfoResolver.class);

        EasyMock.replay(resolver);

        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(resolver)
        {
            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                return null;
            }

            Query getIsEmptyQuery(final String fieldName)
            {
                return null;
            }

            Query getIsNotEmptyQuery(final String fieldName)
            {
                return null;
            }
        };

        EasyMock.verify(resolver);
    }

    @After
    public void tearDown() throws Exception
    {
        this.equalityQueryFactory = null;
    }

    @Test
    public void testCreateQueryForSingleValueReturnsFalseResultForOperator() throws Exception
    {
        createQuerySingleValueReturnsFalseResultForOperator(Operator.GREATER_THAN_EQUALS);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.GREATER_THAN);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.LESS_THAN);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.LESS_THAN_EQUALS);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.IN);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.NOT_IN);
    }

    @Test
    public void testCreateQueryForMultipleValuesReturnsFalseResultForOperator() throws Exception
    {
        createQueryMultipleValuesReturnsFalseResultForOperator(Operator.LESS_THAN);
        createQueryMultipleValuesReturnsFalseResultForOperator(Operator.LESS_THAN_EQUALS);
        createQueryMultipleValuesReturnsFalseResultForOperator(Operator.GREATER_THAN);
        createQueryMultipleValuesReturnsFalseResultForOperator(Operator.GREATER_THAN_EQUALS);
        createQueryMultipleValuesReturnsFalseResultForOperator(Operator.EQUALS);
        createQueryMultipleValuesReturnsFalseResultForOperator(Operator.NOT_EQUALS);
        createQueryMultipleValuesReturnsFalseResultForOperator(Operator.IS);
        createQueryMultipleValuesReturnsFalseResultForOperator(Operator.IS_NOT);
        createQueryMultipleValuesReturnsFalseResultForOperator(Operator.LIKE);
        createQueryMultipleValuesReturnsFalseResultForOperator(Operator.NOT_LIKE);
    }

    @Test
    public void testHandlesOperator() throws Exception
    {
        assertFalse(equalityQueryFactory.handlesOperator(Operator.GREATER_THAN));
        assertFalse(equalityQueryFactory.handlesOperator(Operator.GREATER_THAN_EQUALS));
        assertFalse(equalityQueryFactory.handlesOperator(Operator.LESS_THAN));
        assertFalse(equalityQueryFactory.handlesOperator(Operator.LESS_THAN_EQUALS));
        assertFalse(equalityQueryFactory.handlesOperator(Operator.LIKE));
        assertFalse(equalityQueryFactory.handlesOperator(Operator.NOT_LIKE));

        assertTrue(equalityQueryFactory.handlesOperator(Operator.EQUALS));
        assertTrue(equalityQueryFactory.handlesOperator(Operator.NOT_EQUALS));
        assertTrue(equalityQueryFactory.handlesOperator(Operator.IN));
        assertTrue(equalityQueryFactory.handlesOperator(Operator.NOT_IN));
        assertTrue(equalityQueryFactory.handlesOperator(Operator.IS));
        assertTrue(equalityQueryFactory.handlesOperator(Operator.IS_NOT));
    }

    @Test
    public void testCreateQueryForSingleValueEquals() throws Exception
    {
        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();
        mockIssueConstantInfoResolverControl.replay();

        final AtomicBoolean handleCalled = new AtomicBoolean(false);
        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(mockIssueConstantInfoResolver)
        {
            Query getIsNotEmptyQuery(final String fieldName)
            {
                return null;
            }

            @Override
            QueryFactoryResult handleEquals(final String fieldName, final List<String> indexValues)
            {
                handleCalled.set(true);
                return null;
            }

            Query getIsEmptyQuery(final String fieldName)
            {
                return null;
            }

            @Override
            List<String> getIndexValues(final List<QueryLiteral> rawValues)
            {
                return null;
            }

            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                return null;
            }
        };

        equalityQueryFactory.createQueryForSingleValue("testField", Operator.EQUALS, Collections.singletonList(createLiteral(12L)));

        assertTrue(handleCalled.get());
    }

    @Test
    public void testCreateQueryForSingleValueNotEquals() throws Exception
    {
        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();
        mockIssueConstantInfoResolverControl.replay();

        final AtomicBoolean handleCalled = new AtomicBoolean(false);
        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(mockIssueConstantInfoResolver)
        {
            @Override
            QueryFactoryResult handleNotEquals(final String fieldName, final List<String> indexValues)
            {
                handleCalled.set(true);
                return null;
            }

            Query getIsEmptyQuery(final String fieldName)
            {
                return null;
            }

            Query getIsNotEmptyQuery(final String fieldName)
            {
                return null;
            }

            @Override
            List<String> getIndexValues(final List<QueryLiteral> rawValues)
            {
                return null;
            }

            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                return null;
            }
        };

        equalityQueryFactory.createQueryForSingleValue("testField", Operator.NOT_EQUALS, Collections.singletonList(createLiteral(12L)));

        assertTrue(handleCalled.get());
    }

    @Test
    public void testHandleEqualsNullOrEmptyList() throws Exception
    {
        final QueryFactoryResult result1 = equalityQueryFactory.handleEquals("blah", null);
        assertFalse(result1.mustNotOccur());
        assertEquals(new BooleanQuery(), result1.getLuceneQuery());
        final QueryFactoryResult result2 = equalityQueryFactory.handleEquals("blah", Collections.<String>emptyList());
        assertFalse(result2.mustNotOccur());
        assertEquals(new BooleanQuery(), result2.getLuceneQuery());
    }

    @Test
    public void testHandleEqualsOnlyOne() throws Exception
    {
        final QueryFactoryResult generatedQuery = equalityQueryFactory.handleEquals("blah", Collections.singletonList("12"));
        assertFalse(generatedQuery.mustNotOccur());
        assertEquals(new TermQuery(new Term("blah", "12")), generatedQuery.getLuceneQuery());
    }

    @Test
    public void testHandleEqualsOnlyOneThatIsNull() throws Exception
    {
        List<String> list = new ArrayList<String>();
        list.add(null);

        final Query result = new BooleanQuery();
        final AtomicBoolean createEmptyCalled = new AtomicBoolean(false);

        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(resolver)
        {
            Query getIsEmptyQuery(final String fieldName)
            {
                createEmptyCalled.set(true);
                return result;
            }

            Query getIsNotEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }

            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                throw new UnsupportedOperationException();
            }
        };

        // when given a list of strings that contains a single null, the result will be equivalent to the result
        // produced for an Empty Operand.
        final QueryFactoryResult expectedResult = new QueryFactoryResult(result);
        final QueryFactoryResult generatedQuery = equalityQueryFactory.handleEquals("blah", list);

        assertEquals(expectedResult, generatedQuery);
        assertTrue(createEmptyCalled.get());
    }

    @Test
    public void testHandleEqualsMultipleIndexValuesAllNull() throws Exception
    {
        List<String> list = new ArrayList<String>();
        list.add(null);
        list.add(null);

        final TermQuery expectedTerm = new TermQuery(new Term("blah", "empty"));
        final AtomicBoolean createEmptyCalled = new AtomicBoolean(false);

        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(resolver)
        {
            Query getIsEmptyQuery(final String fieldName)
            {
                createEmptyCalled.set(true);
                return expectedTerm;
            }

            @Override
            Query getIsNotEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }

            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                throw new UnsupportedOperationException();
            }
        };

        // when given a list of strings that contains multiple nulls, the result will be equivalent to the result
        // produced for a single Empty Operand joined together multiple times
        final QueryFactoryResult generatedQuery = equalityQueryFactory.handleEquals("blah", list);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(expectedTerm, BooleanClause.Occur.SHOULD);
        expectedQuery.add(expectedTerm, BooleanClause.Occur.SHOULD);

        assertTrue(createEmptyCalled.get());
        
        assertEquals(expectedQuery, generatedQuery.getLuceneQuery());
        assertFalse(generatedQuery.mustNotOccur());
    }

    @Test
    public void testHandleEqualsMultipleIndexValuesSomeNull() throws Exception
    {
        List<String> list = new ArrayList<String>();
        list.add(null);
        list.add("12");
        list.add(null);

        final TermQuery expectedEmptyTerm = new TermQuery(new Term("blah", "empty"));
        final AtomicBoolean createEmptyCalled = new AtomicBoolean(false);

        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(resolver)
        {
            Query getIsEmptyQuery(final String fieldName)
            {
                createEmptyCalled.set(true);
                return expectedEmptyTerm;
            }

            @Override
            Query getIsNotEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }

            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                throw new UnsupportedOperationException();
            }
        };

        // when given a list of strings that contains multiple nulls, the result will be equivalent to the result
        // produced for a single Empty Operand joined together multiple times
        final QueryFactoryResult generatedQuery = equalityQueryFactory.handleEquals("blah", list);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(expectedEmptyTerm, BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("blah", "12")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(expectedEmptyTerm, BooleanClause.Occur.SHOULD);

        assertTrue(createEmptyCalled.get());
        
        assertEquals(expectedQuery, generatedQuery.getLuceneQuery());
        assertFalse(generatedQuery.mustNotOccur());
    }

    @Test
    public void testHandleEqualsMultipleIndexValues() throws Exception
    {
        final QueryFactoryResult generatedQuery = equalityQueryFactory.handleEquals("blah", CollectionBuilder.newBuilder("12", "13").asList());
        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term("blah", "12")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("blah", "13")), BooleanClause.Occur.SHOULD);
        assertFalse(generatedQuery.mustNotOccur());
        assertEquals(expectedQuery, generatedQuery.getLuceneQuery());
    }

    @Test
    public void testHandleNotEqualsNullOrEmptyList() throws Exception
    {
        final Query notEmptyQuery = new TermQuery(new Term("blah", "-1"));

        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(null)
        {
            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsNotEmptyQuery(final String fieldName)
            {
                return notEmptyQuery;
            }
        };

        final QueryFactoryResult result1 = equalityQueryFactory.handleNotEquals("blah", null);
        assertFalse(result1.mustNotOccur());
        assertEquals(notEmptyQuery, result1.getLuceneQuery());
        final QueryFactoryResult result2 = equalityQueryFactory.handleNotEquals("blah", Collections.<String>emptyList());
        assertFalse(result2.mustNotOccur());
        assertEquals(notEmptyQuery, result2.getLuceneQuery());
    }

    @Test
    public void testHandleNotEqualsOnlyOne() throws Exception
    {
        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();

        mockIssueConstantInfoResolverControl.replay();
        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(mockIssueConstantInfoResolver)
        {

            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsNotEmptyQuery(final String fieldName)
            {
                final BooleanQuery notEmpty = new BooleanQuery();
                notEmpty.add(getTermQuery("blah", "-1"), BooleanClause.Occur.MUST_NOT);
                notEmpty.add(TermQueryFactory.visibilityQuery("blah"), BooleanClause.Occur.MUST);
                return notEmpty;
            }
        };

        final QueryFactoryResult generatedQuery = equalityQueryFactory.handleNotEquals("blah", Collections.singletonList("12"));
        assertFalse(generatedQuery.mustNotOccur());
        assertEquals("+(-blah:-1 +visiblefieldids:blah) -blah:12 +visiblefieldids:blah", generatedQuery.getLuceneQuery().toString());
    }

    @Test
    public void testHandleNotEqualsOnlyOneEmptyMustOccur() throws Exception
    {
        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();

        mockIssueConstantInfoResolverControl.replay();
        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(mockIssueConstantInfoResolver)
        {
            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsNotEmptyQuery(final String fieldName)
            {
                return getTermQuery(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "blah");
            }
        };

        final ArrayList<String> strings = new ArrayList<String>();
        strings.add(null);
        final QueryFactoryResult generatedQuery = equalityQueryFactory.handleNotEquals("blah", strings);
        final TermQuery expectedQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "blah"));
        assertFalse(generatedQuery.mustNotOccur());
        assertEquals(expectedQuery, generatedQuery.getLuceneQuery());
    }

    @Test
    public void testHandleNotEqualsMultipleIndexValuesMultipleValues() throws Exception
    {
        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();

        mockIssueConstantInfoResolverControl.replay();
        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(mockIssueConstantInfoResolver)
        {
            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsNotEmptyQuery(final String fieldName)
            {
                return getTermQuery(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "blah");
            }
        };

        List<String> list = new ArrayList<String>();
        list.add("dude");
        list.add("sweet");
        final QueryFactoryResult generatedQuery = equalityQueryFactory.handleNotEquals("blah", list);
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "blah")), BooleanClause.Occur.MUST);
        expectedQuery.add(new TermQuery(new Term("blah", "dude")), BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(new TermQuery(new Term("blah", "sweet")), BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, "blah")), BooleanClause.Occur.MUST);
        assertFalse(generatedQuery.mustNotOccur());
        assertEquals(expectedQuery, generatedQuery.getLuceneQuery());
    }

    @Test
    public void testHandleNotEqualsMultipleIndexValues() throws Exception
    {
        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();

        mockIssueConstantInfoResolverControl.replay();
        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(mockIssueConstantInfoResolver)
        {
            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsNotEmptyQuery(final String fieldName)
            {
                final BooleanQuery notEmpty = new BooleanQuery();
                notEmpty.add(getTermQuery("blah", "-1"), BooleanClause.Occur.MUST_NOT);
                notEmpty.add(TermQueryFactory.visibilityQuery("blah"), BooleanClause.Occur.MUST);
                return notEmpty;
            }
        };

        final QueryFactoryResult generatedQuery = equalityQueryFactory.handleNotEquals("blah", CollectionBuilder.newBuilder("12", "13").asList());
        assertFalse(generatedQuery.mustNotOccur());
        assertEquals("+(-blah:-1 +visiblefieldids:blah) -blah:12 -blah:13 +visiblefieldids:blah", generatedQuery.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForMultipleValuesInEmptyList() throws Exception
    {
        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();

        mockIssueConstantInfoResolverControl.replay();
        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(mockIssueConstantInfoResolver)
        {
            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsNotEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }
        };

        final QueryFactoryResult generatedQuery = equalityQueryFactory.createQueryForMultipleValues("test", Operator.IN, Collections.<QueryLiteral>emptyList());
        assertFalse(generatedQuery.mustNotOccur());
        assertEquals(new BooleanQuery(), generatedQuery.getLuceneQuery());
    }

    @Test
    public void testCreateQueryForMultipleValuesNotInSingletonList() throws Exception
    {
        final MockControl mockIssueConstantInfoResolverControl = MockClassControl.createControl(IssueConstantInfoResolver.class);
        final IssueConstantInfoResolver mockIssueConstantInfoResolver = (IssueConstantInfoResolver) mockIssueConstantInfoResolverControl.getMock();
        mockIssueConstantInfoResolver.getIndexedValues("blah");
        mockIssueConstantInfoResolverControl.setReturnValue(Collections.singletonList("blah"));

        mockIssueConstantInfoResolverControl.replay();

        final BooleanQuery notEmpty = new BooleanQuery();
        notEmpty.add(new TermQuery(new Term("test", "-1")), BooleanClause.Occur.MUST_NOT);
        notEmpty.add(TermQueryFactory.visibilityQuery("test"), BooleanClause.Occur.MUST);

        equalityQueryFactory = new AbstractEqualityQueryFactory<Priority>(mockIssueConstantInfoResolver)
        {
            public QueryFactoryResult createQueryForEmptyOperand(final String fieldName, final Operator operator)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsEmptyQuery(final String fieldName)
            {
                throw new UnsupportedOperationException();
            }

            Query getIsNotEmptyQuery(final String fieldName)
            {
                return notEmpty;
            }
        };

        final QueryFactoryResult generatedQuery = equalityQueryFactory.createQueryForMultipleValues("test", Operator.NOT_IN, Collections.singletonList(createLiteral("blah")));
        assertFalse(generatedQuery.mustNotOccur());
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(notEmpty, BooleanClause.Occur.MUST);
        expectedQuery.add(new TermQuery(new Term("test", "blah")), BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, "test")), BooleanClause.Occur.MUST);
        assertEquals(expectedQuery, generatedQuery.getLuceneQuery());
    }

    private void createQueryMultipleValuesReturnsFalseResultForOperator(Operator operator)
    {
        final QueryFactoryResult result = equalityQueryFactory.createQueryForMultipleValues("test", operator, Collections.<QueryLiteral>emptyList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    private void createQuerySingleValueReturnsFalseResultForOperator(Operator operator)
    {
        final QueryFactoryResult result = equalityQueryFactory.createQueryForSingleValue("test", operator, Collections.<QueryLiteral>emptyList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

}
