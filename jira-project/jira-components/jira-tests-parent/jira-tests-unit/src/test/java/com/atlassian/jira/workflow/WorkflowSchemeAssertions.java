package com.atlassian.jira.workflow;

import java.util.Map;

import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @since v5.2
 */
class WorkflowSchemeAssertions
{
    static void checkInvalidMapping(WorkflowSchemeStore.State.Builder<?> builder, String issueType, String workflowName)
    {
        Map<String, String> validMappings = Maps.newHashMap(builder.getMappings());
        Map<String, String> newMappings = Maps.newHashMap(builder.getMappings());
        newMappings.put(issueType, workflowName);
        try
        {
            builder.setMappings(newMappings);
            fail("Expected an error");
        }
        catch (IllegalArgumentException expected)
        {
            //expected
        }
        assertEquals(validMappings, builder.getMappings());
    }

    static Map<String, String> assertSetMapping(WorkflowSchemeStore.State.Builder<?> builder)
    {
        String string256 = StringUtils.repeat("a", 256);
        String string255 = StringUtils.repeat("a", 255);

        //Workflow name and issue type name must be non-null and < 255 characters.
        MapBuilder<String, String> mappings = MapBuilder.newBuilder();
        builder.setMappings(mappings.toHashMap());
        assertEquals(mappings.toHashMap(), builder.getMappings());

        mappings.add(string255, string255);
        builder.setMappings(mappings.toHashMap());
        assertEquals(mappings.toHashMap(), builder.getMappings());

        mappings.add(null, string255);
        builder.setMappings(mappings.toHashMap());
        assertEquals(mappings.toHashMap(), builder.getMappings());

        checkInvalidMapping(builder, string256, string255);
        checkInvalidMapping(builder, string255, null);
        checkInvalidMapping(builder, string255, string256);
        checkInvalidMapping(builder, "   ", string255);
        checkInvalidMapping(builder, string255, "   ");

        return mappings.toHashMap();
    }
}
