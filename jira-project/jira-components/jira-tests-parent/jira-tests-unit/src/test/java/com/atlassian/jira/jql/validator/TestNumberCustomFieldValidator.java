package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.NumberIndexValueConverter;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;
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
public class TestNumberCustomFieldValidator
{
    @Mock private I18nHelper.BeanFactory beanFactory;
    @Mock private JqlOperandResolver jqlOperandResolver;
    @Mock private NumberIndexValueConverter numberIndexValueConverter;

    @After
    public void tearDown()
    {
        beanFactory = null;
        jqlOperandResolver = null;
        numberIndexValueConverter = null;
    }

    @Test
    public void testAddErrorFunction() throws Exception
    {
        final FunctionOperand fop = new FunctionOperand("function");
        final TerminalClause clause = new TerminalClauseImpl("field", Operator.EQUALS, fop);
        when(jqlOperandResolver.isFunctionOperand(fop)).thenReturn(true);

        final NumberCustomFieldValidator validator = new NumberCustomFieldValidator(jqlOperandResolver, numberIndexValueConverter, beanFactory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };

        final IndexValuesValidator indexValuesValidator = validator.getIndexValuesValidator(numberIndexValueConverter);
        final MessageSet messageSet = new MessageSetImpl();
        indexValuesValidator.addError(messageSet, null, clause, new QueryLiteral(fop, "10a"));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.invalid.number.value.function [function] [field]");
    }

    @Test
    public void testAddErrorValue() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("10a");
        final TerminalClause clause = new TerminalClauseImpl("field", Operator.EQUALS, "blah");

        final NumberCustomFieldValidator validator = new NumberCustomFieldValidator(jqlOperandResolver, numberIndexValueConverter, beanFactory)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };


        final IndexValuesValidator indexValuesValidator = validator.getIndexValuesValidator(numberIndexValueConverter);
        final MessageSet messageSet = new MessageSetImpl();
        indexValuesValidator.addError(messageSet, null, clause, new QueryLiteral(operand, "10a"));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.invalid.number.value [field] [10a]");
    }
}
