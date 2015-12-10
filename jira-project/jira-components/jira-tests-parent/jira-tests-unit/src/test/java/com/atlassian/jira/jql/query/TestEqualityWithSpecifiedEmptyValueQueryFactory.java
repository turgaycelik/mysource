package com.atlassian.jira.jql.query;

import com.atlassian.query.operator.Operator;
import com.atlassian.query.operator.OperatorDoesNotSupportEmptyOperand;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v4.0
 */
public class TestEqualityWithSpecifiedEmptyValueQueryFactory
{
    @Test
    public void testCreateQueryForEmptyOperandPositiveOperator() throws Exception
    {
        final EqualityWithSpecifiedEmptyValueQueryFactory<Void> equalityQueryFactory = new EqualityWithSpecifiedEmptyValueQueryFactory<Void>(null, "BLAH");
        QueryFactoryResult emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.EQUALS);
        assertEquals("test:BLAH",emptyQuery.getLuceneQuery().toString());
        assertFalse(emptyQuery.mustNotOccur());
        emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.IS);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("test:BLAH",emptyQuery.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForEmptyOperandNegativeOperator() throws Exception
    {
        final EqualityWithSpecifiedEmptyValueQueryFactory<Void> equalityQueryFactory = new EqualityWithSpecifiedEmptyValueQueryFactory<Void>(null, "BLAH");
        QueryFactoryResult emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.NOT_EQUALS);
        assertEquals("-test:BLAH +visiblefieldids:test",emptyQuery.getLuceneQuery().toString());
        assertFalse(emptyQuery.mustNotOccur());
        emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.IS_NOT);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("-test:BLAH +visiblefieldids:test",emptyQuery.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForEmptyOperandBadOperators() throws Exception
    {
        _testCreateQueryForEmptyOperandBadOperator(Operator.GREATER_THAN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.GREATER_THAN_EQUALS);
        _testCreateQueryForEmptyOperandBadOperator(Operator.LESS_THAN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.LESS_THAN_EQUALS);
        _testCreateQueryForEmptyOperandBadOperator(Operator.IN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.NOT_IN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.LIKE);
        _testCreateQueryForEmptyOperandBadOperator(Operator.NOT_LIKE);
    }

    private void _testCreateQueryForEmptyOperandBadOperator(final Operator operator)
    {
        final EqualityWithSpecifiedEmptyValueQueryFactory<Void> queryFactory = new EqualityWithSpecifiedEmptyValueQueryFactory<Void>(null, "BLAH");
        try
        {
            queryFactory.createQueryForEmptyOperand("test", operator);
        }
        catch (OperatorDoesNotSupportEmptyOperand expected) {}
    }
}
