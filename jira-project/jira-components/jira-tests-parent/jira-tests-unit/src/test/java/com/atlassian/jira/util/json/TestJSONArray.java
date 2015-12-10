package com.atlassian.jira.util.json;

import com.google.common.collect.Lists;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestJSONArray
{
    @Test
    public void testIsNull() throws Exception
    {
        final JSONArray jsonArray = new JSONArray(Lists.newArrayList("Text", JSONObject.NULL));

        assertTrue(jsonArray.isNull(-10)); // null 'cause index is out of bounds
        assertTrue(jsonArray.isNull(10));  // null 'cause index is out of bounds
        assertTrue(jsonArray.isNull(1));   // nusl 'cause it is NULL object

        assertFalse(jsonArray.isNull(0));
    }

    @Test
    public void testToStringWithException() throws Exception
    {
        final JSONArray jsonArray = new JSONArray()
        {
            public String join(final String separator) throws JSONException
            {
                throw new RuntimeException("intentional");
            }
        };

        assertEquals("", jsonArray.toString());
    }

    @Test
    public void testToString() throws Exception
    {
        final JSONArray jsonArray = new JSONArray(Lists.newArrayList("Text", JSONObject.NULL));

        assertEquals("[\"Text\",null]", jsonArray.toString());
    }

}
