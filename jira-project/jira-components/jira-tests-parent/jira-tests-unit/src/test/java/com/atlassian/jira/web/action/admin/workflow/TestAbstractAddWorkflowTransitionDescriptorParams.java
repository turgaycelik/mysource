package com.atlassian.jira.web.action.admin.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for the AbstractAddWorkflowTransitionDescriptorParams class.
 */
public class TestAbstractAddWorkflowTransitionDescriptorParams
{
    /**
     * Mock class only used to instantiate AbstractAddWorkflowTransitionDescriptorParams
     * so it can be tested. None of the methods work.
     */
    private final class MockChangeWorkflowTransitionDescriptorParams extends AbstractAddWorkflowTransitionDescriptorParams
    {

        private final List<String> errors = new ArrayList<String>();

        public MockChangeWorkflowTransitionDescriptorParams()
        {
            super(null, null, null, null);
        }

        protected Class getWorkflowModuleDescriptorClass()
        {
            return null;
        }

        protected void addWorkflowDescriptor()
        {

        }

        public String getWorkflowDescriptorName()
        {
            return null;
        }

        public String getText(String key)
        {
            return key;
        }

        public void addErrorMessage(String string)
        {
            errors.add(string);
        }

    }


    @Test
    public void testSetupWorkflowDescriptorParams()
    {
        MockChangeWorkflowTransitionDescriptorParams adder = new MockChangeWorkflowTransitionDescriptorParams();
        assertThat(adder.errors, Matchers.<String>empty());
        adder.setupWorkflowDescriptorParams(new HashMap());
        Map<?, ?> params = adder.getDescriptorParams();
        assertTrue(params.isEmpty());
        assertEquals(6, adder.errors.size());
        assertThat(adder.errors, Matchers.<String>hasItem("admin.errors.workflows.cannot.find.name"));
        assertThat(adder.errors, Matchers.<String>hasItem("admin.errors.workflows.cannot.find.step"));
        assertThat(adder.errors, Matchers.<String>hasItem("admin.errors.workflows.cannot.find.transition"));
        assertThat(adder.errors, Matchers.<String>hasItem("admin.errors.workflows.cannot.find.cannot.find.plugin.module.key"));
        assertThat(adder.errors, Matchers.<String>hasItem("admin.errors.workflows.cannot.find.count"));
        assertThat(adder.errors, Matchers.<String>hasItem("admin.errors.workflows.cannot.find.nested"));
    }

    @Test
    public void testSetupWithStartingMap()
    {
        MockChangeWorkflowTransitionDescriptorParams adder = new MockChangeWorkflowTransitionDescriptorParams();
        Map<String, String> existing = ImmutableMap.of("count", "admin.errors.workflows.cannot.find.count");
        assertThat(existing, Matchers.hasKey("count"));
        adder.setupWorkflowDescriptorParams(existing);
        assertEquals(5, adder.errors.size());
        Map params = adder.getDescriptorParams();
        assertFalse(params.containsKey("count"));
    }
}
