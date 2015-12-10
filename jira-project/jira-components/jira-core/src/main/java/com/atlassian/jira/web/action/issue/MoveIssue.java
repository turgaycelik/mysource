package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.MoveIssueBean;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class MoveIssue extends AbstractCommentableAssignableIssue
{
    public static final String SUBTASK_STATUS_PREFIX = "subtaskstatus_";
    private static final String SUBTASK_ISSUETYPE_PREFIX = "subtaskissuetype_";

    protected final ConstantsManager constantsManager;
    protected final WorkflowManager workflowManager;
    protected final FieldManager fieldManager;
    protected final FieldLayoutManager fieldLayoutManager;
    protected final IssueFactory issueFactory;

    public MoveIssue(SubTaskManager subTaskManager, ConstantsManager constantsManager, WorkflowManager workflowManager,
                     FieldManager fieldManager, FieldLayoutManager fieldLayoutManager, IssueFactory issueFactory,
                     FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService, UserUtil userUtil)
    {
        super(subTaskManager, fieldScreenRendererFactory, commentService, userUtil);
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
        this.fieldManager = fieldManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.issueFactory = issueFactory;
    }

    /**
     * Handles the request to submit the input from the first step of the wizard, the submitted information is the target
     * project and the target issue type.
     * <br/><br/>
     * If the issue has sub-tasks it redirects to {@link MoveIssueSubtasks#doDefault()}
     * so that the tasks issue types can be mapped; otherwise, it redirects to
     * {@link com.atlassian.jira.web.action.issue.enterprise.MoveIssueUpdateWorkflow#doDefault()}
     *
     * @return It actually doesn't return anything. If the issue has sub-tasks it redirects to {@link MoveIssueSubtasks#doDefault()}
     * so that the tasks issue types can be mapped; otherwise, it redirects to
     * {@link com.atlassian.jira.web.action.issue.enterprise.MoveIssueUpdateWorkflow#doDefault()}
     */
    protected String doExecute() throws Exception
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        if (isHasSubTasks())
        {
            getMoveIssueBean().addAvailablePreviousStep(1);
            return forceRedirect("MoveIssueSubtasks!default.jspa?id=" + id);
        }
        else
        {
            return forceRedirect("MoveIssueUpdateWorkflow!default.jspa?id=" + id + "&assignee=" + URLEncoder.encode("" + getAssignee(), "UTF8"));
        }
    }

    /**
     * Handles the initial request to move an issue. It returns the view responsible for rendering the first step of the
     * wizard, if the user is authorised to perform the move and if the issue exists.
     *
     * @return "securitybreach" if the user is not authorised to move the issue; {@link webwork.action.Action#ERROR} if
     * the issue can't be found (which means it was probably deleted), or if the user isn't authorised to browse the
     * issue; otherwise, {@link webwork.action.Action#INPUT}} is returned to render the view for the first step of the
     * wizard.
     */
    public String doDefault() throws Exception
    {
        try
        {
            if (!hasIssuePermission(Permissions.MOVE_ISSUE, getIssueObject()))
            {
                return "securitybreach";
            }
        }
        catch (IssueNotFoundException e)
        {
            // Error is added above [the getIssue() method adds the appropriate messages to the error collection]
            return ERROR;
        }
        catch (IssuePermissionException e)
        {
            return ERROR;
        }

        // "Delegate moving issues with sub-tasks to the Bulk Migration Wizard to ensure that we have the correct mapping
        // logic for the fields in the parent issue and its sub-tasks. See JRA-17312
        if (isHasSubTasks())
        {
            return forceRedirect("views/bulkedit/BulkMigrateDetails.jspa?singleIssueId=" + getIssueObject().getId());
        }

        // Initialise MoveIssueBean
        // Reset bean if stepping back in move issue "wizard"
        if (ActionContext.getSingleValueParameters().containsKey("reset") && ("true".equals(ActionContext.getSingleValueParameters().get("reset"))) && getMoveIssueBean() != null)
        {
            Long selectedPid = getMoveIssueBean().getTargetPid();
            String selectedIssueTypeId = getMoveIssueBean().getTargetIssueType();

            getMoveIssueBean().clearAvailablePreviousSteps();
            getMoveIssueBean().reset();
            getMoveIssueBean().setIssueId(getIssue().getLong("id"));
            getMoveIssueBean().setSourceIssueKey(getIssueObject().getKey());

            // Initialise the values to the previously selected ones.
            getMoveIssueBean().getFieldValuesHolder().put(IssueFieldConstants.PROJECT, selectedPid);
            getMoveIssueBean().getFieldValuesHolder().put(IssueFieldConstants.ISSUE_TYPE, selectedIssueTypeId);
        }
        else
        {
            MoveIssueBean moveIssueBean = new MoveIssueBean(constantsManager, projectManager);
            moveIssueBean.setIssueId(getIssue().getLong("id"));
            moveIssueBean.setSourceIssueKey(getIssueObject().getKey());
            ActionContext.getSession().put(SessionKeys.MOVEISSUEBEAN, moveIssueBean);

            // Initialise the values of project and issue type to the issue's values
            Issue issueObject = getIssueObject(getIssue());
            // Cannot use getOrderableField for project as it is not part of the orderable field collection (to ensure it cannot be placed on Field Screens)
            fieldManager.getProjectField().populateFromIssue(getMoveIssueBean().getFieldValuesHolder(), issueObject);
            fieldManager.getIssueTypeField().populateFromIssue(getMoveIssueBean().getFieldValuesHolder(), issueObject);
        }

        getMoveIssueBean().setCurrentStep(1);

        return INPUT;
    }

    protected void doValidation()
    {
        if (getMoveIssueBean() != null)
        {
            try
            {
                if (!hasIssuePermission(Permissions.MOVE_ISSUE, getIssueObject()))
                {
                    // Add error message and do not continue
                    addErrorMessage(getText("move.issue.nopermissions"));
                    return;
                }

                fieldManager.getProjectField().populateFromParams(getMoveIssueBean().getFieldValuesHolder(), ActionContext.getParameters());
                fieldManager.getIssueTypeField().populateFromParams(getMoveIssueBean().getFieldValuesHolder(), ActionContext.getParameters());

                // Validate as if we are creating a new issue
                Issue issueObject = getIssueObject(null);
                fieldManager.getProjectField().validateParams(getMoveIssueBean(), this, this, issueObject, null);
                fieldManager.getIssueTypeField().validateParams(getMoveIssueBean(), this, this, issueObject, null);

                if (getIssue().getLong("project").equals(getMoveIssueBean().getTargetPid()) &&
                        getIssue().getString("type").equals(getMoveIssueBean().getTargetIssueType()))
                {
                    addErrorMessage(getText("move.issue.nochange"));
                }

                if (!invalidInput())
                {
                    //Validate permissions
                    validateAttachmentMove();
                    validateCreateIssue();
                }
            }
            catch (Exception e)
            {
                log.error("Exception: " + e, e);
                addErrorMessage("An exception occurred: " + e + ".");
            }
        }
    }

    protected String redirectToSessionTimeoutPage()
    {
        ActionContext.getSession().put(SessionKeys.SESSION_TIMEOUT_MESSAGE, getText("moveissue.session.timeout.message"));
        return getRedirect("SessionTimeoutMessage.jspa");
    }

    protected void validateAttachmentMove()
    {
        Collection attachments = getIssueObject().getAttachments();

        if (!hasProjectPermission(Permissions.CREATE_ATTACHMENT, getTargetProjectObj()) && !attachments.isEmpty())
        {
            addErrorMessage(getText("moveissue.create.attachment.permission"));
        }
    }

    protected void validateCreateIssue() throws GenericEntityException
    {
        if (!hasProjectPermission(Permissions.CREATE_ISSUE, getTargetProjectObj()))
        {
            addErrorMessage(getText("moveissue.no.create.permission"));
        }
    }

    public Long getTargetPid()
    {
        return getMoveIssueBean().getTargetPid();
    }

    public String getTargetStatusId()
    {
        return getMoveIssueBean().getTargetStatusId();
    }

    public GenericValue getTargetProject()
    {
        return getProjectManager().getProject(getTargetPid());
    }

    public Project getTargetProjectObj()
    {
        return getProjectManager().getProjectObj(getTargetPid());
    }

    public GenericValue getTargetIssueTypeGV()
    {
        return constantsManager.getIssueType(getTargetIssueType());
    }

    public IssueType getTargetIssueTypeObject()
    {
        return constantsManager.getIssueTypeObject(getTargetIssueType());
    }

    public JiraWorkflow getTargetWorkflow() throws WorkflowException
    {
        return getWorkflowForType(getTargetPid(), getTargetIssueType());
    }

    public JiraWorkflow getCurrentWorkflow() throws WorkflowException, GenericEntityException
    {
        return getWorkflowForType(getProject().getLong("id"), getIssue().getString("type"));
    }

    public JiraWorkflow getWorkflowForType(Long projectId, String issueTypeId) throws WorkflowException
    {
        return workflowManager.getWorkflow(projectId, issueTypeId);
    }

    public GenericValue getCurrentStatusGV()
    {
        return constantsManager.getStatus(getIssue().getString("status"));
    }

    public Status getCurrentStatusObject()
    {
        return constantsManager.getStatusObject(getIssue().getString("status"));
    }

    public String getTargetIssueType()
    {
        return getMoveIssueBean().getTargetIssueType();
    }

    public String getCurrentIssueType()
    {
        return getIssue().getString("type");
    }

    public Collection getAllowedProjects()
    {
        // Move issue allows an issue to be moved between project and issue type.
        // Hence, it is possible to move an issue to a different type but remain in the same project - a kind of "uber edit"
        return ComponentAccessor.getPermissionManager().getProjects(Permissions.CREATE_ISSUE, getLoggedInUser());
    }

    public boolean isSubTask()
    {
        return getSubTaskManager().isSubTask(getIssue());
    }

    /**
     * Retrieve a collection of target workflow statuses from the workflow associated with the specified issue type id.
     * @param issueTypeId the id of the specified issue type.
     * @return A collection of target workflow statuses from the workflow associated with the specified issue type id.
     */
    public Collection getTargetWorkflowStatuses(String issueTypeId)
    {
        JiraWorkflow workflow = getWorkflowForType(getTargetPid(), issueTypeId);
        return workflow.getLinkedStatuses();
    }

    /**
     * Check if the workflow is the same for the current and target issue types
     * @param currentIssueTypeId the id of the issue's current issue type.
     * @param targetIssueTypeId the id of the target issue type.
     * @return true if the workflows associated to the current and target issue types are the same; otherwise, false.
     */
    public boolean isWorkflowMatch(String currentIssueTypeId, String targetIssueTypeId)
    {
        return getWorkflowForType(getProject().getLong("id"), currentIssueTypeId).equals(getWorkflowForType(getTargetPid(), targetIssueTypeId));
    }

    /**
     * Checks if the current issue has sub-tasks.
     * @return true if sub-tasks have been enabled and the current issue has sub-tasks; otherwise, false.
     */
    public boolean isHasSubTasks()
    {
        return getSubTaskManager().isSubTasksEnabled() && !getSubTaskManager().getSubTasks(getIssue()).isEmpty();
    }

    /**
     * Retrieves the collection of sub-tasks associated with the current issue.
     * @return The collection of sub-tasks associated with the current issue.
     */
    public Collection<GenericValue> getSubTasks()
    {
        return getSubTaskManager().getSubTasks(getIssue());
    }

    /**
     * Gets the collection of sub-task types used in the sub-tasks associated with the current issue.
     * @return The collection of sub-task types used in the sub-tasks associated with the current issue.
     */
    public Collection<GenericValue> getSubTaskTypesUsed()
    {
        Collection<GenericValue> subTasks = getSubTasks();
        Collection<GenericValue> usedSubTaskTypes = new HashSet<GenericValue>();

        for (final GenericValue subTask : subTasks)
        {
            usedSubTaskTypes.add(constantsManager.getIssueType(subTask.getString("type")));
        }
        return usedSubTaskTypes;
    }

    // Return subtask status id with prefix - this is used to retrieve the subtask status id from the action params
    // This maps from (NEW issue type, ORIGINAL status id) -> new status id.
    // You may need to use the getPrefixIssueTypeId() part of the map to get from the old issue type to
    // the new issue type.
    public String getPrefixTaskStatusId(String taskTypeId, String taskStatusId)
    {
        return SUBTASK_STATUS_PREFIX + taskTypeId + "_" + taskStatusId;
    }

    // Construct a key into the fieldValuesHolder map in the move issue bean.
    // This maps from original issue type -> destination issue type
    public String getPrefixIssueTypeId(final String issueType)
    {
        return SUBTASK_ISSUETYPE_PREFIX + issueType;
    }

    /**
     * Retrieve the task status associated with this type and current status.
     * @param taskTypeId the id of the task type.
     * @param taskStatusId the id of the task status.
     * @return The task status associated with this type and current status.
     */
    public String getSubTaskTargetStatus(String taskTypeId, String taskStatusId)
    {
        Map actionParameters = ActionContext.getParameters();

        String subTaskTypeKey = SUBTASK_STATUS_PREFIX + taskTypeId + "_" + taskStatusId;
        Object o = actionParameters.get(subTaskTypeKey);

        if (o instanceof String[])
        {
            String[] strings = (String[]) o;
            // There should only be one sub task target status
            return strings[0];
        }
        else
        {
            return null;
        }
    }

    /**
     * Checks if the current statuses of the issue and its sub-tasks do not exist in the target workflow.
     *
     * This is used to determine if step 2 requires input from the user, i.e. If all current statuses exist in the
     * target workflows - no input is needed.
     * @return true if the current statuses of the issue and its sub-tasks do not exist in the target workflow;
     * otherwise, false.
     */
    public boolean isStatusChangeRequired()
    {
        try
        {
            if (isIssueStatusValid())
            {
                // DODGY code, if it is a sub-task it can't have more sub-tasks, therefore the second bit of the if
                // is always going to be true and you'll end up doing a bunch of work for nothing in
                // isTaskStatusChangeRequired because it will always return false for sub-tasks. We should get rid of
                // the second bit of the if to be more efficient and clear.
                if (!isSubTask() || getSubTaskManager().getSubTasks(getIssue()).isEmpty())
                {
                    return isTaskStatusChangeRequired();
                }
                return false;
            }
        }
        catch (WorkflowException e)
        {
            log.error(e, e);
        }
        return true;
    }

    public boolean isTaskStatusChangeRequired() throws WorkflowException
    {
        Collection<GenericValue> types = getSubTaskTypesUsed();
        for (final GenericValue type : types)
        {
            if (!getTaskInvalidStatuses(type.getString("id")).isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    public boolean isIssueStatusValid() throws WorkflowException
    {
        String status = getIssue().getString("status");
        GenericValue statusGV = constantsManager.getStatus(status);

        Collection availableStatuses = getTargetWorkflow().getLinkedStatuses();
        return availableStatuses.contains(statusGV);
    }

    public boolean isTaskStatusValid(String typeId, String statusId) throws WorkflowException
    {
        JiraWorkflow targetWorkflow = getWorkflowForType(getTargetPid(), typeId);
        GenericValue statusGV = constantsManager.getStatus(statusId);

        Collection availableStatuses = targetWorkflow.getLinkedStatuses();
        return availableStatuses.contains(statusGV);
    }

    // Return a collection of task status GVs associated with the specified type that are invalid in the target workflow
    public Collection getTaskInvalidStatuses(String typeId) throws WorkflowException
    {
        Collection<GenericValue> subTasks = getSubTasks();
        Collection<GenericValue> invalidStatuses = new HashSet<GenericValue>();

        for (final GenericValue subTask : subTasks)
        {
            String taskType = subTask.getString("type");
            String taskStatus = subTask.getString("status");

            if (typeId.equals(taskType))
            {
                GenericValue subStatusGV = constantsManager.getStatus(taskStatus);
                if (!invalidStatuses.contains(subStatusGV))
                {
                    Collection availableStatuses = getTargetWorkflowStatuses(taskType);

                    if (!availableStatuses.contains(subStatusGV))
                    {
                        invalidStatuses.add(subStatusGV);
                    }
                }
            }
        }
        return invalidStatuses;
    }

    public Collection getTaskInvalidStatusObjects(String typeId) throws WorkflowException
    {
        final Collection<GenericValue> statuses = getTaskInvalidStatuses(typeId);
        final Collection<Status> statusObjects = new HashSet<Status>();

        for (final GenericValue status : statuses)
        {
            statusObjects.add(constantsManager.getStatusObject(status.getString("id")));
        }

        return statusObjects;
    }

    // Retrieve a collection of tasks with the specified status
    public Collection getTasksWithStatus(String invalidStatusId) throws WorkflowException
    {
        Collection<GenericValue> subTasks = getSubTaskManager().getSubTasks(getIssue());
        Collection<GenericValue> tasksWithStatus = new ArrayList<GenericValue>();

        for (final GenericValue subTaskGV : subTasks)
        {
            if (subTaskGV.getString("status").equals(invalidStatusId))
            {
                tasksWithStatus.add(subTaskGV);
            }
        }
        return tasksWithStatus;
    }

    // ---- Managers ----
    protected FieldLayoutManager getFieldLayoutManager()
    {
        return fieldLayoutManager;
    }

    public ConstantsManager getConstantsManager()
    {
        return constantsManager;
    }

    protected WorkflowManager getWorkflowManager()
    {
        return workflowManager;
    }

    // ---- MoveIssueBean methods ---
    public MoveIssueBean getMoveIssueBean()
    {
        return (MoveIssueBean) ActionContext.getSession().get(SessionKeys.MOVEISSUEBEAN);
    }

    public void setBeanTargetStatusId(String targetStatusId)
    {
        if (targetStatusId != null)
        {
            getMoveIssueBean().setTargetStatusId(targetStatusId);
        }
    }

    public String getBeanTargetStatusId()
    {
        return getMoveIssueBean().getTargetStatusId();
    }

    public String getFieldHtml(String fieldId) throws Exception
    {
        return ((OrderableField) fieldManager.getField(fieldId)).getCreateHtml(null, getMoveIssueBean(), this, getIssueObject(getIssue()), getViewHtmlParams());
    }

    protected Map getViewHtmlParams()
    {
        return EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.TRUE);
    }

    public MutableIssue getIssueObject(GenericValue issue)
    {
        return issueFactory.getIssue(issue);
    }

    protected FieldManager getFieldManager()
    {
        return fieldManager;
    }

    /**
     * Get the target issue type for a migrating issue type.
     * <p>
     * NOTE: This is currently used solely in moveissue-updateworkflow.jsp to construct a dropdown of statuses
     * for the subtask target issue type.
     * @param subtaskIssueType the current issue type id
     * @return the target issue type id
     */
    public String getSubtaskTargetIssueType(final String subtaskIssueType)
    {
        final String issueTypeKey = getPrefixIssueTypeId(subtaskIssueType);
        return (String) getMoveIssueBean().getFieldValuesHolder().get(issueTypeKey);
    }
}
