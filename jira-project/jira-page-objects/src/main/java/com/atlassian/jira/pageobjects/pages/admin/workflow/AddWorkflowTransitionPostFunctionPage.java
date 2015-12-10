package com.atlassian.jira.pageobjects.pages.admin.workflow;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import org.openqa.selenium.By;

/**
 * Add workflow post function page
 *
 * @since v6.2
 */
public class AddWorkflowTransitionPostFunctionPage extends AbstractJiraPage
{
    private static final String WORKFLOW_TRANSITION_POST_FUNCTION_URL = "/secure/admin/workflows/AddWorkflowTransitionPostFunction!default.jspa";

    private final String stepNumber;
    private final String workflowName;
    private final String workflowMode;
    private final String transitionNumber;
    private final URI uri;

    @ElementBy (className = "aui-page-panel-content")
    private PageElement addPostFunctionPanel;

    @ElementBy (id = "descriptors_table")
    private PageElement postSelectionFunctionTable;

    @ElementBy (id = "add_submit")
    private PageElement submitButton;

    public AddWorkflowTransitionPostFunctionPage(final String workflowMode, final String workflowName, final String stepNumber, final String transitionNumber)
    {
        this.workflowMode = workflowMode;
        this.workflowName = workflowName;
        this.stepNumber = stepNumber;
        this.transitionNumber = transitionNumber;
        this.uri = UriBuilder.fromPath(WORKFLOW_TRANSITION_POST_FUNCTION_URL).queryParam("workflowMode", this.workflowMode)
                .queryParam("workflowName", this.workflowName).queryParam("workflowStep", this.stepNumber)
                .queryParam("workflowTransition", this.transitionNumber).build();
    }

    @Override
    public TimedCondition isAt()
    {
        return postSelectionFunctionTable.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return uri.toString();
    }

    public AddWorkflowTransitionFunctionParamsPage selectAndSubmitByName(final String postFunctionName)
    {
        final List<PageElement> postFunctions = postSelectionFunctionTable.find(By.tagName("tbody")).findAll(By.tagName("tr"));

        for (final PageElement postFunction : postFunctions)
        {
            final PageElement label = postFunction.find(By.tagName("label"));
            final String nameLabel = label.getText().trim();
            if (postFunctionName.equals(nameLabel))
            {
                final String pluginModuleKey = label.getAttribute("for").toString().trim();
                final PageElement selectedRadioButton = postFunction.find(By.tagName("input"));
                selectedRadioButton.click();
                submitButton.click();

                return pageBinder.bind(AddWorkflowTransitionFunctionParamsPage.class, workflowMode, workflowName, stepNumber, transitionNumber, pluginModuleKey);
            }
        }
        throw new IllegalArgumentException(String.format("Given post function name %s cannot be found in post function table", postFunctionName));
    }
}
