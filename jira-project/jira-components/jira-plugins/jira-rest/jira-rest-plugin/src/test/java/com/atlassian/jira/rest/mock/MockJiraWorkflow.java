package com.atlassian.jira.rest.mock;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;

import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.2
 */
public class MockJiraWorkflow implements JiraWorkflow
{
    private final String name;
    private final String description;
    private final WorkflowDescriptor workflowDescriptor = DescriptorFactory.getFactory().createWorkflowDescriptor();
    private String updatedAuthorName;
    private ApplicationUser updateAuthor;
    private Date updatedDate;

    public MockJiraWorkflow(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String getDisplayName()
    {
        return getName();
    }

    public String getDescription()
    {
        return description;
    }

    public WorkflowDescriptor getDescriptor()
    {
        return workflowDescriptor;
    }

    public Collection<ActionDescriptor> getAllActions()
    {
        return null;
    }

    public Collection<ActionDescriptor> getActionsWithResult(final StepDescriptor stepDescriptor)
    {
        return null;
    }

    public boolean removeStep(final StepDescriptor stepDescriptor)
    {
        return false;
    }

    public StepDescriptor getLinkedStep(final GenericValue status)
    {
        return null;
    }

    public StepDescriptor getLinkedStep(final Status status)
    {
        return getLinkedStep(status.getGenericValue());
    }

    public List<GenericValue> getLinkedStatuses()
    {
        return null;
    }

    public List<Status> getLinkedStatusObjects()
    {
        return null;
    }

    public Set<String> getLinkedStatusIds()
    {
        return null;
    }

    public boolean isActive() throws WorkflowException
    {
        return true;
    }

    public boolean isSystemWorkflow() throws WorkflowException
    {
        return false;
    }

    public boolean isEditable() throws WorkflowException
    {
        return false;
    }

    public boolean isDefault()
    {
        return false;
    }

    public boolean hasDraftWorkflow()
    {
        return false;
    }

    public int getNextActionId()
    {
        return 0;
    }

    public Collection<StepDescriptor> getStepsForTransition(final ActionDescriptor action)
    {
        return null;
    }

    public Collection<FunctionDescriptor> getPostFunctionsForTransition(final ActionDescriptor actionDescriptor)
    {
        return null;
    }

    public boolean isInitialAction(final ActionDescriptor actionDescriptor)
    {
        return false;
    }

    public boolean isCommonAction(final ActionDescriptor actionDescriptor)
    {
        return false;
    }

    public boolean isGlobalAction(final ActionDescriptor actionDescriptor)
    {
        return false;
    }

    public boolean isOrdinaryAction(final ActionDescriptor actionDescriptor)
    {
        return false;
    }

    public GenericValue getLinkedStatus(final StepDescriptor stepDescriptor)
    {
        return null;
    }

    public Status getLinkedStatusObject(final StepDescriptor stepDescriptor)
    {
        return null;
    }

    @Override
    public String getLinkedStatusId(final StepDescriptor stepDescriptor)
    {
        return null;
    }

    public String getActionType(final ActionDescriptor actionDescriptor)
    {
        return null;
    }

    public void reset()
    {
    }

    public Collection<ActionDescriptor>  getActionsForScreen(final FieldScreen fieldScreen)
    {
        return null;
    }

    public String getUpdateAuthorName()
    {
        return updatedAuthorName;
    }

    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    public ApplicationUser getUpdateAuthor()
    {
        return updateAuthor;
    }

    public MockJiraWorkflow setUpdateAuthor(ApplicationUser updateAuthor)
    {
        this.updateAuthor = updateAuthor;
        return this;
    }

    public String getMode()
    {
        return null;
    }

    public int compareTo(final JiraWorkflow o)
    {
        return 0;
    }

    public boolean isDraftWorkflow()
    {
        return false;
    }

    public MockJiraWorkflow setUpdatedAuthorName(String updatedAuthorName)
    {
        this.updatedAuthorName = updatedAuthorName;
        return this;
    }

    public MockJiraWorkflow setUpdatedDate(Date updatedDate)
    {
        this.updatedDate = updatedDate;
        return this;
    }
}
