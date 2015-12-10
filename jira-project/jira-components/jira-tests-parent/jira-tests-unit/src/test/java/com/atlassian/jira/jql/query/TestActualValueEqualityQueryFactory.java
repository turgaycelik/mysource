package com.atlassian.jira.jql.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

import static com.atlassian.jira.issue.index.indexers.FieldIndexer.NO_VALUE_INDEX_VALUE;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestActualValueEqualityQueryFactory
{
    private static final String FIELD_NAME = "integerField";

    @Test
    public void testEquals() throws Exception
    {
        assertQueryHappy(Operator.EQUALS, "integerField:1800", "30");
        assertQueryHappy(Operator.EQUALS, "integerField:-1800", "-30");
        assertQueryHappy(Operator.EQUALS, "integerField:1800", 30L);
        assertQueryHappy(Operator.EQUALS, "integerField:-1800", -30L);
    }

    @Test
    public void testNotEquals() throws Exception
    {
        assertQueryHappy(Operator.NOT_EQUALS, "+(-integerField:-1 +visiblefieldids:integerField) -integerField:1800 +visiblefieldids:integerField", "30");
        assertQueryHappy(Operator.NOT_EQUALS, "+(-integerField:-1 +visiblefieldids:integerField) -integerField:-1800 +visiblefieldids:integerField", "-30");
        assertQueryHappy(Operator.NOT_EQUALS, "+(-integerField:-1 +visiblefieldids:integerField) -integerField:1800 +visiblefieldids:integerField", 30L);
        assertQueryHappy(Operator.NOT_EQUALS, "+(-integerField:-1 +visiblefieldids:integerField) -integerField:-1800 +visiblefieldids:integerField", -30L);
    }

    @Test
    public void testInMultiple() throws Exception
    {
        assertQueryHappyMultiple(Operator.IN, "integerField:1800 integerField:-1500 integerField:1200 integerField:-900", false, "30", "-25", 20L, -15L);
    }

    @Test
    public void testInSingle() throws Exception
    {
        assertQueryHappyMultiple(Operator.IN, "integerField:-900", false, -15L);
    }

    @Test
    public void testNotInMultiple() throws Exception
    {
        assertQueryHappyMultiple(Operator.NOT_IN, "+(-integerField:-1 +visiblefieldids:integerField) -integerField:1800 -integerField:-1500 -integerField:1200 -integerField:-900 +visiblefieldids:integerField", false, "30", "-25", 20L, -15L);
    }

    @Test
    public void testNotInSingle() throws Exception
    {
        assertQueryHappyMultiple(Operator.NOT_IN, "+(-integerField:-1 +visiblefieldids:integerField) -integerField:1800 +visiblefieldids:integerField", false, "30");
    }

    @Test
    public void testSingleValueWithBadOperators() throws Exception
    {
        _testSingleValueWithBadOperator(Operator.GREATER_THAN);
        _testSingleValueWithBadOperator(Operator.GREATER_THAN_EQUALS);
        _testSingleValueWithBadOperator(Operator.LESS_THAN);
        _testSingleValueWithBadOperator(Operator.LESS_THAN_EQUALS);
        _testSingleValueWithBadOperator(Operator.IN);
        _testSingleValueWithBadOperator(Operator.IS);
    }

    private void _testSingleValueWithBadOperator(Operator operator) throws Exception
    {
        final ActualValueEqualityQueryFactory factory = createFactory(NO_VALUE_INDEX_VALUE);
        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", operator, Collections.singletonList(createLiteral("Value1")));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testMultiValueWithBadOperators() throws Exception
    {
        _testMultiValueWithBadOperator(Operator.GREATER_THAN);
        _testMultiValueWithBadOperator(Operator.GREATER_THAN_EQUALS);
        _testMultiValueWithBadOperator(Operator.LESS_THAN);
        _testMultiValueWithBadOperator(Operator.LESS_THAN_EQUALS);
        _testMultiValueWithBadOperator(Operator.EQUALS);
        _testMultiValueWithBadOperator(Operator.NOT_EQUALS);
        _testMultiValueWithBadOperator(Operator.IS);
        _testMultiValueWithBadOperator(Operator.IS_NOT);
        _testMultiValueWithBadOperator(Operator.LIKE);
        _testMultiValueWithBadOperator(Operator.NOT_LIKE);
    }

    private void _testMultiValueWithBadOperator(Operator operator) throws Exception
    {
        final ActualValueEqualityQueryFactory factory = createFactory(NO_VALUE_INDEX_VALUE);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("testField", operator, Collections.singletonList(createLiteral("Value1")));
        QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testHandlesOperator() throws Exception
    {
        final ActualValueEqualityQueryFactory factory = createFactory(NO_VALUE_INDEX_VALUE);
        assertFalse(factory.handlesOperator(Operator.GREATER_THAN));
        assertFalse(factory.handlesOperator(Operator.GREATER_THAN_EQUALS));
        assertFalse(factory.handlesOperator(Operator.LESS_THAN));
        assertFalse(factory.handlesOperator(Operator.LESS_THAN_EQUALS));
        assertFalse(factory.handlesOperator(Operator.LIKE));
        assertFalse(factory.handlesOperator(Operator.NOT_LIKE));

        assertTrue(factory.handlesOperator(Operator.EQUALS));
        assertTrue(factory.handlesOperator(Operator.NOT_EQUALS));
        assertTrue(factory.handlesOperator(Operator.IS));
        assertTrue(factory.handlesOperator(Operator.IS_NOT));
        assertTrue(factory.handlesOperator(Operator.IN));
        assertTrue(factory.handlesOperator(Operator.NOT_IN));
    }

    @Test
    public void testCreateQueryForEmptyOperandSpecialValue() throws Exception
    {
        final ActualValueEqualityQueryFactory factory = createFactory(NO_VALUE_INDEX_VALUE);
        QueryFactoryResult emptyQuery = factory.createQueryForEmptyOperand("test", Operator.EQUALS);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("test:-1",emptyQuery.getLuceneQuery().toString());
        emptyQuery = factory.createQueryForEmptyOperand("test", Operator.IS);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("test:-1",emptyQuery.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForEmptyOperandNotEmptyOperatorsSpecialValue() throws Exception
    {
        final ActualValueEqualityQueryFactory factory = createFactory(NO_VALUE_INDEX_VALUE);

        QueryFactoryResult emptyQuery = factory.createQueryForEmptyOperand("test", Operator.NOT_EQUALS);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("-test:-1 +visiblefieldids:test",emptyQuery.getLuceneQuery().toString());

        emptyQuery = factory.createQueryForEmptyOperand("test", Operator.IS_NOT);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("-test:-1 +visiblefieldids:test",emptyQuery.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForEmptyOperand() throws Exception
    {
        final ActualValueEqualityQueryFactory factory = createFactory(null);
        final BooleanQuery expectedQuery = getEmptyQueryForField("test");

        final QueryFactoryResult expectedResult = new QueryFactoryResult(expectedQuery);

        QueryFactoryResult emptyQuery = factory.createQueryForEmptyOperand("test", Operator.EQUALS);
        assertEquals(expectedResult,emptyQuery);

        emptyQuery = factory.createQueryForEmptyOperand("test", Operator.IS);
        assertEquals(expectedResult,emptyQuery);
    }

    @Test
    public void testCreateQueryForEmptyOperandNotEmptyOperators() throws Exception
    {
        final ActualValueEqualityQueryFactory factory = createFactory(null);
        final QueryFactoryResult expectedResult = new QueryFactoryResult(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "test")), false);

        QueryFactoryResult emptyQuery = factory.createQueryForEmptyOperand("test", Operator.NOT_EQUALS);
        assertEquals(expectedResult,emptyQuery);

        emptyQuery = factory.createQueryForEmptyOperand("test", Operator.IS_NOT);
        assertEquals(expectedResult,emptyQuery);
    }

    @Test
    public void testCreateQueryForEmptyOperandBadOperator() throws Exception
    {
        final ActualValueEqualityQueryFactory factory = createFactory(NO_VALUE_INDEX_VALUE);
        final QueryFactoryResult result = factory.createQueryForEmptyOperand("test", Operator.LIKE);

        final QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testEqualsNoResolvedLiterals() throws Exception
    {
        QueryLiteral literal = createLiteral(5L);

        final IndexValueConverter indexValueConverter = mock(IndexValueConverter.class);
        final ActualValueEqualityQueryFactory factory = new ActualValueEqualityQueryFactory(indexValueConverter);
        final QueryFactoryResult result = factory.createQueryForSingleValue("dateField", Operator.EQUALS, Collections.singletonList(literal));

        final Query expectedQuery = new BooleanQuery();
        assertEquals(expectedQuery, result.getLuceneQuery());
        assertThat(result.mustNotOccur(), is(false));

        verify(indexValueConverter).convertToIndexValue(literal);
        verifyNoMoreInteractions(indexValueConverter);
    }

    @Test
    public void testEqualsEmpty() throws Exception
    {
        final QueryLiteral literal = new QueryLiteral();

        final IndexValueConverter indexValueConverter = mock(IndexValueConverter.class);

        final ActualValueEqualityQueryFactory factory = new ActualValueEqualityQueryFactory(indexValueConverter);
        final QueryFactoryResult result = factory.createQueryForSingleValue("dateField", Operator.EQUALS, Collections.singletonList(literal));
        final Query expectedQuery = getEmptyQueryForField("dateField");

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertThat(result.mustNotOccur(), is(false));
        verifyZeroInteractions(indexValueConverter);
    }

    @Test
    public void testNotEqualsEmpty() throws Exception
    {
        QueryLiteral literal = new QueryLiteral();

        final IndexValueConverter indexValueConverter = mock(IndexValueConverter.class);

        final ActualValueEqualityQueryFactory factory = new ActualValueEqualityQueryFactory(indexValueConverter);
        final QueryFactoryResult result = factory.createQueryForSingleValue("dateField", Operator.NOT_EQUALS, Collections.singletonList(literal));

        final TermQuery expectedQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "dateField"));

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertThat(result.mustNotOccur(), is(false));

        verifyZeroInteractions(indexValueConverter);
    }

    @Test
    public void testInWithEmpty() throws Exception
    {
        QueryLiteral emptyLiteral = new QueryLiteral();
        QueryLiteral longLiteral = createLiteral(10L);

        final IndexValueConverter indexValueConverter = mock(IndexValueConverter.class);
        when(indexValueConverter.convertToIndexValue(longLiteral)).thenReturn("10");

        final ActualValueEqualityQueryFactory factory = new ActualValueEqualityQueryFactory(indexValueConverter);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.IN, CollectionBuilder.newBuilder(emptyLiteral, longLiteral).asList());

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(getEmptyQueryForField("dateField"), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("dateField", "10")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertThat(result.mustNotOccur(), is(false));
    }

    @Test
    public void testInWithEmptyAndEmptyIndexValue() throws Exception
    {
        QueryLiteral emptyLiteral = new QueryLiteral();
        QueryLiteral longLiteral = createLiteral(10L);

        final IndexValueConverter indexValueConverter = mock(IndexValueConverter.class);
        when(indexValueConverter.convertToIndexValue(longLiteral)).thenReturn("10");

        final ActualValueEqualityQueryFactory factory = new ActualValueEqualityQueryFactory(indexValueConverter, "-1");
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.IN, CollectionBuilder.newBuilder(emptyLiteral, longLiteral).asList());

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term("dateField", "-1")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("dateField", "10")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertThat(result.mustNotOccur(), is(false));
    }

    @Test
    public void testInWithSingleEmpty() throws Exception
    {
        QueryLiteral emptyLiteral = new QueryLiteral();

        final IndexValueConverter indexValueConverter = mock(IndexValueConverter.class);

        final ActualValueEqualityQueryFactory factory = new ActualValueEqualityQueryFactory(indexValueConverter);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.IN, CollectionBuilder.newBuilder(emptyLiteral).asList());

        Query expectedQuery = getEmptyQueryForField("dateField");

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertThat(result.mustNotOccur(), is(false));

        verifyZeroInteractions(indexValueConverter);
    }

    @Test
    public void testNotInNoResolvedLiterals() throws Exception
    {
        QueryLiteral longLiteral = createLiteral(10L);

        final IndexValueConverter indexValueConverter = mock(IndexValueConverter.class);

        final ActualValueEqualityQueryFactory factory = new ActualValueEqualityQueryFactory(indexValueConverter);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN, CollectionBuilder.newBuilder(longLiteral).asList());

        final Query expectedQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "dateField"));

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertThat(result.mustNotOccur(), is(false));

        verify(indexValueConverter).convertToIndexValue(longLiteral);
        verifyNoMoreInteractions(indexValueConverter);
    }

    @Test
    public void testNotInWithEmpty() throws Exception
    {
        QueryLiteral emptyLiteral = new QueryLiteral();
        QueryLiteral longLiteral = createLiteral(10L);

        final IndexValueConverter indexValueConverter = mock(IndexValueConverter.class);
        when(indexValueConverter.convertToIndexValue(longLiteral)).thenReturn("10");

        final ActualValueEqualityQueryFactory factory = new ActualValueEqualityQueryFactory(indexValueConverter);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN, CollectionBuilder.newBuilder(emptyLiteral, longLiteral).asList());

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "dateField")), BooleanClause.Occur.MUST);
        expectedQuery.add(new TermQuery(new Term("dateField", "10")), BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, "dateField")), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertThat(result.mustNotOccur(), is(false));
    }

    @Test
    public void testNotInWithEmptyAndEmptyIndexValue() throws Exception
    {
        QueryLiteral emptyLiteral = new QueryLiteral();
        QueryLiteral longLiteral = createLiteral(10L);

        final IndexValueConverter indexValueConverter = mock(IndexValueConverter.class);
        when(indexValueConverter.convertToIndexValue(longLiteral)).thenReturn("10");

        final ActualValueEqualityQueryFactory factory = new ActualValueEqualityQueryFactory(indexValueConverter, "-1");
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN, CollectionBuilder.newBuilder(emptyLiteral, longLiteral).asList());

        final BooleanQuery expectedNotEmptyQuery = new BooleanQuery();
        expectedNotEmptyQuery.add(new TermQuery(new Term("dateField", "-1")), BooleanClause.Occur.MUST_NOT);
        expectedNotEmptyQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, "dateField")), BooleanClause.Occur.MUST);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(expectedNotEmptyQuery, BooleanClause.Occur.MUST);
        expectedQuery.add(new TermQuery(new Term("dateField", "10")), BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, "dateField")), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertThat(result.mustNotOccur(), is(false));
    }

    @Test
    public void testNotInWithSingleEmpty() throws Exception
    {
        QueryLiteral emptyLiteral = new QueryLiteral();

        final IndexValueConverter indexValueConverter = mock(IndexValueConverter.class);

        final ActualValueEqualityQueryFactory factory = new ActualValueEqualityQueryFactory(indexValueConverter);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN, CollectionBuilder.newBuilder(emptyLiteral).asList());

        Query expectedQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "dateField"));

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertThat(result.mustNotOccur(), is(false));
    }

    private static void assertQueryHappy(Operator op, String expectedLuceneQuery, final Object value)
    {
        final ActualValueEqualityQueryFactory factory = createFactory(NO_VALUE_INDEX_VALUE);

        QueryFactoryResult query = factory.createQueryForSingleValue(FIELD_NAME, op, Collections.singletonList(literal(value)));
        assertEquals(expectedLuceneQuery, query.getLuceneQuery().toString());
        assertThat(query.mustNotOccur(), is(false));
    }

    private static void assertQueryHappyMultiple(Operator op, String luceneQuery, final boolean expectedMustNotOccur, Object... values)
    {
        final ActualValueEqualityQueryFactory factory = createFactory(NO_VALUE_INDEX_VALUE);

        final List<QueryLiteral> list = new ArrayList<QueryLiteral>(values.length);
        for (Object value : values)
        {
            list.add(literal(value));
        }
        QueryFactoryResult query = factory.createQueryForMultipleValues(FIELD_NAME, op, list);

        assertNotNull(query.getLuceneQuery());
        assertEquals(luceneQuery, query.getLuceneQuery().toString(""));
        assertEquals(expectedMustNotOccur, query.mustNotOccur());
    }

    private static QueryLiteral literal(Object value)
    {
        if (value instanceof String)
        {
            return createLiteral((String) value);
        }
        if (value instanceof Long)
        {
            return createLiteral((Long) value);
        }
        throw new IllegalArgumentException();
    }

    // use a simple DurationSupport that doesn't attempt to pad and rebase the Long values.
    private static ActualValueEqualityQueryFactory createFactory(final String emptyValue)
    {
        final IndexValueConverter valueConverter = new IndexValueConverter()
        {
            @Override
            public String convertToIndexValue(QueryLiteral rawValue)
            {
                Long value = rawValue.getLongValue();
                if (value == null)
                {
                    if (rawValue.getStringValue() != null)
                    {
                        value = new Long(rawValue.getStringValue());
                    }
                }

                if (value != null)
                {
                    return String.valueOf(value * 60);
                }
                else
                {
                    return null;
                }
            }
        };

        if (emptyValue != null)
        {
            return new ActualValueEqualityQueryFactory(valueConverter, emptyValue);
        }
        else
        {
            return new ActualValueEqualityQueryFactory(valueConverter);
        }
    }

    private static BooleanQuery getEmptyQueryForField(String field)
    {
        final TermQuery emptyTermQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, field));
        final TermQuery visibilityTermQuery = new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, field));
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(emptyTermQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(visibilityTermQuery, BooleanClause.Occur.MUST);
        return expectedQuery;
    }
}
