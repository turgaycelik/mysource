package com.atlassian.jira.issue.fields.rest;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import webwork.action.Action;

import java.util.List;

/**
 * Helper factory to get a list of all the fields required for create, edit and creating subtasks.
 *
 * @since v5.0.3
 */
@ExperimentalApi
public interface FieldHtmlFactory
{
    /**
     * Returns a list of fields on the create screen as specified by the newIssueObject skeleton (it contains the
     * project and issue type context) including the project and issue type fields.
     *
     * @param user the remote user
     * @param operationContext the webwork action performing this operation
     * @param action the webwork action performing this operation
     * @param newIssueObject skeleton issue object
     * @param retainValues If we should keep all the field values when switching from full back to quick edit
     * @param fieldsToRetain List of fields to retain when creating multiple issues in a row
     * @return A list of {@link FieldHtmlBean}s
     */
    List<FieldHtmlBean> getCreateFields(final User user, final OperationContext operationContext,
            final Action action, final MutableIssue newIssueObject, boolean retainValues, final List<String> fieldsToRetain);

    /**
     * Returns all the edit fields required for the specified issue object.  This also includes a comment field if the
     * user has permission to add comments.
     *
     * @param user the remote user
     * @param operationContext the webwork action performing this operation
     * @param action the webwork action performing this operation
     * @param issue the issue being edited. Used to populate the field values.
     * @param retainValues If we should keep all the field values when switching from full back to quick edit
     * @return A list of {@link FieldHtmlBean}s
     */
    List<FieldHtmlBean> getEditFields(final User user, final OperationContext operationContext,
            final Action action, final Issue issue, final boolean retainValues);

    /**
     * This essentially returns the same as the {@link #getCreateFields(com.atlassian.crowd.embedded.api.User,
     * com.atlassian.jira.issue.customfields.OperationContext, webwork.action.Action,
     * com.atlassian.jira.issue.MutableIssue, boolean, java.util.List)} method but it strips out certain fields such as
     * the project selector.
     *
     * @param user the remote user
     * @param operationContext the webwork action performing this operation
     * @param action the webwork action performing this operation
     * @param newIssueObject skeleton issue object
     * @param retainValues If we should keep all the field values when switching from full back to quick edit
     * @param fieldsToRetain List of fields to retain when creating multiple issues in a row
     * @return A list of {@link FieldHtmlBean}s
     */
    List<FieldHtmlBean> getSubTaskCreateFields(final User user, final OperationContext operationContext,
            final Action action, final MutableIssue newIssueObject, boolean retainValues, final List<String> fieldsToRetain);
}
