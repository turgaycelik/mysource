/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.admin.workflow.analytics.WorkflowTransitionTabEvent;
import com.atlassian.jira.web.action.util.workflow.WorkflowEditorTransitionConditionUtil;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.atlassian.jira.workflow.tabs.WorkflowTransitionTabProvider;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionalResultDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import org.ofbiz.core.entity.GenericValue;

import webwork.action.ActionContext;

@WebSudoRequired
public class ViewWorkflowTransition extends AbstractWorkflowTransitionAction
{
    private final ConstantsManager constantsManager;
    private final CollectionReorderer collectionReorderer;
    private final WorkflowActionsBean workflowActionsBean;
    private final WorkflowTransitionTabProvider workflowTransitionTabProvider;
    private final EventPublisher eventPublisher;

    private int up;
    private int down;

    private String count;
    private String currentCount;

    private String descriptorTab;

    public ViewWorkflowTransition(JiraWorkflow workflow, StepDescriptor step, ActionDescriptor transition,
            PluginAccessor pluginAccessor, ConstantsManager constantsManager, CollectionReorderer collectionReorderer,
            WorkflowService workflowService, WorkflowTransitionTabProvider workflowTransitionTabProvider, final EventPublisher eventPublisher)
    {
        super(workflow, step, transition, pluginAccessor, workflowService);
        this.constantsManager = constantsManager;
        this.collectionReorderer = collectionReorderer;
        this.eventPublisher = eventPublisher;
        this.workflowActionsBean = new WorkflowActionsBean();
        this.workflowTransitionTabProvider = workflowTransitionTabProvider;
    }

    public ViewWorkflowTransition(JiraWorkflow workflow, ActionDescriptor transition, PluginAccessor pluginAccessor,
            ConstantsManager constantsManager, CollectionReorderer collectionReorderer, WorkflowService workflowService,
            WorkflowTransitionTabProvider workflowTransitionTabProvider, final EventPublisher eventPublisher)
    {
        // Used for working with global actions 
        super(workflow, transition, pluginAccessor, workflowService);
        this.constantsManager = constantsManager;
        this.collectionReorderer = collectionReorderer;
        this.eventPublisher = eventPublisher;
        this.workflowActionsBean = new WorkflowActionsBean();
        this.workflowTransitionTabProvider = workflowTransitionTabProvider;
    }

    public StepDescriptor getStepDescriptor(ConditionalResultDescriptor conditionalResultDescriptor)
    {
        final int targetStepId = conditionalResultDescriptor.getStep();
        return getWorkflow().getDescriptor().getStep(targetStepId);
    }

    public GenericValue getStatus(String id)
    {
        return constantsManager.getStatus(id);
    }


    public Status getStatusObject(String id)
    {
        return constantsManager.getStatusObject(id);
    }

    @RequiresXsrfCheck
    public String doMoveWorkflowFunctionUp() throws Exception
    {
        final List postFunctions = getTransition().getUnconditionalResult().getPostFunctions();

        if (up <= 0 || up >= postFunctions.size())
        {
            addErrorMessage(getText("admin.errors.workflows.invalid.index", "" + up));
        }
        else
        {
            Object toMove = postFunctions.get(up);
            collectionReorderer.increasePosition(postFunctions, toMove);
            workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());
        }

