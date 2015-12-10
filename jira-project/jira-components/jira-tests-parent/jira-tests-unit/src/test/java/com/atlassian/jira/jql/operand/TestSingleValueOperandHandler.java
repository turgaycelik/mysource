package com.atlassian.jira.jql.operand;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestSingleValueOperandHandler
{
    private User theUser = null;
    private QueryCreationContext queryCreationContext = new QueryCreationContextImpl(theUser);

    @Test
    public void testIsEmpty() throws Exception
    {
        assertFalse(new SingleValueOperandHandler().isEmpty());
    }

    @Test
    public void testIsList() throws Exception
    {
        assertFalse(new SingleValueOperandHandler().isList());
    }

    @Test
    public void testIsFunction() throws Exception
    {
        assertFalse(new SingleValueOperandHandler().isFunction());
    }

    @Test
    public void testValidate() throws Exception
    {
        final SingleValueOperandHandler operandHandler = new SingleValueOperandHandler();
        assertNotNull(operandHandler.validate(null, null, null));
        assertFalse(operandHandler.validate(null, null, null).hasAnyMessages());
    }

    @Test
    public void testGetValues() throws Exception
    {
        final SingleValueOperandHandler operandHandler = new SingleValueOperandHandler();

        SingleValueOperand operand = new SingleValueOperand("test");
        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.EQUALS, operand);
        List<QueryLiteral> list = operandHandler.getValues(queryCreationContext, operand, terminalClause);
        assertEquals(1, list.size());
        assertEquals("test", list.iterator().next().getStringValue());
        assertNull(list.iterator().next().getLongValue());

        operand = new SingleValueOperand(10L);
        terminalClause = new TerminalClauseImpl("field", Operator.EQUALS, operand);
        list = operandHandler.getValues(queryCreationContext, operand, terminalClause);
        assertEquals(1, list.size());
        assertEquals(new Long(10), list.iterator().next().getLongValue());
        assertNull(list.iterator().next().getStringValue());
    }
}
