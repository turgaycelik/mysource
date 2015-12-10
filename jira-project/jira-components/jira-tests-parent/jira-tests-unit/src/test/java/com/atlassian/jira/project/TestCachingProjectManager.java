/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.association.NodeAssociationStoreImpl;
import com.atlassian.jira.bc.project.component.DefaultProjectComponentManager;
import com.atlassian.jira.bc.project.component.OfBizProjectComponentStore;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.MockEventPublisher;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.util.ProjectKeyStoreImpl;
import com.atlassian.jira.user.util.MockUserManager;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class TestCachingProjectManager
{
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private ProjectManager projectManagerMock;

    @AvailableInContainer
    private final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    @AvailableInContainer
    ProjectComponentManager projectComponentManager = new DefaultProjectComponentManager(
        new OfBizProjectComponentStore(ofBizDelegator), null, new MockEventPublisher(), new MockUserManager(), null);

    @AvailableInContainer
    NodeAssociationStore nodeAssocStore = new NodeAssociationStoreImpl(ofBizDelegator);

    @AvailableInContainer
    ProjectFactory projectFactory = new DefaultProjectFactory();

    private CachingProjectManager testedManager;

    private GenericValue project1;
    private GenericValue project2;
    private GenericValue project3;
    public GenericValue projectCat;
    private GenericValue projectCat2;
    private ProjectComponent component1;
    private ProjectComponent component2;

    public TestCachingProjectManager()
    {
    }

    @Before
    public void setUp() throws Exception
    {

        testedManager = new CachingProjectManager(projectManagerMock, projectComponentManager,
            projectFactory, null, null, new ProjectKeyStoreImpl(ofBizDelegator), new MemoryCacheManager(), nodeAssocStore);

        UtilsForTests.getTestEntity("ProjectKey", ImmutableMap.of("projectId", new Long(10), "projectKey", "ABC-123"));
        UtilsForTests.getTestEntity("ProjectKey", ImmutableMap.of("projectId", new Long(11), "projectKey", "ABC-124"));
        UtilsForTests.getTestEntity("ProjectKey", ImmutableMap.of("projectId", new Long(11), "projectKey", "OLDKEY"));
        UtilsForTests.getTestEntity("ProjectKey", ImmutableMap.of("projectId", new Long(12), "projectKey", "CURRENT"));
        project1 = UtilsForTests.getTestEntity("Project", ImmutableMap.of("id", new Long(10), "key", "ABC-123", "name", "This Project", "counter", new Long(10)));
        project2 = UtilsForTests.getTestEntity("Project", ImmutableMap.of("id", new Long(11), "key", "ABC-124", "name", "This Project 2", "counter", new Long(10)));
        project3 = UtilsForTests.getTestEntity("Project", ImmutableMap.of("id", new Long(12), "key", "CURRENT", "name", "This Project 3", "counter", new Long(10)));
        UtilsForTests.getTestEntity("Version", ImmutableMap.of("id", new Long(20), "name", "ver1", "project", new Long(10), "released", "true", "sequence", new Long(1)));
        UtilsForTests.getTestEntity("Version", ImmutableMap.of("id", new Long(21), "name", "ver2", "project", new Long(10), "released", "true", "sequence", new Long(2)));
        UtilsForTests.getTestEntity("Version", ImmutableMap.of("id", new Long(22), "name", "ver3", "project", new Long(10), "sequence", new Long(3)));
        projectCat = UtilsForTests.getTestEntity("ProjectCategory", ImmutableMap.of("id", new Long(30), "name", "cat1", "description", "cat1Description"));
        projectCat2 = UtilsForTests.getTestEntity("ProjectCategory", ImmutableMap.of("id", new Long(31), "name", "cat2", "description", "cat2Description"));

        final ProjectComponentManager pcm = ComponentAccessor.getComponent(ProjectComponentManager.class);
        component1 = pcm.create("com1", null, null, 0, 10L);
        component2 = pcm.create("com2", null, null, 0, 10L);

        testedManager.updateCache();

        // mocking
        when(projectManagerMock.getProjects()).thenReturn(projects());
    }

    @Test
    public void testUpdateCache() throws GenericEntityException
    {
        GenericValue oldProject = testedManager.getProject(project1.getLong("id"));
        testedManager.updateCache();

        GenericValue newProject = testedManager.getProject(project1.getLong("id"));
        assertEquals(oldProject, newProject);
        oldProject = testedManager.getProject(project1.getLong("id"));
        testedManager.refresh();
        newProject = testedManager.getProject(project1.getLong("id"));

        testedManager.getNextId(testedManager.getProjectObj(project1.getLong("id")));
        verify(projectManagerMock).getNextId(any(Project.class));
    }

    @Test
    public void testCachingProjectManager() throws GenericEntityException
    {
        final GenericValue oldProject = testedManager.getProject(project1.getLong("id"));
        final Collection<GenericValue> oldComponents = testedManager.getComponents(oldProject);
        assertThat(oldComponents, hasItems(component1.getGenericValue(), component2.getGenericValue()));

        final GenericValue newProject = testedManager.getProject(project1.getLong("id"));
        assertEquals(oldProject, newProject);

        final Collection newComponents = testedManager.getComponents(newProject);
        assertEquals(oldComponents, newComponents);
    }

    @Test
    public void testGetProject() throws GenericEntityException
    {
        final GenericValue project = testedManager.getProject(new Long(10));

        assertThat(project, is(testedManager.getProject(new Long(10))));
        assertThat(project, is(testedManager.getProjectByKey("ABC-123")));
        assertThat(project, is(testedManager.getProjectByName("This Project")));
    }

    @Test
    public void testGetComponents() throws GenericEntityException
    {
        final Collection<GenericValue> oldComponents = testedManager.getComponents(project1);
        assertNotNull(oldComponents);
        assertEquals(2, oldComponents.size());

        GenericValue oldComponent = testedManager.getComponent(component1.getId());
        assertThat(oldComponents, hasItems(oldComponent));
        oldComponent = testedManager.getComponent(component2.getId());
        assertThat(oldComponents, hasItems(oldComponent));
    }

    @Test
    public void testGetProjects() throws GenericEntityException
    {
        final Collection<GenericValue> projects = testedManager.getProjects();
        assertNotNull(projects);
        assertThat(projects, IsCollectionWithSize.hasSize(3));
        assertThat(projects, hasItems(project1, project2));
    }

    @Test
    public void testGetProjectCategories() throws GenericEntityException
    {
        final Collection<GenericValue> projectCategories = testedManager.getProjectCategories();
        assertNotNull(projectCategories);
        assertEquals(2, projectCategories.size());
        assertThat(projectCategories, hasItems(projectCat));
    }

    @Test
    public void testNotNull()
    {
        assertEquals(Collections.<Object>emptyList(), CachingProjectManager.<Object>noNull(null));
    }

    @Test
    public void testGetProjectCategory() throws GenericEntityException
    {
        assertEquals(projectCat, testedManager.getProjectCategory(new Long(30)));
    }

    @Test
    public void testGetProjectCategoryFromProject() throws GenericEntityException
    {
        // null project id
        GenericValue projectCategory = testedManager.getProjectCategoryFromProject(null);
        assertNull(projectCategory);

        //valid project id but no association set
        projectCategory = testedManager.getProjectCategoryFromProject(project1);
        assertNull(projectCategory);

        //valid project id and association exists.. return the projectCategory
        ComponentAccessor.getComponent(NodeAssociationStore.class).createAssociation(project1, projectCat, ProjectRelationConstants.PROJECT_CATEGORY);
        testedManager.refresh();
        projectCategory = testedManager.getProjectCategoryFromProject(project1);
        assertEquals(projectCat, projectCategory);
    }

    @Test
    public void testGetProjectsFromProjectCategory() throws GenericEntityException
    {
        // test null projectCategory id
        Collection<GenericValue> projects = testedManager.getProjectsFromProjectCategory((GenericValue) null);
        assertThat(projects, Matchers.<GenericValue>emptyIterable());

        // test a valid projectCategory id associated with NO projects
        projects = testedManager.getProjectsFromProjectCategory(projectCat);
        assertThat(projects, Matchers.<GenericValue>emptyIterable());

        // test a valid projectCategory associated with a project
        testedManager.setProjectCategory(project1, projectCat);

        verify(projectManagerMock).setProjectCategory(eq(project1), eq(projectCat));;
    }

    @Test
    public void testGetProjectObjectsFromProjectCategory() throws GenericEntityException
    {
        // test null projectCategory id
        Collection<Project> projects = testedManager.getProjectObjectsFromProjectCategory(null);
        assertThat(projects, Matchers.<Project>emptyIterable());

        // test a valid projectCategory id associated with NO projects
        projects = testedManager.getProjectObjectsFromProjectCategory(30L);
        assertThat(projects, Matchers.<Project>emptyIterable());

        // test a valid projectCategory associated with a project
        testedManager.setProjectCategory(project1, projectCat);

        verify(projectManagerMock).setProjectCategory(eq(project1), eq(projectCat));
    }

    @Test
    public void testGetProjectObj()
    {
        final ProjectManager pm = testedManager;

        // non-existing project - ID is null - not a requirement at the moment
        Project project = pm.getProjectObj(null);
        assertNull(project);

        // non-existing project
        project = pm.getProjectObj(new Long(666));
        assertNull(project);

        // existing project
        final Long projectId1 = project1.getLong("id");
        project = pm.getProjectObj(projectId1);
        assertNotNull(project);
        assertEquals(projectId1, project.getId());

        final Long projectId2 = project2.getLong("id");
        project = pm.getProjectObj(projectId2);
        assertNotNull(project);
        assertEquals(projectId2, project.getId());
    }

    @Test
    public void testGetAllProjectObjects() throws Exception
    {
        final ProjectManager pm = testedManager;
        final Collection<Project> projects = pm.getProjectObjects();
        assertNotNull(projects);
        assertThat(projects, IsCollectionWithSize.hasSize(3));
        final Collection<Long> foundProjectIds = Collections2.transform(projects, new Function<Project, Long>(){
            @Override
            public Long apply(final Project p)
            {
                return p.getId();
            }
        });
        assertThat(foundProjectIds, hasItems(10L, 11L));
    }

    @Test
    public void testGetProjectCount()
    {
        final ProjectManager pm = testedManager;
        assertEquals(3, pm.getProjectCount());

        for (final Project project : pm.getProjectObjects())
        {
            pm.removeProject(project);
        }
        verify(projectManagerMock, times(3)).removeProject(isA(Project.class));
    }

    @Test
    public void testGetProjectByCurrentKey() throws GenericEntityException {

        assertThat("Should not match project by old key.", testedManager.getProjectByCurrentKeyIgnoreCase("oldkey"), IsNull.<Project>nullValue());
        assertThat("Should not match project by old key.", testedManager.getProjectByCurrentKey("OLDKEY"), IsNull.<Project>nullValue());

        assertThat(Preconditions.checkNotNull(testedManager.getProjectObjByKey("OLDKEY")).getKey(), equalTo("ABC-124"));
        assertThat(Preconditions.checkNotNull(testedManager.getProjectObjByKeyIgnoreCase("oldkey")).getKey(), equalTo("ABC-124"));

        assertThat(Preconditions.checkNotNull(testedManager.getProjectByCurrentKey("CURRENT")).getName(), equalTo("This Project 3"));

        assertThat(Preconditions.checkNotNull(testedManager.getProjectByCurrentKeyIgnoreCase("currENT")).getKey(), equalTo("CURRENT"));
        assertThat(Preconditions.checkNotNull(testedManager.getProjectByCurrentKeyIgnoreCase("currENT")).getName(), equalTo("This Project 3"));
    }

    private Collection<GenericValue> projects()
    {
        return Lists.newArrayList(project1, project2, project3);
    }

}
