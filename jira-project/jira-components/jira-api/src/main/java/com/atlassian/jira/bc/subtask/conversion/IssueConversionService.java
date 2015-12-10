package com.atlassian.jira.bc.subtask.conversion;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.util.I18nHelper;

import java.util.Collection;

@PublicApi
public interface IssueConversionService
{
    /**
     * Determines if user can convert given issue.
     *
     * @param context JIRA Service Context
     * @param issue   issue to convert
     * @return true if user can convert the given issue, false otherwise
     * @throws IllegalArgumentException if issue is null
     */
    boolean canConvertIssue(JiraServiceContext context, Issue issue);

    /**
     * Checks the user's permission to convert given issue. User is retrieved
     * from the context.
     *
     * @param context jira service context
     * @param issue   issue to check the permission on
     * @return true if user is granted permission to convert given issue, false otherwise
     */
    boolean hasPermission(JiraServiceContext context, Issue issue);


    /**
     * Determines if the workflow status exists in the target workflow (based
     * on current project and target issue type).
     * <br/>
     * Returns true if the issue's status is not a valid status for the target
     * issue type workflow in the issue's project - issue's status needs to
     * change, false otherwise.
     *
     * @param context   jira service context
     * @param issue     issue to convert
     * @param issueType target issue type
     * @return true if status change is needed, otherwise false
     */
    boolean isStatusChangeRequired(JiraServiceContext context, Issue issue, IssueType issueType);

    /**
     * Retrieves the collection of {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem}
     * required to be entered from converting issue from given issue to given target issue.
     *
     * @param context       jira service context
     * @param originalIssue Current Issue
     * @param targetIssue   Issue with updated issue type
     * @return A Collection of {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem} that require values, never null
     *
     * @deprecated Use {@link #getFieldLayoutItems(com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.Issue)} instead. Since v6.2.
     */
    Collection <FieldLayoutItem> getFieldLayoutItems(JiraServiceContext context, Issue originalIssue, Issue targetIssue);

    /**
     * Retrieves the collection of {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem}
     * required to be entered from converting issue from given issue to given target issue.
     *
     * @param originalIssue Current Issue
     * @param targetIssue   Issue with updated issue type
     * @return A Collection of {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem} that require values, never null
     */
    Collection<FieldLayoutItem> getFieldLayoutItems(Issue originalIssue, Issue targetIssue);

    /**
     * Validates that the issue can be converted to given issue type.
     * Any errors are communicated back via error collection in the context.
     *
     * @param context              jira service context
     * @param issue                issue to convert
     * @param issueType            target issue type
     * @param fieldNameIssueTypeId form field name of the issue type id
     */
    void validateTargetIssueType(JiraServiceContext context, Issue issue, IssueType issueType, final String fieldNameIssueTypeId);

    /**
     * Validates that the target status is a valid status for the issue's
     * project and the target issue type. In case of invalid status a new
     * error message is added to the context's error collection under the given
     * fieldName.
     *
     * @param context   jira service context
     * @param status    target status
     * @param fieldName form field name
     * @param issue     issue to convert
     * @param issueType target issue type
     */
    public void validateTargetStatus(JiraServiceContext context, Status status, final String fieldName, Issue issue, IssueType issueType);


    /**
     * Populates the operationContext from the params in the {@link webwork.action.ActionContext}
     * In case of invalid values, new error messages are added to the context's error collection under the
     * fieldName.
     *
     * @param context          jira service context
     * @param operationContext contains Map (CustomFieldValuesHolder) where field values will be populated
     * @param i18nHelper       helper bean for i18n (needed by fields)
     * @param targetIssue      Current issue with updated parent, issue type and status
     * @param fieldLayoutItems Collection of {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem} that required input
     */
    public void populateFields(JiraServiceContext context, OperationContext operationContext, I18nHelper i18nHelper, Issue targetIssue, Collection<FieldLayoutItem> fieldLayoutItems);

    /**
     * Retrieves the fields that have values on the issue but are no longer needed.
     *
     * @param origIssue issue
     * @param targetIssue issue
     * @return collection of {@link com.atlassian.jira.issue.fields.OrderableField}, never null
     */
    public Collection<OrderableField> getRemovedFields(Issue origIssue, Issue targetIssue);

    /**
     * Retrieves the fields that have values on the issue but are no longer needed.
     *
     * @param context jira service context
     * @param origIssue issue
     * @param targetIssue issue
     * @return collection of {@link com.atlassian.jira.issue.fields.OrderableField}, never null
     *
     * @deprecated Use {@link #getRemovedFields(Issue, Issue)} instead. Since v5.1.
     */
    public Collection<OrderableField> getRemovedFields(JiraServiceContext context, Issue origIssue, Issue targetIssue);

    /**
     * This is the core method that converts given issue to an issue represented by updatedIssue.
     *
     * @param context      jira service context, any error are added here
     * @param issue        original issue
     * @param updatedIssue target issue
     */
    public void convertIssue(JiraServiceContext context, Issue issue, MutableIssue updatedIssue);

    /**
     * Validates that all fields inputed have valid values.
     * In case of invalid values, new error messages are added to the context's error collection under the
     * fieldName.
     *
     * @param context          jira service context
     * @param operationContext contains Map (CustomFieldValuesHolder) containing values to be validated
     * @param i18nHelper       helper bean for i18n (needed by fields)
     * @param targetIssue      Current issue with updated parent, issue type and status
     * @param fieldLayoutItems Collection of {@link com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem} that required input
     */
    public void validateFields(JiraServiceContext context, OperationContext operationContext, I18nHelper i18nHelper, Issue targetIssue, Collection<FieldLayoutItem> fieldLayoutItems);

    /**
     * Allows for a plugin point to extra updates specific to that sub class
     *
     * @param context      jira service context
     * @param changeHolder holds all the change items
     * @param currentIssue original issue with no updates
     * @param targetIssue  target issue with all fields updated
     */
    public void preStoreUpdates(JiraServiceContext context, IssueChangeHolder changeHolder, Issue currentIssue, MutableIssue targetIssue);
}
