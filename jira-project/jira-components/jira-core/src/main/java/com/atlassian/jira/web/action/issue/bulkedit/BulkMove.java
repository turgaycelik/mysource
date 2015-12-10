package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.impl.VersionCFType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class BulkMove extends AbstractBulkOperationDetailsAction
{
    private static final String PARENT_SELECTION = "parent";

    private boolean subTaskPhase = false;

    protected BulkMoveOperation bulkMoveOperation;

    protected final FieldManager fieldManager;
    protected final WorkflowManager workflowManager;
    protected final ConstantsManager constantsManager;
    protected final IssueFactory issueFactory;
    protected final PermissionManager permissionManager;

    public BulkMove(final SearchService searchService, final BulkMoveOperation bulkMoveOperation,
                    final FieldManager fieldManager, final WorkflowManager workflowManager,
                    final ConstantsManager constantsManager, final IssueFactory issueFactory,
                    final PermissionManager permissionManager,
                    final BulkEditBeanSessionHelper bulkEditBeanSessionHelper,
                    final TaskManager taskManager, final I18nHelper i18nHelper)
    {
        super(searchService, bulkEditBeanSessionHelper, taskManager, i18nHelper);
        this.fieldManager = fieldManager;
        this.workflowManager = workflowManager;
        this.bulkMoveOperation = bulkMoveOperation;
        this.constantsManager = constantsManager;
        this.issueFactory = issueFactory;
        this.permissionManager = permissionManager;
    }

    // TODO many of the doXxx() methods in this Action are no longer called since we moved to BulkMigrate.

    public String doDefault() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        // set BulkEditBean to use the issues that have now been selected rather than the issues from the search request
        getBulkEditBean().setIssuesInUse(getBulkEditBean().getSelectedIssues());
        getBulkEditBean().setOperationName(BulkMoveOperation.NAME);

        return super.doDefault();
    }

    // Verify selection of issues - parents or subtasks
    public String doDetails()
    {
        final BulkEditBean bulkEditBean = getRootBulkEditBean();

        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (bulkEditBean == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        // Reset the collection to contain only parent issues (subtasks can't be "moved" only migrated)
        resetIssueCollection(PARENT_SELECTION);

        bulkEditBean.resetMoveData();
        bulkEditBean.clearAvailablePreviousSteps();
        bulkEditBean.addAvailablePreviousStep(1);
        bulkEditBean.addAvailablePreviousStep(2);
        bulkEditBean.setCurrentStep(3);
        return INPUT;
    }

    // Verify and perform the move operation
    public String doPerform() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        try
        {
            // Ensure the user has the global BULK CHANGE permission
            if (!permissionManager.hasPermission(Permissions.BULK_CHANGE, getLoggedInUser()))
            {
                addErrorMessage(getText("bulk.change.no.permission",
                        String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            }

            // Ensure the user can perform the operation
            if (!getBulkMoveOperation().canPerform(getRootBulkEditBean(), getLoggedInApplicationUser()))
            {
                addErrorMessage(getText("bulk.edit.cannotperform.error",
                        String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            }
        }
        catch (Exception e1)
        {
            log.error("Error occurred while testing operation.", e1);
            addErrorMessage(getText("bulk.canperform.error"));
        }

        if (invalidInput())
        {
            return ERROR;
        }

        final String taskName = getText("bulk.operation.progress.taskname.move",
                getRootBulkEditBean().getSelectedIssuesIncludingSubTasks().size());
        return submitBulkOperationTask(getRootBulkEditBean(), getBulkMoveOperation(), taskName);
    }

    public String doDetailsValidation() throws Exception
    {
        throw new IllegalArgumentException("This should never be called.");
    }

    public boolean isHasAvailableActions() throws Exception
    {
        return getBulkMoveOperation().canPerform(getBulkEditBean(), getLoggedInApplicationUser());
    }

    // Validate the Project and Issue Type selected
    public String doContextValidation() throws Exception
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        getBulkEditBean().resetMoveData();

        // Validate & commit context
        getBulkMoveOperation().chooseContext(getBulkEditBean(), getLoggedInApplicationUser(), this, this);

        if (invalidInput())
        {
            return ERROR;
        }

        // Check if status change is required for any issues
        if (!getBulkMoveOperation().isStatusValid(getBulkEditBean()))
        {
            return "statuserror";
        }
        else
        {
            return SUCCESS;
        }
    }

    // Populate status mappings for parents and subtasks
    public String doStatusValidation() throws Exception
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        getBulkMoveOperation().setStatusFields(getBulkEditBean());

        return getResult();
    }

    public String doFieldsValidation() throws Exception
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        getBulkMoveOperation().validatePopulateFields(getBulkEditBean(), this, this);

        if (invalidInput())
        {
            return ERROR;
        }

        // If there's another layer of sub-tasking
        if (getBulkEditBean().getSubTaskBulkEditBean() != null)
        {
            setSubTaskPhase(true);
            return "subtaskphase";
        }
        else
        {
            setSubTaskPhase(false);
            // Progress to the final level
            progressToLastStep();

            return getResult();
        }
    }

    /**
     * Only invoked when displaying the Project and Issue type as part of the bulk move operation
     *
     * @return Field HTML
     */
    public String getFieldHtml(final String fieldId, final BulkEditBean bulkEditBean)
    {
        final OrderableField orderableField = (OrderableField) fieldManager.getField(fieldId);
        return orderableField.getBulkEditHtml(bulkEditBean, this, bulkEditBean,
                EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.TRUE,
                        "fieldNamePrefix", bulkEditBean.getKey()));
    }

    public String getFieldHtml(final String fieldId)
    {
        return getFieldHtml(fieldId, getBulkEditBean());
    }

    /**
     * Used when displaying the fields to be edited during the bulk move operation
     *
     * @return Field HTML
     */
    public String getFieldHtml(final FieldLayoutItem fieldLayoutItem)
    {
        final OrderableField orderableField = fieldLayoutItem.getOrderableField();
        if (orderableField.isShown(getBulkEditBean().getFirstTargetIssueObject()))
        {
            // Need to display edit template with target fieldlayout item
            return orderableField.getBulkEditHtml(getBulkEditBean(), this, getBulkEditBean(),
                    EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.TRUE));
        }
        else
        {
            return "";
        }
    }

    public boolean isIssueTypesAvailable()
    {
        final IssueTypeSystemField issueTypeField =
                (IssueTypeSystemField) fieldManager.getField(IssueFieldConstants.ISSUE_TYPE);
        return !issueTypeField.isHasCommonIssueTypes(getBulkEditBean().getSelectedIssues());
    }

    // Determine whether there are available target subtasks
    public boolean isSubTaskTypesAvailable()
    {
        final IssueTypeSystemField issueTypeField =
                (IssueTypeSystemField) fieldManager.getField(IssueFieldConstants.ISSUE_TYPE);
        final Collection<Issue> selectedIssues = getBulkEditBean().getSelectedIssues();
        final Collection selectedSubTasks = new ArrayList();

        for (final Issue issue : selectedIssues)
        {
            if (issue.isSubTask())
            {
                selectedSubTasks.add(issue);
            }
        }
        return !issueTypeField.isHasCommonIssueTypes(selectedSubTasks);
    }

    public String getFieldViewHtml(final OrderableField orderableField)
    {
        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("readonly", Boolean.TRUE)
                .add("nolink", Boolean.TRUE)
                .add("bulkoperation", getBulkEditBean().getOperationName()).toMutableMap();

        // Use the layout item of where we are going since we are moving to that space
        final FieldLayoutItem layoutItem = getBulkEditBean().getTargetFieldLayout().getFieldLayoutItem(orderableField);
        return orderableField.getViewHtml(layoutItem, this, getBulkEditBean().getFirstTargetIssueObject(),
                getBulkEditBean().getFieldValues().get(orderableField.getId()), displayParams);
    }

    public Collection getMoveFieldLayoutItems()
    {
        return getBulkEditBean().getMoveFieldLayoutItems();
    }

    public String getFieldName(final Field field)
    {
        if (field instanceof CustomField)
        {
            return field.getName();
        }
        else
        {
            return getText(field.getNameKey());
        }
    }

    public String getNewViewHtml(final OrderableField field)
    {
        final Map displayParameters =
                MapBuilder.newBuilder("readonly", Boolean.TRUE).add("nolink", Boolean.TRUE).toMap();
        return field.getViewHtml(getBulkEditBean().getTargetFieldLayout().getFieldLayoutItem(field), this,
                getBulkEditBean().getFirstTargetIssueObject(), displayParameters);
    }

    public String getNewViewHtml(final BulkEditBean bulkEditBean, final OrderableField field)
    {
        final Map<String, Object> displayParameters = MapBuilder.<String, Object>newBuilder("readonly", Boolean.TRUE)
                .add("nolink", Boolean.TRUE)
                .add("prefix",
                        bulkEditBean.getProject().getString("id") + "_" + bulkEditBean.getIssueType().getString("id") +
                                "_")
                .toMap();
        return field.getViewHtml(bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(field), this,
                bulkEditBean.getFirstTargetIssueObject(), displayParameters);
    }

    public boolean isFieldUsingSubstitutions(final BulkEditBean bulkEditBean, final OrderableField field)
    {
        final Map<Long, Long> substitutions = bulkEditBean.getFieldSubstitutionMap().get(field.getId());
        return substitutions != null;
    }

    public Map<Long, Long> getSubstitutionsForField(final BulkEditBean bulkEditBean, final OrderableField field)
    {
        return bulkEditBean.getFieldSubstitutionMap().get(field.getId());
    }

    public String getMappingViewHtml(final BulkEditBean bulkEditBean, final OrderableField field, final Long id,
                                     final boolean showProject)
    {
        final FieldLayoutItem fieldLayoutItem = bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(field);
        final Issue baseIssue = bulkEditBean.getFirstTargetIssueObject();
        return getViewHtmlForId(baseIssue, fieldLayoutItem, field, id, showProject);
    }

    private String getViewHtmlForId(final Issue baseIssue, final FieldLayoutItem fieldLayoutItem,
                                    final OrderableField field, final Long id, final boolean showProject)
    {
        // -1 means no value 
        if (id == null || id == -1L)
        {
            return getText("common.words.unknown");
        }

        // dummy up the value for an issue
        final Object value;
        if (field instanceof CustomField)
        {
            value = getValueForId((CustomField) field, id);
        }
        else
        {
            value = getValueForId(field, id);
        }

        // set the value in a field values holder
        final Map<String, Object> fieldValuesHolder = new LinkedHashMap<String, Object>();
        fieldValuesHolder.put(field.getId(), value);

        // update an issue with these values
        final MutableIssue dummyIssue = issueFactory.cloneIssue(baseIssue);
        field.updateIssue(fieldLayoutItem, dummyIssue, fieldValuesHolder);

        // now render the field
        if (showProject)
        {
            return field.getViewHtml(fieldLayoutItem, this, dummyIssue, MapBuilder.singletonMap("showProject", true));
        }
        else
        {
            return field.getViewHtml(fieldLayoutItem, this, dummyIssue);
        }
    }

    // custom field values are expected to be as a list of strings inside a CustomFieldParams
    private CustomFieldParams getValueForId(final CustomField customField, final Long id)
    {
        return new CustomFieldParamsImpl(customField, Collections.singletonList(id.toString()));
    }

    // system field values are simply a collection of Longs
    private Collection<Long> getValueForId(final OrderableField orderableField, final Long id)
    {
        return Collections.singletonList(id);
    }

    public boolean isAvailable(final String action) throws Exception
    {
        return true;
    }

    public boolean isAllowProjectEdit()
    {
        return isAllowProjectEdit(getBulkEditBean());
    }

    public boolean isAllowProjectEdit(final BulkEditBean bulkEditBean)
    {
        return !bulkEditBean.isSubTaskCollection();
    }

    public String getOperationDetailsActionName()
    {
        return getBulkMoveOperation().getOperationName() + "Details.jspa";
    }

    // This is taken out as a protected method such that it can be overridden, and the doValidation() method reused by subclass actions
    protected void populateFromParams(final OrderableField orderableField)
    {
        orderableField.populateFromParams(getBulkEditBean().getFieldValuesHolder(), ActionContext.getParameters());
    }

    // Retrieve collection of target workflow statuses from the workflow associated with the specified issue type id
    public Collection getTargetWorkflowStatuses(final String issueTypeId) throws WorkflowException
    {
        final JiraWorkflow workflow = getWorkflowForType(getBulkEditBean().getTargetPid(), issueTypeId);
        return workflow.getLinkedStatuses();
    }

    public JiraWorkflow getWorkflowForType(final Long projectId, final String issueTypeId) throws WorkflowException
    {
        return workflowManager.getWorkflow(projectId, issueTypeId);
    }

    public String getStatusName(final String id)
    {
        return constantsManager.getStatus(id).getString("name");
    }

    public String getCurrentTargetPid()
    {
        return String.valueOf(getBulkEditBean().getTargetPid());
    }

    public GenericValue getCurrentTargetProject()
    {
        return getBulkEditBean().getTargetProjectGV();
    }

    // Reset the selected issues to contain only parent or sub-task issues
    private void resetIssueCollection(final String collectionType)
    {
        final Collection<Issue> selectedIssues = getBulkEditBean().getSelectedIssues();
        final Collection modifiedSelection = new ArrayList();

        if (PARENT_SELECTION.equals(collectionType))
        {
            for (final Issue issue : selectedIssues)
            {
                if (!issue.isSubTask())
                {
                    modifiedSelection.add(issue);
                }
            }
        }
        else
        {
            for (final Issue issue : selectedIssues)
            {
                if (issue.isSubTask())
                {
                    modifiedSelection.add(issue);
                }
            }
        }
        getBulkEditBean().initSelectedIssues(modifiedSelection);

    }

    protected void progressToLastStep()
    {
        if (getRootBulkEditBean() != null)
        {
            getRootBulkEditBean().clearAvailablePreviousSteps();
            getRootBulkEditBean().addAvailablePreviousStep(1);
            getRootBulkEditBean().addAvailablePreviousStep(2);
            getRootBulkEditBean().addAvailablePreviousStep(3);
            getRootBulkEditBean().setCurrentStep(4);
        }
    }

    private BulkMoveOperation getBulkMoveOperation()
    {
        return bulkMoveOperation;
    }

    protected MutableIssue getIssueObject(final GenericValue issueGV)
    {
        return issueFactory.getIssue(issueGV);
    }

    public ConstantsManager getConstantsManager()
    {
        return ComponentAccessor.getConstantsManager();
    }

    public String getCurrentIssueType()
    {
        return ((Issue) getBulkEditBean().getSelectedIssues().get(0)).getIssueTypeObject().getId();
    }

    public boolean isSubTaskPhase()
    {
        return subTaskPhase;
    }

    public void setSubTaskPhase(final boolean subTaskPhase)
    {
        this.subTaskPhase = subTaskPhase;
    }

    /**
     * Method to determine if a field must try to retain the values already set in issues. In the case of Components, Versions
     * and Version custom fields, we must retain where possible since if we select issues that don't need moving, then
     * no mapping options will be presented, but we don't want other values to be chosen for those issues. Hence, their
     * values must be retained.
     *
     * @param field the field to check for
     * @return true if retaining should be mandatory; false otherwise.
     */
    public boolean isRetainMandatory(final OrderableField field)
    {
        if (field instanceof CustomField)
        {
            final CustomField customField = (CustomField) field;
            return customField.getCustomFieldType() instanceof VersionCFType;
        }
        else
        {
            final String id = field.getId();
            return (IssueFieldConstants.FIX_FOR_VERSIONS.equals(id) ||
                    IssueFieldConstants.AFFECTED_VERSIONS.equals(id) || IssueFieldConstants.COMPONENTS.equals(id));
        }
    }

    public BulkEditBean getBulkEditBean()
    {
        final BulkEditBean bulkEditBean = getRootBulkEditBean();
        if (!isSubTaskPhase())
        {
            return bulkEditBean;
        }
        else
        {
            return bulkEditBean != null ? bulkEditBean.getSubTaskBulkEditBean() : null;
        }
    }
}

