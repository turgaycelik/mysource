/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.configurable.ObjectConfigurationFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * A convenience class - if you extend this class, all that needs to be implemented is run()
 */
@PublicSpi
public abstract class AbstractService implements JiraService
{
    public static final String SERVICE_EVENT = "ServiceEvent";

    protected Logger log = Logger.getLogger(this.getClass().getName());
    protected String name;
    private PropertySet props;

    /**
     * This method must be implemented in a subclass, and performs the functionality
     * that the service performs.  This method is called after the duration specified through
     * the administration web interface.
     */
    public abstract void run();

    public void init(final PropertySet props) throws ObjectConfigurationException
    {
        this.props = props;
    }

    /**
     * This method is called when the service is unloaded (usually when the web application or server
     * is being shut down).
     * <p/>
     * You may wish to remove any connections that you have established, eg. database connections.
     */
    public void destroy()
    {}

    /**
     * By default, returns false.
     *
     * @return false
     */
    public boolean isInternal()
    {
        return false;
    }

    public String getKey()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * By default, services are not unique.
     *
     * @return false.
     */
    public boolean isUnique()
    {
        return false;
    }

    public String getDescription()
    {
        return null;
    }

    public boolean hasProperty(final String propertyKey) throws ObjectConfigurationException
    {
        return props.exists(propertyKey);
    }

    public String getProperty(final String propertyKey) throws ObjectConfigurationException
    {
        return props.getString(propertyKey);
    }

    public String getTextProperty(final String propertyKey) throws ObjectConfigurationException
    {
        return props.getText(propertyKey);
    }

    public Long getLongProperty(final String propertyKey) throws ObjectConfigurationException
    {
        final String property = getProperty(propertyKey);
        try
        {
            return new Long(property);
        }
        catch (final NumberFormatException e)
        {
            throw new ObjectConfigurationException("Could not get Long from " + property);
        }
    }

    public String getDefaultProperty(final String propertyKey) throws ObjectConfigurationException
    {
        return getObjectConfiguration().getFieldDefault(propertyKey);
    }

    public PropertySet getProperties() throws ObjectConfigurationException
    {
        return props;
    }

    protected ObjectConfiguration getObjectConfiguration(final String id, final String xmlfile, final Map<String, String[]> params) throws ObjectConfigurationException
    {
        final ObjectConfigurationFactory objectConfigurationFactory = ComponentAccessor.getComponent(ObjectConfigurationFactory.class);
        if (!objectConfigurationFactory.hasObjectConfiguration(id))
        {
            try
            {
                // JRA-19075. Pass this service's ClassLoader in case it comes from an OSGi plugin.
                objectConfigurationFactory.loadObjectConfiguration(xmlfile, id, this.getClass().getClassLoader());
            }
            catch (ObjectConfigurationException ex)
            {
                // Add the Service context and re-throw.
                throw new ObjectConfigurationException("Unable to load configuration for Service '" + getName() + "'. " + ex.getMessage(), ex);
            }
        }
        return objectConfigurationFactory.getObjectConfiguration(id, params);
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }
}
