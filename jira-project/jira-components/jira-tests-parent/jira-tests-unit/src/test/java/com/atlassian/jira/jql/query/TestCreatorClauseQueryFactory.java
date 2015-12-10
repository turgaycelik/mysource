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
 * @since v6.2
 */
public class TestCreatorClauseQueryFactory extends TestUserClauseQueryFactory
{

    public TestCreatorClauseQueryFactory()
    {
        this.fieldNameUnderTest = "creator";
        this.clauseQueryFactory = new CreatorClauseQueryFactory(userResolver, MockJqlOperandResolver.createSimpleSupport());
    }

}
