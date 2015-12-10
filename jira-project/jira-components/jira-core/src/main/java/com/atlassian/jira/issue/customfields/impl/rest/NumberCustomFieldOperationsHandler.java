package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

/**
 * Basic operation handler for single value Text Fields.
 *
 * @since v5.0
 */
public class NumberCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<Double>
{
    private final DoubleConverter doubleConverter;

    public NumberCustomFieldOperationsHandler(CustomField field, DoubleConverter doubleConverter , I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
        this.doubleConverter = doubleConverter;
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected Double getInitialValue(Issue issue, ErrorCollection errors)
    {
        return (Double) field.getValue(issue);
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     * @param issueCtx
     */
    protected Double getInitialCreateValue(IssueContext issueCtx)
    {
        return null;
    }

    /**
     * takes the value returnd by a call to applyOperation() and puts it into the inputparams
     */
    protected void finaliseOperation(Double finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), finalValue != null ? doubleConverter.getString(finalValue) : null);
    }

    @Override
    protected Double handleSetOperation(IssueContext issueCtx, Issue issue, Double currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return null;
        }
        Object data = operationValue.getData();
        if (!(data instanceof Number))
        {
            errors.addError(field.getId(), i18nHelper.getText("rest.operation.must.be.number"), ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }
        return ((Number) data).doubleValue();
    }
}
