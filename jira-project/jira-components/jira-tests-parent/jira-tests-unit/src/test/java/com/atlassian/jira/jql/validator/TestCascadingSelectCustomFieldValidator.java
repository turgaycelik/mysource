package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCascadingSelectCustomFieldValidator
{
    private static final User ANONYMOUS = null;

    @Mock private CustomField customField;
    @Mock private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    @Mock private I18nHelper.BeanFactory beanFactory;

    private MockJqlOperandResolver jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
    private SupportedOperatorsValidator operatorsValidator;

    @Before
    public void setUp() throws Exception
    {
        when(beanFactory.getInstance(ANONYMOUS)).thenReturn(new MockI18nHelper());

        operatorsValidator = new SupportedOperatorsValidator()
        {
            @Override
            public MessageSet validate(final User searcher, final TerminalClause terminalClause)
            {
                return new MessageSetImpl();
            }
        };
    }

    @Test
    public void testValidateBadOperator() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, "blah");
        final MessageSetImpl messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("o no!!");

        final SupportedOperatorsValidator operatorsValidator = mock(SupportedOperatorsValidator.class);
        when(operatorsValidator.validate(ANONYMOUS, clause)).thenReturn(messageSet);

        final CascadingSelectCustomFieldValidator validator = new CascadingSelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = validator.validate(null, clause);
        assert1ErrorNoWarnings(result, "o no!!");
    }

    @Test
    public void testValidateEmptyOperand() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, EmptyOperand.EMPTY);

        final CascadingSelectCustomFieldValidator validator = new CascadingSelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = validator.validate(null, clause);
        assertNoMessages(result);
    }

    @Test
    public void testValidateLiteralHasNoOptions() throws Exception
    {
        final QueryLiteral literal = createLiteral("blah");
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, new SingleValueOperand(literal));
        when(jqlSelectOptionsUtil.getOptions(customField, ANONYMOUS, literal, true)).thenReturn(ImmutableList.<Option>of());

        final CascadingSelectCustomFieldValidator validator = new CascadingSelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = validator.validate(null, clause);
        assert1ErrorNoWarnings(result, "jira.jql.clause.select.option.does.not.exist [blah] [blah]");
    }

    @Test
    public void testValidateLiteralHasOneVisible() throws Exception
    {
        final QueryLiteral literal = createLiteral("blah");
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, new SingleValueOperand(literal));
        final Option option = new MockOption(null, null, null, null, null, 10L);
        when(jqlSelectOptionsUtil.getOptions(customField, ANONYMOUS, literal, true)).thenReturn(ImmutableList.of(option));

        final CascadingSelectCustomFieldValidator validator = new CascadingSelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = validator.validate(null, clause);
        assertNoMessages(result);
    }

    @Test
    public void testValidateNegativeLiteral() throws Exception
    {
        final QueryLiteral literal = createLiteral(-555L);
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.LESS_THAN, new SingleValueOperand(literal));
        final Option option1 = new MockOption(null, null, null, null, null, 10L);
        final Option option2 = new MockOption(null, null, null, null, null, 10L);

        when(jqlSelectOptionsUtil.getOptions(customField, ANONYMOUS, literal, true))
                .thenReturn(ImmutableList.of(option2));
        when(jqlSelectOptionsUtil.getOptions(customField, ANONYMOUS, createLiteral(-literal.getLongValue()), true))
                .thenReturn(ImmutableList.of(option1));

        final CascadingSelectCustomFieldValidator validator = new CascadingSelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return operatorsValidator;
            }
        };

        final MessageSet result = validator.validate(null, clause);
        assertNoMessages(result);
    }
}
