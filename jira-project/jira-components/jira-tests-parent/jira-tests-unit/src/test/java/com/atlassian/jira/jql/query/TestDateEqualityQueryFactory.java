package com.atlassian.jira.jql.query;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
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
public class TestDateEqualityQueryFactory
{
    private static final Date A_DATE_LOWER = createDate(2005, 11, 25, 0);
    private static final String A_DATE_LOWER_STRING = LuceneUtils.dateToString(A_DATE_LOWER);
    private static final Date B_DATE_LOWER = createDate(2008, 12, 2, 5);
    private static final String B_DATE_LOWER_STRING = "imlow";

    @Mock private JqlDateSupport dateSupport;

    @After
    public void tearDown()
    {
        dateSupport = null;
    }

    @Test
    public void testEquals()
    {
        when(dateSupport.convertToDate("Value1")).thenReturn(A_DATE_LOWER);
        when(dateSupport.getIndexedValue(A_DATE_LOWER)).thenReturn(A_DATE_LOWER_STRING);

        assertQueryHappy(dateSupport, Operator.EQUALS, "dateField:" + A_DATE_LOWER_STRING);
    }

    @Test
    public void testNotEquals()
    {
        when(dateSupport.convertToDate("Value1")).thenReturn(A_DATE_LOWER);
        when(dateSupport.getIndexedValue(A_DATE_LOWER)).thenReturn(A_DATE_LOWER_STRING);

        assertQueryHappy(dateSupport, Operator.NOT_EQUALS, "dateField:[* TO " + A_DATE_LOWER_STRING + "} dateField:{" + A_DATE_LOWER_STRING + " TO *]");
    }

    @Test
    public void testInMultiple()
    {
        when(dateSupport.convertToDate("Value1")).thenReturn(A_DATE_LOWER);
        when(dateSupport.convertToDate("Value2")).thenReturn(B_DATE_LOWER);
        when(dateSupport.getIndexedValue(A_DATE_LOWER)).thenReturn(A_DATE_LOWER_STRING);
        when(dateSupport.getIndexedValue(B_DATE_LOWER)).thenReturn(B_DATE_LOWER_STRING);

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult query = factory.createQueryForMultipleValues("dateField", Operator.IN, ImmutableList.of(
                createLiteral("Value1"),
                createLiteral("Value2")));
        assertResult(query, "dateField:" + A_DATE_LOWER_STRING + " dateField:imlow");
    }

    @Test
    public void testInSingle()
    {
        when(dateSupport.convertToDate("Value1")).thenReturn(A_DATE_LOWER);
        when(dateSupport.getIndexedValue(A_DATE_LOWER)).thenReturn(A_DATE_LOWER_STRING);

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult query = factory.createQueryForMultipleValues("dateField", Operator.IN,
                ImmutableList.of(createLiteral("Value1")));
        assertResult(query, "dateField:" + A_DATE_LOWER_STRING);
    }

