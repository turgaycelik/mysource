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
public class MultiUserCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<Collection<String>>
{
    public MultiUserCustomFieldOperationsHandler(CustomField field, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.ADD.getName(), StandardOperation.SET.getName(), StandardOperation.REMOVE.getName());
    }

    @Override
    protected Collection<String> handleAddOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        String newUser = operationValue.asObjectWithProperty("name", field.getId(), errors);
        List<String> newList = new ArrayList<String>(currentFieldValue);
        if (!newList.contains(newUser)) {
            newList.add(newUser);
        }
        return newList;
    }


    @Override
    protected Collection<String> handleRemoveOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        return filter(currentFieldValue, not(equalTo(operationValue.asObjectWithProperty("name", field.getId(), errors))));
    }

    @Override
    protected Collection<String> handleSetOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        return operationValue.isNull() ? null : operationValue.asArrayOfObjectsWithProperty("name", field.getId(), errors);
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected List<String> getInitialValue(Issue issue, ErrorCollection errors)
    {
        @SuppressWarnings ({ "unchecked" }) Collection<ApplicationUser> fieldValue = (Collection<ApplicationUser>) field.getValue(issue);
        if (fieldValue == null)
        {
            return emptyList();
        }

        Iterable<String> users = Iterables.transform(fieldValue, new Function<ApplicationUser, String>()
        {
            @Override
            public String apply(ApplicationUser from)
            {
                return from.getUsername();
            }
        });
        return Lists.newArrayList(users);
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     * @param issueCtx the Issue Context
     */
    protected List<String> getInitialCreateValue(IssueContext issueCtx)
    {
        return Collections.emptyList();
    }

    @Override
    protected void finaliseOperation(Collection<String> finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), (finalValue != null) ? finalValue.toArray(new String[finalValue.size()]) : new String[]{});
    }

}
