/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 24, 2004
 * Time: 6:37:13 PM
 */
package com.atlassian.jira.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.user.ApplicationUser;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractJiraWorkflow implements JiraWorkflow
{
    private static final Logger log = Logger.getLogger(AbstractJiraWorkflow.class);

    WorkflowDescriptor descriptor;
    protected final WorkflowManager workflowManager;

    // Records the field screens that this workflow uses.
    private Multimap<FieldScreen, ActionDescriptor> fieldScreenActions = null;

    protected AbstractJiraWorkflow(final WorkflowManager workflowManager, final WorkflowDescriptor workflowDescriptor)
    {
        this.workflowManager = workflowManager;
        descriptor = workflowDescriptor;
    }

    public abstract String getName();

    public String getDescription()
    {
        return (String) descriptor.getMetaAttributes().get(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE);
    }

    @Override
    public String getDisplayName()
    {
        return getName();
    }

    public WorkflowDescriptor getDescriptor()
    {
        return descriptor;
    }

    public Collection<ActionDescriptor> getAllActions()
    {
        return getAllActionsMap().values();
    }

    public int getNextActionId()
    {
        int offset = 0;
        final SortedMap<Integer, ActionDescriptor> allActionsMap = getAllActionsMap();
        if (!allActionsMap.isEmpty())
        {
            offset = allActionsMap.lastKey().intValue();
        }

        return offset + 10;
    }

    private SortedMap<Integer, ActionDescriptor> getAllActionsMap()
    {
        final SortedMap<Integer, ActionDescriptor> actions = new TreeMap<Integer, ActionDescriptor>();

        // Register all initial actions
        addActionsToMap(actions, descriptor.getInitialActions());

        // Register all global actions
        addActionsToMap(actions, descriptor.getGlobalActions());

        // Register all common actions
        actions.putAll(descriptor.getCommonActions());

        // Register all normal actions
        final List steps = descriptor.getSteps();
        for (final Object step : steps)
        {
            final StepDescriptor stepDescriptor = (StepDescriptor) step;
            for (final Object o : stepDescriptor.getActions())
            {
                final ActionDescriptor actionDescriptor = (ActionDescriptor) o;
                // If the action id is already in the list - it is likely to be a common action :)
                // So no need to add it as it is already in the list
                if (!actions.containsKey(new Integer(actionDescriptor.getId())))
                {
                    actions.put(new Integer(actionDescriptor.getId()), actionDescriptor);
                }
            }
        }

        return actions;
    }

    private void addActionsToMap(final SortedMap<Integer, ActionDescriptor> actionMap, final Collection<ActionDescriptor> actions)
    {
        for (ActionDescriptor actionDescriptor : actions)
        {
            actionMap.put(new Integer(actionDescriptor.getId()), actionDescriptor);
        }
    }

    public Collection<ActionDescriptor> getActionsWithResult(final StepDescriptor stepDescriptor)
    {
        final Collection<ActionDescriptor> actions = getAllActions();
        actionloop : for (final Iterator iterator = actions.iterator(); iterator.hasNext();)
        {
            final ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();
            // Check all conditional results
            for (final Object o : actionDescriptor.getConditionalResults())
            {
                final ResultDescriptor resultDescriptor = (ResultDescriptor) o;
                if (resultDescriptor.getStep() == stepDescriptor.getId())
                {
                    // The step is a destination step for action's conditional result
                    // Leave the action in the collection
                    continue actionloop;
                }
            }

            // Now check the unconditional result
            if (actionDescriptor.getUnconditionalResult().getStep() != stepDescriptor.getId())
            {
                // If the step is not a destination of any conditional and unconditional result remove the action from the list
                iterator.remove();
            }
        }
        return actions;
    }

    public Collection getStepsWithAction(final StepDescriptor stepDescriptor)
    {
        // If global Action

        final Collection actions = getAllActions();
        actionloop : for (final Iterator iterator = actions.iterator(); iterator.hasNext();)
        {
            final ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();
            // Check all conditional results
            for (final Object o : actionDescriptor.getConditionalResults())
            {
                final ResultDescriptor resultDescriptor = (ResultDescriptor) o;
                if (resultDescriptor.getStep() == stepDescriptor.getId())
                {
                    // The step is a destination step for action's conditional result
                    // Leave the action in the collection
                    continue actionloop;
                }
            }

            // Now check the unconditional result
            if (actionDescriptor.getUnconditionalResult().getStep() != stepDescriptor.getId())
            {
                // If the step is not a destination of any conditional and unconditional result remove the action from the list
                iterator.remove();
            }
        }
        return actions;
    }

    public boolean removeStep(final StepDescriptor stepDescriptor)
    {
        // Remove any transitions that end in this step
        if (!getActionsWithResult(stepDescriptor).isEmpty())
        {
            throw new IllegalArgumentException("Cannot remove step - it is a destination step of at least one transition.");
        }

        return descriptor.getSteps().remove(stepDescriptor);
    }

    /* @return Workflow step associated with a status in this workflow, or null if not found. */
    public StepDescriptor getLinkedStep(final GenericValue status)
    {
        if (status == null)
        {
            throw new IllegalArgumentException();
        }

        return getLinkedStep(status.getString("id"));
    }

    public StepDescriptor getLinkedStep(final Status status)
    {
        if (status == null)
        {
            throw new IllegalArgumentException();
        }

        return getLinkedStep(status.getId());
    }

    private StepDescriptor getLinkedStep(final String statusId)
    {
        if (statusId == null)
        {
            throw new IllegalArgumentException();
        }
        for (final Object o : descriptor.getSteps())
        {
            final StepDescriptor stepDescriptor = (StepDescriptor) o;
            if (statusId.equals(stepDescriptor.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY)))
            {
                return stepDescriptor;
            }
        }
        // not found
        return null;
    }

    public GenericValue getLinkedStatus(final StepDescriptor stepDescriptor)
    {
        return ComponentAccessor.getConstantsManager().getStatus(getLinkedStatusId(stepDescriptor));
    }

    public Status getLinkedStatusObject(final StepDescriptor stepDescriptor)
    {
        return ComponentAccessor.getConstantsManager().getStatusObject(getLinkedStatusId(stepDescriptor));
    }

    @Override
    public String getLinkedStatusId(final StepDescriptor stepDescriptor)
    {
        if (stepDescriptor == null)
        {
            throw new IllegalArgumentException("Step cannot be null.");
        }

        if ((stepDescriptor.getMetaAttributes() != null) && stepDescriptor.getMetaAttributes().containsKey(STEP_STATUS_KEY))
        {
            return (String) stepDescriptor.getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY);
        }

        throw new IllegalStateException("Step with id '" + stepDescriptor.getId() + "' does not have a valid linked status.");
    }

    public List getLinkedStatuses()
    {
        final List statuses = new ArrayList();
        ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();

        for (final Object o : descriptor.getSteps())
        {
            final StepDescriptor stepDescriptor = (StepDescriptor) o;
            statuses.add(constantsManager.getStatus(getLinkedStatusId(stepDescriptor)));
        }

        return statuses;
    }

    public List /* <Status> */getLinkedStatusObjects()
    {
        final List statuses = new ArrayList();
        ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();

        for (final Object o : descriptor.getSteps())
        {
            final StepDescriptor stepDescriptor = (StepDescriptor) o;
            statuses.add(constantsManager.getStatusObject(getLinkedStatusId(stepDescriptor)));
        }

        return statuses;
    }

    public Set<String> getLinkedStatusIds()
    {
        final Set<String> ids = new HashSet();

        for (final Object o : descriptor.getSteps())
        {
            final StepDescriptor stepDescriptor = (StepDescriptor) o;
            ids.add(getLinkedStatusId(stepDescriptor));
        }

        return ImmutableSet.copyOf(ids);
    }

    public Collection getStepsForTransition(final ActionDescriptor actionDescriptor)
    {
        if (isInitialAction(actionDescriptor))
        {
            // There are no originating steps for the initial action
            return Collections.EMPTY_LIST;
        }

        if (isGlobalAction(actionDescriptor))
        {
            // Global actions are available from everywhere
            return getDescriptor().getSteps();
        }

        if (isCommonAction(actionDescriptor))
        {
            final Collection steps = new LinkedList();

            for (final Object o1 : descriptor.getSteps())
            {
                final StepDescriptor stepDescriptor = (StepDescriptor) o1;
                for (final Object o : stepDescriptor.getCommonActions())
                {
                    if (((Integer) o).intValue() == actionDescriptor.getId())
                    {
                        steps.add(stepDescriptor);
                    }
                }
            }

            return steps;
        }
        else
        {
            final Collection steps = new LinkedList();

            for (final Object o1 : descriptor.getSteps())
            {
                final StepDescriptor stepDescriptor = (StepDescriptor) o1;
                for (final Object o : stepDescriptor.getActions())
                {
                    final ActionDescriptor ad = (ActionDescriptor) o;
                    if (ad.getId() == actionDescriptor.getId())
                    {
                        steps.add(stepDescriptor);
                        // If the action is a 'ordinary action' then it is available from only one step
                        return steps;
                    }
                }
            }

            // Could not find any steps
            return Collections.EMPTY_LIST;
        }
    }

    public Collection<FunctionDescriptor> getPostFunctionsForTransition(final ActionDescriptor actionDescriptor)
    {
        final Collection<FunctionDescriptor> allPostFunctions = new ArrayList<FunctionDescriptor>();

        if ((actionDescriptor.getUnconditionalResult() != null) && (actionDescriptor.getUnconditionalResult().getPostFunctions() != null))
        {
            allPostFunctions.addAll(actionDescriptor.getUnconditionalResult().getPostFunctions());
        }

        final List conditionalResults = actionDescriptor.getConditionalResults();
        if (conditionalResults != null)
        {
            for (final Object conditionalResult : conditionalResults)
            {
                final ResultDescriptor resultDescriptor = (ResultDescriptor) conditionalResult;
                allPostFunctions.addAll(resultDescriptor.getPostFunctions());
            }
        }

        if (actionDescriptor.getPostFunctions() != null)
        {
            allPostFunctions.addAll(actionDescriptor.getPostFunctions());
        }

        return allPostFunctions;
    }

    public boolean isActive() throws WorkflowException
    {
        return workflowManager.isActive(this);
    }

    public boolean isSystemWorkflow() throws WorkflowException
    {
        return workflowManager.isSystemWorkflow(this);
    }

    public boolean isEditable() throws WorkflowException
    {
        return !isSystemWorkflow() && !isActive();
    }

    public boolean isDefault()
    {
        return DEFAULT_WORKFLOW_NAME.equals(getName());
    }

    public boolean isInitialAction(final ActionDescriptor actionDescriptor)
    {
        return getDescriptor().getInitialActions().contains(actionDescriptor);
    }

    public boolean isCommonAction(final ActionDescriptor actionDescriptor)
    {
        return actionDescriptor.isCommon();
    }

    public boolean isGlobalAction(final ActionDescriptor actionDescriptor)
    {
        return getDescriptor().getGlobalActions().contains(actionDescriptor);
    }

    public boolean isOrdinaryAction(final ActionDescriptor actionDescriptor)
    {
        return !(isInitialAction(actionDescriptor) || isCommonAction(actionDescriptor) || isGlobalAction(actionDescriptor));
    }

    public String getActionType(final ActionDescriptor actionDescriptor)
    {
        if (actionDescriptor == null)
        {
            throw new IllegalArgumentException("ActionDescriptor cannot be null.");
        }

        if (isInitialAction(actionDescriptor))
        {
            return ACTION_TYPE_INITIAL;
        }
        else if (isGlobalAction(actionDescriptor))
        {
            return ACTION_TYPE_GLOBAL;
        }
        else if (isCommonAction(actionDescriptor))
        {
            return ACTION_TYPE_COMMON;
        }
        else if (isOrdinaryAction(actionDescriptor))
        {
            return ACTION_TYPE_ORDINARY;
        }

        throw new IllegalArgumentException("The action with id '" + actionDescriptor.getId() + "' is of unknown type.");
    }

    public void reset()
    {
        fieldScreenActions = null;
    }

    public Collection<ActionDescriptor> getActionsForScreen(final FieldScreen fieldScreen)
    {
        if (fieldScreenActions == null)
            fieldScreenActions = loadFieldScreenActions();

        if (fieldScreenActions.containsKey(fieldScreen))
        {
            return fieldScreenActions.get(fieldScreen);
        }

        return Collections.emptyList();
    }

    private Multimap<FieldScreen, ActionDescriptor> loadFieldScreenActions()
    {
        final WorkflowActionsBean workflowActionsBean = new WorkflowActionsBean();
        Multimap<FieldScreen, ActionDescriptor> map = HashMultimap.create();
        for (final ActionDescriptor actionDescriptor : getAllActions())
        {
            map.put(workflowActionsBean.getFieldScreenForView(actionDescriptor), actionDescriptor);
        }
        return map;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractJiraWorkflow))
        {
            return false;
        }

        final AbstractJiraWorkflow abstractJiraWorkflow = (AbstractJiraWorkflow) o;

        return getName().equals(abstractJiraWorkflow.getName());
    }

    @Override
    public int hashCode()
    {
        return (getName() != null ? getName().hashCode() : 0);
    }

    public int compareTo(final JiraWorkflow o)
    {
        return JiraWorkflowComparator.COMPARATOR.compare(this, o);
    }

    public String getUpdateAuthorName()
    {
        ApplicationUser user =  getUpdateAuthor();
        if(ComponentAccessor.getUserManager().isUserExisting(user)){
            return user.getUsername();
        }
        return null;

    }

    public ApplicationUser getUpdateAuthor(){
        String updateAuthor = null;
        final Map metaAttributes = descriptor.getMetaAttributes();
        if (metaAttributes != null)
        {
            updateAuthor = (String) metaAttributes.get(JIRA_META_UPDATE_AUTHOR_KEY);
        }
        return ComponentAccessor.getUserManager().getUserByKeyEvenWhenUnknown(updateAuthor);
    }

    public Date getUpdatedDate()
    {
        final Map metaAttributes = descriptor.getMetaAttributes();
        if (metaAttributes != null)
        {
            final String updateDateStr = (String) metaAttributes.get(JIRA_META_UPDATED_DATE);
            if (updateDateStr != null)
            {
                try
                {
                    final long timeInMillis = Long.parseLong(updateDateStr);
                    return new Date(timeInMillis);
                }
                catch (final NumberFormatException e)
                {
                    log.error("The workflow '" + getName() + "' is storing a invalid updated date string '" + updateDateStr + "'.", e);
                }
            }
        }
        return null;
    }

    public boolean hasDraftWorkflow()
    {
        // Test if we can get a draft workflow with our name from the manager.
        return workflowManager.getDraftWorkflow(getName()) != null;
    }

    public String getMode()
    {
        if (isDraftWorkflow())
        {
            return DRAFT;
        }
        else
        {
            return LIVE;
        }
    }
}