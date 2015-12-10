package com.atlassian.jira.jql.context;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.jira.jql.validator.AlwaysValidOperatorUsageValidator;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.EasyMock;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestIssueTypeClauseContextFactory extends MockControllerTestCase
{
    @Test
    public void testGetClauseContextSingleIssueTypeEquals() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("it1");
        final TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, operand);
        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();

        mockController.replay();

        final ClauseContext result = factory.getClauseContext(null, clause);
        ClauseContext expectedResult = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it1"))
            ).asSet());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextMultiIssueTypeEquals() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand("it2", "it1");
        TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, operand);

        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();
        mockController.replay();

        final ClauseContext result = factory.getClauseContext(null, clause);
        ClauseContext expectedResult = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it1")),
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it2"))
            ).asSet());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextMultiIssueTypeWithEmptyEquals() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand("it1", "it2");
        TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, operand);

        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();
        mockController.replay();

        final ClauseContext result = factory.getClauseContext(null, clause);
        ClauseContext expectedResult = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it1")),
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it2"))
            ).asSet());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextSingleIssueTypeNotEquals() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("it1");
        TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.NOT_EQUALS, operand);

        ConstantsManager mgr = mockController.getMock(ConstantsManager.class);
        EasyMock.expect(mgr.getAllIssueTypeIds()).andReturn(CollectionBuilder.newBuilder("it1", "it2").asList());
        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();

        mockController.replay();

        final ClauseContext result = factory.getClauseContext(null, clause);
        ClauseContext expectedResult = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it2"))
            ).asSet());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextMultiIssueTypeNotEquals() throws Exception
    {
        final Operand operand = new MultiValueOperand("it1");
        TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.NOT_EQUALS, operand);

        ConstantsManager mgr = mockController.getMock(ConstantsManager.class);
        EasyMock.expect(mgr.getAllIssueTypeIds()).andReturn(CollectionBuilder.newBuilder("it1", "it2", "it4").asList());

        IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();

        mockController.replay();

        final ClauseContext result = factory.getClauseContext(null, clause);
        ClauseContext expectedResult = new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it2")),
                new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it4"))
            ).asSet());

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextForNullLiterals() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.IS, operand);

        mockController.addObjectInstance(new AlwaysValidOperatorUsageValidator());
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        expect(jqlOperandResolver.getValues((User)null, operand, clause)).andReturn(null);

        final IssueTypeClauseContextFactory factory = mockController.instantiate(IssueTypeClauseContextFactory.class);

        final ClauseContext result = factory.getClauseContext(null, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextForEmpty() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.IS, operand);

        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();

        mockController.replay();

        final ClauseContext result = factory.getClauseContext(null, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextForNotEmpty() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.IS_NOT, EmptyOperand.EMPTY);
        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();

        mockController.replay();

        final ClauseContext result = factory.getClauseContext(null, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextForNotEmptyOnlyInList() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.IS_NOT, new MultiValueOperand(EmptyOperand.EMPTY));
        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();

        mockController.replay();

        final ClauseContext result = factory.getClauseContext(null, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextForEmptyInList() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.IN, new MultiValueOperand(EmptyOperand.EMPTY, new SingleValueOperand("it2")));
        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();

        mockController.replay();

        final ClauseContext actualResult = factory.getClauseContext(null, clause);
        final ClauseContext expectedResult = new ClauseContextImpl(
                Collections.<ProjectIssueTypeContext>singleton(
                        new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it2")
                )));
        
        assertEquals(expectedResult, actualResult);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextForNotEmptyInList() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.NOT_IN, new MultiValueOperand(EmptyOperand.EMPTY, new SingleValueOperand("it2")));
        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();

        final ConstantsManager mgr = mockController.getMock(ConstantsManager.class);
        EasyMock.expect(mgr.getAllIssueTypeIds()).andReturn(CollectionBuilder.newBuilder("it1", "it2", "it4").asList());

        mockController.replay();

        final ClauseContext actualResult = factory.getClauseContext(null, clause);
        final ClauseContext expectedResult = new ClauseContextImpl(
                CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                        new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it1")),
                        new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, new IssueTypeContextImpl("it4"))
                ).asSet());

        assertEquals(expectedResult, actualResult);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextForNotInAll() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.NOT_IN, new MultiValueOperand("it1", "it2", "it4"));
        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();

        final ConstantsManager mgr = mockController.getMock(ConstantsManager.class);
        EasyMock.expect(mgr.getAllIssueTypeIds()).andReturn(CollectionBuilder.newBuilder("it1", "it2", "it4").asList());

        mockController.replay();

        final ClauseContext actualResult = factory.getClauseContext(null, clause);
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, actualResult);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextBadOperator() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        TerminalClause clause = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.LESS_THAN, operand);

        final IssueTypeClauseContextFactory factory = createNoopIssueTypeClauseContextFactory();

        mockController.replay();

        final ClauseContext result = factory.getClauseContext(null, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetIdStringValue() throws Exception
    {
        final List<String> ids = CollectionBuilder.newBuilder("name").asList();
        IssueTypeResolver resolver = getMock(IssueTypeResolver.class);
        expect(resolver.getIdsFromName("name")).andReturn(ids);

        IssueTypeClauseContextFactory factory = mockController.instantiate(IssueTypeClauseContextFactory.class);

        QueryLiteral literal = createLiteral("name");
                
        assertEquals(ids, factory.getIds(literal));

        mockController.verify();

    }

    @Test
    public void testGetIdLongValue() throws Exception
    {
        final List<String> ids = CollectionBuilder.newBuilder("10").asList();
        IssueTypeResolver resolver = getMock(IssueTypeResolver.class);
        expect(resolver.idExists(10L)).andReturn(true);

        final IssueTypeClauseContextFactory factory = mockController.instantiate(IssueTypeClauseContextFactory.class);

        QueryLiteral literal = createLiteral(10L);

        assertEquals(ids, factory.getIds(literal));

        mockController.verify();
    }

    private IssueTypeClauseContextFactory createNoopIssueTypeClauseContextFactory()
    {
        return new IssueTypeClauseContextFactory(mockController.getMock(IssueTypeResolver.class), MockJqlOperandResolver.createSimpleSupport(), mockController.getMock(ConstantsManager.class))
        {
            @Override
            List<String> getIds(final QueryLiteral value)
            {
                return CollectionBuilder.newBuilder(value.getStringValue()).asList();
            }
        };
    }
}
