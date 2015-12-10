package com.atlassian.jira.jql.util;

import java.util.List;
import java.util.Set;

import com.atlassian.fugue.Option;
import com.atlassian.query.Query;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.ChangedClause;
import com.atlassian.query.clause.ChangedClauseImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.ClausePrecedence;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.Property;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.clause.WasClauseImpl;
import com.atlassian.query.history.AndHistoryPredicate;
import com.atlassian.query.history.HistoryPredicate;
import com.atlassian.query.history.TerminalHistoryPredicate;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Test for {@link com.atlassian.jira.jql.util.ToJqlStringVisitor}.
 *
 * @since v4.0
 */
public class TestToJqlStringVisitor
{
    @Test
    public void testVisitEmptyOperand() throws Exception
    {
        assertEquals(EmptyOperand.OPERAND_NAME, EmptyOperand.EMPTY.accept(new ToJqlStringVisitor(new MockJqlStringSupport())));
    }

    @Test
    public void testVisitFunctionOperandNoArgs() throws Exception
    {
        FunctionOperand operand = new FunctionOperand("function");
        assertEquals("funcName:function()", operand.accept(new ToJqlStringVisitor(new MockJqlStringSupport())));
    }

    @Test
    public void testVisitFunctionOperandArgs() throws Exception
    {
        FunctionOperand operand = new FunctionOperand("functionName", "arg2", "arg3");
        assertEquals("funcName:functionName(funcArg:arg2, funcArg:arg3)", operand.accept(new ToJqlStringVisitor(new MockJqlStringSupport())));
    }

    @Test
    public void testVisitMultiValueOperand() throws Exception
    {
        MultiValueOperand multi = new MultiValueOperand("value1", "value2");
        assertEquals("(value:value1, value:value2)", multi.accept(new ToJqlStringVisitor(new MockJqlStringSupport())));
    }

    @Test
    public void testVisitSingleValueOperandString() throws Exception
    {
        SingleValueOperand single = new SingleValueOperand("value1");
        assertEquals("value:value1", single.accept(new ToJqlStringVisitor(new MockJqlStringSupport())));
    }

    @Test
    public void testVisitSingleValueOperandNumber() throws Exception
    {
        SingleValueOperand single = new SingleValueOperand(1029L);
        assertEquals("1029", single.accept(new ToJqlStringVisitor(new MockJqlStringSupport())));
    }

    @Test
    public void testVisitTerminalClause() throws Exception
    {
        TerminalClause clause = new TerminalClauseImpl("field", Operator.EQUALS, "value");
        final ToJqlStringVisitor.Result actual = clause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:field = value:value", actual.getJql());
    }

    @Test
    public void testVisitNotWithTermincalClause() throws Exception
    {
        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.EQUALS, "value");
        NotClause notClause = new NotClause(terminalClause);

