/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.security.IssueSecurityHelper;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.WorkflowManager;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MoveIssueUpdateFields extends MoveIssue implements OperationContext
{
    // Required to record actual changes made - such as deselecting a component - is stored in bean on validation
    private MutableIssue targetIssue;
    private final IssueSecurityHelper issueSecurityHelper;

    public MoveIssueUpdateFields(SubTaskManager subTaskManager, ConstantsManager constantsManager,
                                 WorkflowManager workflowManager, FieldManager fieldManager,
                                 FieldLayoutManager fieldLayoutManager, IssueFactory issueFactory,
                                 FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService,
                                 IssueSecurityHelper issueSecurityHelper, UserUtil userUtil)
    {
        super(subTaskManager, constantsManager, workflowManager, fieldManager, fieldLayoutManager,
                issueFactory, fieldScreenRendererFactory, commentService, userUtil);
        this.issueSecurityHelper = issueSecurityHelper;
    }

    public String doDefault()
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        Issue originalIssue = getIssueObject(getIssue());
        Issue targetIssue = getTargetIssueObject();

        // Only initialise field values if we are coming to this step in the wizard for the first time. Otherwise show selected values of the fields.
        if (!(ActionContext.getSingleValueParameters().containsKey("reset") && ("true".equals(ActionContext.getSingleValueParameters().get("reset")))))
        {
            Collection moveFieldLayoutItems = new LinkedList();
            // Loop over all the visible fields and see which ones require to be moved
            FieldLayout targetFieldLayout = getTargetFieldLayout();
            for (FieldLayoutItem fieldLayoutItem : targetFieldLayout.getVisibleLayoutItems(getTargetProjectObj(), EasyList.build(getTargetIssueType())))
            {
                OrderableField orderableField = fieldLayoutItem.getOrderableField();
                // Issue type is shown on the first stage of the move so no need to work with it here
                if (!IssueFieldConstants.ISSUE_TYPE.equals(orderableField.getId()))
                {
                    if (orderableField.needsMove(EasyList.build(originalIssue), targetIssue, fieldLayoutItem).getResult())
                    {
                        if (orderableField.isShown(targetIssue))
                        {
                            // As the issue can be shown then need to initialise it with a value
                            orderableField.populateForMove(getMoveIssueBean().getFieldValuesHolder(), originalIssue, targetIssue);
                        }
                        // Record that the field needs to be edited for the move
                        moveFieldLayoutItems.add(fieldLayoutItem);
                    }
                }
            }
            getMoveIssueBean().setMoveFieldLayoutItems(moveFieldLayoutItems);
        }

        validateAttachmentMove();

        // SubTask move does not have status selection step
        if (isSubTask())
        {
            getMoveIssueBean().setCurrentStep(2);
            getMoveIssueBean().addAvailablePreviousStep(3);
        }
        else
        {
            getMoveIssueBean().setCurrentStep(3);
            getMoveIssueBean().addAvailablePreviousStep(2);
        }

        return INPUT;
    }

    public Collection getMoveFieldLayoutItems()
    {
        return getMoveIssueBean().getMoveFieldLayoutItems();
    }

    /**
     * Retrieves the issue from the IssueManager and updates the Project and IssueType, and possibly clears the security.
     * <p>
     * JRA-11605 - we need this method so that the display can prompt the user with values from system fields that are
     * unencumbered by security level permission checks. It will set security level to null if the issue is moving
     * somewhere that the old security level is no relevant.
     * </p>
     * @return Target Issue Object with required Project and Issue Type added in.
     * @see #getTargetIssueObjectWithSecurityLevel()
     */
    protected MutableIssue getTargetIssueObject()
    {
        if (targetIssue == null)
        {
            targetIssue = getTargetIssueObjectWithSecurityLevel();
            if(issueSecurityHelper.securityLevelNeedsMove(getIssueObject(), targetIssue))
            {
                targetIssue.setSecurityLevel(null);
            }
        }
        return targetIssue;
    }

    /**
     * Retrieves the issue from the IssueManager and updates the Project and IssueType.
     * The security level setting is left in place - see <code>getTargetIssueObject()</code> for an alternative method.
     * @return Issue Object with required Project and Issue Type added in.
     * @see #getTargetIssueObject()
     */
    protected MutableIssue getTargetIssueObjectWithSecurityLevel()
    {
        MutableIssue targetIssue;
        // This will go off to the database.  This is only called twice (once in the getTargetIssue() method
        // and once in the doExecute() method.
        targetIssue = getIssueManager().getIssueObject(getId());
        targetIssue.setProjectObject(getTargetProjectObj());
        targetIssue.setIssueTypeObject(getTargetIssueTypeObject());

        return targetIssue;
    }

    protected FieldLayout getTargetFieldLayout()
    {
        return getFieldLayoutManager().getFieldLayout(getTargetProject(), getTargetIssueType());
    }

    public String getFieldHtml(FieldLayoutItem fieldLayoutItem)
    {
        OrderableField orderableField = fieldLayoutItem.getOrderableField();
        MutableIssue targetIssue = getTargetIssueObject();
        if (orderableField.isShown(targetIssue))
        {
            return orderableField.getCreateHtml(fieldLayoutItem, getMoveIssueBean(), this, targetIssue);
        }
        else
        {
            return "";
        }
    }

    public String getFieldName(Field field)
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

    protected void doValidation()
    {
        if (getMoveIssueBean() != null)
        {
            try
            {
                if (!hasIssuePermission(Permissions.MOVE_ISSUE, getIssueObject()))
                {
                    addErrorMessage(getText("moveissue.no.permission"));
                }

                Issue targetIssue = getTargetIssueObject();

                // Loop over all the fields that need to be edited for the move
                for (final Object o : getMoveFieldLayoutItems())
                {
                    FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) o;
                    OrderableField orderableField = fieldLayoutItem.getOrderableField();

                    if (orderableField.isShown(targetIssue))
                    {
                        // As the issue has been shown then initialise it from the action's parameters
                        populateFromParams(orderableField);
                        // Validate the value of the field
                        orderableField.validateParams(getMoveIssueBean(), this, this, targetIssue, new FieldScreenRenderLayoutItemImpl(null, fieldLayoutItem));
                    }
                    else
                    {
                        // If the field has not been shown (but it needs to be moved) populate with the default value
                        popluateDefault(orderableField);

                        // Validate the parameter. In theory as the field places a default value itself the value should be valid, however, a check for
                        // 'requireability' still has to be made.
                        ErrorCollection errorCollection = new SimpleErrorCollection();
                        orderableField.validateParams(getMoveIssueBean(), errorCollection, this, targetIssue, new FieldScreenRenderLayoutItemImpl(null, fieldLayoutItem));
                        if (errorCollection.getErrors() != null && !errorCollection.getErrors().isEmpty())
                        {
                            // The field has reported errors but is not rendered on the screen - report errors as error messages
                            for (final String s : errorCollection.getErrors().values())
                            {
                                addErrorMessage(getFieldName(orderableField) + ": " + s);
                            }
                        }
                        addErrorMessages(errorCollection.getErrorMessages());
                    }
                }
            }
            catch (Exception e)
            {
                addErrorMessage(getText("admin.errors.issues.error.occurred.validating.field", e));
            }
        }
    }

    // This is taken out as a protected method such that it can be overridden, and the doValidation() method reused by subclass actions
    protected void popluateDefault(OrderableField orderableField)
    {
        orderableField.populateDefaults(getMoveIssueBean().getFieldValuesHolder(), getTargetIssueObject());
    }

    // This is taken out as a protected method such that it can be overridden, and the doValidation() method reused by subclass actions
    protected void populateFromParams(OrderableField orderableField)
    {
        orderableField.populateFromParams(getMoveIssueBean().getFieldValuesHolder(), ActionContext.getParameters());
    }

    protected String doExecute() throws Exception
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        // JRA-11605 - we need the real target issue object including the security level so that the changed value
        // of the security level will be correctly recorded by the field. This also has the effect of letting the
        // system field update the subtask issue security level (handled by the field but probably should not be).
        MutableIssue targetIssue = getTargetIssueObjectWithSecurityLevel();

        for (final Object o1 : getMoveFieldLayoutItems())
        {
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) o1;
            OrderableField orderableField = fieldLayoutItem.getOrderableField();
            orderableField.updateIssue(fieldLayoutItem, targetIssue, getMoveIssueBean().getFieldValuesHolder());
        }

        Collection removedFields = new LinkedList();
        // Hidden fields include custom fields that are not in scope
        for (final Object o : getTargetHiddenFields())
        {
            Field field = (Field) o;
            if (field != null && getFieldManager().isOrderableField(field))
            {
                boolean doValueCheck = isShouldCheckFieldValue(getIssueObject(), field);

                OrderableField orderableField = (OrderableField) field;
                // Remove values of all the fields that have a value but are hidden in the target project
                if (doValueCheck && orderableField.hasValue(targetIssue)
                        && orderableField.canRemoveValueFromIssueObject(targetIssue))
                {
                    // JRA-13479 Clear out the value for this field
                    orderableField.removeValueFromIssueObject(targetIssue);
                    // Add the field to our list of removed fields that is displayed on the confirm screen.
                    removedFields.add(orderableField);
                }
            }
        }

        getMoveIssueBean().setRemovedFields(removedFields);
        getMoveIssueBean().setUpdatedIssue(targetIssue);

        return SUCCESS;
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
        List issueTypeList = EasyList.build(origIssue.getIssueTypeObject().getId());
        if (fieldManager.isCustomField(field) && !((CustomField) field).isInScope(origIssue.getProjectObject(), issueTypeList))
        {
            doValueCheck = false;
        }
        return doValueCheck;
    }

    // Also used in move issue confirm jsp to show the user that the values of these fields are being cleared.
    protected Collection getTargetHiddenFields()
    {
        return getTargetFieldLayout().getHiddenFields(getTargetProjectObj(), EasyList.build(getTargetIssueType()));
    }

    // ---- Custom Field Methods ----

    public Map getCustomFieldValuesHolder()
    {
        return getMoveIssueBean().getFieldValuesHolder();
    }

    /**
     * Returns a collection of target custom field objects.
     * @param targetIssueTypeId the id of the target issue type.
     * @return a collection of target custom field objects.
     */
    protected Collection<CustomField> getTargetCustomFieldObjects(String targetIssueTypeId)
    {
        return getCustomFieldManager().getCustomFieldObjects(getTargetPid(), targetIssueTypeId);
    }

    /**
     * Retrieve the custom fields that are applicable in the target destination.
     * @return A collection of the custom fields that are applicable in the target destination.
     */
    public Collection getTargetCustomFields()
    {
        return getTargetCustomFieldObjects(getTargetIssueType());
    }
}
