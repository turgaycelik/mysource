package com.atlassian.jira.sharing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

import org.apache.commons.collections.set.ListOrderedSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Test class for {@link com.atlassian.jira.sharing.TestSharePermissionUtils}.
 *
 * @since v3.13
 */
public class TestSharePermissionUtils
{
    private static final String TYPE_KEY = "type";
    private static final String PARAM2_KEY = "param2";
    private static final String PARAM1_KEY = "param1";

    private static final SharePermission PERM_NO_PARAM = new SharePermissionImpl(new ShareType.Name("type1"), null, null);
    private static final SharePermission PERM_PARAM1 = new SharePermissionImpl(new ShareType.Name("type2"), "param 2 <>", null);
    private static final SharePermission PERM_PARAM1_PARAM2 = new SharePermissionImpl(new ShareType.Name("type3"), "param 3 []", "par\"am2");

    private static final JSONObject JSON_PERM_NO_PARAM = new JSONObject();
    private static final JSONObject JSON_PERM_PARAM1 = new JSONObject();

    private static final JSONObject JSON_PERM_PARAM1_PARAM2 = new JSONObject();

    private static final JSONObject JSON_PERM_BAD_TYPE = new JSONObject();
    private static final JSONObject JSON_PERM_BAD_PARAM2 = new JSONObject();

    private static final JSONObject JSON_PERM_BLANK_TYPE = new JSONObject();
    private static final JSONObject JSON_PERM_BLANK_PARAM1 = new JSONObject();
    private static final JSONObject JSON_PERM_BLANK_PARAM2 = new JSONObject();

    static
    {
        try
        {
            TestSharePermissionUtils.JSON_PERM_NO_PARAM.put(TestSharePermissionUtils.TYPE_KEY, TestSharePermissionUtils.PERM_NO_PARAM.getType());

            TestSharePermissionUtils.JSON_PERM_PARAM1.put(TestSharePermissionUtils.TYPE_KEY, TestSharePermissionUtils.PERM_PARAM1.getType());
            TestSharePermissionUtils.JSON_PERM_PARAM1.put(TestSharePermissionUtils.PARAM1_KEY, TestSharePermissionUtils.PERM_PARAM1.getParam1());

            TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2.put(TestSharePermissionUtils.TYPE_KEY, TestSharePermissionUtils.PERM_PARAM1_PARAM2.getType());
            TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2.put(TestSharePermissionUtils.PARAM1_KEY, TestSharePermissionUtils.PERM_PARAM1_PARAM2.getParam1());
            TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2.put(TestSharePermissionUtils.PARAM2_KEY, TestSharePermissionUtils.PERM_PARAM1_PARAM2.getParam2());

            TestSharePermissionUtils.JSON_PERM_BAD_PARAM2.put(TestSharePermissionUtils.TYPE_KEY, "randomType");
            TestSharePermissionUtils.JSON_PERM_BAD_PARAM2.put(TestSharePermissionUtils.PARAM2_KEY, "bad param");

            TestSharePermissionUtils.JSON_PERM_BLANK_TYPE.put(TestSharePermissionUtils.TYPE_KEY, "");

            TestSharePermissionUtils.JSON_PERM_BLANK_PARAM1.put(TestSharePermissionUtils.TYPE_KEY, "randomType");
            TestSharePermissionUtils.JSON_PERM_BLANK_PARAM1.put(TestSharePermissionUtils.PARAM1_KEY, "");

            TestSharePermissionUtils.JSON_PERM_BLANK_PARAM2.put(TestSharePermissionUtils.TYPE_KEY, "randomType");
            TestSharePermissionUtils.JSON_PERM_BLANK_PARAM2.put(TestSharePermissionUtils.PARAM1_KEY, "randomParam1");
            TestSharePermissionUtils.JSON_PERM_BLANK_PARAM2.put(TestSharePermissionUtils.PARAM2_KEY, "");

        }
        catch (final JSONException e)
        {
            throw new RuntimeException("This test is broken.", e);
        }
    }

