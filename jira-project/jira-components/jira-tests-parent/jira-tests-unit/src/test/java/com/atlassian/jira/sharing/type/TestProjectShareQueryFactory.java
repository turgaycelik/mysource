package com.atlassian.jira.sharing.type;

import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.search.ProjectShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test for ProjectShareQueryFactory
 *
 * @since v3.13
 */
public class TestProjectShareQueryFactory extends MockControllerTestCase
{
    ApplicationUser user;
    private static final Long PROJECT_ID_123 = new Long(123);
    private static final Long ROLE_ID_456 = new Long(456);
    private static final Project PROJECT_1 = new MockProject(PROJECT_ID_123, "AA", "AA project");
    private static final ProjectRole ROLE_1 = new MockProjectRoleManager.MockProjectRole(ROLE_ID_456.longValue(), "Role1", "Role1Desc");

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("admin");
    }

    private void basicProjectMocks(boolean hasProjectPermission)
    {
        final GenericValue mockProjectGV = new MockGenericValue("Project");
        final List gvList = EasyList.build(mockProjectGV);
        final List projectList = EasyList.build(PROJECT_1);
        final List projectRoleList = EasyList.build(ROLE_1);

        ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        projectManager.getProjects();
        mockController.setReturnValue(gvList);

        ProjectFactory projectFactory = (ProjectFactory) mockController.getMock(ProjectFactory.class);
        projectFactory.getProjects(gvList);
        mockController.setReturnValue(projectList);

        ProjectRoleManager projectRoleManager = (ProjectRoleManager) mockController.getMock(ProjectRoleManager.class);
        projectRoleManager.getProjectRoles(new MockApplicationUser(user.getName(),user.getDisplayName(),user.getEmailAddress()), PROJECT_1);
        mockController.setReturnValue(projectRoleList);

        PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        permissionManager.hasPermission(Permissions.BROWSE, PROJECT_1, user);
        mockController.setReturnValue(hasProjectPermission);
    }

    @Test
    public void testGetTerms()
    {
        basicProjectMocks(true);

        ProjectShareQueryFactory queryFactory = (ProjectShareQueryFactory) mockController.instantiate(ProjectShareQueryFactory.class);
        Term[] terms = queryFactory.getTerms(user);
        assertEquals(2, terms.length);
        assertEquals("shareTypeProject", terms[0].field());
        assertEquals("123:456", terms[0].text());
        assertEquals("shareTypeProject", terms[1].field());
        assertEquals("123", terms[1].text());
    }

    @Test
    public void testGetTermsWithoutProjectPermission()
    {
        basicProjectMocks(false);

        ProjectShareQueryFactory queryFactory = (ProjectShareQueryFactory) mockController.instantiate(ProjectShareQueryFactory.class);
        Term[] terms = queryFactory.getTerms(user);
        assertEquals(1, terms.length);
        assertEquals("shareTypeProject", terms[0].field());
        assertEquals("123:456", terms[0].text());
    }

    @Test
    public void testGetQuery()
    {
        ProjectShareQueryFactory queryFactory = (ProjectShareQueryFactory) mockController.instantiate(ProjectShareQueryFactory.class);

        // search when we have a project and role id
        ShareTypeSearchParameter searchParameter = new ProjectShareTypeSearchParameter(PROJECT_ID_123, ROLE_ID_456);
        Query query = queryFactory.getQuery(searchParameter);
        assertNotNull(query);
        assertEquals("shareTypeProject:123:456", query.toString());

        // search when we have just a project id
        searchParameter = new ProjectShareTypeSearchParameter(PROJECT_ID_123);
        query = queryFactory.getQuery(searchParameter);
        assertNotNull(query);
        assertEquals("shareTypeProject:123 shareTypeProject:123:*", query.toString());
    }

    @Test
    public void testGetQuery_WithUser_ProjectAndRole()
    {
        ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        projectManager.getProjectObj(PROJECT_ID_123);
        mockController.setReturnValue(PROJECT_1);

        PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        permissionManager.hasPermission(Permissions.BROWSE, PROJECT_1, user);
        mockController.setReturnValue(true);

        ProjectRoleManager projectRoleManager = (ProjectRoleManager) mockController.getMock(ProjectRoleManager.class);
        projectRoleManager.getProjectRole(ROLE_ID_456);
        mockController.setReturnValue(ROLE_1);

        projectRoleManager.isUserInProjectRole(new MockApplicationUser(user.getName(),user.getDisplayName(),user.getEmailAddress()), ROLE_1, PROJECT_1);
        mockController.setReturnValue(true);

        ProjectShareQueryFactory queryFactory = (ProjectShareQueryFactory) mockController.instantiate(ProjectShareQueryFactory.class);

        // search when we have a project and role id
        ShareTypeSearchParameter searchParameter = new ProjectShareTypeSearchParameter(PROJECT_ID_123, ROLE_ID_456);
        Query query = queryFactory.getQuery(searchParameter, user);
        assertNotNull(query);
        assertEquals("shareTypeProject:123:456", query.toString());
    }

    @Test
    public void testGetQuery_WithUser_ProjectOnly()
    {
        ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        projectManager.getProjectObj(PROJECT_ID_123);
        mockController.setReturnValue(PROJECT_1);

        PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        permissionManager.hasPermission(Permissions.BROWSE, PROJECT_1, user);
        mockController.setReturnValue(true);

        projectManager.getProjectObj(PROJECT_ID_123);
        mockController.setReturnValue(PROJECT_1);

        ProjectRoleManager projectRoleManager = (ProjectRoleManager) mockController.getMock(ProjectRoleManager.class);
        projectRoleManager.getProjectRoles(new MockApplicationUser(user.getName(),user.getDisplayName(),user.getEmailAddress()), PROJECT_1);
        mockController.setReturnValue(EasyList.build(ROLE_1));

        ProjectShareQueryFactory queryFactory = (ProjectShareQueryFactory) mockController.instantiate(ProjectShareQueryFactory.class);

        // search when we have a project only
        ShareTypeSearchParameter searchParameter = new ProjectShareTypeSearchParameter(PROJECT_ID_123);
        Query query = queryFactory.getQuery(searchParameter, user);
        assertNotNull(query);
        assertEquals("shareTypeProject:123 shareTypeProject:123:456", query.toString());
    }


    @Test
    public void testGetQuery_WithNullUser()
    {
        ProjectManager projectManager = (ProjectManager) mockController.getMock(ProjectManager.class);
        projectManager.getProjectObj(PROJECT_ID_123);
        mockController.setReturnValue(PROJECT_1);
        PermissionManager permissionManager = (PermissionManager) mockController.getMock(PermissionManager.class);
        permissionManager.hasPermission(Permissions.BROWSE, PROJECT_1, (ApplicationUser) null);
        mockController.setReturnValue(false);

        ProjectShareQueryFactory queryFactory = (ProjectShareQueryFactory) mockController.instantiate(ProjectShareQueryFactory.class);

        // search when we have a project and role id
        ShareTypeSearchParameter searchParameter = new ProjectShareTypeSearchParameter(PROJECT_ID_123, ROLE_ID_456);
        Query query = null;
        try
        {
            query = queryFactory.getQuery(searchParameter, (ApplicationUser) null);
            fail("Expected an IllegalStateException");
        }
        catch (IllegalStateException e)
        {
        }
    }

    @Test
    public void testGetField()
    {
        final SharePermissionImpl projectSharePermission = new SharePermissionImpl(new ShareType.Name("project"), String.valueOf(PROJECT_ID_123), String.valueOf(ROLE_ID_456));
        ProjectShareQueryFactory queryFactory = (ProjectShareQueryFactory) mockController.instantiate(ProjectShareQueryFactory.class);
        Field field = queryFactory.getField(null, projectSharePermission);
        assertNotNull(field);
        assertEquals("shareTypeProject", field.name());
        assertTrue(field.isStored());
        assertTrue(field.isIndexed());
        assertEquals("123:456",field.stringValue());
    }
}
