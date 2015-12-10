package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.MockOperandHandler;
import com.atlassian.jira.jql.operand.OperandHandler;
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

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;

/**
 * @since v4.0
 */
public class TestIntegerValueValidator
{
    private static final User ANONYMOUS = null;
    private static final String FIELD_NAME = "field";
    
    private IntegerValueValidator validator;

    @Before
    public void setUp() throws Exception
    {
        validator = createValidator();
    }

    @After
    public void tearDown() throws Exception
    {
        validator = null;
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testNullArgsInConstructor() throws Exception
    {
        new IntegerValueValidator(null);
    }

    @Test
    public void testValidateLongs() throws Exception
    {
        assertValid(-999L);
        assertValid(0L);
        assertValid(999L);
    }

    @Test
    public void testValidateStrings() throws Exception
    {
        assertValid("-999");
        assertValid("0");
        assertValid("999");
        assertInvalid(" 999");
        assertInvalid("999 ");
        assertInvalid("-999.9");
        assertInvalid("0.0");
        assertInvalid("999.5");
        assertInvalid("99a");
        assertInvalid("a99");
        assertInvalid("aaa");
    }

    @Test
    public void testValidateEmptyOperand() throws Exception
    {
        validator = createValidator();

        final TerminalClauseImpl clause = new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, EmptyOperand.EMPTY);

        final MessageSet messageSet = validator.validate(null, clause);
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidateStringsFromFunction() throws Exception
    {
        final MockOperandHandler<?> handler = new MockOperandHandler(false, false, true);
        handler.add("0", "abc");
        validator = createValidator(handler);

        final MessageSet messageSet = validator.validate(null, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "ignored"));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.integer.format.invalid.from.func [SingleValueOperand] [field]");
    }

    private void assertValid(long operand)
    {
        final MessageSet messageSet = validator.validate(ANONYMOUS, new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, operand));
        assertNoMessages(messageSet);
    }

    private void assertValid(String operand)
    {
        final MessageSet messageSet = validator.validate(ANONYMOUS, createClause(operand));
        assertNoMessages(messageSet);
    }

    private void assertInvalid(String operand)
    {
        final MessageSet messageSet = validator.validate(ANONYMOUS, createClause(operand));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.integer.format.invalid [" + operand + "] [field]");
    }

    private static TerminalClauseImpl createClause(final String operand)
    {
        return new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, operand);
    }

    private static IntegerValueValidator createValidator(OperandHandler<?>... handlers)
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
        return new IntegerValueValidator(mockJqlOperandSupport)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };
    }
}