    @Test
    public void testToJsonWithNull() throws JSONException
    {
        try
        {
            SharePermissionUtils.toJson(null);
            fail("This should not accept null shares.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testToJson() throws JSONException
    {
        assertJsonEquals(TestSharePermissionUtils.JSON_PERM_NO_PARAM, SharePermissionUtils.toJson(TestSharePermissionUtils.PERM_NO_PARAM));
        assertJsonEquals(TestSharePermissionUtils.JSON_PERM_PARAM1, SharePermissionUtils.toJson(TestSharePermissionUtils.PERM_PARAM1));
        assertJsonEquals(TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2, SharePermissionUtils.toJson(TestSharePermissionUtils.PERM_PARAM1_PARAM2));
    }

    @Test
    public void testToJsonArrayWithNullShares() throws JSONException
    {
        try
        {
            SharePermissionUtils.toJsonArray(null);
            fail("This should not accept null shares.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testToJsonArrayWithNoShares() throws JSONException
    {
        final JSONArray array = SharePermissionUtils.toJsonArray(Collections.EMPTY_SET);
        assertJsonArrayEquals(new JSONArray(), array);
    }

    @Test
    public void testToJsonArray() throws JSONException
    {
        final JSONArray expected = new JSONArray();
        expected.put(TestSharePermissionUtils.JSON_PERM_NO_PARAM);
        expected.put(TestSharePermissionUtils.JSON_PERM_PARAM1);
        expected.put(TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2);

        final Set actualSet = new ListOrderedSet();
        actualSet.add(TestSharePermissionUtils.PERM_NO_PARAM);
        actualSet.add(TestSharePermissionUtils.PERM_PARAM1);
        actualSet.add(TestSharePermissionUtils.PERM_PARAM1_PARAM2);

        final JSONArray actualArray = SharePermissionUtils.toJsonArray(actualSet);
        assertJsonArrayEquals(expected, actualArray);
    }

    @Test
    public void testFromJsonObjectWithNull() throws JSONException
    {
        try
        {
            SharePermissionUtils.fromJsonObject(null);
            fail("Should not accept null argument.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectWithBadType()
    {
        try
        {
            SharePermissionUtils.fromJsonObject(TestSharePermissionUtils.JSON_PERM_BAD_TYPE);
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectWithBadParams()
    {
        try
        {
            SharePermissionUtils.fromJsonObject(TestSharePermissionUtils.JSON_PERM_BAD_PARAM2);
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectWithBlankType()
    {
        try
        {
            SharePermissionUtils.fromJsonObject(TestSharePermissionUtils.JSON_PERM_BLANK_TYPE);
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectWithBlankParam1()
    {
        try
        {
            SharePermissionUtils.fromJsonObject(TestSharePermissionUtils.JSON_PERM_BLANK_PARAM1);
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectWithBlankParam2()
    {
        try
        {
            SharePermissionUtils.fromJsonObject(TestSharePermissionUtils.JSON_PERM_BLANK_PARAM2);
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObject() throws JSONException
    {
        assertEquals(TestSharePermissionUtils.PERM_NO_PARAM, SharePermissionUtils.fromJsonObject(TestSharePermissionUtils.JSON_PERM_NO_PARAM));
        assertEquals(TestSharePermissionUtils.PERM_PARAM1, SharePermissionUtils.fromJsonObject(TestSharePermissionUtils.JSON_PERM_PARAM1));
        assertEquals(TestSharePermissionUtils.PERM_PARAM1_PARAM2, SharePermissionUtils.fromJsonObject(TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2));
    }

    @Test
    public void testFromJsonObjectStringWithBlankString() throws JSONException
    {
        assertNull(SharePermissionUtils.fromJsonObjectString(""));
    }

    @Test
    public void testFromJsonObjectStringWithNullString() throws JSONException
    {
        try
        {
            SharePermissionUtils.fromJsonObjectString(null);
            fail("Should not accept null string.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectStringWithBadType()
    {
        try
        {
            SharePermissionUtils.fromJsonObjectString(TestSharePermissionUtils.JSON_PERM_BAD_TYPE.toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectStringWithBadParams()
    {
        try
        {
            SharePermissionUtils.fromJsonObjectString(TestSharePermissionUtils.JSON_PERM_BAD_PARAM2.toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectStringWithBlankType()
    {
        try
        {
            SharePermissionUtils.fromJsonObjectString(TestSharePermissionUtils.JSON_PERM_BLANK_TYPE.toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectStringWithBlankParam1()
    {
        try
        {
            SharePermissionUtils.fromJsonObjectString(TestSharePermissionUtils.JSON_PERM_BLANK_PARAM1.toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectStringWithBlankParam2()
    {
        try
        {
            SharePermissionUtils.fromJsonObjectString(TestSharePermissionUtils.JSON_PERM_BLANK_PARAM2.toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectStringBadJson()
    {
        try
        {
            SharePermissionUtils.fromJsonObjectString("[sdssss[[[[[[[");
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonObjectString() throws JSONException
    {
        assertEquals(TestSharePermissionUtils.PERM_NO_PARAM, SharePermissionUtils.fromJsonObjectString(TestSharePermissionUtils.JSON_PERM_NO_PARAM.toString()));
        assertEquals(TestSharePermissionUtils.PERM_PARAM1, SharePermissionUtils.fromJsonObjectString(TestSharePermissionUtils.JSON_PERM_PARAM1.toString()));
        assertEquals(TestSharePermissionUtils.PERM_PARAM1_PARAM2, SharePermissionUtils.fromJsonObjectString(TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2.toString()));
    }

    @Test
    public void testFromJsonArrayWithNull() throws JSONException
    {
        try
        {
            SharePermissionUtils.fromJsonArray(null);
            fail("Should not accept null argument.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayWithBadType()
    {
        try
        {
            SharePermissionUtils.fromJsonArray(createJsonArray(TestSharePermissionUtils.JSON_PERM_BAD_TYPE));
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayWithBadParams()
    {
        try
        {
            SharePermissionUtils.fromJsonArray(createJsonArray(TestSharePermissionUtils.JSON_PERM_BAD_PARAM2));
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayWithBlankType()
    {
        try
        {
            SharePermissionUtils.fromJsonArray(createJsonArray(TestSharePermissionUtils.JSON_PERM_BLANK_TYPE));
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayWithBlankParam1()
    {
        try
        {
            SharePermissionUtils.fromJsonArray(createJsonArray(TestSharePermissionUtils.JSON_PERM_BLANK_PARAM1));
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayWithBlankParam2()
    {
        try
        {
            SharePermissionUtils.fromJsonArray(createJsonArray(TestSharePermissionUtils.JSON_PERM_BLANK_PARAM2));
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayWithBadArgument()
    {
        try
        {
            SharePermissionUtils.fromJsonArray(createJsonArray("ssss"));
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayWithPartialBadInput()
    {
        try
        {
            final JSONArray inputArray = new JSONArray();
            inputArray.put(TestSharePermissionUtils.JSON_PERM_PARAM1);
            inputArray.put(TestSharePermissionUtils.JSON_PERM_BAD_TYPE);
            SharePermissionUtils.fromJsonArray(inputArray);
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayWithEmptArray() throws JSONException
    {
        assertEquals(SharePermissions.PRIVATE, SharePermissionUtils.fromJsonArray(new JSONArray()));
    }

    @Test
    public void testFromJsonArray() throws JSONException
    {
        final Set expected = new HashSet();
        expected.add(TestSharePermissionUtils.PERM_NO_PARAM);
        expected.add(TestSharePermissionUtils.PERM_PARAM1);
        expected.add(TestSharePermissionUtils.PERM_PARAM1_PARAM2);

        final JSONArray inputArray = new JSONArray();
        inputArray.put(TestSharePermissionUtils.JSON_PERM_PARAM1);
        inputArray.put(TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2);
        inputArray.put(TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2);
        inputArray.put(TestSharePermissionUtils.JSON_PERM_NO_PARAM);

        assertEquals(new SharePermissions(expected), SharePermissionUtils.fromJsonArray(inputArray));
    }

    @Test
    public void testFromJsonArrayStringWithNull() throws JSONException
    {
        try
        {
            SharePermissionUtils.fromJsonArrayString(null);
            fail("Should not accept null argument.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayStringWithBadType()
    {
        try
        {
            SharePermissionUtils.fromJsonArrayString(createJsonArray(TestSharePermissionUtils.JSON_PERM_BAD_TYPE).toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayStringWithBadParams()
    {
        try
        {
            SharePermissionUtils.fromJsonArrayString(createJsonArray(TestSharePermissionUtils.JSON_PERM_BAD_PARAM2).toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayStringWithBlankType()
    {
        try
        {
            SharePermissionUtils.fromJsonArrayString(createJsonArray(TestSharePermissionUtils.JSON_PERM_BLANK_TYPE).toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayStringWithBlankParam1()
    {
        try
        {
            SharePermissionUtils.fromJsonArrayString(createJsonArray(TestSharePermissionUtils.JSON_PERM_BLANK_PARAM1).toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayStringWithBlankParam2()
    {
        try
        {
            SharePermissionUtils.fromJsonArrayString(createJsonArray(TestSharePermissionUtils.JSON_PERM_BLANK_PARAM2).toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayStringWithBadArgument()
    {
        try
        {
            SharePermissionUtils.fromJsonArrayString("a]]]");
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayStringWithJsonObject()
    {
        try
        {
            SharePermissionUtils.fromJsonArrayString(TestSharePermissionUtils.JSON_PERM_NO_PARAM.toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayStringWithPartialBadInput()
    {
        try
        {
            final JSONArray inputArray = new JSONArray();
            inputArray.put(TestSharePermissionUtils.JSON_PERM_PARAM1);
            inputArray.put(TestSharePermissionUtils.JSON_PERM_BAD_TYPE);
            SharePermissionUtils.fromJsonArrayString(inputArray.toString());
            fail("Should not accept bad JSON.");
        }
        catch (final JSONException e)
        {
            // expected.
        }
    }

    @Test
    public void testFromJsonArrayStringWithEmptyArray() throws JSONException
    {
        assertEquals(SharePermissions.PRIVATE, SharePermissionUtils.fromJsonArrayString(new JSONArray().toString()));
    }

    @Test
    public void testFromJsonArrayStringWithBlankInput() throws JSONException
    {
        assertEquals(SharePermissions.PRIVATE, SharePermissionUtils.fromJsonArrayString(""));
    }

    @Test
    public void testFromJsonArrayString() throws JSONException
    {
        final Set expected = new HashSet();
        expected.add(TestSharePermissionUtils.PERM_NO_PARAM);
        expected.add(TestSharePermissionUtils.PERM_PARAM1);
        expected.add(TestSharePermissionUtils.PERM_PARAM1_PARAM2);

        final JSONArray inputArray = new JSONArray();
        inputArray.put(TestSharePermissionUtils.JSON_PERM_PARAM1);
        inputArray.put(TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2);
        inputArray.put(TestSharePermissionUtils.JSON_PERM_PARAM1_PARAM2);
        inputArray.put(TestSharePermissionUtils.JSON_PERM_NO_PARAM);

        assertEquals(new SharePermissions(expected), SharePermissionUtils.fromJsonArrayString(inputArray.toString()));
    }

    private static JSONArray createJsonArray(final Object o)
    {
        final JSONArray array = new JSONArray();
        array.put(o);

        return array;
    }

    private static void assertJsonArrayEquals(final JSONArray expected, final JSONArray actual)
    {
        try
        {
            assertEquals("JSON arrays do not have same length.", expected.length(), actual.length());
            for (int i = 0; i < expected.length(); i++)
            {
                TestSharePermissionUtils.assertJsonEquals(expected.getJSONObject(i), actual.getJSONObject(i));
            }
        }
        catch (final JSONException e)
        {
            fail("JSON exception thrown while comparing JSON arrays: " + e.getMessage());
        }
    }

    private static void assertJsonEquals(final JSONObject expected, final JSONObject actual)
    {
        try
        {
            assertEquals("JSON object do not have the same length.", expected.length(), actual.length());
            for (final Iterator iter = expected.keys(); iter.hasNext();)
            {
                final String key = (String) iter.next();
                assertEquals("JSON property '" + key + "' is not the same.", expected.get(key), actual.get(key));
            }
        }
        catch (final JSONException e)
        {
            fail("JSON exception thrown while comparing JSON objects: " + e.getMessage());
        }
    }
}
