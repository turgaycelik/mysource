package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @since v5.0
 */
public class UserCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<String>
{
    public UserCustomFieldOperationsHandler(CustomField field, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        return operationValue.isNull() ? null : operationValue.asObjectWithProperty("name", field.getId(), errors);
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        ApplicationUser user = (ApplicationUser) field.getValue(issue);
        return user == null ? null : user.getUsername();
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     * @param issueCtx IssueContext
     */
    protected String getInitialCreateValue(IssueContext issueCtx)
    {
        return null;
    }

    @Override
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), (finalValue != null) ? finalValue : "");
    }

}
