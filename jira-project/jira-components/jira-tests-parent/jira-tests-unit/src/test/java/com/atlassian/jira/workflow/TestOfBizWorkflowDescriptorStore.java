package com.atlassian.jira.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

/**
 *
 */
public class TestOfBizWorkflowDescriptorStore
{
    private static final String WORKFLOW_NAME = "testWorkflow";
    private static final String WORKFLOW_DESCRIPTOR_XML =
            "<workflow>\n" +
                    "  <initial-actions>\n" +
                    "    <action id=\"1\" name=\"Create Issue\">\n" +
                    "      <results>\n" +
                    "        <unconditional-result old-status=\"Finished\" status=\"Open\" step=\"1\"/>\n" +
                    "      </results>\n" +
                    "    </action>\n" +
                    "  </initial-actions>\n" +
                    "  <steps>\n" +
                    "    <step id=\"1\" name=\"Open\">\n" +
                    "    </step>\n" +
                    "  </steps>\n" +
                    "</workflow>\n";

    private static final String WORKFLOW_DESCRIPTOR =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.7//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_7.dtd\">\n" +
                    WORKFLOW_DESCRIPTOR_XML;

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);
    
    @AvailableInContainer
    private MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();

    @Test
    public void testGetWorkflow() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));


        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);
        WorkflowDescriptor workflowDescriptor = ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);

        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, workflowDescriptor.asXML());
    }

    @Test
    public void testGetWorkflowWithNoWorkflow() throws FactoryException
    {
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);
        WorkflowDescriptor workflowDescriptor = ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);

        assertNull(workflowDescriptor);
    }

    @Test
    public void testGetWorkflowWithNullName() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));


        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);
        try
        {
            ofBizWorkflowStore.getWorkflow(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //YAY!!
        }
    }

    @Test
    public void testGetWorkflowWithMultipleWorkflows() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "ANOTHER DESCRIPTOR"));


        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);
        try
        {
            ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
            fail();
        }
        catch (IllegalStateException e)
        {
            //YAY!!
        }
    }

    @Test
    public void testRemoveWorkflow() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "AnotherWorkflow",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));


        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);
        assertTrue(ofBizWorkflowStore.removeWorkflow(WORKFLOW_NAME));
        assertNull(ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME));
        assertNotNull(ofBizWorkflowStore.getWorkflow("AnotherWorkflow"));
    }

    @Test
    public void testRemoveWorkflowWithNullName()
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));


        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);
        try
        {
            ofBizWorkflowStore.removeWorkflow(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //yay
        }
    }

    @Test
    public void testRemoveWorkflowNonExistant()
    {
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);
        assertFalse(ofBizWorkflowStore.removeWorkflow(WORKFLOW_NAME));
    }

    @Test
    public void testSaveWorkflowCreate() throws FactoryException
    {
        final WorkflowDescriptor workflowDescriptor = DescriptorFactory.getFactory().createWorkflowDescriptor();
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator)
        {
            String convertDescriptorToXML(WorkflowDescriptor descriptor)
            {
                assertEquals(workflowDescriptor, descriptor);
                return WORKFLOW_DESCRIPTOR;
            }
        };

        ofBizWorkflowStore.saveWorkflow(WORKFLOW_NAME, workflowDescriptor, false);
        WorkflowDescriptor storedDescriptor = ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, storedDescriptor.asXML());
    }

    @Test
    public void testSaveWorkflowUpdate() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "THIS IS CRAP"));

        final WorkflowDescriptor workflowDescriptor = DescriptorFactory.getFactory().createWorkflowDescriptor();
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator)
        {
            String convertDescriptorToXML(WorkflowDescriptor descriptor)
            {
                assertEquals(workflowDescriptor, descriptor);
                return WORKFLOW_DESCRIPTOR;
            }
        };

        try
        {
            ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
            fail();
        }
        catch (FactoryException e)
        {
            //yay, the workflow is in a bad state in the db. Lets update it
        }

        ofBizWorkflowStore.saveWorkflow(WORKFLOW_NAME, workflowDescriptor, true);
        WorkflowDescriptor updatedDescriptor = ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
        assertNotNull(updatedDescriptor);
        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, updatedDescriptor.asXML());
    }

    @Test
    public void testSaveWorkflowNullName()
    {
        final WorkflowDescriptor workflowDescriptor = DescriptorFactory.getFactory().createWorkflowDescriptor();
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);
        try
        {
            ofBizWorkflowStore.saveWorkflow(null, workflowDescriptor, false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Workflow name cannot be null!", e.getMessage());
        }
    }

    @Test
    public void testSaveWorkflowNullDescriptor()
    {
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);
        try
        {
            ofBizWorkflowStore.saveWorkflow(WORKFLOW_NAME, null, false);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Workflow descriptor cannot be null!", e.getMessage());
        }
    }

    @Test
    public void testSaveWorkflowReplaceWithFalseReplace() throws FactoryException
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, WORKFLOW_NAME,
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "THIS IS CRAP"));

        final WorkflowDescriptor workflowDescriptor = DescriptorFactory.getFactory().createWorkflowDescriptor();
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator)
        {
            String convertDescriptorToXML(WorkflowDescriptor descriptor)
            {
                if (!descriptor.equals(workflowDescriptor))
                {
                    throw new RuntimeException("workflowDescriptor to be converted to XML is not equal to original descriptor!");
                }
                return WORKFLOW_DESCRIPTOR;
            }
        };

        try
        {
            ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
            fail();
        }
        catch (FactoryException e)
        {
            //yay, the workflow is in a bad state in the db. Lets update it
        }

        assertFalse(ofBizWorkflowStore.saveWorkflow(WORKFLOW_NAME, workflowDescriptor, false));
        try
        {
            ofBizWorkflowStore.getWorkflow(WORKFLOW_NAME);
            fail();
        }
        catch (FactoryException e)
        {
            //yay, the workflow is still in a bad state in the db
        }
    }

    @Test
    public void testGetWorkflowNames()
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 1",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "THIS IS CRAP"));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 2",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "THIS IS CRAP"));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 3",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, "THIS IS CRAP"));

        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);

        final String[] workflowNames = ofBizWorkflowStore.getWorkflowNames();
        final List<String> workflowNamesList = Arrays.asList(workflowNames);
        assertTrue(workflowNamesList.contains("Name 1"));
        assertTrue(workflowNamesList.contains("Name 2"));
        assertTrue(workflowNamesList.contains("Name 3"));
    }

    @Test
    public void testGetWorkflowNamesNoNames()
    {
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);

        assertEquals(0, ofBizWorkflowStore.getWorkflowNames().length);
    }

    @Test
    public void testGetAllWorkflowDescriptors()
    {
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 1",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 2",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));
        UtilsForTests.getTestEntity(OfBizWorkflowDescriptorStore.WORKFLOW_ENTITY_NAME,
                ImmutableMap.of(OfBizWorkflowDescriptorStore.NAME_ENTITY_FIELD, "Name 3",
                        OfBizWorkflowDescriptorStore.DESCRIPTOR_ENTITY_FIELD, WORKFLOW_DESCRIPTOR));

        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);

        final List<JiraWorkflowDTO> workflowDescriptors = ofBizWorkflowStore.getAllJiraWorkflowDTOs();
        assertEquals(3, workflowDescriptors.size());
        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, ((JiraWorkflowDTO) workflowDescriptors.get(0)).getDescriptor().asXML());
        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, ((JiraWorkflowDTO) workflowDescriptors.get(1)).getDescriptor().asXML());
        assertEqualsIgnoreWhitespace(WORKFLOW_DESCRIPTOR_XML, ((JiraWorkflowDTO) workflowDescriptors.get(2)).getDescriptor().asXML());
    }

    @Test
    public void testGetAllWorkflowDescriptorsWithNone()
    {
        OfBizWorkflowDescriptorStore ofBizWorkflowStore = new OfBizWorkflowDescriptorStore(mockOfBizDelegator);

        final List<JiraWorkflowDTO> workflowDescriptors = ofBizWorkflowStore.getAllJiraWorkflowDTOs();
        assertEquals(0, workflowDescriptors.size());
    }

    private void assertEqualsIgnoreWhitespace(String original, String stringBeingTested)
    {
        assertEquals(StringUtils.deleteWhitespace(original), StringUtils.deleteWhitespace(stringBeingTested));
    }    
}
