package com.atlassian.jira.jql.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IssueSecurityLevelResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestIssueSecurityLevelClauseContextFactory extends MockControllerTestCase
{
    private User theUser;

    @Test
    public void testCreator() throws Exception
    {
        IssueSecurityLevelClauseContextFactory.Creator creator =
                mockController.instantiate(IssueSecurityLevelClauseContextFactory.Creator.class);
        assertNotNull(creator.create());
    }

    @Test
    public void testGetSecurityLevelsFromClause() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");
        final List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
        final List<IssueSecurityLevel> levels = new ArrayList<IssueSecurityLevel>();
        final TerminalClause terminalClause = new TerminalClauseImpl("level", Operator.EQUALS, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, terminalClause);
        mockController.setReturnValue(literals);

        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        resolver.getIssueSecurityLevels(theUser, literals);
        mockController.setReturnValue(levels);

        IssueSecurityLevelClauseContextFactory factory = mockController.instantiate(IssueSecurityLevelClauseContextFactory.class);

        assertEquals(levels, factory.getSecurityLevelsFromClause(theUser, terminalClause));
        
        mockController.verify();
    }

    @Test
    public void testGetSecurityLevelsFromClauseNullLiterals() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");
        final TerminalClause terminalClause = new TerminalClauseImpl("level", Operator.EQUALS, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, terminalClause);
        mockController.setReturnValue(null);

        IssueSecurityLevelClauseContextFactory factory = mockController.instantiate(IssueSecurityLevelClauseContextFactory.class);

        final List<IssueSecurityLevel> result = factory.getSecurityLevelsFromClause(theUser, terminalClause);
        assertTrue(result.isEmpty());

        mockController.verify();
    }

    @Test
    public void testGetSecurityLevelsFromClauseNegative() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");
        final List<QueryLiteral> literals = ImmutableList.of(createLiteral("qwerty"));
        final List<IssueSecurityLevel> levels = Lists.newArrayList(createMockSecurityLevel(1, "one"), createMockSecurityLevel(2, "two"));
        final List<IssueSecurityLevel> expectedResult = ImmutableList.of(createMockSecurityLevel(3, "three"));
        final List<IssueSecurityLevel> allLevels = newArrayList(concat(levels, expectedResult));

        final TerminalClause terminalClause = new TerminalClauseImpl("level", Operator.NOT_EQUALS, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(theUser, operand, terminalClause)).andReturn(literals);

        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        expect(resolver.getIssueSecurityLevels(theUser, literals)).andReturn(levels);
        expect(resolver.getAllSecurityLevels(theUser)).andReturn(allLevels);

        IssueSecurityLevelClauseContextFactory factory = mockController.instantiate(IssueSecurityLevelClauseContextFactory.class);

        assertEquals(expectedResult, factory.getSecurityLevelsFromClause(theUser, terminalClause));

        mockController.verify();
    }

    @Test
    public void testGetSecurityLevelsFromClauseNegativeWithEmpty() throws Exception
    {
        final IssueSecurityLevel mockLevel1 = createMockSecurityLevel(1, "one");
        final IssueSecurityLevel mockLevel2 = createMockSecurityLevel(2, "two");

        final List<IssueSecurityLevel> allLevels = Lists.newArrayList(mockLevel1, mockLevel2);
        final List<IssueSecurityLevel> levels = Lists.newArrayList(null, mockLevel1, null);

        final SingleValueOperand operand = new SingleValueOperand("test");
        final List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
        final List<IssueSecurityLevel> expectedResult = Arrays.asList(mockLevel2);

        final TerminalClause terminalClause = new TerminalClauseImpl("level", Operator.IS_NOT, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(theUser, operand, terminalClause)).andReturn(literals);

        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        expect(resolver.getIssueSecurityLevels(theUser, literals)).andReturn(levels);
        expect(resolver.getAllSecurityLevels(theUser)).andReturn(allLevels);

        final IssueSecurityLevelClauseContextFactory factory = mockController.instantiate(IssueSecurityLevelClauseContextFactory.class);
        assertEquals(expectedResult, factory.getSecurityLevelsFromClause(theUser, terminalClause));

        mockController.verify();
    }

    @Test
    public void testGetSecurityLevelsFromClauseNegativeWithEmptyOnly() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");
        final List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
        final List<IssueSecurityLevel> levels = Lists.newArrayList(null, null, null);
        final List<IssueSecurityLevel> expectedResult = Lists.newArrayList(null, null, null);

        final TerminalClause terminalClause = new TerminalClauseImpl("level", Operator.NOT_IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(theUser, operand, terminalClause)).andReturn(literals);

        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        expect(resolver.getIssueSecurityLevels(theUser, literals)).andReturn(levels);

        final IssueSecurityLevelClauseContextFactory factory = mockController.instantiate(IssueSecurityLevelClauseContextFactory.class);
        assertEquals(expectedResult, factory.getSecurityLevelsFromClause(theUser, terminalClause));

        mockController.verify();
    }

    @Test
    public void testGetProjectsForSecurityLevel() throws Exception
    {
        final IssueSecurityLevel securityLevel = createMockSecurityLevel(100l, 543L);
        final Project project = new MockProject(666L);

        final IssueSecuritySchemeManager schemeManager = mockController.getMock(IssueSecuritySchemeManager.class);
        schemeManager.getProjectsUsingScheme(543L);
        mockController.setReturnValue(ImmutableList.of(project));

        IssueSecurityLevelClauseContextFactory factory = mockController.instantiate(IssueSecurityLevelClauseContextFactory.class);

        final Collection<Project> projects = factory.getProjectsForSecurityLevel(securityLevel);
        assertEquals(1, projects.size());
        assertEquals(project, projects.iterator().next());

        mockController.verify();
    }

    @Test
    public void testGetAssociatedProjectsFromClause() throws Exception
    {
        final IssueSecurityLevel level1 = createMockSecurityLevel(1L, 1L);
        final IssueSecurityLevel level2 = createMockSecurityLevel(2L, 1L);
        final Project project1 = new MockProject(11L);
        final Project project2 = new MockProject(22L);
        final Project project3 = new MockProject(33L);

        final Map<IssueSecurityLevel, Collection<Project>> levelToProjectMap = ImmutableMap.<IssueSecurityLevel, Collection<Project>>of
                (
                        level1, ImmutableList.of(project1, project3),
                        level2, ImmutableList.of(project2, project3)

                );

        final IssueSecurityLevelClauseContextFactory factory = createFactory(levelToProjectMap);
        mockController.replay();

        final Set<Project> associatedProjects = factory.getAssociatedProjectsFromClause(theUser, null);
        assertEquals(3, associatedProjects.size());
        assertTrue(associatedProjects.contains(project1));
        assertTrue(associatedProjects.contains(project2));
        assertTrue(associatedProjects.contains(project3));
        
        mockController.verify();
    }

    @Test
    public void testGetClauseContextBadOperators() throws Exception
    {
        IssueSecurityLevelClauseContextFactory factory = mockController.instantiate(IssueSecurityLevelClauseContextFactory.class);

        for (Operator operator : Operator.values())
        {
            if (operator != Operator.EQUALS && operator != Operator.NOT_EQUALS &&
                operator != Operator.IN && operator != Operator.NOT_IN &&
                operator != Operator.IS && operator != Operator.IS_NOT)
            {
                final TerminalClause terminalClause = new TerminalClauseImpl("level", operator, "test");
                final ClauseContext clauseContext = factory.getClauseContext(theUser, terminalClause);
                assertEquals(ClauseContextImpl.createGlobalClauseContext(), clauseContext);
            }
        }

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseIsGlobalWhenNoneWhereCalculated() throws Exception
    {
        final TerminalClause terminalClause = new TerminalClauseImpl("level", Operator.EQUALS, "test");
        final Set<Project> associatedProjects = Collections.emptySet();
        final IssueSecurityLevelClauseContextFactory factory = createFactory(associatedProjects);
        mockController.replay();

        final ClauseContext context = factory.getContextFromClause(theUser, terminalClause);
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedContext, context);

        mockController.verify();
    }

    @Test
    public void testGetContextFromClauseEquals() throws Exception
    {
        final TerminalClause terminalClause = new TerminalClauseImpl("level", Operator.EQUALS, "test");
        final Set<Project> associatedProjects = ImmutableSet.<Project>of(new MockProject(666L));
        final IssueSecurityLevelClauseContextFactory factory = createFactory(associatedProjects);
        mockController.replay();

        final ClauseContext context = factory.getContextFromClause(theUser, terminalClause);
        assertEquals(1, context.getContexts().size());
        ProjectIssueTypeContext expected = createExpectedContext(666L);
        assertTrue(context.getContexts().contains(expected));

        mockController.verify();
    }

    @Test
    public void testGetClauseContextEmpty() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClause terminalClause = new TerminalClauseImpl("level", Operator.IS, operand);

        final List<QueryLiteral> literals = CollectionBuilder.<QueryLiteral>newBuilder(new QueryLiteral()).asList();
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, operand, terminalClause);
        mockController.setReturnValue(literals);

        final IssueSecurityLevelResolver securityLevelResolver = mockController.getMock(IssueSecurityLevelResolver.class);
        securityLevelResolver.getIssueSecurityLevels(theUser, literals);
        mockController.setReturnValue(Collections.<GenericValue>singletonList(null));

        IssueSecurityLevelClauseContextFactory factory = mockController.instantiate(IssueSecurityLevelClauseContextFactory.class);

        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, terminalClause));
        
        mockController.verify();
    }

    @Test
    public void testGetClauseContextNotEmpty() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");
        final TerminalClause terminalClause = new TerminalClauseImpl("level", Operator.EQUALS, operand);
        final ClauseContext contextFromClause = new ClauseContextImpl(ImmutableSet.<ProjectIssueTypeContext>of(createExpectedContext(555L)));

        final IssueSecurityLevelClauseContextFactory factory = createFactory(contextFromClause);
        mockController.replay();

        assertEquals(contextFromClause, factory.getClauseContext(theUser, terminalClause));
        
        mockController.verify();
    }

    private ProjectIssueTypeContextImpl createExpectedContext(final long projectId)
    {
        return new ProjectIssueTypeContextImpl(new ProjectContextImpl(projectId), AllIssueTypesContext.INSTANCE);
    }

    private IssueSecurityLevelClauseContextFactory createFactory(final Map<IssueSecurityLevel, Collection<Project>> associatedProjects)
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final IssueSecuritySchemeManager schemeManager = mockController.getMock(IssueSecuritySchemeManager.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);

        return new IssueSecurityLevelClauseContextFactory(resolver, jqlOperandResolver, schemeManager, projectManager)
        {
            @Override
            List<IssueSecurityLevel> getSecurityLevelsFromClause(final User searcher, final TerminalClause terminalClause)
            {
                return new ArrayList<IssueSecurityLevel>(associatedProjects.keySet());
            }

            @Override
            Collection<Project> getProjectsForSecurityLevel(final IssueSecurityLevel securityLevel)
            {
                return associatedProjects.get(securityLevel);
            }
        };
    }

    private IssueSecurityLevelClauseContextFactory createFactory(final Set<Project> associatedProjects)
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final IssueSecuritySchemeManager schemeManager = mockController.getMock(IssueSecuritySchemeManager.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);

        return new IssueSecurityLevelClauseContextFactory(resolver, jqlOperandResolver, schemeManager, projectManager)
        {
            @Override
            Set<Project> getAssociatedProjectsFromClause(final User searcher, final TerminalClause terminalClause)
            {
                return associatedProjects;
            }
        };
    }

    private IssueSecurityLevelClauseContextFactory createFactory(final ClauseContext contextFromClause)
    {
        final IssueSecurityLevelResolver resolver = mockController.getMock(IssueSecurityLevelResolver.class);
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        final IssueSecuritySchemeManager schemeManager = mockController.getMock(IssueSecuritySchemeManager.class);
        final ProjectManager projectManager = mockController.getMock(ProjectManager.class);

        return new IssueSecurityLevelClauseContextFactory(resolver, jqlOperandResolver, schemeManager, projectManager)
        {
            @Override
            ClauseContext getContextFromClause(final User searcher, final TerminalClause terminalClause)
            {
                return contextFromClause;
            }
        };
    }

    private IssueSecurityLevel createMockSecurityLevel(final Long id, final Long schemeId)
    {
        return new IssueSecurityLevelImpl(id, null, null, schemeId);
    }

    private IssueSecurityLevel createMockSecurityLevel(final long id, final String name)
    {
        return new IssueSecurityLevelImpl(id, name, null, null);
    }

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
    }

    @After
    public void tearDown() throws Exception
    {
        theUser = null;
    }
}
