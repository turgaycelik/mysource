package com.atlassian.jira.jql.util;

import java.math.BigInteger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Simple tests for {@link com.atlassian.jira.jql.util.JqlCustomFieldId}.
 *
 * @since v4.0
 */
public class TestJqlCustomFieldId
{
    @Test
    public void testConstructor()
    {
        JqlCustomFieldId id = new JqlCustomFieldId(10);
        assertEquals(10, id.getId());

        id = new JqlCustomFieldId(0);
        assertEquals(0, id.getId());

        try
        {
            new JqlCustomFieldId(-1);
            fail("Negative values should not be supported.");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testGetName()
    {
        JqlCustomFieldId id = new JqlCustomFieldId(10);
        assertEquals("cf[10]", id.getJqlName());
    }

    @SuppressWarnings ({ "EqualsBetweenInconvertibleTypes", "ObjectEqualsNull" })
    @Test
    public void testEquals()
    {
        JqlCustomFieldId id1 = new JqlCustomFieldId(10);
        JqlCustomFieldId id2 = new JqlCustomFieldId(10);
        JqlCustomFieldId id3 = new JqlCustomFieldId(10);
        JqlCustomFieldId id4 = new JqlCustomFieldId(1000);

        assertEquals(id1, id2);
        assertEquals(id2, id1);

        assertEquals(id1, id1);
        assertEquals(id1, id3);
        assertEquals(id3, id2);

        assertFalse(id1.equals(10L));
        assertFalse(id4.equals(id1));
        assertFalse(id1.equals(id4));
        assertFalse(id1.equals(null));
    }

    @Test
    public void testHashCode()
    {
        JqlCustomFieldId id1 = new JqlCustomFieldId(10);
        JqlCustomFieldId id2 = new JqlCustomFieldId(10);

        assertEquals(id1.hashCode(), id2.hashCode());
        assertEquals(id1.hashCode(), id1.hashCode());
    }

    @Test
    public void testToString()
    {
        assertEquals("cf[10]", JqlCustomFieldId.toString(10));
        assertEquals("cf[0]", JqlCustomFieldId.toString(0));
        assertEquals("cf[" + Long.MAX_VALUE + "]", JqlCustomFieldId.toString(Long.MAX_VALUE));

        try
        {
            JqlCustomFieldId.toString(-19);
            fail("-19 should not be a valid id.");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testIsCustomFieldId()
    {
        assertTrue(JqlCustomFieldId.isJqlCustomFieldId("cf[102]"));
        assertTrue(JqlCustomFieldId.isJqlCustomFieldId("cf [102]"));
        assertTrue(JqlCustomFieldId.isJqlCustomFieldId("cf   [    102    ]     "));
        assertTrue(JqlCustomFieldId.isJqlCustomFieldId("   \t\n cf[   102]"));
        assertTrue(JqlCustomFieldId.isJqlCustomFieldId("cf[102]"));
        assertTrue(JqlCustomFieldId.isJqlCustomFieldId("cf[" + Long.MAX_VALUE + "   ]"));

        assertFalse(JqlCustomFieldId.isJqlCustomFieldId("cf[" + BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE) + "   ]"));
        assertFalse(JqlCustomFieldId.isJqlCustomFieldId("   "));
        assertFalse(JqlCustomFieldId.isJqlCustomFieldId(null));
        assertFalse(JqlCustomFieldId.isJqlCustomFieldId("cf[102"));
        assertFalse(JqlCustomFieldId.isJqlCustomFieldId("cf [[102]"));
        assertFalse(JqlCustomFieldId.isJqlCustomFieldId("cr   [    102    ]     "));
        assertFalse(JqlCustomFieldId.isJqlCustomFieldId("    cf   102]"));
        assertFalse(JqlCustomFieldId.isJqlCustomFieldId("cf[1023283829843029834092843092385748375398754938754528905]"));
    }

    public void parseJqlCustomFieldId()
    {
        assertEquals(new JqlCustomFieldId(102), JqlCustomFieldId.parseJqlCustomFieldId("cf[102]"));
        assertEquals(new JqlCustomFieldId(2313), JqlCustomFieldId.parseJqlCustomFieldId("cf [2313]"));
        assertEquals(new JqlCustomFieldId(556), JqlCustomFieldId.parseJqlCustomFieldId("cf   [    556    ]     "));
        assertEquals(new JqlCustomFieldId(148), JqlCustomFieldId.parseJqlCustomFieldId("   \t\n cf[   148]"));
        assertEquals(new JqlCustomFieldId(6655), JqlCustomFieldId.parseJqlCustomFieldId("cf[6655]"));
        assertEquals(new JqlCustomFieldId(0), JqlCustomFieldId.parseJqlCustomFieldId("cf[ 0  \t     \n\r  ]"));
        assertEquals(new JqlCustomFieldId(Long.MAX_VALUE), JqlCustomFieldId.parseJqlCustomFieldId("cf[" + Long.MAX_VALUE + "   ]"));

        assertNull(JqlCustomFieldId.parseJqlCustomFieldId("cf[" + BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE) + "   ]"));
        assertNull(JqlCustomFieldId.parseJqlCustomFieldId("   "));
        assertNull(JqlCustomFieldId.parseJqlCustomFieldId(null));
        assertNull(JqlCustomFieldId.parseJqlCustomFieldId("cf[102"));
        assertNull(JqlCustomFieldId.parseJqlCustomFieldId("cf [[102]"));
        assertNull(JqlCustomFieldId.parseJqlCustomFieldId("cr   [    102    ]     "));
        assertNull(JqlCustomFieldId.parseJqlCustomFieldId("    cf   102]"));
        assertNull(JqlCustomFieldId.parseJqlCustomFieldId("cf[10232838298430298340928430923857483753987549387545289058309845039854093804980937498237489237]"));
    }

    public void parseId()
    {
        assertEquals(102, JqlCustomFieldId.parseId("cf[102]"));
        assertEquals(2313, JqlCustomFieldId.parseId("cf [2313]"));
        assertEquals(556, JqlCustomFieldId.parseId("cf   [    556    ]     "));
        assertEquals(148, JqlCustomFieldId.parseId("   \t\n cf[   148]"));
        assertEquals(6655, JqlCustomFieldId.parseId("cf[6655]"));
        assertEquals(0, JqlCustomFieldId.parseId("cf[ 0  \t     \n\r  ]"));
        assertEquals(Long.MAX_VALUE, JqlCustomFieldId.parseId("cf[" + Long.MAX_VALUE + "   ]"));

        assertTrue(JqlCustomFieldId.parseId("cf[" + BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE) + "   ]") < 0);
        assertTrue(JqlCustomFieldId.parseId("   ") < 0);
        assertTrue(JqlCustomFieldId.parseId(null) < 0);
        assertTrue(JqlCustomFieldId.parseId("cf[102") < 0);
        assertTrue(JqlCustomFieldId.parseId("cf [[102]") < 0);
        assertTrue(JqlCustomFieldId.parseId("cr   [    102    ]     ") < 0);
        assertTrue(JqlCustomFieldId.parseId("    cf   102]") < 0);
        assertTrue(JqlCustomFieldId.parseId("cf[10232838298430298340928430923857483753987549387545289058309845039854093804980937498237489237]") < 0);
    }
}
