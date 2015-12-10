package com.atlassian.jira.security.roles;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("UnusedParameters")
public class TestCachingProjectRoleAndActorStoreForConcurrency
{
    private static final Set<User> NO_USERS = ImmutableSet.of();

    public static void main(final String[] args) throws Exception
    {
        final ExecutorService executorService = Executors.newFixedThreadPool(100);
        try
        {
            final ExecutorCompletionService<Void> completionService = new ExecutorCompletionService<Void>(executorService);
            final int iterations = 100000;
            for (int i = 0; i < iterations; i++)
            {
                completionService.submit(new Runnable()
                {
                    public void run()
                    {
                        new TestCachingProjectRoleAndActorStoreForConcurrency().testConcurrentProjectRoleActorUpdateDoesntInvalidateCache();
                    }
                }, null);
            }
            for (int i = 0; i < iterations; i++)
            {
                completionService.take().get();
            }
        }
        finally
        {
            executorService.shutdown();
        }
    }

    /**
     * test that multiple clients hitting the getAllProjectRoles() method concurrently do not bloat the cache with
     * duplicates.
     */
    @Test
    public void testGetAllProjectRolesRaceCondition() throws Exception
    {
        final List<ProjectRole> allRoles = MockProjectRoleManager.DEFAULT_ROLE_TYPES;
        final int WORKERS = 10;
        final AtomicInteger allProjectsCalledCount = new AtomicInteger(0);

        // ducktype mock implements part of ProjectRoleAndActorStore
        final Object mock = new Object()
        {
            @SuppressWarnings("unused")
            public Collection<ProjectRole> getAllProjectRoles()
            {
                allProjectsCalledCount.incrementAndGet();
                try
                {
                    Thread.sleep(20);
                }
                catch (final InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                return allRoles;
            }
        };
        final ProjectRoleAndActorStore slowProjectRoleAndActorStore = (ProjectRoleAndActorStore)DuckTypeProxy.getProxy(
                ProjectRoleAndActorStore.class,
                ImmutableList.of(mock));

        final CachingProjectRoleAndActorStore cachingProjectRoleAndActorStore = new CachingProjectRoleAndActorStore(
                slowProjectRoleAndActorStore,
                new MockProjectRoleManager.MockRoleActorFactory(),
                new MemoryCacheManager());

        final AtomicInteger calledCount = new AtomicInteger(0);
        final CountDownLatch workersRunning = new CountDownLatch(WORKERS);
        final CountDownLatch workersRan = new CountDownLatch(WORKERS);

        final ExecutorService pool = Executors.newCachedThreadPool();
        try
        {
            // Using the latch to make certain that all threads meet where we want them to.
            // This allows us to force all the threads into the code at a point in which they could possibly be
            // populating the cache with duplicates. As the cache uses locks to ensure the delegate doesn't get called
            // multiple times, we can't block inside the delegate (called method).
            for (int i = 0; i < WORKERS; i++)
            {
                pool.submit(new Runnable()
                {
                    public void run()
                    {
                        workersRunning.countDown();
                        try
                        {
                            workersRunning.await(10, TimeUnit.SECONDS);
                        }
                        catch (final InterruptedException e)
                        {
                            throw new RuntimeException(e);
                        }
                        cachingProjectRoleAndActorStore.getAllProjectRoles();
                        calledCount.getAndIncrement();
                        workersRan.countDown();
                    }
                });
            }

            workersRan.await(10, TimeUnit.SECONDS);
            assertEquals(WORKERS, calledCount.get());

            assertEquals(3, cachingProjectRoleAndActorStore.getAllProjectRoles().size());
        }
        finally
        {
            pool.shutdown();
        }
    }

    /**
     * makes sure an iterator on the Collection returned by getAllProjectRoles will not throw CCME even if the
     * underlying collection is concurrently modified.
     */
    @Test
    public void testGetAllProjectRolesDoesntThrowConcurrentMod() throws Exception
    {
        final List<ProjectRole> allRoles = MockProjectRoleManager.DEFAULT_ROLE_TYPES;
        @SuppressWarnings("unused")
        final Object mock = new Object()
        {
            public Collection<ProjectRole> getAllProjectRoles()
            {
                return allRoles;
            }
        };
        final ProjectRoleAndActorStore slowProjectRoleAndActorStore = (ProjectRoleAndActorStore) DuckTypeProxy.getProxy(
                ProjectRoleAndActorStore.class,
                ImmutableList.of(mock));

        final CachingProjectRoleAndActorStore cachingProjectRoleAndActorStore = new CachingProjectRoleAndActorStore(slowProjectRoleAndActorStore,
            new MockProjectRoleManager.MockRoleActorFactory(), new MemoryCacheManager());

        final Iterator<ProjectRole> it = cachingProjectRoleAndActorStore.getAllProjectRoles().iterator();
        it.next();

        cachingProjectRoleAndActorStore.clearCaches();

        it.next();
    }

    /**
     * makes sure an iterator on the Collection returned by getAllProjectRoles will not throw CCME even if the
     * underlying collection is concurrently modified.
     */
    @Test
    public void testRemovingRoleDoesntCauseExistingRoleListToThrowConcurrentModSingleThreaded() throws Exception
    {
        final List<ProjectRole> allRoles = MockProjectRoleManager.DEFAULT_ROLE_TYPES;
        @SuppressWarnings("unused")
        final Object mock = new Object()
        {
            public Collection<ProjectRole> getAllProjectRoles()
            {
                return allRoles;
            }

            public void deleteProjectRole(final ProjectRole projectRole)
            {}
        };
        final ProjectRoleAndActorStore slowProjectRoleAndActorStore = (ProjectRoleAndActorStore) DuckTypeProxy.getProxy(
                ProjectRoleAndActorStore.class,
                ImmutableList.of(mock));

        final CachingProjectRoleAndActorStore cachingProjectRoleAndActorStore = new CachingProjectRoleAndActorStore(slowProjectRoleAndActorStore,
            new MockProjectRoleManager.MockRoleActorFactory(), new MemoryCacheManager());

        final Iterator<ProjectRole> it = cachingProjectRoleAndActorStore.getAllProjectRoles().iterator();
        it.next();
        cachingProjectRoleAndActorStore.deleteProjectRole(MockProjectRoleManager.PROJECT_ROLE_TYPE_3);
        it.next();
    }

    /**
     * makes sure an iterator on the Collection returned by getAllProjectRoles will not throw CCME even if the
     * underlying collection is concurrently modified by another thread.
     */
    @Test
    public void testRemovingRoleDoesntCauseExistingRoleListToThrowConcurrentModMultiThread() throws Exception
    {
        final List<ProjectRole> allRoles = MockProjectRoleManager.DEFAULT_ROLE_TYPES;
        @SuppressWarnings("unused")
        final Object mock = new Object()
        {
            public Collection<ProjectRole> getAllProjectRoles()
            {
                return allRoles;
            }

            public void deleteProjectRole(final ProjectRole projectRole)
            {}
        };
        final ProjectRoleAndActorStore slowProjectRoleAndActorStore = (ProjectRoleAndActorStore)DuckTypeProxy.getProxy(
                ProjectRoleAndActorStore.class,
                ImmutableList.of(mock));

        final CachingProjectRoleAndActorStore cachingProjectRoleAndActorStore = new CachingProjectRoleAndActorStore(
                slowProjectRoleAndActorStore,
                new MockProjectRoleManager.MockRoleActorFactory(),
                new MemoryCacheManager());

        final Iterator<ProjectRole> it = cachingProjectRoleAndActorStore.getAllProjectRoles().iterator();
        final ExecutorService pool = Executors.newCachedThreadPool();
        try
        {
            final Future<?> future = pool.submit(new Runnable()
            {
                public void run()
                {
                    cachingProjectRoleAndActorStore.deleteProjectRole(MockProjectRoleManager.PROJECT_ROLE_TYPE_3);
                }
            });
            assertNull(future.get());

            it.next();
            it.next();

            assertNull(future.get());
        }
        finally
        {
            pool.shutdown();
        }
    }

    /**
     * makes sure an iterator on the Collection returned by getAllProjectRoles will not throw CCME even if the
     * underlying collection is concurrently modified.
     */
    @Test
    public void testAddingRoleDoesntCauseExistingRoleListToThrowConcurrentModSingleThread() throws Exception
    {
        final List<ProjectRole> allRoles = MockProjectRoleManager.DEFAULT_ROLE_TYPES;

        @SuppressWarnings("unused")
        final Object mock = new Object()
        {
            public Collection<ProjectRole> getAllProjectRoles()
            {
                return allRoles;
            }

            public ProjectRole addProjectRole(final ProjectRole projectRole)
            {
                return projectRole;
            }
        };

        final ProjectRoleAndActorStore slowProjectRoleAndActorStore = (ProjectRoleAndActorStore)DuckTypeProxy.getProxy(
                ProjectRoleAndActorStore.class,
                ImmutableList.of(mock));

        final CachingProjectRoleAndActorStore cachingProjectRoleAndActorStore = new CachingProjectRoleAndActorStore(
                slowProjectRoleAndActorStore,
                new MockProjectRoleManager.MockRoleActorFactory(),
                new MemoryCacheManager());

        final Iterator<ProjectRole> it = cachingProjectRoleAndActorStore.getAllProjectRoles().iterator();
        it.next();
        cachingProjectRoleAndActorStore.addProjectRole(MockProjectRoleManager.PROJECT_ROLE_TYPE_3);
        // should not throw ConcurrentMod
        it.next();
    }

    /**
     * makes sure an iterator on the Collection returned by getAllProjectRoles will not throw CCME even if the
     * underlying collection is concurrently modified by another thread.
     */
    @Test
    public void testAddingRoleDoesntCauseExistingRoleListToThrowConcurrentModMultiThread() throws Exception
    {
        final List<ProjectRole> allRoles = MockProjectRoleManager.DEFAULT_ROLE_TYPES;

        @SuppressWarnings("unused")
        final Object mock = new Object()
        {
            public Collection<ProjectRole> getAllProjectRoles()
            {
                return allRoles;
            }

            public void deleteProjectRole(final ProjectRole projectRole)
            {}
        };
        final ProjectRoleAndActorStore mockProjectRoleAndActorStore = (ProjectRoleAndActorStore) DuckTypeProxy.getProxy(
                ProjectRoleAndActorStore.class,
                ImmutableList.of(mock));

        final CachingProjectRoleAndActorStore cachingProjectRoleAndActorStore = new CachingProjectRoleAndActorStore(
                mockProjectRoleAndActorStore,
                new MockProjectRoleManager.MockRoleActorFactory(),
                new MemoryCacheManager());

        final Iterator<ProjectRole> it = cachingProjectRoleAndActorStore.getAllProjectRoles().iterator();
        final ExecutorService pool = Executors.newCachedThreadPool();
        try
        {
            final Future<?> future = pool.submit(new Runnable()
            {
                public void run()
                {
                    cachingProjectRoleAndActorStore.deleteProjectRole(MockProjectRoleManager.PROJECT_ROLE_TYPE_3);
                }
            });

            // make sure the mod has actually happened in the other thread
            assertNull(future.get());

            it.next();
            it.next();

            // make sure there weren't any exceptions in the other thread
            assertNull(future.get());
        }
        finally
        {
            pool.shutdown();
        }
    }

    /**
     * makes sure an iterator on the Collection returned by getAllProjectRoles will not throw CCME even if the
     * underlying collection is concurrently modified.
     */
    @Test
    public void testCacheClearingDoesntCauseExistingRoleListToThrowConcurrentMod() throws Exception
    {
        final List<ProjectRole> allRoles = MockProjectRoleManager.DEFAULT_ROLE_TYPES;
        @SuppressWarnings("unused")
        final Object mock = new Object()
        {
            public Collection<ProjectRole> getAllProjectRoles()
            {
                return allRoles;
            }
        };
        final ProjectRoleAndActorStore slowProjectRoleAndActorStore = (ProjectRoleAndActorStore) DuckTypeProxy.getProxy(
                ProjectRoleAndActorStore.class,
                ImmutableList.of(mock));

        final CachingProjectRoleAndActorStore cachingProjectRoleAndActorStore = new CachingProjectRoleAndActorStore(slowProjectRoleAndActorStore,
            new MockProjectRoleManager.MockRoleActorFactory(), new MemoryCacheManager());

        final Iterator<ProjectRole> it = cachingProjectRoleAndActorStore.getAllProjectRoles().iterator();
        it.next();
        cachingProjectRoleAndActorStore.clearCaches();
        it.next();
    }

    /**
     * Test that the ProjectRoleActors cached for a project/projectRole combination are correct when one thread calls
     * get, then another calls update and the update returns before the get. In this situation it might be possible for
     * the result of the get to be stale and overwrite the correct value in the cache. This test verifies that the
     * update beats the get in this situation.
     */
    @Test
    public void testConcurrentProjectRoleActorUpdateDoesntInvalidateCache()
    {
        try
        {
            internalTest();
        }
        catch (RoleActorDoesNotExistException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public void internalTest() throws RoleActorDoesNotExistException
    {
        final Project mockProject = new MockProject(1L, "TST", "Test");
        final Set<RoleActor> actors = newHashSet();
        final Long roleId = MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getId();
        actors.add(new MockProjectRoleManager.MockRoleActor(1L, roleId, null, NO_USERS, UserRoleActorFactory.TYPE, "testuser"));

        // stale data should not be cached
        final Long projectId = mockProject.getId();
        final ProjectRoleActors firstProjectRoleActorsWithUsers = new ProjectRoleActorsImpl(projectId, roleId, actors);
        try
        {
            actors.add(new MockProjectRoleManager.MockRoleActor(2L, roleId, null, NO_USERS, UserRoleActorFactory.TYPE, "fred"));
        }
        catch (RoleActorDoesNotExistException e)
        {
            throw new RuntimeException(e);
        }
        // good data, we want to see this even though the bad one is returned by the delegate.get and returns AFTER the update returns
        final ProjectRoleActors updatedProjectRoleActorsWithUsers = new ProjectRoleActorsImpl(projectId, roleId, newHashSet(actors));

        // we need the first get to be called before, but complete after the update call
        final CountDownLatch waitUntilUpdateComplete = new CountDownLatch(1);
        // guarantee that get is processing
        final CountDownLatch waitUntilGetCalled = new CountDownLatch(1);

        // make sure that subsequent calls after the update will hit the cache (ie. update SHOULD NOT invalidate the cache)
        final AtomicBoolean firstCallToGetProjectRoleActors = new AtomicBoolean(true);

        @SuppressWarnings("unused")
        final Object mock = new Object()
        {
            private ProjectRoleActors myRoleActors = firstProjectRoleActorsWithUsers;
            public ProjectRoleActors getProjectRoleActors(final Long projectRoleId, final Long projectId)
            {
                // we should only be called once, and return AFTER the update has completed and therefore NOT cache this result
                assertTrue(firstCallToGetProjectRoleActors.get());
                waitUntilGetCalled.countDown();
                try
                {
                    if (!waitUntilUpdateComplete.await(10, TimeUnit.SECONDS))
                    {
                        throw new RuntimeException("Timed out waitingUntilUpdateComplete.");
                    }
                }
                catch (final InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                assertTrue("noone else called me in the meantime", firstCallToGetProjectRoleActors.compareAndSet(true, false));
                try
                {
                    // now that we have waited for the delegate.update method to complete, give a little time for the
                    // cache to be tested/updated
                    Thread.sleep(1);
                }
                catch (final InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
                return myRoleActors;
            }

            public void updateProjectRoleActors(final ProjectRoleActors projectRoleActors)
            {
                try
                {
                    // just in case we get really lucky and return before the call to cache.get has even started...
                    if (!waitUntilGetCalled.await(10, TimeUnit.SECONDS))
                    {
                        throw new RuntimeException("Timed out waitingUntilGetCalled.");
                    }
                     myRoleActors = projectRoleActors;
                }
                catch (final InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

        final ProjectRoleAndActorStore mockProjectRoleAndActorStore = (ProjectRoleAndActorStore) DuckTypeProxy.getProxy(
                ProjectRoleAndActorStore.class,
                ImmutableList.of(mock));

        final CachingProjectRoleAndActorStore cachingProjectRoleAndActorStore = new CachingProjectRoleAndActorStore(mockProjectRoleAndActorStore,
            new MockProjectRoleManager.MockRoleActorFactory(), new MemoryCacheManager());

        final CountDownLatch getCalledBeforeUpdate = new CountDownLatch(1);
        // Thread responsible for invoking the get
        final ExecutorService pool = Executors.newCachedThreadPool();
        try
        {
            final Future<?> firstGet = pool.submit(new Runnable()
            {
                public void run()
                {
                    getCalledBeforeUpdate.countDown();
                    // this should always return two!!!
                    final ProjectRoleActors roleActors = cachingProjectRoleAndActorStore.getProjectRoleActors(
                            MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getId(),
                            mockProject.getId());
                    final Set<RoleActor> set = roleActors.getRoleActors();
                    assertEquals(set.toString(), 2, set.size());
                }
            });

            // Thread responsible for invoking the update
            final Future<?> updater = pool.submit(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        if (!getCalledBeforeUpdate.await(10, TimeUnit.SECONDS))
                        {
                            throw new RuntimeException("timed out waiting for get to be called");
                        }
                        //                        Thread.sleep(10);
                    }
                    catch (final InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                    // Call the get, the call through to the backing store should return before the update is processed
                    cachingProjectRoleAndActorStore.updateProjectRoleActors(updatedProjectRoleActorsWithUsers);
                    waitUntilUpdateComplete.countDown();
                }
            });

            // make sure nothing bad happened in the clients and wait for them to complete...
            try
            {
                firstGet.get();
                updater.get();
            }
            catch (final InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            catch (final ExecutionException e)
            {
                throw new RuntimeException(e);
            }

            // The value we get from the get call should always be the value set by the update call
            final ProjectRoleActors projectRoleActors = cachingProjectRoleAndActorStore.getProjectRoleActors(
                    MockProjectRoleManager.PROJECT_ROLE_TYPE_1.getId(),
                    mockProject.getId());
            assertEquals(2, projectRoleActors.getRoleActors().size());
        }
        finally
        {
            pool.shutdown();
        }
    }
}
