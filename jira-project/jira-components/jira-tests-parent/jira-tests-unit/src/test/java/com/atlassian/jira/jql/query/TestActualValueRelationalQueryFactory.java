package com.atlassian.jira.jql.query;

import java.util.Collections;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.query.operator.Operator;

import org.junit.Test;
import org.mockito.Mockito;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @since v4.0
 */
public class TestActualValueRelationalQueryFactory
{
    private static final String FIELD_NAME = "durationField";

    @Test
    public void testDoesNotSupportOperators() throws Exception
    {
        _testDoesNotSupportOperator(Operator.EQUALS);
        _testDoesNotSupportOperator(Operator.NOT_EQUALS);
        _testDoesNotSupportOperator(Operator.LIKE);
        _testDoesNotSupportOperator(Operator.IN);
        _testDoesNotSupportOperator(Operator.IS);
    }

    private void _testDoesNotSupportOperator(Operator operator) throws Exception
    {
        final ActualValueRelationalQueryFactory factory = createFactory(FIELD_NAME);

        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", operator, Collections.singletonList(createLiteral(1000L)));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateForMultipleValues() throws Exception
    {
        final ActualValueRelationalQueryFactory factory = createFactory(FIELD_NAME);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("testField", Operator.IN, Collections.singletonList(createLiteral(1000L)));
        QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testCreateForEmptyOperand() throws Exception
    {
        final ActualValueRelationalQueryFactory factory = createFactory(FIELD_NAME);
        final QueryFactoryResult result = factory.createQueryForEmptyOperand("testField", Operator.IN);
        QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testCreateForSingleValueEmptyLiteral() throws Exception
    {
        final ActualValueRelationalQueryFactory factory = createFactory(FIELD_NAME);
        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", Operator.LESS_THAN, Collections.singletonList(new QueryLiteral()));
        QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testCreateForSingleValueNoIndexValues() throws Exception
    {
        final ActualValueRelationalQueryFactory factory = new ActualValueRelationalQueryFactory(Mockito.mock(IndexValueConverter.class), FIELD_NAME);
        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", Operator.LESS_THAN, Collections.singletonList(createLiteral("XX")));
        QueryFactoryResult expectedResult = QueryFactoryResult.createFalseResult();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testLessThanWithEmptyIndexValue() throws Exception
    {
        assertQueryHappy("-1", Operator.LESS_THAN, "+durationField:[* TO 1800} -durationField:-1", 30L);
        assertQueryHappy("-1", Operator.LESS_THAN, "+durationField:[* TO 1800} -durationField:-1", "30");
        assertQueryHappy("-1", Operator.LESS_THAN, "+durationField:[* TO -1800} -durationField:-1", -30L);
        assertQueryHappy("-1", Operator.LESS_THAN, "+durationField:[* TO -1800} -durationField:-1", "-30");
    }

    @Test
    public void testLessThan() throws Exception
    {
        assertQueryHappy(Operator.LESS_THAN, "durationField:[* TO 1800}", 30L);
        assertQueryHappy(Operator.LESS_THAN, "durationField:[* TO 1800}", "30");
        assertQueryHappy(Operator.LESS_THAN, "durationField:[* TO -1800}", -30L);
        assertQueryHappy(Operator.LESS_THAN, "durationField:[* TO -1800}", "-30");
    }

    @Test
    public void testLessThanEquals() throws Exception
    {
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "durationField:[* TO 1800]", 30L);
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "durationField:[* TO 1800]", "30");
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "durationField:[* TO -1800]", -30L);
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "durationField:[* TO -1800]", "-30");
    }

    @Test
    public void testGreaterThan() throws Exception
    {
        assertQueryHappy(Operator.GREATER_THAN, "durationField:{1800 TO *]", 30L);
        assertQueryHappy(Operator.GREATER_THAN, "durationField:{1800 TO *]", "30");
        assertQueryHappy(Operator.GREATER_THAN, "durationField:{-1800 TO *]", -30L);
        assertQueryHappy(Operator.GREATER_THAN, "durationField:{-1800 TO *]", "-30");
    }

    @Test
    public void testGreaterThanEquals() throws Exception
    {
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "durationField:[1800 TO *]", 30L);
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "durationField:[1800 TO *]", "30");
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "durationField:[-1800 TO *]", -30L);
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "durationField:[-1800 TO *]", "-30");
    }

    private void assertQueryHappy(Operator op, String luceneQuery, final Object value)
    {
        assertQueryHappy(null, op, luceneQuery, value);
    }

    private void assertQueryHappy(final String emptyIndexValue, Operator op, String luceneQuery, final Object value)
    {
        final ActualValueRelationalQueryFactory factory = createFactory(emptyIndexValue);

        QueryFactoryResult query = factory.createQueryForSingleValue(FIELD_NAME, op, Collections.singletonList(createQL(value)));
        assertFalse(query.mustNotOccur());
        assertNotNull(query.getLuceneQuery());
        assertEquals(luceneQuery, query.getLuceneQuery().toString());
    }

    private QueryLiteral createQL(Object value)
    {
        if (value instanceof String)
        {
            return createLiteral((String) value);
        }
        else if (value instanceof Long)
        {
            return createLiteral((Long) value);
        }
        throw new IllegalArgumentException();
    }

    private static ActualValueRelationalQueryFactory createFactory(final String emptyIndexValue)
    {
        return new ActualValueRelationalQueryFactory(new MockIndexValueConverter(), emptyIndexValue);
    }

    private static class MockIndexValueConverter implements IndexValueConverter
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
    }
}
