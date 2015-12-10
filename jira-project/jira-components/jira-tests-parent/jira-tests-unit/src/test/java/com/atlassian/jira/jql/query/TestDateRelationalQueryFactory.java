package com.atlassian.jira.jql.query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDateRelationalQueryFactory
{
    private static final Date A_DATE_LOWER = createDate(2005, 11, 25, 0);
    private static final Date A_DATE_UPPER = createDate(2005, 11, 26, 0);
    private static final String A_DATE_LOWER_STRING = LuceneUtils.dateToString(A_DATE_LOWER);
    private static final String A_DATE_UPPER_STRING = LuceneUtils.dateToString(A_DATE_UPPER);

    @Mock private JqlDateSupport dateSupport;

    @After
    public void tearDown()
    {
        dateSupport = null;
    }



    @Test
    public void testCreateQueryForSingleValueEmpty()
    {
        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);

        final QueryFactoryResult query = factory.createQueryForSingleValue("dateField", Operator.LESS_THAN, asList(new QueryLiteral()));
        assertThat(query.getLuceneQuery().toString(), equalTo(""));
        assertThat("mustNotOccur", query.mustNotOccur(), is(false));
    }

    @Test
    public void testCreateQueryForSingleValueNoDates()
    {
        final List<QueryLiteral> literals = asList(createLiteral("Value1"));

        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForSingleValue("dateField", Operator.LESS_THAN, literals);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testLessThanWithLong()
    {
        when(dateSupport.convertToDate(1000L)).thenReturn(A_DATE_LOWER);
        when(dateSupport.getIndexedValue(A_DATE_LOWER)).thenReturn(A_DATE_LOWER_STRING);
        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);

        final QueryFactoryResult query = factory.createQueryForSingleValue("dateField", Operator.LESS_THAN, asList(createLiteral(1000L)));
        assertThat(query.getLuceneQuery().toString(""), equalTo("dateField:[* TO " + A_DATE_LOWER_STRING + '}'));
    }

    @Test
    public void testDoesNotSupportOperators()
    {
        _testDoesNotSupportOperator(Operator.EQUALS);
        _testDoesNotSupportOperator(Operator.NOT_EQUALS);
        _testDoesNotSupportOperator(Operator.LIKE);
        _testDoesNotSupportOperator(Operator.IN);
        _testDoesNotSupportOperator(Operator.IS);
    }

    @Test
    public void testCreateForMultipleValues()
    {
        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);

        final QueryFactoryResult result = factory.createQueryForMultipleValues("testField", Operator.IN, asList(createLiteral(1000L)));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateForEmptyOperand()
    {
        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);

        final QueryFactoryResult result = factory.createQueryForEmptyOperand("testField", Operator.IN);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    private void _testDoesNotSupportOperator(Operator operator)
    {
        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);

        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", operator, asList(createLiteral(1000L)));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testLessThan()
    {
        when(dateSupport.convertToDate("Value1")).thenReturn(A_DATE_UPPER);
        when(dateSupport.getIndexedValue(A_DATE_UPPER)).thenReturn(A_DATE_UPPER_STRING);

        assertQueryHappy(Operator.LESS_THAN, "dateField:[* TO " + A_DATE_UPPER_STRING + '}');
    }

    @Test
    public void testLessThanEquals()
    {
        when(dateSupport.convertToDate("Value1")).thenReturn(A_DATE_UPPER);
        when(dateSupport.getIndexedValue(A_DATE_UPPER)).thenReturn(A_DATE_UPPER_STRING);

        assertQueryHappy(Operator.LESS_THAN_EQUALS, "dateField:[* TO " + A_DATE_UPPER_STRING + ']');
    }

    @Test
    public void testGreaterThan()
    {
        when(dateSupport.convertToDate("Value1")).thenReturn(A_DATE_LOWER);
        when(dateSupport.getIndexedValue(A_DATE_LOWER)).thenReturn(A_DATE_LOWER_STRING);

        assertQueryHappy(Operator.GREATER_THAN, "dateField:{" + A_DATE_LOWER_STRING + " TO *]");
    }

    @Test
    public void testGreaterThanEquals()
    {
        when(dateSupport.convertToDate("Value1")).thenReturn(A_DATE_LOWER);
        when(dateSupport.getIndexedValue(A_DATE_LOWER)).thenReturn(A_DATE_LOWER_STRING);

        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "dateField:[" + A_DATE_LOWER_STRING + " TO *]");
    }

    private void assertQueryHappy(final Operator op, final String expectedQuery)
    {
        final DateRelationalQueryFactory factory = new DateRelationalQueryFactory(dateSupport);

        final QueryFactoryResult query = factory.createQueryForSingleValue("dateField", op, asList(createLiteral("Value1")));
        assertThat(query.getLuceneQuery().toString(""), equalTo(expectedQuery));
    }

    protected static Date createDate(int year, int month, int day, int hour)
    {
        // Note: hardcoded timezone for unit tests to be consistent in all environments
        final Calendar expectedCal = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
        expectedCal.set(year, month - 1, day, hour, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        return expectedCal.getTime();
    }
}
