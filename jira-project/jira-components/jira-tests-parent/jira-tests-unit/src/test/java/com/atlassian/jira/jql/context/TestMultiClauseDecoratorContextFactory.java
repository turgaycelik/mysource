package com.atlassian.jira.jql.context;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.validator.AlwaysValidOperatorUsageValidator;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory}.
 *
 * @since v4.0
 */
public class TestMultiClauseDecoratorContextFactory extends MockControllerTestCase
{
    @Test
    public void testGetClauseContextNoLiterals() throws Exception
    {
        final TerminalClause terminal = new TerminalClauseImpl("one", Operator.IN, EmptyOperand.EMPTY);

        final JqlOperandResolver resolver = getMock(JqlOperandResolver.class);
        expect(resolver.getValues((User) null, terminal.getOperand(), terminal)).andReturn(null);

        final MultiClauseDecoratorContextFactory factory = instantiate(MultiClauseDecoratorContextFactory.class);

        final ClauseContext clauseContext = factory.getClauseContext(null, terminal);
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), clauseContext);

        verify();
    }

    @Test
    public void testGetClauseContextNotList() throws Exception
    {
        final ClauseContext expectedResult = createIssueTypeContext(1, 2, 56);
        final SingleValueOperand operand = new SingleValueOperand("one");
        final TerminalClause terminal = new TerminalClauseImpl("one", Operator.NOT_IN, operand);

        final JqlOperandResolver jqlOperandResolver = getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues((User) null, operand, terminal))
                .andReturn(Collections.singletonList(createLiteral(1L)));
        expect(jqlOperandResolver.isListOperand(operand)).andReturn(false);

        final ClauseContextFactory delegate = getMock(ClauseContextFactory.class);
        expect(delegate.getClauseContext(null, terminal)).andReturn(expectedResult);

        final MultiClauseDecoratorContextFactory factory = instantiate(MultiClauseDecoratorContextFactory.class);

        final ClauseContext clauseContext = factory.getClauseContext(null, terminal);
        assertEquals(expectedResult, clauseContext);

        verify();
    }

    @Test
    public void testGetClauseContextBadOperator() throws Exception
    {
        final ClauseContext expectedResult = createIssueTypeContext(1, 2, 56);

        final TerminalClause terminal = new TerminalClauseImpl("one", Operator.LIKE, new MultiValueOperand("one"));

        final ClauseContextFactory delegate = getMock(ClauseContextFactory.class);
        expect(delegate.getClauseContext(null, terminal)).andReturn(expectedResult);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final MultiClauseDecoratorContextFactory factory = instantiate(MultiClauseDecoratorContextFactory.class);

        final ClauseContext clauseContext = factory.getClauseContext(null, terminal);
        assertEquals(expectedResult, clauseContext);

        verify();
    }

    @Test
    public void testGetClauseContextIn() throws Exception
    {
        final ClauseContext clauseCtx1 = createIssueTypeContext(1, 2, 56);
        final ClauseContext expectedResult = createIssueTypeContext(573832222);
        final Set<ClauseContext> unionedCtxs =
                CollectionBuilder.<ClauseContext>newBuilder(clauseCtx1).asSet();

        final TerminalClause terminal = new TerminalClauseImpl("one", Operator.IN, new MultiValueOperand("one", "two"));

        final ClauseContextFactory delegate = getMock(ClauseContextFactory.class);
        expect(delegate.getClauseContext(null,
                new TerminalClauseImpl("one", Operator.EQUALS, new MultiValueOperand("one", "two")))).andReturn(
                clauseCtx1);

        final ContextSetUtil contextSetUtil = getMock(ContextSetUtil.class);
        expect(contextSetUtil.union(unionedCtxs)).andReturn(expectedResult);

        mockController.addObjectInstance(new AlwaysValidOperatorUsageValidator());
        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final MultiClauseDecoratorContextFactory factory = instantiate(MultiClauseDecoratorContextFactory.class);

        final ClauseContext clauseContext = factory.getClauseContext(null, terminal);
        assertEquals(expectedResult, clauseContext);

        verify();
    }

    @Test
    public void testGetClauseContextNotIn() throws Exception
    {
        final ClauseContext clauseCtx1 = createIssueTypeContext(4835490, 35354, 54353);
        final ClauseContext expectedResult = createIssueTypeContext(22);
        final Set<ClauseContext> unionedCtxs =
                CollectionBuilder.<ClauseContext>newBuilder(clauseCtx1).asSet();

        final TerminalClause terminal = new TerminalClauseImpl("one", Operator.NOT_IN,
                new MultiValueOperand(
                        new SingleValueOperand("one"),
                        new SingleValueOperand("two"),
                        EmptyOperand.EMPTY));

        final ClauseContextFactory delegate = getMock(ClauseContextFactory.class);
        expect(delegate.getClauseContext(null,
                new TerminalClauseImpl("one", Operator.NOT_EQUALS,
                        new MultiValueOperand(new SingleValueOperand("one"), new SingleValueOperand("two"),
                                EmptyOperand.EMPTY)))).andReturn(
                clauseCtx1);

        final ContextSetUtil contextSetUtil = getMock(ContextSetUtil.class);
        expect(contextSetUtil.intersect(unionedCtxs)).andReturn(expectedResult);

        mockController.addObjectInstance(new AlwaysValidOperatorUsageValidator());
        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final MultiClauseDecoratorContextFactory factory = instantiate(MultiClauseDecoratorContextFactory.class);

        final ClauseContext clauseContext = factory.getClauseContext(null, terminal);
        assertEquals(expectedResult, clauseContext);

        verify();
    }

    @Test
    public void testFactoryNoValidate() throws Exception
    {
        final ClauseContextFactory contextFactory = getMock(ClauseContextFactory.class);

        MultiClauseDecoratorContextFactory.Factory factory =
                instantiate(MultiClauseDecoratorContextFactory.Factory.class);
        final ClauseContextFactory clauseContextFactory = factory.create(contextFactory, false);
        assertTrue(clauseContextFactory instanceof MultiClauseDecoratorContextFactory);

        verify();
    }

    @Test
    public void testFactoryValidate() throws Exception
    {
        final ClauseContextFactory contextFactory = getMock(ClauseContextFactory.class);

        MultiClauseDecoratorContextFactory.Factory factory =
                instantiate(MultiClauseDecoratorContextFactory.Factory.class);
        final ClauseContextFactory clauseContextFactory = factory.create(contextFactory, true);
        assertTrue(clauseContextFactory instanceof ValidatingDecoratorContextFactory);

        verify();
    }

    @Test
    public void testFactoryDefault() throws Exception
    {
        final ClauseContextFactory contextFactory = getMock(ClauseContextFactory.class);

        MultiClauseDecoratorContextFactory.Factory factory =
                instantiate(MultiClauseDecoratorContextFactory.Factory.class);
        final ClauseContextFactory clauseContextFactory = factory.create(contextFactory);
        assertTrue(clauseContextFactory instanceof ValidatingDecoratorContextFactory);

        verify();
    }

    private static ClauseContext createIssueTypeContext(int... types)
    {
        Set<ProjectIssueTypeContext> ctxs = new HashSet<ProjectIssueTypeContext>();
        for (int type : types)
        {
            ctxs.add(new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE,
                    new IssueTypeContextImpl(String.valueOf(type))));
        }

        return new ClauseContextImpl(ctxs);
    }
}
