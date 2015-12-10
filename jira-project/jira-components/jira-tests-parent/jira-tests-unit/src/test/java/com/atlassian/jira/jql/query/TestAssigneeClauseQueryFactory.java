package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestAssigneeClauseQueryFactory extends TestUserClauseQueryFactory
{

    public TestAssigneeClauseQueryFactory()
    {
        this.fieldNameUnderTest = "assignee";
        this.clauseQueryFactory = new AssigneeClauseQueryFactory(userResolver, MockJqlOperandResolver.createSimpleSupport());
    }
}
