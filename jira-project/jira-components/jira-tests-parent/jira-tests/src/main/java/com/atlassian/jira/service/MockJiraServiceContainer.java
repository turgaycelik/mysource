package com.atlassian.jira.service;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.services.LocalService;

import com.opensymphony.module.propertyset.PropertySet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class MockJiraServiceContainer implements JiraServiceContainer
{
    private final Integer id;
    private final boolean usable;
    private final boolean running;
    private final boolean due;
    private final String serviceClass;

    private MockJiraServiceContainer(final Integer id, boolean usable, boolean running, boolean due, final String serviceClass)
    {
        this.id = id;
        this.usable = usable;
        this.running = running;
        this.due = due;
        this.serviceClass = serviceClass;
    }

    public long getDelay()
    {
        return 100000;
    }

    public Long getId()
    {
        return id.longValue();
    }

    public long getLastRun()
    {
        return System.currentTimeMillis() - 500000;
    }

    public boolean isDueAt(final long time)
    {
        return due;
    }

    public String getServiceClass()
    {
        return serviceClass;
    }

    @Override
    public Class getServiceClassObject()
    {
        try
        {
            return Class.forName(serviceClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean isRunning()
    {
        return running;
    }

    @Override
    public boolean isLocalService()
    {
        return LocalService.class.isAssignableFrom(getServiceClassObject());
    }

    public boolean isUsable()
    {
        return usable;
    }

    public void setDelay(final long delay)
    {
        throw new UnsupportedOperationException();
    }

    public void setLastRun()
    {
        throw new UnsupportedOperationException();
    }

    public void setRunning(final boolean running)
    {
        throw new UnsupportedOperationException();
    }

    public void destroy()
    {
        throw new UnsupportedOperationException();
    }

    public String getDescription()
    {
        throw new UnsupportedOperationException();
    }

    public String getName()
    {
        throw new UnsupportedOperationException();
    }

    public void init(final PropertySet props) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    public boolean isInternal()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isUnique()
    {
        throw new UnsupportedOperationException();
    }

    public void run()
    {
        throw new UnsupportedOperationException();
    }

    public void setName(final String name)
    {
        throw new UnsupportedOperationException();
    }

    public String getDefaultProperty(final String propertyKey) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    public String getKey()
    {
        throw new UnsupportedOperationException();
    }

    public Long getLongProperty(final String propertyKey) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return null;
    }

    public PropertySet getProperties() throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    public String getProperty(final String propertyKey) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    public String getTextProperty(final String propertyKey) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasProperty(final String propertyKey) throws ObjectConfigurationException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) { return true; }

        if (!(obj instanceof MockJiraServiceContainer)) { return false; }

        MockJiraServiceContainer rhs = (MockJiraServiceContainer) obj;

        return new EqualsBuilder().
                append(id, rhs.id).
                append(serviceClass, rhs.serviceClass).
                isEquals();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("id", id).
                append("usable", usable).
                append("running", running).
                append("due", due).
                append("serviceClass", serviceClass).
                toString();
    }

    public static class Builder
    {
        private int id;
        private boolean usable;
        private boolean running = false;
        private boolean due = false;
        private String serviceClass = "";

        public Builder id(int id)
        {
            this.id = id;
            return this;
        }

        public Builder usable(boolean usable)
        {
            this.usable = usable;
            return this;
        }

        public Builder running(boolean running)
        {
            this.running = running;
            return this;
        }

        public Builder due(boolean due)
        {
            this.due = due;
            return this;
        }

        public Builder serviceClass(String serviceClass)
        {
            this.serviceClass = serviceClass;
            return this;
        }

        public MockJiraServiceContainer build()
        {
            return new MockJiraServiceContainer(id, usable, running, due, serviceClass);
        }
    }
}
