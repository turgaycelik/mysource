package com.atlassian.jira.bc.project.component;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.cache.CacheManager;
import com.atlassian.jira.bc.EntityNotFoundException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ProjectComponentManagerCachedTester extends ProjectComponentManagerTester
{

    private ProjectComponentStore realStore;
    private ProjectComponentStore cacheStore;

    public ProjectComponentManagerCachedTester(ProjectComponentStore store, final CacheManager cacheManager, final ClusterLockService clusterLockService) throws EntityNotFoundException
    {
        super(new CachingProjectComponentStore(store, cacheManager, clusterLockService));
        realStore = store;
        cacheStore = this.store;
    }

    public void testFind()
    {
        try
        {
            managerEnt.find(COMPONENT_STORED.getId());
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

        try
        {
            // verify the component is not found nether in the cache nor in the store
            // ID is random number
            managerEnt.find(new Long(123));
            fail();
        }
        catch (EntityNotFoundException e)
        {
        }

        try
        {
            // add component to the store directly avoiding the cache
            MutableProjectComponent pc = new MutableProjectComponent(null, "Test", "java rulez", null, 0, new Long(20));
            realStore.store(pc);
            try
            {
                // verify the component if found even if not cached
                managerEnt.find(pc.getId());
            }
            catch (EntityNotFoundException e)
            {
                fail();
            }
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }

    }

    public void testCreate1()
    {
        try
        {
            // add component to the store directly avoiding the cache
            MutableProjectComponent pc1 = new MutableProjectComponent(null, "Test", "java rulez", null, 0, new Long(20));
            MutableProjectComponent pc2 = new MutableProjectComponent(null, "Test", "java rulez", null, 0, new Long(20));
            realStore.store(pc1);
            realStore.store(pc2);
            fail();
        }
        catch (IllegalArgumentException e)
        {
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
    }

    public void testUpdate()
    {
        ProjectComponent pc = null;
        try
        {
            // Create component - also added to cache
            pc = managerEnt.create("Test", "desc", null, 0, PROJECT_ID_STORED);
            try
            {
                // Delete component - removed from cache
                managerEnt.delete(pc.getId());
                assertNotNull(pc.getId());
            }
            catch (EntityNotFoundException e)
            {
                fail();
            }
            try
            {
                // Attempt to update the deleted component directly in the store
                MutableProjectComponent mpc = new MutableProjectComponent(pc.getId(), "Test", "desc", null, 0, PROJECT_ID_STORED);
                cacheStore.store(mpc);
                fail();
            }
            catch (EntityNotFoundException e)
            {
            }
        }
        catch (IllegalArgumentException ex)
        {
            fail();
        }

        try
        {
            // Create component - also added to cache
            pc = managerEnt.create("Test", "desc", null, 0, PROJECT_ID_STORED);
            managerEnt.create("Test Again", "desc", null, 0, PROJECT_ID_STORED);

            try
            {
                // Attempt to update the component with a non-unique name
                MutableProjectComponent mpc = new MutableProjectComponent(pc.getId(), "Test Again", "desc", null, 0, PROJECT_ID_STORED);
                cacheStore.store(mpc);
                fail();
            }
            catch (IllegalArgumentException e)
            {
                // Name is not unique - so exception should be thrown
            }
            catch (EntityNotFoundException e)
            {
                fail();
            }
        }
        catch (IllegalArgumentException ex)
        {
            fail();
        }
    }

}
