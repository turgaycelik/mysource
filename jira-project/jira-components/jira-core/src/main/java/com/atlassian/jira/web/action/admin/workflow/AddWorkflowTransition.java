/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.ResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@WebSudoRequired
public class AddWorkflowTransition extends AbstractWorkflowTransition
{
    private final PluginAccessor pluginAccessor;
    private final FieldScreenManager fieldScreenManager;
    private Collection fieldScreens;

    public AddWorkflowTransition(JiraWorkflow workflow, StepDescriptor step, PluginAccessor pluginAccessor,
            FieldScreenManager fieldScreenManager, WorkflowService workflowService)
    {
        super(workflow, step, workflowService);
        this.pluginAccessor = pluginAccessor;
        this.fieldScreenManager = fieldScreenManager;
    }

    public String doDefault() throws Exception
    {
        if (workflowService.isStepOnDraftWithNoTransitionsOnParentWorkflow(getJiraServiceContext(), getWorkflow(),
                getStep().getId()))
        {
            addErrorMessage(getText("admin.workflowtransitions.error.add.transition.draft.step.without.transition",
                    getStep().getName()));
        }
        return super.doDefault();
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(getTransitionName()))
        {
            addError("transitionName", getText("admin.common.errors.validname"));
        }
        else
        {
            // Check if an action with this name already is defined for the step (normal or common action)
            checkDuplicateTransitionName(getStep().getActions(), getTransitionName());

            if (!invalidInput())
            {
                // Check the global actions as well (as after all, they are global)
                checkDuplicateTransitionName(getWorkflow().getDescriptor().getGlobalActions(), getTransitionName());
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

        if (workflowService.isStepOnDraftWithNoTransitionsOnParentWorkflow(getJiraServiceContext(), getWorkflow(),
                getStep().getId()))
        {
            addErrorMessage(getText("admin.workflowtransitions.error.add.transition.draft.step.without.transition",
                    getStep().getName()));
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
        ActionDescriptor action = DescriptorFactory.getFactory().createActionDescriptor();
        action.setId(getWorkflow().getNextActionId());
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

        // setup the result
        ResultDescriptor result = DescriptorFactory.getFactory().createResultDescriptor();
        action.setUnconditionalResult(result);
        result.setStep(getDestinationStep());
        result.setOldStatus("Not Done");
        result.setStatus("Done");

        initialiseTransition(action);

        action.setParent(getStep());
        getStep().getActions().add(action);

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        return getRedirect(getCancelUrl());
    }

    public String getCancelUrl()
    {
        if ("viewWorkflowStep".equals(getOriginatingUrl()))
        {
            return "ViewWorkflowStep.jspa" + getBasicWorkflowParameters() +
                   "&workflowStep=" + getStep().getId();
        }
        else
        {
            return "ViewWorkflowSteps.jspa" + getBasicWorkflowParameters();
        }
    }

    public Collection getFieldScreens()
    {
        if (fieldScreens == null)
        {
            fieldScreens = fieldScreenManager.getFieldScreens();
        }

        return fieldScreens;
    }

    private void initialiseTransition(ActionDescriptor actionDescriptor) throws PluginParseException
    {
        final ResultDescriptor unconditionalResult = actionDescriptor.getUnconditionalResult();

        final List postFunctions = unconditionalResult.getPostFunctions();

        Map functionsToAdd = new TreeMap();

        // Find all the 'default' functions and add them ot the transition
        final List moduleDescriptors = pluginAccessor.getEnabledModuleDescriptorsByType(JiraWorkflowPluginConstants.MODULE_NAME_WORKFLOW_FUNCTION);
        for (final Object moduleDescriptor : moduleDescriptors)
        {
            WorkflowFunctionModuleDescriptor functionModuleDescriptor = (WorkflowFunctionModuleDescriptor) moduleDescriptor;
            if (functionModuleDescriptor.isDefault())
            {
                // Build Function Descriptor
                FunctionDescriptor functionDescriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
                functionDescriptor.setType("class");
                WorkflowPluginFunctionFactory functionFactory = (WorkflowPluginFunctionFactory) functionModuleDescriptor.getModule();
                functionDescriptor.getArgs().put("class.name", functionModuleDescriptor.getImplementationClass().getName());
                functionDescriptor.getArgs().put("full.module.key", getFullModuleKey(functionModuleDescriptor.getPluginKey(), functionModuleDescriptor.getKey()));
                functionDescriptor.getArgs().putAll(functionFactory.getDescriptorParams(Collections.EMPTY_MAP));

                if (functionModuleDescriptor.getWeight() != null)
                {
                    functionsToAdd.put(functionModuleDescriptor.getWeight(), functionDescriptor);
                }
                else
                {
                    functionsToAdd.put(new Integer(Integer.MAX_VALUE), functionDescriptor);
                }
            }
        }

        for (final Object o : functionsToAdd.entrySet())
        {
            Map.Entry entry = (Map.Entry) o;
            postFunctions.add(entry.getValue());
        }
    }
}