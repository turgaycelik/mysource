package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @since v5.0
 */
public class AssigneeRestFieldOperationsHandler extends AbstractFieldOperationsHandler<String>
{
    public AssigneeRestFieldOperationsHandler(I18nHelper i18nHelper)
    {
        super(i18nHelper);
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return null;
        }
        else
        {
            String name = operationValue.asObjectWithProperty("name", IssueFieldConstants.ASSIGNEE, true, errors);
            if (name == null)
            {

            }
            return name;
        }
    }

    @Override
    protected String getInitialCreateValue()
    {
        return null;
    }

    @Override
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        return issue.getAssigneeId();
    }

    @Override
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.setAssigneeId(finalValue);
    }

}
