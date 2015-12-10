package com.atlassian.jira.web.action.admin.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opensymphony.workflow.loader.AbstractDescriptor;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for the AbstractAddWorkflowTransitionDescriptorParams class.
 */
public class TestAbstractEditWorkflowTransitionDescriptorParams
{
    /**
     * Mock class only used to instantiate AbstractEditWorkflowTransitionDescriptorParams
     * so it can be tested. None of the methods work.
     */
    private final class MockChangeWorkflowTransitionDescriptorParams extends AbstractEditWorkflowTransitionDescriptorParams
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

        ////////////////////////////////////////

        protected String getPluginType()
        {
            return null;
        }

        protected void setupWorkflowDescriptor()
        {
        }

        protected String getHighLightParamPrefix()
        {
            return null;
        }

        protected void editWorkflowDescriptor(AbstractDescriptor descriptor, Map params)
        {
        }
    }

    @Test
    public void testSetupWorkflowDescriptorParams()
    {
        MockChangeWorkflowTransitionDescriptorParams adder = new MockChangeWorkflowTransitionDescriptorParams();
        assertThat(adder.errors, Matchers.<String>empty());
        adder.setupWorkflowDescriptorParams(new HashMap());
        Map params = adder.getDescriptorParams();
        assertTrue(params.isEmpty());
        assertEquals(4, adder.errors.size());
        assertThat(adder.errors, Matchers.<String>hasItem("admin.errors.workflows.cannot.find.name"));
        assertThat(adder.errors, Matchers.<String>hasItem("admin.errors.workflows.cannot.find.step"));
        assertThat(adder.errors, Matchers.<String>hasItem("admin.errors.workflows.cannot.find.transition"));
        assertThat(adder.errors, Matchers.<String>hasItem("admin.errors.workflows.cannot.find.count"));
    }
}
