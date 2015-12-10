package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;

import java.util.Date;
import java.util.Set;

/**
 * @since v5.0
 */
public class DueDateRestFieldOperationsHandler extends AbstractFieldOperationsHandler<String>
{
    private final DateFieldFormat dateFieldFormat;

    public DueDateRestFieldOperationsHandler(DateFieldFormat dateFieldFormat, I18nHelper i18nHelper)
    {
        super(i18nHelper);
        this.dateFieldFormat = dateFieldFormat;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (!operationValue.isNull() &&  !operationValue.isString())
        {
            errors.addError(IssueFieldConstants.DUE_DATE, i18nHelper.getText("rest.operation.must.be.string"), ErrorCollection.Reason.VALIDATION_FAILED);
        }
        return operationValue.asString();
    }

    @Override
    protected String getInitialCreateValue()
    {
        return null;
    }

    @Override
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        return Dates.asDateString(issue.getDueDate());
    }

    @Override
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        if (finalValue == null)
        {
            parameters.setDueDate(null);
        }
        else
        {
            Date date = null;
            try
            {
                date = Dates.fromDateString(finalValue);
                parameters.setDueDate(dateFieldFormat.formatDatePicker(date));
            }
            catch (IllegalArgumentException e)
            {
                errors.addError(IssueFieldConstants.DUE_DATE, e.getMessage(), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
    }

}
