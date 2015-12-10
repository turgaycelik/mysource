package com.atlassian.jira.jql.validator;

import java.util.Collection;
import java.util.EnumSet;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.GroupCustomFieldIndexValueConverter;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestGroupCustomFieldValidator
{
    private static final User ANONYMOUS = null;

    @Mock private JqlOperandResolver jqlOperandResolver;
    @Mock private GroupCustomFieldIndexValueConverter groupCustomFieldIndexValueConverter;
    @Mock private IndexValuesValidator indexValuesValidator;

    @After
    public void tearDown()
    {
        jqlOperandResolver = null;
        groupCustomFieldIndexValueConverter = null;
        indexValuesValidator = null;
    }

    @Test
    public void testOperatorChecks() throws Exception
    {
        when(indexValuesValidator.validate(eq(ANONYMOUS), isA(TerminalClause.class))).thenReturn(new MessageSetImpl());

        final Collection<Operator> valid = EnumSet.of(Operator.EQUALS, Operator.IS, Operator.IN, Operator.IS_NOT, Operator.NOT_IN, Operator.NOT_EQUALS);
        final GroupCustomFieldValidator validator = new GroupCustomFieldValidator(jqlOperandResolver, groupCustomFieldIndexValueConverter)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }

            @Override
            IndexValuesValidator getIndexValuesValidator(final GroupCustomFieldIndexValueConverter groupCustomFieldIndexValueConverter)
            {
                return indexValuesValidator;
            }
        };

        for (Operator operator : valid)
        {
            final TerminalClauseImpl clause = new TerminalClauseImpl("group", operator, "blah");
            final MessageSet result = validator.validate(ANONYMOUS, clause);
            assertNoMessages(result);
        }
    }

    @Test
    public void testAddErrorFunction() throws Exception
    {
        final FunctionOperand fop = new FunctionOperand("function");
        final TerminalClause clause = new TerminalClauseImpl("field", Operator.EQUALS, fop);
        when(jqlOperandResolver.isFunctionOperand(clause.getOperand())).thenReturn(true);

        final GroupCustomFieldValidator validator = new GroupCustomFieldValidator(jqlOperandResolver, groupCustomFieldIndexValueConverter)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };


        final IndexValuesValidator indexValuesValidator = validator.getIndexValuesValidator(groupCustomFieldIndexValueConverter);
        final MessageSet messageSet = new MessageSetImpl();
        indexValuesValidator.addError(messageSet, null, clause, new QueryLiteral(fop, "group"));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.invalid.group.value.function [function] [field]");
    }

    @Test
    public void testAddErrorValue() throws Exception
    {
        final FunctionOperand fop = new FunctionOperand("function");
        final TerminalClause clause = new TerminalClauseImpl("field", Operator.EQUALS, "blah");
        final GroupCustomFieldValidator validator = new GroupCustomFieldValidator(jqlOperandResolver, groupCustomFieldIndexValueConverter)
        {
            @Override
            I18nHelper getI18n(final User user)
            {
                return new MockI18nHelper();
            }
        };

        final IndexValuesValidator indexValuesValidator = validator.getIndexValuesValidator(groupCustomFieldIndexValueConverter);
        final MessageSet messageSet = new MessageSetImpl();
        indexValuesValidator.addError(messageSet, null, clause, new QueryLiteral(fop, "group"));
        assert1ErrorNoWarnings(messageSet, "jira.jql.clause.invalid.group.value [field] [group]");
    }
}
