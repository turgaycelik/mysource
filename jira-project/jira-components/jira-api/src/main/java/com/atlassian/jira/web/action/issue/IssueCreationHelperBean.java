package com.atlassian.jira.web.action.issue;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is a helper bean shared by the Issue Service impl and the Create Issue.
 * It should not be considered API - plugins should use IssueService instead.
 *
 * @TODO: Move to jira-core in JIRA 7.0 or later
 * @deprecated Use {@link com.atlassian.jira.bc.issue.IssueService} instead (will be moved into jira-core). Since v6.2.
 */
@Internal
public interface IssueCreationHelperBean
{

    void validateCreateIssueFields(JiraServiceContext jiraServiceContext, Collection<String> providedFields, Issue issueObject, FieldScreenRenderer fieldScreenRenderer,
            OperationContext operationContext,  IssueInputParameters issueInputParameters, I18nHelper i18n);

    /**
     * @deprecated Use {@link IssueCreationHelperBean#validateCreateIssueFields(com.atlassian.jira.bc.JiraServiceContext, java.util.Collection, com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.fields.screen.FieldScreenRenderer, com.atlassian.jira.issue.customfields.OperationContext, com.atlassian.jira.issue.IssueInputParameters, com.atlassian.jira.util.I18nHelper)} instead. Since v6.3.
     *
     */
    void validateCreateIssueFields(JiraServiceContext jiraServiceContext, Collection<String> providedFields, Issue issueObject, FieldScreenRenderer fieldScreenRenderer,
            OperationContext operationContext, Map<String, String[]> parameters, boolean applyDefaults, I18nHelper i18n);

    void validateLicense(ErrorCollection errors, I18nHelper i18n);

    /**
     * @deprecated Use {@link #updateIssueFromFieldValuesHolder(com.atlassian.jira.issue.fields.screen.FieldScreenRenderer, com.atlassian.jira.issue.MutableIssue, java.util.Map)} instead. Since v6.2.
     */
    void updateIssueFromFieldValuesHolder(FieldScreenRenderer fieldScreenRenderer, User remoteUser, MutableIssue issueObject, Map fieldValuesHolder);

    void updateIssueFromFieldValuesHolder(FieldScreenRenderer fieldScreenRenderer, MutableIssue issueObject, Map fieldValuesHolder);

    /**
     * @deprecated Use {@link #createFieldScreenRenderer(com.atlassian.jira.issue.Issue)} instead. Since v6.2.
     */
    FieldScreenRenderer createFieldScreenRenderer(User remoteUser, Issue issueObject);

    FieldScreenRenderer createFieldScreenRenderer(Issue issueObject);

    /**
     * @deprecated Use {@link #getProvidedFieldNames(com.atlassian.jira.issue.Issue)} instead. Since v6.2.
     */
    List<String> getProvidedFieldNames(User remoteUser, Issue issueObject);

    List<String> getProvidedFieldNames(Issue issueObject);

    /**
     * Gets the fields that will be shown in the create issue screen for that issues project and issue type
     *
     * @param user the user in play
     * @param issueObject the as yet saved issue object encompassing project and issue type
     * @return the list of fields that will be shown on the create issue screen
     */
    List<OrderableField> getFieldsForCreate(User user, Issue issueObject);


    void validateProject(Issue issue, OperationContext operationContext, Map actionParams, ErrorCollection errors,
                         I18nHelper i18n);

    void validateIssueType(Issue issue, OperationContext operationContext, Map actionParams, ErrorCollection errors,
                           I18nHelper i18n);

    void validateSummary(Issue issue, OperationContext operationContext, Map actionParams, ErrorCollection errors,
                           I18nHelper i18n);

}
