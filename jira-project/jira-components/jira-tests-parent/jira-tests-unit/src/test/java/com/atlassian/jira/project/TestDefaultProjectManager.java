/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntity;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.association.NodeAssociationStoreImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.transaction.MockTransactionSupport;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

@SuppressWarnings("deprecation")
public class TestDefaultProjectManager
{

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private UserManager userManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private IssueSecurityLevelManager issueSecurityLevelManager;

    private final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
    private final NodeAssociationStore nodeAssociationStore = new NodeAssociationStoreImpl(ofBizDelegator);

    @Mock
    private ProjectFactory projectFactory;

    @Mock
    private IssueManager issueManager;

    private ProjectManager testedObject;

    private Project project1, project2;
    private GenericValue projectGV1;

    private GenericValue projectCategory, projectCategory2;

    private final UserMockFactory userMockFactory = new UserMockFactory();
    private final ProjectMockFactory projectMockFactory = new ProjectMockFactory();
    private final ComponentMockFactory componentMockFactory = new ComponentMockFactory();

    private final class ProjectMock extends ProjectImpl
    {

        public ProjectMock(final GenericValue projectGv)
        {
            super(projectGv);
        }

        @Override
        public Long getCounter()
        {
            return testedObject.getCurrentCounterForProject(getId());
        }

        @Override
        public GenericValue getProjectCategory()
        {
            return testedObject.getProjectCategoryFromProject(getGenericValue());
        }
    }

    private class UserMockFactory
    {

        private User projectLead;
        private User componentLead;

        public User getProjectLead()
        {
            if (projectLead == null)
            {
                projectLead = new MockUser("project-lead");
                final ApplicationUser projetLeadAppUser = ApplicationUsers.from(projectLead);
                when(userManager.getUserByName(projectLead.getName())).thenReturn(projetLeadAppUser);
                when(userManager.getUserByKey(projectLead.getName())).thenReturn(projetLeadAppUser);
            }
            return projectLead;
        }

        public User getComponentLead()
        {
            if (componentLead == null)
            {
                componentLead = new MockUser("component-lead");
                final ApplicationUser componentLeadAppUser = ApplicationUsers.from(componentLead);
                when(userManager.getUserByName(componentLead.getName())).thenReturn(componentLeadAppUser);
                when(userManager.getUserByKey(componentLead.getName())).thenReturn(componentLeadAppUser);
            }
            return componentLead;
        }

    }

    private class ProjectMockFactory
    {

        private GenericValue projectWithDefaultAssigneeLead;
        private GenericValue projectWithDefaultUnassigned;

        public GenericValue getProjectWithDefaultAssigneeLead()
        {
            if (projectWithDefaultAssigneeLead == null)
            {
                projectWithDefaultAssigneeLead = UtilsForTests.getTestEntity("Project", ImmutableMap.of("name", "projectWithAssigneeLead",
                        "key", "DAL", "lead", userMockFactory.getProjectLead().getName(), "assigneetype", new Long(
                                ProjectAssigneeTypes.PROJECT_LEAD)));
            }
            return projectWithDefaultAssigneeLead;
        }

        public GenericValue getProjectWithDefaultUnassigned()
        {
            if (projectWithDefaultUnassigned == null)
            {
                projectWithDefaultUnassigned = UtilsForTests.getTestEntity("Project", ImmutableMap.of("name",
                        "projectWithDefaultUnassigned", "key", "DUL", "assigneetype", new Long(ProjectAssigneeTypes.UNASSIGNED)));
            }
            return projectWithDefaultUnassigned;
        }

    }

    private class ComponentMockFactory
    {

        private GenericValue componentWithProjectLeadAssignee;
        private GenericValue componentWithProjectDefaultAssignee;
        private GenericValue componentWithComponentAssignee;
        private GenericValue componentWithComponentUnassigned;
        private GenericValue componentWithProjectDefaultUnassigned;

        public GenericValue getComponentWithProjectLeadAssignee()
        {
            if (componentWithProjectLeadAssignee == null)
            {
                componentWithProjectLeadAssignee = UtilsForTests.getTestEntity("Component", ImmutableMap.of("name",
                        "componentWithProjectLeadAssignee", "project",
                        projectMockFactory.getProjectWithDefaultAssigneeLead().getLong("id"), "assigneetype", new Long(
                                ComponentAssigneeTypes.PROJECT_LEAD)));
            }
            return componentWithProjectLeadAssignee;
        }

