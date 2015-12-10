package com.atlassian.jira.workflow;

import java.util.Arrays;
import java.util.List;

import com.atlassian.cache.memory.MemoryCacheManager;

import com.google.common.collect.ImmutableList;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/** @since v3.13 */
@RunWith(MockitoJUnitRunner.class)
public class TestCachingWorkflowDescriptorStore
{
    private static final String WORKFLOW_NAME = "jira";
    
    @Mock
    private WorkflowDescriptorStore workflowDescriptorStore;

    @Test
    public void testGetWorkflow() throws FactoryException
    {
        when(workflowDescriptorStore.getAllJiraWorkflowDTOs()).thenReturn(ImmutableList.<JiraWorkflowDTO>of());

        CachingWorkflowDescriptorStore cachingWorkflowDescriptorStore =
                new CachingWorkflowDescriptorStore(workflowDescriptorStore, new MemoryCacheManager());

        try
        {
            cachingWorkflowDescriptorStore.getWorkflow(null);
            fail("getWorkflow(null) is invalid.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testRemoveWorkflow() throws FactoryException
    {
        WorkflowDescriptor workflowDescriptor = new WorkflowDescriptor();
        ImmutableWorkflowDescriptor immutableDescriptor = new ImmutableWorkflowDescriptor(workflowDescriptor);
        final JiraWorkflowDTOImpl dto = new JiraWorkflowDTOImpl(null, WORKFLOW_NAME, workflowDescriptor);
        when(workflowDescriptorStore.getAllJiraWorkflowDTOs()).thenReturn(ImmutableList.<JiraWorkflowDTO>of(dto));
        when(workflowDescriptorStore.removeWorkflow(WORKFLOW_NAME)).thenReturn(Boolean.TRUE);
        when(workflowDescriptorStore.getWorkflow(WORKFLOW_NAME))
                .thenReturn(immutableDescriptor)
                .thenReturn(null);

        CachingWorkflowDescriptorStore cachingWorkflowDescriptorStore =
                new CachingWorkflowDescriptorStore(workflowDescriptorStore, new MemoryCacheManager());

        WorkflowDescriptor cachedDescriptor = cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME);

        try
        {
            cachingWorkflowDescriptorStore.removeWorkflow(null);
            fail("removeWorkflow(null) is invalid.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        boolean removed = cachingWorkflowDescriptorStore.removeWorkflow(WORKFLOW_NAME);
        assertTrue(removed);

        assertNull(cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME));
    }

    @Test
    public void testSaveWorkflow() throws FactoryException
    {
        when(workflowDescriptorStore.getAllJiraWorkflowDTOs()).thenReturn(ImmutableList.<JiraWorkflowDTO>of());
        WorkflowDescriptor workflowDescriptor = new WorkflowDescriptor();
        when(workflowDescriptorStore.saveWorkflow(WORKFLOW_NAME, workflowDescriptor, Boolean.TRUE)).thenReturn(Boolean.TRUE);
        ImmutableWorkflowDescriptor immutableDescriptor = new ImmutableWorkflowDescriptor(workflowDescriptor);
        when(workflowDescriptorStore.getWorkflow(WORKFLOW_NAME))
                .thenReturn(null)
                .thenReturn(immutableDescriptor);

        CachingWorkflowDescriptorStore cachingWorkflowDescriptorStore =
                new CachingWorkflowDescriptorStore(workflowDescriptorStore, new MemoryCacheManager());

        WorkflowDescriptor cachedWorkflowDescriptor= cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME);
        //check that there's no cached DTO.
        assertNull(cachedWorkflowDescriptor);

        try
        {
            cachingWorkflowDescriptorStore.saveWorkflow(null, null, true);
            fail("saveWorkflow(null) is invalid.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        boolean saved = cachingWorkflowDescriptorStore.saveWorkflow(WORKFLOW_NAME, workflowDescriptor, true);
        assertTrue(saved);

        //the cached dto should be equal to the one we just saved.
        assertNotNull(cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME));
        //The cached object should not be the same as the original descriptor. It should be wrapped by
        //an immutable wrapper.
        assertEquals(immutableDescriptor, cachingWorkflowDescriptorStore.getWorkflow(WORKFLOW_NAME));
    }

    @Test
    public void testGetWorkflowNames() throws FactoryException
    {
        when(workflowDescriptorStore.getWorkflowNames()).thenReturn(new String[] { WORKFLOW_NAME, "AnotherWorkflow" });

        CachingWorkflowDescriptorStore cachingWorkflowDescriptorStore =
                new CachingWorkflowDescriptorStore( workflowDescriptorStore, new MemoryCacheManager());

        String[] workflowNames = cachingWorkflowDescriptorStore.getWorkflowNames();
        assertEquals(2, workflowNames.length);
        List workflowNameList = Arrays.asList(workflowNames);
        assertTrue(workflowNameList.contains(WORKFLOW_NAME));
        assertTrue(workflowNameList.contains("AnotherWorkflow"));
    }

    @Test
    public void testGetWorkflowDescriptors() throws FactoryException
    {
        final WorkflowDescriptor descriptor = new WorkflowDescriptor();

        final JiraWorkflowDTOImpl dto = new JiraWorkflowDTOImpl(null, WORKFLOW_NAME, descriptor);
        final WorkflowDescriptor descriptor2 = new WorkflowDescriptor();
        final JiraWorkflowDTOImpl dto2 = new JiraWorkflowDTOImpl(null, "AnotherWorkflow", descriptor2);
        when(workflowDescriptorStore.getAllJiraWorkflowDTOs()).thenReturn(ImmutableList.<JiraWorkflowDTO>of(dto, dto2));

        CachingWorkflowDescriptorStore cachingWorkflowDescriptorStore =
                new CachingWorkflowDescriptorStore(workflowDescriptorStore, new MemoryCacheManager());

        List<JiraWorkflowDTO> workflowDescriptors = cachingWorkflowDescriptorStore.getAllJiraWorkflowDTOs();
        assertEquals(2, workflowDescriptors.size());
        assertTrue(workflowDescriptors.contains(dto));
        assertTrue(workflowDescriptors.contains(dto2));
    }
}
