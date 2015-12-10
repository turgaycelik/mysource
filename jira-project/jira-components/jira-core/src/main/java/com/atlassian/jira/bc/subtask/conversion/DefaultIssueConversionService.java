package com.atlassian.jira.bc.subtask.conversion;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.annotations.VisibleForTesting;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.ActionContext;

import static com.atlassian.jira.issue.util.transformers.IssueChangeHolderTransformer.toIssueUpdateBean;


public abstract class DefaultIssueConversionService implements IssueConversionService
{
    private static final Logger log = Logger.getLogger(DefaultIssueConversionService.class);

    private final PermissionManager permissionManager;
    private final WorkflowManager workflowManager;
    protected final FieldLayoutManager fieldLayoutManager;
    protected final IssueTypeSchemeManager issueTypeSchemeManager;
    protected final JiraAuthenticationContext jiraAuthenticationContext;
    protected final FieldManager fieldManager;
    private final IssueEventManager issueEventManager;
    private final IssueEventBundleFactory issueEventBundleFactory;

    public DefaultIssueConversionService(
            PermissionManager permissionManager,
            WorkflowManager workflowManager,
            FieldLayoutManager fieldLayoutManager,
            IssueTypeSchemeManager issueTypeSchemeManager,
            JiraAuthenticationContext jiraAuthenticationContext,
            FieldManager fieldManager,
            IssueEventManager issueEventManager,
            IssueEventBundleFactory issueEventBundleFactory
    ) {

        this.permissionManager = permissionManager;
        this.workflowManager = workflowManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.fieldManager = fieldManager;
        this.issueEventManager = issueEventManager;
        this.issueEventBundleFactory = issueEventBundleFactory;
    }

    public boolean hasPermission(JiraServiceContext context, Issue issue)
    {
        return permissionManager.hasPermission(getPermissionNeeded(), issue, context.getLoggedInApplicationUser());
    }

    /**
     * Allows sub-classes override to use another permission
     *
     * @return permission to check
     */
    protected int getPermissionNeeded()
    {
        return Permissions.EDIT_ISSUE;
    }

    public boolean isStatusChangeRequired(JiraServiceContext context, Issue issue, IssueType issueType)
    {
        Status status = issue.getStatusObject();
        final Long projectId = issue.getProjectObject().getId();
        final String subTaskId = issueType.getId();
        return !isStatusInWorkflowForProjectAndIssueType(status, projectId, subTaskId);
    }

    public void validateTargetStatus(JiraServiceContext context, Status status, final String fieldName, Issue issue, IssueType issueType)
    {
        final Long projectId = issue.getProjectObject().getId();
        final String subTaskId = issueType.getId();
        final ErrorCollection errorCollection = context.getErrorCollection();

        if (!isStatusChangeRequired(context, issue, issueType) && !status.getId().equals(issue.getStatusObject().getId()))
        {
            errorCollection.addErrorMessage(getText("convert.issue.to.subtask.errormessage.nochangeneeded"));
        }

        if (!isStatusInWorkflowForProjectAndIssueType(status, projectId, subTaskId))
        {
            errorCollection.addError(fieldName, getText("convert.issue.to.subtask.error.invalidstatusfortargetworkflow", status.getNameTranslation()));
        }
    }

