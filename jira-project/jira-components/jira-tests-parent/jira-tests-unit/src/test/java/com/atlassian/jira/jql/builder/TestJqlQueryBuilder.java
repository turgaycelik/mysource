package com.atlassian.jira.jql.builder;

import java.util.Date;
import java.util.TimeZone;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.clause.WasClauseImpl;
import com.atlassian.query.history.TerminalHistoryPredicate;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * @since v4.0
 */
public class TestJqlQueryBuilder extends MockControllerTestCase
{
    @Before
    public void setUp() throws Exception
    {
        TimeZoneManager timeZoneManager = EasyMock.createMock(TimeZoneManager.class);
        EasyMock.expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        EasyMock.replay(timeZoneManager);
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        componentWorker.registerMock(JqlClauseBuilderFactory.class, new JqlClauseBuilderFactoryImpl(new JqlDateSupportImpl(timeZoneManager)));
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    @Test
    public void testJqlClearBuilder() throws Exception
    {
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().unresolved();
        builder.orderBy().project(null);

        assertNotNull(builder.where().buildClause());
        assertFalse(OrderByImpl.NO_ORDER.equals(builder.orderBy().buildOrderBy()));

        builder.clear();

        assertNull(builder.where().buildClause());
        assertEquals(OrderByImpl.NO_ORDER, builder.orderBy().buildOrderBy());
    }

    @Test
    public void testSubBuildersNotNull() throws Exception
    {
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        assertNotNull(builder.where());
        assertNotNull(builder.orderBy());
        assertNotNull(builder.buildQuery());
        assertEquals(new QueryImpl(null, new OrderByImpl(), null), builder.buildQuery());
    }
    
    @Test
    public void testCloneFromExisting() throws Exception
    {
        final OrderByImpl existingOrderBy = new OrderByImpl(new SearchSort("Test", SortOrder.DESC), new SearchSort("Blah", SortOrder.ASC), new SearchSort("Heee", (SortOrder) null), new SearchSort("ASC", "Haaaa"));
        final Clause existingClause = JqlQueryBuilder.newBuilder().where().priority("High").and().dueAfter(new Date()).and().sub().affectedVersion("12").or().not().unresolved().endsub().buildClause();
        final QueryImpl existingSearchQuery = new QueryImpl(existingClause, existingOrderBy, null);
        assertEquals(existingSearchQuery, JqlQueryBuilder.newBuilder(existingSearchQuery).buildQuery());
    }

    @Test
    public void testCloneFromExistingNull() throws Exception
    {
        final QueryImpl existingSearchQuery = new QueryImpl(null, new OrderByImpl(), null);

        assertEquals(existingSearchQuery, JqlQueryBuilder.newBuilder(null).buildQuery());
        assertEquals(existingSearchQuery, JqlQueryBuilder.newBuilder(null).where().and().buildQuery());
        assertEquals(existingSearchQuery, JqlQueryBuilder.newBuilder(null).where().or().buildQuery());
    }

    @Test
    public void testCloneFromExistingEmpty() throws Exception
    {
        final QueryImpl existingSearchQuery = new QueryImpl(null, new OrderByImpl(), null);

        assertEquals(existingSearchQuery, JqlQueryBuilder.newBuilder(existingSearchQuery).buildQuery());
        assertEquals(existingSearchQuery, JqlQueryBuilder.newBuilder(existingSearchQuery).where().and().buildQuery());
        assertEquals(existingSearchQuery, JqlQueryBuilder.newBuilder(existingSearchQuery).where().or().buildQuery());

        //It should be possible to call and()/or() on an empty query.
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(existingSearchQuery);
        builder.where().and().or().environment().like().string("test");

        final QueryImpl expectedSearchQuery = new QueryImpl(new TerminalClauseImpl("environment", Operator.LIKE, "test"), new OrderByImpl(), null);
        assertEquals(expectedSearchQuery, builder.buildQuery());
    }

    @Test
    public void testNewQueryBuilder() throws Exception
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();
        assertNotNull(builder);

        assertSame(builder, builder.and());
        assertSame(builder, builder.or());

        //Make sure the builder is empty initially.
        assertNull(builder.buildClause());

        //Make sure there is no parent.
        assertNull(builder.endWhere());
    }

    @Test
    public void testNewQueryBuilderClause() throws Exception
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder((Clause)null);
        assertNotNull(builder);

        assertSame(builder, builder.and());
        assertSame(builder, builder.or());

