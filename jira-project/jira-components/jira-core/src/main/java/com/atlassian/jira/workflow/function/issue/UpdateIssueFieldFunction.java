/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.TimeTrackingSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.base.Objects;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Function to update an arbitrary field in a JIRA issue.
 */
public class UpdateIssueFieldFunction extends AbstractJiraFunctionProvider
{
    private static final Logger log = Logger.getLogger(UpdateIssueFieldFunction.class);
    public static final String DEFAULT_VALUE = "-1";
    public static final String UNASSIGNED_VALUE = "";

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        FieldManager fieldManager = ComponentAccessor.getFieldManager();
        ApplicationUser caller = getCallerUser(transientVars, args);

        List changeItems = (List) transientVars.get("changeItems");
        if (changeItems == null)
        {
            changeItems = new LinkedList();
        }

        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        String fieldName = (String) args.get("field.name");
        String fieldValue = (String) args.get("field.value");

        // Keep backwards compatibility (with JIRA 3.1 and earlier) - where 'null' could be specified to indicate a null value
        if (fieldValue != null && "null".equals(fieldValue))
        {
            fieldValue = null;
        }

        if (TextUtils.stringSet((String) args.get("field.type")))
        {
            log.debug("There is no need to specify the field type in this version of JIRA. Remove the 'field.type' argument from the functions arguments.");
        }

