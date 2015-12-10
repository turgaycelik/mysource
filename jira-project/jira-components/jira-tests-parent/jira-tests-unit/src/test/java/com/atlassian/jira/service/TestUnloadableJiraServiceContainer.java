package com.atlassian.jira.service;

import com.atlassian.configurable.ObjectConfigurationException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class TestUnloadableJiraServiceContainer
{
    private static final Long SERVICE_ID = new Long(1);
    private static final String SERVICE_NAME = "unloadable name";
    private static final String SERVICE_CLASS = "com.test.class";
    private static final int SERVICE_DELAY = 2;

    @Test
    public void testUnloadableJiraServiceContainerSetName()
    {
        final JiraServiceContainer unloadableJiraService = getServiceContainer();
        assertEquals(SERVICE_NAME, unloadableJiraService.getName());
        unloadableJiraService.setName("new name");
        assertEquals("new name", unloadableJiraService.getName());
    }

    @Test
    public void testUnloadableJiraServiceContainerSetDelay()
    {
        final JiraServiceContainer unloadableJiraService = getServiceContainer();
        assertEquals(SERVICE_DELAY, unloadableJiraService.getDelay());
        unloadableJiraService.setDelay(3);
        assertEquals(3, unloadableJiraService.getDelay());
    }

    @Test
    public void testUnloadableJiraServiceContainerGetServiceClass()
    {
        assertEquals(SERVICE_CLASS, getServiceContainer().getServiceClass());
    }

    @Test
    public void testUnloadableJiraServiceContainerUnusable()
    {
        assertFalse("UnloadableJiraServiceContainer should not be usable", getServiceContainer().isUsable());
    }

    @Test
    public void testUnloadableJiraServiceContainerId()
    {
        assertEquals(SERVICE_ID, getServiceContainer().getId());
    }

    @Test
    public void testUnloadableJiraServiceContainerDestroy()
    {
        try
        {
            getServiceContainer().destroy();
        }
        catch (final UnsupportedOperationException e)
        {
            //JIRA calls destroy for all services when shutting down - JRA-12338
            fail("UnloadableJiraServiceContainer should not throw UnsupportedOperationException for destroy method");
        }
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedGetLastRun()
    {
        //----------------------------------------------------- all the methods that throw UnsupportedOperationException
        try
        {
            getServiceContainer().getLastRun();
            failForMethod("getLastRun");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedSetLastRun()
    {
        try
        {
            getServiceContainer().setLastRun();
            failForMethod("setLastRun");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedIsRunning()
    {
        try
        {
            getServiceContainer().isRunning();
            failForMethod("isRunning");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedInit() throws ObjectConfigurationException
    {
        try
        {
            getServiceContainer().init(null);
            failForMethod("init");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedRun()
    {
        try
        {
            getServiceContainer().run();
            failForMethod("run");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedIsInternal()
    {
        try
        {
            getServiceContainer().isInternal();
            failForMethod("isInternal");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedIsUnique()
    {
        try
        {
            getServiceContainer().isUnique();
            failForMethod("isUnique");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedGetDescription()
    {
        try
        {
            getServiceContainer().getDescription();
            failForMethod("getDescription");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedGetObjectConfiguration() throws ObjectConfigurationException
    {
        try
        {
            getServiceContainer().getObjectConfiguration();
            failForMethod("getObjectConfiguration");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedHasProperty() throws ObjectConfigurationException
    {
        try
        {
            getServiceContainer().hasProperty("");
            failForMethod("hasProperty");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedGetProperty() throws ObjectConfigurationException
    {
        try
        {
            getServiceContainer().getProperty("");
            failForMethod("getProperty");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedGetTextProperty() throws ObjectConfigurationException
    {
        try
        {
            getServiceContainer().getTextProperty("");
            failForMethod("getTextProperty");
        }
        catch (final UnsupportedOperationException expected)
        {}

    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedGetLongProperty() throws ObjectConfigurationException
    {
        try
        {
            getServiceContainer().getLongProperty("");
            failForMethod("getLongProperty");
        }
        catch (final UnsupportedOperationException expected)
        {}

    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedGetDefaultProperty() throws ObjectConfigurationException
    {
        try
        {
            getServiceContainer().getDefaultProperty("");
            failForMethod("getDefualtProperty");
        }
        catch (final UnsupportedOperationException expected)
        {}

    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedGetProperties() throws ObjectConfigurationException
    {
        try
        {
            getServiceContainer().getProperties();
            failForMethod("getProperties");
        }
        catch (final UnsupportedOperationException expected)
        {}

    }

    @Test
    public void testUnloadableJiraServiceContainerUnsupportedGetKey()
    {
        try
        {
            getServiceContainer().getKey();
            failForMethod("getKey");
        }
        catch (final UnsupportedOperationException expected)
        {}
    }

    private JiraServiceContainer getServiceContainer()
    {
        return new UnloadableJiraServiceContainer(SERVICE_ID, SERVICE_NAME, SERVICE_CLASS, SERVICE_DELAY);
    }

    private void failForMethod(final String methodName)
    {
        fail("Expected method '" + methodName + "' of UnloadableJiraServiceContainer to throw UnsupportedOperationException");
    }
}
