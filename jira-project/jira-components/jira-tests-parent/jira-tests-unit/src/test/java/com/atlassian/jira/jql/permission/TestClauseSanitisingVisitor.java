package com.atlassian.jira.jql.permission;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.MockClauseHandler;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link com.atlassian.jira.jql.permission.ClauseSanitisingVisitor}.
 *
 * @since v4.0
 */
@SuppressWarnings("ResultOfObjectAllocationIgnored")  // Constructor tests
@RunWith(MockitoJUnitRunner.class)
public class TestClauseSanitisingVisitor
{
    private static final User ANONYMOUS = null;
    private User fred = new MockUser("fred");

    @Mock SearchHandlerManager searchHandlerManager;
    @Mock JqlOperandResolver jqlOperandResolver;

    @After
    public void tearDown()
    {
        fred = null;
        searchHandlerManager = null;
        jqlOperandResolver = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullSearchHandlerManager() throws Exception
    {
        new ClauseSanitisingVisitor(null, jqlOperandResolver, ANONYMOUS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullJqlOperandResolver() throws Exception
    {
        new ClauseSanitisingVisitor(searchHandlerManager, null, ANONYMOUS);
    }

    @Test
    public void testAndClause() throws Exception
    {
        final Clause expectedClause = new AndClause(new TerminalClauseImpl("field", Operator.EQUALS, "Value"));
        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, ANONYMOUS)
        {
            @Override
            List<Clause> sanitiseChildren(final Clause parentClause)
            {
                assertEquals(expectedClause, parentClause);
                return Collections.singletonList(parentClause);
            }
        };

        final Clause sanitisedClause = expectedClause.accept(visitor);
        assertEquals(new AndClause(expectedClause), sanitisedClause);
    }

    @Test
    public void testOrClause() throws Exception
    {
        final Clause expectedClause = new OrClause(new TerminalClauseImpl("field", Operator.EQUALS, "Value"));
        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, ANONYMOUS)
        {
            @Override
            List<Clause> sanitiseChildren(final Clause parentClause)
            {
                assertEquals(expectedClause, parentClause);
                return Collections.singletonList(parentClause);
            }
        };

        final Clause sanitisedClause = expectedClause.accept(visitor);
        assertEquals(new OrClause(expectedClause), sanitisedClause);
    }

    @Test
    public void testNotClause() throws Exception
    {
        final Clause notChildClause = mock(Clause.class);

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, ANONYMOUS);
        when(notChildClause.accept(visitor)).thenReturn(notChildClause);

        NotClause notClause = new NotClause(notChildClause);
        final Clause sanitisedClause = notClause.accept(visitor);
        assertEquals(notClause, sanitisedClause);
    }

    @Test
    public void testSanitiseChildren() throws Exception
    {
        final Clause childClause = mock(Clause.class);

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, ANONYMOUS);
        when(childClause.accept(visitor)).thenReturn(childClause);

