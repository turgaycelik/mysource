package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @since v4.0
 */
public class TestDefaultOperandSanitisingVisitor extends MockControllerTestCase
{
    private User theUser;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
    }

    @Test
    public void testVisitEmptyReturnsSame() throws Exception
    {
        DefaultOperandSanitisingVisitor visitor = createVisitor();
        assertSame(EmptyOperand.EMPTY, visitor.visit(EmptyOperand.EMPTY));
    }
    
    @Test
    public void testVisitSingleReturnsSame() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("TEST");
        DefaultOperandSanitisingVisitor visitor = new DefaultOperandSanitisingVisitor(MockJqlOperandResolver.createSimpleSupport(), theUser);
        assertSame(operand, visitor.visit(operand));
    }

    @Test
    public void testVisitFunctionReturnsResult() throws Exception
    {
        final FunctionOperand inputFunc = new FunctionOperand("myFunc");
        final FunctionOperand outputFunc = new FunctionOperand("myFunc2");

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        operandResolver.sanitiseFunctionOperand(theUser, inputFunc);
        mockController.setReturnValue(outputFunc);

        DefaultOperandSanitisingVisitor visitor = createVisitor(null, null, operandResolver);
        assertEquals(outputFunc, visitor.visit(inputFunc));
    }

    @Test
    public void testVisitMultiModified() throws Exception
    {
        FunctionOperand func = new FunctionOperand("myFunc");
        EmptyOperand empty = EmptyOperand.EMPTY;
        SingleValueOperand inputSingle = new SingleValueOperand("HSP");
        SingleValueOperand outputSingle = new SingleValueOperand(10000L);
        MultiValueOperand multi = new MultiValueOperand(func, empty, inputSingle);
        MultiValueOperand expectedMulti = new MultiValueOperand(func, empty, outputSingle);

        DefaultOperandSanitisingVisitor visitor = createVisitor(inputSingle, outputSingle);

        assertEquals(expectedMulti, visitor.visit(multi));
    }

    @Test
    public void testVisitMultiModifiedWithDuplicatesRemoved() throws Exception
    {
        FunctionOperand func1 = new FunctionOperand("myFunc");
        FunctionOperand func2 = new FunctionOperand("myFunc");
        EmptyOperand empty = EmptyOperand.EMPTY;
        SingleValueOperand inputSingle = new SingleValueOperand("HSP");
        SingleValueOperand outputSingle = new SingleValueOperand(10000L);
        MultiValueOperand multi = new MultiValueOperand(func1, func2, empty, inputSingle);
        MultiValueOperand expectedMulti = new MultiValueOperand(func1, empty, outputSingle);

        DefaultOperandSanitisingVisitor visitor = createVisitor(inputSingle, outputSingle);

        assertEquals(expectedMulti, visitor.visit(multi));
    }

    @Test
    public void testVisitMultiNotModified() throws Exception
    {
        FunctionOperand func = new FunctionOperand("myFunc");
        EmptyOperand empty = EmptyOperand.EMPTY;
        SingleValueOperand single = new SingleValueOperand("HSP");
        MultiValueOperand multi = new MultiValueOperand(func, empty, single);

        DefaultOperandSanitisingVisitor visitor = createVisitor();

        assertSame(multi, visitor.visit(multi));
    }

    @Test
    public void testVisitMultiNotModifiedWithDuplicatesPreserved() throws Exception
    {
        FunctionOperand func1 = new FunctionOperand("myFunc");
        FunctionOperand func2 = new FunctionOperand("myFunc");
        EmptyOperand empty = EmptyOperand.EMPTY;
        SingleValueOperand single = new SingleValueOperand("HSP");
        MultiValueOperand multi = new MultiValueOperand(func1, func2, empty, single);

        DefaultOperandSanitisingVisitor visitor = createVisitor();

        assertSame(multi, visitor.visit(multi));
    }

    private DefaultOperandSanitisingVisitor createVisitor()
    {
        return new DefaultOperandSanitisingVisitor(MockJqlOperandResolver.createSimpleSupport(), theUser);
    }

    private DefaultOperandSanitisingVisitor createVisitor(final Operand expectedOperand, final Operand operandToReturnForSingle)
    {
        return createVisitor(expectedOperand, operandToReturnForSingle, MockJqlOperandResolver.createSimpleSupport());
    }

    private DefaultOperandSanitisingVisitor createVisitor(final Operand expectedOperand, final Operand operandToReturnForSingle, final JqlOperandResolver operandResolver)
    {
        final DefaultOperandSanitisingVisitor visitor = new DefaultOperandSanitisingVisitor(operandResolver, theUser)
        {
            public Operand visit(final SingleValueOperand singleValueOperand)
            {
                assertEquals(singleValueOperand, expectedOperand);
                return operandToReturnForSingle;
            }
        };

        mockController.replay();

        return visitor;
    }
}