        //Make sure the builder is empty initially.
        assertNull(builder.buildClause());

        //Make sure there is no parent.
        assertNull(builder.endWhere());

        Clause clause = new TerminalClauseImpl("test", "cool");
        builder = JqlQueryBuilder.newClauseBuilder(clause);

        assertEquals(clause, builder.buildClause());
        assertEquals(new AndClause(clause, clause), builder.and().addClause(clause).buildClause());
        assertNull(builder.endWhere());
    }

    @Test
    public void testNewQueryBuilderQuery() throws Exception
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder((Query)null);
        assertNotNull(builder);

        //Make sure the builder is empty initially.
        assertNull(builder.buildClause());

        //Make sure there is no parent.
        assertNull(builder.endWhere());

        Query query = new QueryImpl(null);

        builder = JqlQueryBuilder.newClauseBuilder(query);
        assertNotNull(builder);
        assertNull(builder.buildClause());
        assertNull(builder.endWhere());

        Clause clause = new TerminalClauseImpl("test", "cool");
        query = new QueryImpl(clause);

        builder = JqlQueryBuilder.newClauseBuilder(query);
        assertNotNull(builder);
        assertNull(builder.endWhere());

        assertEquals(clause, builder.buildClause());
        assertEquals(new AndClause(clause, clause), builder.and().addClause(clause).buildClause());
        assertNull(builder.endWhere());
    }

    @Test
    public void testNewOrderByBuilder() throws Exception
    {
        JqlOrderByBuilder builder = JqlQueryBuilder.newOrderByBuilder();
        assertNotNull(builder);

        //Make sure the builder is empty initially.
        assertEquals(OrderByImpl.NO_ORDER, builder.buildOrderBy());

        //Make sure there is no parent.
        assertNull(builder.endOrderBy());
    }

    @Test
    public void testNewOrderByBuilderClause() throws Exception
    {
        JqlOrderByBuilder builder = JqlQueryBuilder.newOrderByBuilder((OrderBy)null);
        assertNotNull(builder);

        //Make sure the builder is empty initially.
        assertEquals(OrderByImpl.NO_ORDER, builder.buildOrderBy());

        //Make sure there is no parent.
        assertNull(builder.endOrderBy());

        OrderBy orderBy = new OrderByImpl(new SearchSort("test", SortOrder.ASC));
        builder = JqlQueryBuilder.newOrderByBuilder(orderBy);

        assertEquals(orderBy, builder.buildOrderBy());
        assertEquals(new OrderByImpl(new SearchSort("test", SortOrder.ASC), new SearchSort("cool")), builder.add("cool").buildOrderBy());
        assertNull(builder.endOrderBy());
    }

    @Test
    public void testNewOrderbyBuilderQuery() throws Exception
    {
        JqlOrderByBuilder builder = JqlQueryBuilder.newOrderByBuilder((Query)null);
        assertNotNull(builder);

        //Make sure the builder is empty initially.
        assertEquals(OrderByImpl.NO_ORDER, builder.buildOrderBy());

        //Make sure there is no parent.
        assertNull(builder.endOrderBy());

        builder = JqlQueryBuilder.newOrderByBuilder(new QueryImpl(null, null, null));
        assertNotNull(builder);

        //Make sure the builder is empty initially.
        assertEquals(OrderByImpl.NO_ORDER, builder.buildOrderBy());

        //Make sure there is no parent.
        assertNull(builder.endOrderBy());

        OrderBy orderBy = new OrderByImpl(new SearchSort("test", SortOrder.ASC));
        builder = JqlQueryBuilder.newOrderByBuilder(new QueryImpl(null, orderBy, null));

        assertEquals(orderBy, builder.buildOrderBy());
        assertEquals(new OrderByImpl(new SearchSort("test", SortOrder.ASC), new SearchSort("cool")), builder.add("cool").buildOrderBy());
        assertNull(builder.endOrderBy());
    }

    @Test
    public void testWasClause()
    {
        WasClauseImpl wasClause = new WasClauseImpl("status", Operator.WAS, new SingleValueOperand("Open"), new TerminalHistoryPredicate(Operator.AFTER, new SingleValueOperand(3500000L)));
        JqlClauseBuilder clauseBuilder = JqlQueryBuilder.newClauseBuilder(wasClause);
        final QueryImpl expectedSearchQuery = new QueryImpl(wasClause);
        assertEquals(expectedSearchQuery, clauseBuilder.buildQuery());
        assertEquals(wasClause, clauseBuilder.buildClause());
    }
}
