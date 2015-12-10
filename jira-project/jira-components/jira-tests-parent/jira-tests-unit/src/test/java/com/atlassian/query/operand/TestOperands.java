package com.atlassian.query.operand;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link Operands}.
 *
 * @since v4.0
 */
public class TestOperands
{
    @Test
    public void testValueOfStringNull() throws Exception
    {
        try
        {
            Operands.valueOf((String)null);
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testValueOfString() throws Exception
    {
        final String value = "test";
        assertEquals(new SingleValueOperand(value), Operands.valueOf(value));   
    }

    @Test
    public void testValueOfStringVarArgsBad() throws Exception
    {
        try
        {
            Operands.valueOf("djaksda", null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOf((String[]) null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOf(new String[]{});
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testValueOfStringVarArgs() throws Exception
    {
        final String value = "test";
        final String value2 = "test2";
        assertEquals(new MultiValueOperand(value, value2), Operands.valueOf(value, value2));
    }

    @Test
    public void testValueOfStringCollectionBad() throws Exception
    {
        try
        {
            Operands.valueOfStrings(CollectionBuilder.newBuilder("djaksda", null).asLinkedList());
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOfStrings(null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOfStrings(Collections.<String>emptyList());
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testValueOfStringCollection() throws Exception
    {
        final String value = "test";
        final String value2 = "test2";
        Collection<String> values = CollectionBuilder.newBuilder(value, value2).asList();
        assertEquals(new MultiValueOperand(value, value2), Operands.valueOfStrings(values));
    }

    @Test
    public void testValueOfNumberNull() throws Exception
    {
        try
        {
            Operands.valueOf((Long)null);
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testValueOfNumber() throws Exception
    {
        final long value = 1;
        assertEquals(new SingleValueOperand(value), Operands.valueOf(value));
    }

    @Test
    public void testValueOfLongVarArgsBad() throws Exception
    {
        try
        {
            Operands.valueOf(5L, null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOf((Long[]) null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOf(new Long[]{});
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testValueOfNumberVarArgs() throws Exception
    {
        final long value = 5738L;
        final long value2 = 384893L;
        assertEquals(new MultiValueOperand(value, value2), Operands.valueOf(value, value2));
    }

    @Test
    public void testValueOfNumberCollectionBad() throws Exception
    {
        try
        {
            Operands.valueOfNumbers(CollectionBuilder.newBuilder(5L, null).asLinkedList());
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOfNumbers(null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOfNumbers(Collections.<Long>emptyList());
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testValueOfNumberCollection() throws Exception
    {
        final long value = 9492098433902843L;
        final long value2 = 8438932L;
        Collection<Long> values = CollectionBuilder.newBuilder(value, value2).asList();
        assertEquals(new MultiValueOperand(value, value2), Operands.valueOfNumbers(values));
    }

      @Test
      public void testValueOfOperandsVarArgsBad() throws Exception
    {
        try
        {
            Operands.valueOf(new SingleValueOperand(5L), null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOf((Operand[]) null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOf(new Operand[]{});
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testValueOfOperandsVarArgs() throws Exception
    {
        final Operand value = new SingleValueOperand(5738L);
        final Operand value2 = new SingleValueOperand(384893L);
        assertEquals(new MultiValueOperand(value, value2), Operands.valueOf(value, value2));
    }

    @Test
    public void testValueOfOperandCollectionBad() throws Exception
    {
        try
        {
            Operands.valueOfOperands(CollectionBuilder.<Operand>newBuilder(new SingleValueOperand(5L), null).asLinkedList());
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOfOperands(null);
        }
        catch (IllegalArgumentException e) { }

        try
        {
            Operands.valueOfOperands(Collections.<Operand>emptyList());
        }
        catch (IllegalArgumentException e) { }
    }

    @Test
    public void testValueOfOperandCollection() throws Exception
    {
        final SingleValueOperand value = new SingleValueOperand(9492098433902843L);
        final SingleValueOperand value2 = new SingleValueOperand(8438932L);

        Collection<Operand> values = CollectionBuilder.<Operand>newBuilder(value, value2).asList();
        assertEquals(new MultiValueOperand(value, value2), Operands.valueOfOperands(values));
    }

    @Test
    public void testGetEmpty() throws Exception
    {
        assertEquals(EmptyOperand.EMPTY, Operands.getEmpty());
    }
}
