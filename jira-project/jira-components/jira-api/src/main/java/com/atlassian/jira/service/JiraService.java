/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.configurable.ObjectConfigurable;
import com.atlassian.configurable.ObjectConfigurationException;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.Comparator;

/**
 * Classes that are to be run as services within JIRA must implement this interface.
 */
@PublicSpi
public interface JiraService extends ObjectConfigurable, Runnable
{
    /**
     * Initialise the service.  This method is guaranteed to be called before the first call to run().
     * <p/>
     * As the parameters are gained from the user's interaction with the website, it
     * is not guaranteed to be called with the correct, or indeed with any parameters.
     * <p/>
     * init() may be called multiple times during the services lifetime.
     *
     * @param props initialisation parameters
     * @throws ObjectConfigurationException in case of an error with initialisation parameters
     */
    void init(PropertySet props) throws ObjectConfigurationException;

    /**
     * Perform the action of this service.  The caller of this method assumes that no housekeeping has been done, and
     * will call setLastRun() after the run() method.
     * <p/>
     * init() is guaranteed to be called before run(), but the parameters passed to init() are not guaranteed to be
     * correct.  Any parameters expected to be set by init() should be checked in this method upon each invocation.
     *
     * @see #init
     */
    void run();

    /**
     * This method is called when the service is unloaded (usually when the web application or server
     * is being shut down).
     * <p/>
     * You may wish to remove any connections that you have established, eg. database connections.
     */
    void destroy();

    /**
     * Indicates whether administrators can delete this service from within the web interface.
     * <p/>
     * Generally only Atlassian services should return true from this.
     *
     * @return true if this service is internal to JIRA, false otherwise
     */
    boolean isInternal();

    /**
     * Whether this service class should be unique.  Some service are fine to have multiples, and some are not.
     * <p/>
     * Having multiple backup services could be fine - perhaps you want to backup once an hour, and also once a day.
     * <p/>
     * With other services, you may wish to enforce their uniqueness
     *
     * @return Whether this service class should be unique.
     */
    boolean isUnique();

    /**
     * A textual description of the service.  You can include HTML if required, but do not use tables, or DHTML, as
     * the description may be displayed inside tables / frames.
     * <p/>
     * A good description will describe what this service does, and then explains the parameters required for
     * configuring the service.
     * <p/>
     * If no description is appropriate, return null.
     *
     * @return A HTML description of the service
     */
    String getDescription();

    /**
     * A textual name of the service as entered by the user from the web interface.
     * <p/>
     * The name should be unique to identify services of the same class.<br>
     * This is enforced when adding new services via the web interface.
     * <p/>
     * The value of name does not effect the service, hence can be set to null
     *
     * @return The name of the service
     */
    String getName();

    /**
     * Used to set the service's name. For details on the services name see {@link #getName()}
     *
     * @param name service name to set
     */
    void setName(String name);

    /**
     * Compares two given JiraService objects by their names and returns the result of the comparison.
     */
    final Comparator<JiraService> NAME_COMPARATOR = new Comparator<JiraService>()
    {
        public int compare(final JiraService js1, final JiraService js2)
        {
            return js1.getName().compareTo(js2.getName());
        }
    };
}
