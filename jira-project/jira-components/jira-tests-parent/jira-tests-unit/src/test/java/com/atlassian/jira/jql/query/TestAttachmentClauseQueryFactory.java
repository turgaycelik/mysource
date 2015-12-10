package com.atlassian.jira.jql.query;

import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Sets;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @since v6.2
 */
public class TestAttachmentClauseQueryFactory
{
    private static final String FIELD_NAME = "attachments";

    @Test
    public void testUnsupportedOperators()
    {
        AttachmentClauseQueryFactory attachmentClauseQueryFactory = new AttachmentClauseQueryFactory();

        Sets.SetView<Operator> invalidOperators = Sets.difference(Sets.newHashSet(Operator.values()), OperatorClasses.EMPTY_ONLY_OPERATORS);

        for (Operator invalidOperator : invalidOperators)
        {
            TerminalClauseImpl terminalClause = new TerminalClauseImpl(FIELD_NAME, invalidOperator, new EmptyOperand());
            QueryFactoryResult result = attachmentClauseQueryFactory.getQuery(mock(QueryCreationContext.class), terminalClause);
            assertThat("Attachments don't support " + invalidOperator + " operator", result, is(QueryFactoryResult.createFalseResult()));
        }
    }

    @Test
    public void testAttachmentsDisabled()
    {
        AttachmentClauseQueryFactory attachmentClauseQueryFactory = new AttachmentClauseQueryFactory();

        TerminalClauseImpl terminalClause = new TerminalClauseImpl(FIELD_NAME, Operator.IS, new EmptyOperand());
        QueryFactoryResult result = attachmentClauseQueryFactory.getQuery(mock(QueryCreationContext.class), terminalClause);
        assertThat(result, not(QueryFactoryResult.createFalseResult()));
    }

}
