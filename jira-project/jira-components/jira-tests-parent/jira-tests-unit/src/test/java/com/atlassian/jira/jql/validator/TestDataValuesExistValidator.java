package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link com.atlassian.jira.jql.validator.DataValuesExistValidator}.
 *
 * @since v4.0
 */
@SuppressWarnings("ResultOfObjectAllocationIgnored")
@RunWith(MockitoJUnitRunner.class)
public class TestDataValuesExistValidator
{
    private static final User ANONYMOUS = null;
    private static final String VALUE = "value";
    private static final String VALUE1 = "value1";
    private static final String CLAUSE = "clause";

    @Mock I18nHelper.BeanFactory beanFactory;
    @Mock NameResolver<?> nameResolver;
    @Mock OperandHandler<SingleValueOperand> operandHandler;

    @After
    public void tearDown()
    {
        beanFactory = null;
        nameResolver = null;
        operandHandler = null;
    }

    @Test
    public void testLookupFailureStringValue()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(VALUE);

        DataValuesExistValidator clauseValidator = new Fixture();

        TerminalClause priorityClause = new TerminalClauseImpl(CLAUSE, Operator.EQUALS, singleValueOperand);
        final MessageSet messages = clauseValidator.validate(ANONYMOUS, priorityClause);
        assert1ErrorNoWarnings(messages, "The value '" + VALUE + "' does not exist for the field '" + CLAUSE + "'.");
    }

    @Test
    public void testLookupFailureStringValueFromFunction()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(VALUE);
        TerminalClause priorityClause = new TerminalClauseImpl(CLAUSE, Operator.EQUALS, singleValueOperand);

        when(operandHandler.getValues(any(QueryCreationContext.class), eq(singleValueOperand), eq(priorityClause)))
                        .thenReturn(ImmutableList.of(createLiteral(VALUE)));
        when(operandHandler.isFunction()).thenReturn(true);

        final JqlOperandResolver mockJqlOperandResolver = new MockJqlOperandResolver().addHandler(SingleValueOperand.OPERAND_NAME, operandHandler);
        DataValuesExistValidator clauseValidator = new Fixture(mockJqlOperandResolver);

        final MessageSet messages = clauseValidator.validate(ANONYMOUS, priorityClause);
        assert1ErrorNoWarnings(messages, "A value provided by the function 'SingleValueOperand' is invalid for the field '" + CLAUSE + "'.");
    }

    @Test
    public void testLookupFailureLongValue()
    {
        final long id = 12345L;
        final SingleValueOperand singleValueOperand = new SingleValueOperand(id);

        DataValuesExistValidator clauseValidator = new Fixture();
        TerminalClause priorityClause = new TerminalClauseImpl(CLAUSE, Operator.EQUALS, singleValueOperand);
        final MessageSet messages = clauseValidator.validate(ANONYMOUS, priorityClause);
        assert1ErrorNoWarnings(messages, "A value with ID '" + id + "' does not exist for the field '" + CLAUSE + "'.");
    }

    @Test
    public void testLookupFailureLongValueFromFunction()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(12345L);
        TerminalClause priorityClause = new TerminalClauseImpl(CLAUSE, Operator.EQUALS, singleValueOperand);

        when(operandHandler.getValues(isA(QueryCreationContext.class), eq(singleValueOperand), eq(priorityClause)))
                .thenReturn(ImmutableList.of(createLiteral(12345L)));
        when(operandHandler.isFunction()).thenReturn(true);

        final JqlOperandResolver mockJqlOperandResolver = new MockJqlOperandResolver().addHandler(SingleValueOperand.OPERAND_NAME, operandHandler);
        DataValuesExistValidator clauseValidator = new Fixture(mockJqlOperandResolver);

        final MessageSet messages = clauseValidator.validate(ANONYMOUS, priorityClause);
        assert1ErrorNoWarnings(messages, "A value provided by the function 'SingleValueOperand' is invalid for the field '" + CLAUSE + "'.");
    }

    @Test
    public void testLookupLongAsName()
    {
        final MultiValueOperand operand = new MultiValueOperand(111L, 123L);
        TerminalClause priorityClause = new TerminalClauseImpl(CLAUSE, Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mock(JqlOperandResolver.class);
        when(jqlOperandResolver.isValidOperand(operand)).thenReturn(true);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, priorityClause)).thenReturn(ImmutableList.of(
                createLiteral(111L),
                createLiteral(123L)));
        when(nameResolver.idExists(111L)).thenReturn(true);
        when(nameResolver.idExists(123L)).thenReturn(true);

        DataValuesExistValidator clauseValidator = new Fixture(jqlOperandResolver);
        MessageSet errorCollection = clauseValidator.validate(ANONYMOUS, priorityClause);
        assertNoMessages(errorCollection);
    }

    @Test
    public void testNoOperandHandler()
    {
        final JqlOperandResolver jqlOperandResolver = mock(JqlOperandResolver.class);

        DataValuesExistValidator clauseValidator = new Fixture(jqlOperandResolver);
        TerminalClause priorityClause = new TerminalClauseImpl(CLAUSE, Operator.IN, new MultiValueOperand(VALUE, VALUE1));
        MessageSet errorCollection = clauseValidator.validate(ANONYMOUS, priorityClause);
        assertFalse(errorCollection.hasAnyMessages());
    }

    @Test
    public void testHappyPath()
    {
        final MultiValueOperand operand = new MultiValueOperand(VALUE, VALUE1);
        TerminalClause priorityClause = new TerminalClauseImpl(CLAUSE, Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mock(JqlOperandResolver.class);
        when(jqlOperandResolver.isValidOperand(operand)).thenReturn(true);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, priorityClause)).thenReturn(ImmutableList.of(
                createLiteral(VALUE),
                createLiteral(VALUE1)));
        when(nameResolver.nameExists(VALUE)).thenReturn(true);
        when(nameResolver.nameExists(VALUE1)).thenReturn(true);

        DataValuesExistValidator clauseValidator = new Fixture(jqlOperandResolver);
        MessageSet errorCollection = clauseValidator.validate(ANONYMOUS, priorityClause);
        assertNoMessages(errorCollection);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNameResolver()
    {
        new DataValuesExistValidator(mock(JqlOperandResolver.class), null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullOperandResolver()
    {
        new DataValuesExistValidator(null, nameResolver, null);
    }



    class Fixture extends DataValuesExistValidator
    {
        Fixture()
        {
            this(MockJqlOperandResolver.createSimpleSupport());
        }

        Fixture(JqlOperandResolver jqlOperandResolver)
        {
            super(jqlOperandResolver, nameResolver, beanFactory);
        }

        @Override
        protected I18nHelper getI18n(final User user)
        {
            return new MockI18nBean();
        }
    }
}
