/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.issue.enterprise;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.issue.MoveIssue;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MoveIssueUpdateWorkflow extends MoveIssue
{
    private Map subTaskTargetStatusHolder = new HashMap();
    private MutableIssue targetIssue;

    public MoveIssueUpdateWorkflow(SubTaskManager subTaskManager, ConstantsManager constantsManager,
                                   WorkflowManager workflowManager, FieldManager fieldManager,
                                   FieldLayoutManager fieldLayoutManager, IssueFactory issueFactory,
                                   FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService,
                                   UserUtil userUtil)
    {
        super(subTaskManager, constantsManager, workflowManager, fieldManager, fieldLayoutManager,
                issueFactory, fieldScreenRendererFactory, commentService, userUtil);
    }

    public String doDefault()
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        if (!hasIssuePermission(Permissions.MOVE_ISSUE, getIssueObject()))
        {
            // Add error message and do not continue
            addErrorMessage(getText("move.issue.nopermissions"));
            return "securitybreach";
        }

        getMoveIssueBean().addAvailablePreviousStep(1);
        // If current statuses for issue and all subtasks exist in the target workflow - progress to step three of move process
        if (isStatusChangeRequired())
        {
            getMoveIssueBean().setCurrentStep(2);
            return INPUT;
        }

        // Status change not required
        getMoveIssueBean().setTargetStatusId(getCurrentStatusGV().getString("id"));
        return forceRedirect("MoveIssueUpdateFields!default.jspa?id=" + getIssue().getString("id") + "&assignee=" + URLEncoder.encode("" + getAssignee()));
    }

    public void doValidation()
    {
        if (getMoveIssueBean() != null)
        {
            if (!hasIssuePermission(Permissions.MOVE_ISSUE, getIssueObject()))
            {
                // Add error message and do not continue
                addErrorMessage(getText("move.issue.nopermissions"));
                return;
            }

            if (!TextUtils.stringSet(getTargetStatusId()))
            {
                addErrorMessage(getText("admin.errors.issues.select.target.status"));
            }
        }
    }

    protected String doExecute() throws WorkflowException
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        if (!isSubTask() && isHasSubTasks())
        {
            populatesubTaskTargetStatusHolder();
            getMoveIssueBean().setTaskTargetStatusHolder(subTaskTargetStatusHolder);
        }

        return forceRedirect("MoveIssueUpdateFields!default.jspa?id=" + getIssue().getString("id") + "&assignee=" + URLEncoder.encode("" + getAssignee()));
    }

    //Return the workflow used by the subtask type in the target project
    public JiraWorkflow getSubTaskTargetWorkflow(String subTaskTypeID) throws WorkflowException
    {
        return getWorkflowManager().getWorkflow(getTargetProject().getLong("id"), subTaskTypeID);
    }

    // Retrieve a collection of types that are associated with an invalid status in the target workflow
    public Collection getTaskInvalidTypes() throws WorkflowException
    {
        Collection<GenericValue> subTasks = getSubTasks();
        Collection invalidTypes = new HashSet();

        for (final GenericValue subTask : subTasks)
        {
            String taskStatusId = subTask.getString("status");
            String taskTypeId = subTask.getString("type");
            GenericValue taskTypeGV = getConstantsManager().getIssueType(taskTypeId);
            GenericValue taskStatusGV = getConstantsManager().getStatus(taskStatusId);

            if (getTaskInvalidStatuses(taskTypeId).contains(taskStatusGV))
            {
                invalidTypes.add(taskTypeGV);
            }
        }
        return invalidTypes;
    }

    public Collection getTargetWorkflows() throws WorkflowException
    {
        Collection<GenericValue> types = getSubTaskTypesUsed();
        Collection<JiraWorkflow> workflows = new ArrayList<JiraWorkflow>();

        for (final GenericValue type : types)
        {
            workflows.add(getWorkflowForType(getTargetPid(), type.getString("id")));
        }

        return workflows;
    }

    public Collection getTaskCurrentWorkflows() throws GenericEntityException, WorkflowException
    {
        Collection<GenericValue> types = getSubTaskTypesUsed();
        Collection<JiraWorkflow> workflows = new ArrayList<JiraWorkflow>();

        for (final GenericValue type : types)
        {
            workflows.add(getWorkflowForType(getProject().getLong("id"), type.getString("id")));
        }

        return workflows;
    }

    // Retrieve the task target workflow assocaited with the specified type
    public JiraWorkflow getTaskTargetWorkflow(GenericValue taskType) throws WorkflowException
    {
        return getWorkflowForType(getTargetPid(), taskType.getString("id"));
    }

    /**
     * Get the "target workflow" for a subtask issue type.
     * Currently this is used solely to display the correct target workflow name in moveissue-updateworkflow.jsp
     * @param subtaskIssueType the subtask issue type id
     * @return the workflow associated with the target issue type
     */
    public JiraWorkflow getSubtaskTargetWorkflow(final String subtaskIssueType)
    {
        final String targetIssueType = getSubtaskTargetIssueType(subtaskIssueType);
        return getWorkflowForType(getTargetPid(), targetIssueType);
    }

    // Retrieve the task current workflow assocaited with the specified type
    public JiraWorkflow getTaskCurrentWorkflow(GenericValue taskType) throws GenericEntityException, WorkflowException
    {
        return getWorkflowForType(getProject().getLong("id"), taskType.getString("id"));
    }

    // Retrieve the tasks that are associated with the specified status and workflow
    public Collection getTasksByStatusWorkflowType(String statusId, JiraWorkflow workflow, String typeId) throws GenericEntityException, WorkflowException
    {
        Collection<GenericValue> subTasks = getSubTasks();
        Collection<GenericValue> tasks = new ArrayList<GenericValue>();

        for (final GenericValue subTask : subTasks)
        {
            String taskTypeId = subTask.getString("type");
            JiraWorkflow taskWorkflow = getWorkflowForType(getProject().getLong("id"), taskTypeId);
            String taskStatusId = subTask.getString("status");

            if (taskStatusId.equals(statusId) && taskWorkflow.equals(workflow) && taskTypeId.equals(typeId))
            {
                tasks.add(subTask);
            }
        }
        return tasks;
    }

    // Retrieve the sub task target statuses from the params
    private void populatesubTaskTargetStatusHolder() throws WorkflowException
    {
        Map actionParameters = ActionContext.getParameters();

        Collection<GenericValue> subTaskTypesUsed = getSubTaskTypesUsed();

        for (final GenericValue subTaskTypeGV : subTaskTypesUsed)
        {
            if (subTaskTypeGV != null)
            {
                String taskTypeId = subTaskTypeGV.getString("id");

                // the key into the map is based on the target issue type. if they are
                // migrating to a new issue type then we need to lookup what the target type is.
                final String key = getPrefixIssueTypeId(subTaskTypeGV.getString("id"));
                String newIssueType = (String) getMoveIssueBean().getFieldValuesHolder().get(key);
                // but if they aren't migrating then we use the current issue type
                if (newIssueType == null)
                {
                    newIssueType = taskTypeId;
                }

                Collection<GenericValue> statuses = getTaskInvalidStatuses(taskTypeId);
                String subTaskTypeKey = null;
                String targetStatusId = null;

                for (final GenericValue status : statuses)
                {
                    String statusId = status.getString("id");

                    // we store subtask target statues in the map with a key based on the NEW issue type.
                    // during MoveIssueConfirm some code paths don't have access to the old issue type.
                    subTaskTypeKey = getPrefixTaskStatusId(newIssueType, statusId);

                    final String lookupSubTaskTypeKey = getPrefixTaskStatusId(subTaskTypeGV.getString("id"), statusId);
                    Object o = actionParameters.get(lookupSubTaskTypeKey);

                    if (o instanceof String[])
                    {
                        String[] strings = (String[]) o;
                        targetStatusId = strings[0];
                    }
                    else
                    {
                        return;
                    }
                }
                subTaskTargetStatusHolder.put(subTaskTypeKey, targetStatusId);
            }
        }
    }

    protected Issue getTargetIssueObject()
    {
        if (targetIssue == null)
        {
            targetIssue = getIssueManager().getIssueObject(getId());
            targetIssue.setProjectObject(getTargetProjectObj());
            targetIssue.setIssueTypeObject(getTargetIssueTypeObject());
        }

        return targetIssue;
    }

    public String getFieldHtml(FieldLayoutItem fieldLayoutItem)
    {
        OrderableField orderableField = fieldLayoutItem.getOrderableField();
        if (orderableField.isShown(getTargetIssueObject()))
        {
            return orderableField.getCreateHtml(fieldLayoutItem, getMoveIssueBean(), this, getTargetIssueObject());
        }
        else
        {
            return "";
        }
    }

}
