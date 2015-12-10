package com.atlassian.jira.security.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.CollectionAssert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test the stores ability to convert to proper objects.
 */
public class TestOfBizProjectRoleAndActorStore
{
    private static final List<Long> NO_PROJECTS = ImmutableList.of();
    private static final Set<User> NO_USERS = ImmutableSet.of();
    private static final String DEVS_DESC = "devs desc";
    private static final String DEVS = "devs";
    private static final String NEW_DEVS_DESC = "new devs desc";

    private ProjectRoleAndActorStore projectRoleAndActorStore;
    private static final String TEST_TYPE = "test type";
    private static final String ACTOR_NAME_TEST_1 = "test 1";
    private static final String ACTOR_NAME_TEST_2 = "test 2";
    private GroupManager mockGroupManager;
    final MockUserManager mockUserManager = new MockUserManager();

    @Before
    public void setUp() throws Exception
    {
        CoreTransactionUtil.setUseTransactions(false);
        mockGroupManager = new MockGroupManager();
        new MockComponentWorker().init()
                .addMock(UserManager.class, mockUserManager)
                .addMock(OfBizDelegator.class, new MockOfBizDelegator());
        projectRoleAndActorStore = new OfBizProjectRoleAndActorStore(ComponentAccessor.getOfBizDelegator(),
                new MockProjectRoleManager.MockRoleActorFactory(), mockGroupManager);
    }

    @After
    public void tearDown() throws Exception
    {
        CoreTransactionUtil.setUseTransactions(true);
    }

