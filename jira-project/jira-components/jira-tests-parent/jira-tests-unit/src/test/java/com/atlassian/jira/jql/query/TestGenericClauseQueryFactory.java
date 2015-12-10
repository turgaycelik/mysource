package com.atlassian.jira.jql.query;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.easymock.MockControl;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestGenericClauseQueryFactory
{
    @Test
    public void testGetQueryNoHandler() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");

        final GenericClauseQueryFactory clauseFactory = new GenericClauseQueryFactory(new SimpleFieldSearchConstants("testField", Collections.<Operator>emptySet(), JiraDataTypes.ALL), Collections.<OperatorSpecificQueryFactory>emptyList(), MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            List<QueryLiteral> getRawValues(final QueryCreationContext queryCreationContext, final TerminalClause clause)
            {
                return TestGenericClauseQueryFactory.asList(createLiteral("1"), createLiteral("2"));
            }
        };

        final QueryFactoryResult result = clauseFactory.getQuery(null, new TerminalClauseImpl("testField", Operator.EQUALS, operand));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testGetQueryNoOperandHandler() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");

        final JqlOperandResolver operandResolver = new MockJqlOperandResolver();

        final GenericClauseQueryFactory clauseFactory = new GenericClauseQueryFactory(new SimpleFieldSearchConstants("testField", Collections.<Operator>emptySet(), JiraDataTypes.ALL), Collections.<OperatorSpecificQueryFactory>emptyList(), operandResolver)
        {
            @Override
            List<QueryLiteral> getRawValues(final QueryCreationContext queryCreationContext, final TerminalClause clause)
            {
                return asList(createLiteral("1"), createLiteral("2"));
            }
        };

        final QueryFactoryResult result = clauseFactory.getQuery(null, new TerminalClauseImpl("testField", Operator.EQUALS, operand));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testGetQueryListValues() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand("test");

        final List<QueryLiteral> values = asList(createLiteral("1"), createLiteral("2"));

        final MockControl mockOperatorSpecificQueryFactoryControl = MockControl.createStrictControl(OperatorSpecificQueryFactory.class);
        final OperatorSpecificQueryFactory mockOperatorSpecificQueryFactory = (OperatorSpecificQueryFactory) mockOperatorSpecificQueryFactoryControl.getMock();
        mockOperatorSpecificQueryFactory.handlesOperator(Operator.EQUALS);
        mockOperatorSpecificQueryFactoryControl.setReturnValue(true);
        mockOperatorSpecificQueryFactory.createQueryForMultipleValues("testField", Operator.EQUALS, values);
        mockOperatorSpecificQueryFactoryControl.setReturnValue(null);
        mockOperatorSpecificQueryFactoryControl.replay();

        final MockControl mockOperatorSpecificQueryFactoryControl2 = MockControl.createStrictControl(OperatorSpecificQueryFactory.class);
        final OperatorSpecificQueryFactory mockOperatorSpecificQueryFactory2 = (OperatorSpecificQueryFactory) mockOperatorSpecificQueryFactoryControl2.getMock();
        mockOperatorSpecificQueryFactory2.handlesOperator(Operator.EQUALS);
        mockOperatorSpecificQueryFactoryControl2.setReturnValue(false);
        mockOperatorSpecificQueryFactoryControl2.replay();

        final GenericClauseQueryFactory clauseFactory = new GenericClauseQueryFactory(new SimpleFieldSearchConstants("testField", Collections.<Operator>emptySet(), JiraDataTypes.ALL), asList(mockOperatorSpecificQueryFactory2, mockOperatorSpecificQueryFactory), MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            List<QueryLiteral> getRawValues(final QueryCreationContext queryCreationContext, final TerminalClause clause)
            {
                return values;
            }
        };

        clauseFactory.getQuery(null, new TerminalClauseImpl("testField", Operator.EQUALS, operand));

        mockOperatorSpecificQueryFactoryControl2.verify();
        mockOperatorSpecificQueryFactoryControl.verify();
    }

    @Test
    public void testGetQuerySingleValue() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");

        final List<QueryLiteral> values = asList(createLiteral("1"), createLiteral("2"));

        final MockControl mockOperatorSpecificQueryFactoryControl = MockControl.createStrictControl(OperatorSpecificQueryFactory.class);
        final OperatorSpecificQueryFactory mockOperatorSpecificQueryFactory = (OperatorSpecificQueryFactory) mockOperatorSpecificQueryFactoryControl.getMock();
        mockOperatorSpecificQueryFactory.handlesOperator(Operator.EQUALS);
        mockOperatorSpecificQueryFactoryControl.setReturnValue(true);
        mockOperatorSpecificQueryFactory.createQueryForSingleValue("testField", Operator.EQUALS, values);
        mockOperatorSpecificQueryFactoryControl.setReturnValue(null);
        mockOperatorSpecificQueryFactoryControl.replay();

        final GenericClauseQueryFactory clauseFactory = new GenericClauseQueryFactory(new SimpleFieldSearchConstants("testField", Collections.<Operator>emptySet(), JiraDataTypes.ALL), asList(mockOperatorSpecificQueryFactory), MockJqlOperandResolver.createSimpleSupport())
        {
            @Override
            List<QueryLiteral> getRawValues(final QueryCreationContext queryCreationContext, final TerminalClause clause)
            {
                return values;
            }
        };

        clauseFactory.getQuery(null, new TerminalClauseImpl("testField", Operator.EQUALS, operand));

        mockOperatorSpecificQueryFactoryControl.verify();
    }

    @Test
    public void testGetQueryEmptyOperand() throws Exception
    {
        final EmptyOperand operand = new EmptyOperand();

        final MockControl mockOperatorSpecificQueryFactoryControl = MockControl.createStrictControl(OperatorSpecificQueryFactory.class);
        final OperatorSpecificQueryFactory mockOperatorSpecificQueryFactory = (OperatorSpecificQueryFactory) mockOperatorSpecificQueryFactoryControl.getMock();
        mockOperatorSpecificQueryFactory.handlesOperator(Operator.EQUALS);
        mockOperatorSpecificQueryFactoryControl.setReturnValue(true);
        mockOperatorSpecificQueryFactory.createQueryForEmptyOperand("testField", Operator.EQUALS);
        mockOperatorSpecificQueryFactoryControl.setReturnValue(null);
        mockOperatorSpecificQueryFactoryControl.replay();

        final GenericClauseQueryFactory clauseFactory = new GenericClauseQueryFactory(new SimpleFieldSearchConstants("testField", Collections.<Operator>emptySet(), JiraDataTypes.ALL), asList(mockOperatorSpecificQueryFactory), MockJqlOperandResolver.createSimpleSupport());

        clauseFactory.getQuery(null, new TerminalClauseImpl("testField", Operator.EQUALS, operand));

        mockOperatorSpecificQueryFactoryControl.verify();
    }    

    private static <T>List<T> asList(T ... elements)
    {
        return CollectionBuilder.newBuilder(elements).asList();
    }
}
