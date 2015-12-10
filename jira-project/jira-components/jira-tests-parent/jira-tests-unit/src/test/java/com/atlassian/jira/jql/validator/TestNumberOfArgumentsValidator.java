package com.atlassian.jira.jql.validator;

import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.operand.FunctionOperand;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v4.0
 */
public class TestNumberOfArgumentsValidator
{
    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new NumberOfArgumentsValidator(-1, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new NumberOfArgumentsValidator(0, -1, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new NumberOfArgumentsValidator(1, 0, new MockI18nBean());
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new NumberOfArgumentsValidator(0, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testValidateExact() throws Exception
    {
        final NumberOfArgumentsValidator validator = new NumberOfArgumentsValidator(0, new MockI18nBean());

        FunctionOperand functionOperand = new FunctionOperand("blah");
        MessageSet messageSet = validator.validate(functionOperand);
        assertFalse(messageSet.hasAnyErrors());

        functionOperand = new FunctionOperand("blah", "1", "2");
        messageSet = validator.validate(functionOperand);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'blah' expected '0' arguments but received '2'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateRange() throws Exception
    {
        final NumberOfArgumentsValidator validator = new NumberOfArgumentsValidator(1, 3, new MockI18nBean());

        FunctionOperand functionOperand = new FunctionOperand("blah", "1");
        MessageSet messageSet = validator.validate(functionOperand);
        assertFalse(messageSet.hasAnyErrors());

        functionOperand = new FunctionOperand("blah", "1", "2");
        messageSet = validator.validate(functionOperand);
        assertFalse(messageSet.hasAnyErrors());

        functionOperand = new FunctionOperand("blah", "1", "2", "3");
        messageSet = validator.validate(functionOperand);
        assertFalse(messageSet.hasAnyErrors());

        functionOperand = new FunctionOperand("blah");
        messageSet = validator.validate(functionOperand);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'blah' expected between '1' and '3' arguments but received '0'.", messageSet.getErrorMessages().iterator().next());

        functionOperand = new FunctionOperand("blah", "1", "2", "3", "4");
        messageSet = validator.validate(functionOperand);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'blah' expected between '1' and '3' arguments but received '4'.", messageSet.getErrorMessages().iterator().next());
    }
}