    @Test
    public void testSimpleProjectRole()
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        // Test the getAll with no values in db
        assertEquals(0, projectRoleAndActorStore.getAllProjectRoles().size());

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);
        assertEquals(DEVS, developers.getName());
        assertEquals(DEVS_DESC, developers.getDescription());
        assertNotNull(developers.getId());
        // Now test the getAll

        ArrayList<ProjectRole> allProjectRoles = newArrayList(projectRoleAndActorStore.getAllProjectRoles());
        assertEquals(1, allProjectRoles.size());
        assertNotNull(allProjectRoles.get(0));

        // Now test an update
        developers = new ProjectRoleImpl(developers.getId(), "blah", NEW_DEVS_DESC);
        projectRoleAndActorStore.updateProjectRole(developers);

        // Implicitly test the get method
        developers = projectRoleAndActorStore.getProjectRole(developers.getId());

        assertEquals(NEW_DEVS_DESC, developers.getDescription());

        // Now test a delete
        projectRoleAndActorStore.deleteProjectRole(developers);

        developers = projectRoleAndActorStore.getProjectRole(developers.getId());

        assertNull(developers);
    }

    @Test
    public void testRoleActorPersistence() throws RoleActorDoesNotExistException
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        Project project = new MockProject(1, "TST");

        Set<RoleActor> roleActors1 = newHashSet();
        final Long roleId = developers.getId();
        final Long projectId = project.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(1L, roleId, projectId, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(2L, roleId, projectId, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_2);
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(3L, roleId, projectId, NO_USERS, TEST_TYPE, "test 3");

        roleActors1.add(actor1);
        roleActors1.add(actor2);

        Set<RoleActor> roleActors2 = newHashSet();
        roleActors2.add(actor1);
        roleActors2.add(actor3);

        ProjectRoleActors projectRoleActors = new ProjectRoleActorsImpl(projectId, roleId, roleActors1);
        projectRoleAndActorStore.updateProjectRoleActors(projectRoleActors);

        projectRoleActors = projectRoleAndActorStore.getProjectRoleActors(roleId, projectId);

        CollectionAssert.assertContainsExactly(roleActors1, projectRoleActors.getRoleActors());
        assertEquals(projectId, projectRoleActors.getProjectId());
        assertEquals(roleId, projectRoleActors.getProjectRoleId());

        // Shift our role actor set so that an update will be forced to do an add and remove
        projectRoleActors = (ProjectRoleActors) projectRoleActors.removeRoleActor(actor2);
        projectRoleActors = (ProjectRoleActors) projectRoleActors.addRoleActor(actor3);
        projectRoleAndActorStore.updateProjectRoleActors(projectRoleActors);

        projectRoleActors = projectRoleAndActorStore.getProjectRoleActors(roleId, projectId);

        CollectionAssert.assertContainsExactly(roleActors2, projectRoleActors.getRoleActors());
    }

    @Test
    public void testDefaultRoleActorPersistence() throws RoleActorDoesNotExistException
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        Set<RoleActor> roleActors1 = newHashSet();
        final Long roleId = developers.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(1L, roleId, null, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(2L, roleId, null, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_2);
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(3L, roleId, null, NO_USERS, TEST_TYPE, "test 3");

        roleActors1.add(actor1);
        roleActors1.add(actor2);

        Set<RoleActor> roleActors2 = newHashSet();
        roleActors2.add(actor1);
        roleActors2.add(actor3);

        DefaultRoleActors defaultRoleActors = new DefaultRoleActorsImpl(roleId, roleActors1);
        projectRoleAndActorStore.updateDefaultRoleActors(defaultRoleActors);

        defaultRoleActors = projectRoleAndActorStore.getDefaultRoleActors(roleId);

        CollectionAssert.assertContainsExactly(defaultRoleActors.getRoleActors(), roleActors1);
        assertEquals(roleId, defaultRoleActors.getProjectRoleId());

        // Shift our role actor set so that an update will be forced to do an add and remove
        defaultRoleActors = defaultRoleActors.removeRoleActor(actor2);
        defaultRoleActors = defaultRoleActors.addRoleActor(actor3);
        projectRoleAndActorStore.updateDefaultRoleActors(defaultRoleActors);

        defaultRoleActors = projectRoleAndActorStore.getDefaultRoleActors(roleId);

        assertFalse(defaultRoleActors instanceof ProjectRoleActors);

        CollectionAssert.assertContainsExactly(defaultRoleActors.getRoleActors(), roleActors2);
    }

    @Test
    public void testApplyDefaultsRolesToProject() throws RoleActorDoesNotExistException
    {
        Project project = new MockProject(1, "TST");

        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        Set<RoleActor> roleActors1 = newHashSet();
        final Long roleId = developers.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(1L, roleId, null, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(2L, roleId, null, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_2);

        roleActors1.add(actor1);
        roleActors1.add(actor2);

        DefaultRoleActors defaultRoleActors = new DefaultRoleActorsImpl(roleId, roleActors1);
        projectRoleAndActorStore.updateDefaultRoleActors(defaultRoleActors);

        // Put the defaults into the project we are passing
        projectRoleAndActorStore.applyDefaultsRolesToProject(project);

        for (ProjectRole projectRole : projectRoleAndActorStore.getAllProjectRoles())
        {
            ProjectRoleActors projectRoleActors = projectRoleAndActorStore.getProjectRoleActors(projectRole.getId(), project.getId());
            CollectionAssert.assertContainsExactly(projectRoleActors.getRoleActors(), roleActors1);
        }
    }

    @Test
    public void testRemoveAllRoleActorsByNameAndType() throws RoleActorDoesNotExistException
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        Project project = new MockProject(1, "TST");

        Set<RoleActor> roleActors1 = newHashSet();
        final Long projectId = project.getId();
        final Long roleId = developers.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(1L, roleId, projectId, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(2L, roleId, projectId, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_2);
        roleActors1.add(actor1);
        roleActors1.add(actor2);

        // Also include a default role actor of the same type and name
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(3L, roleId, null, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor4 = new MockProjectRoleManager.MockRoleActor(4L, roleId, null, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_2);
        Set<RoleActor> defaultRoleActors = newHashSet();
        defaultRoleActors.add(actor3);
        defaultRoleActors.add(actor4);

        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(projectId, roleId, roleActors1));
        projectRoleAndActorStore.updateDefaultRoleActors(new DefaultRoleActorsImpl(roleId, defaultRoleActors));

        // make sure that adding worked so we can be sure removing works
        assertEquals(2, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());
        assertEquals(2, projectRoleAndActorStore.getDefaultRoleActors(roleId).getRoleActors().size());

        projectRoleAndActorStore.removeAllRoleActorsByKeyAndType(ACTOR_NAME_TEST_1, TEST_TYPE);

        // after the previous call we should have removed 1 defaultRoleActor and 1 projectRoleActor
        assertEquals(1, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());
        assertEquals(1, projectRoleAndActorStore.getDefaultRoleActors(roleId).getRoleActors().size());
    }

    @Test
    public void testRemoveAllRoleActorsByProject() throws RoleActorDoesNotExistException
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        Project project = new MockProject(1, "TST");

        Set<RoleActor> roleActors1 = newHashSet();
        final Long roleId = developers.getId();
        final Long projectId = project.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(1L, roleId, projectId, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(2L, roleId, projectId, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_2);
        roleActors1.add(actor1);
        roleActors1.add(actor2);

        // Also include a default role actor of the same type and name
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(3L, roleId, null, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_1);
        MockProjectRoleManager.MockRoleActor actor4 = new MockProjectRoleManager.MockRoleActor(4L, roleId, null, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_2);
        Set<RoleActor> defaultRoleActors = newHashSet();
        defaultRoleActors.add(actor3);
        defaultRoleActors.add(actor4);

        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(projectId, roleId, roleActors1));
        projectRoleAndActorStore.updateDefaultRoleActors(new DefaultRoleActorsImpl(roleId, defaultRoleActors));

        // make sure that adding worked so we can be sure removing works
        assertEquals(2, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());
        assertEquals(2, projectRoleAndActorStore.getDefaultRoleActors(roleId).getRoleActors().size());

        projectRoleAndActorStore.removeAllRoleActorsByProject(project);

        // after the previous call we should have removed 1 defaultRoleActor and 1 projectRoleActor
        assertEquals(0, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());
        assertEquals(2, projectRoleAndActorStore.getDefaultRoleActors(roleId).getRoleActors().size());
    }

    @Test
    public void testGetProjectIdsForUserInGroupsBecauseOfRoleAllProjects() throws Exception
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        // Create a user and group and add the user to the group
        User testUser = new MockUser("dude");
        mockUserManager.addUser(testUser);
        Group group = new MockGroup("mygroup");
        mockGroupManager.addUserToGroup(testUser, group);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        Project project = new MockProject(1, "TST");

        Project project2 = new MockProject(2, "ANA");

        Set<RoleActor> roleActors1 = newHashSet();
        // Add a role actor that will match for project 1
        final Long roleId = developers.getId();
        final Long projectId = project.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(1L, roleId, projectId, NO_USERS, GroupRoleActorFactory.TYPE, group.getName());
        // Add a role actor that will not match
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(3L, roleId, projectId, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_2);
        roleActors1.add(actor1);
        roleActors1.add(actor3);
        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(projectId, roleId, roleActors1));

        Set<RoleActor> roleActors2 = newHashSet();
        // Add a role actor that will match for project 2
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(2L, roleId, project2.getId(), NO_USERS, GroupRoleActorFactory.TYPE, group.getName());
        roleActors2.add(actor2);
        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(project2.getId(), roleId, roleActors2));

        // make sure that adding worked so we can be sure finding works
        assertEquals(2, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());

        Map<Long,List<String>> groupsByProject = projectRoleAndActorStore.getProjectIdsForUserInGroupsBecauseOfRole(NO_PROJECTS, developers, GroupRoleActorFactory.TYPE, testUser.getName());

        assertEquals(2, groupsByProject.size());
        assertNotNull(groupsByProject.get(projectId));
        assertNotNull(groupsByProject.get(project2.getId()));
        assertEquals(1, groupsByProject.get(projectId).size());
        assertEquals(group.getName(), (groupsByProject.get(projectId)).get(0));
        assertEquals(1, groupsByProject.get(project2.getId()).size());
        assertEquals(group.getName(), (groupsByProject.get(project2.getId())).get(0));
    }

    @Test
    public void testGetProjectIdsForUserInGroupsBecauseOfRoleOneProject() throws Exception
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        // Create a user and group and add the user to the group
        User testUser = new MockUser("dude");
        mockUserManager.addUser(testUser);
        Group group = new MockGroup("mygroup");
        mockGroupManager.addUserToGroup(testUser, group);

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        Project project = new MockProject(1, "TST");
        Project project2 = new MockProject(2, "ANA");

        Set<RoleActor> roleActors1 = newHashSet();
        // Add a role actor that will match for project 1
        final Long roleId = developers.getId();
        final Long projectId = project.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(1L, roleId, projectId, NO_USERS, GroupRoleActorFactory.TYPE, group.getName());
        // Add a role actor that will not match
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(3L, roleId, projectId, NO_USERS, TEST_TYPE, ACTOR_NAME_TEST_2);
        roleActors1.add(actor1);
        roleActors1.add(actor3);
        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(projectId, roleId, roleActors1));

        Set<RoleActor> roleActors2 = newHashSet();
        // Add a role actor that will match for project 2
        MockProjectRoleManager.MockRoleActor actor2 = new MockProjectRoleManager.MockRoleActor(2L, roleId, project2.getId(), NO_USERS, GroupRoleActorFactory.TYPE, group.getName());
        roleActors2.add(actor2);
        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(project2.getId(), roleId, roleActors2));

        // make sure that adding worked so we can be sure finding works
        assertEquals(2, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());

        // Make sure we limit the query by projects so that we only get one result, even though we could get two if we
        // queried for all.
        Map<Long,List<String>> groupsByProject = projectRoleAndActorStore.getProjectIdsForUserInGroupsBecauseOfRole(ImmutableList.of(projectId), developers, GroupRoleActorFactory.TYPE, testUser.getName());

        assertEquals(1, groupsByProject.size());
        assertNotNull(groupsByProject.get(projectId));
        assertNull(groupsByProject.get(project2.getId()));
        assertEquals(1, groupsByProject.get(projectId).size());
        assertEquals(group.getName(), (groupsByProject.get(projectId)).get(0));
    }

    @Test
    public void testGetProjectIdsForUserInGroupsBecauseOfRoleBatchGroupInClause() throws Exception
    {
        ProjectRole devel = new ProjectRoleImpl(DEVS, DEVS_DESC);

        // Create a user and group and add the user to two groups
        User testUser = new MockUser("dude");
        mockUserManager.addUser(testUser);
        Group group = new MockGroup("mygroup");
        Group group2 = new MockGroup("myothergroup");
        mockGroupManager.addUserToGroup(testUser, group);
        mockGroupManager.addUserToGroup(testUser, group2);

        // Set the batch size to 1 so we force a batch
        ComponentAccessor.getApplicationProperties().setString(APKeys.DATABASE_QUERY_BATCH_SIZE, "1");

        ProjectRole developers = projectRoleAndActorStore.addProjectRole(devel);

        Project project = new MockProject(1, "TST");

        Set<RoleActor> roleActors1 = newHashSet();
        // Add a role actor that will match for project 1
        final Long roleId = developers.getId();
        final Long projectId = project.getId();
        MockProjectRoleManager.MockRoleActor actor1 = new MockProjectRoleManager.MockRoleActor(1L, roleId, projectId, NO_USERS, GroupRoleActorFactory.TYPE, group.getName());
        // Add a role actor that will not match
        MockProjectRoleManager.MockRoleActor actor3 = new MockProjectRoleManager.MockRoleActor(3L, roleId, projectId, NO_USERS, GroupRoleActorFactory.TYPE, group2.getName());
        roleActors1.add(actor1);
        roleActors1.add(actor3);
        projectRoleAndActorStore.updateProjectRoleActors(new ProjectRoleActorsImpl(projectId, roleId, roleActors1));

        // make sure that adding worked so we can be sure finding works
        assertEquals(2, projectRoleAndActorStore.getProjectRoleActors(roleId, projectId).getRoleActors().size());

        Map<Long,List<String>> groupsByProject = projectRoleAndActorStore.getProjectIdsForUserInGroupsBecauseOfRole(NO_PROJECTS, developers, GroupRoleActorFactory.TYPE, testUser.getName());

        assertEquals(1, groupsByProject.size());
        assertNotNull(groupsByProject.get(projectId));
        assertEquals(2, groupsByProject.get(projectId).size());
        List<String> groupNames = groupsByProject.get(projectId);
        assertTrue(groupNames.contains(group.getName()));
        assertTrue(groupNames.contains(group2.getName()));
    }
}
