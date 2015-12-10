package com.atlassian.configurable;

import org.dom4j.Element;

import java.util.List;

/**
 * Uses an xml element to configure a list of keys and values as the available values for this property.
 */
public class XMLValuesObjectConfigurationProperty extends ObjectConfigurationPropertyImpl
{
    /**
     * Creates a new XMLValuesObjectConfigurationProperty object.
     *
     * @param name         DOCUMENT ME!
     * @param description  DOCUMENT ME!
     * @param defaultValue DOCUMENT ME!
     * @param type         DOCUMENT ME!
     * @param element      DOCUMENT ME!
     */
    XMLValuesObjectConfigurationProperty(String name, String description, String defaultValue, int type, Element element, String enabledConditionClassName)
    {
        this(name, description, defaultValue, type, element, enabledConditionClassName, XMLValuesObjectConfigurationProperty.class.getClassLoader());
    }
    
    XMLValuesObjectConfigurationProperty(String name, String description, String defaultValue, int type, Element element, String enabledConditionClassName, ClassLoader classLoader)
    {
        super(name, description, defaultValue, type, enabledConditionClassName, classLoader);

        //This element will contain multiple value tags containing a key and a value
        if (element != null)
        {
            List elements = element.elements("value");

            for (final Object element1 : elements)
            {
                Element valueElement = (Element) element1;

                //This value element contains a key and value element
                Element keyElement = valueElement.element("key");
                Element keyValueElement = valueElement.element("value");

                getMutableInternalValues().put(keyElement.getTextTrim(), keyValueElement.getTextTrim());
            }
        }
    }
}