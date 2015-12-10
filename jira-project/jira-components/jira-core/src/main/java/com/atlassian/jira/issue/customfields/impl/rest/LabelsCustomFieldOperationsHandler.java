package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static java.util.Collections.emptyList;

/**
 * @since v5.0
 */
public class LabelsCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<Collection<String>>
{
    public LabelsCustomFieldOperationsHandler(CustomField field, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.ADD.getName(), StandardOperation.SET.getName(), StandardOperation.REMOVE.getName());
    }

    @Override
    protected Collection<String> handleRemoveOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (!operationValue.isString())
        {
            errors.addError(field.getId(), i18nHelper.getText("rest.operation.must.be.string"), ErrorCollection.Reason.VALIDATION_FAILED);
            return currentFieldValue;
        }
        return filter(currentFieldValue, not(equalTo(operationValue.asString())));
    }

    @Override
    protected Collection<String> handleSetOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return emptyList();
        }
        return operationValue.asArrayOfStrings(false, field.getId(), errors);
    }

    @Override
    protected Collection<String> handleAddOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (!operationValue.isString())
        {
            errors.addError(field.getId(), i18nHelper.getText("rest.operation.must.be.string"), ErrorCollection.Reason.VALIDATION_FAILED);
            return currentFieldValue;
        }
        String newLabel = operationValue.asString();
        List<String> newList = new ArrayList<String>(currentFieldValue);
        if (!newList.contains(newLabel)) {
            newList.add(newLabel);
        }
        return newList;
    }

    @Override
    protected List<String> getInitialCreateValue(IssueContext issueCtx)
    {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getInitialValue(Issue issue, ErrorCollection errors)
    {
        @SuppressWarnings ({ "unchecked" }) Set<Label> fieldValue = (Set<Label>) field.getValue(issue);
        if (fieldValue == null)
        {
            return emptyList();
        }

        Iterable<String> labels = Iterables.transform(fieldValue, new Function<Label, String>()
        {
            @Override
            public String apply(Label from)
            {
                return from.getLabel();
            }
        });
        return Lists.newArrayList(labels);
    }

    @Override
    protected void finaliseOperation(Collection<String> finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), finalValue.toArray(new String[finalValue.size()]));
    }
}
