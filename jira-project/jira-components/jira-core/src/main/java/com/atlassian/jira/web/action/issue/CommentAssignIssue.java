/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.WorkflowIssueOperationImpl;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.web.action.issue.util.ScreenTabErrorHelper;
import com.atlassian.jira.web.action.workflow.WorkflowAwareAction;
import com.atlassian.jira.web.action.workflow.WorkflowUIDispatcher;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.opensymphony.workflow.loader.ActionDescriptor;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This beautifully named action is in fact the Issue Transition screen.  When a workflow has a screen
 * this action is invoked.
 *
 * We really should rename this one day to say...oooh I dont know....TransitionIssue!
 */
public class CommentAssignIssue extends AbstractCommentableAssignableIssue
        implements OperationContext, WorkflowAwareAction
{
    private final IssueWorkflowManager issueWorkflowManager;
    private WorkflowTransitionUtil workflowTransitionUtil;
    private final IssueService issueService;
    private final Map fieldValuesHolder;
    private IssueService.TransitionValidationResult transitionResult;

    private int action;
    private SortedSet tabsWithErrors;
    private int selectedTab;

    public CommentAssignIssue(final SubTaskManager subTaskManager, final FieldScreenRendererFactory fieldScreenRendererFactory,
            final CommentService commentService, final IssueService issueService,
            final UserUtil userUtil, final IssueWorkflowManager issueWorkflowManager)
    {
        super(subTaskManager, fieldScreenRendererFactory, commentService, userUtil);
        this.issueService = issueService;
        this.issueWorkflowManager = issueWorkflowManager;
        fieldValuesHolder = new HashMap();
    }

    protected WorkflowTransitionUtil getWorkflowTransitionUtil()
    {
        if (workflowTransitionUtil == null)
        {
            workflowTransitionUtil = JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
            workflowTransitionUtil.setIssue(getIssueObject());
            workflowTransitionUtil.setAction(getAction());
        }

        return workflowTransitionUtil;
    }

    public String doDefault() throws Exception
    {
        try
        {
            super.doDefault();
        }
        catch (IssueNotFoundException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ISSUE_PERMISSION_ERROR;
        }

        for (FieldScreenRenderTab fieldScreenRenderTab : getFieldScreenRenderer().getFieldScreenRenderTabs())
        {
            for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItems())
            {
                if (fieldScreenRenderLayoutItem.isShow(getIssueObject()))
                {
                    fieldScreenRenderLayoutItem.populateFromIssue(getFieldValuesHolder(), getIssueObject());
                }
            }
        }

        // validate the transition is still valid else go back to issue screen
        if (invalidAction())
        {
            return WorkflowUIDispatcher.INVALID_ACTION;
        }

        return INPUT;
    }

    protected boolean invalidAction()
    {
        return !issueWorkflowManager.isValidAction(getIssueObject(), action, getLoggedInApplicationUser());
    }

    protected FieldScreenRenderer getFieldScreenRenderer()
    {
        return getWorkflowTransitionUtil().getFieldScreenRenderer();
    }

    public List getFieldScreenRenderTabs()
    {
        return getFieldScreenRenderer().getFieldScreenRenderTabs();
    }

    public Collection getTabsWithErrors()
    {
        if (tabsWithErrors == null)
        {
            initTabsWithErrors();
        }

        return tabsWithErrors;
    }

    private void initTabsWithErrors()
    {
        tabsWithErrors = new TreeSet<FieldScreenRenderTab>();
        selectedTab = new ScreenTabErrorHelper().initialiseTabsWithErrors(tabsWithErrors, getErrors(), getFieldScreenRenderer(), ActionContext.getParameters());
    }

    public int getSelectedTab()
    {
        // Init tabs - as the first tab with error will be calculated then.
        if (tabsWithErrors == null)
        {
            initTabsWithErrors();
        }

        return selectedTab;
    }

    protected void doValidation()
    {
        try
        {
            //just checking that the issue exists and that the user has permission to see it.
            getIssue();
        }
        catch (IssuePermissionException ipe)
        {
            return;
        }
        catch (IssueNotFoundException infe)
        {
            return;
        }

        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters(ActionContext.getParameters());
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(false);
        transitionResult = issueService.validateTransition(getLoggedInUser(), getIssueObject().getId(), action, issueInputParameters);
        setFieldValuesHolder(transitionResult.getFieldValuesHolder());
        if (!transitionResult.isValid())
        {
            addErrorCollection(transitionResult.getErrorCollection());
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (invalidAction())
        {
            return WorkflowUIDispatcher.INVALID_ACTION;
        }

        final IssueService.IssueResult transitionResult = issueService.transition(getLoggedInUser(), this.transitionResult);
        if (!transitionResult.isValid())
        {
            addErrorCollection(transitionResult.getErrorCollection());
            return ERROR;
        }

        if (isInlineDialogMode())
        {
            return returnComplete();
        }
        
        return redirectToView();
    }

    public int getAction()
    {
        return action;
    }

    public void setAction(int action)
    {
        this.action = action;
    }

    public String getI18nTextViaMetaAttr(String key, Object defaultValue)
    {
        final Object i18nKey = getActionDescriptor().getMetaAttributes().get(key);
        String localizedString;
        if ((i18nKey != null) && (i18nKey instanceof String))
        {
            localizedString = getText((String) i18nKey);
            if (!i18nKey.equals(localizedString))
            {
                return localizedString;
            }
            else
            {
                log.warn("The i18n key"+ i18nKey +" in property '"+key +"' for this transition does not contain a valid i18n key.");
            }
        }
        return defaultValue == null ? "" : defaultValue.toString();
    }

    public ActionDescriptor getActionDescriptor()
    {
        return getWorkflowTransitionUtil().getActionDescriptor();
    }

    public Map getCustomFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    public IssueOperation getIssueOperation()
    {
        return new WorkflowIssueOperationImpl(getActionDescriptor());
    }

    public Collection getIgnoreFieldIds()
    {
        return Collections.EMPTY_LIST;
    }

    public String getWorkflowTransitionDisplayName()
    {
        return getWorkflowTransitionDisplayName(getActionDescriptor());
    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