        final ToJqlStringVisitor.Result actual = notClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));

        assertEquals("NOT fieldName:field = value:value", actual.getJql());
        assertSame(ClausePrecedence.NOT, actual.getPrecedence());
    }

    @Test
    public void testVisitNotAndClause() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl("field", Operator.EQUALS, "value"),
                new TerminalClauseImpl("field2", Operator.EQUALS, "value2"));

        NotClause notClause = new NotClause(andClause);

        final ToJqlStringVisitor.Result actual = notClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));

        assertEquals("NOT (fieldName:field = value:value AND fieldName:field2 = value:value2)", actual.getJql());
        assertSame(ClausePrecedence.NOT, actual.getPrecedence());
    }

    @Test
    public void testVisitAndClause() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl("field", Operator.LIKE, "value"),
                new TerminalClauseImpl("field2", Operator.LESS_THAN, "value2"));

        final ToJqlStringVisitor.Result actual = andClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:field ~ value:value AND fieldName:field2 < value:value2", actual.getJql());
        assertSame(ClausePrecedence.AND, actual.getPrecedence());
    }

    @Test
    public void testVisitAndClauseComplex() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl("field", Operator.LIKE, "value"),
                new OrClause(new TerminalClauseImpl("field2", Operator.LESS_THAN, "value2"),
                        new TerminalClauseImpl("field3", Operator.GREATER_THAN, "value3")));

        final ToJqlStringVisitor.Result actual = andClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:field ~ value:value AND (fieldName:field2 < value:value2 OR fieldName:field3 > value:value3)", actual.getJql());
        assertSame(ClausePrecedence.AND, actual.getPrecedence());
    }

    @Test
    public void testVisitOrClause() throws Exception
    {
        OrClause orClause = new OrClause(new TerminalClauseImpl("field", Operator.NOT_IN, "value"),
                new TerminalClauseImpl("field2", Operator.IS, EmptyOperand.EMPTY));

        final ToJqlStringVisitor.Result actual = orClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:field not in value:value OR fieldName:field2 is EMPTY", actual.getJql());
        assertSame(ClausePrecedence.OR, actual.getPrecedence());
    }

    @Test
    public void testVisitOrClauseComplex() throws Exception
    {
        OrClause orClause = new OrClause(new TerminalClauseImpl("field", Operator.NOT_IN, "value"),
                new AndClause(new TerminalClauseImpl("field2", Operator.IS, EmptyOperand.EMPTY),
                        new TerminalClauseImpl("field3", Operator.IS_NOT, EmptyOperand.EMPTY)));

        final ToJqlStringVisitor.Result actual = orClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:field not in value:value OR fieldName:field2 is EMPTY AND fieldName:field3 is not EMPTY", actual.getJql());
        assertSame(ClausePrecedence.OR, actual.getPrecedence());
    }

    @Test
    public void testVisitWasClause() throws Exception
    {
        WasClause wasClause = new WasClauseImpl("field", Operator.WAS, new SingleValueOperand("value"), null);
        final ToJqlStringVisitor.Result actual = wasClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:field was value:value", actual.getJql());
        assertSame(ClausePrecedence.TERMINAL, actual.getPrecedence());
    }

    @Test
    public void testVisitWasNotClause() throws Exception
    {
        WasClause wasClause = new WasClauseImpl("field", Operator.WAS_NOT, new SingleValueOperand("value"), null);
        final ToJqlStringVisitor.Result actual = wasClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:field was not value:value", actual.getJql());
        assertSame(ClausePrecedence.TERMINAL, actual.getPrecedence());
    }

    @Test
    public void testVisitWasClauseWithMultiplePredicates() throws Exception
    {
        WasClause wasClause = new WasClauseImpl("field", Operator.WAS, new SingleValueOperand("value"), getHistoryPredicate());
        final ToJqlStringVisitor.Result actual = wasClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:field was value:value by value:user after value:today ", actual.getJql());
        assertSame(ClausePrecedence.TERMINAL, actual.getPrecedence());
    }

    @Test
    public void testVisitChangedClause() throws Exception
    {
        ChangedClause changedClause = new ChangedClauseImpl("field", Operator.CHANGED, null);
        final ToJqlStringVisitor.Result actual = changedClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:field changed", actual.getJql());
        assertSame(ClausePrecedence.TERMINAL, actual.getPrecedence());
    }

    @Test
    public void testVisitChangedClauseWithMultiplePredicates() throws Exception
    {
        ChangedClause changedClause = new ChangedClauseImpl("field", Operator.CHANGED, getHistoryPredicate());
        final ToJqlStringVisitor.Result actual = changedClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:field changed by value:user after value:today ", actual.getJql());
        assertSame(ClausePrecedence.TERMINAL, actual.getPrecedence());
    }

    @Test
    public void testVisitClauseWithPropertyWithEmptyObjectReference()
    {
        final Property property = new Property(Lists.newArrayList("empty"), Lists.<String>newArrayList());
        final TerminalClauseImpl terminalClause = new TerminalClauseImpl("issue.property", Operator.EQUALS, new SingleValueOperand("abcd"), Option.some(property));

        final ToJqlStringVisitor.Result result = terminalClause.accept(new ToJqlStringVisitor(new MockJqlStringSupport()));
        assertEquals("fieldName:issue.property[fieldName:empty] = value:abcd", result.getJql());
    }

    private HistoryPredicate getHistoryPredicate()
    {
        final List<HistoryPredicate> predicates = Lists.newArrayList();
        predicates.add(new TerminalHistoryPredicate(Operator.BY, new SingleValueOperand("user")));
        predicates.add(new TerminalHistoryPredicate(Operator.AFTER, new SingleValueOperand("today")));

        return new AndHistoryPredicate(predicates);
    }

    private static class MockJqlStringSupport implements JqlStringSupport
    {
        public String encodeStringValue(final String value)
        {
            return "value:" + value;
        }

        public String encodeValue(final String value)
        {
            return "value:" + value;
        }

        public String encodeFunctionArgument(final String argument)
        {
            return "funcArg:" + argument;
        }

        public String encodeFunctionName(final String functionName)
        {
            return "funcName:" + functionName;
        }

        public String encodeFieldName(final String fieldName)
        {
            return "fieldName:" + fieldName;
        }

        public String generateJqlString(final Query query)
        {
            throw new UnsupportedOperationException();
        }

        public String generateJqlString(final Clause clause)
        {
            throw new UnsupportedOperationException();
        }

        public Set<String> getJqlReservedWords()
        {
            throw new UnsupportedOperationException();
        }
    }
}
