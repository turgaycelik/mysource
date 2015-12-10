package com.atlassian.jira.issue.customfields.view;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.fields.CustomField;

import com.google.common.collect.Lists;
import com.mockobjects.dynamic.Mock;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCustomFieldParamsImpl
{
    Mock customFieldMock;
    CustomFieldParamsImpl params;

    @Before
    public void setUp()
    {
        customFieldMock = new Mock(CustomField.class);
        customFieldMock.expectAndReturn("getId", "customField_123");

        params = new CustomFieldParamsImpl((CustomField) customFieldMock.proxy());
    }

    @Test
    public void testGetDefaultParameterQueryString()
    {
        params.addValue(null, EasyList.build("Margaret"));
        assertEquals("customField_123=Margaret", params.getQueryString());
    }

    @Test
    public void testGetQueryStringIgnoresNulls()
    {
        params.addValue(null, null);
        assertEquals("", params.getQueryString());
    }

    @Test
    public void testGetSingleParameterQueryString()
    {
        params.addValue("name", EasyList.build("Bob"));
        assertEquals("customField_123:name=Bob", params.getQueryString());
    }

    @Test
    public void testGetMultiParameterQueryString()
    {
        params.addValue("name", EasyList.build("Angela", "Duncan"));
        assertEquals("customField_123:name=Angela&customField_123:name=Duncan", params.getQueryString());
    }

    @Test
    public void testGetMultiDifferentParameterQueryString()
    {
        params.addValue("name", EasyList.build("Angela", "Duncan"));
        params.addValue("age", EasyList.build("21"));
        params.addValue(null, EasyList.build("Bob"));
        String param1 = "customField_123:age=21";
        String param2 = "customField_123=Bob";
        String param3 = "customField_123:name=Angela";
        String param4 = "customField_123:name=Duncan";

        assertEquals((param1 + "&" + param2 + "&" + param3 + "&" + param4).length(), params.getQueryString().length());
        assertTrue(params.getQueryString().indexOf(param1) != -1);
        assertTrue(params.getQueryString().indexOf(param2) != -1);
        assertTrue(params.getQueryString().indexOf(param3) != -1);
        assertTrue(params.getQueryString().indexOf(param4) != -1);
    }

    @Test
    public void testEquals()
    {
        CustomField cf = EasyMock.createMock(CustomField.class);
        CustomField cf2 = EasyMock.createMock(CustomField.class);

        EasyMock.replay(cf, cf2);

        // both custom fields are null params same
        CustomFieldParamsImpl one = new CustomFieldParamsImpl();
        CustomFieldParamsImpl two = new CustomFieldParamsImpl();
        assertTrue(one.equals(two));

        // both custom fields are null params different
        one.addValue("name", Lists.<String>newArrayList("BOB"));
        assertFalse(one.equals(two));

        // first null, second not null
        two.setCustomField(cf);
        assertFalse(one.equals(two));

        // first not null, second null
        one.setCustomField(cf);
        two.setCustomField(null);
        assertFalse(one.equals(two));

        // both not null + same, params diff
        two.setCustomField(cf);
        assertFalse(one.equals(two));

        // both not null, params same
        two.addValue("name", Lists.<String>newArrayList("BOB"));
        assertTrue(one.equals(two));

        // actually check customFields are different
        two.setCustomField(cf2);
        assertFalse(one.equals(two));

        assertTrue(one.equals(one));
    }
}
