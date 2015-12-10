package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestIssueTypeClauseQueryFactory
{
    @Test
    public void testUnsupportedOperators() throws Exception
    {
        final Operator[] invalidOperators = { Operator.GREATER_THAN, Operator.GREATER_THAN_EQUALS, Operator.LESS_THAN, Operator.LESS_THAN_EQUALS, Operator.LIKE };

        IssueTypeResolver issueTypeResolver = new IssueTypeResolver(new MockConstantsManager());

        final SingleValueOperand singleValueOperand = new SingleValueOperand("testOperand");

        for (Operator invalidOperator : invalidOperators)
        {
            IssueTypeClauseQueryFactory issueTypeClauseQueryFactory = new IssueTypeClauseQueryFactory(issueTypeResolver, MockJqlOperandResolver.createSimpleSupport());

            TerminalClause terminalClause = new TerminalClauseImpl("issueType", invalidOperator, singleValueOperand);

            final QueryFactoryResult result = issueTypeClauseQueryFactory.getQuery(null, terminalClause);
            assertEquals(QueryFactoryResult.createFalseResult(), result);
        }
    }
}
