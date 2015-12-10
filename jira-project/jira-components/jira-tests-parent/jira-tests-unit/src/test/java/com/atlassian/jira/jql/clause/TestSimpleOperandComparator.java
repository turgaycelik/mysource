package com.atlassian.jira.jql.clause;

import java.util.Collections;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestSimpleOperandComparator
{
    @Test
    public void testMultiValueOperands() throws Exception
    {
        Operand o1 = new SingleValueOperand("john");
        Operand o2 = new FunctionOperand("func");
        // order is not relevant for multi value operands
        MultiValueOperand op = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2).asList());
        MultiValueOperand op1 = new MultiValueOperand(CollectionBuilder.newBuilder(o2, o1).asList());
        assertEquiv(op, op1);

        // check that different sizes are autofail
        Operand o3 = new MultiValueOperand("1", "2", "3");
        op = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2, o3).asList());
        op1 = new MultiValueOperand(CollectionBuilder.newBuilder(o2, o1).asList());
        assertUnequiv(op, op1);
        assertUnequiv(op1, op);

        // again ordering is not important
        op = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2, o3).asList());
        op1 = new MultiValueOperand(CollectionBuilder.newBuilder(o3, o2, o1).asList());
        assertEquiv(op, op1);

        // same value twice should be okay
        op = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2, o2).asList());
        op1 = new MultiValueOperand(CollectionBuilder.newBuilder(o2, o2, o1).asList());
        assertEquiv(op, op1);

        // same size but different values == fail
        op = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2, o2).asList());
        op1 = new MultiValueOperand(CollectionBuilder.newBuilder(o3, o2, o1).asList());
        assertUnequiv(op, op1);
    }
    
    @Test
    public void testFunctionOperands() throws Exception
    {
        Operand op = new FunctionOperand("func");
        Operand op1 = new FunctionOperand("func");
        assertEquiv(op, op1);

        op = new FunctionOperand("func");
        op1 = new FunctionOperand("func", Collections.singletonList("test"));
        assertUnequiv(op, op1);

        op = new FunctionOperand("func", Collections.singletonList("test"));
        op1 = new FunctionOperand("func", Collections.singletonList("test"));
        assertEquiv(op, op1);

        // order of args is relevant
        op = new FunctionOperand("func", CollectionBuilder.newBuilder("2", "1").asList());
        op1 = new FunctionOperand("func", CollectionBuilder.newBuilder("1", "2").asList());
        assertUnequiv(op, op1);

        // totally different operands
        op = new FunctionOperand("func", CollectionBuilder.newBuilder("2", "1").asList());
        op1 = new SingleValueOperand("test");
        assertUnequiv(op, op1);
    }

    @Test
    public void testSingleValueOperands() throws Exception
    {
        Operand op = new SingleValueOperand("john");
        Operand op1 = new SingleValueOperand(11L);
        assertUnequiv(op, op1);

        op = new SingleValueOperand("john");
        op1 = new SingleValueOperand("john");
        assertEquiv(op, op1);

        op = new SingleValueOperand(11L);
        op1 = new SingleValueOperand(11L);
        assertEquiv(op, op1);
    }

    private void assertEquiv(Operand o, Operand o1)
    {
        assertTrue(new SimpleOperandComparator().isEquivalent(o, o1));
    }

    private void assertUnequiv(Operand o, Operand o1)
    {
        assertFalse(new SimpleOperandComparator().isEquivalent(o, o1));
    }
}
