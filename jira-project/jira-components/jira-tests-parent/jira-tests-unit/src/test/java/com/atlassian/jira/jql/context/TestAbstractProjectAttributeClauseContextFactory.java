package com.atlassian.jira.jql.context;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static com.atlassian.query.operator.Operator.EQUALS;
import static com.atlassian.query.operator.Operator.GREATER_THAN;
import static com.atlassian.query.operator.Operator.GREATER_THAN_EQUALS;
import static com.atlassian.query.operator.Operator.IN;
import static com.atlassian.query.operator.Operator.IS;
import static com.atlassian.query.operator.Operator.IS_NOT;
import static com.atlassian.query.operator.Operator.LESS_THAN;
import static com.atlassian.query.operator.Operator.LESS_THAN_EQUALS;
import static com.atlassian.query.operator.Operator.NOT_EQUALS;
import static com.atlassian.query.operator.Operator.NOT_IN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractProjectAttributeClauseContextFactory
{
    private static final List<String> TEN = ImmutableList.of("10");
    private static final List<String> EMPTY = ImmutableList.of();

    @Mock IndexInfoResolver<Version> indexInfoResolver;
    @Mock JqlOperandResolver jqlOperandResolver;
    @Mock PermissionManager permissionManager;

    Fixture fixture;

    @Before
    public void setUp()
    {
        fixture = new Fixture();
    }

    @After
    public void tearDown()
    {
        indexInfoResolver = null;
        jqlOperandResolver = null;
        permissionManager = null;
        fixture = null;
    }

    @Test
    public void testIsNegationOperator() throws Exception
    {
        fixture.assertNegation(true, NOT_EQUALS, NOT_IN, IS_NOT);
        fixture.assertNegation(false, IS, EQUALS, IN, GREATER_THAN, GREATER_THAN_EQUALS, LESS_THAN, LESS_THAN_EQUALS);
    }

    @Test
    public void testIsRelationalOperator() throws Exception
    {
        fixture.assertRelational(true, GREATER_THAN, GREATER_THAN_EQUALS, LESS_THAN, LESS_THAN_EQUALS);
        fixture.assertRelational(false, IS_NOT, NOT_EQUALS, NOT_IN, IS, EQUALS, IN);
    }

    @Test
    public void testGetIdsLongValue() throws Exception
    {
        QueryLiteral literal = createLiteral(10L);

        when(indexInfoResolver.getIndexedValues(10L))
                .thenReturn(TEN)
                .thenReturn(EMPTY);

        List<Long> result = fixture.getIds(literal);
        assertThat(result, contains(10L));

        result = fixture.getIds(literal);
        assertThat(result, hasSize(0));
    }

    @Test
    public void testGetIdsFromStringValue() throws Exception
    {
        QueryLiteral literal = createLiteral("test");

        when(indexInfoResolver.getIndexedValues("test"))
                .thenReturn(TEN)
                .thenReturn(EMPTY);

        List<Long> result = fixture.getIds(literal);
        assertThat(result, contains(10L));

        result = fixture.getIds(literal);
        assertThat(result, hasSize(0));
    }

    @Test
    public void testGetContextsForProject() throws Exception
    {
        MockProject project1 = new MockProject(10, "test1");

        when(permissionManager.hasPermission(Permissions.BROWSE, project1, (User)null)).thenReturn(true);

        final ProjectIssueTypeContext expected = new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE);
        final Set<ProjectIssueTypeContext> result = fixture.getContextsForProject(null, project1);
        assertThat(result, contains(expected));
    }

    @Test
    public void testGetContextsForNullProject() throws Exception
    {
        final Set<ProjectIssueTypeContext> result = fixture.getContextsForProject(null, null);

        assertThat(result, hasSize(0));
    }

    @Test
    public void testGetContextsForProjectNoPerm() throws Exception
    {
        final MockProject project1 = new MockProject(10, "test1");
        final Set<ProjectIssueTypeContext> result = fixture.getContextsForProject(null, project1);
        assertThat(result, hasSize(0));
    }

    @Test
    public void testGetClauseContextNotEmpty() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", GREATER_THAN, operand);
        final ClauseContext context = new ClauseContextImpl(ImmutableSet.<ProjectIssueTypeContext>of(
                new ProjectIssueTypeContextImpl(new ProjectContextImpl(2l), new IssueTypeContextImpl("IT2"))));

        fixture = new Fixture()
        {
            @Override
            ClauseContext getContextFromClause(final User searcher, final TerminalClause terminalClause)
            {
                return context;
            }
        };

        final ClauseContext result = fixture.getClauseContext(null, clause);
        assertThat(result, equalTo(context));
    }

    @Test
    public void testGetClauseContextNotEmptyReturnsEmptyContextFromClause() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", GREATER_THAN, operand);

        final ClauseContext result = fixture.getClauseContext(null, clause);
        assertThat(result, equalTo(ClauseContextImpl.createGlobalClauseContext()));
    }

    @Test
    public void testGetClauseContextEmpty() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", GREATER_THAN, operand);

        when(jqlOperandResolver.isEmptyOperand(operand)).thenReturn(true);

        final ClauseContext result = fixture.getClauseContext(null, clause);
        assertThat(result, equalTo(ClauseContextImpl.createGlobalClauseContext()));
    }



    class Fixture extends AbstractProjectAttributeClauseContextFactory<Version>
    {
        public Fixture()
        {
            super(indexInfoResolver, jqlOperandResolver, permissionManager);
        }

        ClauseContext getContextFromClause(final User searcher, final TerminalClause terminalClause)
        {
            return new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());
        }

        void assertNegation(boolean expectedResult, Operator... operators)
        {
            for (Operator operator : operators)
            {
                if (expectedResult != isNegationOperator(operator))
                {
                    fail("Expected isNegationOperator to return " + expectedResult + " for " + operator);
                }
            }
        }

        void assertRelational(boolean expectedResult, Operator... operators)
        {
            for (Operator operator : operators)
            {
                if (expectedResult != isRelationalOperator(operator))
                {
                    fail("Expected isRelationalOperator to return " + expectedResult + " for " + operator);
                }
            }
        }

    }
}
