package com.atlassian.jira.jql.values;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Will get all values for issue types.
 *
 * @since v4.0
 */
public class IssueTypeClauseValuesGenerator extends AbstractIssueConstantValuesGenerator
{
    private final ConstantsManager constantsManager;

    public IssueTypeClauseValuesGenerator(ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    protected List<IssueConstant> getAllConstants()
    {
        return new ArrayList<IssueConstant>(constantsManager.getAllIssueTypeObjects());
    }
}