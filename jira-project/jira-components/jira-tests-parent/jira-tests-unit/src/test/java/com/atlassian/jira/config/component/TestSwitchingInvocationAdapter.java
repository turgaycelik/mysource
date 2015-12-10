package com.atlassian.jira.config.component;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;

import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

import static org.junit.Assert.assertEquals;

public class TestSwitchingInvocationAdapter
{
    MyApplicationProperties applicationProperties = new MyApplicationProperties();

    @Test
    public void testSwap()
    {
        MutablePicoContainer container = getContainer();
        applicationProperties.setEnabled(true);

        AppPropertiesComponentAdaptor componentAdaptor = new AppPropertiesComponentAdaptor(container, IntX.class, EnabledClass.class, DisabledClass.class, "key");

        assertEquals(IntX.class, componentAdaptor.getComponentKey());
        // Get the component Instance
        IntX componentInstance = (IntX) componentAdaptor.getComponentInstance(container);
        // Should start life as enabled.
        assertEquals("enabled", componentInstance.getName());

        // Change to disabled
        applicationProperties.setEnabled(false);
        // Should dynamically switch
        assertEquals("disabled", componentInstance.getName());

        // Change back to enabled just to be sure...
        applicationProperties.setEnabled(true);
        // and assert teh switch back.
        assertEquals("enabled", componentInstance.getName());
    }

    private MutablePicoContainer getContainer()
    {
        MutablePicoContainer container = new DefaultPicoContainer();
        container.addComponent(EnabledClass.class);
        container.addComponent(DisabledClass.class);

        container.addComponent(ApplicationProperties.class, applicationProperties);

        return container;
    }

    public static interface IntX
    {
        public String getName();
    }

    public static class EnabledClass implements IntX
    {
        public String getName()
        {
            return "enabled";
        }
    }

    public static class DisabledClass implements IntX
    {
        public String getName()
        {
            return "disabled";
        }
    }

    private static class MyApplicationProperties extends ApplicationPropertiesImpl
    {
        private boolean enabled;

        private MyApplicationProperties() {super(null);}

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }

        public boolean getOption(String key)
        {
            if ("key".equals(key))
                return enabled;
            throw new IllegalArgumentException("Can only call this with 'key'");
        }
    }

}
