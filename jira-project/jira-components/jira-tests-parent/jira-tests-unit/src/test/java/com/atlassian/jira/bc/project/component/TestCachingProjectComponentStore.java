package com.atlassian.jira.bc.project.component;

import java.util.Arrays;
import java.util.List;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 *
 */
public class TestCachingProjectComponentStore extends TestOfBizProjectComponentStore
{
    protected ProjectComponentStore createStore(MockOfBizDelegator ofBizDelegator)
    {
        ProjectManager projectManager = Mockito.mock(ProjectManager.class);
        final MockProject projecta = new MockProject(ProjectComponentStoreTester.PROJECT_ID_1, "PRA");
        final MockProject projectb = new MockProject(ProjectComponentStoreTester.PROJECT_ID_2, "PRB");
        final List<Project> projects = Arrays.asList(new Project[] {projecta, projectb});
        when(projectManager.getProjectObjects()).thenReturn(projects);

        new MockComponentWorker()
                    .addMock(ProjectManager.class, projectManager)
                    .init();

        final CachingProjectComponentStore projectComponentStore = new CachingProjectComponentStore(new OfBizProjectComponentStore(ofBizDelegator), getCacheManager(), getClusterLockService());
        projectComponentStore.start();
        return projectComponentStore;
    }

    private ClusterLockService getClusterLockService()
    {
        return new SimpleClusterLockService();
    }

    private MemoryCacheManager getCacheManager()
    {
        return new MemoryCacheManager();
    }

    @After
    public void tearDown()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testUpdate()
    {
        ProjectComponentStore store = createStore(new MockOfBizDelegator(null, null));

        MutableProjectComponent component1 = new MutableProjectComponent(null, "name1", "desc - 1", null, 0, ProjectComponentStoreTester.PROJECT_ID_1);
        MutableProjectComponent component2 = new MutableProjectComponent(null, "name2", "desc - 2", null, 0, ProjectComponentStoreTester.PROJECT_ID_1);

        // test that the cached store does not allow duplicate component name per project in insert operation
        try
        {
            component1 = store.store(component1);
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException e)
        {
            fail();
        }
        // insert valid second component
        try
        {
            component2 = store.store(component2);
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException e)
        {
            fail();
        }

        // test that the cached store does not allow duplicate component name per project in update operation
        try
        {
            component1.setName("name2");
            store.store(component1);
            fail();
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // duplicate name per project not alowed
        }
    }


}
