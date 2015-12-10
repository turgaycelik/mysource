package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoErrors;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWorkRatioValidator
{
    private static final User ANONYMOUS = null;

    @Mock private ApplicationProperties applicationProperties;
    @Mock private SupportedOperatorsValidator supportedOperatorsValidator;
    @Mock private IntegerValueValidator integerValueValidator;

    @After
    public void tearDown()
    {
        applicationProperties = null;
        supportedOperatorsValidator = null;
        integerValueValidator = null;
    }

    @Test
    public void testTimeTrackingOff() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);
        final WorkRatioValidator workRatioValidator = new Fixture();

        final MessageSet result = workRatioValidator.validate(ANONYMOUS, clause);
        assert1ErrorNoWarnings(result, "jira.jql.clause.timetracking.disabled [test]");
    }
    
    @Test
    public void testPositiveDurationDelegateNotCalledWithOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("blah blah");

        when(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).thenReturn(true);
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(messageSet);

        final WorkRatioValidator workRatioValidator = new Fixture();
        final MessageSet result = workRatioValidator.validate(ANONYMOUS, clause);
        assert1ErrorNoWarnings(result, "blah blah");
        verify(supportedOperatorsValidator).validate(ANONYMOUS, clause);
        verifyZeroInteractions(integerValueValidator);
    }

    @Test
    public void testPositiveDurationDelegateCalledWithNoOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);
        final MessageSetImpl messageSet = new MessageSetImpl();

        when(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).thenReturn(true);
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(messageSet);
        when(integerValueValidator.validate(ANONYMOUS, clause)).thenReturn(messageSet);

        final WorkRatioValidator workRatioValidator = new Fixture();
        final MessageSet result = workRatioValidator.validate(ANONYMOUS, clause);
        assertNoErrors(result);
        verify(supportedOperatorsValidator).validate(ANONYMOUS, clause);
        verify(integerValueValidator).validate(ANONYMOUS, clause);
    }

    class Fixture extends WorkRatioValidator
    {
        Fixture()
        {
            super(new MockJqlOperandResolver(), applicationProperties);
        }

        @Override
        SupportedOperatorsValidator getSupportedOperatorsValidator()
        {
            return supportedOperatorsValidator;
        }

        @Override
        IntegerValueValidator getIntegerValueValidator(final JqlOperandResolver operandResolver)
        {
            return integerValueValidator;
        }

        @Override
        I18nHelper getI18n(final User user)
        {
            return new MockI18nHelper();
        }
    }
}
