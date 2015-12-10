package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.ApplicationUser;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.MockStepDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.ofbiz.core.entity.GenericValue;

/**
 * New Mock for JiraWorkflow.
 * Note that there is another existing MockJiraWorkflow, but it relies on the underlying OSWorkflow implementation.
 *
 * @since v3.13
 */
public class MockJiraWorkflow implements JiraWorkflow
{
    private final List<GenericValue> statusList = new ArrayList<GenericValue>();
    private final Map<GenericValue, StepDescriptor> statusToStepMap = new HashMap<GenericValue, StepDescriptor>();
    private String name;
    private boolean draftWorkflow = false;
    private WorkflowDescriptor workflowDescriptor;
    private final Collection<ActionDescriptor> actions = new ArrayList<ActionDescriptor>();

    public MockJiraWorkflow()
    {
    }

    public MockJiraWorkflow(String name)
    {
        this.name = name;
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

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return null;
    }

    public WorkflowDescriptor getDescriptor()
    {
        return workflowDescriptor;
    }

    public void setWorkflowDescriptor(final WorkflowDescriptor workflowDescriptor)
    {
        this.workflowDescriptor = workflowDescriptor;
    }

    public Collection<ActionDescriptor> getAllActions()
    {
        return actions;
    }

    public MockJiraWorkflow addAction(ActionDescriptor actionDescriptor)
    {
        actions.add(actionDescriptor);
        return this;
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
        return statusToStepMap.get(status);
    }

    public StepDescriptor getLinkedStep(final Status status)
    {
        return getLinkedStep(status.getGenericValue());
    }

    public List<GenericValue> getLinkedStatuses()
    {
        return statusList;
    }

    public List<Status> getLinkedStatusObjects()
    {
        return Lists.transform(statusList, new Function<GenericValue, Status>()
        {
            @Override
            public Status apply(@Nullable final GenericValue input)
            {
                return new MockStatus(input, null);
            }
        });
    }

    public Set<String> getLinkedStatusIds()
    {
        return ImmutableSet.copyOf(Lists.transform(getLinkedStatusObjects(), new Function<Status, String>()
        {
            @Override
            public String apply(@Nullable final Status input)
            {
                return input.getId();
            }
        }));
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
    {}

    public Collection<ActionDescriptor> getActionsForScreen(final FieldScreen fieldScreen)
    {
        return null;
    }

    public String getUpdateAuthorName()
    {
        return null;
    }

    public ApplicationUser getUpdateAuthor()
    {
        return null;
    }

    public Date getUpdatedDate()
    {
        return null;
    }

    public String getMode()
    {
        return null;
    }

    public int compareTo(final JiraWorkflow o)
    {
        return 0;
    }

    /**
     * Adds a step to this fake workflow with the given status
     */
    public void addStep(final int id, final String statusName)
    {
        final GenericValue gvStatus = new MockGenericValue("Status", EasyMap.build("name", statusName));
        statusList.add(gvStatus);
        final StepDescriptor step = new MockStepDescriptor(id);
        statusToStepMap.put(gvStatus, step);
    }

    public void clear()
    {
        statusList.clear();
        statusToStepMap.clear();
    }

    public boolean isDraftWorkflow()
    {
        return draftWorkflow;
    }

    public void setDraftWorkflow(final boolean draftWorkflow)
    {
        this.draftWorkflow = draftWorkflow;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
