package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestSupportedOperatorsValidator
{
    @Test
    public void testInvalidOperators() throws Exception
    {
        final SupportedOperatorsValidator supportedOperatorsValidator = new SupportedOperatorsValidator(OperatorClasses.NON_RELATIONAL_OPERATORS)
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        TerminalClause clause = new TerminalClauseImpl(DocumentConstants.ISSUE_STATUS, Operator.GREATER_THAN, new SingleValueOperand("test"));
        MessageSet messageSet = supportedOperatorsValidator.validate(null, clause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals("The operator '>' is not supported by the 'status' field.", messageSet.getErrorMessages().iterator().next());

        clause = new TerminalClauseImpl(DocumentConstants.ISSUE_STATUS, Operator.GREATER_THAN_EQUALS, new SingleValueOperand("test"));
        messageSet = supportedOperatorsValidator.validate(null, clause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals("The operator '>=' is not supported by the 'status' field.", messageSet.getErrorMessages().iterator().next());

        clause = new TerminalClauseImpl(DocumentConstants.ISSUE_STATUS, Operator.LESS_THAN, new SingleValueOperand("test"));
        messageSet = supportedOperatorsValidator.validate(null, clause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals("The operator '<' is not supported by the 'status' field.", messageSet.getErrorMessages().iterator().next());

        clause = new TerminalClauseImpl(IssueFieldConstants.PROJECT, Operator.LESS_THAN_EQUALS, new SingleValueOperand("test"));
        messageSet = supportedOperatorsValidator.validate(null, clause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals("The operator '<=' is not supported by the 'project' field.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateOperatorsHappyPath() throws Exception
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("test");

        final SupportedOperatorsValidator supportedOperatorsValidator = new SupportedOperatorsValidator(OperatorClasses.NON_RELATIONAL_OPERATORS);

        TerminalClause clause = new TerminalClauseImpl(DocumentConstants.ISSUE_STATUS, Operator.EQUALS, singleValueOperand);
        MessageSet messageSet = supportedOperatorsValidator.validate(null, clause);
        assertFalse(messageSet.hasAnyMessages());

        clause = new TerminalClauseImpl(DocumentConstants.ISSUE_STATUS, Operator.IN, singleValueOperand);
        messageSet = supportedOperatorsValidator.validate(null, clause);
        assertFalse(messageSet.hasAnyMessages());

        clause = new TerminalClauseImpl(DocumentConstants.ISSUE_STATUS, Operator.NOT_EQUALS, singleValueOperand);
        messageSet = supportedOperatorsValidator.validate(null, clause);
        assertFalse(messageSet.hasAnyMessages());

        clause = new TerminalClauseImpl(DocumentConstants.ISSUE_STATUS, Operator.LIKE, singleValueOperand);
        messageSet = supportedOperatorsValidator.validate(null, clause);
        assertFalse(messageSet.hasAnyMessages());

        clause = new TerminalClauseImpl(DocumentConstants.ISSUE_STATUS, Operator.IS, singleValueOperand);
        messageSet = supportedOperatorsValidator.validate(null, clause);
        assertFalse(messageSet.hasAnyMessages());
    }
}
