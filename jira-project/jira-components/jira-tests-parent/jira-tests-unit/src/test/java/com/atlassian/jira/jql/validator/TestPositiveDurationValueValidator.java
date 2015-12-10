package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.MockOperandHandler;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.util.JqlTimetrackingDurationSupport;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPositiveDurationValueValidator
{
    private static final User ANONYMOUS = null;
    private static final String FIELD_NAME = "field";

    @Mock private JqlTimetrackingDurationSupport converter;

    private PositiveDurationValueValidator validator;

    @Before
    public void setUp()
    {
        validator = createValidator();
    }

    @After
    public void tearDown() throws Exception
    {
        converter = null;
        validator = null;
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testNullArgInConstructor()
    {
        new IntegerValueValidator(null);
    }

    @Test
    public void testValidateLongs() throws Exception
    {
        assertInvalid(-999L);
        assertValid(0L);
        assertValid(999L);
    }

    @Test
    public void testValidateValidStrings() throws Exception
    {
        when(converter.validate("-999")).thenReturn(true);
        assertValid("-999");
    }

    @Test
    public void testValidateInvalidStrings() throws Exception
    {
        assertInvalid("-999");
        assertInvalid("Fred");
    }

    @Test
    public void testValidateStringsFromFunction() throws Exception
    {
        final MockOperandHandler<?> handler = new MockOperandHandler(false, false, true);
        handler.add("0", "-35m");
        validator = createValidator(handler);

        final MessageSet messageSet = validator.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.positive.duration.format.invalid.from.func [SingleValueOperand] [field]");
    }

    @Test
    public void testValidateEmptyLiteral() throws Exception
    {
        validator = createValidator();

        final MessageSet messageSet = validator.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.IS, EmptyOperand.EMPTY));
        assertFalse(messageSet.hasAnyMessages());
    }

    private void assertValid(long operand)
    {
        final MessageSet messageSet = validator.validate(ANONYMOUS, createClause(operand));
        assertNoMessages(messageSet);
    }

    private void assertValid(String operand)
    {
        final MessageSet messageSet = validator.validate(ANONYMOUS, createClause(operand));
        assertNoMessages(messageSet);
    }

    private void assertInvalid(long operand)
    {
        final MessageSet messageSet = validator.validate(ANONYMOUS, createClause(operand));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.positive.duration.format.invalid [" + operand + "] [field]");
    }

    private void assertInvalid(String operand)
    {
        final MessageSet messageSet = validator.validate(ANONYMOUS, createClause(operand));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.positive.duration.format.invalid [" + operand + "] [field]");
    }

    private static TerminalClauseImpl createClause(final String operand)
    {
        return new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, operand);
    }

    private static TerminalClauseImpl createClause(final long operand)
    {
        return new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, operand);
    }

    private PositiveDurationValueValidator createValidator(OperandHandler<?>... handlers)
    {
        final MockJqlOperandResolver mockJqlOperandSupport;
        if (handlers == null || handlers.length == 0)
        {
            mockJqlOperandSupport = MockJqlOperandResolver.createSimpleSupport();
        }
        else
        {
            mockJqlOperandSupport = new MockJqlOperandResolver();
            OperandHandler<?> handler = handlers[0];
            mockJqlOperandSupport.addHandler(SingleValueOperand.OPERAND_NAME, handler);
            mockJqlOperandSupport.addHandler(MultiValueOperand.OPERAND_NAME, handler);
        }

        return new PositiveDurationValueValidator(mockJqlOperandSupport, converter)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };
    }
}
