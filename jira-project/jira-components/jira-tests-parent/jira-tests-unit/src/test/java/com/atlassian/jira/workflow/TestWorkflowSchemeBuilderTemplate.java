package com.atlassian.jira.workflow;

import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.Maps;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v5.2
 */
public class TestWorkflowSchemeBuilderTemplate
{
    @Test
    public void testBuilder()
    {
        TestBuilder testBuilder = new TestBuilder();

        assertTrue(testBuilder.getMappings().isEmpty());
        assertNull(testBuilder.getId());

        checkBuilder(testBuilder, Collections.<String, String>emptyMap());
    }

    @Test
    public void testCotr()
    {
        MockWorkflowScheme scheme = new MockAssignableWorkflowScheme();
        scheme.setId(10L)
                .setDefaultWorkflow("wtf").setMapping("one", "two");

        Map<String, String> initialMap = MapBuilder.build(null, "wtf", "one", "two");

        TestBuilder testBuilder = new TestBuilder(scheme);
        assertEquals(initialMap, testBuilder.getMappings());
        assertEquals(scheme.getId(), testBuilder.getId());

        checkBuilder(testBuilder, initialMap);
    }

    private void checkBuilder(TestBuilder testBuilder, Map<String, String> initialMap)
    {
        testBuilder.setMapping("1", "mapping");

        Map<String, String> mappingsBuilder = Maps.newHashMap(initialMap);
        mappingsBuilder.put("1", "mapping");

        assertEquals(mappingsBuilder, testBuilder.getMappings());

        testBuilder.setDefaultWorkflow("default");
        mappingsBuilder.put(null, "default");

        assertEquals("default", testBuilder.getDefaultWorkflow());
        assertEquals(mappingsBuilder, testBuilder.getMappings());

        testBuilder.setMapping("2", "another");
        testBuilder.setMapping("1", "wf");

        mappingsBuilder.put("2", "another");
        mappingsBuilder.put("1", "wf");

        assertEquals(mappingsBuilder, testBuilder.getMappings());
        assertEquals("another", testBuilder.getMapping("2"));
        assertEquals("wf", testBuilder.getMapping("1"));
        assertEquals(null, testBuilder.getMapping("3"));

        testBuilder.removeDefault();
        testBuilder.removeMapping("2");
        testBuilder.removeMapping("3");

        mappingsBuilder.remove(null);
        mappingsBuilder.remove("2");
        mappingsBuilder.remove("3");

        assertEquals(mappingsBuilder, testBuilder.getMappings());

        mappingsBuilder = Maps.newHashMap();
        mappingsBuilder.put("1", "kill");
        testBuilder.setMappings(mappingsBuilder);

        assertEquals(mappingsBuilder, testBuilder.getMappings());
        assertNotSame(mappingsBuilder, testBuilder.getMappings());

        mappingsBuilder.put("2", "kill");

        assertFalse(mappingsBuilder.equals(testBuilder.getMappings()));
        assertTrue(testBuilder.clearMappings().getMappings().isEmpty());

        testBuilder.setMapping("2", "delete");
        testBuilder.setMapping("3", "deleteLater");
        testBuilder.setMapping("4", "neverDelete");
        testBuilder.setDefaultWorkflow("deleteLater");

        mappingsBuilder.clear();
        mappingsBuilder.put("3", "deleteLater");
        mappingsBuilder.put("4", "neverDelete");
        mappingsBuilder.put(null, "deleteLater");

        testBuilder.removeWorkflow("delete");
        assertEquals(mappingsBuilder, testBuilder.getMappings());

        mappingsBuilder.remove("3");
        mappingsBuilder.remove(null);

        testBuilder.removeWorkflow("deleteLater");
        assertEquals(mappingsBuilder, testBuilder.getMappings());
    }

    private static class TestBuilder extends WorkflowSchemeBuilderTemplate<TestBuilder>
    {
        private TestBuilder()
        {

        }

        private TestBuilder(WorkflowScheme scheme)
        {
            super(scheme);
        }

        @Override
        TestBuilder builder()
        {
            return this;
        }

        @Override
        public boolean isDraft()
        {
            return false;
        }

        @Override
        public boolean isDefault()
        {
            return false;
        }

        @Override
        public String getDescription()
        {
            return null;
        }

        @Override
        public String getName()
        {
            return null;
        }
    }

}
