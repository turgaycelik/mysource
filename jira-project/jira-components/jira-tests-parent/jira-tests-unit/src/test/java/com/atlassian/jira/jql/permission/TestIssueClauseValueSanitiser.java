package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @since v4.0
 */
public class TestIssueClauseValueSanitiser extends MockControllerTestCase
{
    private User theUser;
    private PermissionManager permissionManager;
    private JqlOperandResolver jqlOperandResolver;
    private JqlIssueSupport jqlIssueSupport;
    private String fieldName = "project";
    private String issueKey = "HSP";

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
        permissionManager = mockController.getMock(PermissionManager.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlIssueSupport = mockController.getMock(JqlIssueSupport.class);
    }

    @Test
    public void testSanitiseOperandDoesNotChange() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClause clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, inputOperand);

        final IssueClauseValueSanitiser.IssueOperandSanitisingVisitor visitor = new IssueClauseValueSanitiser.IssueOperandSanitisingVisitor(jqlOperandResolver, permissionManager, theUser, clause, jqlIssueSupport)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return singleValueOperand;
            }
        };

        final IssueClauseValueSanitiser sanitiser = new IssueClauseValueSanitiser(permissionManager, jqlOperandResolver, jqlIssueSupport)
        {
            @Override
            IssueOperandSanitisingVisitor createOperandVisitor(final User user, final TerminalClause terminalClause)
            {
                return visitor;
            }
        };

        replay();

        final Clause result = sanitiser.sanitise(theUser, clause);
        assertSame(result, clause);

        verify();
    }

    @Test
    public void testSanitiseOperandChangesToMultiEquals() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl inputClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, inputOperand);
        final MultiValueOperand outputOperand = new MultiValueOperand(123L, 456L);
        final TerminalClauseImpl outputClause = new TerminalClauseImpl(fieldName, Operator.IN, outputOperand);

        _testSanitiseFromSingleToMulti(inputOperand, inputClause, outputOperand, outputClause);
    }

    @Test
    public void testSanitiseOperandChangesToMultiNotEquals() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl inputClause = new TerminalClauseImpl(fieldName, Operator.NOT_EQUALS, inputOperand);
        final MultiValueOperand outputOperand = new MultiValueOperand(123L, 456L);
        final TerminalClauseImpl outputClause = new TerminalClauseImpl(fieldName, Operator.NOT_IN, outputOperand);

        _testSanitiseFromSingleToMulti(inputOperand, inputClause, outputOperand, outputClause);
    }

    @Test
    public void testSanitiseOperandChangesToMultiIs() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl inputClause = new TerminalClauseImpl(fieldName, Operator.IS, inputOperand);
        final MultiValueOperand outputOperand = new MultiValueOperand(123L, 456L);
        final TerminalClauseImpl outputClause = new TerminalClauseImpl(fieldName, Operator.IN, outputOperand);

        _testSanitiseFromSingleToMulti(inputOperand, inputClause, outputOperand, outputClause);
    }

    @Test
    public void testSanitiseOperandChangesToMultiIsNot() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl inputClause = new TerminalClauseImpl(fieldName, Operator.IS_NOT, inputOperand);
        final MultiValueOperand outputOperand = new MultiValueOperand(123L, 456L);
        final TerminalClauseImpl outputClause = new TerminalClauseImpl(fieldName, Operator.NOT_IN, outputOperand);

        _testSanitiseFromSingleToMulti(inputOperand, inputClause, outputOperand, outputClause);
    }

    @Test
    public void testSanitiseOperandChangesToMultiLike() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl inputClause = new TerminalClauseImpl(fieldName, Operator.LIKE, inputOperand);
        final MultiValueOperand outputOperand = new MultiValueOperand(123L, 456L);
        final TerminalClauseImpl outputClause = inputClause;

        _testSanitiseFromSingleToMulti(inputOperand, inputClause, outputOperand, outputClause);
    }

    @Test
    public void testSanitiseOperandChangesToMultiNotLike() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl inputClause = new TerminalClauseImpl(fieldName, Operator.NOT_LIKE, inputOperand);
        final MultiValueOperand outputOperand = new MultiValueOperand(123L, 456L);
        final TerminalClauseImpl outputClause = inputClause;

        _testSanitiseFromSingleToMulti(inputOperand, inputClause, outputOperand, outputClause);
    }

    @Test
    public void testSanitiseOperandChangesToMultiLessThan() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl inputClause = new TerminalClauseImpl(fieldName, Operator.LESS_THAN, inputOperand);
        final MultiValueOperand outputOperand = new MultiValueOperand(123L, 456L);
        final TerminalClauseImpl outputClause1 = new TerminalClauseImpl(fieldName, Operator.LESS_THAN, 123L);
        final TerminalClauseImpl outputClause2 = new TerminalClauseImpl(fieldName, Operator.LESS_THAN, 456L);
        final OrClause outputClause = new OrClause(outputClause1, outputClause2);

        _testSanitiseFromSingleToMulti(inputOperand, inputClause, outputOperand, outputClause);
    }

    @Test
    public void testSanitiseOperandChangesToMultiLessThanEquals() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl inputClause = new TerminalClauseImpl(fieldName, Operator.LESS_THAN_EQUALS, inputOperand);
        final MultiValueOperand outputOperand = new MultiValueOperand(123L, 456L);
        final TerminalClauseImpl outputClause1 = new TerminalClauseImpl(fieldName, Operator.LESS_THAN_EQUALS, 123L);
        final TerminalClauseImpl outputClause2 = new TerminalClauseImpl(fieldName, Operator.LESS_THAN_EQUALS, 456L);
        final OrClause outputClause = new OrClause(outputClause1, outputClause2);

        _testSanitiseFromSingleToMulti(inputOperand, inputClause, outputOperand, outputClause);
    }

    @Test
    public void testSanitiseOperandChangesToMultiGreaterThanEquals() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl inputClause = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN_EQUALS, inputOperand);
        final MultiValueOperand outputOperand = new MultiValueOperand(123L, 456L);
        final TerminalClauseImpl outputClause1 = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN_EQUALS, 123L);
        final TerminalClauseImpl outputClause2 = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN_EQUALS, 456L);
        final OrClause outputClause = new OrClause(outputClause1, outputClause2);

        _testSanitiseFromSingleToMulti(inputOperand, inputClause, outputOperand, outputClause);
    }

    @Test
    public void testSanitiseOperandChangesToMultiGreaterThan() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand(issueKey);
        final TerminalClauseImpl inputClause = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN, inputOperand);
        final MultiValueOperand outputOperand = new MultiValueOperand(123L, 456L);
        final TerminalClauseImpl outputClause1 = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN, 123L);
        final TerminalClauseImpl outputClause2 = new TerminalClauseImpl(fieldName, Operator.GREATER_THAN, 456L);
        final OrClause outputClause = new OrClause(outputClause1, outputClause2);

        _testSanitiseFromSingleToMulti(inputOperand, inputClause, outputOperand, outputClause);
    }

    private void _testSanitiseFromSingleToMulti(final SingleValueOperand inputOperand, final TerminalClause inputClause, final MultiValueOperand outputOperand, final Clause outputClause)
    {
        final IssueClauseValueSanitiser.IssueOperandSanitisingVisitor visitor = new IssueClauseValueSanitiser.IssueOperandSanitisingVisitor(jqlOperandResolver, permissionManager, theUser, inputClause, jqlIssueSupport)
        {
            @Override
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(inputOperand, singleValueOperand);
                return outputOperand;
            }
        };

        final IssueClauseValueSanitiser sanitiser = new IssueClauseValueSanitiser(permissionManager, jqlOperandResolver, jqlIssueSupport)
        {
            @Override
            IssueOperandSanitisingVisitor createOperandVisitor(final User user, final TerminalClause terminalClause)
            {
                return visitor;
            }
        };

        replay();

        final Clause result = sanitiser.sanitise(theUser, inputClause);
        assertEquals(result, outputClause);

        verify();
    }

}
