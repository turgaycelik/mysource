package com.atlassian.jira.jql.values;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Provideds the possible values for priority
 *
 * @since v4.0
 */
public class PriorityClauseValuesGenerator extends AbstractIssueConstantValuesGenerator
{
    private final ConstantsManager constantsManager;

    public PriorityClauseValuesGenerator(final ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    protected List<IssueConstant> getAllConstants()
    {
        return new ArrayList<IssueConstant>(constantsManager.getPriorityObjects());
    }
}