package com.atlassian.jira.util.json;

import com.atlassian.core.util.map.EasyMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestJSONObject
{

    @Test
    public void testValueToStringReturnsNullString() throws Exception
    {
        assertNullString(JSONObject.valueToString(null));
        assertNullString(JSONObject.valueToString(JSONObject.NULL));
        assertNullString(JSONObject.valueToString(new JSONObject.Null()));
    }

    @Test
    public void testValueToStringMap() throws Exception
    {
        assertNullString(JSONObject.valueToString(null));
    }

    @Test
    public void testIsNull() throws Exception
    {
        final JSONObject jsonObject = new JSONObject(EasyMap.build(
                "1", "Some text",
                "2", JSONObject.NULL
        ));
        assertTrue(jsonObject.isNull(null)); // is null because key is null
        assertTrue(jsonObject.isNull("3")); // is null because the key is not in the map
        assertTrue(jsonObject.isNull("2")); // is null because it is Null object

        assertFalse(jsonObject.isNull("1"));
    }

    @Test
    public void testToString() throws Exception
    {
        final JSONObject jsonObject = new JSONObject();
        
    }

    ////////////////////////////////////////////////////
    // Tests for JSON.Null

    @Test
    public void testNullHashCode() throws Exception
    {
        final JSONObject.Null aNull = new JSONObject.Null();
        assertEquals(0, aNull.hashCode());
        assertEquals(0, JSONObject.NULL.hashCode());        
    }

    @Test
    public void testNullEquals() throws Exception
    {
        final JSONObject.Null aNull = new JSONObject.Null();
        final JSONObject.Null bNull = new JSONObject.Null();
        assertTrue(aNull.equals(aNull));
        assertTrue(aNull.equals(bNull));
        assertTrue(bNull.equals(aNull));
        assertTrue(bNull.equals(bNull));

        assertFalse(aNull.equals(null));
        assertFalse(aNull.equals("Hello"));
    }

    @Test
    public void testNullToString() throws Exception
    {
        final JSONObject.Null aNull = new JSONObject.Null();
        assertEquals("null", aNull.toString());
    }

    private void assertNullString(final String str)
    {
        assertEquals("null", str);
    }


}
