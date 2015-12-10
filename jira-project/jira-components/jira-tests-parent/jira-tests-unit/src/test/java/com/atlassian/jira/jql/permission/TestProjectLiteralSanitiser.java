package com.atlassian.jira.jql.permission;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestProjectLiteralSanitiser extends MockControllerTestCase
{
    private User theUser;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
        mockController.addObjectInstance(theUser);
    }

    @Test
    public void testGetIndexValues() throws Exception
    {
        final List<String> expectedValues1 = Collections.singletonList("StringIndexValues");
        final List<String> expectedValues2 = Collections.singletonList("LongIndexValues");

        final IndexInfoResolver<Project> projectIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(projectIndexInfoResolver.getIndexedValues("litString")).andReturn(expectedValues1);
        EasyMock.expect(projectIndexInfoResolver.getIndexedValues(1111L)).andReturn(expectedValues2);

        ProjectLiteralSanitiser visitor = mockController.instantiate(ProjectLiteralSanitiser.class);

        assertEquals(expectedValues1, visitor.getIndexValues(createLiteral("litString")));
        assertEquals(expectedValues2, visitor.getIndexValues(createLiteral(1111L)));
        assertEquals(Collections.<String>emptyList(), visitor.getIndexValues(new QueryLiteral()));

        mockController.verify();
    }

    @Test
    public void testTwoProjectsNoModification() throws Exception
    {
        final List<QueryLiteral> literals = Collections.singletonList(createLiteral("HSP"));
        final MockProject project1 = new MockProject(10000L);
        final MockProject project2 = new MockProject(20000L);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final IndexInfoResolver<Project> projectIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(projectIndexInfoResolver.getIndexedValues("HSP")).andReturn(CollectionBuilder.newBuilder("10000", "20000").asList());

        final NameResolver<Project> projectResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(projectResolver.get(10000L)).andReturn(project1);
        EasyMock.expect(projectResolver.get(20000L)).andReturn(project2);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).andReturn(true);

        ProjectLiteralSanitiser sanitiser = mockController.instantiate(ProjectLiteralSanitiser.class);

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(literals);
        assertFalse(result.isModified());

        mockController.verify();
    }

    @Test
    public void testOneProjectModification() throws Exception
    {
        final List<QueryLiteral> literals = Collections.singletonList(createLiteral("HSP"));
        final QueryLiteral expectedLiteral = createLiteral(10000L);
        final MockProject project1 = new MockProject(10000L);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final IndexInfoResolver<Project> projectIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(projectIndexInfoResolver.getIndexedValues("HSP")).andReturn(Collections.singletonList("10000"));

        final NameResolver<Project> projectResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(projectResolver.get(10000L)).andReturn(project1);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).andReturn(false);

        ProjectLiteralSanitiser sanitiser = mockController.instantiate(ProjectLiteralSanitiser.class);

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(literals);
        assertTrue(result.isModified());
        assertEquals(1, result.getLiterals().size());
        assertEquals(expectedLiteral, result.getLiterals().get(0));

        mockController.verify();
    }

    @Test
    public void testTwoProjectsOneModification() throws Exception
    {
        final QueryLiteral expectedLiteral1 = createLiteral("HSP");
        final QueryLiteral expectedLiteral2 = createLiteral(20000L);
        final List<QueryLiteral> inputLiterals = Collections.singletonList(expectedLiteral1);
        final List<QueryLiteral> expectedLiterals = CollectionBuilder.newBuilder(expectedLiteral1, expectedLiteral2).asList();
        final MockProject project1 = new MockProject(10000L);
        final MockProject project2 = new MockProject(20000L);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final IndexInfoResolver<Project> projectIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(projectIndexInfoResolver.getIndexedValues("HSP")).andReturn(CollectionBuilder.newBuilder("10000", "20000").asList());

        final NameResolver<Project> projectResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(projectResolver.get(10000L)).andReturn(project1);
        EasyMock.expect(projectResolver.get(20000L)).andReturn(project2);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).andReturn(false);

        ProjectLiteralSanitiser sanitiser = mockController.instantiate(ProjectLiteralSanitiser.class);

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(inputLiterals);
        assertTrue(result.isModified());
        assertEquals(expectedLiterals, result.getLiterals());

        mockController.verify();
    }

    @Test
    public void testTwoProjectsModification() throws Exception
    {
        final List<QueryLiteral> inputLiterals = Collections.singletonList(createLiteral("HSP"));
        final List<QueryLiteral> expectedLiterals = CollectionBuilder.newBuilder(createLiteral(10000L), createLiteral(20000L)).asList();
        final MockProject project1 = new MockProject(10000L);
        final MockProject project2 = new MockProject(20000L);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final IndexInfoResolver<Project> projectIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(projectIndexInfoResolver.getIndexedValues("HSP")).andReturn(CollectionBuilder.newBuilder("10000", "20000").asList());

        final NameResolver<Project> projectResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(projectResolver.get(10000L)).andReturn(project1);
        EasyMock.expect(projectResolver.get(20000L)).andReturn(project2);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).andReturn(false);

        ProjectLiteralSanitiser sanitiser = mockController.instantiate(ProjectLiteralSanitiser.class);

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(inputLiterals);
        assertTrue(result.isModified());
        assertEquals(expectedLiterals, result.getLiterals());

        mockController.verify();
    }

    @Test
    public void testTwoProjectsOneDoesNotExist() throws Exception
    {
        final List<QueryLiteral> inputLiterals = Collections.singletonList(createLiteral("HSP"));
        final List<QueryLiteral> expectedLiterals = CollectionBuilder.newBuilder(createLiteral("HSP"), createLiteral(20000L)).asList();
        final MockProject project2 = new MockProject(20000L);

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final IndexInfoResolver<Project> projectIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(projectIndexInfoResolver.getIndexedValues("HSP")).andReturn(CollectionBuilder.newBuilder("10000", "20000").asList());

        final NameResolver<Project> projectResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(projectResolver.get(10000L)).andReturn(null);
        EasyMock.expect(projectResolver.get(20000L)).andReturn(project2);

        final PermissionManager permissionManager = mockController.getMock(PermissionManager.class);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).andReturn(false);

        ProjectLiteralSanitiser sanitiser = mockController.instantiate(ProjectLiteralSanitiser.class);

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(inputLiterals);
        assertTrue(result.isModified());
        assertEquals(expectedLiterals, result.getLiterals());

        mockController.verify();
    }

    @Test
    public void testTwoProjectsTwoDoesNotExist() throws Exception
    {
        final List<QueryLiteral> inputLiterals = Collections.singletonList(createLiteral("HSP"));

        mockController.addObjectInstance(MockJqlOperandResolver.createSimpleSupport());

        final IndexInfoResolver<Project> projectIndexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(projectIndexInfoResolver.getIndexedValues("HSP")).andReturn(CollectionBuilder.newBuilder("10000", "20000").asList());

        final NameResolver<Project> projectResolver = mockController.getMock(NameResolver.class);
        EasyMock.expect(projectResolver.get(10000L)).andReturn(null);
        EasyMock.expect(projectResolver.get(20000L)).andReturn(null);

        ProjectLiteralSanitiser sanitiser = mockController.instantiate(ProjectLiteralSanitiser.class);

        final LiteralSanitiser.Result result = sanitiser.sanitiseLiterals(inputLiterals);
        assertFalse(result.isModified());

        mockController.verify();
    }
}
