package com.atlassian.jira.workflow;

import java.util.Arrays;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/** @since v3.13 */
public class TestJiraWorkflowFactory
{
    @Test
    public void testGetDefaultWorkflow() throws FactoryException
    {
        final Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);

        JiraWorkflowFactory jiraWorkflowFactory = new JiraWorkflowFactory()
        {
            WorkflowDescriptorStore getWorkflowDescriptorStore()
            {
                return (WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy();
            }
        };
        //load the default workflow via XML from the classpath.
        jiraWorkflowFactory.initDone();

        final WorkflowDescriptor factoryDescriptor = jiraWorkflowFactory.getWorkflow("jira");
        // We just loaded the default WorkFlow - do a couple of quick assertions that it looks good.
        assertEquals("jira", factoryDescriptor.getName());
        assertEquals("Open", factoryDescriptor.getStep(1).getName());
        assertEquals("In Progress", factoryDescriptor.getStep(3).getName());
        assertEquals("Resolved", factoryDescriptor.getStep(4).getName());
        assertEquals("Reopened", factoryDescriptor.getStep(5).getName());
        assertEquals("Closed", factoryDescriptor.getStep(6).getName());
    }

    @Test
    public void testGetWorkflow() throws FactoryException
    {
        final ImmutableWorkflowDescriptor descriptor = new ImmutableWorkflowDescriptor(new DescriptorFactory().createWorkflowDescriptor());
        final Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);
        mockWorkflowDescriptorStore.setStrict(true);
        mockWorkflowDescriptorStore.expectAndReturn("getWorkflow", new Constraint[] { P.eq("testWorkflow") }, descriptor);


        JiraWorkflowFactory jiraWorkflowFactory = new JiraWorkflowFactory()
        {
            WorkflowDescriptorStore getWorkflowDescriptorStore()
            {
                return (WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy();
            }
        };
        jiraWorkflowFactory.initDone();

        final WorkflowDescriptor factoryDescriptor = jiraWorkflowFactory.getWorkflow("testWorkflow");
        assertEquals(descriptor, factoryDescriptor);
        mockWorkflowDescriptorStore.verify();
    }

    @Test
    public void testGetWorkflowNames() throws FactoryException
    {
        String[] workflowNames = new String[] { "Hilde", "Barney" };
        final Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);
        mockWorkflowDescriptorStore.setStrict(true);
        mockWorkflowDescriptorStore.expectAndReturn("getWorkflowNames", workflowNames);


        JiraWorkflowFactory jiraWorkflowFactory = new JiraWorkflowFactory()
        {
            WorkflowDescriptorStore getWorkflowDescriptorStore()
            {
                return (WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy();
            }
        };
        jiraWorkflowFactory.initDone();

        String[] factoryWorkflowNames = jiraWorkflowFactory.getWorkflowNames();
        String[] expectedNames = new String[] { "jira", "Hilde", "Barney" };
        assertTrue(Arrays.equals(expectedNames, factoryWorkflowNames));
    }

    @Test
    public void testSaveWorkflow() throws FactoryException
    {
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        final Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);
        mockWorkflowDescriptorStore.expectAndReturn("saveWorkflow",
                new Constraint[] { P.eq("Werner"), P.eq(descriptor), P.IS_TRUE },
                Boolean.TRUE);


        JiraWorkflowFactory jiraWorkflowFactory = new JiraWorkflowFactory()
        {
            WorkflowDescriptorStore getWorkflowDescriptorStore()
            {
                return (WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy();
            }
        };
        jiraWorkflowFactory.initDone();

        boolean saved = jiraWorkflowFactory.saveWorkflow(null, null, true);
        assertFalse(saved);

        //can't save over the default workflow
        saved = jiraWorkflowFactory.saveWorkflow("jira", descriptor, true);
        assertFalse(saved);

        saved = jiraWorkflowFactory.saveWorkflow("Werner", descriptor, true);
        assertTrue(saved);
    }

    @Test
    public void testIsModifiableNullWorkflow() throws FactoryException
    {
        final Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);
        mockWorkflowDescriptorStore.setStrict(true);
        mockWorkflowDescriptorStore.expectAndReturn("getWorkflow", new Constraint[] { P.eq("Werner") }, null);

        JiraWorkflowFactory jiraWorkflowFactory = new JiraWorkflowFactory()
        {
            WorkflowDescriptorStore getWorkflowDescriptorStore()
            {
                return (WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy();
            }
        };
        jiraWorkflowFactory.initDone();

        boolean modifiable = jiraWorkflowFactory.isModifiable("jira");
        assertFalse(modifiable);

        try
        {
            jiraWorkflowFactory.isModifiable(null);
            fail("should have failed with an IAE");
        }
        catch (IllegalArgumentException e)
        {
            //yay
        }

        try
        {
            jiraWorkflowFactory.isModifiable("Werner");
            fail("There's no workflow, so this should have failed with an IAE");
        }
        catch (IllegalArgumentException e)
        {
            //yay
        }
    }
    
    @Test
    public void testIsModifiable() throws FactoryException
    {
        final WorkflowDescriptor descriptor = new DescriptorFactory().createWorkflowDescriptor();
        final Mock mockWorkflowDescriptorStore = new Mock(WorkflowDescriptorStore.class);
        mockWorkflowDescriptorStore.setStrict(true);
        mockWorkflowDescriptorStore.expectAndReturn("getWorkflow", new Constraint[] { P.eq("Werner" )}, new ImmutableWorkflowDescriptor(descriptor));

        JiraWorkflowFactory jiraWorkflowFactory = new JiraWorkflowFactory()
        {
            WorkflowDescriptorStore getWorkflowDescriptorStore()
            {
                return (WorkflowDescriptorStore) mockWorkflowDescriptorStore.proxy();
            }
        };
        jiraWorkflowFactory.initDone();

        boolean modifiable = jiraWorkflowFactory.isModifiable("Werner");
        assertTrue(modifiable);
    }

}

