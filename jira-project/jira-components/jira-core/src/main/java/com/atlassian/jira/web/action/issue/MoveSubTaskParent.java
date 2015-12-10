/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.JiraException;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.movesubtask.MoveSubTaskOperationManager;
import com.atlassian.jira.movesubtask.operation.MoveSubTaskOperation;
import com.atlassian.jira.movesubtask.operation.MoveSubTaskParentOperation;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class MoveSubTaskParent extends MoveIssue
{
    private MoveSubTaskOperation moveSubTaskOperation;
    private final IssueUpdater issueUpdater;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private String parentIssue;
    private static final String FIELD_PARENT_ISSUE = "parentIssue";

    public MoveSubTaskParent(MoveSubTaskOperationManager moveSubTaskOperationManager,
                             SubTaskManager subTaskManager, ConstantsManager constantsManager,
                             WorkflowManager workflowManager, FieldManager fieldManager,
                             FieldLayoutManager fieldLayoutManager, IssueFactory issueFactory,
                             FieldScreenRendererFactory fieldScreenRendererFactory, IssueUpdater issueUpdater,
                             CommentService commentService, IssueTypeSchemeManager issueTypeSchemeManager, UserUtil userUtil)
    {
        super(subTaskManager, constantsManager, workflowManager, fieldManager, fieldLayoutManager,
                issueFactory, fieldScreenRendererFactory, commentService, userUtil);
        this.issueUpdater = issueUpdater;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        moveSubTaskOperation = moveSubTaskOperationManager.getOperation(MoveSubTaskParentOperation.NAME_KEY);
    }

    public MoveSubTaskOperation getMoveSubTaskOperation()
    {
        return moveSubTaskOperation;
    }

    public Collection getSubTaskTypes()
    {
        return issueTypeSchemeManager.getSubTaskIssueTypesForProject(getIssueObject().getProjectObject());
    }

    public String doDefault()
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        if (getMoveIssueBean() != null)
        {
            getMoveIssueBean().setCurrentStep(3);
            getMoveIssueBean().addAvailablePreviousStep(0);
        }
        return INPUT;
    }

    protected void doValidation()
    {
        if (getMoveIssueBean() != null)
        {
            Issue currentIssue;
            try
            {
                currentIssue = getIssueObject();
            }
            catch (IssueNotFoundException e)
            {
                // we are relying on getIssue() to add the errormessage
                return;
            }
            catch (IssuePermissionException e)
            {
                // we are relying on getIssue() to add the errormessage
                return;
            }
            catch (Exception e)
            {
                // Don't think this has been added as a message
                addErrorMessage(getText("error.unexpected.condition", e.getMessage()));
                return;
            }

            if (!hasIssuePermission(Permissions.MOVE_ISSUE, currentIssue))
            {
                addErrorMessage(getText("move.issue.nopermissions"));
                return;
            }

            if (!TextUtils.stringSet(getParentIssue()))
            {
                addError(FIELD_PARENT_ISSUE, getText("move.subtask.parent.issue.required"));
                return;
            }

            GenericValue oldParentIssue = getSubTaskManager().getParentIssue(currentIssue.getGenericValue());
            GenericValue newParentIssue = getNewParentIssue();

            if (newParentIssue == null)
            {
                addError(FIELD_PARENT_ISSUE, getText("move.subtask.parent.error.cannotlink.issue.notexist", getParentIssue()));
            }
            else
            {
                if (oldParentIssue.equals(newParentIssue))
                {
                    addError(FIELD_PARENT_ISSUE, getText("move.subtask.parent.error.cannotlink.current.parent"));
                }
                else if (!oldParentIssue.get("project").equals(newParentIssue.get("project")))
                {
                    addError(FIELD_PARENT_ISSUE, getText("move.subtask.parent.error.cannotlink.different.project"));
                }

                if (getSubTaskManager().isSubTask(newParentIssue))
                {
                    if (newParentIssue.equals(currentIssue.getGenericValue()))
                    {
                        addError(FIELD_PARENT_ISSUE, getText("move.subtask.parent.error.cannotlink.itself"));
                    }
                    else
                    {
                        addError(FIELD_PARENT_ISSUE, getText("move.subtask.parent.error.cannotlink.subtask"));
                    }
                }

                if(!getIssueObject(newParentIssue).isEditable())
                {
                    addError(FIELD_PARENT_ISSUE, getText("move.subtask.parent.error.parentnoteditable", getParentIssue()));

                }


            }
        }
    }

    /**
     * Important do not change the workflow as there is validation for the parent issue and you don't want to delete
     * the link and find out that there is an error in the parent issue...
     */
    @RequiresXsrfCheck
    protected String doExecute() throws RemoveException, CreateException
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        GenericValue subTask = getIssue();
        GenericValue parentIssue = getNewParentIssue();
        IssueUpdateBean iub = getSubTaskManager().changeParent(subTask, parentIssue, getLoggedInUser());

        issueUpdater.doUpdate(iub, true);
        // Return to subTask
        return getRedirect("/browse/" + StringUtils.trim(getIssue().getString("key")));
    }

    /**
     * Validate and return the parent Issue
     *
     * @throws com.atlassian.jira.exception.IssueNotFoundException
     *
     */
    private GenericValue getNewParentIssue()
    {
        String newParentIssueId;
        GenericValue parentIssue;

        //todo - shortened the code with a getter (this should be there since its checked in validation)
        newParentIssueId = getParentIssue();

        //todo - changed it to a getter for the issueManager instead of chaning the field to a protected
        try
        {
            parentIssue = getIssueManager().getIssue(newParentIssueId);
        }
        catch (GenericEntityException e)
        {
            return null;
        }

        return parentIssue;
    }

    public MutableIssue getIssueObject(GenericValue issue)
    {
        MutableIssue issueObject = super.getIssueObject(issue);
        issueObject.setParentId(getSubTaskManager().getParentIssueId(getIssue()));
        return issueObject;
    }

    /**
     * Needed to specify which project issues to display in the issue picker.
     * @return The current project's id.
     */
    public String getCurrentPid()
    {
        return getProject().getString("id");
    }

    public String getParentIssue()
    {
        return parentIssue;
    }

    public void setParentIssue(String parentIssue)
    {
        this.parentIssue = parentIssue;
    }

    public String getParentKey()
    {
        GenericValue gv = getSubTaskManager().getParentIssue(getIssue());
        if (gv != null)
        {
            return (String) gv.get("key");
        }
        return null;
    }
}
