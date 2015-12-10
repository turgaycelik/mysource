package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @since v5.0
 */
public class ReporterRestFieldOperationsHandler extends AbstractFieldOperationsHandler<String>
{
    public ReporterRestFieldOperationsHandler(I18nHelper i18nHelper)
    {
        super(i18nHelper);
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        return operationValue.isNull() ? null : operationValue.asObjectWithProperty("name", IssueFieldConstants.REPORTER, errors);
    }

    @Override
    protected String getInitialCreateValue()
    {
        return null;
    }

    @Override
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        return issue.getReporterId();
    }

    @Override
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.setReporterId(finalValue);
    }

}
