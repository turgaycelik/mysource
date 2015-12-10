package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

/**
 * Basic operation handler for single value Text Fields.
 *
 * @since v5.0
 */
@Internal
public class TextCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<String>
{
    /**
     * Constructor.
     * @param field Field the handler is for.
     * @param i18nHelper I18nHelper
     */
    public TextCustomFieldOperationsHandler(CustomField field, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
    }

    /**
     * Constructor.
     * @param field Field the handler is for.
     *
     * @deprecated @since 5.0-RC2 Use the constructor {@link #TextCustomFieldOperationsHandler(CustomField field, I18nHelper i18nHelper)}
     */
    public TextCustomFieldOperationsHandler(CustomField field)
    {
        super(field);
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        return (String) field.getValue(issue);
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     * @param issueCtx
     */
    protected String getInitialCreateValue(IssueContext issueCtx)
    {
        return null;
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return null;
        }
        if (!operationValue.isString())
        {
            errors.addError(field.getId(), i18nHelper.getText("rest.operation.must.be.string"), ErrorCollection.Reason.VALIDATION_FAILED);
        }
        return operationValue.asString();
    }

    /**
     * takes the value returnd by a call to applyOperation() and puts it into the inputparams
     */
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), finalValue);
    }

}
