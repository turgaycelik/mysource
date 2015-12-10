package com.atlassian.jira.workflow;

import java.util.Map;

import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v5.2
 */
public class TestWorkflowSchemeImpl
{
    @Test
    public void testBuilder()
    {
        String name = "name";
        String description = "description";
        Map<String,String> mappings = MapBuilder.build(null, "wtf", "one", "two");
        Long id = 78L;

        MockAssignableWorkflowScheme scheme = new MockAssignableWorkflowScheme(id, name, description);
        scheme.setMapping("one", "two").setDefaultWorkflow("wtf");
        AssignableWorkflowScheme copy = scheme.builder().build();

        assertEquals(name, copy.getName());
        assertEquals(description, copy.getDescription());
        assertEquals(mappings, copy.getMappings());
        assertFalse(copy.isDraft());
        assertFalse(copy.isDefault());
    }
}
