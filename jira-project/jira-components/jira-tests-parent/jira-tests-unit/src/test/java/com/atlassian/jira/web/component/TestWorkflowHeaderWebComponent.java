package com.atlassian.jira.web.component;

import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.help.HelpUrls;
import com.atlassian.jira.help.MockHelpUrls;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.workflow.MockJiraWorkflow;
import com.atlassian.jira.workflow.ProjectWorkflowSchemeHelper;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.model.WebPanel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestWorkflowHeaderWebComponent
{
    @Rule
    public RuleChain container = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private HelpUrls urls = new MockHelpUrls();

    @Mock
    private WebInterfaceManager webInterfaceManager;
    @Mock
    private ProjectWorkflowSchemeHelper projectWorkflowSchemeHelper;
    @Mock
    private VelocityParamFactory velocityParamFactory;
    @Mock
    private WebPanel webPanel;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockJiraWorkflow jiraWorkflow = new MockJiraWorkflow();

    @Captor
    private ArgumentCaptor<Map<String, Object>> argCaptor;

    private WorkflowHeaderWebComponent workflowHeaderWebComponent;

    @Before
    public void setUp()
    {
        ExecutingHttpRequest.set(request, null);

        when(webInterfaceManager.getWebPanels("workflow.header")).thenReturn(Collections.singletonList(webPanel));

        workflowHeaderWebComponent = new WorkflowHeaderWebComponent(webInterfaceManager, projectWorkflowSchemeHelper, velocityParamFactory);
    }

    @After
    public void tearDown()
    {
        ExecutingHttpRequest.clear();
    }

    @Test
    public void displayUpdatedDateIsTrueOnContextWhenContextPathEndsWithClassicWorkflowDesignerPath()
    {
        request.setRequestURL("/ClassicWorkflowDesigner.jspa");
        workflowHeaderWebComponent.getHtml(jiraWorkflow, "key");

        verify(velocityParamFactory, times(1)).getDefaultVelocityParams(argCaptor.capture());
        assertThat((Boolean) argCaptor.getValue().get("displayUpdatedDate"), equalTo(Boolean.TRUE));
    }

    @Test
    public void displayUpdatedDateIsTrueOnContextWhenContextPathEndsWithTextModePath()
    {
        request.setRequestURL("/ViewWorkflowSteps.jspa");
        workflowHeaderWebComponent.getHtml(jiraWorkflow, "key");

        verify(velocityParamFactory, times(1)).getDefaultVelocityParams(argCaptor.capture());
        assertThat((Boolean) argCaptor.getValue().get("displayUpdatedDate"), equalTo(Boolean.TRUE));
    }

    @Test
    public void displayUpdatedDateIsFalseOnContextWhenContextPathEndsWithRegularWorkflowDesignerPath()
    {
        request.setRequestURL("/WorkflowDesigner.jspa");
        workflowHeaderWebComponent.getHtml(jiraWorkflow, "key");

        verify(velocityParamFactory, times(1)).getDefaultVelocityParams(argCaptor.capture());
        assertThat((Boolean) argCaptor.getValue().get("displayUpdatedDate"), equalTo(Boolean.FALSE));
    }
}
