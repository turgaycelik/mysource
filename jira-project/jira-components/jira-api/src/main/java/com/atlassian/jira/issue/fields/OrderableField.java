/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import webwork.action.Action;

import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Interface for fields in JIRA which are able to be placed on "screens" - once they are on the screen they have an "order".
 * More generally, {@link OrderableField}s can be viewed and edited.
 */
@PublicApi
public interface OrderableField extends Field, SearchableField
{
    String TEMPLATE_DIRECTORY_PATH = "templates/jira/issue/field/";
    String MOVE_ISSUE_PARAM_KEY = "moveissue";
    String NO_HEADER_PARAM_KEY = "noHeader";

    /**
     * Returns the HTML that should be shown when the issue is being created.
     *
     * @param fieldLayoutItem  FieldLayoutItem
     * @param operationContext OperationContext
     * @param action           Action
     * @param issue            Issue
     * @return the HTML that should be shown when the issue is being created.
     */
    String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue);

    /**
     * Returns the HTML that should be shown when the issue is being created.
     *
     * @param fieldLayoutItem   FieldLayoutItem
     * @param operationContext  OperationContext
     * @param action            Action
     * @param issue             Issue
     * @param displayParameters Map of display parameters.
     * @return the HTML that should be shown when the issue is being created.
     */
    String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters);

    /**
     * Returns HTML that should be shown when the issue is being edited.
     *
     * @param fieldLayoutItem  FieldLayoutItem
     * @param operationContext OperationContext
     * @param action           Action
     * @param issue            Issue
     * @return HTML that should be shown when the issue is being edited.
     */
    String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue);

    /**
     * Returns HTML that should be shown when the issue is being edited.
     *
     * @param fieldLayoutItem   FieldLayoutItem
     * @param operationContext  OperationContext
     * @param action            Action
     * @param issue             Issue
     * @param displayParameters Map of display parameters.
     * @return HTML that should be shown when the issue is being edited.
     */
    String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters);

    /**
     * Returns HTML that should be shown when the issue is being bulk edited.
     *
     * @param operationContext  OperationContext
     * @param action            Action
     * @param bulkEditBean      BulkEditBean
     * @param displayParameters Map of display parameters.
     * @return HTML that should be shown when the issue is being edited.
     */
    String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters);

    String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue);

    String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters);

    String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters);

    /**
     * Tests field specific way to determine if it should be shown or not.
     *
     * @param issue The Issue.
     * @return true if it should be shown.
     */
    boolean isShown(Issue issue);

    /**
     * Populate the fieldValueHolder with a value that should be shown by default when the issue
     * has not been created yet.
     *
     * @param fieldValuesHolder The fieldValuesHolder Map to be populated.
     * @param issue             The Issue.
     */
    void populateDefaults(Map<String, Object> fieldValuesHolder, Issue issue);

    /**
     * Checks to see if the (web) parameters contains a relevant value with which to populate the issue
     *
     * @param parameters        Map of HTTP request parameters ("Action parameters").
     */
    boolean hasParam(Map<String, String[]> parameters);

    /**
     * Populate the fieldValuesHolder with a value from (web) parameters.
     *
     * @param fieldValuesHolder The fieldValuesHolder Map to be populated.
     * @param parameters        Map of HTTP parameters.
     */
    void populateFromParams(Map<String, Object> fieldValuesHolder, Map<String, String[]> parameters);

    /**
     * Used to initialise the fieldValuesHolder from the current value of teh issue. Used, for example, when
     * showing the Edit Issue screen to show the issue's current values.
     *
     * @param fieldValuesHolder The fieldValuesHolder Map to be populated.
     * @param issue             The Issue.
     */
    void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue);

    /**
     * Ensure that the parameters are valid
     *
     * @param operationContext            OperationContext
     * @param errorCollectionToAddTo      ErrorCollection to add to.
     * @param i18n                        I18nHelper
     * @param issue                       This is passed to get the value of other fields that may or may not have been modified.
     * @param fieldScreenRenderLayoutItem FieldScreenRenderLayoutItem
     */
    void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem);

    /**
     * The value that should be set on the issue if the field is not shown for some reason.
     * <p>
     * For example: The user does not have permission to see the field, or the field is not part of the create screen.
     * </p>
     *
     * @param issue the Issue.
     * @return The default value. It can be null.
     */
    @Nullable
    Object getDefaultValue(Issue issue);

    /**
     * Create the value of the issue in the data store.
     *
     * @param issue Issue this field is part of
     * @param value Value to store in this field (eg. Collection for Version fields). Cannot be null.
     */
    void createValue(Issue issue, Object value);

    /**
     * Update the issue in the data store.
     * <p>
     * Fields that are simply stored in the JIRAISSUE DB table, need not do anything here as this table is written to
     * one time to include all fields that changed.
     * Fields using other DB tables must update that table with the appropriate values.
     * </p>
     *
     * @param fieldLayoutItem   for this field within this context
     * @param issue             Issue this field is part of
     * @param modifiedValue     new value to set field to. Cannot be null.
     * @param issueChangeHolder an object to record any changes made to the issue by this method.
     */
    void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder);

    /**
     * Record the value on the issue (not saving it to the database, see updateValue).
     *
     * @param fieldLayoutItem  FieldLayoutItem
     * @param issue            MutableIssue
     * @param fieldValueHolder Field Value Holder Map
     * @see #updateValue
     */
    void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder);

    /**
     * Removes the field value from the given MutableIssue object.
     * <p>
     * Note that this operation does not actually persist the change, it just clears the value from the given Issue object.
     * The caller will still need to call #updateValue() in order to persist the change.
     * </p>
     * <p>
     * Some Fields may choose to quietly ignore this request.
     * eg The SecurityLevel Field will not remove Security Level from subtasks because the subtask must always take the
     * Security Level of its parent issue.
     * </p>
     *
     * @param issue The issue object to be effected.
     * @see #canRemoveValueFromIssueObject(com.atlassian.jira.issue.Issue)
     * @see #updateValue
     */
    void removeValueFromIssueObject(MutableIssue issue);

    /**
     * Returns <code>true</code> if a call to {@link #removeValueFromIssueObject(com.atlassian.jira.issue.MutableIssue)}
     * will actually remove the value.
     * <p>
     * There a few different reasons why this method may return false:
     * <ul>
     * <li>The OrderableField can sometimes remove the value, but it decides that it is not relevant for this Issue.
     * eg: Security Level will ignore remove requests on a subtask, as the subtask Security is not set explicitly
     * (it is always inherited from its parent issue).</li>
     * <li>It may possible to remove this value, but the OrderableField does not actually do the job, it is done through
     * some special mechanism. eg Attachments.</li>
     * <li>This field is some special system field which it is invalid to remove. eg Project. In these cases calling
     * removeValueFromIssueObject() would normally raise an UnsupportedOperationException.</li>
     * </ul>
     * In general it is safe to call removeValueFromIssueObject() even if this method returns false, as the request will
     * be silently ignored.
     * However there are a couple of special fields where calling removeValueFromIssueObject() will throw an
     * UnsupportedOperationException - eg if you try to remove the Project.
     * </p>
     *
     * @param issue The Issue object.
     * @return <code>true</code> if a call to {@link #removeValueFromIssueObject(com.atlassian.jira.issue.MutableIssue)}
     *         will actually remove the value.
     * @see #removeValueFromIssueObject(com.atlassian.jira.issue.MutableIssue)
     */
    boolean canRemoveValueFromIssueObject(Issue issue);

    /**
     * Used to determine if the field needs input from user to be moved to the new project and/or issue type.
     * This method is called only if the field is visible in the target project/issue type.
     * <p/>
     *
     * @param originalIssues        Collection of original Issues.
     * @param targetIssue           Target Issue.
     * @param targetFieldLayoutItem FieldLayoutItem
     * @return A MessagedResult with result=true if the field needs input to be moved.
     */
    MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem);

    /**
     * Used to populate the fieldValueHolder with parameters for move issue screen.
     * This field is only called if the issue needs to be updated during move, i.e. {@link #needsMove(java.util.Collection,com.atlassian.jira.issue.Issue,com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem)}
     * returned true, and the remote user (the one performing the move) has the permission to actually update this field.
     *
     * @param fieldValuesHolder Map of field Values.
     * @param originalIssue     orignal Issue
     * @param targetIssue       target Issue
     */
    void populateForMove(Map<String, Object> fieldValuesHolder, Issue originalIssue, Issue targetIssue);

    /**
     * Determines if the field has a value for the given issue.
     *
     * @param issue the Issue.
     * @return true if the given Issue has a value for this field.
     */
    boolean hasValue(Issue issue);

    /**
     * Determines whether the field is available for bulk editing given the BulkEditBean.
     *
     * @param bulkEditBean holds the state of the bulk-edit wizard (e.g. the selected issues for bulk editing)
     * @return <code>null</code> if the field is available for bulk-editing or an i18n key of the error message to show as
     *         why the field is not available.
     */
    String availableForBulkEdit(BulkEditBean bulkEditBean);

    /**
     * Get a field value from the map of parameters passed.
     * The params map may contain other parameters that are not relevant to this custom field.
     *
     * @param params the map of parameters.
     * @return Value for this field from the map of parameters.
     * @throws FieldValidationException if there is a problem with Field Validation.
     */
    Object getValueFromParams(Map params) throws FieldValidationException;

    /**
     * Used to convert from a user friendly string value and put the result into the fieldValuesHolder.
     * This method is useful for places like Jelly where the field value can be a name (e.g. issue type name) and not a
     * regular id that is used in the web pages.
     *
     * @param fieldValuesHolder Map of field Values.
     * @param stringValue       user friendly string value
     * @param issue             the Issue
     * @throws FieldValidationException if cannot convert to a value from the given string
     */
    void populateParamsFromString(Map<String, Object> fieldValuesHolder, String stringValue, Issue issue)
            throws FieldValidationException;
}
