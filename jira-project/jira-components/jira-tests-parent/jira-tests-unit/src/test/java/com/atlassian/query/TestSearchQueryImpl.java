package com.atlassian.query;

import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestSearchQueryImpl
{
    @Test
    public void testToString() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));

        final String userString = "testField  =  test";
        final QueryImpl query = new QueryImpl(clause, userString);

        assertEquals("{testField = \"test\"}", query.toString());
        assertEquals(userString, query.getQueryString());

        final QueryImpl queryWithSort = new QueryImpl(clause, new OrderByImpl(new SearchSort("what", SortOrder.DESC)), userString);

        assertEquals("{testField = \"test\"} order by what DESC", queryWithSort.toString());
        assertEquals(userString, queryWithSort.getQueryString());

        final QueryImpl emptyQuery = new QueryImpl();

        assertEquals("", emptyQuery.toString());
        assertNull(userString, emptyQuery.getQueryString());
    }

    @Test
    public void testWhereClause() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("anotherValue"));
        AndClause andClause = new AndClause(clause1, clause2);
        final TerminalClauseImpl clause3 = new TerminalClauseImpl("differentField", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        OrClause orClause = new OrClause(clause3, andClause);

        final QueryImpl query = new QueryImpl(orClause, null);
        assertEquals(orClause, query.getWhereClause());

        final QueryImpl nullWhere = new QueryImpl(null);
        assertNull(nullWhere.getWhereClause());
    }

    @Test
    public void testSortOrder() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));

        final QueryImpl queryNoSort = new QueryImpl(clause1, null);
        assertEquals(clause1, queryNoSort.getWhereClause());
        assertEquals(new OrderByImpl(), queryNoSort.getOrderByClause());

        final OrderByImpl expectedOrderBy = new OrderByImpl(new SearchSort("testField", SortOrder.ASC));
        final QueryImpl queryWithSort = new QueryImpl(clause1, expectedOrderBy, null);
        assertEquals(expectedOrderBy, queryWithSort.getOrderByClause());

        final QueryImpl nullOrderBy = new QueryImpl(clause1, null, null);
        assertNull(nullOrderBy.getOrderByClause());
    }

    @Test
    public void testGetSearchStringReturnsToStringWhenNull() throws Exception
    {
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("test"));
        final TerminalClauseImpl clause2 = new TerminalClauseImpl("testField", Operator.EQUALS, new SingleValueOperand("anotherValue"));
        AndClause andClause = new AndClause(clause1, clause2);
        final TerminalClauseImpl clause3 = new TerminalClauseImpl("differentField", Operator.EQUALS, new SingleValueOperand("thirdValue"));
        OrClause orClause = new OrClause(clause3, andClause);

        final QueryImpl query = new QueryImpl(orClause, null);

        assertNull(query.getQueryString());
        assertEquals("{differentField = \"thirdValue\"} OR {testField = \"test\"} AND {testField = \"anotherValue\"}", query.toString());
    }
}
