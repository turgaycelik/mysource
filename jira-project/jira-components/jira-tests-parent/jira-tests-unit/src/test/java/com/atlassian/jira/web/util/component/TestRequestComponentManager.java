package com.atlassian.jira.web.util.component;

import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.MockJiraWorkflow;
import com.atlassian.jira.workflow.MockWorkflowManager;
import com.atlassian.jira.workflow.WorkflowManager;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.behaviors.Caching;

import webwork.action.ServletActionContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * TestCase for RequestComponentManager.
 *
 * @since v3.13
 */
public class TestRequestComponentManager
{
    private DefaultPicoContainer parentPicoContainer = new DefaultPicoContainer(new Caching());
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    RequestComponentManager requestComponentManager = new RequestComponentManager();

    @Before
    public void setUp() throws Exception
    {
        // Register a mock HTTP request with ServletActionContext
        ServletActionContext.setRequest(mockRequest);
    }

    @Test
    public void testGetContainerNoWorkflow()
    {
        // With no "workflow" parameter, we should not add any components to PicoContainer
        PicoContainer container = requestComponentManager.getContainer(parentPicoContainer);
        assertTrue(container.getComponents().isEmpty());

        // With empty "workflow" parameter, we should not add any components to PicoContainer
        mockRequest.setParameter("workflowName", "");
        container = requestComponentManager.getContainer(parentPicoContainer);
        assertTrue(container.getComponents().isEmpty());
    }

    @Test
    public void testGetContainerInvalidParameters()
    {
        // register a mock WorkflowManager
        parentPicoContainer.addComponent(WorkflowManager.class, MockWorkflowManager.class);
        // Assert we have exactly one component registered
        assertEquals(1, parentPicoContainer.getComponents().size());

        // make a request that is missing the workflowMode parameter
        mockRequest.setParameter("workflowName", "Tom");
        mockRequest.setRequestURL("http://stuff.com/jira/DoWorkflowStuff");
        try
        {
            requestComponentManager.injectWorkflow(parentPicoContainer, mockRequest);
            fail("Expected an IllegalStateException.");
        }
        catch (IllegalStateException ex)
        {
            // Expected
            assertEquals("Found a 'workflow' in the request parameters, but there is no 'workflowMode'. " +
                         "http://stuff.com/jira/DoWorkflowStuff?workflowName=Tom", ex.getMessage());
        }

        // Set an invalid workflowMode
        mockRequest.setParameter("workflowName", "Tom");
        mockRequest.setParameter("workflowMode", "dead");
        mockRequest.setRequestURL("http://stuff.com/jira/DoWorkflowStuff");
        try
        {
            requestComponentManager.injectWorkflow(parentPicoContainer, mockRequest);
            fail("Expected an IllegalStateException.");
        }
        catch (IllegalStateException ex)
        {
            // Expected
            assertEquals("Invalid workflow mode 'dead'. " +
                         "http://stuff.com/jira/DoWorkflowStuff?workflowName=Tom&workflowMode=dead", ex.getMessage());
        }

        // Set a non-existing workflowName.
        mockRequest.setParameter("workflowName", "Tom");
        mockRequest.setParameter("workflowMode", "live");
        mockRequest.setRequestURL("http://stuff.com/jira/DoWorkflowStuff");
        try
        {
            requestComponentManager.injectWorkflow(parentPicoContainer, mockRequest);
            fail("Expected an IllegalStateException.");
        }
        catch (IllegalStateException ex)
        {
            // Expected
            assertEquals("No live workflow was found for 'Tom'.", ex.getMessage());
        }
    }

    @Test
    public void testGetContainer()
    {
        // register a mock WorkflowManager
        parentPicoContainer.addComponent(WorkflowManager.class, MockWorkflowManager.class);
        MockWorkflowManager mockWorkflowManager = (MockWorkflowManager) parentPicoContainer.getComponent(WorkflowManager.class);
        // Assert we have exactly one component registered
        assertEquals(1, parentPicoContainer.getComponents().size());

        // Add a "Live" workflow with name "Tom" to the mock Workflow manager
        MockJiraWorkflow jiraWorkflow = new MockJiraWorkflow();
        jiraWorkflow.setName("Tom");
        mockWorkflowManager.saveWorkflow(null, jiraWorkflow);
        // Add a "draft" workflow with name "Tom" to the mock Workflow manager
        MockJiraWorkflow draftWorkflow = new MockJiraWorkflow();
        draftWorkflow.setName("Tom");
        mockWorkflowManager.updateDraftWorkflow(null, "Tom", draftWorkflow);

        // request the live version of this workflow
        mockRequest.setParameter("workflowName", "Tom");
        mockRequest.setParameter("workflowMode", "live");
        PicoContainer container = requestComponentManager.getContainer(parentPicoContainer);
        // Get the copy of the registered JiraWorkflow component
        // This should be our "live" instance.
        assertSame(jiraWorkflow, container.getComponent(JiraWorkflow.class));

        // Now add that we want the "Draft" version to the request
        mockRequest.setParameter("workflowMode", "draft");
        container = requestComponentManager.getContainer(parentPicoContainer);
        // Get the copy of the registered JiraWorkflow component
        // This should be our "draft" instance.
        assertSame(draftWorkflow, container.getComponent(JiraWorkflow.class));
    }
}
