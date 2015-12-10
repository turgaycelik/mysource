/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.net.NetworkUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.component.PicoContainerFactory;
import com.atlassian.jira.config.properties.APKeys;

import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Miscellaneous utility methods. Most have moved into more specific classes.
 *
 * @see JiraEntityUtils
 * @see JiraKeyUtils
 * @see JiraTypeUtils
 * @see com.atlassian.jira.user.util.UserUtil
 * @see AttachmentUtils
 */
public class JiraUtils
{

    private static final Logger LOG = LoggerFactory.getLogger(JiraUtils.class);
    public static final String AM = "am";
    public static final String PM = "pm";

    /**
     * Returns true if JIRA is <strong>really</strong> running in public mode.
     *
     * <p> That is, <em>JIRA Mode</em> is enabled AND there is at least one writable User Directory.
     *
     * <p> JRA-15966 Public mode should only be allowed if External User Management is disabled, otherwise we cannot
     * create a new user in JIRA.
     * Older versions of JIRA would allow the admin to set up an invalid combination of both External User Management
     * and Public Mode enabled, and so we need to account for this possibility.
     *
     * @return true if we allow public signup in this JIRA instance.
     */
    public static boolean isPublicMode()
    {
        final String signupMode = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_MODE);
        boolean publicMode = signupMode == null || signupMode.equals("public");
        return publicMode && ComponentAccessor.getUserManager().hasWritableDirectory();
    }

    /**
     * This method will load and construct a class, but also pass it through PicoContainer, to inject any dependencies.
     *
     * @param className the name of the class to load.
     * @param callingClass the class requesting the class be loaded.
     * @throws ClassNotFoundException if the class is not found on the classpath of the classloader of the calling class.
     * @see ClassLoaderUtils#loadClass(java.lang.String,java.lang.Class)
     * @see ComponentManager
     * @return the instance of the requested class.
     */
    public static <T> T loadComponent(final String className, final Class<?> callingClass) throws ClassNotFoundException
    {
        @SuppressWarnings("unchecked")
        final Class<T> componentClass = ClassLoaderUtils.loadClass(className, callingClass);
        return loadComponent(componentClass);
    }

    /**
     * This method will load and construct a class, but also pass it through PicoContainer, to inject any dependencies.
     *
     * Please note that this method will only find Plugins2 classes in plugins that are enabled, and cannot inject plugins2 components.
     *
     * @param className the name of the class to load
     * @param classLoader the classloader to use to load the class
     * @return the instance of the requested class
     * @throws ClassNotFoundException if the given className was not able to be loaded.
     *
     * @deprecated This method does not work well with Plugins2 classes and dependency injection. Please use {@link com.atlassian.jira.plugin.ComponentClassManager}.
     */
    public static <T> T loadComponent(final String className, final ClassLoader classLoader)
            throws ClassNotFoundException
    {
        @SuppressWarnings("unchecked")
        final Class<T> componentClass = ClassLoaderUtils.loadClass(className, classLoader);
        return loadComponent(componentClass);
    }

    public static <T> T loadComponent(final Class<T> componentClass)
    {
        return loadComponent(componentClass, Collections.emptyList());
    }

    public static <T> T loadComponent(final Class<T> componentClass, final Collection<Object> extraParameters)
    {
        if (componentClass.isAssignableFrom(Void.class))
        {
            // TODO work out why we try and ask Pico to instantiate this...
            return null;
        }
        final PicoContainer applicationContainer = ComponentManager.getInstance().getContainer();
        final MutablePicoContainer tempContainer = PicoContainerFactory.defaultJIRAContainer(applicationContainer);

        for (final Object parameter : extraParameters)
        {
            // Register the dependency
            tempContainer.addComponent(parameter);
        }

        //register the class that we need.
        tempContainer.addComponent(componentClass);
        // Instantiate the object
        return componentClass.cast(tempContainer.getComponent(componentClass));
    }

    /**
     * Calculate meridian adjustment add 12 hours if meridian is set to PM - note 12pm is 12:00 in 24 hour time.
     *
     * @param meridianIndicator am or pm
     * @param hours             1-12
     * @return 1-24
     */
    public static int get24HourTime(final String meridianIndicator, final int hours)
    {
        // two special cases 12 AM & 12 PM
        if (hours == 12)
        {
            if (AM.equalsIgnoreCase(meridianIndicator))
            {
                return 0;
            }

            if (PM.equalsIgnoreCase(meridianIndicator))
            {
                return 12;
            }
        }

        final int onceMeridianAdjustment = PM.equalsIgnoreCase(meridianIndicator) ? 12 : 0;
        return hours + onceMeridianAdjustment;
    }

    public static boolean isSetup()
    {
        return (ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_SETUP) != null);
    }

    /**
     * Returns the server ip where the vm is running.
     * @return the ip or if error 0.0.0.0
     */
    public static String getHostname()
    {
        try
        {
            return NetworkUtils.getLocalHostName();
        }
        catch (UnknownHostException e)
        {
            LOG.warn("IP/Hostname address cannot be calculated for this host. Please fix this.");
            return "0.0.0.0";
        }
    }
}
