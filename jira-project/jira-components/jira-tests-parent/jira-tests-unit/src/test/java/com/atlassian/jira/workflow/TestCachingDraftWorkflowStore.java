package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class TestCachingDraftWorkflowStore
{
    private static final String WORKFLOW_XML = "<workflow>testXML</workflow>";

    private ApplicationUser testUser;
    private CacheManager cacheManager;

    @Before
    public void init(){
        testUser = new MockApplicationUser("username");
        cacheManager = new MemoryCacheManager();
    }

    @Test
    public void testGetDraftWorkflow()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        mockJiraWorkflow.expectAndReturn("getDescriptor", descriptor);
        final Mock mockDelegate = new Mock(DraftWorkflowStore.class);
        mockDelegate.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("parentWorkflow") }, mockJiraWorkflow.proxy());
        final CachingDraftWorkflowStore cachingDraftWorkflowStore = new CachingDraftWorkflowStore((DraftWorkflowStore) mockDelegate.proxy(), cacheManager)
        {

            @Override
            String convertDescriptorToXML(final WorkflowDescriptor descriptor)
            {
                return WORKFLOW_XML;
            }

            @Override
            WorkflowDescriptor convertXMLtoWorkflowDescriptor(final String parentWorkflowXML) throws FactoryException
            {
                if (!parentWorkflowXML.equals(WORKFLOW_XML))
                {
                    throw new RuntimeException("XML not equal to original XML: '" + parentWorkflowXML + "' vs '" + WORKFLOW_XML + "'");
                }
                return descriptor;
            }

            @Override
            WorkflowManager getWorkflowManager()
            {
                return null;
            }
        };
        final JiraWorkflow jiraWorkflow = cachingDraftWorkflowStore.getDraftWorkflow("parentWorkflow");
        assertNotNull(jiraWorkflow);
        assertEquals(descriptor, jiraWorkflow.getDescriptor());

        mockDelegate.verify();
        mockJiraWorkflow.verify();
    }

    @Test
    public void testGetDraftWorkflowTwice()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        mockJiraWorkflow.expectAndReturn("getDescriptor", descriptor);
        final MockControl mockDraftWorkflowStoreControl = MockControl.createStrictControl(DraftWorkflowStore.class);
        final DraftWorkflowStore mockDraftWorkflowStore = (DraftWorkflowStore) mockDraftWorkflowStoreControl.getMock();

        mockDraftWorkflowStore.getDraftWorkflow("parentWorkflow");
        mockDraftWorkflowStoreControl.setReturnValue(mockJiraWorkflow.proxy(), 1);
        mockDraftWorkflowStoreControl.replay();
        final CachingDraftWorkflowStore cachingDraftWorkflowStore = new CachingDraftWorkflowStore(mockDraftWorkflowStore, cacheManager)
        {

            @Override
            String convertDescriptorToXML(final WorkflowDescriptor descriptor)
            {
                return WORKFLOW_XML;
            }

            @Override
            WorkflowDescriptor convertXMLtoWorkflowDescriptor(final String parentWorkflowXML) throws FactoryException
            {
                if (!parentWorkflowXML.equals(WORKFLOW_XML))
                {
                    throw new RuntimeException("XML not equal to original XML: '" + parentWorkflowXML + "' vs '" + WORKFLOW_XML + "'");
                }
                return descriptor;
            }

            @Override
            WorkflowManager getWorkflowManager()
            {
                return null;
            }
        };
        JiraWorkflow jiraWorkflow = cachingDraftWorkflowStore.getDraftWorkflow("parentWorkflow");
        assertNotNull(jiraWorkflow);
        assertEquals(descriptor, jiraWorkflow.getDescriptor());
        jiraWorkflow = cachingDraftWorkflowStore.getDraftWorkflow("parentWorkflow");
        assertNotNull(jiraWorkflow);
        assertEquals(descriptor, jiraWorkflow.getDescriptor());

        mockDraftWorkflowStoreControl.verify();
        mockJiraWorkflow.verify();
    }

    @Test
    public void testGetDraftWorkflowNullParentWorkflowName()
    {
        final CachingDraftWorkflowStore cachingDraftWorkflowStore = new CachingDraftWorkflowStore(null, cacheManager);
        try
        {
            cachingDraftWorkflowStore.getDraftWorkflow(null);
            fail();
        }
        catch (final IllegalArgumentException e)
        {
            //YAY
        }
    }

    @Test
    public void testGetDraftWorkflowWithNoDraftWorkflowAvailable()
    {
        final Mock mockDelegate = new Mock(DraftWorkflowStore.class);
        mockDelegate.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("parentWorkflow") }, null);
        final CachingDraftWorkflowStore cachingDraftWorkflowStore = new CachingDraftWorkflowStore((DraftWorkflowStore) mockDelegate.proxy(), cacheManager);

        final JiraWorkflow jiraWorkflow = cachingDraftWorkflowStore.getDraftWorkflow("parentWorkflow");
        assertNull(jiraWorkflow);

        mockDelegate.verify();
    }

    @Test
    public void testCreateDraftWorkflow()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        final Mock mockDelegate = new Mock(DraftWorkflowStore.class);
        final JiraWorkflow workflow = (JiraWorkflow) mockJiraWorkflow.proxy();
        mockDelegate.expectAndReturn("createDraftWorkflow", new Constraint[] { P.eq(testUser), P.eq(workflow) }, workflow);

        final CachingDraftWorkflowStore cachingDraftWorkflowStore = new CachingDraftWorkflowStore((DraftWorkflowStore) mockDelegate.proxy(), cacheManager)
        {
            @Override
            String convertDescriptorToXML(final WorkflowDescriptor descriptor)
            {
                return WORKFLOW_XML;
            }
        };

        final JiraWorkflow jiraWorkflow = cachingDraftWorkflowStore.createDraftWorkflow(testUser, workflow);
        assertNotNull(jiraWorkflow);
        assertEquals(jiraWorkflow, workflow);

        mockDelegate.verify();
        mockJiraWorkflow.verify();
    }

    //test ensures that the get will retrieve the workflow from the cache rather than from the delegate after a create.
    @Test
    public void testCreateAndGetDraftWorkflow()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        mockJiraWorkflow.expectAndReturn("getDescriptor", descriptor);
        mockJiraWorkflow.expectAndReturn("getName", "parentWorkflow");
        final Mock mockDelegate = new Mock(DraftWorkflowStore.class);
        mockJiraWorkflow.setStrict(true);
        final JiraWorkflow workflow = (JiraWorkflow) mockJiraWorkflow.proxy();
        mockDelegate.expectAndReturn("createDraftWorkflow", new Constraint[] { P.eq(testUser), P.eq(workflow) }, workflow);
        mockDelegate.expectAndReturn("getDraftWorkflow", new Constraint[] { P.eq("parentWorkflow") }, workflow);
        mockDelegate.setStrict(true);

        final CachingDraftWorkflowStore cachingDraftWorkflowStore = new CachingDraftWorkflowStore((DraftWorkflowStore) mockDelegate.proxy(), cacheManager)
        {
            @Override
            String convertDescriptorToXML(final WorkflowDescriptor descriptor)
            {
                return WORKFLOW_XML;
            }

            @Override
            WorkflowDescriptor convertXMLtoWorkflowDescriptor(final String parentWorkflowXML) throws FactoryException
            {
                if (!WORKFLOW_XML.equals(parentWorkflowXML))
                {
                    throw new RuntimeException("Cached workflow XML does not equal original XML!");
                }
                return descriptor;
            }

            @Override
            WorkflowManager getWorkflowManager()
            {
                return null;
            }
        };

        cachingDraftWorkflowStore.createDraftWorkflow(testUser, workflow);
        cachingDraftWorkflowStore.getDraftWorkflow("parentWorkflow");

        mockDelegate.verify();
        mockJiraWorkflow.verify();
    }

    @Test
    public void testDeleteDraftWorkflow()
    {
        final Mock mockDelegate = new Mock(DraftWorkflowStore.class);
        mockDelegate.expectAndReturn("deleteDraftWorkflow", new Constraint[] { P.eq("parentWorkflow") }, Boolean.TRUE);
        mockDelegate.setStrict(true);

        final CachingDraftWorkflowStore cachingDraftWorkflowStore = new CachingDraftWorkflowStore((DraftWorkflowStore) mockDelegate.proxy(), cacheManager);

        final boolean deleted = cachingDraftWorkflowStore.deleteDraftWorkflow("parentWorkflow");
        assertTrue(deleted);

        mockDelegate.verify();
    }

    @Test
    public void testCreateAndDeleteDraftWorkflow()
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.setStrict(true);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        mockJiraWorkflow.expectAndReturn("getDescriptor", descriptor);
        mockJiraWorkflow.expectAndReturn("getName", "parentWorkflow");
        final JiraWorkflow workflow = (JiraWorkflow) mockJiraWorkflow.proxy();
        final MockControl mockDraftWorkflowStoreControl = MockClassControl.createStrictControl(DraftWorkflowStore.class);
        final DraftWorkflowStore mockDraftWorkflowStore = (DraftWorkflowStore) mockDraftWorkflowStoreControl.getMock();
        mockDraftWorkflowStore.createDraftWorkflow(testUser, workflow);
        mockDraftWorkflowStoreControl.setReturnValue(workflow);
        mockDraftWorkflowStore.getDraftWorkflow("parentWorkflow");
        mockDraftWorkflowStoreControl.setReturnValue(workflow);
        mockDraftWorkflowStore.deleteDraftWorkflow("parentWorkflow");
        mockDraftWorkflowStoreControl.setReturnValue(true, 1);
        mockDraftWorkflowStore.getDraftWorkflow("parentWorkflow");
        mockDraftWorkflowStoreControl.setReturnValue(null, 1);
        mockDraftWorkflowStoreControl.replay();

        final CachingDraftWorkflowStore cachingDraftWorkflowStore = new CachingDraftWorkflowStore(mockDraftWorkflowStore, cacheManager)
        {
            @Override
            String convertDescriptorToXML(final WorkflowDescriptor descriptor)
            {
                return WORKFLOW_XML;
            }

            @Override
            WorkflowDescriptor convertXMLtoWorkflowDescriptor(final String parentWorkflowXML) throws FactoryException
            {
                if (!WORKFLOW_XML.equals(parentWorkflowXML))
                {
                    throw new RuntimeException("Error, cached value '" + parentWorkflowXML + "' does not equal original '" + WORKFLOW_XML + "'.");
                }
                return descriptor;
            }

            @Override
            WorkflowManager getWorkflowManager()
            {
                return null;
            }
        };

        cachingDraftWorkflowStore.createDraftWorkflow(testUser, workflow);
        //check that something is in the cache
        JiraWorkflow jiraWorkflow = cachingDraftWorkflowStore.getDraftWorkflow("parentWorkflow");
        assertNotNull(jiraWorkflow);
        //now delete the draft workflow. This should also delete the cache.
        final boolean deleted = cachingDraftWorkflowStore.deleteDraftWorkflow("parentWorkflow");
        assertTrue(deleted);

        //this should call through to the delegate and eventually return null.
        jiraWorkflow = cachingDraftWorkflowStore.getDraftWorkflow("parentWorkflow");
        assertNull(jiraWorkflow);

        mockDraftWorkflowStoreControl.verify();
        mockJiraWorkflow.verify();
    }

    @Test
    public void testDeleteWorkflowIsAtomic() throws ExecutionException, InterruptedException
    {
        final Mock mockJiraWorkflow = new Mock(JiraWorkflow.class);
        mockJiraWorkflow.setStrict(true);
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        mockJiraWorkflow.expectAndReturn("getName", "parentWorkflow");
        final JiraWorkflow workflow = (JiraWorkflow) mockJiraWorkflow.proxy();
        final MockDraftWorkflowStore mockDraftWorkflowStore = new MockDraftWorkflowStore();
        mockDraftWorkflowStore.createdWorkflow = workflow;
        final CountDownLatch deleteLatch = new CountDownLatch(1);
        mockDraftWorkflowStore.deleteLatch = deleteLatch;

        final CachingDraftWorkflowStore cachingDraftWorkflowStore = new CachingDraftWorkflowStore(mockDraftWorkflowStore, cacheManager)
        {
            @Override
            String convertDescriptorToXML(final WorkflowDescriptor descriptor)
            {
                return WORKFLOW_XML;
            }

            @Override
            WorkflowDescriptor convertXMLtoWorkflowDescriptor(final String parentWorkflowXML) throws FactoryException
            {
                if (!WORKFLOW_XML.equals(parentWorkflowXML))
                {
                    throw new RuntimeException("Error, cached value '" + parentWorkflowXML + "' does not equal original '" + WORKFLOW_XML + "'.");
                }
                return descriptor;
            }

            @Override
            WorkflowManager getWorkflowManager()
            {
                return null;
            }
        };

        cachingDraftWorkflowStore.createDraftWorkflow(testUser, workflow);

        final List tasks = new ArrayList();
        tasks.add(new Callable()
        {
            public Object call() throws Exception
            {
                cachingDraftWorkflowStore.deleteDraftWorkflow("parentWorkflow");

                return null;
            }
        });
        tasks.add(new Callable()
        {

            public Object call() throws Exception
            {
                deleteLatch.await();
                final JiraWorkflow deletedWorkflow = cachingDraftWorkflowStore.getDraftWorkflow("parentWorkflow");
                //ensure the get can never see the worfklow that's just been deleted
                assertNull(deletedWorkflow);
                return null;
            }
        });

        runMultiThreadedTest(tasks, 2);

        mockJiraWorkflow.verify();
    }

    @Test
    public void testUpdateWorkflow()
    {
        final MockControl mockJiraDraftWorkflowControl = MockClassControl.createControl(JiraDraftWorkflow.class);
        final JiraDraftWorkflow mockJiraDraftWorkflow = (JiraDraftWorkflow) mockJiraDraftWorkflowControl.getMock();

        mockJiraDraftWorkflowControl.replay();

        final Mock mockDelegate = new Mock(DraftWorkflowStore.class);
        mockDelegate.expectAndReturn("updateDraftWorkflow",
            new Constraint[] { P.eq(testUser), P.eq("parentWorkflow"), P.eq(mockJiraDraftWorkflow) }, mockJiraDraftWorkflow);

        final CachingDraftWorkflowStore cachingDraftWorkflowStore = new CachingDraftWorkflowStore((DraftWorkflowStore) mockDelegate.proxy(), cacheManager)
        {

            @Override
            String convertDescriptorToXML(final WorkflowDescriptor descriptor)
            {
                return WORKFLOW_XML;
            }
        };

        final JiraWorkflow updatedWorkflow = cachingDraftWorkflowStore.updateDraftWorkflow(testUser, "parentWorkflow", mockJiraDraftWorkflow);
        assertNotNull(updatedWorkflow);
        assertEquals(mockJiraDraftWorkflow, updatedWorkflow);

        mockDelegate.verify();
        mockJiraDraftWorkflowControl.verify();
    }

    private void runMultiThreadedTest(final List tasks, final int threads) throws InterruptedException, ExecutionException
    {
        final ExecutorService pool = Executors.newFixedThreadPool(threads);

        List /*<Future>*/futures;
        try
        {
            futures = pool.invokeAll(tasks);
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        //wait until all tasks have finished executing.
        for (final Iterator it = futures.iterator(); it.hasNext();)
        {
            final Future future = (Future) it.next();
            future.get();
        }
    }

    private class MockDraftWorkflowStore implements DraftWorkflowStore
    {
        public JiraWorkflow createdWorkflow;
        public CountDownLatch deleteLatch;

        public JiraWorkflow getDraftWorkflow(final String parentWorkflowName) throws DataAccessException
        {
            return createdWorkflow;
        }

        public JiraWorkflow createDraftWorkflow(final String authorName, final JiraWorkflow parentWorkflow) throws DataAccessException, IllegalStateException, IllegalArgumentException
        {
            return createdWorkflow;
        }

        public JiraWorkflow createDraftWorkflow(final ApplicationUser author, final JiraWorkflow parentWorkflow) throws DataAccessException, IllegalStateException, IllegalArgumentException
        {
            return createdWorkflow;
        }

        public boolean deleteDraftWorkflow(final String parentWorkflowName) throws DataAccessException, IllegalArgumentException
        {
            try
            {
                createdWorkflow = null;
                deleteLatch.countDown();
                Thread.sleep(50);
                return true;
            }
            catch (final InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }

        public JiraWorkflow updateDraftWorkflow(final String username, final String parentWorkflowName, final JiraWorkflow workflow) throws DataAccessException
        {
            return null;
        }

        public JiraWorkflow updateDraftWorkflow(final ApplicationUser user, final String parentWorkflowName, final JiraWorkflow workflow) throws DataAccessException
        {
            return null;
        }

        public JiraWorkflow updateDraftWorkflowWithoutAudit(final String parentWorkflowName, final JiraWorkflow workflow)
                throws DataAccessException
        {
            return null;
        }
    }

}
