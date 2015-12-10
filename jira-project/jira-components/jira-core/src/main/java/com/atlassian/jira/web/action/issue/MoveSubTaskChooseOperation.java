/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.movesubtask.MoveSubTaskOperationManager;
import com.atlassian.jira.movesubtask.operation.MoveSubTaskOperation;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.util.TextUtils;

import java.util.Collection;

/**
 * This action is used to present the user with a list of allowed bulk operations
 * on the selected issues
 */
public class MoveSubTaskChooseOperation extends MoveIssue
{
    private Collection moveSubTaskOperations;
    private String operation;
    final MoveSubTaskOperationManager moveSubTaskOperationManager;

    public MoveSubTaskChooseOperation(SubTaskManager subTaskManager, ConstantsManager constantsManager,
                                      WorkflowManager workflowManager, FieldManager fieldManager,
                                      FieldLayoutManager fieldLayoutManager, IssueFactory issueFactory,
                                      FieldScreenRendererFactory fieldScreenRendererFactory,
                                      MoveSubTaskOperationManager moveSubTaskOperationManager,
                                      CommentService commentService, UserUtil userUtil)
    {
        super(subTaskManager, constantsManager, workflowManager, fieldManager, fieldLayoutManager,
                issueFactory, fieldScreenRendererFactory, commentService, userUtil);
        this.moveSubTaskOperationManager = moveSubTaskOperationManager;
        this.moveSubTaskOperations = moveSubTaskOperationManager.getMoveSubTaskOperations();
    }

    public String doDefault() throws Exception
    {
        String result = super.doDefault();
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }
        getMoveIssueBean().setCurrentStep(0);
        return result;
    }

    protected void doValidation()
    {
        if (getMoveIssueBean() != null)
        {
            // Do validation
            if (getIssue() == null)
            {
                addErrorMessage("move.subtask.no.issue.selected");
            }
            if (!TextUtils.stringSet(getOperation()))
            {
                addErrorMessage(getText("move.chooseoperation.error.choose.operation"));
            }
            else
            {
                // Check if the operation exists
                if (!moveSubTaskOperationManager.isValidOperation(getOperation()))
                {
                    addErrorMessage(getText("move.chosseoperation.error.invalid.operation"));
                }
            }
        }
    }

    protected String doExecute() throws Exception
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        String operationName = moveSubTaskOperationManager.getOperation(this.getOperation()).getOperationName();
        getMoveIssueBean().setCurrentStep(1);
        getMoveIssueBean().addAvailablePreviousStep(0);
        return forceRedirect(operationName + "!default.jspa?id=" + getIssue().get("id"));
    }

    public Collection getMoveSubTaskOperations()
    {
        return moveSubTaskOperations;
    }

    public boolean isCanPerform(MoveSubTaskOperation moveSubTaskOperation) throws Exception
    {
        return moveSubTaskOperation.canPerform(getMoveIssueBean(), getLoggedInUser());
    }

    /**
     * Get the i18n key for why this operation can not be performed
     * @param moveSubTaskOperation The operation to check
     * @return  the i18n key for the reason it can't be displayed
     */
    public String getCannotPerformMessageKey(MoveSubTaskOperation moveSubTaskOperation)
    {
        return moveSubTaskOperation.getCannotPerformMessageKey(getMoveIssueBean());
    }

    public boolean isHasAvailableOperations() throws Exception
    {
        for (final Object moveSubTaskOperation1 : moveSubTaskOperations)
        {
            MoveSubTaskOperation moveSubTaskOperation = (MoveSubTaskOperation) moveSubTaskOperation1;
            if (isCanPerform(moveSubTaskOperation))
            {
                return true;
            }
        }
        return false;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public void setCurrentStep(int step)
    {
        getMoveIssueBean().setCurrentStep(step);
    }
}
