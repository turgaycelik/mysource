package com.atlassian.jira.jql.values;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Gets all values for status
 *
 * @since v4.0
 */
public class StatusClauseValuesGenerator extends AbstractIssueConstantValuesGenerator
{
    private final ConstantsManager constantsManager;

    public StatusClauseValuesGenerator(final ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    protected List<IssueConstant> getAllConstants()
    {
        return new ArrayList<IssueConstant>(constantsManager.getStatusObjects());
    }
}
