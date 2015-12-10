package com.atlassian.query.operand;

import com.atlassian.jira.jql.operand.QueryLiteral;

import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link com.atlassian.query.operand.SingleValueOperand}.
 *
 * @since v4.0
 */
public class TestSingleValueOperand
{

    @Test
    public void testConstructor()
    {
        try
        {
            new SingleValueOperand((Long) null);
            fail("expected IAE");
        }
        catch (IllegalArgumentException expected)
        {

        }
        try
        {
            new SingleValueOperand((String) null);
            fail("expected IAE");
        }
        catch (IllegalArgumentException expected)
        {

        }
        try
        {
            new SingleValueOperand((QueryLiteral) null);
            fail("expected IAE");
        }
        catch (IllegalArgumentException expected)
        {

        }
        try
        {
            new SingleValueOperand(createLiteral((String) null));
            fail("expected IAE");
        }
        catch (IllegalArgumentException expected)
        {

        }
    }

    @Test
    public void testGetLongFromQueryLiteral()
    {
        SingleValueOperand svo = new SingleValueOperand(createLiteral(123L));
        assertEquals(new Long(123), svo.getLongValue());
        assertNull(svo.getStringValue());
        assertEquals("123", svo.getDisplayString());
    }

    @Test
    public void testGetStringFromQueryLiteral()
    {
        SingleValueOperand svo = new SingleValueOperand(createLiteral("123"));
        assertNull(svo.getLongValue());
        assertEquals("123", svo.getStringValue());
        assertEquals("\"123\"", svo.getDisplayString());
    }

    @Test
    public void testGetLongValues()
    {
        SingleValueOperand svo = new SingleValueOperand(123L);
        assertEquals(new Long(123), svo.getLongValue());
        assertNull(svo.getStringValue());
        assertEquals("123", svo.getDisplayString());
    }

    @Test
    public void testGetStringValues()
    {
        SingleValueOperand svo = new SingleValueOperand("foobar");
        assertEquals("foobar", svo.getStringValue());
        assertNull(svo.getLongValue());
        assertEquals("\"foobar\"", svo.getDisplayString());
    }

    @Test
    public void testName() throws Exception
    {
        assertEquals("SingleValueOperand", new SingleValueOperand(123L).getName());
    }

}