        public GenericValue getComponentWithProjectDefaultAssignee()
        {
            if (componentWithProjectDefaultAssignee == null)
            {
                componentWithProjectDefaultAssignee = UtilsForTests.getTestEntity("Component", ImmutableMap.of("name",
                        "componentWithProjectDefaultAssignee", "project",
                        projectMockFactory.getProjectWithDefaultAssigneeLead().getLong("id"), "assigneetype", new Long(
                                ComponentAssigneeTypes.PROJECT_DEFAULT)));
            }
            return componentWithProjectDefaultAssignee;
        }

        public GenericValue getComponentWithComponentAssignee()
        {
            if (componentWithComponentAssignee == null)
            {
                componentWithComponentAssignee = UtilsForTests.getTestEntity("Component", ImmutableMap.of("name",
                        "componentWithComponentAssignee", "project", projectMockFactory.getProjectWithDefaultUnassigned().getLong("id"),
                        "lead", userMockFactory.getComponentLead().getName().toLowerCase(), "assigneetype", new Long(
                                ComponentAssigneeTypes.COMPONENT_LEAD)));
            }
            return componentWithComponentAssignee;
        }

        public GenericValue getComponentWithComponentUnassigned()
        {
            if (componentWithComponentUnassigned == null)
            {
                componentWithComponentUnassigned = UtilsForTests.getTestEntity("Component", ImmutableMap.of("name",
                        "componentWithComponentUnassigned", "project", projectMockFactory.getProjectWithDefaultUnassigned().getLong("id"),
                        "assigneetype", new Long(ComponentAssigneeTypes.UNASSIGNED)));
            }
            return componentWithComponentUnassigned;
        }

        public GenericValue getComponentWithProjectDefaultUnassigned()
        {
            if (componentWithProjectDefaultUnassigned == null)
            {
                componentWithProjectDefaultUnassigned = UtilsForTests.getTestEntity("Component", ImmutableMap.of("name",
                        "componentWithProjectDefaultUnassigned", "project",
                        projectMockFactory.getProjectWithDefaultUnassigned().getLong("id"), "assigneetype", new Long(
                                ComponentAssigneeTypes.PROJECT_DEFAULT)));
            }
            return componentWithProjectDefaultUnassigned;
        }

    }

    @Before
    public void setUp() throws Exception
    {
        final MockitoContainer mockitoContainer = MockitoMocksInContainer.rule(mockitoMocksInContainer);

        testedObject = new DefaultProjectManager(ofBizDelegator, nodeAssociationStore, projectFactory, mock(ProjectRoleManager.class),
                issueManager, mock(AvatarManager.class), userManager, mock(ProjectCategoryStore.class), ComponentAccessor.getApplicationProperties(),
                mock(ProjectKeyStore.class), new MockTransactionSupport(), mock(PropertiesManager.class),
                mock(JsonEntityPropertyManager.class), mock(EventPublisher.class));

        mockitoContainer.getMockWorker()
                .addMock(OfBizDelegator.class, ofBizDelegator)
                .addMock(UserManager.class, userManager)
                .addMock(PermissionManager.class, permissionManager)
                .addMock(IssueSecurityLevelManager.class, issueSecurityLevelManager)
                .addMock(ProjectManager.class, testedObject)
                .init();

        when(projectFactory.getProject(Mockito.<GenericValue> any())).thenAnswer(new Answer<Project>()
        {

            @Override
            public Project answer(final InvocationOnMock invocation) throws Throwable
            {
                return new ProjectMock((GenericValue) invocation.getArguments()[0]);
            }

        });
        when(projectFactory.getProjects(Mockito.<Collection<GenericValue>> any())).thenAnswer(new Answer<List<Project>>()
        {

            @Override
            public List<Project> answer(final InvocationOnMock invocation) throws Throwable
            {
                final List<Project> result = new LinkedList<Project>();
                @SuppressWarnings("unchecked")
                final Collection<GenericValue> projectGVs = (Collection<GenericValue>) invocation.getArguments()[0];
                for (final GenericValue projectGV : projectGVs)
                {
                    result.add(new ProjectMock(projectGV));
                }
                return result;
            }

        });

        project1 = addProject(Long.valueOf(100), "ABC", "Project 1", Long.valueOf(100));
        projectGV1 = ofBizDelegator.findById("Project", project1.getId());
        project2 = addProject(Long.valueOf(101), "XYZ", "Project 2", Long.valueOf(101));

        addIssue(project1, Long.valueOf(99));
        addIssue(project1, Long.valueOf(100));
        addIssue(project1, Long.valueOf(101));
        addIssue(project1, Long.valueOf(102));

        projectCategory = addProjectCategory(Long.valueOf(30), "foo", "bar");
        projectCategory2 = addProjectCategory(Long.valueOf(31), "bib", "la");
    }

