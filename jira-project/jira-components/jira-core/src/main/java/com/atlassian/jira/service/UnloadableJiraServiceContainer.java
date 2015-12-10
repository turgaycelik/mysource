package com.atlassian.jira.service;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.services.LocalService;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * Used to represent a ServiceContainer that could not be properly loaded. This is used to represent
 * database values that are left behind from old services that are no longer deployed. We need this so
 * that the users can remove this service if need be.
 */
public class UnloadableJiraServiceContainer implements JiraServiceContainer
{
    private final String clazz;
    private long delay;
    private String name;
    private final Long id;

    public UnloadableJiraServiceContainer(final Long id, final String name, final String clazz, final long delay)
    {
        this.clazz = clazz;
        this.delay = delay;
        this.name = name;
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public long getDelay()
    {
        return delay;
    }

    public void setDelay(final long delay)
    {
        this.delay = delay;
    }

    public String getServiceClass()
    {
        return clazz;
    }

    @Override
    public Class getServiceClassObject()
    {
        try
        {
            return Class.forName(clazz);
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
    }

    @Override
    public boolean isLocalService()
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public boolean isUsable()
    {
        return false;
    }

    public Long getId()
    {
        return id;
    }

    public boolean isDueAt(final long time)
    {
        return false;
    }

    public long getLastRun()
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public void setLastRun()
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public boolean isRunning()
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public void init(final PropertySet props) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public void run()
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public void destroy()
    {
    //on shutdown jira destroys all services, so do nothing here
    }

    public boolean isInternal()
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public boolean isUnique()
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public String getDescription()
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public boolean hasProperty(final String propertyKey) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public String getProperty(final String propertyKey) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public String getTextProperty(final String propertyKey) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public Long getLongProperty(final String propertyKey) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public String getDefaultProperty(final String propertyKey) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public PropertySet getProperties() throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }

    public String getKey()
    {
        throw new UnsupportedOperationException("This method is not supported in an UnloadableJiraServiceContainer");
    }
}
