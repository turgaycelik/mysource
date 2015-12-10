package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.MockOperandHandler;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.queryParser.QueryParser;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v4.0
 */
public class TestFreeTextFieldValidator extends MockControllerTestCase
{
    private QueryParser queryParser;

    @Before
    public void setUp() throws Exception
    {
        queryParser = mockController.getMock(QueryParser.class);
    }

    @Test
    public void testValidateEmpty() throws Exception
    {
        ClauseValidator validator = new FreeTextFieldValidator("indexField", MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.IS, EmptyOperand.EMPTY);

        replay();

        MessageSet messageSet = validator.validate(null, terminalClause);
        assertFalse(messageSet.hasAnyMessages());
    }
    
    @Test
    public void testValidateHappyPath() throws Exception
    {
        EasyMock.expect(queryParser.parse((String) EasyMock.notNull()))
                .andStubReturn(null);

        replay();

        _testValidateHappyPathForOperator(Operator.LIKE);
        _testValidateHappyPathForOperator(Operator.NOT_LIKE);
        _testValidateHappyPathForOperator(Operator.IS);
        _testValidateHappyPathForOperator(Operator.IS_NOT);

        verify();
    }

    private void _testValidateHappyPathForOperator(final Operator operator)
    {
        ClauseValidator validator = new FreeTextFieldValidator("indexField", MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return queryParser;
            }
        };

        TerminalClause terminalClause = new TerminalClauseImpl("field", operator, "val");

        MessageSet messageSet = validator.validate(null, terminalClause);
        assertFalse(messageSet.hasAnyMessages());
    }

    @Test
    public void testInvalidQueryDoesntParse() throws Exception
    {
        queryParser.parse("val");
        mockController.setThrowable(new org.apache.lucene.queryParser.ParseException());

        ClauseValidator validator = new FreeTextFieldValidator("field", MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return queryParser;
            }
        };

        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.LIKE, "val");

        replay();

        MessageSet messageSet = validator.validate(null, terminalClause);

        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Unable to parse the text 'val' for field 'field'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testInvalidQueryDoesntParseForBadFuzzyQuery() throws Exception
    {
        // JRA-27018
        queryParser.parse("a~1");
        mockController.setThrowable(new IllegalArgumentException());

        ClauseValidator validator = new FreeTextFieldValidator("field", MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return queryParser;
            }
        };

        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.LIKE, "a~1");

        replay();

        MessageSet messageSet = validator.validate(null, terminalClause);

        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Unable to parse the text 'a~1' for field 'field'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testTwoQueriesSecondDoesntParse() throws Exception
    {
        queryParser.parse("val1");
        mockController.setReturnValue(null);
        queryParser.parse("val2");
        mockController.setThrowable(new org.apache.lucene.queryParser.ParseException());

        ClauseValidator validator = new FreeTextFieldValidator("field", MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return queryParser;
            }
        };

        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.LIKE, new MultiValueOperand("val1", "val2"));

        replay();

        MessageSet messageSet = validator.validate(null, terminalClause);

        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Unable to parse the text 'val2' for field 'field'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testInvalidQueryDoesntParseFunction() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand("function");
        queryParser.parse("val");
        mockController.setThrowable(new org.apache.lucene.queryParser.ParseException());

        final MockOperandHandler handler = new MockOperandHandler(true, false, true);
        handler.add(new QueryLiteral(operand, "val"));

        final MockJqlOperandResolver operandResolver = MockJqlOperandResolver.createSimpleSupport();
        operandResolver.addHandler("function", handler);

        ClauseValidator validator = new FreeTextFieldValidator("field", operandResolver)
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }

            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return queryParser;
            }
        };

        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.LIKE, operand);

        replay();

        MessageSet messageSet = validator.validate(null, terminalClause);

        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("The field 'field' is unable to parse the text given to it by the function 'function'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testInputIsEmptyString() throws Exception
    {
        ClauseValidator validator = new FreeTextFieldValidator("indexField", MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.LIKE, "");

        replay();

        MessageSet messageSet = validator.validate(null, terminalClause);
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("The field 'field' does not support searching for an empty string.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testInputIsBlankString() throws Exception
    {
        ClauseValidator validator = new FreeTextFieldValidator("indexField", MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        TerminalClause terminalClause = new TerminalClauseImpl("field", Operator.LIKE, "  ");

        replay();

        MessageSet messageSet = validator.validate(null, terminalClause);
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("The field 'field' does not support searching for an empty string.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testInvalidOperators() throws Exception
    {
        replay();

        _testValidateInvalidOperator(Operator.EQUALS);
        _testValidateInvalidOperator(Operator.NOT_EQUALS);
        _testValidateInvalidOperator(Operator.IN);
        _testValidateInvalidOperator(Operator.NOT_IN);
        _testValidateInvalidOperator(Operator.GREATER_THAN);
        _testValidateInvalidOperator(Operator.GREATER_THAN_EQUALS);
        _testValidateInvalidOperator(Operator.LESS_THAN);
        _testValidateInvalidOperator(Operator.LESS_THAN_EQUALS);
    }

    private void _testValidateInvalidOperator(Operator operator)
    {
        ClauseValidator validator = new FreeTextFieldValidator("indexField", MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        TerminalClause terminalClause = new TerminalClauseImpl("field", operator, "!val");

        MessageSet messageSet = validator.validate(null, terminalClause);
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("The operator '"+ operator.getDisplayString()+ "' is not supported by the 'field' field.", messageSet.getErrorMessages().iterator().next());
    }

}