    @Test
    public void testGetProjectObj()
    {
        // non-existing project - ID is null - not a requirement at the moment
        Project project = testedObject.getProjectObj(null);
        assertNull(project);

        // non-existing project
        project = testedObject.getProjectObj(new Long(666));
        assertNull(project);

        // existing project
        project = testedObject.getProjectObj(project1.getId());
        assertEquals(project1, project);

        project = testedObject.getProjectObj(project2.getId());
        assertEquals(project2, project);
    }

    @Test
    public void testGetNextIdWithDuplicateKey() throws GenericEntityException
    {
        // Set up
        /**
         * project (ABC) next counter = 101 existing issue keys: ABC-99, ABC-100, ABC-101, ABC-102 getNextId() should skip counters 101 and
         * 102 as they are already associated with existing issues
         */
        assertEquals(Long.valueOf(100), project1.getCounter()); // ensure that the counter starts where we think

        // Invoke and check
        assertEquals(103, testedObject.getNextId(project1));
        assertEquals(104, testedObject.getNextId(project1));
        assertEquals(105, testedObject.getNextId(project1));

        project1 = testedObject.getProjectObj(project1.getId());
        assertEquals(105, project1.getCounter().longValue()); // ensure that the counter is incremented properly
        projectGV1 = ofBizDelegator.findById("Project", project1.getId());
        assertEquals(projectGV1, testedObject.getProjectByKey("ABC"));
        assertEquals(projectGV1, testedObject.getProjectByName("Project 1"));
    }

    @Test
    public void testUpdateProject()
    {
        testedObject.updateProject(testedObject.getProjectObjByKey("ABC"), "Snookums", "Snook snooky", "vez", null,
                AssigneeTypes.PROJECT_LEAD);
        final Project project = testedObject.getProjectObjByKey("ABC");

        assertEquals("ABC", project.getKey());
        assertEquals("Snookums", project.getName());
        assertEquals("Snook snooky", project.getDescription());
        assertEquals("vez", project.getLeadUserKey());
        assertEquals(new Long(2), project.getAssigneeType());
        assertEquals(null, project.getUrl());

        assertNotNull(testedObject.getProjectObjByName("Snookums"));
        assertNotNull(testedObject.getProjectObjByKey("ABC"));
    }

    @Test
    public void testGetProjects() throws GenericEntityException
    {
        final List<Project> projects = testedObject.getProjectObjects();
        assertEquals(2, projects.size());
        assertEquals(projects.get(0), project1);
        assertEquals(projects.get(1), project2);
    }

    @Test
    public void testUpdateProjectCategory() throws GenericEntityException
    {
        projectCategory.set("name", "A New Name");
        projectCategory.set("description", "A New Description");
        testedObject.updateProjectCategory(projectCategory);

        final GenericValue retrievedProjectCat = testedObject.getProjectCategory(projectCategory.getLong("id"));
        assertEquals("A New Name", retrievedProjectCat.getString("name"));
        assertEquals("A New Description", retrievedProjectCat.getString("description"));
    }

    @Test
    public void testGetProjectCategoryFromProject() throws GenericEntityException
    {
        // null project id
        GenericValue actualProjectCategory = testedObject.getProjectCategoryFromProject(null);
        assertNull(actualProjectCategory);

        // valid project id but no association set
        projectCategory = testedObject.getProjectCategoryFromProject(projectGV1);
        assertNull(actualProjectCategory);

        // valid project id and association exists.. return the projectCategory
        actualProjectCategory = testedObject.getProjectCategoryFromProject(projectGV1);
        assertEquals(projectCategory, actualProjectCategory);
    }

