/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.issue.util.ScreenTabErrorHelper;
import com.atlassian.jira.workflow.WorkflowManager;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class EditIssue extends AbstractCommentableAssignableIssue implements OperationContext
{
    private final ConstantsManager constantsManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final WorkflowManager workflowManager;

    private FieldScreenRenderer fieldScreenRenderer;
    private FieldScreenRendererFactory fieldScreenRendererFactory;
    private final IssueService issueService;
    private SortedSet tabsWithErrors;
    private int selectedTab;
    private IssueService.UpdateValidationResult updateValidationResult;

    private Collection ignoreFieldIds = new LinkedList();

    public EditIssue(SubTaskManager subTaskManager, ConstantsManager constantsManager,
            FieldLayoutManager fieldLayoutManager, WorkflowManager workflowManager,
            FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService, final IssueService issueService, final UserUtil userUtil)
    {
        super(subTaskManager, fieldScreenRendererFactory, commentService, userUtil);
        this.constantsManager = constantsManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.workflowManager = workflowManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.issueService = issueService;
    }

    public String doDefault() throws Exception
    {
        try
        {
            if (!isEditable())
            {
                return ERROR;
            }
        }
        catch (IssuePermissionException ipe)
        {
            return ISSUE_PERMISSION_ERROR;
        }
        catch (IssueNotFoundException e)
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

        return super.doDefault();
    }

    protected FieldScreenRenderer getFieldScreenRenderer()
    {
        if (fieldScreenRenderer == null)
        {
            fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(getIssueObject(), IssueOperations.EDIT_ISSUE_OPERATION);
        }

        return fieldScreenRenderer;
    }

    protected void doValidation()
    {
        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters(ActionContext.getParameters());
        issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(false);
        updateValidationResult = issueService.validateUpdate(getLoggedInUser(), getIssueObject().getId(), issueInputParameters);
        setIssueObject(updateValidationResult.getIssue());
        setFieldValuesHolder(updateValidationResult.getFieldValuesHolder());
        if (!updateValidationResult.isValid())
        {
            addErrorCollection(updateValidationResult.getErrorCollection());
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            final IssueService.IssueResult issueResult = issueService.update(getLoggedInUser(), updateValidationResult);
            if (!issueResult.isValid())
            {
                addErrorCollection(issueResult.getErrorCollection());
            }

            if (isInlineDialogMode())
            {
                return returnComplete();
            }

            return getRedirect(getViewUrl());
        }
        catch (Exception issueEditException)
        {
            addErrorMessage(getText("admin.errors.issues.exception.occured", issueEditException));
            log.error("An exception occurred editing issue: " + getIssueObject().getKey(), issueEditException);
            return ERROR;
        }
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

    public IssueOperation getIssueOperation()
    {
        return IssueOperations.EDIT_ISSUE_OPERATION;
    }

    public ConstantsManager getConstantsManager()
    {
        return constantsManager;
    }

    public FieldLayoutManager getFieldLayoutManager()
    {
        return fieldLayoutManager;
    }

    public WorkflowManager getWorkflowManager()
    {
        return workflowManager;
    }

    public Collection getIgnoreFieldIds()
    {
        return ignoreFieldIds;
    }

    @Override
    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>(super.getDisplayParams());
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
