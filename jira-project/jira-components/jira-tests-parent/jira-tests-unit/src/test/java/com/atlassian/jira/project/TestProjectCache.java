/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import java.util.Collection;

import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.project.util.ProjectKeyStoreImpl;
import com.atlassian.jira.transaction.MockTransactionSupport;

import com.google.common.collect.Lists;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestProjectCache
{
    private ProjectCache pCache;
    private GenericValue project1;
    private GenericValue project2;
    public GenericValue projectCat;
    private ProjectKeyStore projectKeyStore;
    private DefaultProjectManager projectManager;
    @AvailableInContainer private OfBizDelegator mockOfBizDelegator = new MockOfBizDelegator(); 
    @Mock @AvailableInContainer private NodeAssociationStore mockNodeAssociationStore;
    @Mock private JsonEntityPropertyManager jsonEntityPropertyManager;
    
    @Rule public RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this); 

    @Before
    public void setUp() throws Exception
    {
        projectKeyStore = new ProjectKeyStoreImpl(mockOfBizDelegator);
        projectManager = new DefaultProjectManager(mockOfBizDelegator, mockNodeAssociationStore, null, null, null,
                null, null, null, null, projectKeyStore, new MockTransactionSupport(), null, jsonEntityPropertyManager, null);
        mockOfBizDelegator.createValue("ProjectKey", FieldMap.build("projectId", 10L, "projectKey", "OLDKEY"));
        mockOfBizDelegator.createValue("ProjectKey", FieldMap.build("projectId", 10L, "projectKey", "ABC-123"));
        mockOfBizDelegator.createValue("ProjectKey", FieldMap.build("projectId", 11L, "projectKey", "ABC-124"));
        project1 = mockOfBizDelegator.createValue("Project", FieldMap.build("id", 10L, "key", "ABC-123", "name", "This Project", "counter", 10L));
        project2 = mockOfBizDelegator.createValue("Project", FieldMap.build("id", 11L, "key", "ABC-124", "name", "This Project 2", "counter", 10L));
        mockOfBizDelegator.createValue("Component", FieldMap.build("id", 10L, "name", "com1", "project", 10L));
        mockOfBizDelegator.createValue("Component", FieldMap.build("id", 11L, "name", "com2", "project", 10L));

        projectCat = mockOfBizDelegator.createValue("ProjectCategory", FieldMap.build("id", 30L, "name", "cat1", "description", "cat1Description"));

        pCache = new ProjectCache(projectManager, projectKeyStore, mockNodeAssociationStore);
    }

    @Test 
    public void testUpdateCache() throws GenericEntityException
    {
        final GenericValue oldProject = pCache.getProject(project1.getLong("id"));
        pCache = new ProjectCache(projectManager, projectKeyStore, mockNodeAssociationStore);

        final GenericValue newProject = pCache.getProject(project1.getLong("id"));
        assertEquals(oldProject, newProject);
    }

    @Test      
    public void testGetProject() throws GenericEntityException
    {
        final GenericValue project = pCache.getProject(10L);
        assertThat(pCache.getProject(10L), sameInstance(project));
        assertThat(pCache.getProjectByKey("ABC-123"), sameInstance(project));
        assertThat(pCache.getProjectByName("This Project"), sameInstance(project));
        assertThat(pCache.getProject(9L), nullValue());
        assertThat(pCache.getProjectByKey("ABC-1231"), nullValue());
        assertThat(pCache.getProjectByName("This Project1"), nullValue());
    }

    @Test      
    public void testGetProjectByPreviousKey() throws GenericEntityException
    {
        final GenericValue project = pCache.getProject(10L);
        assertThat(pCache.getProjectByKey("OLDKEY"), sameInstance(project));
    }

    @Test      
    public void testGetProjects() throws GenericEntityException
    {
        assertThat(pCache.getProjects(), containsInAnyOrder(project1, project2));
    }

    @Test      
    public void testGetProjectCategories()
    {
        assertThat(pCache.getProjectCategories(), contains(projectCat));
    }

    @Test
    public void testRefreshProjectCategoryMaps() throws GenericEntityException
    {
        // want to make sure that the refresh actually does work.
        // on BOTH the maps within the cache;
        // - the one which maps project to projectCategory
        // - and the one which maps projectCategory to projects
        when(mockNodeAssociationStore.getSourcesFromSink(projectCat, "Project", ProjectRelationConstants.PROJECT_CATEGORY)).thenReturn(Lists.newArrayList(project1));
        pCache = new ProjectCache(projectManager, projectKeyStore, mockNodeAssociationStore);
        assertThat(pCache.getProjectsFromProjectCategory(projectCat), contains(project1));
        assertThat(pCache.getProjectCategoryFromProject(project1), equalTo(projectCat));
    }

    /**
     * Test get ProjectCategoriesFromProject
     * Essentially the same test as the one above which checks that the refresh works
     */
    @Test      
    public void testGetProjectsFromProjectCategory() throws GenericEntityException
    {
        assertThat(pCache.getProjectCategoryFromProject(project1), nullValue());
        assertThat(pCache.getProjectsFromProjectCategory(projectCat), Matchers.<GenericValue>emptyIterable());

        when(mockNodeAssociationStore.getSourcesFromSink(projectCat, "Project", ProjectRelationConstants.PROJECT_CATEGORY)).thenReturn(Lists.newArrayList(project1, project2));
        pCache = new ProjectCache(projectManager, projectKeyStore, mockNodeAssociationStore);
        assertEquals(projectCat, pCache.getProjectCategoryFromProject(project1));
        assertEquals(projectCat, pCache.getProjectCategoryFromProject(project2));

        final Collection<GenericValue> projects = pCache.getProjectsFromProjectCategory(projectCat);
        assertThat(projects, containsInAnyOrder(project1, project2));
    }

    @Test
    public void testGetCategory()
    {
        assertEquals(projectCat, pCache.getProjectCategory(30L));
    }

}