    @Test
    public void testGetProjectsFromProjectCategory() throws GenericEntityException
    {
        // test null projectCategory id
        Collection<GenericValue> projects = testedObject.getProjectsFromProjectCategory((GenericValue) null);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory id associated with NO projects
        projects = testedObject.getProjectsFromProjectCategory(projectCategory);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory associated with a project
        testedObject.setProjectCategory(projectGV1, projectCategory);
        projects = testedObject.getProjectsFromProjectCategory(projectCategory);
        assertEquals(Collections.singletonList(projectGV1), projects);
    }

    @Test
    public void testGetProjectObjectsFromProjectCategory() throws GenericEntityException
    {
        // test null projectCategory id
        Collection<Project> projects = testedObject.getProjectObjectsFromProjectCategory(null);
        assertTrue(projects.isEmpty());

        // test a valid projectCategory id associated with NO projects
        projects = testedObject.getProjectObjectsFromProjectCategory(projectCategory.getLong("id"));
        assertTrue(projects.isEmpty());

        // test a valid projectCategory associated with a project
        testedObject.setProjectCategory(projectGV1, projectCategory);
        projects = testedObject.getProjectObjectsFromProjectCategory(projectCategory.getLong("id"));
        final Project project = Iterables.getOnlyElement(projects);
        assertEquals(project1.getId(), project.getId());
        assertEquals(projectCategory, project.getProjectCategory());
    }

    @Test
    public void testSetProjectCategoryNotNull() throws GenericEntityException
    {
        // test null project
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot associate a category with a null project");
        testedObject.setProjectCategory((GenericValue) null, null);
    }

    @Test
    public void testSetProjectCategory() throws GenericEntityException
    {
        // test setting up a relation with a project that has no categories
        assertNull(testedObject.getProjectCategoryFromProject(null));
        testedObject.setProjectCategory(projectGV1, projectCategory);
        assertEquals(projectCategory, testedObject.getProjectCategoryFromProject(projectGV1));
        assertEquals(1, nodeAssociationStore.getSinksFromSource(projectGV1, "ProjectCategory", ProjectRelationConstants.PROJECT_CATEGORY)
                .size());

        // test setting up a relation with a project that has one category already
        testedObject.setProjectCategory(projectGV1, projectCategory2);
        assertEquals(projectCategory2, testedObject.getProjectCategoryFromProject(projectGV1));
        assertEquals(1, nodeAssociationStore.getSinksFromSource(projectGV1, "ProjectCategory", ProjectRelationConstants.PROJECT_CATEGORY)
                .size());

        // test setting up a relation with a null category (ie no project category)
        testedObject.setProjectCategory(project1, null);
        assertEquals(null, testedObject.getProjectCategoryFromProject(projectGV1));
        assertEquals(0, nodeAssociationStore.getSinksFromSource(projectGV1, "ProjectCategory", ProjectRelationConstants.PROJECT_CATEGORY)
                .size());
    }

    @Test
    public void testDefaultAssigneeWithNoUnassigned() throws DefaultAssigneeException, OperationNotPermittedException,
            InvalidUserException, InvalidCredentialException
    {
        final User projectLead = userMockFactory.getProjectLead();
        final GenericValue projectWithDefaultAssigneeLead = projectMockFactory.getProjectWithDefaultAssigneeLead();

        // Should be false as project lead cannot be assigned issues.
        _testNoDefaultAssignee(projectWithDefaultAssigneeLead, null);

        when(permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, projectWithDefaultAssigneeLead, projectLead)).thenReturn(
                Boolean.TRUE);

