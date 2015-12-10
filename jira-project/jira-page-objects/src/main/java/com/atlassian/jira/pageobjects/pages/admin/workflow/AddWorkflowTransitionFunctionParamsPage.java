package com.atlassian.jira.pageobjects.pages.admin.workflow;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowTransitionPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

import org.openqa.selenium.By;

/**
 * Add workflow transition post function parameters for "Assign to role member" post function.
 *
 * @since v6.2
 */
public class AddWorkflowTransitionFunctionParamsPage extends AbstractJiraPage
{
    private static final String WORKFLOW_TRANSITION_FUNCTION_URL = "/secure/admin/workflows/AddWorkflowTransitionFunctionParams!default.jspa";

    private final String stepNumber;
    private final String workflowName;
    private final String workflowMode;
    private final String transitionNumber;
    private final String pluginModuleKey;
    private final URI uri;

    @ElementBy(className = "aui-page-panel-content")
    private PageElement postFunctionParamPanel;

    @ElementBy(name = "jiraform")
    private PageElement postFunctionParamForm;

    @ElementBy(id = "add_submit")
    private PageElement submitButton;

    public AddWorkflowTransitionFunctionParamsPage(final String workflowMode, final String workflowName,
            final String stepNumber, final String transitionNumber, final String pluginModuleKey)
    {
        this.workflowMode = workflowMode;
        this.workflowName = workflowName;
        this.stepNumber = stepNumber;
        this.transitionNumber = transitionNumber;
        this.pluginModuleKey = pluginModuleKey;
        this.uri = UriBuilder.fromPath(WORKFLOW_TRANSITION_FUNCTION_URL).queryParam("workflowName", this.workflowName)
                .queryParam("workflowMode", this.workflowMode).queryParam("workflowStep", this.stepNumber)
                .queryParam("workflowTransition", this.transitionNumber).queryParam("pluginModuleKey", this.pluginModuleKey).build();
    }

    @Override
    public TimedCondition isAt()
    {
        return postFunctionParamForm.find(By.id("pluginModuleKey")).timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return uri.toString();
    }

    public ViewWorkflowTransitionPage submit()
    {
        submitButton.click();
        elementFinder.find(By.className("workflow-browser-items"), TimeoutType.PAGE_LOAD);
        return pageBinder.bind(ViewWorkflowTransitionPage.class, workflowMode, workflowName, stepNumber, transitionNumber);
    }
}
