package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
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

/**
 * @since v5.0
 */
public class LabelsRestFieldOperationsHandler extends AbstractFieldOperationsHandler<Collection<String>>
{
    public LabelsRestFieldOperationsHandler(I18nHelper i18nHelper)
    {
        super(i18nHelper);
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.ADD.getName(), StandardOperation.SET.getName(), StandardOperation.REMOVE.getName());
    }

    @Override
    protected Collection<String> handleRemoveOperation(IssueContext issueCtx, Issue issue, String fieldId, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (!operationValue.isString())
        {
            errors.addError(IssueFieldConstants.LABELS, i18nHelper.getText("rest.operation.must.be.string"), ErrorCollection.Reason.VALIDATION_FAILED);
            return currentFieldValue;
        }
        return filter(currentFieldValue, not(equalTo(operationValue.asString())));
    }

    @Override
    protected Collection<String> handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return Collections.emptyList();
        }
        return operationValue.asArrayOfStrings(false, IssueFieldConstants.LABELS, errors);
    }

    @Override
    protected Collection<String> handleAddOperation(IssueContext issueCtx, Issue issue, String fieldId, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (!operationValue.isString())
        {
            errors.addError(IssueFieldConstants.LABELS, i18nHelper.getText("rest.operation.must.be.string"), ErrorCollection.Reason.VALIDATION_FAILED);
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
    protected List<String> getInitialCreateValue()
    {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getInitialValue(Issue issue, ErrorCollection errors)
    {
        Iterable<String> labels = Iterables.transform(issue.getLabels(), new Function<Label, String>()
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
        parameters.getActionParameters().put(SystemSearchConstants.forLabels().getFieldId(), finalValue.toArray(new String[finalValue.size()]));
    }
}
