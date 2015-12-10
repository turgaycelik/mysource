package com.atlassian.jira.issue.search.searchers.renderer;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.priority.MockPriority;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestStatusSearchRenderer extends MockControllerTestCase
{
    private FieldVisibilityManager fieldVisibilityManager;
    private VelocityTemplatingEngine templatingEngine;
    private ApplicationProperties applicationProperties;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private ProjectManager projectManager;
    private WorkflowManager workflowManager;
    private ConstantsManager constantsManager;
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        searchContext = mockController.getMock(SearchContext.class);
        constantsManager = mockController.getMock(ConstantsManager.class);
        workflowManager = mockController.getMock(WorkflowManager.class);
        projectManager = mockController.getMock(ProjectManager.class);
        velocityRequestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);
        templatingEngine = mockController.getMock(VelocityTemplatingEngine.class);
        fieldVisibilityManager = mockController.getMock(FieldVisibilityManager.class);
    }

    @Test
    public void testSelectListOptionsNullProjectIdsAndNoWorkFlows() throws Exception
    {
        mockController.replay();
        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManagerControl.replay();

        final MockControl mockJiraAuthenticationContextControl = MockControl.createStrictControl(JiraAuthenticationContext.class);
        final JiraAuthenticationContext mockJiraAuthenticationContext = (JiraAuthenticationContext) mockJiraAuthenticationContextControl.getMock();
        mockJiraAuthenticationContextControl.replay();

        final MockControl mockVelocityRequestContextFactoryControl = MockClassControl.createControl(DefaultVelocityRequestContextFactory.class);
        final VelocityRequestContextFactory mockVelocityRequestContextFactory = (VelocityRequestContextFactory) mockVelocityRequestContextFactoryControl.getMock();
        mockVelocityRequestContextFactoryControl.replay();

        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationPropertiesControl.replay();

        final MockControl mockVelocityManagerControl = MockControl.createStrictControl(VelocityTemplatingEngine.class);
        final VelocityTemplatingEngine mockTemplatingEngine = (VelocityTemplatingEngine) mockVelocityManagerControl.getMock();
        mockVelocityManagerControl.replay();

        final MockControl mockFieldVisibilityManagerControl = MockClassControl.createControl(FieldVisibilityManager.class);
        final FieldVisibilityManager mockFieldVisibilityManager = (FieldVisibilityManager) mockFieldVisibilityManagerControl.getMock();
        mockFieldVisibilityManagerControl.replay();

        final MockControl mockWorkflowManagerControl = MockControl.createStrictControl(WorkflowManager.class);
        final WorkflowManager mockWorkflowManager = (WorkflowManager) mockWorkflowManagerControl.getMock();
        mockWorkflowManager.getActiveWorkflows();
        mockWorkflowManagerControl.setReturnValue(Collections.EMPTY_LIST);

        mockWorkflowManagerControl.replay();

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManagerControl.replay();

        StatusSearchRenderer statusSearchRenderer =
                new StatusSearchRenderer
                        (
                                "name", mockConstantsManager, mockVelocityRequestContextFactory,
                                mockApplicationProperties, mockTemplatingEngine, mockFieldVisibilityManager,
                                mockWorkflowManager, mockProjectManager
                        );

        final MockControl mockSearchContextControl = MockControl.createStrictControl(SearchContext.class);
        final SearchContext mockSearchContext = (SearchContext) mockSearchContextControl.getMock();

        mockSearchContext.getProjectIds();
        mockSearchContextControl.setReturnValue(null);
        mockSearchContextControl.replay();

        Collection<Status> result = statusSearchRenderer.getSelectListOptions(mockSearchContext);
        assertEquals(0, result.size());

        mockConstantsManagerControl.verify();
        mockJiraAuthenticationContextControl.verify();
        mockVelocityRequestContextFactoryControl.verify();
        mockApplicationPropertiesControl.verify();
        mockVelocityManagerControl.verify();
        mockFieldVisibilityManagerControl.verify();
        mockWorkflowManagerControl.verify();
        mockProjectManagerControl.verify();
        mockSearchContextControl.verify();
    }

    @SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
    @Test
    public void testSelectListOptionsNullProjectIdsAndWorkFlowsException() throws Exception
    {
        mockController.replay();
        MockPriority mockPriority = new MockPriority("123", "testStatus");

        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getStatusObjects();
        mockConstantsManagerControl.setReturnValue(EasyList.build(mockPriority));
        mockConstantsManagerControl.replay();

        final MockControl mockJiraAuthenticationContextControl = MockControl.createStrictControl(JiraAuthenticationContext.class);
        final JiraAuthenticationContext mockJiraAuthenticationContext = (JiraAuthenticationContext) mockJiraAuthenticationContextControl.getMock();
        mockJiraAuthenticationContextControl.replay();

        final MockControl mockVelocityRequestContextFactoryControl = MockClassControl.createControl(DefaultVelocityRequestContextFactory.class);
        final VelocityRequestContextFactory mockVelocityRequestContextFactory = (VelocityRequestContextFactory) mockVelocityRequestContextFactoryControl.getMock();
        mockVelocityRequestContextFactoryControl.replay();


        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationPropertiesControl.replay();

        final MockControl mockVelocityTemplatingEngineControl = MockControl.createStrictControl(VelocityTemplatingEngine.class);
        final VelocityTemplatingEngine mockTemplatingEngine = (VelocityTemplatingEngine) mockVelocityTemplatingEngineControl.getMock();
        mockVelocityTemplatingEngineControl.replay();

        final MockControl mockFieldVisibilityManagerControl = MockClassControl.createControl(FieldVisibilityManager.class);
        final FieldVisibilityManager mockFieldVisibilityManager = (FieldVisibilityManager) mockFieldVisibilityManagerControl.getMock();
        mockFieldVisibilityManagerControl.replay();

        final MockControl mockWorkflowManagerControl = MockControl.createStrictControl(WorkflowManager.class);
        final WorkflowManager mockWorkflowManager = (WorkflowManager) mockWorkflowManagerControl.getMock();
        mockWorkflowManager.getActiveWorkflows();
        mockWorkflowManagerControl.setThrowable(new WorkflowException("bugger"));
        mockWorkflowManagerControl.replay();

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManagerControl.replay();

        StatusSearchRenderer statusSearchRenderer = new StatusSearchRenderer("name", mockConstantsManager,
                mockVelocityRequestContextFactory,
                                                                            mockApplicationProperties,
                                                                            mockTemplatingEngine,
                mockFieldVisibilityManager,
                                                                            mockWorkflowManager,
                                                                            mockProjectManager
        );

        final MockControl mockSearchContextControl = MockControl.createStrictControl(SearchContext.class);
        final SearchContext mockSearchContext = (SearchContext) mockSearchContextControl.getMock();

        mockSearchContext.getProjectIds();
        mockSearchContextControl.setReturnValue(null);
        mockSearchContextControl.replay();

        Collection<Status> result = statusSearchRenderer.getSelectListOptions(mockSearchContext);
        assertEquals(1, result.size());
        //noinspection SuspiciousMethodCalls
        assertTrue(result.contains(mockPriority));

        mockConstantsManagerControl.verify();
        mockJiraAuthenticationContextControl.verify();
        mockVelocityRequestContextFactoryControl.verify();
        mockApplicationPropertiesControl.verify();
        mockVelocityTemplatingEngineControl.verify();
        mockFieldVisibilityManagerControl.verify();
        mockWorkflowManagerControl.verify();
        mockProjectManagerControl.verify();
        mockSearchContextControl.verify();
    }

    @Test
    public void testGetSelectListOptionsProjectAndIssueTypeDefined() throws Exception
    {
        final JiraWorkflow workflow = mockController.getMock(JiraWorkflow.class);

        Long pid = 10L;
        String tid = "20";

        final MockProject project = new MockProject(pid);

        final MockStatus status1 = new MockStatus("1", "Status 1");
        status1.setSequence(1L);
        final MockStatus status2 = new MockStatus("2", "Status 2");
        status1.setSequence(2L);

        searchContext.getProjectIds();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder(pid).asList());

        searchContext.getIssueTypeIds();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder(tid).asList());

        projectManager.getProjectObj(pid);
        mockController.setReturnValue(project);

        workflowManager.getWorkflow(pid, tid);
        mockController.setReturnValue(workflow);

        workflow.getLinkedStatusObjects();
        mockController.setReturnValue(CollectionBuilder.newBuilder(status2, status1).asList());

        mockController.replay();
        final StatusSearchRenderer renderer = new StatusSearchRenderer("blarg", constantsManager, velocityRequestContextFactory, applicationProperties, templatingEngine, fieldVisibilityManager, workflowManager, projectManager);
        renderer.getSelectListOptions(searchContext);
        mockController.verify();
    }

    @Test
    public void testGetSelectListOptionsProjectDefined() throws Exception
    {
        final JiraWorkflow workflow = mockController.getMock(JiraWorkflow.class);

        Long pid = 10L;
        String tid = "20";

        final MockProject project = new MockProject(pid);

        final MockStatus status1 = new MockStatus("1", "Status 1");
        status1.setSequence(1L);
        final MockStatus status2 = new MockStatus("2", "Status 2");
        status1.setSequence(2L);

        searchContext.getProjectIds();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder(pid).asList());

        searchContext.getIssueTypeIds();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder().asList());

        constantsManager.getAllIssueTypeIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder(tid).asList());        

        projectManager.getProjectObj(pid);
        mockController.setReturnValue(project);

        workflowManager.getWorkflow(pid, tid);
        mockController.setReturnValue(workflow);

        workflow.getLinkedStatusObjects();
        mockController.setReturnValue(CollectionBuilder.newBuilder(status2, status1).asList());

        mockController.replay();
        final StatusSearchRenderer renderer = new StatusSearchRenderer("blarg", constantsManager, velocityRequestContextFactory, applicationProperties, templatingEngine, fieldVisibilityManager, workflowManager, projectManager);
        renderer.getSelectListOptions(searchContext);
        mockController.verify();
    }

    @Test
    public void testGetSelectListOptionsProjectDefinedButDoesntExist() throws Exception
    {
        Long pid = 10L;
        String tid = "20";

        searchContext.getProjectIds();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder(pid).asList());

        searchContext.getIssueTypeIds();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder().asList());

        constantsManager.getAllIssueTypeIds();
        mockController.setReturnValue(CollectionBuilder.newBuilder(tid).asList());

        projectManager.getProjectObj(pid);
        mockController.setReturnValue(null);

        mockController.replay();
        final StatusSearchRenderer renderer = new StatusSearchRenderer("blarg", constantsManager, velocityRequestContextFactory, applicationProperties, templatingEngine, fieldVisibilityManager, workflowManager, projectManager);
        renderer.getSelectListOptions(searchContext);
        mockController.verify();
    }

    @Test
    public void testGetSelectListOptionsNoProjectDefined() throws Exception
    {
        final JiraWorkflow workflow = mockController.getMock(JiraWorkflow.class);

        final MockStatus status1 = new MockStatus("1", "Status 1");
        status1.setSequence(1L);
        final MockStatus status2 = new MockStatus("2", "Status 2");
        status1.setSequence(2L);

        searchContext.getProjectIds();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder().asList());

        searchContext.getIssueTypeIds();
        mockController.setDefaultReturnValue(CollectionBuilder.newBuilder().asList());

        workflowManager.getActiveWorkflows();
        mockController.setReturnValue(CollectionBuilder.newBuilder(workflow).asCollection());

        workflow.getLinkedStatusObjects();
        mockController.setReturnValue(CollectionBuilder.newBuilder(status2, status1).asList());

        mockController.replay();
        final StatusSearchRenderer renderer = new StatusSearchRenderer("blarg", constantsManager, velocityRequestContextFactory, applicationProperties, templatingEngine, fieldVisibilityManager, workflowManager, projectManager);
        renderer.getSelectListOptions(searchContext);
        mockController.verify();
    }

}
