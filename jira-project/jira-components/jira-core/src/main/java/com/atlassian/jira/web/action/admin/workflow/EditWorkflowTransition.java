package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditWorkflowTransition extends AbstractWorkflowTransition {
    private final ActionDescriptor action;
    private final FieldScreenManager fieldScreenManager;
    private final WorkflowActionsBean workflowActionsBean;
    private Collection fieldScreens;

    public EditWorkflowTransition(JiraWorkflow workflow, ActionDescriptor action, FieldScreenManager fieldScreenManager,
            WorkflowService workflowService)
    {
        super(workflow, workflowService);
        this.action = action;
        this.fieldScreenManager = fieldScreenManager;
        this.workflowActionsBean = new WorkflowActionsBean();
    }

    public EditWorkflowTransition(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor action,
            FieldScreenManager fieldScreenManager, WorkflowService workflowService)
    {
        super(workflow, step, workflowService);
        this.action = action;
        this.fieldScreenManager = fieldScreenManager;
        this.workflowActionsBean = new WorkflowActionsBean();
    }

    public String doDefault() throws Exception
    {
        setTransitionName(action.getName());
        setDescription((String) action.getMetaAttributes().get(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE));
        setDestinationStep(action.getUnconditionalResult().getStep());

        FieldScreen fieldScreen = workflowActionsBean.getFieldScreenForView(action);
        setView(fieldScreen != null ? fieldScreen.getId().toString() : "");

        return super.doDefault();
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(getTransitionName()))
        {
            addError("transitionName", getText("admin.errors.workflows.name.must.be.specified"));
        }
        else
        {
            // Only check the name if it has changed
            if (!getTransitionName().equalsIgnoreCase(action.getName()))
            {
                // Check if the action is not a global action
                if (getStep() != null)
                {
                    // Check if an action with this name already is defined for the step (normal or common action)
                    checkDuplicateTransitionName(getStep().getActions(), getTransitionName());
                }

                if (!invalidInput())
                {
                    // Check the global actions as well (as after all, they are global)
                    checkDuplicateTransitionName(getWorkflow().getDescriptor().getGlobalActions(), getTransitionName());
                }
            }
        }

        // Ensure the screen exists
        if (TextUtils.stringSet(getView()))
        {
            if (getFieldScreen() == null)
            {
                addError("view", getText("admin.errors.workflows.invalid.screen"));
            }
        }

        super.doValidation();
    }

    private FieldScreen getFieldScreen()
    {
        return fieldScreenManager.getFieldScreen(new Long(getView()));
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // setup the transition action
        action.setName(getTransitionName());
        action.getMetaAttributes().put(JiraWorkflow.WORKFLOW_DESCRIPTION_ATTRIBUTE, getDescription());
        if (TextUtils.stringSet(getView()))
        {
            action.setView(WorkflowTransitionUtil.VIEW_SCREEN);
            action.getMetaAttributes().put("jira.fieldscreen.id", getFieldScreen().getId().toString());
        }
        else
        {
            action.setView(null);
        }

        // setup the destination step result
        ResultDescriptor result = action.getUnconditionalResult();
        result.setStep(getDestinationStep());

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        return getRedirect(getCancelUrl());
    }

    public String getCancelUrl()
    {
        StringBuilder url = new StringBuilder("ViewWorkflowTransition.jspa")
                            .append(getBasicWorkflowParameters());

        if (getStep() != null)
        {
            url.append("&workflowStep=" + getStep().getId());
        }

        url.append("&workflowTransition=" + action.getId());
        return url.toString();
    }

    public ActionDescriptor getTransition()
    {
        return action;
    }

    public String isNameI8n()
    {
        ActionDescriptor transition = getTransition();
        return (String) transition.getMetaAttributes().get(JiraWorkflow.JIRA_META_ATTRIBUTE_I18N);
    }

    public boolean isSetView()
    {
        // Only allow to set the view if the transition is not an initial transition.
        return !getWorkflow().isInitialAction(getTransition());
    }

    public Collection getFieldScreens()
    {
        if (fieldScreens == null)
        {
            fieldScreens = fieldScreenManager.getFieldScreens();
        }

        return fieldScreens;
    }

    public List getTransitionSteps()
    {
        List transitions = getWorkflow().getDescriptor().getSteps();
        if(getTransition().getUnconditionalResult().getStep() == JiraWorkflow.ACTION_ORIGIN_STEP_ID)
        {
            StepDescriptor dummyStep = new DescriptorFactory().createStepDescriptor();
            dummyStep.setId(JiraWorkflow.ACTION_ORIGIN_STEP_ID);
            dummyStep.setName(getText("admin.workflowtransitions.same.destination.step"));
            transitions.add(dummyStep);
        }
        return transitions;
    }

    public boolean isGlobal()
    {
        return getWorkflow().isGlobalAction(getTransition());
    }
}
