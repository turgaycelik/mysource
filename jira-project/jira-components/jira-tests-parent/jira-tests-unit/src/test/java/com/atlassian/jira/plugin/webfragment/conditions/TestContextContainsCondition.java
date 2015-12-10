package com.atlassian.jira.plugin.webfragment.conditions;

import java.util.HashMap;

import com.atlassian.plugin.PluginParseException;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestContextContainsCondition
{

    @Test
    public void testNullParams()
    {
        final ContextContainsCondition condition = new ContextContainsCondition();

        try
        {
            final HashMap<String, Object> map = new HashMap<String, Object>();
            condition.init(map);
            fail("Shoould have failed");

            map.put(ContextContainsCondition.CONTEXT_KEY, "something");

            condition.init(map);
            fail("Shoould have failed");

            map.put(ContextContainsCondition.CONTEXT_VALUE, "something");
            map.put(ContextContainsCondition.CONTEXT_KEY, null);

            condition.init(map);
            fail("Shoould have failed");

        }
        catch (PluginParseException e)
        {
            // good
        }
    }


    @Test
    public void testNoObject()
    {
        final ContextContainsCondition condition = new ContextContainsCondition();
        final HashMap<String, Object> initMap = new HashMap<String, Object>();
        initMap.put(ContextContainsCondition.CONTEXT_VALUE, "something-value");
        initMap.put(ContextContainsCondition.CONTEXT_KEY, "something-key");

        final HashMap<String, Object> contextMap = new HashMap<String, Object>();

        condition.init(initMap);

        assertFalse(condition.shouldDisplay(contextMap));
    }

    @Test
    public void testObjectNotEqual()
    {
        final ContextContainsCondition condition = new ContextContainsCondition();
        final HashMap<String, Object> initMap = new HashMap<String, Object>();
        initMap.put(ContextContainsCondition.CONTEXT_VALUE, "something-value");
        initMap.put(ContextContainsCondition.CONTEXT_KEY, "something-key");

        final HashMap<String, Object> contextMap = new HashMap<String, Object>();
        contextMap.put("something-key", "another-value");

        condition.init(initMap);

        assertFalse(condition.shouldDisplay(contextMap));
    }   

    @Test
    public void testObjectEqual()
    {
        final ContextContainsCondition condition = new ContextContainsCondition();
        final HashMap<String, Object> initMap = new HashMap<String, Object>();
        initMap.put(ContextContainsCondition.CONTEXT_VALUE, "something-value");
        initMap.put(ContextContainsCondition.CONTEXT_KEY, "something-key");

        final HashMap<String, Object> contextMap = new HashMap<String, Object>();
        contextMap.put("something-key", "something-value");

        condition.init(initMap);

        assertTrue(condition.shouldDisplay(contextMap));
    }
}
