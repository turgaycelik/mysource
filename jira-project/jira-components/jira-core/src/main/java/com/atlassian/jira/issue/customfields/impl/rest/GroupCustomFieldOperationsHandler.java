package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.issue.fields.rest.StandardOperation.SET;

/**
 * @since v5.0
 */
public class GroupCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<Collection<String>>
{
    public GroupCustomFieldOperationsHandler(CustomField field, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(SET.getName());
    }

    @Override
    protected Collection<String> handleSetOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        GroupJsonBean group = operationValue.convertValue(field.getId(), GroupJsonBean.class, errors);

        return group == null ? null : Collections.singletonList(group.getName());
    }

    @Override
    protected List<String> getInitialCreateValue(IssueContext issueCtx)
    {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getInitialValue(Issue issue, ErrorCollection errors)
    {
        @SuppressWarnings ({ "unchecked" }) Collection<Group> value = (Collection<Group>) field.getValue(issue);
        if (value == null)
        {
            return null;
        }

        return Lists.newArrayList(Iterables.transform(value, GroupFunctions.GROUP_TO_NAME));
    }

    @Override
    protected void finaliseOperation(Collection<String> finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        if (finalValue == null)
        {
            parameters.addCustomFieldValue(field.getId(), null);
        }
        else
        {
            String group = finalValue.isEmpty() ? null : finalValue.iterator().next();
            parameters.addCustomFieldValue(field.getId(), group);
        }
    }
}
