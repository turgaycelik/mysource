package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.FieldOperationHolder;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * @since v5.0
 */
@PublicSpi
public abstract class AbstractCustomFieldOperationsHandler<T> implements RestFieldOperationsHandler
{
    protected final CustomField field;
    protected final I18nHelper i18nHelper;

    /**
     * Constructor.
     * @param field Field the handler is for.
     * @param i18nHelper I18nHelper
     */
    public AbstractCustomFieldOperationsHandler(CustomField field, I18nHelper i18nHelper)
    {
        this.field = field;
        this.i18nHelper = i18nHelper;
    }

    /**
     * Constructor.
     * @param field Field the handler is for.
     *
     * @deprecated @since 5.0-RC2 Use the constructor {@link #AbstractCustomFieldOperationsHandler(CustomField field, I18nHelper i18nHelper)}
     */
    public AbstractCustomFieldOperationsHandler(CustomField field)
    {
        this.field = field;
        i18nHelper = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected abstract T getInitialValue(Issue issue, ErrorCollection errors);
    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected abstract T getInitialCreateValue(IssueContext issueCtx);

    /**
     * takes the value returnd by a call to applyOperation() and puts it into the inputparams
     */
    protected abstract void finaliseOperation(T finalValue, IssueInputParameters parameters, ErrorCollection errors);

    @Override
    public ErrorCollection updateIssueInputParameters(IssueContext issueCtx, Issue issue, String fieldId, IssueInputParameters inputParameters, List<FieldOperationHolder> operations)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        T newFieldValue = issue == null ? getInitialCreateValue(issueCtx) : getInitialValue(issue, errors);
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
                        newFieldValue = handleAddOperation(issueCtx, issue, newFieldValue, operationValue, errors);
                        break;
                    case SET:
                        newFieldValue = handleSetOperation(issueCtx, issue, newFieldValue, operationValue, errors);
                        break;
                    case REMOVE:
                        newFieldValue = handleRemoveOperation(issueCtx, issue, newFieldValue, operationValue, errors);
                        break;
                    default:
                        newFieldValue = applyOperation(issueCtx, issue, standardOperation, newFieldValue, operationValue, errors);
                        break;
                }
            }
            catch (IllegalArgumentException ex)
            {
                errors.addErrorMessage("No operation with name '" + oper + "' found. Valid operations are '" + StringUtils.join(StandardOperation.values(), ","));
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

    protected T handleSetOperation(IssueContext issueCtx, Issue issue, T currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        throw new UnsupportedOperationException("Remove operation not supported!");
    }

    protected T handleRemoveOperation(IssueContext issueCtx, Issue issue, T currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        throw new UnsupportedOperationException("Remove operation not supported!");
    }

    protected T handleAddOperation(IssueContext issueCtx, Issue issue, T currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        throw new UnsupportedOperationException("Add operation not supported!");
    }

    /**
     * gets called (perhaps multiple times) to apply the given operation to the given currentValuye, retyrning the new
     * value
     */
    protected T applyOperation(IssueContext issueCtx, Issue issue, StandardOperation operation, T currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        throw new UnsupportedOperationException("'" + operation.getName() + "' operation not supported!");
    }

}
