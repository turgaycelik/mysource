package com.atlassian.jira.jql.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestProjectClauseContextFactory extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private NameResolver<Project> projectResolver;
    private PermissionManager permissionManager;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        projectResolver = mockController.getMock(NameResolver.class);
        permissionManager = mockController.getMock(PermissionManager.class);
    }

    @After
    public void tearDown() throws Exception
    {
        jqlOperandResolver = null;
        projectResolver = null;
        permissionManager = null;
    }

    @Test
    public void testGetClauseContextBadOperator() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.GREATER_THAN, 12345L);

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        mockController.replay();

        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextForEmpty() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.EQUALS, EmptyOperand.EMPTY);
        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        mockController.replay();
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextForNoVisibleProjects() throws Exception
    {
        final MockProject mockProject = new MockProject(12345L, "TST", "Test Project");
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.EQUALS, 12345L);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(Collections.<Project>emptyList());

        // NOTE: this is done this way because EasyMock has a bug of some sort that won't let us mock out the
        // projectResolver class
        final ProjectResolver projectResolver = new ProjectResolver(mockController.getMock(ProjectManager.class))
        {
            @Override
            public Project get(final Long id)
            {
                assertEquals(new Long(12345L), id);
                return mockProject;
            }
        };

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(MockJqlOperandResolver.createSimpleSupport(), projectResolver, permissionManager);

        mockController.replay();
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextForNullLiterals() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.EQUALS, 12345L);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(theUser, projectClause.getOperand(), projectClause)).andReturn(null);

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        mockController.replay();
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextForOneVisibleProject() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.EQUALS, 12345L);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, projectClause.getOperand(), projectClause);
        mockController.setReturnValue(Collections.singletonList(createLiteral(12345L)));
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        final MockProject mockProject = new MockProject(12345L, "TST", "Test Project");
        mockController.setReturnValue(Collections.singletonList(mockProject));

        // NOTE: this is done this way because EasyMock has a bug of some sort that won't let us mock out the
        // projectResolver class
        final ProjectResolver projectResolver = new ProjectResolver(mockController.getMock(ProjectManager.class))
        {
            @Override
            public Project get(final Long id)
            {
                assertEquals(new Long(12345), id);
                return mockProject;
            }
        };

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        mockController.replay();
        final ClauseContext expectedContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(12345L), AllIssueTypesContext.INSTANCE)));
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextForOneVisibleProjectMultpleSpecified() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", 12345L, 54321L);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, projectClause.getOperand(), projectClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(12345L), createLiteral(54321L)).asList());
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        final MockProject mockProject1 = new MockProject(12345L, "TST", "Test Project");
        final MockProject mockProject2 = new MockProject(54321L, "ANA", "Another Project");
        mockController.setReturnValue(Collections.singletonList(mockProject1));

        // NOTE: this is done this way because EasyMock has a bug of some sort that won't let us mock out the
        // projectResolver class
        final ProjectResolver projectResolver = new ProjectResolver(mockController.getMock(ProjectManager.class))
        {
            boolean calledFirst = true;
            @Override
            public Project get(final Long id)
            {
                if (calledFirst)
                {
                    calledFirst = false;
                    assertEquals(new Long(12345), id);
                    return mockProject1;
                }
                else
                {
                    assertEquals(new Long(54321), id);
                    return mockProject2;
                }
            }
        };
        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        mockController.replay();
        final ClauseContext expectedContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(12345L), AllIssueTypesContext.INSTANCE)));
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextForTwoVisibleProjectsSameName() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.EQUALS, "Test Project");

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, projectClause.getOperand(), projectClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral("Test Project")).asList());
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        final MockProject mockProject1 = new MockProject(12345L, "TST", "Test Project");
        final MockProject mockProject2 = new MockProject(54321L, "ANA", "Test Project");
        mockController.setReturnValue(CollectionBuilder.newBuilder(mockProject1, mockProject2).asList());

        // NOTE: this is done this way because EasyMock has a bug of some sort that won't let us mock out the
        // projectResolver class
        final ProjectResolver projectResolver = new ProjectResolver(mockController.getMock(ProjectManager.class))
        {
            @Override
            public List<String> getIdsFromName(final String name)
            {
                return CollectionBuilder.newBuilder("12345", "54321").asList();
            }
        };
        mockController.replay();

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        final ClauseContext expectedContext =
                new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                        new ProjectIssueTypeContextImpl(new ProjectContextImpl(12345L), AllIssueTypesContext.INSTANCE),
                        new ProjectIssueTypeContextImpl(new ProjectContextImpl(54321L), AllIssueTypesContext.INSTANCE)).asSet());
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextForTwoVisibleProjectsNotEquals() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.NOT_EQUALS, 54321L);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, projectClause.getOperand(), projectClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(54321L)).asList());
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        final MockProject mockProject1 = new MockProject(12345L, "TST", "Test Project");
        final MockProject mockProject2 = new MockProject(54321L, "ANA", "Test Project");
        mockController.setReturnValue(CollectionBuilder.newBuilder(mockProject1, mockProject2).asList());

        // NOTE: this is done this way because EasyMock has a bug of some sort that won't let us mock out the
        // projectResolver class
        final ProjectResolver projectResolver = new ProjectResolver(mockController.getMock(ProjectManager.class))
        {
            @Override
            public Project get(final Long id)
            {
                assertEquals(new Long(54321), id);
                return mockProject2;
            }
        };

        mockController.replay();
        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        final ClauseContext expectedContext =
                new ClauseContextImpl(CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                        new ProjectIssueTypeContextImpl(new ProjectContextImpl(12345L), AllIssueTypesContext.INSTANCE)).asSet());
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextForOneVisibleProjectNoTypes() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.EQUALS, 12345L);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(theUser, projectClause.getOperand(), projectClause);
        mockController.setReturnValue(Collections.singletonList(createLiteral(12345L)));
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        permissionManager.getProjectObjects(Permissions.BROWSE, (User) null);
        final MockProject mockProject = new MockProject(12345L, "TST", "Test Project");
        mockController.setReturnValue(Collections.singletonList(mockProject));

        // NOTE: this is done this way because EasyMock has a bug of some sort that won't let us mock out the
        // projectResolver class
        final ProjectResolver projectResolver = new ProjectResolver(mockController.getMock(ProjectManager.class))
        {
            @Override
            public Project get(final Long id)
            {
                assertEquals(new Long(12345), id);
                return mockProject;
            }
        };
        mockController.replay();

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        final ClauseContext expectedContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(mockProject.getId()), AllIssueTypesContext.INSTANCE)));
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextEmptyInList() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(new EmptyOperand()));
        final JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        mockController.replay();
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextEmptyInWithValues() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(new EmptyOperand(), new SingleValueOperand(12345L)));
        final MockProject mockProject = new MockProject(12345L, "TST", "Test Project");

        final JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(Collections.<Project>singletonList(mockProject));

        // NOTE: this is done this way because EasyMock has a bug of some sort that won't let us mock out the
        // projectResolver class
        final ProjectResolver projectResolver = new ProjectResolver(mockController.getMock(ProjectManager.class))
        {
            @Override
            public Project get(final Long id)
            {
                assertEquals(new Long(12345), id);
                return mockProject;
            }
        };

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        mockController.replay();
        final ClauseContext expectedContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(12345L), AllIssueTypesContext.INSTANCE)));
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextEmptyInListNegation() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.NOT_IN, new MultiValueOperand(new EmptyOperand()));
        final JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        mockController.replay();
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextEmptyInWithValuesNegation() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.NOT_IN, new MultiValueOperand(new EmptyOperand(), new SingleValueOperand(12345L)));
        final MockProject mockProject = new MockProject(12345L, "TST", "Test Project");
        final MockProject mockProject2 = new MockProject(12346L, "BAD", "BAD Project");

        final JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(Arrays.<Project>asList(mockProject, mockProject2));

        // NOTE: this is done this way because EasyMock has a bug of some sort that won't let us mock out the
        // projectResolver class
        final ProjectResolver projectResolver = createResolverForProjects(mockProject, mockProject2);

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        mockController.replay();
        final ClauseContext expectedContext = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(12346L), AllIssueTypesContext.INSTANCE)));
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextAllValuesNegation() throws Exception
    {
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.NOT_IN, new MultiValueOperand(new SingleValueOperand(12345L), new SingleValueOperand(12346L)));
        final MockProject mockProject = new MockProject(12345L, "TST", "Test Project");
        final MockProject mockProject2 = new MockProject(12346L, "BAD", "BAD Project");

        final JqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null)).andReturn(Arrays.<Project>asList(mockProject, mockProject2));

        // NOTE: this is done this way because EasyMock has a bug of some sort that won't let us mock out the
        // projectResolver class
        final ProjectResolver projectResolver = createResolverForProjects(mockProject, mockProject2);

        final ProjectClauseContextFactory clauseContextFactory =
                new ProjectClauseContextFactory(jqlOperandResolver, projectResolver, permissionManager);

        mockController.replay();
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        final ClauseContext clauseContext = clauseContextFactory.getClauseContext(theUser, projectClause);
        assertEquals(expectedContext, clauseContext);
        mockController.verify();
    }

    private ProjectResolver createResolverForProjects(Project...projects)
    {
        final Map<Long, Project> projectMap = new HashMap<Long, Project>();
        for (Project project : projects)
        {
            projectMap.put(project.getId(), project);
        }

        return new ProjectResolver(mockController.getMock(ProjectManager.class))
        {
            @Override
            public Project get(final Long id)
            {
                if (projectMap.containsKey(id))
                {
                    return projectMap.get(id);
                }
                else
                {
                    fail("Got a request for project '" + id + "' that we did not expect.");
                    return null;
                }
            }
        };
    }
}
