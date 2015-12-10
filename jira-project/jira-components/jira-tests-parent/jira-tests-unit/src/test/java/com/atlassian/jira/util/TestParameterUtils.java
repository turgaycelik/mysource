package com.atlassian.jira.util;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.webwork.ParameterMapBuilder;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestParameterUtils
{
    private FieldValuesHolder params;

    @Before
    public void setUp() throws Exception
    {
        new MockGenericValue("Version", FieldMap.build("id", new Long(10), "project", new Long(1), "name", "testVersion1"));
        new MockGenericValue("Version", FieldMap.build("id", new Long(20), "project", new Long(1), "name", "testVersion2"));
        new MockGenericValue("Component", FieldMap.build("id", new Long(100), "project", new Long(1), "name", "testComponent1"));
        new MockGenericValue("Component", FieldMap.build("id", new Long(200), "project", new Long(1), "name", "testComponent2"));

        params = EasyFvh.build("pid", Lists.newArrayList("1"));
        params.put("types", new String[] { "2" });
        params.put("text", "Query");
        params.put("priority", new String[] { "3" });
        params.put("resolution", new String[] { "4" });
        params.put("status", new String[] { "2" });
        params.put("created:after", "1-Jan-2003");
        params.put("created:before", "13-Jan-2003");
        params.put("updated:after", "1-Jan-2003");
        params.put("updated:before", "13-Jan-2003");
        params.put("updated:previous", 7L);
        params.put("reporterSelect", DocumentConstants.SPECIFIC_USER);
        params.put("reporter", "bob");
        params.put("assigneeSelect", DocumentConstants.SPECIFIC_USER);
        params.put("assignee", "bob");
        params.put("fixfor", new String[] { "10", "20" });
        params.put("component", new String[] { "100", "200" });
        params.put("version", new String[] { "10", "20" });
    }

    @Test
    public void test_getStringArrayParam()
    {
        Map parameters = new ParameterMapBuilder()
                .add("alpha", "whiskey tango")
                .add("beta", "charlie foxtrot")
                .add("beta", "gamma sausage")
                .toMap();

        // test no key
        assertNull(ParameterUtils.getStringArrayParam(parameters, "doesnoteexist"));

        // test single value
        assertStringArraysEqual(new String[] { "whiskey tango"}, ParameterUtils.getStringArrayParam(parameters, "alpha"));

        // test multi value
        assertStringArraysEqual(new String[] { "charlie foxtrot", "gamma sausage"}, ParameterUtils.getStringArrayParam(parameters, "beta"));

        // null object
        parameters = FieldMap.build("alpha", null);
        assertNull(ParameterUtils.getStringArrayParam(parameters, "alpha"));

        // string object
        parameters = FieldMap.build("alpha", "whiskey tango");
        assertStringArraysEqual(new String[] { "whiskey tango"}, ParameterUtils.getStringArrayParam(parameters, "alpha"));

        // collection
        parameters = FieldMap.build("beta", Lists.newArrayList("charlie foxtrot", "gamma sausage"));
        assertStringArraysEqual(new String[] { "charlie foxtrot", "gamma sausage"}, ParameterUtils.getStringArrayParam(parameters, "beta"));

        // collection with a null
        parameters = FieldMap.build("beta", Lists.newArrayList("charlie foxtrot", null, "gamma sausage"));
        assertStringArraysEqual(new String[] { "charlie foxtrot", null, "gamma sausage"}, ParameterUtils.getStringArrayParam(parameters, "beta"));

        // arbitrary object
        Date now = new Date();
        String nowStr = now.toString();
        parameters = FieldMap.build("obj", now);
        assertStringArraysEqual(new String[] { nowStr}, ParameterUtils.getStringArrayParam(parameters, "obj"));
    }


    @Test
    public void test_getLong()
    {
        Map parameters = new ParameterMapBuilder()
                .add("bad1", "123a")
                .add("bad2", "a123")
                .add("bad3", "")
                .add("bad4", "\t")
                .add("good1", "123")
                .add("beta", "gamma sausage")
                .toMap();

        assertNull(ParameterUtils.getLongParam(parameters, "doesnotexist"));
        assertNull(ParameterUtils.getLongParam(parameters, "bad1"));
        assertNull(ParameterUtils.getLongParam(parameters, "bad2"));
        assertNull(ParameterUtils.getLongParam(parameters, "bad3"));
        assertNull(ParameterUtils.getLongParam(parameters, "bad4"));

        assertEquals(new Long(123), ParameterUtils.getLongParam(parameters, "good1"));
    }

    @Test
    public void test_getDouble()
    {
        Map parameters = new ParameterMapBuilder()
                .add("bad1", "123a")
                .add("bad2", "a123")
                .add("bad3", "")
                .add("bad4", "\t")
                .add("good1", "123")
                .add("good2", "123e12")
                .add("beta", "gamma sausage")
                .toMap();

        assertNull(ParameterUtils.getDoubleParam(parameters, "doesnotexist"));
        assertNull(ParameterUtils.getDoubleParam(parameters, "bad1"));
        assertNull(ParameterUtils.getDoubleParam(parameters, "bad2"));
        assertNull(ParameterUtils.getDoubleParam(parameters, "bad3"));
        assertNull(ParameterUtils.getDoubleParam(parameters, "bad4"));

        assertEquals(Double.valueOf(123d), ParameterUtils.getDoubleParam(parameters, "good1"));
        assertEquals(Double.valueOf(123e12), ParameterUtils.getDoubleParam(parameters, "good2"));
    }

    public void test_getIntParam()
    {
        Map parameters = new ParameterMapBuilder()
                .add("bad1", "123a")
                .add("bad2", "a123")
                .add("bad3", "")
                .add("bad4", "\t")
                .add("good1", "123")
                .add("beta", "gamma sausage")
                .toMap();

        assertEquals(666, ParameterUtils.getIntParam(parameters, "doesnotexist", 666));
        assertEquals(666, ParameterUtils.getIntParam(parameters, "bad1", 666));
        assertEquals(666, ParameterUtils.getIntParam(parameters, "bad2", 666));
        assertEquals(666, ParameterUtils.getIntParam(parameters, "bad3", 666));
        assertEquals(666, ParameterUtils.getIntParam(parameters, "bad4", 666));

        assertEquals(123, ParameterUtils.getIntParam(parameters, "good1", 999));
    }

    @Test
    public void test_getBooleanParam()
    {
        Map parameters = new ParameterMapBuilder()
                .add("bad1", "123a")
                .add("bad2", "a123")
                .add("bad3", "")
                .add("bad4", "\t")
                .add("good1", "true")
                .add("beta", "gamma sausage")
                .toMap();

        assertFalse(ParameterUtils.getBooleanParam(parameters, "doesnotexist"));
        assertFalse(ParameterUtils.getBooleanParam(parameters, "bad1"));
        assertFalse(ParameterUtils.getBooleanParam(parameters, "bad2"));
        assertFalse(ParameterUtils.getBooleanParam(parameters, "bad3"));
        assertFalse(ParameterUtils.getBooleanParam(parameters, "bad4"));

        assertTrue(ParameterUtils.getBooleanParam(parameters, "good1"));
    }

    private void assertStringArraysEqual(final String[] expected, final String[] actual)
    {
        assertEquals(expected.length, actual.length);
        assertTrue(Arrays.equals(expected,actual));
    }
}
