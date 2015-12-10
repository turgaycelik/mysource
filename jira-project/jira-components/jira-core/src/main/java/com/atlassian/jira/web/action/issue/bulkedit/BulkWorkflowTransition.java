package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.operation.BulkEditAction;
import com.atlassian.jira.bulkedit.operation.BulkEditActionImpl;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItemImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.issue.util.ScreenTabErrorHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.jelly.util.NestedRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class BulkWorkflowTransition extends AbstractBulkOperationDetailsAction
{
    private static final String WORKFLOW_TRANSITION = "wftransition";
    public static final String RADIO_ERROR_MSG = "buik.edit.must.select.one.action.to.perform";
    private static final String FORCED_RESOLUTION = "forcedResolution";

    // actions array is retrieved from the checkbox group in the JSP called "actions"
    // this stores the fields that the user has indicated an intention to bulk edit (ie. my checking it)
    private String[] actions;
    private Map selectedActions;
    private String commentaction;

    // Cache the fields available for editing per field screen tab
    // Maps the field tab name -> collection of available fields for editing
    private Map editActionsMap;

    private SortedSet tabsWithErrors;
    private int selectedTab;

    private final IssueWorkflowManager issueWorkflowManager;
    private final WorkflowManager workflowManager;
    private final IssueManager issueManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final FieldManager fieldManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final JiraAuthenticationContext authenticationContext;
    private final BulkWorkflowTransitionOperation bulkWorkflowTransitionOperation;
    private BulkEditActionImpl commentBulkEditAction;
    private final PermissionManager permissionManager;
    private final ConstantsManager constantsManager;

    public BulkWorkflowTransition(final SearchService searchService, final IssueWorkflowManager issueWorkflowManager,
                                  final WorkflowManager workflowManager, final IssueManager issueManager,
                                  final FieldScreenRendererFactory fieldScreenRendererFactory,
                                  final FieldManager fieldManager,
                                  final JiraAuthenticationContext authenticationContext,
                                  final FieldLayoutManager fieldLayoutManager,
                                  final BulkWorkflowTransitionOperation bulkWorkflowTransitionOperation,
                                  final PermissionManager permissionManager, final ConstantsManager constantsManager,
                                  final BulkEditBeanSessionHelper bulkEditBeanSessionHelper,
                                  final TaskManager taskManager,
                                  final I18nHelper i18nHelper)
    {
        super(searchService, bulkEditBeanSessionHelper, taskManager, i18nHelper);
        this.issueWorkflowManager = issueWorkflowManager;
        this.workflowManager = workflowManager;
        this.issueManager = issueManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.fieldManager = fieldManager;
        this.authenticationContext = authenticationContext;
        this.fieldLayoutManager = fieldLayoutManager;
        this.bulkWorkflowTransitionOperation = bulkWorkflowTransitionOperation;
        this.permissionManager = permissionManager;
        this.constantsManager = constantsManager;
    }

    public String doDetails() throws Exception
    {
        final BulkEditBean bulkEditBean = getBulkEditBean();

        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (bulkEditBean == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        bulkEditBean.setCurrentStep(3);
        getBulkEditBean().addAvailablePreviousStep(2);

        // Ensure that bulk notification can be disabled
        if (isCanDisableMailNotifications())
            bulkEditBean.setSendBulkNotification(false);
        else
            bulkEditBean.setSendBulkNotification(true);

        setWorkflowTransitionMap();

        // Ensure no selections are still available
        bulkEditBean.setActions(null);
        bulkEditBean.setSelectedWFTransitionKey(null);

        return INPUT;
    }

    public String doDetailsValidation() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        if (!setWorkflowTransitionSelection())
        {
            addErrorMessage(getText("bulkworkflowtransition.select.transition.error"));
            return ERROR;
        }

        initBeanWithSelection();

        return SUCCESS;
    }

    private void initBeanWithSelection() throws Exception
    {
        final MultiMap workflowTransitionMap = getBulkEditBean().getWorkflowTransitionMap();
        final Collection issues = new ArrayList();

        final WorkflowTransitionKey workflowTransitionKey = getBulkEditBean().getSelectedWFTransitionKey();
        final Collection issueKeys = (Collection) workflowTransitionMap.get(workflowTransitionKey);
        final ActionDescriptor actionDescriptor =
                getBulkWorkflowTransitionOperation().getActionDescriptor(workflowTransitionKey);

        for (final Object issueKey1 : issueKeys)
        {
            final String issueKey = (String) issueKey1;
            final Issue issue = issueManager.getIssueObject(issueKey);
            issues.add(issue);
        }

        getBulkEditBean().initSelectedIssues(issues);

        final FieldScreenRenderer fieldScreenRenderer =
                fieldScreenRendererFactory.getFieldScreenRenderer(issues, actionDescriptor);

        getBulkEditBean().setFieldScreenRenderer(fieldScreenRenderer);
    }

    public String doEditValidation()
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        validateInput();

        if (invalidInput())
            return ERROR;
        else
        {
            updateBean();
            return SUCCESS;
        }
    }

    public String doPerform() throws Exception
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        validationPerform();

        if (invalidInput())
        {
            return ERROR;
        }

        final String taskName = getText("bulk.operation.progress.taskname.transit",
                getRootBulkEditBean().getSelectedIssuesIncludingSubTasks().size());
        return submitBulkOperationTask(getBulkEditBean(), getBulkWorkflowTransitionOperation(), taskName);
    }

    /**
     * Determine if the bulk workflow transition action can be completed
     */
    private void validationPerform()
    {
        // Ensure the user has the global BULK CHANGE permission
        if (!permissionManager.hasPermission(Permissions.BULK_CHANGE, getLoggedInApplicationUser()))
        {
            addErrorMessage(
                    getText("bulk.change.no.permission", String.valueOf(getBulkEditBean().getSelectedIssues().size())));
        }
        else if (!getBulkWorkflowTransitionOperation()
                .canPerformOnAnyIssue(getBulkEditBean(), getLoggedInApplicationUser()))
        {
            addErrorMessage(getText("bulk.workflowtransition.cannotperform", "", "",
                    String.valueOf(getBulkEditBean().getSelectedIssues().size())));
        }
    }

    // Retrive edit actions appearing on the specified field screen tab
    public Collection getEditActions(final String fieldScreenTabName)
    {
        if (editActionsMap == null)
        {
            editActionsMap = new HashMap();
        }
        else if (editActionsMap.containsKey(fieldScreenTabName))
        {
            return (Collection) editActionsMap.get(fieldScreenTabName);
        }

        final Collection editActions = new ArrayList();

        final Collection<FieldScreenRenderTab> fieldScreenRenderTabs =
                getBulkEditBean().getFieldScreenRenderer().getFieldScreenRenderTabs();

        for (final FieldScreenRenderTab screenRenderTab : fieldScreenRenderTabs)
        {
            if (screenRenderTab.getName().equals(fieldScreenTabName))
            {
                final Collection<FieldScreenRenderLayoutItem> bulkFieldScreenRenderLayoutItems =
                        screenRenderTab.getFieldScreenRenderLayoutItems();

                for (final FieldScreenRenderLayoutItem bulkFieldScreenRenderLayoutItem : bulkFieldScreenRenderLayoutItems)
                {
                    final String actionName = bulkFieldScreenRenderLayoutItem.getFieldScreenLayoutItem().getFieldId();
                    editActions.add(buildBulkEditAction(actionName));
                }
                break;
            }
        }

        editActionsMap.put(fieldScreenTabName, editActions);
        return editActions;
    }

    public BulkEditAction getCommentBulkEditAction()
    {
        if (commentBulkEditAction == null)
            commentBulkEditAction =
                    new BulkEditActionImpl(IssueFieldConstants.COMMENT, fieldManager, authenticationContext);

        return commentBulkEditAction;
    }

    public String getCommentHtml()
    {
        final OrderableField commentField = fieldManager.getOrderableField(IssueFieldConstants.COMMENT);
        boolean required = false;
        boolean hidden = false;
        String rendererType = null;

        for (final FieldLayout fieldLayout : getBulkEditBean().getFieldLayouts())
        {
            final FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(commentField);
            if (fieldLayoutItem.isHidden())
            {
                hidden = true;
            }

            if (fieldLayoutItem.isRequired())
            {
                required = true;
            }

            // If the field is using different renderers then it should not be available for
            // editing. So just record a renderer here
            rendererType = fieldLayoutItem.getRendererType();

            // If both are true then no need to look further
            if (hidden && required)
            {
                break;
            }
        }

        final FieldLayoutItemImpl fieldLayoutItem = new FieldLayoutItemImpl.Builder()
                .setOrderableField(commentField)
                .setFieldDescription(null)
                .setHidden(hidden)
                .setRequired(required)
                .setRendererType(rendererType)
                .build();

        return commentField.getEditHtml(fieldLayoutItem, getBulkEditBean(), this, null,
                EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.TRUE));
    }


    public Map getAllEditActions()
    {
        final Collection<FieldScreenRenderTab> fieldScreenRenderTabs =
                getBulkEditBean().getFieldScreenRenderer().getFieldScreenRenderTabs();

        final Map editActions = new HashMap();

        for (final FieldScreenRenderTab screenRenderTab : fieldScreenRenderTabs)
        {
            final Collection<FieldScreenRenderLayoutItem> bulkFieldScreenRenderLayoutItems =
                    screenRenderTab.getFieldScreenRenderLayoutItems();

            for (final FieldScreenRenderLayoutItem bulkFieldScreenRenderLayoutItem : bulkFieldScreenRenderLayoutItems)
            {
                final String actionName = bulkFieldScreenRenderLayoutItem.getFieldScreenLayoutItem().getFieldId();
                editActions.put(actionName, buildBulkEditAction(actionName));
            }
        }

        return editActions;

    }

    // Build a BulkEditAction for specified field
    private BulkEditAction buildBulkEditAction(final String fieldId)
    {
        return new BulkEditActionImpl(fieldId, fieldManager, authenticationContext);
    }

    // Validate the edit actions selected
    private void validateInput()
    {
        selectedActions = new ListOrderedMap();

        if (getActions() != null && getActions().length != 0)
        {
            final Map allEditActions = getAllEditActions();

            for (int i = 0; i < getActions().length; i++)
            {
                final String fieldId = getActions()[i];
                final BulkEditAction bulkEditAction = (BulkEditAction) allEditActions.get(fieldId);
                selectedActions.put(bulkEditAction.getField().getId(), bulkEditAction);
                bulkEditAction.getField()
                        .populateFromParams(getBulkEditBean().getFieldValuesHolder(), ActionContext.getParameters());
                for (final Issue issue : getBulkEditBean().getSelectedIssues())
                {
                    bulkEditAction.getField().validateParams(getBulkEditBean(), this, this, issue,
                            buildFieldScreenRenderLayoutItem(bulkEditAction.getField(), issue.getGenericValue()));
                }
            }
        }

        // Validate Comment
        if (TextUtils.stringSet(getCommentaction()))
        {
            final BulkEditAction bulkEditAction = getCommentBulkEditAction();
            if (bulkEditAction.isAvailable(getBulkEditBean()))
            {
                selectedActions.put(bulkEditAction.getField().getId(), bulkEditAction);
                bulkEditAction.getField()
                        .populateFromParams(getBulkEditBean().getFieldValuesHolder(), ActionContext.getParameters());
                for (final Issue issue : getBulkEditBean().getSelectedIssues())
                {
                    bulkEditAction.getField().validateParams(getBulkEditBean(), this, this, issue,
                            buildFieldScreenRenderLayoutItem(bulkEditAction.getField(), issue.getGenericValue()));
                }
            }
            else
            {
                addErrorMessage(getText("bulkworkflowtransition.comment.cannotspecify"));
            }
        }
    }

    // Check if there are available transitions on the selected issues
    @SuppressWarnings("UnusedDeclaration") // used in .jsp files
    public boolean isHasAvailableActions() throws Exception
    {
        // Check if we can perform operation on at least one issue - just because someone might just edit
        // issue and we don't wanna to fail everything (we will ignore issues that cannot be transitioned later)
        return getBulkWorkflowTransitionOperation()
                .canPerformOnAnyIssue(getBulkEditBean(), getLoggedInApplicationUser());
    }

    private void updateBean()
    {
        if (selectedActions != null && !selectedActions.isEmpty())
        {
            // set values in bean once form data has been validated
            getBulkEditBean().setActions(selectedActions);
            try
            {
                for (final BulkEditAction bulkEditAction : getBulkEditBean().getActions().values())
                {
                    final OrderableField field = bulkEditAction.getField();
                    final Object value = field.getValueFromParams(getBulkEditBean().getFieldValuesHolder());
                    getBulkEditBean().getFieldValues().put(field.getId(), value);
                }
            }
            catch (FieldValidationException e)
            {
                log.error("Error getting field value.", e);
                throw new NestedRuntimeException("Error getting field value.", e);
            }
        }

        getBulkEditBean().clearAvailablePreviousSteps();
        getBulkEditBean().addAvailablePreviousStep(1);
        getBulkEditBean().addAvailablePreviousStep(2);
        getBulkEditBean().addAvailablePreviousStep(3);
        getBulkEditBean().setCurrentStep(4);
    }

    public String getOperationDetailsActionName()
    {
        return getBulkWorkflowTransitionOperation().getOperationName() + "Details.jspa";
    }

    // Sets checkbox value if field validation error occurred.
    public boolean isChecked(final String value)
    {
        if (getActions() == null || getActions().length == 0)
        {
            // If there were no actions submitted we are either being invoked with no check boxes checked
            // (which should be OK, as there is nothing to validate), or we are coming from the later stage of
            // the wizard. In this case we should look into BulkEditBean
            if (getBulkEditBean().getActions() != null)
            {
                return getBulkEditBean().getActions().containsKey(value);
            }

            return false;
        }
        else
        {
            if (IssueFieldConstants.COMMENT.equals(value))
            {
                return TextUtils.stringSet(getCommentaction());
            }

            // If we have check boxes (actions) submitted use them
            for (int i = 0; i < getActions().length; i++)
            {
                final String action = getActions()[i];
                if (action.equals(value))
                    return true;
            }

            return false;
        }
    }

    /**
     * Initialise the 'workflow to transition' multimap.
     * <p/>
     * Multimap:
     * Key = WorkflowTransitionKey
     * Value = Collection of Issue Keys
     *
     * @throws WorkflowException
     */
    private void setWorkflowTransitionMap() throws WorkflowException
    {
        if (getBulkEditBean().getWorkflowTransitionMap() == null ||
                getBulkEditBean().getWorkflowTransitionMap().isEmpty())
        {
            final Collection<Issue> selectedIssues = getBulkEditBean().getSelectedIssues();
            final MultiMap workflowTransitionMap = new MultiHashMap();

            for (final Issue issue : selectedIssues)
            {
                final JiraWorkflow workflow = workflowManager.getWorkflow(issue.getGenericValue());
                final String workflowName = workflow.getName();

                final Collection<ActionDescriptor> availableActions =
                        issueWorkflowManager.getAvailableActions(issue, getLoggedInApplicationUser());

                for (final ActionDescriptor actionDescriptor : availableActions)
                {
                    final String actionDescriptorId = String.valueOf(actionDescriptor.getId());

                    final int resultStep = actionDescriptor.getUnconditionalResult().getStep();
                    final WorkflowTransitionKey workflowTransitionKey;
                    if (resultStep == JiraWorkflow.ACTION_ORIGIN_STEP_ID)
                    {
                        //JRA-12017: if we have workflowtransition that goes back to itself, we use the original status in for the
                        //destination status.  This means that if two issues have different statuses currently, they will
                        //show up as two separate lines in the transitions screeen.  This is correct, as permissions may
                        //be different dependening on the destination status, which determines what may be shown on
                        //the fields screen on the next page in the wizard.
                        workflowTransitionKey = new WorkflowTransitionKey(workflowName, actionDescriptorId,
                                issue.getStatusObject().getId());
                    }
                    else
                    {
                        final Status resultStatus =
                                workflow.getLinkedStatusObject(workflow.getDescriptor().getStep(resultStep));
                        workflowTransitionKey = new WorkflowTransitionKey(workflowName, actionDescriptorId,
                                resultStatus.getId());
                    }

                    workflowTransitionMap.put(workflowTransitionKey, issue.getKey());
                }

            }

            getBulkEditBean().setWorkflowTransitionMap(workflowTransitionMap);
        }
    }

    // Set the selected transition for this bulk workflow transition operation
    private boolean setWorkflowTransitionSelection()
    {
        // Reset the selection as new selection is being made
        getBulkEditBean().resetWorkflowTransitionSelection();
        boolean selectionMade = false;

        final Map params = ActionContext.getParameters();
        if (params != null && !params.isEmpty())
        {
            final Set keys = params.keySet();

            for (final Object key1 : keys)
            {
                final String key = (String) key1;
                if (key.equals(WORKFLOW_TRANSITION))
                {
                    final String[] actionId = (String[]) params.get(key);
                    final String code = actionId[0];
                    final WorkflowTransitionKey wtkey = decodeWorkflowTransitionKey(code);
                    getBulkEditBean().setSelectedWFTransitionKey(wtkey);
                    selectionMade = true;
                }
            }
        }
        return selectionMade;
    }

    /**
     * Decodes a string into its WorkflowTransitionKey.
     *
     * @param encoded a string-encoded WorkflowTransitionKey literal.
     * @return the WorkflowTransitionKey that for the given encoded string
     */
    public WorkflowTransitionKey decodeWorkflowTransitionKey(final String encoded)
    {
        // last two indexes of _ will be our origin status and actiondescriptor Id.  Anything before that is the
        // workflowname.
        final int i = encoded.lastIndexOf('_');
        final String destinationStatus = encoded.substring(i + 1, encoded.length());
        final String rest = encoded.substring(0, i);
        final int secondDividerIndex = rest.lastIndexOf('_');
        final String actionId = rest.substring(secondDividerIndex + 1, rest.length());
        final String workflowName = rest.substring(0, secondDividerIndex);

        return new WorkflowTransitionKey(workflowName, actionId, destinationStatus);
    }

    public String encodeWorkflowTransitionKey(final WorkflowTransitionKey workflowTransitionKey)
    {
        return workflowTransitionKey.getWorkflowName() + "_" +
                workflowTransitionKey.getActionDescriptorId() + "_" +
                workflowTransitionKey.getDestinationStatus();
    }

    /**
     * @deprecated Use {@link #getOriginStatusObject(WorkflowTransitionKey)} instead. Since v5.0.
     */
    @Deprecated
    public GenericValue getOriginStatus(final WorkflowTransitionKey workflowTransitionKey)
    {
        final Collection issueKeys =
                (Collection) getBulkEditBean().getWorkflowTransitionMap().get(workflowTransitionKey);

        final String issueKey = (String) issueKeys.iterator().next();
        final Issue issue = issueManager.getIssueObject(issueKey);
        //noinspection deprecation
        return issue.getStatus();
    }

    public Status getOriginStatusObject(final WorkflowTransitionKey workflowTransitionKey)
    {
        final GenericValue originStatusGV = getOriginStatus(workflowTransitionKey);

        return constantsManager.getStatusObject(originStatusGV != null ? originStatusGV.getString("id") : null);
    }

    /**
     * @deprecated Use {@link #getDestinationStatusObject(WorkflowTransitionKey)} instead. Since v5.0.
     */
    @Deprecated
    public GenericValue getDestinationStatus(final WorkflowTransitionKey workflowTransitionKey)
    {
        final ActionDescriptor actionDescriptor =
                getBulkWorkflowTransitionOperation().getActionDescriptor(workflowTransitionKey);
        final JiraWorkflow workflow = workflowManager.getWorkflow(workflowTransitionKey.getWorkflowName());
        final int newStepId = actionDescriptor.getUnconditionalResult().getStep();
        if (newStepId == JiraWorkflow.ACTION_ORIGIN_STEP_ID)
        {
            return getOriginStatus(workflowTransitionKey);
        }
        else
        {
            final StepDescriptor step = workflow.getDescriptor().getStep(newStepId);
            return workflow.getLinkedStatus(step);
        }
    }

    public Status getDestinationStatusObject(final WorkflowTransitionKey workflowTransitionKey)
    {
        final GenericValue destinationStatusGV = getDestinationStatus(workflowTransitionKey);

        return constantsManager
                .getStatusObject(destinationStatusGV != null ? destinationStatusGV.getString("id") : null);
    }

    // Returns a short list of the issue collection
    public List getShortListTransitionIssueKeys(final Collection issueKeys)
    {
        int count = 0;
        final List shortList = new ArrayList();

        for (final Object issueKey1 : issueKeys)
        {
            final String issueKey = (String) issueKey1;
            shortList.add(issueKey);
            count++;

            if (count >= 5)
            {
                break;
            }
        }

        return shortList;
    }

    public String[] getActions()
    {
        final Map params = ActionContext.getParameters();

        // Conflicting names in func tests forces the resolution to be added to the actions as follows
        if (params != null && params.containsKey(FORCED_RESOLUTION))
        {
            final String[] strings = (String[]) params.get(FORCED_RESOLUTION);
            if (actions == null)
            {
                actions = new String[1];
                actions[0] = strings[0];
                return actions;
            }
            else
            {
                final String[] newActions = new String[actions.length + 1];
                for (int i = 0; i < actions.length; i++)
                {
                    newActions[i] = actions[i];
                }
                newActions[actions.length] = strings[0];
                return newActions;
            }
        }

        return actions;
    }

    public void setActions(final String[] actions)
    {
        this.actions = actions;
    }

    // Used to force the selection of a resolution if one is detected on a screen.
    // Avoids the scenario where an issue is transitioned to a 'Resolved' status without
    // setting the 'resolution'.
    public boolean isForceResolution(final Field field)
    {
        return IssueFieldConstants.RESOLUTION.equals(field.getId());
    }

    public String getCommentaction()
    {
        return commentaction;
    }

    public void setCommentaction(final String commentaction)
    {
        this.commentaction = commentaction;
    }

    public String getFieldViewHtml(final OrderableField orderableField)
    {
        // There is a validation that will not allow an edit to occur on a field that has different renderer types
        // defined in the field layout item so if we get here then we know it is safe to grab the first layout
        // item we can find for the field and that this will imply the correct renderer type.
        FieldLayoutItem layoutItem = null;
        if (!getBulkEditBean().getFieldLayouts().isEmpty())
        {
            layoutItem = ((FieldLayout) getBulkEditBean().getFieldLayouts().iterator().next())
                    .getFieldLayoutItem(orderableField);
        }

        // Let the fields know they are being shown as part of the Preview for Bulk Transition. For example, the comment
        // field needs to display the user group its been retsricted to, not just the comment text

        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("readonly", Boolean.TRUE)
                .add("nolink", Boolean.TRUE)
                .add("bulkoperation", getBulkEditBean().getOperationName())
                .add("prefix", "new_").toMutableMap();

        return orderableField
                .getViewHtml(layoutItem, this, (Issue) getBulkEditBean().getSelectedIssues().iterator().next(),
                        getBulkEditBean().getFieldValues().get(orderableField.getId()), displayParams);
    }

    public String getFieldHtml(final OrderableField orderableField) throws Exception
    {
        return orderableField.getBulkEditHtml(getBulkEditBean(), this, getBulkEditBean(),
                EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.TRUE));
    }

    public Collection getFieldScreenRenderTabs()
    {
        return getBulkEditBean().getFieldScreenRenderer().getFieldScreenRenderTabs();
    }

    protected FieldScreenRenderLayoutItem buildFieldScreenRenderLayoutItem(final OrderableField field,
                                                                           final GenericValue issue)
    {
        return new FieldScreenRenderLayoutItemImpl(null,
                fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(field));
    }

    public BulkWorkflowTransitionOperation getBulkWorkflowTransitionOperation()
    {
        return bulkWorkflowTransitionOperation;
    }

    private void initTabsWithErrors()
    {
        tabsWithErrors = new TreeSet<FieldScreenRenderTab>();
        selectedTab = new ScreenTabErrorHelper()
                .initialiseTabsWithErrors(tabsWithErrors, getErrors(), getBulkEditBean().getFieldScreenRenderer(),
                        ActionContext.getParameters());
    }

    public Collection getTabsWithErrors()
    {
        if (tabsWithErrors == null)
        {
            initTabsWithErrors();
        }

        return tabsWithErrors;
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

    public String removeSpaces(final String string)
    {
        return StringUtils.deleteWhitespace(string);
    }
}
