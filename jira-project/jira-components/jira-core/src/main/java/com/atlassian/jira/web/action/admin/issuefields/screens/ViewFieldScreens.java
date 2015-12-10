package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class ViewFieldScreens extends AbstractFieldScreenAction
{
    private String confirm;
    private Map fieldScreenSchemeMap;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private final WorkflowManager workflowManager;
    private List<JiraWorkflow> workflowsIncludingDrafts;

    public ViewFieldScreens(FieldScreenManager fieldScreenManager, FieldScreenSchemeManager fieldScreenSchemeManager, WorkflowManager workflowManager)
    {
        super(fieldScreenManager);
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.workflowManager = workflowManager;
        fieldScreenSchemeMap = new HashMap();
    }

    protected String doExecute() throws Exception
    {
        return getResult();
    }

    public String doAddNewFieldScreen() throws Exception
    {
        return INPUT;
    }

    @RequiresXsrfCheck
    public String doAddFieldScreen()
    {
        // Ensure no screen with this name already exists
        validateScreenName();

        if (!invalidInput())
        {
            FieldScreen fieldScreen =  new FieldScreenImpl(fieldScreenManager);
            fieldScreen.setName(getFieldScreenName());
            fieldScreen.setDescription(getFieldScreenDescription());
            fieldScreen.store();
            fieldScreen.addTab(getText("admin.field.screen.default"));
            return returnCompleteWithInlineRedirect("ConfigureFieldScreen.jspa?id=" + fieldScreen.getId());
        }
        else
        {
            return ERROR;
        }
    }

    public String doViewDeleteFieldScreen()
    {
        validateId();

        if (invalidInput())
        {
            return getResult();
        }

        // Ensure the field screen is not used in any Field Screen Schemes
        if (!isDeletable(getFieldScreen()))
        {
            addErrorMessage(getText("admin.errors.screens.cannot.delete.screen.used.screen.schemes"));
        }

        if (!TextUtils.stringSet(confirm))
        {
            return "confirm";
        }

       return redirectToView();
    }

    @RequiresXsrfCheck
    public String doDeleteFieldScreen()
    {
        validateId();

        if (invalidInput())
        {
            return getResult();
        }

        // Ensure the field screen is not used in any Field Screen Schemes
        if (!isDeletable(getFieldScreen()))
        {
            addErrorMessage(getText("admin.errors.screens.cannot.delete.screen.used.screen.schemes"));
        }

        getFieldScreen().remove();
        return redirectToView();
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public Collection getFieldScreenSchemes(FieldScreen fieldScreen)
    {
        // Use id for caching as it hashCOde and equals methods are so much simpler :)
        if (!fieldScreenSchemeMap.containsKey(fieldScreen.getId()))
        {
            fieldScreenSchemeMap.put(fieldScreen.getId(), fieldScreenSchemeManager.getFieldScreenSchemes(fieldScreen));
        }

        return (Collection) fieldScreenSchemeMap.get(fieldScreen.getId());
    }

    public Map<JiraWorkflow, Collection<ActionDescriptor>> getWorkflows(FieldScreen fieldScreen)
    {
        Map<JiraWorkflow, Collection<ActionDescriptor>> workflows = new TreeMap<JiraWorkflow, Collection<ActionDescriptor>>();
        for (final JiraWorkflow workflow : workflowManager.getWorkflows())
        {
            Collection<ActionDescriptor> actions = workflow.getActionsForScreen(fieldScreen);
            if (actions != null && !actions.isEmpty())
            {
                workflows.put(workflow, actions);
            }
        }
        return workflows;
    }

    public Collection<WorkflowTransitionViewHelper> getWorkflowTransitionViews(FieldScreen fieldScreen)
    {
        List<WorkflowTransitionViewHelper> answer = new ArrayList<WorkflowTransitionViewHelper>();
        for (final JiraWorkflow workflow : workflowManager.getWorkflows())
        {
            Collection<ActionDescriptor> actions = workflow.getActionsForScreen(fieldScreen);
            if (actions != null && !actions.isEmpty())
            {
                for (ActionDescriptor action : actions)
                {
                    answer.add(new WorkflowTransitionViewHelper(workflow, action));
                }
            }
        }
        Collections.sort(answer);
        return answer;
    }

    private boolean hasWorkflowsIncludingDrafts(FieldScreen fieldScreen) {
        for (JiraWorkflow workflow: getWorkflowsIncludingDrafts())
        {
            Collection<ActionDescriptor> actions = workflow.getActionsForScreen(fieldScreen);
            if (actions != null && !actions.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private List<JiraWorkflow> getWorkflowsIncludingDrafts()
    {
        // Cache this call as it is expensive.
        if (workflowsIncludingDrafts == null)
        {
            workflowsIncludingDrafts = workflowManager.getWorkflowsIncludingDrafts();
        }
        return workflowsIncludingDrafts;
    }

    public boolean isDeletable(FieldScreen fieldScreen)
    {
        // Check if the field screen is used by any field screen schemes or workflows;
        return getFieldScreenSchemes(fieldScreen).isEmpty() && !hasWorkflowsIncludingDrafts(fieldScreen);
    }

    /**
     * Facilitates creating view list of the workflow transitions for the viewfieldscreen
     * page purposes. 
     *
     */
    public static class WorkflowTransitionViewHelper implements Comparable<WorkflowTransitionViewHelper>
    {
        private final JiraWorkflow workflow;
        private final ActionDescriptor transition;
        private final Collection<StepDescriptor> steps;


        private WorkflowTransitionViewHelper(final JiraWorkflow workflow, final ActionDescriptor transition)
        {
            this.workflow = workflow;
            this.transition = transition;
            this.steps = initTransitionSteps();
        }

        private Collection<StepDescriptor> initTransitionSteps()
        {
            return workflow.getStepsForTransition(transition);
        }

        public int compareTo(final WorkflowTransitionViewHelper other)
        {
            return this.workflow.compareTo(other.workflow);
        }

        public String getWorkflowName()
        {
            return workflow.getName();
        }

        public String getWorkflowMode()
        {
            return workflow.getMode();
        }

        public String getTransitionName()
        {
            return transition.getName();
        }

        public int transitionId()
        {
            return transition.getId();
        }

        public boolean hasSteps()
        {
            return !steps.isEmpty();
        }

        public StepDescriptor getFirstStep()
        {
            if (!hasSteps())
            {
                throw new IllegalStateException("No steps for " + this);
            }
            return steps.iterator().next();
        }

        public boolean isGlobalAction()
        {
            return workflow.isGlobalAction(transition);
        }

        @Override
        public String toString()
        {
            return asString("WorkflowTransition[Workflow=",workflow.getName(),",transition=",transition.getId(),"]");
        }
    }

}