    @Test
    public void testNotInMultiple()
    {
        when(dateSupport.convertToDate("Value1")).thenReturn(A_DATE_LOWER);
        when(dateSupport.convertToDate("Value2")).thenReturn(B_DATE_LOWER);
        when(dateSupport.getIndexedValue(A_DATE_LOWER)).thenReturn(A_DATE_LOWER_STRING);
        when(dateSupport.getIndexedValue(B_DATE_LOWER)).thenReturn(B_DATE_LOWER_STRING);

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult query = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN, ImmutableList.of(
                createLiteral("Value1"),
                createLiteral("Value2")));
        assertResult(query, "+(dateField:[* TO " + A_DATE_LOWER_STRING + "} dateField:{" + A_DATE_LOWER_STRING +
                " TO *]) +(dateField:[* TO imlow} dateField:{imlow TO *])");
    }

    @Test
    public void testNotInSingle()
    {
        when(dateSupport.convertToDate("Value1")).thenReturn(A_DATE_LOWER);
        when(dateSupport.getIndexedValue(A_DATE_LOWER)).thenReturn(A_DATE_LOWER_STRING);

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult query = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN,
                ImmutableList.of(createLiteral("Value1")));
        assertResult(query, "dateField:[* TO " + A_DATE_LOWER_STRING + "} dateField:{" + A_DATE_LOWER_STRING + " TO *]");
    }

    @Test
    public void testSingleValueWithBadOperators()
    {
        _testSingleValueWithBadOperator(Operator.GREATER_THAN);
        _testSingleValueWithBadOperator(Operator.GREATER_THAN_EQUALS);
        _testSingleValueWithBadOperator(Operator.LESS_THAN);
        _testSingleValueWithBadOperator(Operator.LESS_THAN_EQUALS);
        _testSingleValueWithBadOperator(Operator.IN);
        _testSingleValueWithBadOperator(Operator.IS);
    }

    private void _testSingleValueWithBadOperator(final Operator operator)
    {
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", operator,
                asList(createLiteral("Value1")));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testMultiValueWithBadOperators()
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

    @Test
    public void testCreateQueryForSingleValueNoDates()
    {
        final List<QueryLiteral> literals = ImmutableList.of(createLiteral("Value1"));

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);

        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", Operator.EQUALS, literals);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    private void _testMultiValueWithBadOperator(final Operator operator)
    {
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);

        final QueryFactoryResult result = factory.createQueryForMultipleValues("testField", operator, asList(createLiteral("Value1")));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testHandlesOperator()
    {
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final EnumSet<Operator> supported = EnumSet.of(Operator.EQUALS, Operator.NOT_EQUALS, Operator.IS,
                Operator.IS_NOT, Operator.IN, Operator.NOT_IN);
        for (Operator operator : Operator.values())
        {
            assertHandlesOperator(factory, operator, supported.contains(operator));
        }
    }

    @Test
    public void testCreateQueryForEmptyOperand()
    {
        final DateEqualityQueryFactory equalityQueryFactory = new DateEqualityQueryFactory(dateSupport);

        QueryFactoryResult emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.EQUALS);
        assertResult(emptyQuery, "-nonemptyfieldids:test +visiblefieldids:test");

        emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.IS);
        assertResult(emptyQuery, "-nonemptyfieldids:test +visiblefieldids:test");
    }

    @Test
    public void testCreateQueryForEmptyOperandNotEmptyOperators()
    {
        final DateEqualityQueryFactory equalityQueryFactory = new DateEqualityQueryFactory(dateSupport);
        QueryFactoryResult emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.NOT_EQUALS);
        assertResult(emptyQuery, "nonemptyfieldids:test");

        emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.IS_NOT);
        assertResult(emptyQuery, "nonemptyfieldids:test");
    }

    @Test
    public void testCreateQueryForEmptyOperandBadOperator()
    {
        final DateEqualityQueryFactory equalityQueryFactory = new DateEqualityQueryFactory(dateSupport);

        final QueryFactoryResult result = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.LIKE);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testEqualsEmpty()
    {
        final QueryLiteral literal = new QueryLiteral();

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForSingleValue("dateField", Operator.EQUALS, ImmutableList.of(literal));

        assertResult(result, getEmptyQueryForField("dateField"));
    }

    @Test
    public void testNotEqualsEmpty()
    {
        final QueryLiteral literal = new QueryLiteral();

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForSingleValue("dateField", Operator.NOT_EQUALS, ImmutableList.of(literal));

        final TermQuery expectedQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "dateField"));
        assertResult(result, expectedQuery);
    }

    @Test
    public void testInWithEmpty()
    {
        final QueryLiteral emptyLiteral = new QueryLiteral();
        final QueryLiteral longLiteral = createLiteral(10L);

        final Date date = new Date();
        when(dateSupport.convertToDate(10L)).thenReturn(date);
        when(dateSupport.getIndexedValue(date)).thenReturn("10");

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.IN, ImmutableList.of(
                emptyLiteral,
                longLiteral ));

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(getEmptyQueryForField("dateField"), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term("dateField", "10")), BooleanClause.Occur.SHOULD);
        assertResult(result, expectedQuery);
    }

    @Test
    public void testInWithSingleEmpty()
    {
        final QueryLiteral emptyLiteral = new QueryLiteral();
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.IN,
                ImmutableList.of(emptyLiteral));
        final Query expectedQuery = getEmptyQueryForField("dateField");

        assertResult(result, expectedQuery);
    }

    @Test
    public void testNotInWithEmpty()
    {
        final QueryLiteral emptyLiteral = new QueryLiteral();
        final QueryLiteral longLiteral = createLiteral(10L);

        final Date date = new Date();
        when(dateSupport.convertToDate(10L)).thenReturn(date);
        when(dateSupport.getIndexedValue(date)).thenReturn("10");

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN,
                ImmutableList.of(emptyLiteral, longLiteral));

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "dateField")), BooleanClause.Occur.SHOULD);

        final BooleanQuery combined = new BooleanQuery();
        combined.add(new TermRangeQuery("dateField", null, "10", true, false), BooleanClause.Occur.SHOULD);
        combined.add(new TermRangeQuery("dateField", "10", null, false, true), BooleanClause.Occur.SHOULD);

        expectedQuery.add(combined, BooleanClause.Occur.MUST);
        assertResult(result, expectedQuery);
    }

    @Test
    public void testNotInWithSingleEmpty()
    {
        final QueryLiteral emptyLiteral = new QueryLiteral();

        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(dateSupport);
        final QueryFactoryResult result = factory.createQueryForMultipleValues("dateField", Operator.NOT_IN, ImmutableList.of(emptyLiteral));

        final Query expectedQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "dateField"));
        assertResult(result, expectedQuery);
    }

    protected static void assertQueryHappy(final JqlDateSupport support, final Operator op, final String expectedQuery)
    {
        final DateEqualityQueryFactory factory = new DateEqualityQueryFactory(support);
        final QueryFactoryResult result = factory.createQueryForSingleValue("dateField", op,
                ImmutableList.of(createLiteral("Value1")));
        assertResult(result, expectedQuery);
    }

    protected static Date createDate(final int year, final int month, final int day, final int hour)
    {
        // Note: hardcoded timezone for unit tests to be consistent in all environments
        final Calendar expectedCal = Calendar.getInstance(TimeZone.getTimeZone("Australia/Sydney"));
        expectedCal.set(year, month - 1, day, hour, 0, 0);
        expectedCal.set(Calendar.MILLISECOND, 0);
        return expectedCal.getTime();
    }

    private static BooleanQuery getEmptyQueryForField(final String field)
    {
        final TermQuery emptyTermQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, field));
        final TermQuery visibilityTermQuery = new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, field));
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(emptyTermQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(visibilityTermQuery, BooleanClause.Occur.MUST);
        return expectedQuery;
    }

    private static void assertHandlesOperator(DateEqualityQueryFactory factory, Operator operator, boolean expected)
    {
        assertThat(operator.name(), factory.handlesOperator(operator), is(expected));
    }

    private static void assertResult(QueryFactoryResult result, Query expectedQuery)
    {
        assertThat("query", result.getLuceneQuery(), equalTo(expectedQuery));
        assertThat("mustNotOccur", result.mustNotOccur(), is(false));
    }

    private static void assertResult(QueryFactoryResult result, String expectedQuery)
    {
        assertThat("query", result.getLuceneQuery().toString(), equalTo(expectedQuery));
        assertThat("mustNotOccur", result.mustNotOccur(), is(false));
    }
}
