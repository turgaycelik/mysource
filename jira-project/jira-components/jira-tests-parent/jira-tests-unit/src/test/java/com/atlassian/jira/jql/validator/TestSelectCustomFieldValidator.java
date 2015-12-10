package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSelectCustomFieldValidator
{
    private static final String FIELD = "field";
    private static final User ANONYMOUS = null;

    @Mock private CustomField customField;
    @Mock private SupportedOperatorsValidator supportedOperatorsValidator;
    @Mock private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    @Mock private JqlOperandResolver jqlOperandResolver;
    @Mock private I18nHelper.BeanFactory beanFactory;

    private Operand operand;
    private TerminalClauseImpl clause;

    @Before
    public void setUp()
    {
        when(beanFactory.getInstance(ANONYMOUS)).thenReturn(new MockI18nHelper());

        operand = new SingleValueOperand("value");
        clause = new TerminalClauseImpl(FIELD, Operator.LESS_THAN_EQUALS, operand);
    }

    @After
    public void tearDown()
    {
        customField = null;
        supportedOperatorsValidator = null;
        jqlSelectOptionsUtil = null;
        jqlOperandResolver = null;
        beanFactory = null;

        operand = null;
        clause = null;
    }

    @Test
    public void testValidateInvalidOperator() throws Exception
    {
        final MessageSet messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("Nope!");
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(messageSet);

        final SelectCustomFieldValidator validator = fixture();
        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assert1ErrorNoWarnings(result, "Nope!");
    }

    @Test
    public void testValidateNoValues() throws Exception
    {
        final MessageSetImpl emptyMessageSet = new MessageSetImpl();
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(emptyMessageSet);

        jqlOperandResolver = mock(JqlOperandResolver.class);
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.<QueryLiteral>of());

        final SelectCustomFieldValidator validator = fixture();
        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assertNoMessages(result);
    }

    @Test
    public void testValidateNullValues() throws Exception
    {
        final MessageSetImpl emptyMessageSet = new MessageSetImpl();
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(emptyMessageSet);
        jqlOperandResolver = mock(JqlOperandResolver.class);

        final SelectCustomFieldValidator validator = fixture();
        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assertNoMessages(result);
    }

    @Test
    public void testValidateSearcherNoVisibleOptions() throws Exception
    {
        final QueryLiteral literal = createLiteral("value");
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(new MessageSetImpl());
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(literal));
        when(jqlSelectOptionsUtil.getOptions(customField, ANONYMOUS, literal, false)).thenReturn(ImmutableList.<Option>of());

        final SelectCustomFieldValidator validator = fixture();
        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assert1ErrorNoWarnings(result,
                "jira.jql.clause.select.option.does.not.exist [value] [" + FIELD + ']');
    }

    @Test
    public void testValidateSearcherNoVisibleOptionsFunction() throws Exception
    {
        final QueryLiteral literal = createLiteral("value");
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(new MessageSetImpl());
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(literal));
        when(jqlOperandResolver.isFunctionOperand(operand)).thenReturn(true);
        when(jqlSelectOptionsUtil.getOptions(customField, ANONYMOUS, literal, false)).thenReturn(ImmutableList.<Option>of());

        final SelectCustomFieldValidator validator = fixture();
        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assert1ErrorNoWarnings(result,
                "jira.jql.clause.select.option.does.not.exist.function [SingleValueOperand] [" + FIELD + ']');
    }

    @Test
    public void testValidateSearcherOneVisibleOption() throws Exception
    {
        final QueryLiteral literal = createLiteral("value1");
        final Option option1 = new MockOption(null, null, null, "value", null, 10L);
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(new MessageSetImpl());
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(literal));
        when(jqlSelectOptionsUtil.getOptions(customField, ANONYMOUS, literal, false)).thenReturn(ImmutableList.of(option1));

        final SelectCustomFieldValidator validator = fixture();
        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assertNoMessages(result);
    }

    @Test
    public void testValidateSearcherOneEmpty() throws Exception
    {
        final QueryLiteral literal = createLiteral("value1");
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(new MessageSetImpl());
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(literal));
        when(jqlSelectOptionsUtil.getOptions(customField, ANONYMOUS, literal, false)).thenReturn(asList((Option) null));

        final SelectCustomFieldValidator validator = fixture();
        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assertNoMessages(result);
    }

    @Test
    public void testValidateSearcherOneEmptyLiteral() throws Exception
    {
        final QueryLiteral literal = new QueryLiteral();
        when(supportedOperatorsValidator.validate(ANONYMOUS, clause)).thenReturn(new MessageSetImpl());
        when(jqlOperandResolver.getValues(ANONYMOUS, operand, clause)).thenReturn(ImmutableList.of(literal));

        final SelectCustomFieldValidator validator = fixture();
        final MessageSet result = validator.validate(ANONYMOUS, clause);
        assertNoMessages(result);
    }

    private SelectCustomFieldValidator fixture()
    {
        return new SelectCustomFieldValidator(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory)
        {
            @Override
            SupportedOperatorsValidator getSupportedOperatorsValidator()
            {
                return supportedOperatorsValidator;
            }
        };
    }
}
