package com.atlassian.jira.jql.query;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v4.0
 */
public class TestEqualityQueryFactory
{
    @Test
    public void testCreateQueryForEmptyOperand() throws Exception
    {
        final EqualityQueryFactory equalityQueryFactory = new EqualityQueryFactory(null);
        QueryFactoryResult emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.EQUALS);
        assertEquals("-nonemptyfieldids:test +visiblefieldids:test",emptyQuery.getLuceneQuery().toString());
        assertFalse(emptyQuery.mustNotOccur());
        emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.IS);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("-nonemptyfieldids:test +visiblefieldids:test",emptyQuery.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForNotEmptyOperand() throws Exception
    {
        final EqualityQueryFactory equalityQueryFactory = new EqualityQueryFactory(null);
        QueryFactoryResult emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.NOT_EQUALS);
        assertEquals("nonemptyfieldids:test",emptyQuery.getLuceneQuery().toString());
        assertFalse(emptyQuery.mustNotOccur());
        emptyQuery = equalityQueryFactory.createQueryForEmptyOperand("test", Operator.IS_NOT);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("nonemptyfieldids:test",emptyQuery.getLuceneQuery().toString());
    }

    @Test
    public void testGetIsNotEmptyQuery() throws Exception
    {
        final EqualityQueryFactory equalityQueryFactory = new EqualityQueryFactory(null);
        final Query notEmptyQuery = equalityQueryFactory.getIsNotEmptyQuery("hello");
        assertEquals(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "hello")), notEmptyQuery);
    }

    @Test
    public void testCreateQueryForEmptyOperandBadOperator() throws Exception
    {
        _testCreateQueryForEmptyOperandBadOperator(Operator.LIKE);
        _testCreateQueryForEmptyOperandBadOperator(Operator.NOT_LIKE);
        _testCreateQueryForEmptyOperandBadOperator(Operator.IN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.NOT_IN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.GREATER_THAN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.GREATER_THAN_EQUALS);
        _testCreateQueryForEmptyOperandBadOperator(Operator.LESS_THAN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.LESS_THAN_EQUALS);
    }

    public void _testCreateQueryForEmptyOperandBadOperator(Operator operator) throws Exception
    {
        final EqualityQueryFactory equalityQueryFactory = new EqualityQueryFactory(null);
        final QueryFactoryResult result = equalityQueryFactory.createQueryForEmptyOperand("test", operator);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }
}
