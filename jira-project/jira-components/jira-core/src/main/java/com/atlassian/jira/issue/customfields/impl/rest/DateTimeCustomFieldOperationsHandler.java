package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import java.util.Date;

/**
 * Basic operation handler for single value Text Fields.
 *
 * @since v5.0
 */
public class DateTimeCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<String>
{
    private final DateTimeFormatter dateTimeFormatter;


    public DateTimeCustomFieldOperationsHandler(CustomField field, DateTimeFormatter dateTimeFormatter, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
        this.dateTimeFormatter = dateTimeFormatter;
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        Object value = field.getValue(issue);
        return value == null ? null : dateTimeFormatter.format((Date) value);
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     * @param issueCtx
     */
    protected String getInitialCreateValue(IssueContext issueCtx)
    {
        return null;
    }

    /**
     * takes the value returnd by a call to applyOperation() and puts it into the inputparams
     */
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        if (finalValue == null)
        {
            parameters.addCustomFieldValue(field.getId(), null);
        }
        else
        {
            Date date = null;
            try
            {
                date = Dates.fromTimeString(finalValue);
                parameters.addCustomFieldValue(field.getId(), dateTimeFormatter.format(date));
            }
            catch (IllegalArgumentException e)
            {
                errors.addError(field.getId(), e.getMessage(), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (!operationValue.isNull() &&  !operationValue.isString())
        {
            errors.addError(field.getId(), i18nHelper.getText("rest.operation.must.be.string"), ErrorCollection.Reason.VALIDATION_FAILED);
        }
        return operationValue.asString();
    }
}
