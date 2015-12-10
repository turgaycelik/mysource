package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.TypeRef;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.issue.customfields.impl.rest.GroupFunctions.GROUP_BEAN_TO_NAME;
import static com.atlassian.jira.issue.fields.rest.StandardOperation.ADD;
import static com.atlassian.jira.issue.fields.rest.StandardOperation.REMOVE;
import static com.atlassian.jira.issue.fields.rest.StandardOperation.SET;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static java.util.Collections.emptyList;

/**
 * @since v5.0
 */
public class MultiGroupCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<Collection<String>>
{
    private static final TypeRef<List<GroupJsonBean>> LIST_OF_GROUP_JSON_BEAN = new TypeRef<List<GroupJsonBean>>()
    {
        // empty
    };

    public MultiGroupCustomFieldOperationsHandler(CustomField field, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(ADD.getName(), SET.getName(), REMOVE.getName());
    }

    @Override
    protected Collection<String> handleSetOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return Collections.emptyList();
        }
        List<GroupJsonBean> newGroup = operationValue.convertValue(field.getId(), LIST_OF_GROUP_JSON_BEAN, errors);
        if (newGroup == null)
        {
            errors.addError(field.getId(), i18nHelper.getText("rest.group.value.not.group.objects"), ErrorCollection.Reason.VALIDATION_FAILED);
            return currentFieldValue;
        }

        return Lists.newArrayList(Lists.transform(newGroup, GROUP_BEAN_TO_NAME));
    }

    @Override
    protected Collection<String> handleAddOperation(IssueContext issueCtx, Issue issue, Collection<String> currentValue, JsonData operationValue, ErrorCollection errors)
    {
        GroupJsonBean newGroup = operationValue.convertValue(field.getId(), GroupJsonBean.class, errors);
        if (newGroup == null)
        {
            errors.addError(field.getId(), i18nHelper.getText("rest.group.value.not.group.objects"), ErrorCollection.Reason.VALIDATION_FAILED);
            return currentValue;
        }

        if (currentValue.contains(newGroup.getName()))
        {
            return currentValue;
        }

        List<String> newValue = Lists.newArrayListWithCapacity(currentValue.size() + 1);
        newValue.addAll(currentValue);
        newValue.add(newGroup.getName());

        return newValue;
    }

    @Override
    protected Collection<String> handleRemoveOperation(IssueContext issueCtx, Issue issue, Collection<String> currentValue, JsonData operationValue, ErrorCollection errors)
    {
        GroupJsonBean newGroup = operationValue.convertValue(field.getId(), GroupJsonBean.class, errors);
        if (newGroup == null)
        {
            errors.addError(field.getId(), i18nHelper.getText("rest.group.value.not.group.objects"), ErrorCollection.Reason.VALIDATION_FAILED);
            return currentValue;
        }

        return filter(currentValue, not(equalTo(newGroup.getName())));
    }

    @Override
    protected List<String> getInitialCreateValue(IssueContext issueCtx)
    {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getInitialValue(Issue issue, ErrorCollection errors)
    {
        @SuppressWarnings ({ "unchecked" }) Collection<Group> fieldValue = (Collection<Group>) field.getValue(issue);
        if (fieldValue == null)
        {
            return emptyList();
        }

        Iterable<String> groups = Iterables.transform(fieldValue, new Function<Group, String>()
        {
            @Override
            public String apply(Group from)
            {
                return from.getName();
            }
        });

        return Lists.newArrayList(groups);
    }

    @Override
    protected void finaliseOperation(Collection<String> finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), finalValue.toArray(new String[finalValue.size()]));
    }
}
