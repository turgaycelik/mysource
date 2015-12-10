package com.atlassian.jira.jql.validator;

import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IssueTypeResolver;
import com.atlassian.jira.mock.Strict;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestIssueTypeValidator
{
    @Test
    public void testRawValuesDelegateNotCalledWithOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final JqlOperandResolver mockJqlOperandResolver = mock(JqlOperandResolver.class);
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("blah blah");
        final SupportedOperatorsValidator supportedOperatorsValidator = mock(SupportedOperatorsValidator.class);
        when(supportedOperatorsValidator.validate(null, clause)).thenReturn(messageSet);

        final RawValuesExistValidator rawValuesExistValidator = mock(RawValuesExistValidator.class, new Strict());

        final IssueTypeValidator issueTypeValidator = new IssueTypeValidator(null, mockJqlOperandResolver, null)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return supportedOperatorsValidator;
            }

            @Override
            RawValuesExistValidator getRawValuesValidator(final IssueTypeResolver issueTypeResolver, final JqlOperandResolver operandSupport)
            {
                return rawValuesExistValidator;
            }
        };

        issueTypeValidator.validate(null, clause);
        verify(supportedOperatorsValidator).validate(null, clause);
    }

    @Test
    public void testRawValuesDelegateCalledWithNoOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final JqlOperandResolver mockJqlOperandResolver = mock(JqlOperandResolver.class);


        final SupportedOperatorsValidator supportedOperatorsValidator = mock(SupportedOperatorsValidator.class);
        when(supportedOperatorsValidator.validate(null, clause)).thenReturn(new MessageSetImpl());

        final RawValuesExistValidator rawValuesExistValidator = mock(RawValuesExistValidator.class);
        when(rawValuesExistValidator.validate(null, clause)).thenReturn(new MessageSetImpl());

        final IssueTypeValidator issueTypeValidator = new IssueTypeValidator(null, mockJqlOperandResolver, null)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return supportedOperatorsValidator;
            }

            @Override
            RawValuesExistValidator getRawValuesValidator(final IssueTypeResolver issueTypeResolver, final JqlOperandResolver operandSupport)
            {
                return rawValuesExistValidator;
            }
        };

        issueTypeValidator.validate(null, clause);

        verify(supportedOperatorsValidator).validate(null, clause);
        verify(rawValuesExistValidator).validate(null, clause);
    }
}
