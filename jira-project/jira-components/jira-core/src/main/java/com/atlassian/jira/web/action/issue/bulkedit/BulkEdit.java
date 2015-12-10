/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.operation.BulkEditAction;
import com.atlassian.jira.bulkedit.operation.BulkEditOperation;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;
import org.apache.commons.jelly.util.NestedRuntimeException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BulkEdit extends AbstractBulkOperationDetailsAction
{
    public static final String RADIO_ERROR_MSG = "buik.edit.must.select.one.action.to.perform";
    public static final String SHOW_BULK_EDIT_WARNING = "showBulkEditWarning";
    private BulkEditOperation bulkEditOperation;

    // actions array is retrieved from the checkbox group in the JSP called "actions"
    // this stores the fields that the user has indicated an intention to bulk edit (ie. my checking it)
    private String[] actions;
    private Map editActions;
    private Map selectedActions;
    ArrayList visibleActions;
    ArrayList hiddenActions;
    private List customFields;

    private final FieldManager fieldManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final IssueFactory issueFactory;
    private final PermissionManager permissionManager;

    public BulkEdit(SearchService searchService, BulkOperationManager bulkOperationManager, FieldManager fieldManager,
                    IssueFactory issueFactory, PermissionManager permissionManager,
                    final FieldLayoutManager fieldLayoutManager,
                    final BulkEditBeanSessionHelper bulkEditBeanSessionHelper, final TaskManager taskManager,
                    final I18nHelper i18nHelper)
    {
        super(searchService, bulkEditBeanSessionHelper, taskManager, i18nHelper);
        this.fieldManager = fieldManager;
        this.bulkEditOperation = (BulkEditOperation) bulkOperationManager.getProgressAwareOperation(
                BulkEditOperation.NAME_KEY);
        this.issueFactory = issueFactory;
        this.permissionManager = permissionManager;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    public String getFieldHtml(String fieldId) throws Exception
    {
        OrderableField orderableField = fieldManager.getOrderableField(fieldId);
        final Map displayParameters =
                EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.TRUE, SHOW_BULK_EDIT_WARNING, Boolean.TRUE);
        return orderableField.getBulkEditHtml(getBulkEditBean(), this, getBulkEditBean(), displayParameters);
    }

    public String getFieldViewHtml(OrderableField orderableField)
    {
        // There is a validation that will not allow an edit to occur on a field that has different renderer types
        // defined in the field layout item so if we get here then we know it is safe to grab the first layout
        // item we can find for the field and that this will imply the correct renderer type.
        FieldLayoutItem layoutItem = null;
        if (!getBulkEditBean().getFieldLayouts().isEmpty())
        {
            layoutItem = getBulkEditBean().getFieldLayouts().iterator().next().getFieldLayoutItem(orderableField);
        }

        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("readonly", Boolean.TRUE)
                .add("nolink", Boolean.TRUE)
                .add("bulkoperation", getBulkEditBean().getOperationName())
                .add("prefix", "new_").toMutableMap();

        return orderableField.getViewHtml(layoutItem, this, getBulkEditBean().getSelectedIssues().iterator().next(),
                getBulkEditBean().getFieldValues().get(orderableField.getId()), displayParams);
    }

    protected Issue getIssueObject(GenericValue issueGV)
    {
        return issueFactory.getIssue(issueGV);
    }

    protected FieldScreenRenderLayoutItem buildFieldScreenRenderLayoutItem(final OrderableField field,
                                                                           GenericValue issue)
    {
        return new FieldScreenRenderLayoutItemImpl(null,
                fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(field));
    }

    public String doDetails()
    {
        BulkEditBean bulkEditBean = getBulkEditBean();
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)
        if (bulkEditBean == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        setFieldDefaults();
        bulkEditBean.clearAvailablePreviousSteps();
        bulkEditBean.addAvailablePreviousStep(1);
        bulkEditBean.addAvailablePreviousStep(2);

        // Ensure that bulk notification can be disabled
        if (isCanDisableMailNotifications())
            bulkEditBean.setSendBulkNotification(false);
        else
            bulkEditBean.setSendBulkNotification(true);

        setCurrentStep(3);
        return INPUT;
    }

    private void setFieldDefaults()
    {
        for (final Object o : getEditActions().values())
        {
            BulkEditAction bulkEditAction = (BulkEditAction) o;
            if (bulkEditAction.isAvailable(getBulkEditBean()))
            {
                // TODO Might have to create another method - populateForBulkEdit
                bulkEditAction.getField()
                        .populateDefaults(getBulkEditBean().getFieldValuesHolder(), getIssueObject(null));
            }
        }
    }

    @RequiresXsrfCheck
    public String doDetailsValidation() throws Exception
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
            updateBean();

        return getResult();
    }

    @RequiresXsrfCheck
    public String doPerform() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }
        doValidationPerform();
        if (invalidInput())
        {
            return ERROR;
        }

        final String taskName = getText("bulk.operation.progress.taskname.edit",
                getRootBulkEditBean().getSelectedIssuesIncludingSubTasks().size());
        return submitBulkOperationTask(getBulkEditBean(), getBulkEditOperation(), taskName);
    }

    private void doValidationPerform()
    {
        try
        {
            // Ensure the user has the global BULK CHANGE permission
            if (!permissionManager.hasPermission(Permissions.BULK_CHANGE, getLoggedInUser()))
            {
                addErrorMessage(getText("bulk.change.no.permission",
                        String.valueOf(getBulkEditBean().getSelectedIssues().size())));
                return;
            }

            // Ensure the user can perform the operation
            if (!getBulkEditOperation().canPerform(getBulkEditBean(), getLoggedInApplicationUser()))
            {
                addErrorMessage(getText("bulk.edit.cannotperform.error",
                        String.valueOf(getBulkEditBean().getSelectedIssues().size())));
                return;
            }
        }
        catch (Exception e)
        {
            log.error("Error occurred while testing operation.", e);
            addErrorMessage(getText("bulk.canperform.error"));
            return;
        }

        try
        {

            for (BulkEditAction bulkEditAction : getBulkEditBean().getActions().values())
            {
                if (!bulkEditAction.isAvailable(getBulkEditBean()))
                {
                    addErrorMessage(getText("bulk.edit.perform.invalid.action",
                            String.valueOf(getBulkEditBean().getSelectedIssues().size())));
                }
            }
        }
        catch (Exception e)
        {
            log.error("Error occurred validating available update operations.", e);
            addErrorMessage(getText("bulk.canperform.error"));
        }
    }

    public String doDefault() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }
        // set BulkEditBean to use the issues that have now been selected rather than the issues from the search request
        getBulkEditBean().setIssuesInUse(getBulkEditBean().getSelectedIssues());
        return super.doDefault();
    }

    private void validateInput()
    {
        if (getActions() == null || getActions().length == 0)
        {
            addErrorMessage(getText(RADIO_ERROR_MSG));
            return;
        }

        selectedActions = new LinkedHashMap();
        for (int i = 0; i < getActions().length; i++)
        {
            String fieldId = getActions()[i];
            BulkEditAction bulkEditAction = (BulkEditAction) getEditActions().get(fieldId);
            selectedActions.put(bulkEditAction.getField().getId(), bulkEditAction);
            // Validate the field for all issues
            bulkEditAction.getField()
                    .populateFromParams(getBulkEditBean().getFieldValuesHolder(), ActionContext.getParameters());
            for (Issue issue : getBulkEditBean().getSelectedIssues())
            {
                bulkEditAction.getField().validateParams(getBulkEditBean(), this, this, issue,
                        buildFieldScreenRenderLayoutItem(bulkEditAction.getField(), issue.getGenericValue()));
            }
        }
    }

    public boolean isHasAvailableActions() throws Exception
    {
        return getBulkEditOperation().canPerform(getBulkEditBean(), getLoggedInApplicationUser());
    }

    private void updateBean()
    {
        // set values in bean once form data has been validated
        getBulkEditBean().setActions(selectedActions);
        try
        {
            for (BulkEditAction bulkEditAction : getBulkEditBean().getActions().values())
            {
                OrderableField field = bulkEditAction.getField();
                Object value = field.getValueFromParams(getBulkEditBean().getFieldValuesHolder());
                getBulkEditBean().getFieldValues().put(field.getId(), value);
            }
        }
        catch (FieldValidationException e)
        {
            log.error("Error getting field value.", e);
            throw new NestedRuntimeException("Error getting field value.", e);
        }

        getBulkEditBean().clearAvailablePreviousSteps();
        getBulkEditBean().addAvailablePreviousStep(1);
        getBulkEditBean().addAvailablePreviousStep(2);
        getBulkEditBean().addAvailablePreviousStep(3);
        setCurrentStep(4);
    }

    /**
     * Returns a list of bulk actions
     * If search request was performed on "All Projects" (ie. multiple projects) certain actions such as fixfor will not
     * be displayed, as fixfor versions obviously differ across projects.
     * <p/>
     * If no issues have been selected then no actions should be shown
     */
    public Map getEditActions()
    {
        if (editActions == null)
        {
            editActions = getBulkEditOperation().getActions(getBulkEditBean(), getLoggedInApplicationUser());
        }
        return editActions;
    }

    /**
     * Returns a list of bulk actions which are visible/available
     */
    public Collection getVisibleActions()
    {
        if (visibleActions == null)
        {
            seperateVisibleAndHiddenActions();
        }
        return visibleActions;
    }

    /**
     * Returns a list of bulk actions which are hidden/unavailable
     */
    public Collection getHiddenActions()
    {
        if (hiddenActions == null)
        {
            seperateVisibleAndHiddenActions();
        }
        return hiddenActions;
    }

    /**
     * Initialises the visibleActions and hiddenActions collection
     */
    private void seperateVisibleAndHiddenActions()
    {
        visibleActions = new ArrayList();
        hiddenActions = new ArrayList();
        Iterator actions = getEditActions().values().iterator();
        while (actions.hasNext())
        {
            BulkEditAction action = (BulkEditAction) actions.next();
            if (action.isAvailable(getBulkEditBean()))
            {
                visibleActions.add(action);
            }
            else
            {
                hiddenActions.add(action);
            }
        }
    }

    public boolean isAvailable(String action) throws Exception
    {
        return getEditActions().containsKey(action);
    }

    public Collection getCustomFields()
    {
        if (customFields == null)
        {
//            customFields = getBulkEditOperation().getCustomFields(getBulkEditBean(), getLoggedInUser());
            customFields = new LinkedList();
        }

        return customFields;
    }

    // Check if the array of the selected actions is null and if it contains any elements
    // The user may select an action but not select an argument for the action.
    // Also check if the argument is null.
    public boolean isHasFirstElement(List actions)
    {
        if (actions != null && !actions.isEmpty() && (actions.get(0) != null))
        {
            // Action selected with a corresponding argument
            return true;
        }
        return false;
    }

    public void setCurrentStep(int step)
    {
        getBulkEditBean().setCurrentStep(step);
    }

    public String[] getActions()
    {
        return actions;
    }

    public void setActions(String[] actions)
    {
        this.actions = actions;
    }

    private BulkEditOperation getBulkEditOperation()
    {
        return bulkEditOperation;
    }

    public String getOperationDetailsActionName()
    {
        return getBulkEditOperation().getOperationName() + "Details.jspa";
    }

    public boolean isChecked(String value)
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
            // If we have check boxes (actions) submitted use them
            for (int i = 0; i < getActions().length; i++)
            {
                String action = getActions()[i];
                if (action.equals(value))
                    return true;
            }

            return false;
        }
    }
}