        return getViewRedirect("&currentCount=workflow-function" + (up));
    }

    @RequiresXsrfCheck
    public String doMoveWorkflowFunctionDown() throws Exception
    {
        final List postFunctions = getTransition().getUnconditionalResult().getPostFunctions();

        if (down < 0 || down >= (postFunctions.size() - 1))
        {
            addErrorMessage(getText("admin.errors.workflows.invalid.index", "" + down));
        }
        else
        {
            Object toMove = postFunctions.get(down);
            collectionReorderer.decreasePosition(postFunctions, toMove);
            workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());
        }

        return getViewRedirect("&currentCount=workflow-function" + (down + 2));
    }

    @RequiresXsrfCheck
    public String doChangeLogicOperator() throws Exception
    {
        WorkflowEditorTransitionConditionUtil wetcu = new WorkflowEditorTransitionConditionUtil();
        wetcu.changeLogicOperator(getTransition(), getCount());

        workflowService.updateWorkflow(getJiraServiceContext(), getWorkflow());

        return getViewRedirect("");
    }

    protected String getViewRedirect(String postfix)
    {
        if (getStep() == null)
        {
            return getRedirect("ViewWorkflowTransition.jspa" + getBasicWorkflowParameters() +
                    "&workflowTransition=" + getTransition().getId() + postfix);
        }
        else
        {
            return getRedirect("ViewWorkflowTransition.jspa" + getBasicWorkflowParameters() +
                    "&workflowStep=" + getStep().getId() +
                    "&workflowTransition=" + getTransition().getId() + postfix);
        }
    }

    public int getUp()
    {
        return up;
    }

    public void setUp(int up)
    {
        this.up = up;
    }

    public int getDown()
    {
        return down;
    }

    public void setDown(int down)
    {
        this.down = down;
    }

    public Collection getStepsForTransition()
    {
        return getWorkflow().getStepsForTransition(getTransition());
    }

    public boolean isInitial()
    {
        return getWorkflow().isInitialAction(getTransition());
    }

    public boolean isGlobal()
    {
        return getWorkflow().isGlobalAction(getTransition());
    }

    public boolean isCommon()
    {
        return getWorkflow().isCommonAction(getTransition());
    }

    public boolean isTransitionWithoutStepChange()
    {
        return getTransition().getUnconditionalResult().getStep() == JiraWorkflow.ACTION_ORIGIN_STEP_ID;
    }

    public String getDescriptorTab()
    {

        if (!TextUtils.stringSet(descriptorTab))
        {
            descriptorTab = (String) ActionContext.getSession().get(SessionKeys.WF_EDITOR_TRANSITION_TAB);
        }

        List<WorkflowTransitionTabProvider.WorkflowTransitionTab> tabPanels = Lists.newArrayList(getTabPanels());
        WorkflowTransitionTabProvider.WorkflowTransitionTab selectedTab = Iterables.find(tabPanels, new Predicate<WorkflowTransitionTabProvider.WorkflowTransitionTab>()
        {
            @Override
            public boolean apply(WorkflowTransitionTabProvider.WorkflowTransitionTab tab)
            {
                return tab.getModule().getKey().equals(descriptorTab);
            }
        }, null);

        if (selectedTab == null)
        {
            descriptorTab = !tabPanels.isEmpty() ? tabPanels.get(0).getModule().getKey() : "";
        }

        return descriptorTab;
    }

    public void setDescriptorTab(String descriptorTab)
    {
        if (TextUtils.stringSet(descriptorTab))
        {
            ActionContext.getSession().put(SessionKeys.WF_EDITOR_TRANSITION_TAB, descriptorTab);
        }

        this.descriptorTab = descriptorTab;

        eventPublisher.publish(new WorkflowTransitionTabEvent(descriptorTab));
    }

    public FieldScreen getFieldScreen()
    {
        return workflowActionsBean.getFieldScreenForView(getTransition());
    }

    public String getCount()
    {
        return count;
    }

    public void setCount(String count)
    {
        this.count = count;
    }

    public String getCurrentCount()
    {
        return currentCount;
    }

    public void setCurrentCount(String currentCount)
    {
        this.currentCount = currentCount;
    }

    public Iterator<WorkflowTransitionTabProvider.WorkflowTransitionTab> getTabPanels()
    {
        return workflowTransitionTabProvider.getTabs(getTransition(), getWorkflow()).iterator();
    }

    public String getTabPanelContent()
    {
        return workflowTransitionTabProvider.getTabContentHtml(getDescriptorTab(), getTransition(), getWorkflow());
    }
}