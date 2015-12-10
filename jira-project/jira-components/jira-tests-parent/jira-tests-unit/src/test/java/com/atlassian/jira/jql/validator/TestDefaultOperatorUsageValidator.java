package com.atlassian.jira.jql.validator;

import java.util.Collections;

import com.atlassian.jira.jql.operand.MultiValueOperandHandler;
import com.atlassian.jira.jql.operand.SingleValueOperandHandler;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestDefaultOperatorUsageValidator
{
    @Test
    public void testInOperatorWithNoListOperand() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand("testFunc", Collections.<String>emptyList());

        final MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver().addHandler("testFunc", new SingleValueOperandHandler());

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(jqlOperandSupport, new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.IN, functionOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals("Operator 'in' does not support the non-list value 'testFunc()' for field 'testField'.", messageSet.getErrorMessages().iterator().next());
        assertFalse(operatorUsageValidator.check(null, clause));
    }

    @Test
    public void testNotInOperatorWithNoListOperand() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand("testFunc", Collections.<String>emptyList());

        final MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver().addHandler("testFunc", new SingleValueOperandHandler());

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(jqlOperandSupport, new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.NOT_IN, functionOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals("Operator 'not in' does not support the non-list value 'testFunc()' for field 'testField'.", messageSet.getErrorMessages().iterator().next());
        assertFalse(operatorUsageValidator.check(null, clause));
    }

    @Test
    public void testNonInOperatorWithListOperand() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand("testFunc", Collections.<String>emptyList());

        final MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler("testFunc", new MultiValueOperandHandler(jqlOperandSupport));

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(jqlOperandSupport, new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.EQUALS, functionOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals("Operator '=' does not support the list value 'testFunc()' for field 'testField'.", messageSet.getErrorMessages().iterator().next());
        assertFalse(operatorUsageValidator.check(null, clause));
    }

    @Test
    public void testUnsupportedOperatorsWithEmptyOperand() throws Exception
    {
        _testUnsupportedOperatorWithEmptyOperand(Operator.IN);
        _testUnsupportedOperatorWithEmptyOperand(Operator.NOT_IN);
        _testUnsupportedOperatorWithEmptyOperand(Operator.GREATER_THAN);
        _testUnsupportedOperatorWithEmptyOperand(Operator.GREATER_THAN_EQUALS);
        _testUnsupportedOperatorWithEmptyOperand(Operator.LESS_THAN);
        _testUnsupportedOperatorWithEmptyOperand(Operator.LESS_THAN_EQUALS);
    }

    @Test
    public void testSupportedOperatorsWithEmptyOperand() throws Exception
    {
        _testSupportedOperatorWithEmptyOperand(Operator.EQUALS);
        _testSupportedOperatorWithEmptyOperand(Operator.NOT_EQUALS);
        _testSupportedOperatorWithEmptyOperand(Operator.LIKE);
        _testSupportedOperatorWithEmptyOperand(Operator.NOT_LIKE);
        _testSupportedOperatorWithEmptyOperand(Operator.IS);
        _testSupportedOperatorWithEmptyOperand(Operator.IS_NOT);
    }

    private void _testSupportedOperatorWithEmptyOperand(Operator operator)
    {
        final EmptyOperand emptyOperand = new EmptyOperand();

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(MockJqlOperandResolver.createSimpleSupport(),
                new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", operator, emptyOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertFalse(messageSet.hasAnyMessages());
        assertTrue(operatorUsageValidator.check(null, clause));
    }

    private void _testUnsupportedOperatorWithEmptyOperand(Operator operator)
    {
        final EmptyOperand emptyOperand = new EmptyOperand();

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(MockJqlOperandResolver.createSimpleSupport(),
                new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", operator, emptyOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals("Operator '" + operator.getDisplayString() + "' does not support the empty value representation 'EMPTY' for field 'testField'.", messageSet.getErrorMessages().iterator().next());
        assertFalse(operatorUsageValidator.check(null, clause));
    }

    @Test
    public void testInHappyPath() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand("testFunc", Collections.<String>emptyList());

        final MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler("testFunc", new MultiValueOperandHandler(jqlOperandSupport));

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(jqlOperandSupport,
                new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.IN, functionOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertFalse(messageSet.hasAnyMessages());
        assertTrue(operatorUsageValidator.check(null, clause));
    }

    @Test
    public void testNotInHappyPath() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand("testFunc", Collections.<String>emptyList());

        final MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler("testFunc", new MultiValueOperandHandler(jqlOperandSupport));

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(jqlOperandSupport,
                new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.NOT_IN, functionOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertFalse(messageSet.hasAnyMessages());
        assertTrue(operatorUsageValidator.check(null, clause));
    }

    @Test
    public void testOtherOperatorsHappyPath() throws Exception
    {
        final FunctionOperand functionOperand = new FunctionOperand("testFunc", Collections.<String>emptyList());

        final MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler("testFunc", new SingleValueOperandHandler());

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(jqlOperandSupport,
                new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.GREATER_THAN, functionOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertFalse(messageSet.hasAnyMessages());
        assertTrue(operatorUsageValidator.check(null, clause));
    }

    @Test
    public void testIsOperatorNotEmptyOperand() throws Exception
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("test");

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(MockJqlOperandResolver.createSimpleSupport(),
                new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.IS, singleValueOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertTrue(messageSet.hasAnyErrors());
        assertEquals("Operator 'is' does not support searching for non-empty values for field 'testField'.", messageSet.getErrorMessages().iterator().next());
        assertFalse(operatorUsageValidator.check(null, clause));
    }

    @Test
    public void testIsNotOperatorNotEmptyOperand() throws Exception
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("test");

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(MockJqlOperandResolver.createSimpleSupport(),
                new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.IS_NOT, singleValueOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertTrue(messageSet.hasAnyErrors());
        assertEquals("Operator 'is not' does not support searching for non-empty values for field 'testField'.", messageSet.getErrorMessages().iterator().next());
        assertFalse(operatorUsageValidator.check(null, clause));
    }

    @Test
    public void testRelationalOperatorWithEmptyOperand() throws Exception
    {
        final EmptyOperand emptyOperand = new EmptyOperand();

        DefaultOperatorUsageValidator operatorUsageValidator = new DefaultOperatorUsageValidator(MockJqlOperandResolver.createSimpleSupport(),
                new MockI18nBean.MockI18nBeanFactory());

        final TerminalClauseImpl clause = new TerminalClauseImpl("testField", Operator.GREATER_THAN, emptyOperand);
        final MessageSet messageSet = operatorUsageValidator.validate(null, clause);

        assertTrue(messageSet.hasAnyErrors());
        assertEquals("Operator '>' does not support the empty value representation 'EMPTY' for field 'testField'.", messageSet.getErrorMessages().iterator().next());
        assertFalse(operatorUsageValidator.check(null, clause));
    }
}
