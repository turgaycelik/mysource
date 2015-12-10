package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @since v5.0
 */
public abstract class AbstractFieldOperationsHandler<T> implements RestFieldOperationsHandler
{
    protected final I18nHelper i18nHelper;

    public AbstractFieldOperationsHandler(I18nHelper i18nHelper)
    {
        this.i18nHelper = i18nHelper;
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected abstract T getInitialValue(Issue issue, ErrorCollection errors);
    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected abstract T getInitialCreateValue();


    /**
     * takes the valuye returnd by a call to applyOperation() and puts it into the inputparams
     */
    protected abstract void finaliseOperation(T finalValue, IssueInputParameters parameters, ErrorCollection errors);


    @Override
    public ErrorCollection updateIssueInputParameters(IssueContext issueCtx, Issue issue, String fieldId, IssueInputParameters inputParameters, List<FieldOperationHolder> operations)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        T newFieldValue = issue == null ? getInitialCreateValue() : getInitialValue(issue, errors);
        if (errors.hasAnyErrors())
        {
            return errors;
        }
        for (FieldOperationHolder operation : operations)
        {
            String oper = operation.getOperation();
            JsonData operationValue = operation.getData();
            try
            {
                StandardOperation standardOperation = StandardOperation.valueOf(oper.toUpperCase());
                switch (standardOperation)
                {
                    case ADD:
                        newFieldValue = handleAddOperation(issueCtx, issue, fieldId, newFieldValue, operationValue, errors);
                        break;
                    case SET:
                        newFieldValue = handleSetOperation(issueCtx, issue, fieldId, newFieldValue, operationValue, errors);
                        break;
                    case REMOVE:
                        newFieldValue = handleRemoveOperation(issueCtx, issue, fieldId, newFieldValue, operationValue, errors);
                        break;
                    default:
                        newFieldValue = applyOperation(issueCtx, issue, fieldId, standardOperation, newFieldValue, operationValue, errors);
                        break;
                }
            }
            catch (IllegalArgumentException ex)
            {
                errors.addErrorMessage(i18nHelper.getText("rest.operation.invalid", oper, StringUtils.join(StandardOperation.values(), ",")));
                continue;
            }
        }
        if (errors.hasAnyErrors())
        {
            return errors;
        }
        finaliseOperation(newFieldValue, inputParameters, errors);
        return errors;
    }

    /**
     * gets called (perhaps multiple times) to apply the given operation to the given currentValuye, retyrning the new
     * value
     */
    protected T applyOperation(IssueContext issueCtx, Issue issue, String fieldId, StandardOperation operation, T currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        throw new UnsupportedOperationException(i18nHelper.getText("rest.operation.not.supported", operation.getName()));
    }

    protected T handleRemoveOperation(IssueContext issueCtx, Issue issue, String fieldId, T currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        throw new UnsupportedOperationException(i18nHelper.getText("rest.operation.not.supported", "Remove"));
    }

    protected T handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, T currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        throw new UnsupportedOperationException(i18nHelper.getText("rest.operation.not.supported", "Set"));
    }

    protected T handleAddOperation(IssueContext issueCtx, Issue issue, String fieldId, T currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        throw new UnsupportedOperationException(i18nHelper.getText("rest.operation.not.supported", "Add"));
    }

    protected Long[] toLongIds(Collection<String> ids, String fieldName, ErrorCollection errors)
    {
        ArrayList<Long> result = new ArrayList<Long>(ids.size());
        for (String id : ids)
        {
            try
            {
                result.add(Long.parseLong(id));
            }
            catch (NumberFormatException e)
            {
                errors.addError(fieldName, i18nHelper.getText("rest.could.not.parse.id", id), ErrorCollection.Reason.VALIDATION_FAILED);
                return null;
            }
        }
        return result.toArray(new Long[result.size()]);
    }


}
