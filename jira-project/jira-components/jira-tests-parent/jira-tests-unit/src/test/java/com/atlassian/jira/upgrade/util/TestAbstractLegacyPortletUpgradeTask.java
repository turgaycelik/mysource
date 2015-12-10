package com.atlassian.jira.upgrade.util;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mock.propertyset.MockPropertySet;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestAbstractLegacyPortletUpgradeTask
{
    @Test
    public void testConvertUserPrefsEmptyPropertySet()
    {
        AbstractLegacyPortletUpgradeTask task = getUpgradeTask();
        MockPropertySet ps = new MockPropertySet();

        final Map<String, String> ret = task.convertUserPrefs(ps);
        assertEquals(Collections.<String, String>emptyMap(), ret);
    }

    @Test
    public void testConvertUserPrefs()
    {
        AbstractLegacyPortletUpgradeTask task = getUpgradeTask();
        final Date date = new Date();
        //should really only be able to deal with strings but you never know with propertysets.
        final Map<String, Object> properties = MapBuilder.<String, Object>newBuilder().
                add("aLong", 100L).
                add("aString", "Cows").
                add("aBool", Boolean.TRUE).
                add("aDate", date).
                add("aMultValue", "value1_*|*_value2_*|*_val|ue3_*|*_value4").
                add("nullValue", null).toMap();

        MockPropertySet ps = new MockPropertySet(properties);

        final Map<String, String> ret = task.convertUserPrefs(ps);
        Map<String, String> expected = MapBuilder.<String, String>newBuilder().
                add("aLong", "100").
                add("aString", "Cows").
                add("aBool", "true").
                add("aDate", date.toString()).
                //'|' in a value should be % encoded.
                add("aMultValue", "value1|value2|val%7Cue3|value4").
                add("nullValue", null).toMap();
        assertEquals(expected, ret);
    }

    private AbstractLegacyPortletUpgradeTask getUpgradeTask()
    {
        AbstractLegacyPortletUpgradeTask task = new AbstractLegacyPortletUpgradeTask()
        {
            public String getPortletKey()
            {
                return null;
            }

            public URI getGadgetUri()
            {
                return null;
            }
        };
        return task;
    }
}
