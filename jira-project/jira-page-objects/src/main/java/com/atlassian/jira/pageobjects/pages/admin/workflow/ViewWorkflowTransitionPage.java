package com.atlassian.jira.pageobjects.pages.admin.workflow;

import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;

import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.openqa.selenium.By;

/**
 * View workflow transition page for adding post functions and editing specific transitions.
 *
 * @since v6.2
 */
public class ViewWorkflowTransitionPage extends AbstractJiraAdminPage
{
    private static final String WORKFLOW_TRANSITION_URL = "/secure/admin/workflows/ViewWorkflowTransition.jspa";

    private final String stepNumber;
    private final String workflowName;
    private final String workflowMode;
    private final String transitionNumber;
    private final URI uri;

    @ElementBy(className = "aui-page-panel-content")
    private PageElement workflowTransitionPanel;

    @ElementBy(id = "view_post_functions")
    private PageElement viewPostFunctionsTab;

    @ElementBy(id = "workflow-post-functions")
    private PageElement workflowPostFunctionsTable;

    public ViewWorkflowTransitionPage(final String workflowMode, final String workflowName, final String stepNumber, final String transitionNumber)
    {
        this.workflowMode = workflowMode;
        this.workflowName = workflowName;
        this.stepNumber = stepNumber;
        this.transitionNumber = transitionNumber;
        this.uri = UriBuilder.fromPath(WORKFLOW_TRANSITION_URL).queryParam("workflowMode", this.workflowMode)
                .queryParam("workflowName", this.workflowName).queryParam("workflowStep", this.stepNumber)
                .queryParam("workflowTransition", this.transitionNumber).build();
    }

    @Override
    public String linkId()
    {
        return "workflows";
    }

    @Override
    public TimedCondition isAt()
    {
        return workflowTransitionPanel.find(By.id("workflow-transition-header")).timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return uri.toString();
    }


    public AddWorkflowTransitionPostFunctionPage goToAddPostFunction()
    {
        viewPostFunctionsTab.click();
        final PageElement addPostFunctionLink = workflowPostFunctionsTable.find(By.className("criteria-post-function-add"), TimeoutType.COMPONENT_LOAD);
        Poller.waitUntilTrue(addPostFunctionLink.timed().isVisible());
        addPostFunctionLink.click();
        final PageElement postFunctionTable = elementFinder.find(By.name("jiraform"), TimeoutType.PAGE_LOAD);
        Poller.waitUntilTrue(postFunctionTable.timed().isPresent());
        return pageBinder.bind(AddWorkflowTransitionPostFunctionPage.class, workflowMode, workflowName, stepNumber, transitionNumber);
    }

    public List<String> getVisibleTabsIds() {
        final List<PageElement> tabLinks = elementFinder.findAll(By.cssSelector("#workflow-transition-info .menu-item a"));
        return Lists.transform(tabLinks, PageElements.getAttribute("id"));
    }

    public ViewWorkflowTransitionPage openTab(String id) {
        elementFinder.find(By.id(id)).click();
        return pageBinder.bind(ViewWorkflowTransitionPage.class, workflowMode, workflowName, stepNumber, transitionNumber);
    }

    public String getActiveTabHtml() {
        return PageElements.getAttribute("innerHTML").apply(elementFinder.find(By.className("active-pane")));
    }

    public String getActiveTabId() {
        return elementFinder.find(By.cssSelector("#workflow-transition-info .menu-item.active-tab a")).getAttribute("id");
    }
}
