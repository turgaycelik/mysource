package com.atlassian.jira.jql.context;

import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.ComponentResolver;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestComponentClauseContextFactory
{
    private static final User ANONYMOUS = null;

    @Mock private JqlOperandResolver jqlOperandResolver;
    @Mock private ProjectManager projectManager;
    @Mock private PermissionManager permissionManager;
    @Mock private ComponentResolver componentResolver;

    @After
    public void tearDown()
    {
        jqlOperandResolver = null;
        projectManager = null;
        permissionManager = null;
        componentResolver = null;
    }

    @Test
    public void testGetContextFromClauseSingleEmptyValuePositiveOperator() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.IS, operand);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(new QueryLiteral()));

        final ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver,
                componentResolver, projectManager, permissionManager);

        final ClauseContext result = factory.getContextFromClause(ANONYMOUS, clause);
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetContextFromClauseSingleEmptyValueNegativeOperator() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.IS_NOT, operand);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(new QueryLiteral()));

        final ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver,
                componentResolver, projectManager, permissionManager);

        final ClauseContext result = factory.getContextFromClause(ANONYMOUS, clause);
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetContextFromClauseSingleValueEqualityOperand() throws Exception
    {
        final MockProject project = new MockProject(1234L);
        when(projectManager.getProjectObj(1234L)).thenReturn(project);

        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.EQUALS, operand);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(createLiteral("blarg")));

        final ProjectComponent component = new MockProjectComponent(10L, "component", project.getId());
        when(componentResolver.get(anyLong())).thenReturn(component);

        final Set<ProjectIssueTypeContext> issueTypeContexts = contexts(context(10L, "it"));
        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return ImmutableList.of(10L).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                return issueTypeContexts;
            }
        };

        final ClauseContext result = factory.getContextFromClause(ANONYMOUS, clause);
        final ClauseContext expectedResult = new ClauseContextImpl(issueTypeContexts);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetContextFromClauseSingleValueNegationOperand() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, operand);

        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(createLiteral(10L)));

        final MockProject project1 = new MockProject(1234L);
        final MockProject project2 = new MockProject(5678L);
        final MockProject project3 = new MockProject(9876L);
        when(projectManager.getProjectObj(5678L)).thenReturn(project2);
        when(projectManager.getProjectObj(9876L)).thenReturn(project3);
        when(permissionManager.hasPermission(eq(Permissions.BROWSE), any(Project.class), eq(ANONYMOUS))).thenReturn(true);

        final ProjectComponent excludedComponent = new MockProjectComponent(10L, "excludedComponent", project1.getId());
        final ProjectComponent component1 = new MockProjectComponent(15L, "component1", project2.getId());
        final ProjectComponent component2 = new MockProjectComponent(20L, "component2", project3.getId());

        when(componentResolver.idExists(10L)).thenReturn(true);
        when(componentResolver.get(10L)).thenReturn(excludedComponent);
        when(componentResolver.getAll()).thenReturn(ImmutableList.of(excludedComponent, component1, component2));

        final Set<ProjectIssueTypeContext> issueTypeContexts1 = contexts(context(10L, "id"));
        final Set<ProjectIssueTypeContext> issueTypeContexts2 = contexts(context(50L, "it2"));

        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager)
        {
            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                if (project.getId().equals(5678L))
                {
                    return issueTypeContexts1;
                }
                else if (project.getId().equals(9876L))
                {
                    return issueTypeContexts2;
                }
                return null;
            }
        };

        final ClauseContext result = factory.getContextFromClause(ANONYMOUS, clause);
        final ClauseContext expectedResult = new ClauseContextImpl(contexts(issueTypeContexts1, issueTypeContexts2));
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetContextFromClauseMultiValueEqualityOperand() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.EQUALS, operand);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(
                createLiteral(10L),
                createLiteral(20L)));

        final MockProject project1 = new MockProject(5678L);
        final MockProject project2 = new MockProject(9876L);
        when(projectManager.getProjectObj(5678L)).thenReturn(project1);
        when(projectManager.getProjectObj(9876L)).thenReturn(project2);

        final ProjectComponent component1 = new MockProjectComponent(10L, "component", project1.getId());
        final ProjectComponent component2 = new MockProjectComponent(20L, "component", project2.getId());
        when(componentResolver.get(10L)).thenReturn(component1);
        when(componentResolver.get(20L)).thenReturn(component2);

        final Set<ProjectIssueTypeContext> issueTypeContexts1 = contexts(context(10L, "it"));
        final Set<ProjectIssueTypeContext> issueTypeContexts2 = contexts(context(20L, "it2"));
        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return ImmutableList.of(literal.getLongValue()).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                if (project.getId().equals(5678L))
                {
                    return issueTypeContexts1;
                }
                else if (project.getId().equals(9876L))
                {
                    return issueTypeContexts2;
                }
                return null;
            }
        };

        final ClauseContext result = factory.getContextFromClause(ANONYMOUS, clause);
        final ClauseContext expectedResult = new ClauseContextImpl(contexts(issueTypeContexts1, issueTypeContexts2));
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetContextFromClauseMultieValueNegationOperand() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.NOT_EQUALS, operand);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(createLiteral(10L)));

        final MockProject project1 = new MockProject(1234L);
        final MockProject project2 = new MockProject(5678L);
        final MockProject project3 = new MockProject(9876L);
        when(projectManager.getProjectObj(5678L)).thenReturn(project2);

        final ProjectComponent excludedComponent1 = new MockProjectComponent(10L, "excludedComponent1", project1.getId());
        final ProjectComponent excludedComponent2 = new MockProjectComponent(20L, "excludedComponent2", project3.getId());
        final ProjectComponent component1 = new MockProjectComponent(15L, "component1", project2.getId());

        when(componentResolver.get(10L)).thenReturn(excludedComponent1);
        when(componentResolver.get(20L)).thenReturn(excludedComponent2);
        when(componentResolver.getAll()).thenReturn(ImmutableList.of(excludedComponent1, component1, excludedComponent2));

        final Set<ProjectIssueTypeContext> issueTypeContexts1 = contexts(context(10L, "it"));
        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager)
        {
            @Override
            List<Long> getIds(final QueryLiteral literal)
            {
                return ImmutableList.of(10L, 20L).asList();
            }

            @Override
            Set<ProjectIssueTypeContext> getContextsForProject(final User searcher, final Project project)
            {
                if (project.getId().equals(5678L))
                {
                    return issueTypeContexts1;
                }
                return null;
            }
        };

        final ClauseContext result = factory.getContextFromClause(ANONYMOUS, clause);
        final ClauseContext expectedResult = new ClauseContextImpl(issueTypeContexts1);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetContextFromClauseNullLiterals() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.EQUALS, operand);

        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager);

        final ClauseContext result = factory.getContextFromClause(ANONYMOUS, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetContextFromClauseNoLiterals() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;

        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.EQUALS, operand);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.<QueryLiteral>of());

        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager);

        final ClauseContext result = factory.getContextFromClause(ANONYMOUS, clause);
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetContextFromClauseInvalidOperator() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blarg");
        final TerminalClauseImpl clause = new TerminalClauseImpl("blarg", Operator.LESS_THAN, operand);

        ComponentClauseContextFactory factory = new ComponentClauseContextFactory(jqlOperandResolver, componentResolver, projectManager, permissionManager);

        final ClauseContext result = factory.getContextFromClause(ANONYMOUS, clause);
        ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedResult, result);
    }



    private static ProjectIssueTypeContext context(final long projectId, final String issueTypeId)
    {
        return new ProjectIssueTypeContextImpl(new ProjectContextImpl(projectId), new IssueTypeContextImpl(issueTypeId));
    }

    private static Set<ProjectIssueTypeContext> contexts(ProjectIssueTypeContext... contexts)
    {
        return ImmutableSet.copyOf(contexts);
    }

    private static Set<ProjectIssueTypeContext> contexts(Set<ProjectIssueTypeContext> contexts1, Set<ProjectIssueTypeContext> contexts2)
    {
        return ImmutableSet.<ProjectIssueTypeContext>builder()
                .addAll(contexts1)
                .addAll(contexts2)
                .build();
    }
}
