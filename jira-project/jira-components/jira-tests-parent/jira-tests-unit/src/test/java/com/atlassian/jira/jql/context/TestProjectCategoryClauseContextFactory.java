package com.atlassian.jira.jql.context;

import java.util.Collection;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.ProjectCategoryResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestProjectCategoryClauseContextFactory extends MockControllerTestCase
{
    private PermissionManager permissionManager;
    private ProjectCategoryResolver projectCategoryResolver;
    private JqlOperandResolver jqlOperandResolver;
    private Collection<Project> allVisibleProjects;
    private MockProject project1;
    private MockProject project2;
    private MockProject project3;
    private MockProject project4;
    private MockProject project5;
    private User theUser = null;


    @Before
    public void setUp() throws Exception
    {
        permissionManager = mockController.getMock(PermissionManager.class);
        projectCategoryResolver = mockController.getMock(ProjectCategoryResolver.class);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        project1 = new MockProject(1L);
        project2 = new MockProject(2L);
        project3 = new MockProject(3L);
        project4 = new MockProject(4L);
        project5 = new MockProject(5L);
        allVisibleProjects = CollectionBuilder.<Project>newBuilder(
                project1,
                project2,
                project3,
                project4
        ).asSet();
    }

    @After
    public void tearDown() throws Exception
    {

        permissionManager = null;
        projectCategoryResolver = null;
        jqlOperandResolver = null;

        project1 = null;
        project2 = null;
        project3 = null;
        project4 = null;
        project5 = null;

        allVisibleProjects = null;
    }

    @Test
    public void testBadOperator() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("category", Operator.LESS_THAN, "test");

        replay();
        final ProjectCategoryClauseContextFactory factory = new ProjectCategoryClauseContextFactory(permissionManager, projectCategoryResolver, jqlOperandResolver);

        final ClauseContext result = factory.getClauseContext(theUser, clause);
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), result);
        
        verify();
    }

    @Test
    public void testNullLiterals() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");
        final TerminalClause clause = new TerminalClauseImpl("category", Operator.EQUALS, operand);

        EasyMock.expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null))
                .andReturn(allVisibleProjects);

        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        EasyMock.expect(jqlOperandResolver.getValues(theUser, operand, clause))
                .andReturn(null);

        replay();
        final ProjectCategoryClauseContextFactory factory = new ProjectCategoryClauseContextFactory(permissionManager, projectCategoryResolver, jqlOperandResolver);

        final ClauseContext result = factory.getClauseContext(theUser, clause);
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), result);

        verify();
    }

    @Test
    public void testEquality() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand(new QueryLiteral(), createLiteral(1L));
        final TerminalClause clause = new TerminalClauseImpl("category", Operator.IN, operand);

        EasyMock.expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null))
                .andReturn(allVisibleProjects);

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(new QueryLiteral()))
                .andReturn(CollectionBuilder.<Project>newBuilder(project1, project5).asSet());

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral(1L)))
                .andReturn(CollectionBuilder.<Project>newBuilder(project2, project3).asSet());

        replay();
        final ProjectCategoryClauseContextFactory factory = new ProjectCategoryClauseContextFactory(permissionManager, projectCategoryResolver, jqlOperandResolver);

        final ClauseContext result = factory.getClauseContext(theUser, clause);
        final ClauseContext expectedContext = new ClauseContextImpl(
                CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                        new ProjectIssueTypeContextImpl(new ProjectContextImpl(1L), AllIssueTypesContext.INSTANCE),
                        new ProjectIssueTypeContextImpl(new ProjectContextImpl(2L), AllIssueTypesContext.INSTANCE),
                        new ProjectIssueTypeContextImpl(new ProjectContextImpl(3L), AllIssueTypesContext.INSTANCE)
                )
                .asSet()
        );
        assertEquals(expectedContext, result);

        verify();
    }

    @Test
    public void testEqualityNoResolvedProjects() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand(createLiteral(1L));
        final TerminalClause clause = new TerminalClauseImpl("category", Operator.IN, operand);

        EasyMock.expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null))
                .andReturn(allVisibleProjects);

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral(1L)))
                .andReturn(CollectionBuilder.<Project>newBuilder().asSet());

        replay();
        final ProjectCategoryClauseContextFactory factory = new ProjectCategoryClauseContextFactory(permissionManager, projectCategoryResolver, jqlOperandResolver);

        final ClauseContext result = factory.getClauseContext(theUser, clause);
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedContext, result);

        verify();
    }

    @Test
    public void testInequalityNoResolvedProjects() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand(createLiteral(1L));
        final TerminalClause clause = new TerminalClauseImpl("category", Operator.NOT_IN, operand);

        EasyMock.expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null))
                .andReturn(allVisibleProjects);

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral(1L)))
                .andReturn(CollectionBuilder.<Project>newBuilder().asSet());

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(new QueryLiteral()))
                .andReturn(CollectionBuilder.<Project>newBuilder().asSet());

        replay();
        final ProjectCategoryClauseContextFactory factory = new ProjectCategoryClauseContextFactory(permissionManager, projectCategoryResolver, jqlOperandResolver);

        final ClauseContext result = factory.getClauseContext(theUser, clause);
        final ClauseContext expectedContext = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedContext, result);

        verify();
    }

    @Test
    public void testInequalityWithEmpty() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand(new QueryLiteral(), createLiteral(1L));
        final TerminalClause clause = new TerminalClauseImpl("category", Operator.NOT_IN, operand);

        EasyMock.expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null))
                .andReturn(allVisibleProjects);

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(new QueryLiteral()))
                .andReturn(CollectionBuilder.<Project>newBuilder(project1, project5).asSet());

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral(1L)))
                .andReturn(CollectionBuilder.<Project>newBuilder(project2, project3).asSet());

        replay();
        final ProjectCategoryClauseContextFactory factory = new ProjectCategoryClauseContextFactory(permissionManager, projectCategoryResolver, jqlOperandResolver);

        final ClauseContext result = factory.getClauseContext(theUser, clause);
        final ClauseContext expectedContext = new ClauseContextImpl(
                CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                        new ProjectIssueTypeContextImpl(new ProjectContextImpl(4L), AllIssueTypesContext.INSTANCE)
                )
                .asSet()
        );
        assertEquals(expectedContext, result);

        verify();
    }

    @Test
    public void testInequalityWithoutEmpty() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand(createLiteral(1L));
        final TerminalClause clause = new TerminalClauseImpl("category", Operator.NOT_IN, operand);

        EasyMock.expect(permissionManager.getProjectObjects(Permissions.BROWSE, (User) null))
                .andReturn(allVisibleProjects);

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(createLiteral(1L)))
                .andReturn(CollectionBuilder.<Project>newBuilder(project2, project3).asSet());

        EasyMock.expect(projectCategoryResolver.getProjectsForCategory(new QueryLiteral()))
                .andReturn(CollectionBuilder.<Project>newBuilder(project1, project5).asSet());

        replay();
        final ProjectCategoryClauseContextFactory factory = new ProjectCategoryClauseContextFactory(permissionManager, projectCategoryResolver, jqlOperandResolver);

        final ClauseContext result = factory.getClauseContext(theUser, clause);
        final ClauseContext expectedContext = new ClauseContextImpl(
                CollectionBuilder.<ProjectIssueTypeContext>newBuilder(
                        new ProjectIssueTypeContextImpl(new ProjectContextImpl(4L), AllIssueTypesContext.INSTANCE)
                )
                .asSet()
        );
        assertEquals(expectedContext, result);

        verify();
    }
}
