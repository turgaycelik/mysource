package com.atlassian.jira.jql.builder;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.plugin.jql.function.AllStandardIssueTypesFunction;
import com.atlassian.jira.plugin.jql.function.AllSubIssueTypesFunction;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.Operands;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Test for {@link DefaultJqlClauseBuilder}.
 *
 * @since v4.0
 */
public class TestDefaultJqlClauseBuilder extends MockControllerTestCase
{

    private TimeZoneManager timeZoneManager;

    @Before
    public void setUp() throws Exception
    {
        timeZoneManager = createMock(TimeZoneManager.class);
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(TimeZoneManager.class, timeZoneManager);
        ComponentAccessor.initialiseWorker(componentWorker);
    }


    @Test
    public void testClear() throws Exception
    {
        final IMocksControl control = EasyMock.createStrictControl();
        control.checkOrder(true);

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final SimpleClauseBuilder clauseBuilder2 = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clear()).andReturn(clauseBuilder2);
        expect(clauseBuilder2.clear()).andReturn(clauseBuilder);
        replay(timeZoneManager);
        control.replay();

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.clear());
        assertEquals(builder, builder.clear());

        control.verify();
    }

    @Test
    public void testEmptyCondition() throws Exception
    {
        TimeZoneManager timeZoneManager = createMock(TimeZoneManager.class);
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addEmptyCondition(null);
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        final String clauseName = "name";

        final IMocksControl control = EasyMock.createControl();

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.IS, EmptyOperand.EMPTY))).andReturn(clauseBuilder);

        replay(timeZoneManager);
        control.replay();

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addEmptyCondition(clauseName));

        control.verify();
    }

    @Test
    public void testEndWhere() throws Exception
    {
        final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        replay(timeZoneManager);
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(null, timeZoneManager);
        assertNull(builder.endWhere());

        builder = new DefaultJqlClauseBuilder(queryBuilder, timeZoneManager);
        assertSame(queryBuilder, builder.endWhere());
    }

    @Test
    public void testBuildQuery() throws Exception
    {
        replay(timeZoneManager);
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(null, timeZoneManager);
        builder.unresolved();
        final Clause expectedClause = builder.buildClause();

        assertEquals(new QueryImpl(expectedClause), builder.buildQuery());

        final JqlQueryBuilder parent = JqlQueryBuilder.newBuilder();
        parent.orderBy().assignee(null).endOrderBy().where().workRatio().gt().number(5L);

        builder = new DefaultJqlClauseBuilder(parent, timeZoneManager);
        builder.unresolved();

        final Query expectedQuery = parent.buildQuery();

        //This ensures that we call build on the "parent" because we look for "workRatio > 5" rather
        //than "resolution is EMPTY".
        assertEquals(expectedQuery, builder.buildQuery());
    }

    @Test
    public void testAddDateCondition() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addDateCondition(null, Operator.LESS_THAN, new Date());
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        try
        {
            builder.addDateCondition("qwerty", null, new Date());
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        try
        {
            builder.addDateCondition("shasg", Operator.IN, (Date) null);
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        final Date date = new Date();
        final String dateString = "%&*##*$&$";
        final String clauseName = "name";

        final IMocksControl control = EasyMock.createControl();

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final JqlDateSupport dateSupport = control.createMock(JqlDateSupport.class);

        expect(dateSupport.getDateString(date)).andReturn(dateString);
        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.LIKE, dateString))).andReturn(clauseBuilder);

        replay(timeZoneManager);
        control.replay();

        builder = createBuilder(clauseBuilder, dateSupport);
        assertEquals(builder, builder.addDateCondition(clauseName, Operator.LIKE, date));

        control.verify();
    }

    @Test
    public void testAddDateRangeConditionDate() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addDateRangeCondition(null, new Date(), new Date());
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        try
        {
            builder.addDateRangeCondition("qwerty", null, null);
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        final Date dateStart = new Date(34573284534L);
        final Date dateEnd = new Date(3452785834573895748L);
        final String dateStartString = "%&*##*$&$";
        final String dateEndString = "dhakjdhakjhsdsa";

        final String clauseName = "CreateDate";

        final IMocksControl control = EasyMock.createControl();

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final JqlDateSupport dateSupport = control.createMock(JqlDateSupport.class);

        expect(dateSupport.getDateString(dateStart)).andReturn(dateStartString).anyTimes();
        expect(dateSupport.getDateString(dateEnd)).andReturn(dateEndString).anyTimes();

        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, dateStartString))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.LESS_THAN_EQUALS, dateEndString))).andReturn(clauseBuilder);
        expect(
            clauseBuilder.clause(new AndClause(new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, dateStartString),
                new TerminalClauseImpl(clauseName, Operator.LESS_THAN_EQUALS, dateEndString)))).andReturn(clauseBuilder);

        control.replay();

        builder = createBuilder(clauseBuilder, dateSupport);
        assertEquals(builder, builder.addDateRangeCondition(clauseName, dateStart, null));
        assertEquals(builder, builder.addDateRangeCondition(clauseName, null, dateEnd));
        assertEquals(builder, builder.addDateRangeCondition(clauseName, dateStart, dateEnd));

        control.verify();
    }

    @Test
    public void testAddStringRangeConditionString() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addStringRangeCondition(null, "", "");
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        try
        {
            builder.addStringRangeCondition("qwerty", null, null);
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        final String start = "%&*##*$&$";
        final String end = "dhakjdhakjhsdsa";
        final String clauseName = "weqekwq";

        final IMocksControl control = EasyMock.createControl();

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, start))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.LESS_THAN_EQUALS, end))).andReturn(clauseBuilder);
        expect(
            clauseBuilder.clause(new AndClause(new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, start), new TerminalClauseImpl(
                clauseName, Operator.LESS_THAN_EQUALS, end)))).andReturn(clauseBuilder);

        replay(timeZoneManager);
        control.replay();

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addStringRangeCondition(clauseName, start, null));
        assertEquals(builder, builder.addStringRangeCondition(clauseName, null, end));
        assertEquals(builder, builder.addStringRangeCondition(clauseName, start, end));

        control.verify();
    }

    @Test
    public void testAddNumberRangeConditionString() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addNumberRangeCondition(null, 8L, 23L);
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        try
        {
            builder.addNumberRangeCondition("qwerty", null, null);
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        final Long start = 538748L;
        final Long end = 567L;
        final String clauseName = "blash";

        final IMocksControl control = EasyMock.createControl();

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, start))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.LESS_THAN_EQUALS, end))).andReturn(clauseBuilder);
        expect(
            clauseBuilder.clause(new AndClause(new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, start), new TerminalClauseImpl(
                clauseName, Operator.LESS_THAN_EQUALS, end)))).andReturn(clauseBuilder);

        replay(timeZoneManager);
        control.replay();

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addNumberRangeCondition(clauseName, start, null));
        assertEquals(builder, builder.addNumberRangeCondition(clauseName, null, end));
        assertEquals(builder, builder.addNumberRangeCondition(clauseName, start, end));

        control.verify();
    }

    @Test
    public void testAddRangeConditionString() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addRangeCondition(null, Operands.valueOf(10L), Operands.valueOf(57584L));
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        try
        {
            builder.addRangeCondition("qwerty", null, null);
            fail("Excepted an exception.");
        }
        catch (final IllegalArgumentException expected)
        {}

        final Operand start = Operands.valueOf("%&*##*$&$");
        final Operand end = Operands.valueOf("dhakjdhakjhsdsa");
        final String clauseName = "qwerty";

        final IMocksControl control = EasyMock.createControl();

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, start))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.LESS_THAN_EQUALS, end))).andReturn(clauseBuilder);
        expect(
            clauseBuilder.clause(new AndClause(new TerminalClauseImpl(clauseName, Operator.GREATER_THAN_EQUALS, start), new TerminalClauseImpl(
                clauseName, Operator.LESS_THAN_EQUALS, end)))).andReturn(clauseBuilder);

        replay(timeZoneManager);
        control.replay();

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addRangeCondition(clauseName, start, null));
        assertEquals(builder, builder.addRangeCondition(clauseName, null, end));
        assertEquals(builder, builder.addRangeCondition(clauseName, start, end));

        control.verify();
    }

    @Test
    public void testAddDateConditionVarArgs() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addDateCondition(null, new Date(), new Date());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", (Date[]) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", new Date(), null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final Date date1 = new Date(1L);
        final Date date2 = new Date(2L);
        final String date1String = "IamDate1String";
        final String date2String = "Date2StringIam";
        final String clauseName = "clauseName";

        final IMocksControl mocksControl = EasyMock.createControl();
        final SimpleClauseBuilder clauseBuilder = mocksControl.createMock(SimpleClauseBuilder.class);
        final JqlDateSupport dateSupport = mocksControl.createMock(JqlDateSupport.class);

        expect(dateSupport.getDateString(date1)).andReturn(date1String).atLeastOnce();
        expect(dateSupport.getDateString(date2)).andReturn(date2String).atLeastOnce();

        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.EQUALS, new SingleValueOperand(date1String)))).andReturn(
            clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.IN, new MultiValueOperand(date1String, date2String)))).andReturn(
            clauseBuilder);
        replay(timeZoneManager);
        mocksControl.replay();

        builder = createBuilder(clauseBuilder, dateSupport);
        assertEquals(builder, builder.addDateCondition(clauseName, date1));
        assertEquals(builder, builder.addDateCondition(clauseName, date1, date2));

        mocksControl.verify();
    }

    @Test
    public void testAddDateConditionVarArgsOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addDateCondition(null, Operator.IN, new Date(), new Date());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("brenden", (Operator) null, new Date(), new Date());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", Operator.IN, (Date[]) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", Operator.EQUALS, new Date(), null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", Operator.NOT_IN);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final Date date1 = new Date(1L);
        final Date date2 = new Date(2L);
        final String date1String = "IamDate1String";
        final String date2String = "Date2StringIam";
        final String clauseName = "clauseName";

        final IMocksControl mocksControl = EasyMock.createControl();
        final SimpleClauseBuilder clauseBuilder = mocksControl.createMock(SimpleClauseBuilder.class);
        final JqlDateSupport dateSupport = mocksControl.createMock(JqlDateSupport.class);

        expect(dateSupport.getDateString(date1)).andReturn(date1String).anyTimes();
        expect(dateSupport.getDateString(date2)).andReturn(date2String).anyTimes();

        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.LIKE, new MultiValueOperand(date1String)))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.LIKE, new MultiValueOperand(date1String, date2String)))).andReturn(
            clauseBuilder);
        replay(timeZoneManager);
        mocksControl.replay();

        builder = createBuilder(clauseBuilder, dateSupport);
        assertEquals(builder, builder.addDateCondition(clauseName, Operator.LIKE, new Date[] { date1 }));
        assertEquals(builder, builder.addDateCondition(clauseName, Operator.LIKE, date1, date2));

        mocksControl.verify();
    }

    @Test
    public void testAddDateConditionCollection() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addDateCondition(null, Collections.singletonList(new Date()));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", (Collection<Date>) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", Collections.<Date> singletonList(null));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", Collections.<Date> emptyList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final Date date1 = new Date(1L);
        final Date date2 = new Date(2L);
        final String date1String = "IamDate1String";
        final String date2String = "Date2StringIam";
        final String clauseName = "clauseName";

        final IMocksControl mocksControl = EasyMock.createControl();
        final SimpleClauseBuilder clauseBuilder = mocksControl.createMock(SimpleClauseBuilder.class);
        final JqlDateSupport dateSupport = mocksControl.createMock(JqlDateSupport.class);

        expect(dateSupport.getDateString(date1)).andReturn(date1String).atLeastOnce();
        expect(dateSupport.getDateString(date2)).andReturn(date2String).atLeastOnce();

        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.EQUALS, new SingleValueOperand(date1String)))).andReturn(
            clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.IN, new MultiValueOperand(date1String, date2String)))).andReturn(
            clauseBuilder);

        replay(timeZoneManager);
        mocksControl.replay();

        builder = createBuilder(clauseBuilder, dateSupport);
        assertEquals(builder, builder.addDateCondition(clauseName, Collections.singletonList(date1)));
        assertEquals(builder, builder.addDateCondition(clauseName, CollectionBuilder.newBuilder(date1, date2).asCollection()));

        mocksControl.verify();
    }

    @Test
    public void testAddDateConditionCollectionOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addDateCondition(null, Operator.LIKE, Collections.singletonList(new Date()));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("crap", null, Collections.singletonList(new Date()));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", Operator.GREATER_THAN_EQUALS, (Collection<Date>) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", Operator.IS, Collections.<Date> singletonList(null));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addDateCondition("cool", Operator.IS_NOT, Collections.<Date> emptyList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final Date date1 = new Date(1L);
        final Date date2 = new Date(2L);
        final String date1String = "IamDate1String";
        final String date2String = "Date2StringIam";
        final String clauseName = "clauseName";

        final IMocksControl mocksControl = EasyMock.createControl();
        final SimpleClauseBuilder clauseBuilder = mocksControl.createMock(SimpleClauseBuilder.class);
        final JqlDateSupport dateSupport = mocksControl.createMock(JqlDateSupport.class);

        expect(dateSupport.getDateString(date1)).andReturn(date1String).atLeastOnce();
        expect(dateSupport.getDateString(date2)).andReturn(date2String).atLeastOnce();

        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.GREATER_THAN, new MultiValueOperand(date1String)))).andReturn(
            clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl(clauseName, Operator.IS, new MultiValueOperand(date1String, date2String)))).andReturn(
            clauseBuilder);

        replay(timeZoneManager);
        mocksControl.replay();

        builder = createBuilder(clauseBuilder, dateSupport);
        assertEquals(builder, builder.addDateCondition(clauseName, Operator.GREATER_THAN, Collections.singletonList(date1)));
        assertEquals(builder, builder.addDateCondition(clauseName, Operator.IS, CollectionBuilder.newBuilder(date1, date2).asCollection()));

        mocksControl.verify();
    }

    @Test
    public void testEmpty() throws Exception
    {
        replay(timeZoneManager);
        final DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        assertNull(builder.buildClause());
    }

    @Test
    public void testAddFunctionConditionNoArgs() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addFunctionCondition(null, "blah");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.EQUALS, new FunctionOperand("func")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addFunctionCondition("name", "func"));

        verify(clauseBuilder);
    }

    @Test
    public void testAddFunctionConditionVarArgs() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addFunctionCondition(null, "blah", "what");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", null, "what");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", "blah", (String[]) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", "blah", null, "contains null");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.EQUALS, new FunctionOperand("func", "arg1", "arg2")))).andReturn(
            clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addFunctionCondition("name", "func", "arg1", "arg2"));

        verify(clauseBuilder);
    }

    @Test
    public void testAddFunctionConditionCollection() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addFunctionCondition(null, "blah", "what");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", null, "what");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", "blah", (Collection<String>) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", "blah", Collections.<String> singletonList(null));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        final Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "arg3").asCollection();

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.EQUALS, new FunctionOperand("func", args)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addFunctionCondition("name", "func", args));

        verify(clauseBuilder);
    }

    @Test
    public void testAddFunctionConditionWithOperatorNoArgs() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addFunctionCondition(null, Operator.EQUALS, "blah");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", Operator.EQUALS, null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", null, "func");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.LESS_THAN, new FunctionOperand("func")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addFunctionCondition("name", Operator.LESS_THAN, "func"));

        verify(clauseBuilder);
    }

    @Test
    public void testAddFunctionConditionWithOperatorVarArgs() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addFunctionCondition(null, Operator.EQUALS, "blah", "what");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", Operator.EQUALS, null, "what");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", Operator.EQUALS, "blah", (String[]) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", Operator.EQUALS, "blah", null, "contains null");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", (Operator) null, "blah", "aaa");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.GREATER_THAN, new FunctionOperand("func", "arg1", "arg2")))).andReturn(
            clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addFunctionCondition("name", Operator.GREATER_THAN, "func", "arg1", "arg2"));

        verify(clauseBuilder);
    }

    @Test
    public void testAddFunctionConditionWithOperatorCollection() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addFunctionCondition(null, Operator.NOT_EQUALS, "blah", "what");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", Operator.NOT_EQUALS, null, "what");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", Operator.NOT_EQUALS, "blah", (Collection<String>) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", Operator.NOT_EQUALS, "blah", Collections.<String> singletonList(null));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addFunctionCondition("cool", null, "blah", Collections.<String> singletonList("me"));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        final Collection<String> args = CollectionBuilder.newBuilder("arg1", "arg2", "arg3").asCollection();

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.NOT_EQUALS, new FunctionOperand("func", args)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addFunctionCondition("name", Operator.NOT_EQUALS, "func", args));

        verify(clauseBuilder);
    }

    @Test
    public void testAddStringConditionSingle() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addStringCondition(null, "blah");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", (String) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.EQUALS, "value"))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addStringCondition("name", "value"));

        verify(clauseBuilder);
    }

    @Test
    public void testAddStringConditionVarArgs() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addStringCondition(null, "blah", "blah2");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", (String[]) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", "me", null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.EQUALS, new SingleValueOperand("value")))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.IN, new MultiValueOperand("value", "value2")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addStringCondition("name", new String[] { "value" }));
        assertEquals(builder, builder.addStringCondition("name", "value", "value2"));

        verify(clauseBuilder);
    }

    @Test
    public void testAddStringConditionCollection() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addStringCondition(null, CollectionBuilder.newBuilder("blah", "blah2").asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", (Collection<String>) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", CollectionBuilder.newBuilder("blah", null).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.EQUALS, new SingleValueOperand("value")))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.IN, new MultiValueOperand("value", "value2")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addStringCondition("name", Collections.singletonList("value")));
        assertEquals(builder, builder.addStringCondition("name", CollectionBuilder.newBuilder("value", "value2").asList()));

        verify(clauseBuilder);
    }

    @Test
    public void testAddStringConditionSingleOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addStringCondition(null, Operator.IN, "blah");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", Operator.IS_NOT, (String) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", null, "ejklwr");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.LIKE, "value"))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addStringCondition("name", Operator.LIKE, "value"));

        verify(clauseBuilder);
    }

    @Test
    public void testAddStringConditionVarArgsOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addStringCondition(null, Operator.LIKE, "blah", "blah2");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", Operator.IS_NOT, (String[]) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", Operator.LIKE, "me", null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", (Operator) null, "me", "two");
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.LIKE, new MultiValueOperand("value")))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.NOT_IN, new MultiValueOperand("value", "value2")))).andReturn(
            clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addStringCondition("name", Operator.LIKE, new String[] { "value" }));
        assertEquals(builder, builder.addStringCondition("name", Operator.NOT_IN, "value", "value2"));

        verify(clauseBuilder);
    }

    @Test
    public void testAddStringConditionCollectionOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addStringCondition(null, Operator.GREATER_THAN, CollectionBuilder.newBuilder("blah", "blah2").asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", Operator.LESS_THAN, (Collection<String>) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", Operator.LESS_THAN, CollectionBuilder.newBuilder("blah", null).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addStringCondition("cool", null, CollectionBuilder.newBuilder("blah", "qwerty").asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.GREATER_THAN_EQUALS, new MultiValueOperand("value")))).andReturn(
            clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.LESS_THAN_EQUALS, new MultiValueOperand("value", "value2")))).andReturn(
            clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addStringCondition("name", Operator.GREATER_THAN_EQUALS, Collections.singletonList("value")));
        assertEquals(builder, builder.addStringCondition("name", Operator.LESS_THAN_EQUALS, CollectionBuilder.newBuilder("value", "value2").asList()));

        verify(clauseBuilder);
    }

    @Test
    public void testAddNumberConditionSingle() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addNumberCondition(null, 5L);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("DJHSKJD", (Long) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.EQUALS, 5))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addNumberCondition("name", 5L));

        verify(clauseBuilder);
    }

    @Test
    public void testAddNumberConditionVarArgs() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addNumberCondition(null, 5L, 6L);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", (Long[]) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", 6L, null, 56383L);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.EQUALS, new SingleValueOperand(6L)))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.IN, new MultiValueOperand(6L, 8L)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addNumberCondition("name", new Long[] { 6L }));
        assertEquals(builder, builder.addNumberCondition("name", 6L, 8L));

        verify(clauseBuilder);
    }

    @Test
    public void testAddNumberConditionCollection() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addNumberCondition(null, CollectionBuilder.newBuilder(5l, 6l).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", (Collection<Long>) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", CollectionBuilder.newBuilder(5L, null).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.EQUALS, new SingleValueOperand(5L)))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.IN, new MultiValueOperand(6L, 5747L)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addNumberCondition("name", Collections.singletonList(5L)));
        assertEquals(builder, builder.addNumberCondition("name", CollectionBuilder.newBuilder(6L, 5747L).asList()));

        verify(clauseBuilder);
    }

    @Test
    public void testAddNumberConditionSingleOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addNumberCondition(null, Operator.IN, 6L);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", null, 5L);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", Operator.LESS_THAN, (Long) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.LIKE, 6L))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addNumberCondition("name", Operator.LIKE, 6L));

        verify(clauseBuilder);
    }

    @Test
    public void testAddNumberConditionMultipleOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addNumberCondition(null, Operator.LIKE, 5L, 8L);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", Operator.IS_NOT, (Long[]) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", Operator.IS_NOT, 5L, null, 47373L);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", (Operator) null, 3L, 95L);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.LIKE, new MultiValueOperand(7L)))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.NOT_IN, new MultiValueOperand(6L, 12L)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addNumberCondition("name", Operator.LIKE, new Long[] { 7L }));
        assertEquals(builder, builder.addNumberCondition("name", Operator.NOT_IN, 6L, 12L));

        verify(clauseBuilder);
    }

    @Test
    public void testAddNumberConditionCollectionOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addNumberCondition(null, Operator.GREATER_THAN, CollectionBuilder.newBuilder(65l, 3423l).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", Operator.LESS_THAN, (Collection<Long>) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", Operator.LESS_THAN, CollectionBuilder.newBuilder(6l, null).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addNumberCondition("cool", null, CollectionBuilder.newBuilder(6l, 7657l).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.GREATER_THAN_EQUALS, new MultiValueOperand(5L)))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.LESS_THAN_EQUALS, new MultiValueOperand(67L, 654L)))).andReturn(
            clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addNumberCondition("name", Operator.GREATER_THAN_EQUALS, Collections.singletonList(5L)));
        assertEquals(builder, builder.addNumberCondition("name", Operator.LESS_THAN_EQUALS, CollectionBuilder.newBuilder(67L, 654L).asList()));

        verify(clauseBuilder);
    }

    @Test
    public void testConditionBuilder() throws Exception
    {
        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("meh", Operator.EQUALS, 16L))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addCondition("meh").eq().number(16L));

        verify(clauseBuilder);
    }

    @Test
    public void testConditionSingle() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addCondition(null, new SingleValueOperand("5"));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("DJHSKJD", (Operand) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.EQUALS, new SingleValueOperand(5L)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addCondition("name", new SingleValueOperand(5L)));

        verify(clauseBuilder);
    }

    @Test
    public void testAddConditionVarArgs() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addCondition(null, new SingleValueOperand(5L), new SingleValueOperand(6L));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", (Operand[]) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", new SingleValueOperand(6L), null, new SingleValueOperand(56383L));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.IN, new MultiValueOperand(6L)))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.IN, new MultiValueOperand(6L, 8L)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addCondition("name", new Operand[] { new SingleValueOperand(6L) }));
        assertEquals(builder, builder.addCondition("name", new SingleValueOperand(6L), new SingleValueOperand(8L)));

        verify(clauseBuilder);
    }

    @Test
    public void testAddConditionCollection() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addCondition(null, CollectionBuilder.newBuilder(new SingleValueOperand(5l), new SingleValueOperand(6l)).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", (Collection<Operand>) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", CollectionBuilder.newBuilder(new SingleValueOperand(5L), null).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.IN, new MultiValueOperand(5L)))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.IN, new MultiValueOperand(6L, 5747L)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addCondition("name", Collections.singletonList(new SingleValueOperand(5L))));
        assertEquals(builder, builder.addCondition("name",
            CollectionBuilder.newBuilder(new SingleValueOperand(6L), new SingleValueOperand(5747L)).asList()));

        verify(clauseBuilder);
    }

    @Test
    public void testAddConditionSingleOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addCondition(null, Operator.IN, new SingleValueOperand(6L));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", null, new SingleValueOperand(5L));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", Operator.LESS_THAN, (Operand) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.LIKE, 6L))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addCondition("name", Operator.LIKE, new SingleValueOperand(6L)));

        verify(clauseBuilder);
    }

    @Test
    public void testAddConditionMultipleOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addCondition(null, Operator.LIKE, new SingleValueOperand(5L), new SingleValueOperand(8L));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", Operator.IS_NOT, (SingleValueOperand[]) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", Operator.IS_NOT, new SingleValueOperand(5L), null, new SingleValueOperand(5349239852L));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", (Operator) null, new SingleValueOperand(5L), new SingleValueOperand(95L));
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.LIKE, new MultiValueOperand(7L)))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.NOT_IN, new MultiValueOperand(6L, 12L)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addCondition("name", Operator.LIKE, new Operand[] { new SingleValueOperand(7L) }));
        assertEquals(builder, builder.addCondition("name", Operator.NOT_IN, new SingleValueOperand(6L), new SingleValueOperand(12L)));

        verify(clauseBuilder);
    }

    @Test
    public void testAddConditionCollectionOperator() throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            builder.addCondition(null, Operator.GREATER_THAN,
                CollectionBuilder.newBuilder(new SingleValueOperand(65l), new SingleValueOperand(3423l)).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", Operator.LESS_THAN, (Collection<Operand>) null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", Operator.LESS_THAN, CollectionBuilder.newBuilder(new SingleValueOperand(6l), null).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            builder.addCondition("cool", null, CollectionBuilder.newBuilder(new SingleValueOperand(6L), new SingleValueOperand(7657l)).asList());
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.GREATER_THAN_EQUALS, new MultiValueOperand(5L)))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl("name", Operator.LESS_THAN_EQUALS, new MultiValueOperand(67L, 654L)))).andReturn(
            clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addCondition("name", Operator.GREATER_THAN_EQUALS, Collections.singletonList(new SingleValueOperand(5L))));
        assertEquals(builder, builder.addCondition("name", Operator.LESS_THAN_EQUALS, CollectionBuilder.newBuilder(new SingleValueOperand(67L),
            new SingleValueOperand(654L)).asList()));

        verify(clauseBuilder);
    }

    @Test
    public void testBuildClause() throws Exception
    {
        final Clause expectedReturn = new TerminalClauseImpl("check", Operator.GREATER_THAN_EQUALS, 5L);

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.build()).andReturn(expectedReturn);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(expectedReturn, builder.buildClause());

        verify(clauseBuilder);
    }

    @Test
    public void testDefaultAnd() throws Exception
    {
        final IMocksControl control = EasyMock.createStrictControl();
        control.checkOrder(true);

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final SimpleClauseBuilder clauseBuilder2 = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.defaultAnd()).andReturn(clauseBuilder2);
        expect(clauseBuilder2.defaultAnd()).andReturn(clauseBuilder);
        replay(timeZoneManager);
        control.replay();

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.defaultAnd());
        assertEquals(builder, builder.defaultAnd());

        control.verify();
    }

    @Test
    public void testAnd() throws Exception
    {
        final IMocksControl control = EasyMock.createStrictControl();
        control.checkOrder(true);

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final SimpleClauseBuilder clauseBuilder2 = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.and()).andReturn(clauseBuilder2);
        expect(clauseBuilder2.and()).andReturn(clauseBuilder);
        replay(timeZoneManager);
        control.replay();

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.and());
        assertEquals(builder, builder.and());

        control.verify();
    }

    @Test
    public void testDefaultOr() throws Exception
    {
        final IMocksControl control = EasyMock.createStrictControl();
        control.checkOrder(true);

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final SimpleClauseBuilder clauseBuilder2 = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.defaultOr()).andReturn(clauseBuilder2);
        expect(clauseBuilder2.defaultOr()).andReturn(clauseBuilder);
        replay(timeZoneManager);
        control.replay();

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.defaultOr());
        assertEquals(builder, builder.defaultOr());

        control.verify();
    }

    @Test
    public void testOr() throws Exception
    {
        final IMocksControl control = EasyMock.createStrictControl();
        control.checkOrder(true);

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final SimpleClauseBuilder clauseBuilder2 = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.or()).andReturn(clauseBuilder2);
        expect(clauseBuilder2.or()).andReturn(clauseBuilder);
        replay(timeZoneManager);
        control.replay();

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.or());
        assertEquals(builder, builder.or());

        control.verify();
    }

    @Test
    public void testDefaultNone() throws Exception
    {
        final IMocksControl control = EasyMock.createStrictControl();
        control.checkOrder(true);

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final SimpleClauseBuilder clauseBuilder2 = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.defaultNone()).andReturn(clauseBuilder2);
        expect(clauseBuilder2.defaultNone()).andReturn(clauseBuilder);
        replay(timeZoneManager);
        control.replay();

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.defaultNone());
        assertEquals(builder, builder.defaultNone());

        control.verify();
    }

    @Test
    public void testNot() throws Exception
    {
        final IMocksControl control = EasyMock.createStrictControl();
        control.checkOrder(true);

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final SimpleClauseBuilder clauseBuilder2 = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.not()).andReturn(clauseBuilder2);
        expect(clauseBuilder2.not()).andReturn(clauseBuilder);
        replay(timeZoneManager);
        control.replay();

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.not());
        assertEquals(builder, builder.not());

        control.verify();
    }

    @Test
    public void testSub() throws Exception
    {
        final IMocksControl control = EasyMock.createStrictControl();
        control.checkOrder(true);

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final SimpleClauseBuilder clauseBuilder2 = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.sub()).andReturn(clauseBuilder2);
        expect(clauseBuilder2.sub()).andReturn(clauseBuilder);
        replay(timeZoneManager);
        control.replay();

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.sub());
        assertEquals(builder, builder.sub());

        control.verify();
    }

    @Test
    public void testEndSub() throws Exception
    {
        final IMocksControl control = EasyMock.createStrictControl();
        control.checkOrder(true);

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final SimpleClauseBuilder clauseBuilder2 = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.endsub()).andReturn(clauseBuilder2);
        expect(clauseBuilder2.endsub()).andReturn(clauseBuilder);
        replay(timeZoneManager);
        control.replay();

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.endsub());
        assertEquals(builder, builder.endsub());

        control.verify();
    }

    @Test
    public void testAddClause() throws Exception
    {
        final Clause clause1 = new TerminalClauseImpl("name", Operator.EQUALS, "value");
        final Clause clause2 = new TerminalClauseImpl("bad", Operator.NOT_EQUALS, "egg");

        final IMocksControl control = EasyMock.createStrictControl();
        control.checkOrder(true);

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final SimpleClauseBuilder clauseBuilder2 = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(clause1)).andReturn(clauseBuilder2);
        expect(clauseBuilder2.clause(clause2)).andReturn(clauseBuilder);
        replay(timeZoneManager);
        control.replay();

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, builder.addClause(clause1));
        assertEquals(builder, builder.addClause(clause2));

        control.verify();
    }

    @Test
    public void testAffectedVersionSingle() throws Exception
    {
        assertStringSingle("affectedVersion", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.affectedVersion(argument);
            }
        });
    }

    @Test
    public void testAffectedVersionMultiple() throws Exception
    {
        assertStringVarArgs("affectedVersion", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.affectedVersion(argument);
            }
        });
    }

    @Test
    public void testAffectedVersionIsEmpty() throws Exception
    {
        assertEmpty("affectedVersion", new TestCallable<JqlClauseBuilder, Void>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.affectedVersionIsEmpty();
            }
        });
    }

    @Test
    public void testAffectedVersion() throws Exception
    {
        assertBuilder("affectedVersion", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.affectedVersion();
            }
        });
    }

    @Test
    public void testFixVersionSingle() throws Exception
    {
        assertStringSingle("fixVersion", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.fixVersion(argument);
            }
        });
    }

    @Test
    public void testFixVersionMultiple() throws Exception
    {
        assertStringVarArgs("fixVersion", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.fixVersion(argument);
            }
        });
    }

    @Test
    public void testFixVersionIdSingle() throws Exception
    {
        assertLongSingle("fixVersion", new TestCallable<JqlClauseBuilder, Long>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Long argument)
            {
                return builder.fixVersion(argument);
            }
        });
    }

    @Test
    public void testFixVersionIdMultiple() throws Exception
    {
        assertLongVarArgs("fixVersion", new TestCallable<JqlClauseBuilder, Long[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Long[] argument)
            {
                return builder.fixVersion(argument);
            }
        });
    }

    @Test
    public void testFixVersionIsEmpty() throws Exception
    {
        assertEmpty("fixVersion", new TestCallable<JqlClauseBuilder, Void>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.fixVersionIsEmpty();
            }
        });
    }

    @Test
    public void testFixVersion() throws Exception
    {
        assertBuilder("fixVersion", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.fixVersion();
            }
        });
    }

    @Test
    public void testPriorityMultiple() throws Exception
    {
        assertStringVarArgs("priority", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.priority(argument);
            }
        });
    }

    @Test
    public void testPriority() throws Exception
    {
        assertBuilder("priority", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.priority();
            }
        });
    }

    @Test
    public void testResolutionMultiple() throws Exception
    {
        assertStringVarArgs("resolution", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.resolution(argument);
            }
        });
    }

    @Test
    public void testResolution() throws Exception
    {
        assertBuilder("resolution", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.resolution();
            }
        });
    }

    @Test
    public void testUnresolved() throws Exception
    {
        final SimpleClauseBuilder builder = EasyMock.createMock(SimpleClauseBuilder.class);
        final TerminalClause clause = new TerminalClauseImpl("resolution", Operator.EQUALS, "Unresolved");

        expect(builder.clause(clause)).andReturn(builder);

        final JqlClauseBuilder clauseBuilder = createBuilder(builder);
        replay(builder);

        assertSame(clauseBuilder, clauseBuilder.unresolved());

        verify(builder);
    }

    @Test
    public void testStatusMultiple() throws Exception
    {
        assertStringVarArgs("status", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.status(argument);
            }
        });
    }

    @Test
    public void testStatusBuilder() throws Exception
    {
        assertBuilder("status", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.status();
            }
        });
    }

    @Test
    public void testIssueTypeMultiple() throws Exception
    {
        assertStringVarArgs("issuetype", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.issueType(argument);
            }
        });
    }

    @Test
    public void testIssueTypeBuilder() throws Exception
    {
        assertBuilder("issuetype", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.issueType();
            }
        });
    }

    @Test
    public void testIssueTypeIsStandard() throws Exception
    {
        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(
            clauseBuilder.clause(new TerminalClauseImpl("issuetype", Operator.IN, new FunctionOperand(
                AllStandardIssueTypesFunction.FUNCTION_STANDARD_ISSUE_TYPES)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        builder.issueTypeIsStandard();

        verify(clauseBuilder);
    }

    @Test
    public void testIssueTypeIsSubtask() throws Exception
    {
        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(
            clauseBuilder.clause(new TerminalClauseImpl("issuetype", Operator.IN, new FunctionOperand(
                AllSubIssueTypesFunction.FUNCTION_SUB_ISSUE_TYPES)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        builder.issueTypeIsSubtask();

        verify(clauseBuilder);
    }

    @Test
    public void testDescriptionSingleValue() throws Exception
    {
        assertStringSingle("description", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.description(argument);
            }
        }, Operator.LIKE);
    }

    @Test
    public void testDescriptionIsEmpty() throws Exception
    {
        assertEmpty("description", new TestCallable<JqlClauseBuilder, Void>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.descriptionIsEmpty();
            }
        });
    }

    @Test
    public void testDescriptionBuilder() throws Exception
    {
        assertBuilder("description", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.description();
            }
        });
    }

    @Test
    public void testSummarySingleValue() throws Exception
    {
        assertStringSingle("summary", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.summary(argument);
            }
        }, Operator.LIKE);
    }

    @Test
    public void testSummaryBuilder() throws Exception
    {
        assertBuilder("summary", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.summary();
            }
        });
    }

    @Test
    public void testEnvironmentSingleValue() throws Exception
    {
        assertStringSingle("environment", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.environment(argument);
            }
        }, Operator.LIKE);
    }

    @Test
    public void testEnvironmentIsEmpty() throws Exception
    {
        assertEmpty("environment", new TestCallable<JqlClauseBuilder, Void>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.environmentIsEmpty();
            }
        });
    }

    @Test
    public void testEnvironmentBuilder() throws Exception
    {
        assertBuilder("environment", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.environment();
            }
        });
    }

    @Test
    public void testCommentSingleValue() throws Exception
    {
        assertStringSingle("comment", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.comment(argument);
            }
        }, Operator.LIKE);
    }

    @Test
    public void testCommentBuilder() throws Exception
    {
        assertBuilder("comment", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.comment();
            }
        });
    }

    @Test
    public void testProjectStrings() throws Exception
    {
        assertStringVarArgs("project", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.project(argument);
            }
        });
    }

    @Test
    public void testProjectLongs() throws Exception
    {
        assertLongVarArgs("project", new TestCallable<JqlClauseBuilder, Long[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Long[] argument)
            {
                return builder.project(argument);
            }
        });
    }

    @Test
    public void testProjectBuilder() throws Exception
    {
        assertBuilder("project", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.project();
            }
        });
    }

    @Test
    public void testCategoryStrings() throws Exception
    {
        assertStringVarArgs("category", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.category(argument);
            }
        });
    }

    @Test
    public void testCategoryBuilder() throws Exception
    {
        assertBuilder("category", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.category();
            }
        });
    }

    @Test
    public void testCreatedAfterDate() throws Exception
    {
        assertDateAfterDate("created", new TestCallable<JqlClauseBuilder, Date>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Date argument)
            {
                return builder.createdAfter(argument);
            }
        });
    }

    @Test
    public void testCreatedAfterString() throws Exception
    {
        assertDateAfterString("created", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.createdAfter(argument);
            }
        });
    }

    @Test
    public void testCreatedBetweenDate() throws Exception
    {
        assertDateRangeDate("created", new TestCallable<JqlClauseBuilder, Date[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Date[] argument)
            {
                return builder.createdBetween(argument[0], argument[1]);
            }
        });
    }

    @Test
    public void testCreatedBetweenString() throws Exception
    {
        assertDateRangeString("created", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.createdBetween(argument[0], argument[1]);
            }
        });
    }

    @Test
    public void testCreatedBuilder() throws Exception
    {
        assertBuilder("created", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.created();
            }
        });
    }

    @Test
    public void testUpdatedAfterDate() throws Exception
    {
        assertDateAfterDate("updated", new TestCallable<JqlClauseBuilder, Date>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Date argument)
            {
                return builder.updatedAfter(argument);
            }
        });
    }

    @Test
    public void testUpdatedAfterString() throws Exception
    {
        assertDateAfterString("updated", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.updatedAfter(argument);
            }
        });
    }

    @Test
    public void testUpdatedBetweenDate() throws Exception
    {
        assertDateRangeDate("updated", new TestCallable<JqlClauseBuilder, Date[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Date[] argument)
            {
                return builder.updatedBetween(argument[0], argument[1]);
            }
        });
    }

    @Test
    public void testUpdatedBetweenString() throws Exception
    {
        assertDateRangeString("updated", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.updatedBetween(argument[0], argument[1]);
            }
        });
    }

    @Test
    public void testUpdatedBuilder() throws Exception
    {
        assertBuilder("updated", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.updated();
            }
        });
    }

    @Test
    public void testDueAfterDate() throws Exception
    {
        assertDateAfterDate("due", new TestCallable<JqlClauseBuilder, Date>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Date argument)
            {
                return builder.dueAfter(argument);
            }
        });
    }

    @Test
    public void testDueAfterString() throws Exception
    {
        assertDateAfterString("due", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.dueAfter(argument);
            }
        });
    }

    @Test
    public void testDueBetweenDate() throws Exception
    {
        assertDateRangeDate("due", new TestCallable<JqlClauseBuilder, Date[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Date[] argument)
            {
                return builder.dueBetween(argument[0], argument[1]);
            }
        });
    }

    @Test
    public void testDueBetweenString() throws Exception
    {
        assertDateRangeString("due", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.dueBetween(argument[0], argument[1]);
            }
        });
    }

    @Test
    public void testDueBuilder() throws Exception
    {
        assertBuilder("due", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.due();
            }
        });
    }


    @Test
    public void testLastViewedAfterDate() throws Exception
    {
        assertDateAfterDate("lastViewed", new TestCallable<JqlClauseBuilder, Date>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Date argument)
            {
                return builder.lastViewedAfter(argument);
            }
        });
    }

    @Test
    public void testLastViewedAfterString() throws Exception
    {
        assertDateAfterString("lastViewed", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.lastViewedAfter(argument);
            }
        });
    }

    @Test
    public void testLastViewedBetweenDate() throws Exception
    {
        assertDateRangeDate("lastViewed", new TestCallable<JqlClauseBuilder, Date[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Date[] argument)
            {
                return builder.lastViewedBetween(argument[0], argument[1]);
            }
        });
    }

    @Test
    public void testLastViewedBetweenString() throws Exception
    {
        assertDateRangeString("lastViewed", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.lastViewedBetween(argument[0], argument[1]);
            }
        });
    }

    @Test
    public void testLastViewedBuilder() throws Exception
    {
        assertBuilder("lastViewed", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.lastViewed();
            }
        });
    }

    @Test
    public void testResolutionDateBetweenDate() throws Exception
    {
        assertDateRangeDate("resolved", new TestCallable<JqlClauseBuilder, Date[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Date[] argument)
            {
                return builder.resolutionDateBetween(argument[0], argument[1]);
            }
        });
    }

    // JRA-21590
    @Test
    public void testResolutionDateBetweenString() throws Exception
    {
        assertDateRangeString("resolved", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.resolutionDateBetween(argument[0], argument[1]);
            }
        });
    }

    @Test
    public void testCreateBetween()
    {
        replay(timeZoneManager);
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        builder.createdBetween("-1h", null);
        assertEquals("{created >= \"-1h\"}", builder.buildClause().toString());
        builder = new DefaultJqlClauseBuilder(timeZoneManager);
        builder.createdBetween(null, "-1h");
        assertEquals("{created <= \"-1h\"}", builder.buildClause().toString());
    }

    @Test
    public void testResolutionDateBuilder() throws Exception
    {
        assertBuilder("resolved", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.resolutionDate();
            }
        });
    }

    @Test
    public void testReporterUser() throws Exception
    {
        assertStringSingle("reporter", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.reporterUser(argument);
            }
        });
    }

    @Test
    public void testReporterInGroup() throws Exception
    {
        final String jqlName = "reporter";
        final String group = "group1";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.IN, new FunctionOperand("membersOf", group)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        try
        {
            builder.reporterInGroup(null);
            fail("I was expecting an exception with null group");
        }
        catch (final IllegalArgumentException expected)
        {}

        assertEquals(builder, builder.reporterInGroup(group));

        verify(clauseBuilder);
    }

    @Test
    public void testReporterIsCurrent() throws Exception
    {
        final String jqlName = "reporter";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.EQUALS, new FunctionOperand("currentUser")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        assertEquals(builder, builder.reporterIsCurrentUser());

        verify(clauseBuilder);
    }

    @Test
    public void testReporterIsEmpty() throws Exception
    {
        assertEmpty("reporter", new TestCallable<JqlClauseBuilder, Void>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.reporterIsEmpty();
            }
        });
    }

    @Test
    public void testReporterBuilder() throws Exception
    {
        assertBuilder("reporter", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.reporter();
            }
        });
    }

    @Test
    public void testAssigneeUser() throws Exception
    {
        assertStringSingle("assignee", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.assigneeUser(argument);
            }
        });
    }

    @Test
    public void testAssigneeInGroup() throws Exception
    {
        final String jqlName = "assignee";
        final String group = "group1";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.IN, new FunctionOperand("membersOf", group)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        try
        {
            builder.assigneeInGroup(null);
            fail("I was expecting an exception with null group");
        }
        catch (final IllegalArgumentException expected)
        {}

        assertEquals(builder, builder.assigneeInGroup(group));

        verify(clauseBuilder);
    }

    @Test
    public void testAssigneeIsCurrent() throws Exception
    {
        final String jqlName = "assignee";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.EQUALS, new FunctionOperand("currentUser")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        assertEquals(builder, builder.assigneeIsCurrentUser());

        verify(clauseBuilder);
    }

    @Test
    public void testAssigneeIsEmpty() throws Exception
    {
        assertEmpty("assignee", new TestCallable<JqlClauseBuilder, Void>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.assigneeIsEmpty();
            }
        });
    }

    @Test
    public void testAssigneeBuilder() throws Exception
    {
        assertBuilder("assignee", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.assignee();
            }
        });
    }

    @Test
    public void testComponentStrings() throws Exception
    {
        assertStringVarArgs("component", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.component(argument);
            }
        });
    }

    @Test
    public void testComponentLongs() throws Exception
    {
        assertLongVarArgs("component", new TestCallable<JqlClauseBuilder, Long[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Long[] argument)
            {
                return builder.component(argument);
            }
        });
    }

    @Test
    public void testComponentEmpty() throws Exception
    {
        assertEmpty("component", new TestCallable<JqlClauseBuilder, Void>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.componentIsEmpty();
            }
        });
    }

    @Test
    public void testComponentBuilder() throws Exception
    {
        assertBuilder("component", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.component();
            }
        });
    }

    @Test
    public void testIssueKeys() throws Exception
    {
        assertStringVarArgs("key", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.issue(argument);
            }
        });
    }

    @Test
    public void testIssueKeyInHistory() throws Exception
    {
        final String jqlName = "key";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.IN, new FunctionOperand("issueHistory")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        assertEquals(builder, builder.issueInHistory());

        verify(clauseBuilder);
    }

    @Test
    public void testIssueKeyInWatchedIssues() throws Exception
    {
        final String jqlName = "key";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.IN, new FunctionOperand("watchedIssues")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        assertEquals(builder, builder.issueInWatchedIssues());

        verify(clauseBuilder);
    }

    @Test
    public void testIssueKeyInVotedIssues() throws Exception
    {
        final String jqlName = "key";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.IN, new FunctionOperand("votedIssues")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        assertEquals(builder, builder.issueInVotedIssues());

        verify(clauseBuilder);
    }

    @Test
    public void testIssueKeyBuilder() throws Exception
    {
        assertBuilder("key", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.issue();
            }
        });
    }

    @Test
    public void testIssueParent() throws Exception
    {
        assertStringVarArgs("parent", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.issueParent(argument);
            }
        });
    }

    @Test
    public void testIssueParentBuilder() throws Exception
    {
        assertBuilder("parent", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.issueParent();
            }
        });
    }

    @Test
    public void testOriginalEstimateBuilder() throws Exception
    {
        assertBuilder("originalEstimate", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.originalEstimate();
            }
        });
    }

    @Test
    public void testCurrentEstimateBuilder() throws Exception
    {
        assertBuilder("remainingEstimate", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.currentEstimate();
            }
        });
    }

    @Test
    public void testTimespent() throws Exception
    {
        assertBuilder("timespent", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.timeSpent();
            }
        });
    }

    @Test
    public void testWorkRatio() throws Exception
    {
        assertBuilder("workratio", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.workRatio();
            }
        });
    }

    @Test
    public void testIssueLevel() throws Exception
    {
        assertStringVarArgs("level", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.level(argument);
            }
        });
    }

    @Test
    public void testIssueLevelBuilder() throws Exception
    {
        assertBuilder("level", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.level();
            }
        });
    }

    @Test
    public void testSavedFilter() throws Exception
    {
        assertStringVarArgs("filter", new TestCallable<JqlClauseBuilder, String[]>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String[] argument)
            {
                return builder.savedFilter(argument);
            }
        });
    }

    @Test
    public void testSavedFilterBuilder() throws Exception
    {
        assertBuilder("filter", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.savedFilter();
            }
        });
    }

    @Test
    public void testVotesBuilder() throws Exception
    {
        assertBuilder("votes", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.votes();
            }
        });
    }

    @Test
    public void testVoterUser() throws Exception
    {
        assertStringSingle("voter", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.voterUser(argument);
            }
        });
    }

    @Test
    public void testVoterInGroup() throws Exception
    {
        final String jqlName = "voter";
        final String group = "group1";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.IN, new FunctionOperand("membersOf", group)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        try
        {
            builder.voterInGroup(null);
            fail("I was expecting an exception with null group");
        }
        catch (final IllegalArgumentException expected)
        {}

        assertEquals(builder, builder.voterInGroup(group));

        verify(clauseBuilder);
    }

    @Test
    public void testVoterIsCurrent() throws Exception
    {
        final String jqlName = "voter";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.EQUALS, new FunctionOperand("currentUser")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        assertEquals(builder, builder.voterIsCurrentUser());

        verify(clauseBuilder);
    }

    @Test
    public void testVoterIsEmpty() throws Exception
    {
        assertEmpty("voter", new TestCallable<JqlClauseBuilder, Void>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.voterIsEmpty();
            }
        });
    }

    @Test
    public void testVoterBuilder() throws Exception
    {
        assertBuilder("voter", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.voter();
            }
        });
    }

    @Test
    public void testWatcherUser() throws Exception
    {
        assertStringSingle("watcher", new TestCallable<JqlClauseBuilder, String>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final String argument)
            {
                return builder.watcherUser(argument);
            }
        });
    }

    @Test
    public void testWatcherInGroup() throws Exception
    {
        final String jqlName = "watcher";
        final String group = "group1";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.IN, new FunctionOperand("membersOf", group)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        try
        {
            builder.watcherInGroup(null);
            fail("I was expecting an exception with null group");
        }
        catch (final IllegalArgumentException expected)
        {}

        assertEquals(builder, builder.watcherInGroup(group));

        verify(clauseBuilder);
    }

    @Test
    public void testWatcherIsCurrent() throws Exception
    {
        final String jqlName = "watcher";

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.EQUALS, new FunctionOperand("currentUser")))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);

        assertEquals(builder, builder.watcherIsCurrentUser());

        verify(clauseBuilder);
    }

    @Test
    public void testWatcherIsEmpty() throws Exception
    {
        assertEmpty("watcher", new TestCallable<JqlClauseBuilder, Void>()
        {
            public JqlClauseBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.watcherIsEmpty();
            }
        });
    }

    @Test
    public void testWatcherBuilder() throws Exception
    {
        assertBuilder("watcher", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.watcher();
            }
        });
    }

    @Test
    public void testFieldBuilder() throws Exception
    {
        final String fieldName = "my field";
        assertBuilder(fieldName, new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.field(fieldName);
            }
        });
    }

    @Test
    public void testCustomField() throws Exception
    {
        final Long id = 50023428L;
        assertBuilder("cf[" + id + "]", new TestCallable<ConditionBuilder, Void>()
        {
            public ConditionBuilder call(final JqlClauseBuilder builder, final Void argument)
            {
                return builder.customField(id);
            }
        });
    }

    private interface TestCallable<V, A>
    {
        V call(JqlClauseBuilder builder, A argument);
    }

    private void assertBuilder(final String jqlName, final TestCallable<ConditionBuilder, Void> callable) throws Exception
    {
        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.EQUALS, 16L))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, callable.call(builder, null).eq().number(16L));

        verify(clauseBuilder);
    }

    private void assertEmpty(final String jqlName, final TestCallable<JqlClauseBuilder, Void> callable) throws Exception
    {
        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.IS, EmptyOperand.EMPTY))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        final JqlClauseBuilder builder = createBuilder(clauseBuilder);
        assertEquals(builder, callable.call(builder, null));

        verify(clauseBuilder);
    }

    private void assertStringSingle(final String jqlName, final TestCallable<JqlClauseBuilder, String> callable) throws Exception
    {
        assertStringSingle(jqlName, callable, Operator.EQUALS);
    }

    private void assertStringSingle(final String jqlName, final TestCallable<JqlClauseBuilder, String> callable, final Operator operator) throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);

        try
        {
            callable.call(builder, null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        final String value = "10";

        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, operator, value))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, callable.call(builder, value));

        verify(clauseBuilder);
    }

    private void assertStringVarArgs(final String jqlName, final TestCallable<JqlClauseBuilder, String[]> callable) throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            callable.call(builder, new String[] {});
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            callable.call(builder, new String[] { "value", null });
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        final String[] values = new String[] { "10", "", "3939" };

        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.IN, new MultiValueOperand(values)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, callable.call(builder, values));

        verify(clauseBuilder);
    }

    private void assertLongSingle(final String jqlName, final TestCallable<JqlClauseBuilder, Long> callable) throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);

        try
        {
            callable.call(builder, null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);
        final Long value = 10L;

        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.EQUALS, value))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, callable.call(builder, value));

        verify(clauseBuilder);
    }

    private void assertLongVarArgs(final String jqlName, final TestCallable<JqlClauseBuilder, Long[]> callable) throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            callable.call(builder, new Long[] {});
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            callable.call(builder, new Long[] { 6L, null });
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final SimpleClauseBuilder clauseBuilder = createMock(SimpleClauseBuilder.class);

        final Long[] values = new Long[] { 1L, 50L, 60L };

        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.IN, new MultiValueOperand(values)))).andReturn(clauseBuilder);
        replay(clauseBuilder);

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, callable.call(builder, values));

        verify(clauseBuilder);
    }

    private void assertDateAfterDate(final String jqlName, final TestCallable<JqlClauseBuilder, Date> callable) throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            callable.call(builder, null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final Date dateStart = new Date(34573284534L);
        final String dateStartString = "%&*##*$&$";

        final IMocksControl control = EasyMock.createControl();

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final JqlDateSupport dateSupport = control.createMock(JqlDateSupport.class);

        expect(dateSupport.getDateString(dateStart)).andReturn(dateStartString).anyTimes();

        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.GREATER_THAN_EQUALS, dateStartString))).andReturn(clauseBuilder);

        replay(timeZoneManager);
        control.replay();

        builder = createBuilder(clauseBuilder, dateSupport);
        assertEquals(builder, callable.call(builder, dateStart));

        control.verify();
    }

    private void assertDateAfterString(final String jqlName, final TestCallable<JqlClauseBuilder, String> callable) throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            callable.call(builder, null);
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final String dateStartString = "%&*##*$&$";

        final IMocksControl control = EasyMock.createControl();

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.GREATER_THAN_EQUALS, dateStartString))).andReturn(clauseBuilder);

        replay(timeZoneManager);
        control.replay();

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, callable.call(builder, dateStartString));

        control.verify();
    }

    private void assertDateRangeDate(final String jqlName, final TestCallable<JqlClauseBuilder, Date[]> callable) throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            callable.call(builder, new Date[] { null, null });
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final Date dateStart = new Date(34573284534L);
        final Date dateEnd = new Date(3452785834573895748L);
        final String dateStartString = "%&*##*$&$";
        final String dateEndString = "dhakjdhakjhsdsa";

        final IMocksControl control = EasyMock.createControl();

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);
        final JqlDateSupport dateSupport = control.createMock(JqlDateSupport.class);

        expect(dateSupport.getDateString(dateStart)).andReturn(dateStartString).anyTimes();
        expect(dateSupport.getDateString(dateEnd)).andReturn(dateEndString).anyTimes();

        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.GREATER_THAN_EQUALS, dateStartString))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.LESS_THAN_EQUALS, dateEndString))).andReturn(clauseBuilder);
        expect(
            clauseBuilder.clause(new AndClause(new TerminalClauseImpl(jqlName, Operator.GREATER_THAN_EQUALS, dateStartString),
                new TerminalClauseImpl(jqlName, Operator.LESS_THAN_EQUALS, dateEndString)))).andReturn(clauseBuilder);

        replay(timeZoneManager);
        control.replay();

        builder = createBuilder(clauseBuilder, dateSupport);
        assertEquals(builder, callable.call(builder, new Date[] { dateStart, null }));
        assertEquals(builder, callable.call(builder, new Date[] { null, dateEnd }));
        assertEquals(builder, callable.call(builder, new Date[] { dateStart, dateEnd }));

        control.verify();
    }

    private void assertDateRangeString(final String jqlName, final TestCallable<JqlClauseBuilder, String[]> callable) throws Exception
    {
        DefaultJqlClauseBuilder builder = new DefaultJqlClauseBuilder(timeZoneManager);
        try
        {
            callable.call(builder, new String[] { null, null });
            fail("Expecting an error to be thrown.");
        }
        catch (final IllegalArgumentException e)
        {
            //expected.
        }

        final String dateStartString = "%&*##*$&$";
        final String dateEndString = "dhakjdhakjhsdsa";

        final IMocksControl control = EasyMock.createControl();

        final SimpleClauseBuilder clauseBuilder = control.createMock(SimpleClauseBuilder.class);

        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.GREATER_THAN_EQUALS, dateStartString))).andReturn(clauseBuilder);
        expect(clauseBuilder.clause(new TerminalClauseImpl(jqlName, Operator.LESS_THAN_EQUALS, dateEndString))).andReturn(clauseBuilder);
        expect(
            clauseBuilder.clause(new AndClause(new TerminalClauseImpl(jqlName, Operator.GREATER_THAN_EQUALS, dateStartString),
                new TerminalClauseImpl(jqlName, Operator.LESS_THAN_EQUALS, dateEndString)))).andReturn(clauseBuilder);

        replay(timeZoneManager);
        control.replay();

        builder = createBuilder(clauseBuilder);
        assertEquals(builder, callable.call(builder, new String[] { dateStartString, null }));
        assertEquals(builder, callable.call(builder, new String[] { null, dateEndString }));
        assertEquals(builder, callable.call(builder, new String[] { dateStartString, dateEndString }));

        control.verify();
    }

    static DefaultJqlClauseBuilder createBuilder(final SimpleClauseBuilder clauseBuilder)
    {
        final JqlDateSupport dateSupport = (JqlDateSupport) DuckTypeProxy.getProxy(JqlDateSupport.class, new Object());
        return createBuilder(clauseBuilder, dateSupport);
    }

    private static DefaultJqlClauseBuilder createBuilder(final SimpleClauseBuilder clauseBuilder, final JqlDateSupport dateSupport)
    {
        return new DefaultJqlClauseBuilder(null, clauseBuilder, dateSupport);
    }
}