        final AndClause andClause = new AndClause(childClause);
        final List<Clause> sanitisedChildren = visitor.sanitiseChildren(andClause);
        assertThat(sanitisedChildren, contains(childClause));
    }

    @Test
    public void testSanitiseOperandDoesNotChange() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final TerminalClause clause = new TerminalClauseImpl("project", Operator.EQUALS, inputOperand);

        final DefaultOperandSanitisingVisitor operandVisitor = new DefaultOperandSanitisingVisitor(jqlOperandResolver, fred)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return singleValueOperand;
            }
        };

        final ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, fred)
        {
            @Override
            DefaultOperandSanitisingVisitor createOperandVisitor(final User user)
            {
                return operandVisitor;
            }
        };

        final Clause result = visitor.sanitiseOperands(clause);
        assertSame(result, clause);
    }

    @Test
    public void testSanitiseOperandChangesToSingle() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final SingleValueOperand outputOperand = new SingleValueOperand(10000L);

        final TerminalClause clause = new TerminalClauseImpl("project", Operator.EQUALS, inputOperand);
        final TerminalClause expectedClause = new TerminalClauseImpl("project", Operator.EQUALS, outputOperand);

        final DefaultOperandSanitisingVisitor operandVisitor = new DefaultOperandSanitisingVisitor(jqlOperandResolver, fred)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return outputOperand;
            }
        };

        final ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, fred)
        {
            @Override
            DefaultOperandSanitisingVisitor createOperandVisitor(final User user)
            {
                return operandVisitor;
            }
        };

        final Clause result = visitor.sanitiseOperands(clause);
        assertNotSame(result, clause);
        assertEquals(result, expectedClause);
    }

    @Test
    public void testSanitiseOperandChangesToMulti() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final SingleValueOperand outputOperand = new SingleValueOperand(10000L);

        final TerminalClause clause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(inputOperand));
        final TerminalClause expectedClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(outputOperand));

        final DefaultOperandSanitisingVisitor operandVisitor = new DefaultOperandSanitisingVisitor(jqlOperandResolver, fred)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return outputOperand;
            }
        };

        final ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, fred)
        {
            @Override
            DefaultOperandSanitisingVisitor createOperandVisitor(final User user)
            {
                return operandVisitor;
            }
        };

        final Clause result = visitor.sanitiseOperands(clause);
        assertNotSame(result, clause);
        assertEquals(result, expectedClause);
    }

    @Test
    public void testTerminalClauseNoSearchHandlers() throws Exception
    {
        final TerminalClause input = new TerminalClauseImpl("field", Operator.EQUALS, "Value");
        when(searchHandlerManager.getClauseHandler(ANONYMOUS, "field")).thenReturn(Collections.<ClauseHandler>emptyList());

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, ANONYMOUS)
        {
            @Override
            TerminalClause sanitiseOperands(final TerminalClause clause)
            {
                return clause;
            }
        };

        final Clause clause = input.accept(visitor);
        assertSame(clause, input);
    }

    @Test
    public void testTerminalClauseOneSearchHandler() throws Exception
    {
        final TerminalClause input = new TerminalClauseImpl("field", Operator.EQUALS, "Value");

        final ClausePermissionHandler permissionHandler = mock(ClausePermissionHandler.class);
        when(permissionHandler.sanitise(null, input)).thenReturn(input);

        final ClauseHandler handler = new MockClauseHandler(null, null, permissionHandler, null);
        when(searchHandlerManager.getClauseHandler(ANONYMOUS, "field")).thenReturn(Collections.singletonList(handler));

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, ANONYMOUS)
        {
            @Override
            TerminalClause sanitiseOperands(final TerminalClause clause)
            {
                return clause;
            }
        };

        final Clause clause = input.accept(visitor);
        assertSame(clause, input);
    }

    @Test
    public void testTerminalClauseTwoSearchHandlersSame() throws Exception
    {
        final TerminalClause input1 = new TerminalClauseImpl("field", Operator.EQUALS, "Value");
        final TerminalClause input2 = new TerminalClauseImpl("field", Operator.EQUALS, "Value");

        final ClausePermissionHandler permissionHandler = mock(ClausePermissionHandler.class);
        when(permissionHandler.sanitise(null, input1)).thenReturn(input1);
        when(permissionHandler.sanitise(null, input1)).thenReturn(input2);

        final ClauseHandler handler1 = new MockClauseHandler(null, null, permissionHandler, null);
        final ClauseHandler handler2 = new MockClauseHandler(null, null, permissionHandler, null);

        when(searchHandlerManager.getClauseHandler(ANONYMOUS, "field")).thenReturn(CollectionBuilder.newBuilder(handler1, handler2).asList());

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, ANONYMOUS)
        {
            @Override
            TerminalClause sanitiseOperands(final TerminalClause clause)
            {
                return clause;
            }
        };

        final Clause clause = input1.accept(visitor);
        assertEquals(input1, clause);
    }

    @Test
    public void testTerminalClauseTwoSearchHandlersDifferent() throws Exception
    {
        final TerminalClause input1 = new TerminalClauseImpl("field", Operator.EQUALS, "Value1");
        final TerminalClause input2 = new TerminalClauseImpl("field", Operator.EQUALS, "Value2");

        final ClausePermissionHandler permissionHandler = mock(ClausePermissionHandler.class);
        when(permissionHandler.sanitise(null, input1)).thenReturn(input1, input2);

        final ClauseHandler handler1 = new MockClauseHandler(null, null, permissionHandler, null);
        final ClauseHandler handler2 = new MockClauseHandler(null, null, permissionHandler, null);

        when(searchHandlerManager.getClauseHandler(ANONYMOUS, "field")).thenReturn(CollectionBuilder.newBuilder(handler1, handler2).asList());

        ClauseSanitisingVisitor visitor = new ClauseSanitisingVisitor(searchHandlerManager, jqlOperandResolver, ANONYMOUS)
        {
            @Override
            TerminalClause sanitiseOperands(final TerminalClause clause)
            {
                return clause;
            }
        };

        final Clause expected = new OrClause(input1, input2);
        final Clause clause = input1.accept(visitor);
        assertEquals(expected, clause);
    }

}
