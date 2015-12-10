package com.atlassian.jira.issue.fields.rest;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

/**
 * @since v5.0
 */
public class PriorityRestFieldOperationsHandler extends AbstractFieldOperationsHandler<String>
{
    private final ConstantsManager constantsManager;

    public PriorityRestFieldOperationsHandler(ConstantsManager constantsManager, I18nHelper i18nHelper)
    {
        super(i18nHelper);
        this.constantsManager = constantsManager;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    @Override
    protected String getInitialCreateValue()
    {
        return null;
    }

    @Override
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        return issue.getPriorityObject().getId();
    }

    @Override
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.setPriorityId(finalValue);
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String fieldId, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        String priorityId = operationValue.asObjectWithProperty("id", IssueFieldConstants.PRIORITY, errors);
        if (priorityId == null)
        {
            String priorityName = operationValue.asObjectWithProperty("name", IssueFieldConstants.PRIORITY, errors);
            {
                if (priorityName != null)
                {
                    Collection<Priority> allPriorities = constantsManager.getPriorityObjects();
                    for (Priority priority : allPriorities)
                    {
                        if (priority.getName().equals(priorityName))
                        {
                            priorityId = priority.getId();
                        }
                    }
                    if (priorityId == null)
                    {
                        errors.addError(fieldId, i18nHelper.getText("rest.priority.name.invalid", priorityName), ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }
                else
                {
                    errors.addError(fieldId, i18nHelper.getText("rest.priority.no.name.or.id"), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        return priorityId;
    }

}
