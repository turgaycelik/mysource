/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 6:12:50 PM
 */
package com.atlassian.jira.workflow;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.workflow.function.event.FireIssueEventFunction;
import com.atlassian.jira.workflow.function.issue.IssueCreateFunction;
import com.atlassian.jira.workflow.function.issue.IssueReindexFunction;
import com.atlassian.jira.workflow.validator.PermissionValidator;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

public class ConfigurableJiraWorkflow extends AbstractJiraWorkflow
{
    String name;

    public ConfigurableJiraWorkflow(final String name, final WorkflowDescriptor workflowDescriptor, final WorkflowManager workflowManager)
    {
        super(workflowManager, workflowDescriptor);
        this.name = name;
    }

    public ConfigurableJiraWorkflow(final String name, final WorkflowManager workflowManager)
    {
        super(workflowManager, new WorkflowDescriptor());
        this.name = name;

        // create the initial step - useful to have one of these :)
        final StepDescriptor step = DescriptorFactory.getFactory().createStepDescriptor();
        step.setId(1);
        step.setName("Open");
        step.getMetaAttributes().put(JiraWorkflow.STEP_STATUS_KEY, "1");
        step.setParent(descriptor);
        descriptor.addStep(step);

        // create the initial action
        final ActionDescriptor initialAction = DescriptorFactory.getFactory().createActionDescriptor();
        initialAction.setId(1);
        initialAction.setName("Create");
        initialAction.setParent(descriptor);
        descriptor.addInitialAction(initialAction);

        // add create issue permission validator
        initialAction.getValidators().add(PermissionValidator.makeDescriptor("Create Issue"));

        // setup result to always be step 1
        final ResultDescriptor resultDescriptor = DescriptorFactory.getFactory().createResultDescriptor();
        resultDescriptor.setStep(1);
        resultDescriptor.setStatus("open");
        initialAction.setUnconditionalResult(resultDescriptor);

        // setup post functions
        resultDescriptor.getPostFunctions().add(IssueCreateFunction.makeDescriptor());
        resultDescriptor.getPostFunctions().add(IssueReindexFunction.makeDescriptor());
        resultDescriptor.getPostFunctions().add(FireIssueEventFunction.makeDescriptor(EventType.ISSUE_CREATED_ID));
    }

    public String getName()
    {
        return name;
    }

    /**
     * This method will always return false as this implementation is not used for DraftWorkflows.
     * @since v3.13
     * @return false
     */
    public boolean isDraftWorkflow()
    {
        return false;
    }

    public void setDescription(final String description)
    {
        descriptor.getMetaAttributes().put(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE, description);
    }

    public void setDescriptor(final WorkflowDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

}