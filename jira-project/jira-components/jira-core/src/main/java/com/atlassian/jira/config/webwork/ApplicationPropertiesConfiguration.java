package com.atlassian.jira.config.webwork;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import webwork.config.ConfigurationInterface;
import webwork.config.WebworkConfigurationNotFoundException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A webwork configuration class that will add JIRA's Application properties to the configuration stack.
 * <p/>
 * In practice we only server a few properties such as max upload size and so on and these are not in the actual
 * properties file but in the database (as set by the user).
 */
public class ApplicationPropertiesConfiguration implements ConfigurationInterface
{
    public ApplicationPropertiesConfiguration()
    {
    }

    /*
    * Its expensive to look into the database and the cache the values for keys we dont know about
     * and yet the old naive implementation did just that.  So we should only respond to keys we
     * know about. JRA_24893
     *
     * And since its only ever webwork that is asking us for values and we only respond to two 'webwork.' keys,
     * we need only respond with those named keys.  If we need to respond to more then we need to add the keys
     * to the whitelist or change the code appropriately.  But DO NOT respond to any key becase it involves
     * an SQL satement and then some memory in CachingPropertySet to represent that we dont know the answer
    */
    private static Set<String> WHITELIST_OF_KEYS = new HashSet<String>();

    static
    {
        WHITELIST_OF_KEYS.add("webwork.multipart.maxSize");
        WHITELIST_OF_KEYS.add("webwork.i18n.encoding");
    }


    /**
     * Get a named setting.
     */
    public Object getImpl(String aName) throws IllegalArgumentException
    {
        Object setting = null;
        if (WHITELIST_OF_KEYS.contains(aName))
        {
            setting = getApplicationProperties().getDefaultBackedString(aName);
        }
        if (setting == null)
        {
            throw new WebworkConfigurationNotFoundException(this.getClass(), "No such setting", aName);
        }
        return setting;
    }


    ApplicationProperties getApplicationProperties()
    {
        return ComponentAccessor.getComponent(ApplicationProperties.class);
    }

    /**
     * Set a named setting
     */
    public void setImpl(String aName, Object aValue) throws IllegalArgumentException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Cannot set Application Properties through this interface");
    }

    /**
     * List setting names
     */
    public Iterator listImpl()
    {
        return getApplicationProperties().getKeys().iterator();
    }
}
