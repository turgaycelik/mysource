package com.atlassian.jira.jql.permission;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @since v4.0
 */
public class TestAbstractLiteralSanitisingVisitor
{
    private ApplicationUser appUser = new MockApplicationUser("Fred");
    private User user = appUser.getDirectoryUser();

    @After
    public void tearDown() throws Exception
    {
        appUser = null;
        user = null;
    }

    @Test
    public void testVisitNoValues() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("HSP");
        final TerminalClause clause = new TerminalClauseImpl("bah", Operator.EQUALS, operand);

        final JqlOperandResolver operandResolver = mock(JqlOperandResolver.class);

        final AbstractLiteralSanitisingVisitor visitor = new AbstractLiteralSanitisingVisitor(operandResolver, user, clause)
        {
            @Override
            protected LiteralSanitiser createLiteralSanitiser()
            {
                return new MockLiteralSanitiser(new LiteralSanitiser.Result(false, Collections.<QueryLiteral>emptyList()));
            }
        };

        assertSame(operand, visitor.visit(operand));

        verify(operandResolver).getValues(user, operand, clause);
    }

    @Test
    public void testNoModification() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("HSP");
        TerminalClause clause = new TerminalClauseImpl("bah", Operator.EQUALS, operand);

        AbstractLiteralSanitisingVisitor visitor = createVisitor(false, null, clause, createLiteral("HSP"));

        assertSame(operand, visitor.visit(operand));
    }

    @Test
    public void testModificationWithOneResultantLiteral() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final SingleValueOperand expectedOperand = new SingleValueOperand("NEW HSP");
        TerminalClause clause = new TerminalClauseImpl("bah", Operator.EQUALS, inputOperand);

        AbstractLiteralSanitisingVisitor visitor = createVisitor(true, Collections.singletonList(createLiteral("NEW HSP")), clause, createLiteral("HSP"));

        assertEquals(expectedOperand, visitor.visit(inputOperand));
    }

    @Test
    public void testModificationWithTwoResultantLiterals() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final MultiValueOperand expectedOperand = new MultiValueOperand("NEW", "HSP");
        TerminalClause clause = new TerminalClauseImpl("bah", Operator.EQUALS, inputOperand);

        AbstractLiteralSanitisingVisitor visitor = createVisitor(true, CollectionBuilder.newBuilder(createLiteral("NEW"), createLiteral("HSP")).asList(), clause, createLiteral("HSP"));

        assertEquals(expectedOperand, visitor.visit(inputOperand));
    }

    private AbstractLiteralSanitisingVisitor createVisitor(final boolean isModified, final List<QueryLiteral> literals, final TerminalClause terminalClause, final QueryLiteral... expectedLiterals)
    {
        final JqlOperandResolver operandResolver = MockJqlOperandResolver.createSimpleSupport();

        final AbstractLiteralSanitisingVisitor visitor = new AbstractLiteralSanitisingVisitor(operandResolver, user, terminalClause)
        {
            @Override
            protected LiteralSanitiser createLiteralSanitiser()
            {
                return new MockLiteralSanitiser(new LiteralSanitiser.Result(isModified, literals), expectedLiterals);
            }
        };

        return visitor;
    }

}
