/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueVerifier;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.security.IssueSecurityHelper;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.collect.ImmutableList;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import static com.atlassian.jira.issue.util.transformers.IssueChangeHolderTransformer.toIssueUpdateBean;

public class MoveIssueConfirm extends MoveIssueUpdateFields
{
    boolean confirm = false;
    private final AttachmentManager attachmentManager;
    private final IssueManager issueManager;
    private final IssueEventManager issueEventManager;
    private final IssueEventBundleFactory issueEventBundleFactory;

    public MoveIssueConfirm(SubTaskManager subTaskManager, AttachmentManager attachmentManager,
            ConstantsManager constantsManager, WorkflowManager workflowManager, FieldManager fieldManager,
            FieldLayoutManager fieldLayoutmanager, IssueFactory issueFactory,
            FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService,
            IssueSecurityHelper issueSecurityHelper, IssueManager issueManager,
            UserUtil userUtil, IssueEventManager issueEventManager, IssueEventBundleFactory issueEventBundleFactory)
    {
        super(subTaskManager, constantsManager, workflowManager, fieldManager, fieldLayoutmanager,
                issueFactory, fieldScreenRendererFactory, commentService, issueSecurityHelper, userUtil);
        this.attachmentManager = attachmentManager;
        this.issueManager = issueManager;
        this.issueEventManager = issueEventManager;
        this.issueEventBundleFactory = issueEventBundleFactory;
    }

    public String doDefault()
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        // Sub task move does not have status selection step
        if (isSubTask())
        {
            getMoveIssueBean().setCurrentStep(3);
            getMoveIssueBean().addAvailablePreviousStep(2);
        }
        else
        {
            getMoveIssueBean().setCurrentStep(4);
            getMoveIssueBean().addAvailablePreviousStep(3);
        }

