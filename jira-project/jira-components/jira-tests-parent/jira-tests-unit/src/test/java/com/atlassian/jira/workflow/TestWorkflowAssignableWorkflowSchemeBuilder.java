package com.atlassian.jira.workflow;

import java.util.Map;

import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v5.2
 */
public class TestWorkflowAssignableWorkflowSchemeBuilder
{
    @Test
    public void testBuilder()
    {
        String name = "name";
        String description = "description";
        Map<String,String> mappings = MapBuilder.build(null, "wtf", "one", "two");
        Long id = 78L;

        MockAssignableWorkflowScheme scheme = new MockAssignableWorkflowScheme(id, name, description);
        scheme.setDefaultWorkflow("wtf").setMapping("one", "two");
        AssignableWorkflowSchemeBuilder builder = new AssignableWorkflowSchemeBuilder(scheme);

        assertEquals(name, builder.getName());
        assertEquals(description, builder.getDescription());
        assertEquals(mappings, builder.getMappings());
        assertEquals(id, builder.getId());
        assertEquals("wtf", builder.getDefaultWorkflow());
        assertEquals("two", builder.getMapping("one"));
        assertFalse(builder.isDraft());
        assertFalse(builder.isDefault());


        AssignableWorkflowScheme copy = builder.build();
        assertEquals(name, copy.getName());
        assertEquals(description, copy.getDescription());
        assertEquals(mappings, copy.getMappings());
        assertFalse(copy.isDraft());
        assertFalse(copy.isDefault());
    }
}
