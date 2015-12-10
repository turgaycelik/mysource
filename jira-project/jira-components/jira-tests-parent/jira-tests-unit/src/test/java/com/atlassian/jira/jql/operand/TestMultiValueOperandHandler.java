package com.atlassian.jira.jql.operand;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestMultiValueOperandHandler extends MockControllerTestCase
{
    private String field = "field";
    private User theUser = null;
    private QueryCreationContext queryCreationContext = new QueryCreationContextImpl(theUser);

    @Test
    public void testValidateWithError() throws Exception
    {
        final SingleValueOperand singleValueOperand1 = new SingleValueOperand("test");
        final SingleValueOperand singleValueOperand2 = new SingleValueOperand(12L);
        MultiValueOperand multiValueOperand = new MultiValueOperand(CollectionBuilder.newBuilder(singleValueOperand1, singleValueOperand2).asList());
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, multiValueOperand);

        final MessageSetImpl messageSet1 = new MessageSetImpl();
        messageSet1.addErrorMessage("Test Error 1");
        final MessageSetImpl messageSet2 = new MessageSetImpl();
        messageSet2.addErrorMessage("Test Error 2");

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.validate((com.atlassian.crowd.embedded.api.User) theUser, singleValueOperand1, clause);
        mockController.setReturnValue(messageSet1);
        jqlOperandResolver.validate((com.atlassian.crowd.embedded.api.User) theUser, singleValueOperand2, clause);
        mockController.setReturnValue(messageSet2);
        mockController.replay();

        MultiValueOperandHandler multiValueOperandHandler = new MultiValueOperandHandler(jqlOperandResolver);
        final MessageSet resultMessageSet = multiValueOperandHandler.validate(theUser, multiValueOperand, clause);

        assertTrue(resultMessageSet.hasAnyMessages());
        assertTrue(resultMessageSet.hasAnyErrors());
        assertFalse(resultMessageSet.hasAnyWarnings());
        assertTrue(resultMessageSet.getErrorMessages().contains("Test Error 1"));
        assertTrue(resultMessageSet.getErrorMessages().contains("Test Error 2"));

        mockController.verify();
    }

    @Test
    public void testValidateHappyPath() throws Exception
    {
        final SingleValueOperand singleValueOperand1 = new SingleValueOperand("test");
        final SingleValueOperand singleValueOperand2 = new SingleValueOperand(12L);
        MultiValueOperand multiValueOperand = new MultiValueOperand(CollectionBuilder.newBuilder(singleValueOperand1, singleValueOperand2).asList());
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, multiValueOperand);

        final MessageSetImpl messageSet1 = new MessageSetImpl();
        final MessageSetImpl messageSet2 = new MessageSetImpl();

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.validate((com.atlassian.crowd.embedded.api.User) theUser, singleValueOperand1, clause);
        mockController.setReturnValue(messageSet1);
        jqlOperandResolver.validate((com.atlassian.crowd.embedded.api.User) theUser, singleValueOperand2, clause);
        mockController.setReturnValue(messageSet2);
        mockController.replay();


        MultiValueOperandHandler multiValueOperandHandler = new MultiValueOperandHandler(jqlOperandResolver);
        final MessageSet resultMessageSet = multiValueOperandHandler.validate(theUser, multiValueOperand, clause);

        assertFalse(resultMessageSet.hasAnyMessages());

        mockController.verify();
    }

    @Test
    public void testGetValuesStrings()
    {
        MultiValueOperand multiValueOperand = new MultiValueOperand("fred", "jane", "jimi", "123");
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, multiValueOperand);

        MultiValueOperandHandler mockMultiValueOperandHandler = new MultiValueOperandHandler(MockJqlOperandResolver.createSimpleSupport());
        List<QueryLiteral> values = mockMultiValueOperandHandler.getValues(queryCreationContext, multiValueOperand, clause);

        final List<QueryLiteral> expectedList = CollectionBuilder.newBuilder(createLiteral("fred"), createLiteral("jane"), createLiteral("jimi"), createLiteral("123")).asList();
        assertEquals(expectedList, values);
    }

    @Test
    public void testGetValuesLongs()
    {
        MultiValueOperand multiValueOperand = new MultiValueOperand(11L, 1L, 0L, 9999L);
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, multiValueOperand);

        MultiValueOperandHandler mockMultiValueOperandHandler = new MultiValueOperandHandler(MockJqlOperandResolver.createSimpleSupport());
        List<QueryLiteral> values = mockMultiValueOperandHandler.getValues(queryCreationContext, multiValueOperand, clause);

        final List<QueryLiteral> expectedList = CollectionBuilder.newBuilder(createLiteral(11L), createLiteral(1L), createLiteral(0L), createLiteral(9999L)).asList();

        assertEquals(expectedList, values);
    }

    @Test
    public void testGetValuesMixture()
    {
        final SingleValueOperand operandStringy = new SingleValueOperand("stringy");
        final SingleValueOperand operand2010 = new SingleValueOperand(2010L);
        final MultiValueOperand operandMulti1 = new MultiValueOperand(CollectionBuilder.newBuilder(new SingleValueOperand("substring svo"), new SingleValueOperand(333L)).asList());
        final MultiValueOperand operandMulti2 = new MultiValueOperand("sublist", "another");
        List<? extends Operand> operandValues = CollectionBuilder.newBuilder(operandStringy, operand2010, operandMulti1, operandMulti2).asList();

        MultiValueOperand multiValueOperand = new MultiValueOperand(operandValues);
        TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, multiValueOperand);

        MultiValueOperandHandler multiValueOperandHandler = new MultiValueOperandHandler(MockJqlOperandResolver.createSimpleSupport());
        List<QueryLiteral> values = multiValueOperandHandler.getValues(queryCreationContext, multiValueOperand, clause);

        final List<QueryLiteral> expectedList = CollectionBuilder.newBuilder(createLiteral("stringy"), createLiteral(2010L), createLiteral("substring svo"), createLiteral(333L), createLiteral("sublist"), createLiteral("another")).asList();
        assertEquals("sublists should be flattened out into one big list", expectedList, values);
    }

    @Test
    public void testIsList() throws Exception
    {
        MultiValueOperandHandler multiValueOperandHandler = mockController.instantiate(MultiValueOperandHandler.class);
        assertTrue(multiValueOperandHandler.isList());
    }

    @Test
    public void testIsEmpty() throws Exception
    {
        MultiValueOperandHandler multiValueOperandHandler = mockController.instantiate(MultiValueOperandHandler.class);
        assertFalse(multiValueOperandHandler.isEmpty());
    }
    
    @Test
    public void testIsFunction() throws Exception
    {
        MultiValueOperandHandler multiValueOperandHandler = mockController.instantiate(MultiValueOperandHandler.class);
        assertFalse(multiValueOperandHandler.isFunction());
    }
}