        return INPUT;
    }

    public Collection getConfimationFieldLayoutItems()
    {
        return getMoveIssueBean().getMoveFieldLayoutItems();
    }

    public Collection getRemoveFields()
    {
        return getMoveIssueBean().getRemovedFields();
    }

    public String getOldViewHtml(OrderableField field)
    {
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(getIssueObject());
        FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(field);

        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("nolink", Boolean.TRUE)
                .add("readonly", Boolean.TRUE)
                .add("prefix", "old_").toMutableMap();

        return field.getViewHtml(fieldLayoutItem, this, getIssueObject(getIssue()), displayParams);
    }

    public String getNewViewHtml(OrderableField field)
    {
        MutableIssue updatedIssue = getMoveIssueBean().getUpdatedIssue();
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(updatedIssue.getProjectObject(), updatedIssue.getIssueTypeObject().getId());
        FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(field);

        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("nolink", Boolean.TRUE)
                .add("readonly", Boolean.TRUE)
                .add("prefix", "new_").toMutableMap();

        return field.getViewHtml(fieldLayoutItem, this, updatedIssue, displayParams);
    }

    protected void doValidation()
    {

        if (getMoveIssueBean() != null)
        {
            //JRA-11605: Do not call super.doValidation() here.  This will cause the Issue security level to
            //be set to null, which will possibly cause problems when detecting the field has been modified.
            // Validation is not necessary in this step as it has been carried out by the previous MoveIssueUpdateFields
            // action already.

            try
            {
                validateAttachmentMove();
                validateCreateIssue();
            }
            catch (GenericEntityException e)
            {
                log.error("Error occurred while moving issue.", e);
                addErrorMessage(getText("moveissue.error.attachment"));
            }

            if (!isConfirm())
            {
                addErrorMessage(getText("admin.errors.error.occured.moving.issue"));
            }
        }
    }

    protected void popluateDefault(OrderableField orderableField)
    {
        // Override the parent method to do nothing - the field values holder should be already populated
    }

    protected void populateFromParams(OrderableField orderableField)
    {
        // Override the parent method to do nothing - the field values holder should be already populated
    }

    protected MutableIssue getTargetIssueObject()
    {
        return getMoveIssueBean().getUpdatedIssue();
    }

    /**
     * Actually does the moving of the issue from one Project/Issue Type to another
     */
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        final MutableIssue originalIssueObject =
                getIssueManager().getIssueObject(getMoveIssueBean().getUpdatedIssue().getId());
        final String sourceIssueKey = getMoveIssueBean().getSourceIssueKey();

        // The Updated Issue object will contain the new project ID, but it kwill still contain the old Issue Key.
        // We check if anyone else has moved the Issue in the meantime by comparing the original issue Key to the current Issue Key
        if (!originalIssueObject.getKey().equals(sourceIssueKey))
        {
            addErrorMessage(getText("moveissue.error.already.moved", sourceIssueKey, originalIssueObject.getKey()));
            return ERROR;
        }

        String originalWfId = null;

        final MutableIssue updatedIssueObject = getMoveIssueBean().getUpdatedIssue();
        if (updatedIssueObject.getWorkflowId() != null)
        {
            originalWfId = updatedIssueObject.getWorkflowId().toString();
        }

        // Verify integrity of issue
        IssueVerifier issueVerifier = new IssueVerifier();
        Map workflowMigrationMapping = new HashMap();
        workflowMigrationMapping.put(updatedIssueObject.getStatusObject().getId(), getTargetStatusId());
        // Validate the current workflow state to ensure that the issue can be moven properly
        ErrorCollection errorCollection = issueVerifier.verifyIssue(originalIssueObject.getGenericValue(), workflowMigrationMapping, true);

        // Verify integrity of subtasks
        if (!updatedIssueObject.isSubTask() && !updatedIssueObject.getSubTaskObjects().isEmpty())
        {
            Collection<GenericValue> subTasks = updatedIssueObject.getSubTasks();
            for (final GenericValue subtask : subTasks)
            {
                workflowMigrationMapping.clear();

                // Task Target Status will remain the same if it exists in the target workflow
                if (isTaskStatusValid(subtask.getString("type"), subtask.getString("status")))
                {
                    workflowMigrationMapping.put(subtask.getString("status"), subtask.getString("status"));
                }
                else
                {
                    // Retrieve target status of subtask in new workflow from moveissuebean
                    final String key = getPrefixIssueTypeId(subtask.getString("type"));
                    String newIssueType = (String) getMoveIssueBean().getFieldValuesHolder().get(key);
                    // newIssueType will be null if they didn't migrate to a new issue type
                    if (newIssueType == null)
                    {
                        newIssueType = subtask.getString("type");
                    }
                    String subTaskTypeKey = getPrefixTaskStatusId(newIssueType, subtask.getString("status"));
                    Map taskTargetStatusMap = getMoveIssueBean().getTaskTargetStatusHolder();
                    workflowMigrationMapping.put(subtask.getString("status"), taskTargetStatusMap.get(subTaskTypeKey));
                }

                // Validate the current workflow state to ensure that the issue can be moven properly
                errorCollection.addErrorCollection(issueVerifier.verifyIssue(subtask, workflowMigrationMapping, true));
            }
        }

        if (errorCollection != null && errorCollection.hasAnyErrors())
        {
            // Do not complete the migration
            addErrorCollection(errorCollection);
            return "workflowmigrationerror";
        }


        Transaction txn = Txn.begin();

        // Move attachments if we are moving to a new project
        if (!(getProject().equals(getTargetProject())))
        {
            final Project project = getTargetProjectObj();
            updatedIssueObject.setProjectObject(project);
            final long incCount = getProjectManager().getNextId(project);
            updatedIssueObject.setNumber(incCount);
            attachmentManager.moveAttachments(updatedIssueObject, updatedIssueObject.getKey());
        }

        try
        {
            moveIssueInTxn(txn, originalIssueObject, updatedIssueObject, originalWfId);
        }
        finally
        {
            txn.finallyRollbackIfNotCommitted();
        }


        // Move subtasks to target and create changelog
        if (!updatedIssueObject.isSubTask() && !updatedIssueObject.getSubTaskObjects().isEmpty())
        {
            moveSubTasks(updatedIssueObject);
        }

        return getRedirect("/browse/" + getKey());
    }

    private void moveIssueInTxn(Transaction txn, MutableIssue originalIssueObject, MutableIssue updatedIssueObject, String originalWfId)
            throws GenericEntityException
    {
        // Only migrate the issue if the target workflow is different from the current workflow
        if (!isWorkflowMatch(getCurrentIssueType(), getTargetIssueType()))
        {
            migrateIssueToWorkflow(updatedIssueObject.getGenericValue(), getIssue().getString("type"), getWorkflowForType(getTargetPid(), getTargetIssueType()), getTargetStatusGV());
        }

        // Log and set new details for target
        updatedIssueObject.setUpdated(new Timestamp(System.currentTimeMillis()));
        IssueChangeHolder issueChangeHolder = moveIssueDetails(updatedIssueObject, originalIssueObject.getKey(), originalWfId);

        //create and store the changelog for this whole process
        GenericValue updateLog = ChangeLogUtils.createChangeGroup(getLoggedInUser(), originalIssueObject.getGenericValue(),
                                        updatedIssueObject.getGenericValue(), issueChangeHolder.getChangeItems(), false);
        // remember the old issue key so we can do fast URL redirects
        if (projectIsMoved())
            issueManager.recordMovedIssueKey(originalIssueObject);

        txn.commit();

        dispatchIssueUpdateEvents(updatedIssueObject, updateLog, issueChangeHolder);
    }

    private boolean projectIsMoved()
    {
        return !getProject().equals(getTargetProject());
    }

    // ---- Move issue details methods ----
    private void dispatchIssueUpdateEvents(Issue newIssue, GenericValue updateLog, IssueChangeHolder issueChangeHolder)
            throws GenericEntityException
    {
        if (updateLog != null && !issueChangeHolder.getChangeItems().isEmpty())
        {
            //dispatch the event - moving a subtask is actually an update event rather than a move event
            Long eventTypeId;
            if (isSubTask() || getProject().equals(getTargetProject()))
            {
                eventTypeId = EventType.ISSUE_UPDATED_ID;
            }
            else
            {
                eventTypeId = EventType.ISSUE_MOVED_ID;
            }
            issueEventManager.dispatchRedundantEvent(eventTypeId, newIssue, getLoggedInUser(), updateLog, true, issueChangeHolder.isSubtasksUpdated());

            ApplicationUser user = getLoggedInApplicationUser();
            IssueEventBundle issueEventBundle = issueEventBundleFactory.createIssueUpdateEventBundle(
                    newIssue,
                    updateLog,
                    toIssueUpdateBean(issueChangeHolder, eventTypeId, user, true),
                    user
            );
            issueEventManager.dispatchEvent(issueEventBundle);
        }
    }

    // Create change log items for all new details - set details of "moved" issue also
    private IssueChangeHolder moveIssueDetails(MutableIssue newIssue, String oldKey, String originalWfId)
            throws GenericEntityException, WorkflowException
    {
        IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();

        GenericValue currentIssueTypeGV = getConstantsManager().getIssueType(getCurrentIssueType());
        GenericValue targetIssueTypeGV = getConstantsManager().getIssueType(getTargetIssueType());

        String newKey = newIssue.getKey();

        // Set new project and issue key - issue key only changes if issue is moving to new project
        if (!(getProject().equals(getTargetProject())))
        {
            changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.PROJECT, getProject().getLong("id").toString(), getProject().getString("name"), getTargetProject().getLong("id").toString(), getTargetProject().getString("name")));
            changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Key", null, oldKey, null, newKey));
        }

        // Check if issue type is changing
        if (isSubTask() || !getCurrentIssueType().equals(getTargetIssueType()))
        {
            changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.ISSUE_TYPE, currentIssueTypeGV.getString("id"), currentIssueTypeGV.getString("name"), targetIssueTypeGV.getString("id"), targetIssueTypeGV.getString("name")));
            newIssue.setIssueType(getTargetIssueTypeGV());
        }

        // Only log a workflow/status change if the target workflow is different from the current workflow
        if (!isWorkflowMatch(getCurrentIssueType(), getTargetIssueType()))
        {
            changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Workflow", originalWfId, getWorkflowManager().getWorkflow(getIssue()).getName(), newIssue.getLong("workflowId").toString(), getWorkflowForType(getTargetPid(), getTargetIssueType()).getName()));

            if (!isStatusMatch())
            {
                changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.STATUS, getCurrentStatusGV().getString("id"), getCurrentStatusGV().getString("name"), getTargetStatusGV().getString("id"), getTargetStatusGV().getString("name")));
            }
        }

        // Store the issue
        newIssue.store();

        // Maybe move this code to the issue.store() method
        Map<String, ModifiedValue> modifiedFields = newIssue.getModifiedFields();
        for (final String fieldId : modifiedFields.keySet())
        {
            if (getFieldManager().isOrderableField(fieldId))
            {
                OrderableField field = getFieldManager().getOrderableField(fieldId);
                FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(newIssue).getFieldLayoutItem(field);
                field.updateValue(fieldLayoutItem, newIssue, modifiedFields.get(fieldId), changeHolder);

                // This code will only become necessary if the following fields become editable during a move operation.
                /*if (IssueFieldConstants.SUMMARY.equals(fieldId) || IssueFieldConstants.DESCRIPTION.equals(fieldId) ||
                        IssueFieldConstants.ENVIRONMENT.equals(fieldId))
                {
                    modifiedText.append(modifiedFields.get(fieldId)).append(" ");
                }*/
            }
        }
        // Reset the fields as they all have been persisted to the db. Maybe move this code to the "createValue"
        // method of the issue, so that the fiels removes itself from the modified list as soon as it is persisted.
        newIssue.resetModifiedFields();

        return changeHolder;
    }

    // ---- Move subtask details methods ----

    /**
     * Move subtasks to new project - setting status, target custom fields, removing non-applicable current custom
     * fields and creating a changelog.
     *
     * @param parentIssue Parent issue.
     * @throws GenericEntityException GenericEntityException
     * @throws FieldValidationException FieldValidationException
     * @throws WorkflowException WorkflowException
     */
    private void moveSubTasks(Issue parentIssue)
            throws GenericEntityException, FieldValidationException, WorkflowException
    {
        Collection<Issue> subTasks = parentIssue.getSubTaskObjects();

        for (final Issue originalSubTask : subTasks)
        {
            MutableIssue targetSubTask = getIssueManager().getIssueObject(originalSubTask.getId());

            List subTaskChangeItems = new ArrayList();

            // first update to a new issue type if necessary
            final String issueTypeKey = getPrefixIssueTypeId(originalSubTask.getIssueTypeObject().getId());
            if (getMoveIssueBean().getFieldValuesHolder().containsKey(issueTypeKey))
            {
                final String newIssueType = (String) getMoveIssueBean().getFieldValuesHolder().get(issueTypeKey);
                targetSubTask.setIssueTypeId(newIssueType);
            }

            Transaction txn = Txn.begin();

            // Move attachments if we are moving to a new project
            if (!getProject().equals(getTargetProject()))
            {
                final Project project = getTargetProjectObj();
                targetSubTask.setProjectObject(project);
                final long incCount = getProjectManager().getNextId(project);
                targetSubTask.setNumber(incCount);
                attachmentManager.moveAttachments(targetSubTask, targetSubTask.getKey());

                issueManager.recordMovedIssueKey(originalSubTask);
            }

            GenericValue subTaskUpdateLog = null;
            try
            {
                // Migrate the subtask to the new workflow/status if necessary
                if (!isWorkflowMatch(originalSubTask.getIssueTypeObject().getId(), targetSubTask.getIssueTypeObject().getId()))
                {
                    List subTaskMigrationItems = migrateSubTask(originalSubTask, targetSubTask);
                    subTaskChangeItems.addAll(subTaskMigrationItems);
                }

                // Set and log subtask details.
                // Note that we can ignore the "subTasks changed" flag here, as we are only dealing with subtasks, not parents.
                List subTaskDetails = moveSubTaskDetails(originalSubTask, targetSubTask);
                subTaskChangeItems.addAll(subTaskDetails);

                //create and store the changelog for this whole process
                subTaskUpdateLog = ChangeLogUtils.createChangeGroup(getLoggedInUser(), originalSubTask, targetSubTask, subTaskChangeItems, false);

                if (subTaskUpdateLog != null && !subTaskChangeItems.isEmpty())
                {
                    targetSubTask.setUpdated(UtilDateTime.nowTimestamp());
                    //update the issue in the database
                    targetSubTask.store();
                }

                txn.commit();
            }
            finally
            {
                txn.finallyRollbackIfNotCommitted();
            }

            dispatchSubTaskUpdateEvents(targetSubTask, subTaskUpdateLog, subTaskChangeItems);
        }
    }

    private void dispatchSubTaskUpdateEvents(MutableIssue subTask, GenericValue subTaskUpdateLog, List subTaskChangeItems)
            throws GenericEntityException
    {
        if (subTaskUpdateLog != null && !subTaskChangeItems.isEmpty())
        {
            Long eventTypeId;
            if (!(getProject().equals(getTargetProject())))
            {
                eventTypeId = EventType.ISSUE_MOVED_ID;
            }
            else
            {
                eventTypeId = EventType.ISSUE_UPDATED_ID;
            }
            issueEventManager.dispatchRedundantEvent(eventTypeId, subTask, getLoggedInUser(), null, null, subTaskUpdateLog);

            ApplicationUser user = getLoggedInApplicationUser();
            IssueEventBundle issueEventBundle = issueEventBundleFactory.createIssueUpdateEventBundle(
                    subTask,
                    subTaskUpdateLog,
                    toIssueUpdateBean(changeHolderWith(subTaskChangeItems), eventTypeId, user, true),
                    user
            );
            issueEventManager.dispatchEvent(issueEventBundle);
        }
    }

    private IssueChangeHolder changeHolderWith(List changeItems)
    {
        DefaultIssueChangeHolder changeHolder = new DefaultIssueChangeHolder();
        changeHolder.addChangeItems(changeItems);
        return changeHolder;
    }

    // Move sub task details to target
    private List moveSubTaskDetails(Issue originalSubTask, MutableIssue targetSubTask)
    {
        IssueChangeHolder subTaskChangeHolder = new DefaultIssueChangeHolder();

        // Change only if target project is different from current project
        if (!(getProject().equals(getTargetProject())))
        {
            subTaskChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Project", getProject().getLong("id").toString(), getProject().getString("name"), getTargetProject().getLong("id").toString(), getTargetProject().getString("name")));
            subTaskChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Key", null, originalSubTask.getKey(), null, targetSubTask.getKey()));

            FieldLayout targetFieldLayout = getTargetFieldLayout(targetSubTask);
            Map fieldValuesHolder = new HashMap();
            List<String> targetIssueTypeIdList = ImmutableList.of(targetSubTask.getIssueTypeObject().getId());
            for (FieldLayoutItem fieldLayoutItem : targetFieldLayout.getVisibleLayoutItems(getTargetProjectObj(), targetIssueTypeIdList))
            {
                OrderableField orderableField = fieldLayoutItem.getOrderableField();
                // Security is always set to the same value as the parent's issue - so no need to process it here
                if (!IssueFieldConstants.SECURITY.equals(orderableField.getId()) && !IssueFieldConstants.ISSUE_TYPE.equals(orderableField.getId()))
                {
                    if (orderableField.needsMove(EasyList.build(originalSubTask), targetSubTask, fieldLayoutItem).getResult())
                    {
                        // For every field that needs to be updated for the move, populate the default value and save the issue
                        orderableField.populateDefaults(fieldValuesHolder, targetSubTask);
                        orderableField.updateIssue(fieldLayoutItem, targetSubTask, fieldValuesHolder);
                    }
                }
            }

            // Remove all the hidden fields
            for (Field field : targetFieldLayout.getHiddenFields(getTargetProjectObj(), targetIssueTypeIdList))
            {
                if (getFieldManager().isOrderableField(field))
                {
                    OrderableField orderableField = (OrderableField) field;
                    // Remove values of all the fields that have a value but are hidden in the target project
                    if (orderableField.hasValue(targetSubTask)
                            && orderableField.canRemoveValueFromIssueObject(targetSubTask))
                    {
                        orderableField.removeValueFromIssueObject(targetSubTask);
                    }
                }
            }

            // Store the issue
            targetSubTask.store();

            // Maybe move this code to the issue.store() method
            Map<String, ModifiedValue> modifiedFields = targetSubTask.getModifiedFields();
            for (final String fieldId : modifiedFields.keySet())
            {
                if (getFieldManager().isOrderableField(fieldId))
                {
                    OrderableField field = getFieldManager().getOrderableField(fieldId);
                    FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(targetSubTask).getFieldLayoutItem(field);
                    field.updateValue(fieldLayoutItem, targetSubTask, modifiedFields.get(fieldId), subTaskChangeHolder);

                    // This code will only become necessary if the following fields become editable during a move operation.
                    /*if (IssueFieldConstants.SUMMARY.equals(fieldId) || IssueFieldConstants.DESCRIPTION.equals(fieldId) ||
                            IssueFieldConstants.ENVIRONMENT.equals(fieldId))
                    {
                      modifiedText.append(modifiedFields.get(fieldId)).append(" ");
                    }*/
                }
            }
            // Reset the fields as they all have been persisted to the db. Maybe move this code to the "createValue"
            // method of the issue, so that the fiels removes itself from the modified list as soon as it is persisted.
            targetSubTask.resetModifiedFields();
        }

        return subTaskChangeHolder.getChangeItems();
    }

    private FieldLayout getTargetFieldLayout(Issue targetSubTask)
    {
        return getFieldLayoutManager().getFieldLayout(getTargetProject(), targetSubTask.getIssueTypeObject().getId());
    }

    // Migrate the subtasks associated with the issue that is moving
    private List migrateSubTask(final Issue originalSubtask, final MutableIssue targetSubtask)
            throws GenericEntityException
    {
        ArrayList subTaskChangeItems = new ArrayList();

        // update the target status if the original status is not valid in the target workflow
        if (!isTaskStatusValid(targetSubtask.getIssueTypeObject().getId(), targetSubtask.getStatusObject().getId()))
        {
            // Retrieve target status of subtask in new workflow from moveissuebean
            String subTaskTypeKey = getPrefixTaskStatusId(targetSubtask.getIssueTypeObject().getId(), targetSubtask.getStatusObject().getId());
            Map taskTargetStatusMap = getMoveIssueBean().getTaskTargetStatusHolder();
            targetSubtask.setStatusId((String) taskTargetStatusMap.get(subTaskTypeKey));
            subTaskChangeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.STATUS, originalSubtask.getStatusObject().getId(), originalSubtask.getStatusObject().getName(),
                    targetSubtask.getStatusObject().getId(), targetSubtask.getStatusObject().getName()));
        }

        // Migrate the subtask to the new status in the target workflow and create a changelog
        if (!isWorkflowMatch(originalSubtask.getIssueTypeObject().getId(), targetSubtask.getIssueTypeObject().getId()))
        {
            migrateIssueToWorkflow(targetSubtask.getGenericValue(), originalSubtask.getIssueTypeObject().getId(), getWorkflowForType(getTargetPid(), targetSubtask.getIssueTypeObject().getId()), targetSubtask.getStatusObject().getGenericValue());
            subTaskChangeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Workflow",
                    originalSubtask.getWorkflowId().toString(), getWorkflowManager().getWorkflow(originalSubtask.getGenericValue()).getName(),
                    targetSubtask.getWorkflowId().toString(), getWorkflowForType(getTargetPid(), targetSubtask.getIssueTypeObject().getId()).getName()));
        }
        return subTaskChangeItems;
    }

    /**
     * Migrate the specified issue to the specified workflow, specified status and target type.
     *
     * @param issue - the issue to migrate - should be the issue that will be changed
     * @param oldIssueType - the old issue type of the issue
     * @param targetWorkflow - the destination workflow
     * @param targetStatus - the destination status
     */
    protected void migrateIssueToWorkflow(GenericValue issue, String oldIssueType, JiraWorkflow targetWorkflow,
            GenericValue targetStatus)
            throws GenericEntityException
    {
        // Do not move if current worklfow is the same as the target workflow
        if (!isWorkflowMatch(oldIssueType, issue.getString("type")))
        {
            getWorkflowManager().migrateIssueToWorkflow(issue, targetWorkflow, targetStatus);
        }
    }

    // Return the custom field id with the custom field prefix
    public String getPrefixCustomFieldId(String key) throws GenericEntityException
    {
        Collection<CustomField> targetCustomFields = getTargetCustomFieldObjects(getTargetIssueType());

        for (final CustomField targetCustomField : targetCustomFields)
        {
            if (key.equals(targetCustomField.getName()))
            {
                return targetCustomField.getId();
            }
        }
        return null;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public GenericValue getTargetStatusGV()
    {
        return getConstantsManager().getStatus(getTargetStatusId());
    }

    // ---- Checks for changes made ----
    public boolean isIssueTypeMatch() throws GenericEntityException
    {
        return getCurrentIssueType().equals(getTargetIssueType());
    }

    public boolean isProjectMatch() throws GenericEntityException
    {
        return getProject().equals(getTargetProject());
    }

    public boolean isStatusMatch()
    {
        return getCurrentStatusGV().equals(getTargetStatusGV());
    }
}
