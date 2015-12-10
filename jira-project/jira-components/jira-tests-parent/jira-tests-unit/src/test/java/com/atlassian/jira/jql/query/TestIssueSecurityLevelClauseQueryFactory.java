package com.atlassian.jira.jql.query;

import java.util.Collections;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IssueSecurityLevelResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.operator.OperatorDoesNotSupportOperand;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestIssueSecurityLevelClauseQueryFactory extends MockControllerTestCase
{
    private User theUser;
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
        final JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        mockController.addObjectInstance(jqlOperandResolver);
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @After
    public void tearDown() throws Exception
    {
        theUser = null;
    }

    @Test
    public void testConstructors() throws Exception
    {
        final IssueSecurityLevelResolver issueSecurityLevelResolver = mockController.getMock(IssueSecurityLevelResolver.class);
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);

        mockController.replay();

        try
        {
            new IssueSecurityLevelClauseQueryFactory(null, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new IssueSecurityLevelClauseQueryFactory(issueSecurityLevelResolver, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        new IssueSecurityLevelClauseQueryFactory(issueSecurityLevelResolver, jqlOperandResolver);
        
        mockController.verify();
    }

    @Test
    public void testIsEmpty() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(theUser, Collections.singletonList(new QueryLiteral()));
        mockController.setReturnValue(Collections.singletonList(null));
        resolver.getIssueSecurityLevels(theUser, Collections.singletonList(new QueryLiteral()));
        mockController.setReturnValue(Collections.singletonList(null));

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.IS, EmptyOperand.EMPTY));
        assertEquals("issue_security_level:-1", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.EQUALS, EmptyOperand.EMPTY));
        assertEquals("issue_security_level:-1", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testIsNotEmpty() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(theUser, ImmutableList.of(new QueryLiteral()));
        mockController.setReturnValue(Collections.singletonList(null));
        resolver.getIssueSecurityLevels(theUser, ImmutableList.of(new QueryLiteral()));
        mockController.setReturnValue(Collections.singletonList(null));

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.IS_NOT, EmptyOperand.EMPTY));
        assertEquals("issue_security_level:-1", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.NOT_EQUALS, EmptyOperand.EMPTY));
        assertEquals("issue_security_level:-1", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());
    }

    @Test
    public void testEquals() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(theUser, ImmutableList.of(createLiteral("TheName")));
        mockController.setReturnValue(ImmutableList.of(createMockSecurityLevel(5L, "TheName")));

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.EQUALS, "TheName"));
        assertEquals("issue_security_level:5", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testEqualsOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        EasyMock.expect(resolver.getIssueSecurityLevelsOverrideSecurity(Collections.singletonList(createLiteral("TheName"))))
                .andReturn(ImmutableList.<IssueSecurityLevel>of(createMockSecurityLevel(5L, "TheName")));

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.EQUALS, "TheName"));
        assertEquals("issue_security_level:5", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testNotEquals() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(theUser, ImmutableList.of(createLiteral("TheName")));
        mockController.setReturnValue(ImmutableList.of(createMockSecurityLevel(5L, "TheName")));

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.NOT_EQUALS, "TheName"));
        assertEquals("-issue_security_level:-1 -issue_security_level:5", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testIn() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(
                theUser,
                ImmutableList.of(createLiteral(5L), createLiteral(6L))

        );
        mockController.setReturnValue(
                ImmutableList.of(createMockSecurityLevel(5L, "TheName"), createMockSecurityLevel(6L, "TheName"))
        );

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.IN, new MultiValueOperand(5L, 6L)));
        assertEquals("issue_security_level:5 issue_security_level:6", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testInWithOnlyOne() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(
                theUser,
                ImmutableList.of(createLiteral(6L))

        );
        mockController.setReturnValue(
                ImmutableList.of(createMockSecurityLevel(6L, "TheName"))

        );

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.IN, new MultiValueOperand(6L)));
        assertEquals("issue_security_level:6", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testNotIn() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(
                theUser,
                ImmutableList.of(createLiteral(5L), createLiteral(6L))
        );
        mockController.setReturnValue(
                ImmutableList.of(createMockSecurityLevel(5L, "TheName"), createMockSecurityLevel(6L, "TheName"))

        );

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.NOT_IN, new MultiValueOperand(5L, 6L)));
        assertEquals("-issue_security_level:-1 -issue_security_level:5 -issue_security_level:6", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testNotInWithOnlyOne() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(
                theUser,
                ImmutableList.of(createLiteral(6L))

        );
        mockController.setReturnValue(
                ImmutableList.of(createMockSecurityLevel(6L, "TheName"))

        );

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.NOT_IN, new MultiValueOperand(6L)));
        assertEquals("-issue_security_level:-1 -issue_security_level:6", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testNotInWithOnlyEmpty() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(
                theUser,
                ImmutableList.of(new QueryLiteral())
        );
        mockController.setReturnValue(
                Collections.<GenericValue>singletonList(null)
        );

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.NOT_IN, new MultiValueOperand(EmptyOperand.EMPTY)));
        assertEquals("issue_security_level:-1", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());
    }

    @Test
    public void testInNotAllAreResolved() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(
                theUser,
                ImmutableList.of(
                        createLiteral(5L),
                        createLiteral(6L),
                        createLiteral(7L),
                        new QueryLiteral())
        );
        mockController.setReturnValue(
                newArrayList(
                        createMockSecurityLevel(5L, "TheName"),
                        createMockSecurityLevel(6L, "TheName"),
                        null)
        );

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.IN, new MultiValueOperand(createLiteral(5L), createLiteral(6L), createLiteral(7L), new QueryLiteral())));
        assertEquals("issue_security_level:5 issue_security_level:6 issue_security_level:-1", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testNotInNotAllAreResolved() throws Exception
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(
                theUser,
                ImmutableList.of(
                        createLiteral(5L),
                        createLiteral(6L),
                        createLiteral(7L))
        );
        mockController.setReturnValue(
                ImmutableList.of(
                        createMockSecurityLevel(5L, "TheName"),
                        createMockSecurityLevel(6L, "TheName"))
        );

        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("level", Operator.NOT_IN, new MultiValueOperand(5L, 6L, 7L)));
        assertEquals("-issue_security_level:-1 -issue_security_level:5 -issue_security_level:6", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testNullLiteralsReturned() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand(5L, 6L, 7L);
        final TerminalClauseImpl clause = new TerminalClauseImpl("level", Operator.NOT_IN, operand);

        final IssueSecurityLevelResolver issueSecurityLevelResolver = mockController.getMock(IssueSecurityLevelResolver.class);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        EasyMock.expect(jqlOperandResolver.getValues(queryCreationContext, operand, clause))
                .andReturn(null);

        replay();
        
        IssueSecurityLevelClauseQueryFactory factory = new IssueSecurityLevelClauseQueryFactory(issueSecurityLevelResolver, jqlOperandResolver);

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testCreateQueryForValuesWithBadOperator() throws Exception
    {
        IssueSecurityLevelClauseQueryFactory factory = mockController.instantiate(IssueSecurityLevelClauseQueryFactory.class);
        for (Operator operator : Operator.values())
        {
            if (Operator.IN == operator || Operator.NOT_IN == operator ||
                Operator.EQUALS == operator || Operator.NOT_EQUALS == operator)
            {
                continue;
            }

            try
            {
                factory.createQueryForValues(operator, Collections.<String>emptyList());
            }
            catch (OperatorDoesNotSupportOperand expected) {}
        }
    }

    private IssueSecurityLevel createMockSecurityLevel(final Long id, final String name)
    {
        return new IssueSecurityLevelImpl(id, name, null, null);
    }
}