    /**
     * Retrieves the workflow for given project and issue type combination and
     * checks whether the given status is in this workflow.
     *
     * @param status      issue status
     * @param projectId   project id
     * @param issueTypeId issue type id
     * @return true if given status is in workflow specified by given project id and issue type id, false otherwise
     */
    protected boolean isStatusInWorkflowForProjectAndIssueType(Status status, Long projectId, String issueTypeId)
    {
        List<Status> linkedStatusObjects = getWorkflowForProjectAndIssueType(projectId, issueTypeId).getLinkedStatusObjects();
        for (Status wfStatus : linkedStatusObjects)
        {
            if (wfStatus.getId().equals(status.getId()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns JIRA workflow for the given project and issue type.
     *
     * @param projectId   project id
     * @param issueTypeId issue type id
     * @return JIRA workflow
     * @throws com.atlassian.jira.exception.DataAccessException
     *          if WorkflowException occurs during workflow retrieval
     */
    private JiraWorkflow getWorkflowForProjectAndIssueType(Long projectId, String issueTypeId)
    {
        try
        {
            return workflowManager.getWorkflow(projectId, issueTypeId);
        }
        catch (WorkflowException e)
        {
            String msg = "Failed retrieving workflow for project: " + projectId + " and issue type:" + issueTypeId;
            log.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    @Override
    public Collection<FieldLayoutItem> getFieldLayoutItems(JiraServiceContext context, Issue originalIssue, Issue targetIssue)
    {
        return getFieldLayoutItems(originalIssue, targetIssue);
    }

    @Override
    public Collection<FieldLayoutItem> getFieldLayoutItems(Issue originalIssue, Issue targetIssue)
    {
        List<FieldLayoutItem> convertFieldLayoutItems = new ArrayList<FieldLayoutItem>();

        // Loop over all the visible fields and see which ones require to be moved
        FieldLayout targetFieldLayout = getFieldLayout(targetIssue);
        final Project targetProject = targetIssue.getProjectObject();
        final List<String> issueTypeIds = Arrays.asList(targetIssue.getIssueTypeObject().getId());
        final List<FieldLayoutItem> visibleLayoutItems = targetFieldLayout.getVisibleLayoutItems(targetProject, issueTypeIds);

        for (FieldLayoutItem fieldLayoutItem : visibleLayoutItems)
        {
            OrderableField orderableField = fieldLayoutItem.getOrderableField();
            // Issue type is shown on the first stage of the move so no need to work with it here
            // If issueSecurityFieldIgnore is true and the field is the security level, it will not be added
            // to the list. This applies for issue -> subtask conversion.  Not for subtask -> issue conversion.
            if (!IssueFieldConstants.ISSUE_TYPE.equals(orderableField.getId()) &&
                    !(canIssueSecurityFieldIgnore() && IssueFieldConstants.SECURITY.equals(orderableField.getId()))
                    && orderableField.needsMove(EasyList.build(originalIssue), targetIssue, fieldLayoutItem).getResult())
            {
                // Record that the field needs to be edited for the move
                convertFieldLayoutItems.add(fieldLayoutItem);
            }
        }

        //ensure the fieldlayoutitems are sorted, to make testing easier.
        Collections.sort(convertFieldLayoutItems);

        return convertFieldLayoutItems;
    }

    /**
     * For an issue to subtask conversion, we can safely ignore the issue security field since the subtask
     * will always take the security level of the parent issue.  This however is not the case for subtask to
     * issue conversion
     *
     * @return true if it is safe to ignore the security field.
     */
    protected abstract boolean canIssueSecurityFieldIgnore();


    /**
     * Utility method tat returns field layout for given issue
     *
     * @param issue issue to get the field layout from
     * @return issue's field layout
     */
    private FieldLayout getFieldLayout(Issue issue)
    {
        return fieldLayoutManager.getFieldLayout(issue);
    }


    public void populateFields(JiraServiceContext context, OperationContext operationContext, I18nHelper i18nHelper, Issue targetIssue, Collection<FieldLayoutItem> fieldLayoutItems)
    {
        ErrorCollection errorCollection = context.getErrorCollection();

        // Loop over all the fields that need to be edited for the move
        for (FieldLayoutItem fieldLayoutItem : fieldLayoutItems)
        {
            OrderableField orderableField = fieldLayoutItem.getOrderableField();

            if (orderableField.isShown(targetIssue))
            {
                // As the issue has been shown then initialise it from the action's parameters
                orderableField.populateFromParams(operationContext.getFieldValuesHolder(), ActionContext.getParameters());
                // Validate the value of the field
                orderableField.validateParams(operationContext, errorCollection, i18nHelper, targetIssue, new FieldScreenRenderLayoutItemImpl(null, fieldLayoutItem));
            }
            else
            {
                // If the field has not been shown (but it needs to be moved) populate with the default value
                orderableField.populateDefaults(operationContext.getFieldValuesHolder(), targetIssue);

                // Validate the parameter. In theory as the field places a default value itself the value should be valid, however, a check for
                // 'requireability' still has to be made.
                orderableField.validateParams(operationContext, errorCollection, i18nHelper, targetIssue, new FieldScreenRenderLayoutItemImpl(null, fieldLayoutItem));
            }
        }
    }

    @Override
    public Collection<OrderableField> getRemovedFields(JiraServiceContext context, Issue origIssue, Issue targetIssue)
    {
        return getRemovedFields(origIssue, targetIssue);
    }

    @Override
    public Collection<OrderableField> getRemovedFields(Issue origIssue, Issue targetIssue)
    {
        Collection<OrderableField> removedFields = new ArrayList<OrderableField>();

        FieldLayout targetFieldLayout = getFieldLayout(targetIssue);

        Collection<Field> hiddenFields = targetFieldLayout.getHiddenFields(targetIssue.getProjectObject(), Arrays.asList(targetIssue.getIssueTypeObject().getId()));

        // Hidden fields include custom fields that are not in scope
        for (Field field : hiddenFields)
        {
            if (field != null && fieldManager.isOrderableField(field))
            {
                boolean doValueCheck = isShouldCheckFieldValue(origIssue, field);

                OrderableField orderableField = (OrderableField) field;
                // Remove values of all the fields that have a value but are hidden in the target project
                if (doValueCheck && orderableField.hasValue(targetIssue)
                        && orderableField.canRemoveValueFromIssueObject(targetIssue))
                {
                    removedFields.add(orderableField);
                }
            }
        }
        return removedFields;

    }

    /**
     * JRA-12671 - need to determine if we should call hasValue on the field.  For calculated custom fields, that are not
     * in scope this isn't the case.  This should prevent us from calling hasValue on calculated custom fields.
     *
     * protected to make it testable.
     * @param origIssue The original issue in which we'll check the context
     * @param field The field in question.
     * @return true, if the field is not a custom value, or it is in scope in the original issue.
     */
    protected boolean isShouldCheckFieldValue(Issue origIssue, Field field)
    {
        boolean doValueCheck = true;
        List<String> issueTypeList = Arrays.asList(origIssue.getIssueTypeObject().getId());
        if (fieldManager.isCustomField(field) && !((CustomField) field).isInScope(origIssue.getProjectObject(), issueTypeList))
        {
            doValueCheck = false;
        }
        return doValueCheck;
    }

    public void validateFields(JiraServiceContext context, OperationContext operationContext, I18nHelper i18nHelper, Issue targetIssue, Collection<FieldLayoutItem> fieldLayoutItems)
    {
        ErrorCollection errorCollection = context.getErrorCollection();

        // Loop over all the fields that need to be edited for the move
        for (FieldLayoutItem fieldLayoutItem : fieldLayoutItems)
        {
            OrderableField orderableField = fieldLayoutItem.getOrderableField();

            orderableField.validateParams(operationContext, errorCollection, i18nHelper, targetIssue, new FieldScreenRenderLayoutItemImpl(null, fieldLayoutItem));

        }
    }

    /**
     * Create change log items for all new details - set details of "moved" issue also
     *
     * @param context      jira service context
     * @param currentIssue current issue
     * @param targetIssue  target issue - sub-task
     * @return list of {@link com.atlassian.jira.issue.history.ChangeItemBean}
     */
    private IssueChangeHolder convertIssueDetails(JiraServiceContext context, Issue currentIssue, MutableIssue targetIssue)
    {
        IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();

        // Only log a workflow/status change if the target workflow is different from the current workflow
        if (!currentIssue.getWorkflowId().equals(targetIssue.getWorkflowId()))
        {
            JiraWorkflow currentWorkflow = getWorkflowForIssue(currentIssue);
            JiraWorkflow targetWorkflow = getWorkflowForIssue(targetIssue);
            changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Workflow", currentIssue.getWorkflowId().toString(), currentWorkflow.getName(),
                    targetIssue.getWorkflowId().toString(), targetWorkflow.getName()));

            Status currentStatus = currentIssue.getStatusObject();
            Status targetStatus = targetIssue.getStatusObject();
            if (!currentStatus.getId().equals(targetStatus.getId()))
            {

                changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.STATUS, currentStatus.getId(), currentStatus.getName(),
                        targetStatus.getId(), targetStatus.getName()));
            }
        }

        preStoreUpdates(context, changeHolder, currentIssue, targetIssue);

        // Store the issue
        targetIssue.store();

        // Update values in the database
        Map<String, ModifiedValue> modifiedFields = targetIssue.getModifiedFields();
        for (final String fieldId : modifiedFields.keySet())
        {
            if (fieldManager.isOrderableField(fieldId))
            {
                OrderableField field = fieldManager.getOrderableField(fieldId);
                FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(targetIssue).getFieldLayoutItem(field);
                field.updateValue(fieldLayoutItem, targetIssue, modifiedFields.get(fieldId), changeHolder);
            }
        }
        // Reset the fields as they all have been persisted to the db. Maybe move this code to the "createValue"
        // method of the issue, so that the fiels removes itself from the modified list as soon as it is persisted.
        targetIssue.resetModifiedFields();

        return changeHolder;
    }

    /**
     * Convenience method that returns a workflow retrieved based on issue's
     * project and issue type.
     * <br/>
     * If you need to retrieve a workflow in a more flexible way use
     * {@link #getWorkflowForProjectAndIssueType(Long,String)} method.
     *
     * @param issue issue to read the project and issue type from
     * @return workflow based on issue's project and issue type
     */
    private JiraWorkflow getWorkflowForIssue(Issue issue)
    {
        return getWorkflowForProjectAndIssueType(issue.getProjectObject().getId(), issue.getIssueTypeObject().getId());
    }

    public void convertIssue(JiraServiceContext context, Issue currentIssue, MutableIssue updatedIssue)
    {

        // migrate workflow first
        migrateWorkflowIfNecessary(currentIssue, updatedIssue);

        // Log and set new details for target
        updatedIssue.setUpdated(new Timestamp(System.currentTimeMillis()));
        IssueChangeHolder issueChangeHolder = convertIssueDetails(context, currentIssue, updatedIssue);

        //create and store the changelog for this whole process
        GenericValue updateLog = ChangeLogUtils.createChangeGroup(context.getLoggedInUser(), currentIssue, updatedIssue,
                issueChangeHolder.getChangeItems(), false);

        if (updateLog != null && !issueChangeHolder.getChangeItems().isEmpty())
        {
            dispatchEvents(updatedIssue, context, updateLog, issueChangeHolder);
        }

    }

    @VisibleForTesting
    void dispatchEvents(Issue updatedIssue, JiraServiceContext context, GenericValue updateLog, IssueChangeHolder issueChangeHolder)
    {
        Long eventType = EventType.ISSUE_UPDATED_ID;
        ApplicationUser applicationUser = context.getLoggedInApplicationUser();

        issueEventManager.dispatchRedundantEvent(eventType, updatedIssue, context.getLoggedInUser(), updateLog, true, issueChangeHolder.isSubtasksUpdated());

        IssueUpdateBean issueUpdateBean = toIssueUpdateBean(issueChangeHolder, eventType, applicationUser, true);
        IssueEventBundle issueUpdateEventBundle = issueEventBundleFactory.createIssueUpdateEventBundle(
                updatedIssue,
                updateLog,
                issueUpdateBean,
                applicationUser
        );
        issueEventManager.dispatchEvent(issueUpdateEventBundle);
    }

    /**
     * Migrates the updated issue to a workflow based on its project and issue
     * type only if it differs from the current issue's workflow.
     *
     * @param currentIssue current issue
     * @param updatedIssue updated issue
     * @throws com.atlassian.jira.exception.DataAccessException
     *          if migration fails
     */
    private void migrateWorkflowIfNecessary(Issue currentIssue, MutableIssue updatedIssue)
    {
        JiraWorkflow currentWorkflow = getWorkflowForIssue(currentIssue);
        JiraWorkflow targetWorkflow = getWorkflowForIssue(updatedIssue);

        if (!targetWorkflow.equals(currentWorkflow))
        {
            try
            {
                workflowManager.migrateIssueToWorkflow(updatedIssue, targetWorkflow, updatedIssue.getStatusObject());
            }
            catch (WorkflowException e)
            {
                String msg = "Could not migrate to sub-task workflow for issue: " + currentIssue.getKey();
                log.error(msg, e);
                throw new DataAccessException(msg, e);
            }
        }
    }

    /**
     * Translates a given key using i18n bean
     *
     * @param key key to translate
     * @return i18n string for given key
     */
    protected String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }

    /**
     * Translates a given key using i18n bean, passing in param
     *
     * @param key   key to transkate
     * @param param param to insert into property
     * @return i18n string for given key, with param inserted
     */
    protected String getText(String key, Object param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }

    /**
     * Translates a given key using i18n bean, passing in params
     *
     * @param key    key to transkate
     * @param param0 1st param to insert into property
     * @param param1 2nd param to insert into property
     * @return i18n string for given key, with params inserted
     */
    protected String getText(String key, String param0, String param1)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param0, param1);
    }
}
