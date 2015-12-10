package com.atlassian.query.operand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link MultiValueOperand}.
 *
 * @since v4.0
 */
public class TestMultiValueOperand
{
    @Test
    public void testConstructor()
    {
        try
        {
            new MultiValueOperand((Long[]) null);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }

        try
        {
            new MultiValueOperand(new Long[] {});
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }

        try
        {
            new MultiValueOperand((String[]) null);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }

        try
        {
            new MultiValueOperand(new String[]{});
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }

        try
        {
            new MultiValueOperand((List<? extends Operand>) null);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }

        try
        {
            new MultiValueOperand((Operand[]) null);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }

        try
        {
            new MultiValueOperand(new Operand[]{});
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }

        try
        {
            new MultiValueOperand((QueryLiteral[]) null);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }

        try
        {
            new MultiValueOperand(new QueryLiteral[]{});
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }

        try
        {
            new MultiValueOperand(Collections.<Operand>emptyList());
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }

        try
        {
            List<? extends Operand> containsNull = new ArrayList<Operand>();
            containsNull.add(null);
            new MultiValueOperand(containsNull);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {

        }
    }

    @Test
    public void testConstructWithStrings() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand("test", "stuff" );
        assertEquals(2, operand.getValues().size());
        assertEquals(new SingleValueOperand("test"), operand.getValues().get(0));
        assertEquals(new SingleValueOperand("stuff"), operand.getValues().get(1));
        assertEquals("(\"test\", \"stuff\")", operand.getDisplayString());
    }

    @Test
    public void testConstructWithQueryLiterals() throws Exception
    {
        List<QueryLiteral> literals = CollectionBuilder.newBuilder(createLiteral("test"), createLiteral(99L)).asList();
        final MultiValueOperand operand = MultiValueOperand.ofQueryLiterals(literals);
        assertEquals(2, operand.getValues().size());
        assertEquals(new SingleValueOperand("test"), operand.getValues().get(0));
        assertEquals(new SingleValueOperand(99L), operand.getValues().get(1));
        assertEquals("(\"test\", 99)", operand.getDisplayString());
    }

    @Test
    public void testDisplayNameWithLongs() throws Exception
    {
        final MultiValueOperand operand = new MultiValueOperand(10L, 999L );
        assertEquals(2, operand.getValues().size());
        assertEquals(new SingleValueOperand(10L), operand.getValues().get(0));
        assertEquals(new SingleValueOperand(999L), operand.getValues().get(1));
        assertEquals("(10, 999)", operand.getDisplayString());
    }

    @Test
    public void testDisplayNameWithOperands() throws Exception
    {
        final SingleValueOperand operand1 = new SingleValueOperand("hello");
        final SingleValueOperand operand2 = new SingleValueOperand(999L);
        final MultiValueOperand operand = new MultiValueOperand(operand1, operand2);
        assertEquals(2, operand.getValues().size());
        assertEquals(operand1, operand.getValues().get(0));
        assertEquals(operand2, operand.getValues().get(1));
        assertEquals("(\"hello\", 999)", operand.getDisplayString());
    }

    @Test
    public void testDisplayNameWithOperandList() throws Exception
    {
        final SingleValueOperand operand1 = new SingleValueOperand("hello");
        final SingleValueOperand operand2 = new SingleValueOperand(999L);
        final MultiValueOperand operand = new MultiValueOperand(CollectionBuilder.newBuilder(operand1, operand2).asList());
        assertEquals(2, operand.getValues().size());
        assertEquals(operand1, operand.getValues().get(0));
        assertEquals(operand2, operand.getValues().get(1));
        assertEquals("(\"hello\", 999)", operand.getDisplayString());
    }

    @Test
    public void testName() throws Exception
    {
        assertEquals("MultiValueOperand", new MultiValueOperand("test", "stuff").getName());
    }
}
