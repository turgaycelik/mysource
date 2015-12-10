package com.atlassian.jira.workflow;

import com.opensymphony.workflow.loader.WorkflowDescriptor;

/**
 * This class represents an draft workflow, that is a copy of an active
 * workflow that may be edited, (and eventually) be used to overwrite an active
 * workflow. It is stored in a separate database table from the main workflows.
 *
 * @since v3.13
 */
public class JiraDraftWorkflow extends AbstractJiraWorkflow
{
    private final String name;

    protected JiraDraftWorkflow(final String name, final WorkflowManager workflowManager, final WorkflowDescriptor workflowDescriptor)
    {
        super(workflowManager, workflowDescriptor);
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    /**
     * A draft Workflow is always editable.
     * @return true
     * @throws WorkflowException
     * @see JiraWorkflow#isEditable()
     */
    public boolean isEditable() throws WorkflowException
    {
        return true;
    }

    /**
     * This method will always return true as this implementation is always used for draft Workflows.
     * @return true
     * @see JiraWorkflow#isDraftWorkflow()
     */
    public boolean isDraftWorkflow()
    {
        return true;
    }
}
