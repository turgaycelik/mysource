package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlTimetrackingDurationSupport;
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
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractTimeTrackingValidator
{
    private static final User ANONYMOUS = null;

    @Mock private ApplicationProperties applicationProperties;
    @Mock private JqlTimetrackingDurationSupport jqlTimetrackingDurationSupport;
    @Mock private SupportedOperatorsValidator supportedOperatorsValidator;
    @Mock private PositiveDurationValueValidator positiveDurationValueValidator;

    private MockJqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

    @After
    public void tearDown()
    {
        applicationProperties = null;
        jqlTimetrackingDurationSupport = null;
        supportedOperatorsValidator = null;
        positiveDurationValueValidator = null;
    }

    @Test
    public void testTimeTrackingOff() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);

        final AbstractTimeTrackingValidator validator = new Fixture();
        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assert1ErrorNoWarnings(result, "jira.jql.clause.timetracking.disabled [test]");
    }

    @Test
    public void testPositiveDurationDelegateNotCalledWithOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("blah blah");

        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(messageSet);
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).thenReturn(true);

        final AbstractTimeTrackingValidator abstractTimeTrackingValidator = new Fixture();
        abstractTimeTrackingValidator.validate(ANONYMOUS, clause);
    }

    @Test
    public void testPositiveDurationDelegateCalledWithNoOperatorProblem() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("test", Operator.GREATER_THAN, 12L);
        final MessageSetImpl messageSet = new MessageSetImpl();

        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(messageSet);
        when(positiveDurationValueValidator.validate(ANONYMOUS, clause)).thenReturn(messageSet);
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).thenReturn(true);

        final AbstractTimeTrackingValidator abstractTimeTrackingValidator = new Fixture();
        abstractTimeTrackingValidator.validate(ANONYMOUS, clause);
    }

    class Fixture extends AbstractTimeTrackingValidator
    {
        Fixture()
        {
            super(jqlOperandResolver, applicationProperties, jqlTimetrackingDurationSupport);
        }

        @Override
        SupportedOperatorsValidator getSupportedOperatorsValidator()
        {
            return supportedOperatorsValidator;
        }

        @Override
        PositiveDurationValueValidator getPositiveDurationValueValidator(final JqlOperandResolver operandSupport, JqlTimetrackingDurationSupport timetrackingDurationSupport)
        {
            return positiveDurationValueValidator;
        }

        @Override
        I18nHelper getI18n(final User user)
        {
            return new MockI18nHelper();
        }
    }
}