        // Should be true as project lead can be assigned issues.
        _testDefaultAssignee(projectWithDefaultAssigneeLead, null, projectLead);
    }

    @Test
    public void testDefaultAssigneeWithUnassigned() throws DefaultAssigneeException, GenericEntityException,
            OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User projectLead = userMockFactory.getProjectLead();
        final GenericValue projectWithDefaultAssigneeLead = projectMockFactory.getProjectWithDefaultAssigneeLead();
        final GenericValue projectWithDefaultUnassigned = projectMockFactory.getProjectWithDefaultUnassigned();

        // Should be false as unassigned is turned off and project lead cannot be assigned issues.
        _testNoDefaultAssignee(projectWithDefaultUnassigned, null);

        when(permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, projectWithDefaultAssigneeLead, projectLead)).thenReturn(
                Boolean.TRUE);
        when(permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, projectWithDefaultUnassigned, projectLead)).thenReturn(
                Boolean.TRUE);

        // Should be false as unassigned is turned off and the lead is null so it fails
        _testNoDefaultAssignee(projectWithDefaultUnassigned, null);

        projectWithDefaultUnassigned.set("lead", projectLead.getName());
        projectWithDefaultUnassigned.store();

        // Should be true as unassigned is turned off and project lead can be assigned issues,
        // so it defaults to project lead.
        assertTrue(testedObject.isDefaultAssignee(projectWithDefaultUnassigned, null));

        final User defaultAssignee = testedObject.getDefaultAssignee(projectWithDefaultUnassigned, null);
        assertEquals(projectLead, defaultAssignee);

        // Turn on unassigned
        ComponentAccessor.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);

        // Reset permissions

        // Should be true as unassigned is turned on
        _testDefaultAssignee(projectWithDefaultUnassigned, null, null);
    }

    @Test
    public void testDefaultAssigneeProjectLead() throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User projectLead = userMockFactory.getProjectLead();
        final GenericValue projectWithDefaultAssigneeLead = projectMockFactory.getProjectWithDefaultAssigneeLead();
        final GenericValue componentWithProjectLeadAssignee = componentMockFactory.getComponentWithProjectLeadAssignee();

        // Should return false as project lead is unassignable
        _testNoDefaultAssignee(projectWithDefaultAssigneeLead, componentWithProjectLeadAssignee);

        when(permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, projectWithDefaultAssigneeLead, projectLead)).thenReturn(
                Boolean.TRUE);

        // Should return true as project lead is assignable
        _testDefaultAssignee(projectWithDefaultAssigneeLead, componentWithProjectLeadAssignee, projectLead);
    }

    @Test
    public void testDefaultAssigneeProjectDefault() throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User projectLead = userMockFactory.getProjectLead();
        final GenericValue projectWithDefaultAssigneeLead = projectMockFactory.getProjectWithDefaultAssigneeLead();
        final GenericValue componentWithProjectDefaultAssignee = componentMockFactory.getComponentWithProjectDefaultAssignee();

        // Should return false as project lead is unassignable and component's default assignee is the project default
        _testNoDefaultAssignee(projectWithDefaultAssigneeLead, componentWithProjectDefaultAssignee);

        when(permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, projectWithDefaultAssigneeLead, projectLead)).thenReturn(
                Boolean.TRUE);

        // Should return true as project lead is assignable and component's default assignee is the project default
        _testDefaultAssignee(projectWithDefaultAssigneeLead, componentWithProjectDefaultAssignee, projectLead);
    }

    @Test
    public void testDefaultAssigneeComponentLead() throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User componentLead = userMockFactory.getComponentLead();
        final GenericValue projectWithDefaultUnassigned = projectMockFactory.getProjectWithDefaultUnassigned();
        final GenericValue componentWithComponentAssignee = componentMockFactory.getComponentWithComponentAssignee();

        // Should return false as components lead is unassignable, unassigned is turned off and project lead is unassignable
        _testNoDefaultAssignee(projectWithDefaultUnassigned, componentWithComponentAssignee);

        when(
                permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, projectFactory.getProject(projectWithDefaultUnassigned),
                        ApplicationUsers.from(componentLead))).thenReturn(Boolean.TRUE);

        // Should return true as component lead is assignable
        _testDefaultAssignee(projectWithDefaultUnassigned, componentWithComponentAssignee, componentLead);
    }

    @Test
    public void testDeafultAssigneeComponentUnassigned() throws OperationNotPermittedException, InvalidUserException,
            InvalidCredentialException
    {
        final GenericValue projectWithDefaultUnassigned = projectMockFactory.getProjectWithDefaultUnassigned();
        final GenericValue componentWithComponentUnassigned = componentMockFactory.getComponentWithComponentUnassigned();

        // Should return false as unassigned is NOT allowed and component's and project's default assignee are set to
        // unassigned
        _testNoDefaultAssignee(projectWithDefaultUnassigned, componentWithComponentUnassigned);

        // Turn on unassigned allowed
        ComponentAccessor.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);

        // Should return true as unassigned is turnned ON
        _testDefaultAssignee(projectWithDefaultUnassigned, componentWithComponentUnassigned, null);
    }

    @Test
    public void testDefaultAssigneeProjectDefaultUnassigned() throws OperationNotPermittedException, InvalidUserException,
            InvalidCredentialException
    {
        final GenericValue projectWithDefaultUnassigned = projectMockFactory.getProjectWithDefaultUnassigned();
        final GenericValue componentWithProjectDefaultUnassigned = componentMockFactory.getComponentWithProjectDefaultUnassigned();
        final GenericValue componentWithComponentUnassigned = componentMockFactory.getComponentWithComponentUnassigned();

        // Should return false as unassigned is NOT allowed and components default assignee is set to
        // project's default (which is unassigned)
        _testNoDefaultAssignee(projectWithDefaultUnassigned, componentWithProjectDefaultUnassigned);

        // Turn on unassigned allowed
        ComponentAccessor.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);

        // Should return true as unassigned is turnned ON!!! yippeeeee (spelled owen's way)
        _testDefaultAssignee(projectWithDefaultUnassigned, componentWithComponentUnassigned, null);
    }

    @Test
    public void testGetProjectByLead() throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final GenericEntity projectWithDefaultAssigneeLead = projectMockFactory.getProjectWithDefaultAssigneeLead();

        // Now test that we can retrieve the project with lead
        final Collection<Project> projectsWithLead = testedObject.getProjectsLeadBy(userMockFactory.getProjectLead());
        assertEquals(1, projectsWithLead.size());
        assertEquals("projectWithAssigneeLead", projectWithDefaultAssigneeLead.getString("name"));
    }

    @Test
    public void testGetProjectByLeadWhereNoProjectsExistForLead() throws OperationNotPermittedException, InvalidUserException,
            InvalidCredentialException
    {
        final Collection<Project> projectsWithoutLead = testedObject.getProjectsLeadBy(userMockFactory.getComponentLead());
        assertEquals(0, projectsWithoutLead.size());
    }

    private void _testNoDefaultAssignee(final GenericValue project, final GenericValue component)
    {
        assertFalse(testedObject.isDefaultAssignee(project, component));
        try {
            testedObject.getDefaultAssignee(project, component);
            fail("Expected DefaultAssigneeException");
        } catch (final DefaultAssigneeException e) {
            assertEquals("The default assignee does NOT have ASSIGNABLE permission OR Unassigned issues are turned off.", e.getMessage());
        }
    }

    private void _testDefaultAssignee(final GenericValue project, final GenericValue component, final User expectedLead)
    {
        assertTrue(testedObject.isDefaultAssignee(project, component));
        final User defaultAssignee = testedObject.getDefaultAssignee(project, component);
        assertEquals(expectedLead, defaultAssignee);
    }

    private Project addProject(final long id, final String key, final String name, final long counter)
    {
        final GenericValue result = UtilsForTests.getTestEntity("Project",
                ImmutableMap.of("id", id, "key", key, "name", name, "counter", counter));
        UtilsForTests.getTestEntity("ProjectKey", ImmutableMap.of("projectId", id, "projectKey", key));
        return new ProjectMock(result);
    }

    private Issue addIssue(final Project project, final long number) throws Exception
    {
        final Issue result = mock(Issue.class);
        when(result.getProjectObject()).thenReturn(project);
        when(result.getNumber()).thenReturn(number);

        UtilsForTests.getTestEntity("Issue", ImmutableMap.of("number", result.getNumber(), "project", result.getProjectObject().getId()));
        when(issueManager.isExistingIssueKey(IssueKey.format(project, result.getNumber()))).thenReturn(Boolean.TRUE);

        return result;
    }

    private GenericValue addProjectCategory(final long id, final String name, final String description)
    {
        return UtilsForTests.getTestEntity("ProjectCategory", ImmutableMap.of("id", id, "name", name, "description", description));
    }

}
