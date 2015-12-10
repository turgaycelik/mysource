package com.atlassian.jira.jql.operand;

import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.query.clause.WasClause;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @since v4.4
 */
public class EmptyWasClauseOperandHandler
{
    private final ChangeHistoryFieldConfigurationManager changeHistoryFieldConfigurationManager;

    public EmptyWasClauseOperandHandler(ChangeHistoryFieldConfigurationManager changeHistoryFieldConfigurationManager)
    {
        this.changeHistoryFieldConfigurationManager = changeHistoryFieldConfigurationManager;
    }

    public List<QueryLiteral> getEmptyValue(WasClause clause)
    {
        List<QueryLiteral> literals = Lists.newArrayList();
        if (clause != null)
        {
            literals.add(new QueryLiteral(clause.getOperand(), getStringValueForEmpty(clause)));
        }
        return literals;
    }

    private String getStringValueForEmpty(WasClause clause)
    {
        return (clause != null) ? changeHistoryFieldConfigurationManager.getEmptyValue(clause.getField().toLowerCase()) : null;
    }
}