        if (IssueFieldConstants.STATUS.equals(fieldName))
        {
            // For backwards compatibility reasons update issue's status.
            log.warn("The use of UpdateIssueFieldFunction to update issue's status is deprecated. Please use UpdateIssueStatusFunction instead.");
            updateStatus(issue, fieldValue, changeItems);
            transientVars.put("changeItems", changeItems);
        }
        else if (IssueFieldConstants.TIME_SPENT.equals(fieldName))
        {
            // This is here for backwards compatibility. Time spent is not an OrderableField but the function was allowed to modify it in previous versions of JIRA.
            // So add code to do that here.
            updateTimeSpent(issue, fieldValue, changeItems, transientVars);
        }
        else if (IssueFieldConstants.TIME_ORIGINAL_ESTIMATE.equals(fieldName) || IssueFieldConstants.TIME_ESTIMATE.equals(fieldName))
        {
            // This is here for backwards compatibility. Both of these issue properties map to the time tracking OrderableField
            try
            {
                // Convert to minutes as in JIRA 3.0 the value was specified in seconds and the default parsing method is in minutes.
                long value = Long.parseLong(fieldValue);
                fieldValue = String.valueOf(value / 60);
            }
            catch (NumberFormatException e)
            {
                // Do nothing - probably means that the value has been specified as a normal time tracking format
                // If the format is wrong it will be detected later on by the fields code
            }

            String targetSubField = IssueFieldConstants.TIME_ORIGINAL_ESTIMATE.equals(fieldName) ? TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE : TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE;

            //
            // in this case we stick in all sorts of parameters so that the the field will do the right thing
            // even if its on old mode or modern mode
            //
            final Map<String, Object> map = MapBuilder.<String, Object>newBuilder()
                    .add(IssueFieldConstants.TIMETRACKING, toArr(fieldValue))
                    .add(TimeTrackingSystemField.TIMETRACKING_TARGETSUBFIELD, toArr(targetSubField))
                    .add(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, toArr(fieldValue))
                    .add(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE, toArr(fieldValue))
                    .toMap();
            processField(issue, IssueFieldConstants.TIMETRACKING, map, caller, changeItems, transientVars);
        }
        else if (IssueFieldConstants.ASSIGNEE.equals(fieldName))
        {
            if (UNASSIGNED_VALUE.equals(fieldValue) || DEFAULT_VALUE.equals(fieldValue))
            {
                processField(issue, fieldName, MapBuilder.build(fieldName, toArr(fieldValue)), caller, changeItems, transientVars);
            }
            else
            {
                UserManager userManager = getUserManager();
                ApplicationUser assignee = userManager.getUserByKeyEvenWhenUnknown(fieldValue);
                if (!userManager.isUserExisting(assignee))
                {
                    throw new WorkflowException("Can not assign issue to nonexistent user: " + fieldValue + ".");
                }
                String translatedFieldValue = assignee.getUsername();
                processField(issue, fieldName, MapBuilder.build(fieldName, toArr(translatedFieldValue)), caller, changeItems, transientVars);
            }
        }
        else if (fieldManager.isOrderableField(fieldName))
        {
            processField(issue, fieldName, MapBuilder.build(fieldName, toArr(fieldValue)), caller, changeItems, transientVars);
        }
        else
        {
            throw new WorkflowException("Cannot update '" + fieldName + "' as it is not an orderable field.");
        }
    }

    private String[] toArr(final String fieldValue)
    {
        return new String[] { fieldValue };
    }

    private void updateTimeSpent(MutableIssue issue, String fieldValue, List changeItems, Map transientVars)
    {
        try
        {
            Long oldTimeSpent = issue.getTimeSpent();
            Long newTimeSpent = new Long(fieldValue);
            if (!Objects.equal(oldTimeSpent, newTimeSpent))
            {
                issue.setTimeSpent(newTimeSpent);
                String from = null;
                String fromString = null;
                String to = null;
                String toString = null;
                if (oldTimeSpent != null)
                {
                    from = fromString = oldTimeSpent.toString();
                }
                if (newTimeSpent != null)
                {
                    to = toString = newTimeSpent.toString();
                }

                changeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.TIME_SPENT, from, fromString, to, toString));
                transientVars.put("changeItems", changeItems);
            }
        }
        catch (NumberFormatException e)
        {
            log.error("Cannot update field '" + IssueFieldConstants.TIME_SPENT + "' to '" + fieldValue + "' as the value must be numeric.");
        }
    }

    private void updateStatus(MutableIssue issue, String fieldValue, List changeItems)
    {
        Status oldStatus = issue.getStatusObject();
        issue.setStatusId(fieldValue);
        Status newStatus = issue.getStatusObject();

        if (!Objects.equal(oldStatus, newStatus))
        {
            if (oldStatus != null)
            {
                changeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.STATUS, oldStatus.getId(), oldStatus.getName(), newStatus.getId(), newStatus.getName()));
            }
            else
            {
                changeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.STATUS, null, null, newStatus.getId(), newStatus.getName()));
            }
        }
    }

    private void processField(MutableIssue issue, String fieldName, Map inputParameters, ApplicationUser user, List changeItems, Map transientVars)
            throws WorkflowException
    {
        FieldManager fieldManager = getFieldManager();
        IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();
        changeHolder.setChangeItems(changeItems);

        OrderableField field = fieldManager.getOrderableField(fieldName);
        Map fieldValuesHolder = new HashMap();
        field.populateFromParams(fieldValuesHolder, inputParameters);
        // In 2005 Nick M said :
        //
        // Temporary solution for JRA-7859 should find perm fix
        //
        // However its now 2010 and we still dont validate field inputs.  I figured you, dear reader,  should know that.
        //
        // In practice this means that the input better be right validation in the field better not have any side effects
        // that field.updateIssue() is relying on.
        //
        // Carry on....
        //
        // ErrorCollection errorCollection = new SimpleErrorCollection();
        // field.validateParams(fieldValuesHolder, errorCollection, new I18nBean(getUser(username)), issue, fieldScreenRenderer.getFieldScreenRenderLayoutItem(field));
        // if (errorCollection.hasAnyErrors())
        // {
        //    throw new WorkflowException("Unable to execute post workflow function. " +
        //            "Issue may be in an inconsistent state. " +
        //            "Please inform your system administrators immediately. Validation of the field '" + fieldName + "' failed: " + errorCollection.getErrorMessages() + " " + errorCollection.getErrors());
        // }

        FieldLayoutItem fieldLayoutItem = null;
        try
        {
            if (issue.getGenericValue() != null)
            {
                fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem(field);
            }
            else
            {
                fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(field);
            }

        }
        catch (DataAccessException e)
        {
            log.error("GenerateChangeHistory is unable to resolve a field layout item for " + field.getName(), e);
        }

        field.updateIssue(fieldLayoutItem, issue, fieldValuesHolder);

        if (issue.getModifiedFields().containsKey(field.getId()))
        {
            // We can ignore the changeHolder.isSubtasksUpdated() flag here because indexing is turned off for now
            // and OSWorkflowManager will re-index subtasks if Security Level of parent has changed.
            field.updateValue(fieldLayoutItem, issue, issue.getModifiedFields().get(field.getId()), changeHolder);
            // Ensure the field is not modified by other workflow functions
            issue.getModifiedFields().remove(field.getId());
        }

        transientVars.put("changeItems", changeHolder.getChangeItems());
    }

    FieldManager getFieldManager()
    {
        return ComponentAccessor.getFieldManager();
    }

    UserKeyService getUserKeyService()
    {
        return ComponentAccessor.getUserKeyService();
    }

    UserManager getUserManager()
    {
        return ComponentAccessor.getUserManager();
    }

    public static FunctionDescriptor makeDescriptor(String fieldName, String fieldValue)
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        descriptor.getArgs().put("class.name", UpdateIssueFieldFunction.class.getName());
        descriptor.getArgs().put("field.name", fieldName);
        descriptor.getArgs().put("field.value", fieldValue);
        return descriptor;
    }
}
