package com.atlassian.jira.config.webwork;

import webwork.config.ConfigurationInterface;

import java.util.Iterator;

/**
 * Simply delegates to another ConfigurationInterface but allows the ConfigurationInterface to be changed out for 
 * another one.  This is used by the WebworkConfigurator to set a new ConfigurationInterface when the system is 
 * restarted.  We need to do this to work around the check in the Configuration.setConfiguration() static method that 
 * disallows the ConfigurationInterface instance to be set to something different after it has been set once.
 * 
 * @since v3.13
 */
public class DelegatingConfiguration implements ConfigurationInterface
{
    private volatile ConfigurationInterface delegate;

    public DelegatingConfiguration()
    {}

    public DelegatingConfiguration(final ConfigurationInterface delegate)
    {
        this.delegate = delegate;
    }

    public Object getImpl(final String name) throws IllegalArgumentException
    {
        return delegate.getImpl(name);
    }

    public void setImpl(final String name, final Object impl) throws IllegalArgumentException, UnsupportedOperationException
    {
        delegate.setImpl(name, impl);
    }

    public Iterator listImpl()
    {
        return delegate.listImpl();
    }

    void setDelegateConfiguration(final ConfigurationInterface delegate)
    {
        this.delegate = delegate;
    }
}
