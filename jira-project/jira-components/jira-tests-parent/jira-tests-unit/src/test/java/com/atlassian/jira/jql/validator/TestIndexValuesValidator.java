package com.atlassian.jira.jql.validator;

import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.IndexValueConverter;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestIndexValuesValidator extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private IndexValueConverter indexValueConverter;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        indexValueConverter = mockController.getMock(IndexValueConverter.class);
    }

    @Test
    public void testValidateNullValid() throws Exception
    {
        final Operand operand = new FunctionOperand("DoesntExist");
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);

        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        mockController.replay();

        final AtomicInteger called = new AtomicInteger(0);

        IndexValuesValidator validator = new IndexValuesValidator(jqlOperandResolver, indexValueConverter)
        {
            @Override
            void addError(final MessageSet messageSet, final User searcher, final TerminalClause terminalClause, final QueryLiteral literal)
            {
                called.getAndIncrement();
            }
        };

        validator.validate(theUser, clause);

        assertEquals(0, called.get());
        mockController.verify();
    }

    @Test
    public void testValidateEmptyValid() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.IS, operand);

        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        mockController.replay();

        final AtomicInteger called = new AtomicInteger(0);

        IndexValuesValidator validator = new IndexValuesValidator(jqlOperandResolver, indexValueConverter)
        {
            @Override
            void addError(final MessageSet messageSet, final User searcher, final TerminalClause terminalClause, final QueryLiteral literal)
            {
                called.getAndIncrement();
            }
        };

        validator.validate(theUser, clause);

        assertEquals(0, called.get());
        mockController.verify();
    }

    @Test
    public void testValidateEmptyInvalid() throws Exception
    {
        final Operand operand = EmptyOperand.EMPTY;
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.IS, operand);

        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();

        mockController.replay();

        final AtomicInteger called = new AtomicInteger(0);

        IndexValuesValidator validator = new IndexValuesValidator(jqlOperandResolver, indexValueConverter, false)
        {
            @Override
            void addError(final MessageSet messageSet, final User searcher, final TerminalClause terminalClause, final QueryLiteral literal)
            {
                called.getAndIncrement();
            }
        };

        validator.validate(theUser, clause);

        assertEquals(1, called.get());
        mockController.verify();
    }

    @Test
    public void testValidateSingleValid() throws Exception
    {
        final Operand operand = new SingleValueOperand("blah");
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = createLiteral("blah");

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(literal).asList());

        indexValueConverter.convertToIndexValue(literal);
        mockController.setReturnValue("a string");

        mockController.replay();

        final AtomicInteger called = new AtomicInteger(0);

        IndexValuesValidator validator = new IndexValuesValidator(jqlOperandResolver, indexValueConverter)
        {
            @Override
            void addError(final MessageSet messageSet, final User searcher, final TerminalClause terminalClause, final QueryLiteral literal)
            {
                called.getAndIncrement();
            }
        };


        validator.validate(theUser, clause);

        assertEquals(0, called.get());
        mockController.verify();
    }

    @Test
    public void testValidateSingleInvalid() throws Exception
    {
        final Operand operand = new SingleValueOperand("blah");
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = createLiteral("blah");

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(literal).asList());

        indexValueConverter.convertToIndexValue(literal);
        mockController.setReturnValue(null);

        mockController.replay();

        final AtomicInteger called = new AtomicInteger(0);

        IndexValuesValidator validator = new IndexValuesValidator(jqlOperandResolver, indexValueConverter)
        {
            @Override
            void addError(final MessageSet messageSet, final User searcher, final TerminalClause terminalClause, final QueryLiteral literal)
            {
                called.getAndIncrement();
            }
        };


        validator.validate(theUser, clause);

        assertEquals(1, called.get());
        mockController.verify();
    }

    @Test
    public void testValidateSingleMultiple() throws Exception
    {
        final Operand operand = new SingleValueOperand("blah");
        final TerminalClause clause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literalInvalid1 = createLiteral("blah1");
        final QueryLiteral literalInvalid2 = createLiteral("blah2");
        final QueryLiteral literalValid1 = createLiteral("blah3");
        final QueryLiteral literalValid2 = createLiteral("blah4");

        jqlOperandResolver.getValues(theUser, operand, clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(literalInvalid1, literalValid1, literalInvalid2, literalValid2).asList());

        indexValueConverter.convertToIndexValue(literalInvalid1);
        mockController.setReturnValue(null);

        indexValueConverter.convertToIndexValue(literalValid1);
        mockController.setReturnValue("a string");

        indexValueConverter.convertToIndexValue(literalInvalid2);
        mockController.setReturnValue(null);

        indexValueConverter.convertToIndexValue(literalValid2);
        mockController.setReturnValue("a string2");

        mockController.replay();

        final AtomicInteger called = new AtomicInteger(0);

        IndexValuesValidator validator = new IndexValuesValidator(jqlOperandResolver, indexValueConverter)
        {
            @Override
            void addError(final MessageSet messageSet, final User searcher, final TerminalClause terminalClause, final QueryLiteral literal)
            {
                called.getAndIncrement();
            }
        };


        validator.validate(theUser, clause);

        assertEquals(2, called.get());
        mockController.verify();
    }


}
