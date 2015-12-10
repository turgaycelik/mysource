/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaProperty;

public class ActionTagSupportDynaClass extends BasicDynaClass
{
    /**
     * Construct a new ActionTagSupportDynaClass with default parameters.
     */
    public ActionTagSupportDynaClass()
    {
    }

    /**
     * Construct a new ActionTagSupportDynaClass with the specified parameters.
     *
     * @param name Name of this ActionTagSupportDynaClass class
     * @param dynaBeanClass The implementation class for new instances
     */
    public ActionTagSupportDynaClass(String name, Class dynaBeanClass)
    {
        super(name, dynaBeanClass);
    }

    /**
     * Construct a new ActionTagSupportDynaClass with the specified parameters.
     *
     * @param name Name of this ActionTagSupportDynaClass class
     * @param dynaBeanClass The implementation class for new intances
     * @param properties Property descriptors for the supported properties
     */
    public ActionTagSupportDynaClass(String name, Class dynaBeanClass, DynaProperty[] properties)
    {
        super(name, dynaBeanClass, properties);
    }

    /**
     * The purpose of the over-ridden method is to dynamically add Properties.
     * If no property matching <code>name</code> exists, a new one is dynically
     * created
     * @param name Name of the property being described
     * @return new or existing <code>DynaProperty</code> matching the name.
     */
    public DynaProperty getDynaProperty(String name)
    {
        DynaProperty dynaProperty = super.getDynaProperty(name);
        if (dynaProperty == null)
        {
            // If it wasnt found, add it property to our dyna class
            dynaProperty = new DynaProperty(name, String.class);
            propertiesMap.put(name, dynaProperty);
        }
        return dynaProperty;
    }
}
