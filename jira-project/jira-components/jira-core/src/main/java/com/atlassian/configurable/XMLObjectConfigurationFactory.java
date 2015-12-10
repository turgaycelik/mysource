package com.atlassian.configurable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * This class retrieves an Object Configuration with a particular id.
 *
 * <p>The Object Configuration must be unique as a user will set their own parameters on the object during its lifetime.
 * </p>
 */
public class XMLObjectConfigurationFactory implements ObjectConfigurationFactory
{
    private final Map<String, ObjectConfigurationHolder> configs = new HashMap<String, ObjectConfigurationHolder>();

    /**
     * Creates a new XMLObjectConfigurationFactory object.
     */
    public XMLObjectConfigurationFactory()
    {
    }

    /**
     * Does this factory know about an ObjectConfiguration with the specified id
     *
     * @param id Identifier for the object configuration to retrieve.
     * @return object configuration is available.
     */
    public boolean hasObjectConfiguration(String id)
    {
        return configs.containsKey(id);
    }

    /**
     * This function retrieves an ObjectConfiguration based on a specified identifier,
     * N.B. must be mutable
     *
     * @param id Identifier for the object configuration to retrieve.
     * @return a ObjectConfiguration
     * @throws ObjectConfigurationException
     */
    public ObjectConfiguration getObjectConfiguration(String id, Map params) throws ObjectConfigurationException
    {
        if (configs.containsKey(id))
        {
            return configs.get(id).getObjectConfiguration(params);
        }
        else
        {
            throw new ObjectConfigurationException("Could not find Object Configuration with id: " + id);
        }
    }

    /**
     * This procedure loads an xml file into a holder object which is then used to construct a real
     * object when the get function is called.  Uses the same class loader that loaded this instance of
     * XMLObjectConfigurationFactory to load the xml file and descriptor classes.
     *
     * @param xmlFile the name of the resource on the classpath from which to load the config.
     * @param id      Identifier for the object configuration to retrieve.
     * @throws ObjectConfigurationException if something is really wrong
     */
    public void loadObjectConfiguration(String xmlFile, String id) throws ObjectConfigurationException
    {
        loadObjectConfiguration(xmlFile, id, getClass().getClassLoader());
    }
    
    /**
     * This procedure loads an xml file into a holder object which is then used to construct a real object when the get
     * function is called.  Uses the provided class loader to load the xml file and descriptor classes.
     *
     * @param xmlFile the name of the resource on the classpath from which to load the config.
     * @param id      Identifier for the object configuration to retrieve.
     * @param classLoader ClassLoader to use to load ObjectDescriptor classes and the xml file 
     * @throws ObjectConfigurationException if something is really wrong
     */
    public void loadObjectConfiguration(String xmlFile, String id, ClassLoader classLoader) throws ObjectConfigurationException
    {
        if (xmlFile != null)
        {
            InputStream is = classLoader.getResourceAsStream(xmlFile);
            if (is == null)
            {
                throw new ObjectConfigurationException("Unable to load the configuration file '" + xmlFile + "' please ensure the file exists.");
            }

            try
            {
                SAXReader reader = new SAXReader();
                Document doc = reader.read(is);
                Element root = doc.getRootElement();

                Element objectDescriptor = root.element("description");
                final ObjectDescriptor od;

                if (objectDescriptor.attribute("class") != null)
                {
                    od = (ObjectDescriptor) classLoader.loadClass(objectDescriptor.attributeValue("class")).newInstance();
                }
                else
                {
                    od = new StringObjectDescription(objectDescriptor.getTextTrim());
                }

                loadObjectConfigurationFromElement(root, od, id);
            }
            catch (DocumentException e)
            {
                throw new ObjectConfigurationException("An Error occurred trying to parse the configuration file '" + xmlFile + "' please ensure the file exists.", e);
            }
            catch (ClassNotFoundException e)
            {
                throw new ObjectConfigurationException("An Error occurred loading class:", e);
            }
            catch (InstantiationException e)
            {
                throw new ObjectConfigurationException("An Error occurred loading class:", e);
            }
            catch (IllegalAccessException e)
            {
                throw new ObjectConfigurationException("An Error occurred loading class:", e);
            }
        }
    }

    public void loadObjectConfigurationFromElement(Element element, ObjectDescriptor od, String id)
    {
        loadObjectConfigurationFromElement(element, od, id, getClass().getClassLoader());
    }
    
    public void loadObjectConfigurationFromElement(Element element, ObjectDescriptor od, String id, ClassLoader classLoader)
    {
        Element propss = element.element("properties");
        Map<String, ObjectConfigurationProperty> values = new ListOrderedMap();

        if (propss != null)
        {
            List props = propss.elements("property");

            for (final Object prop1 : props)
            {
                Element prop = (Element) prop1;
                String defaultValue = "";
                String description = "";

                {
                    final Element descriptionElement = prop.element("description");
                    if (descriptionElement != null)
                    {
                        description = descriptionElement.getTextTrim();
                    }
                }

                {
                    final Element defaultValueElement = prop.element("default");
                    if (defaultValueElement != null)
                    {
                        defaultValue = defaultValueElement.getTextTrim();
                    }
                }

                final String enabledConditionClassName;
                {
                    final Element enabledConditionElement = prop.element("enabled-condition");
                    if (enabledConditionElement != null)
                    {
                        enabledConditionClassName = enabledConditionElement.attributeValue("class");
                    }
                    else
                    {
                        enabledConditionClassName = null;
                    }
                }
                {
                    //Check to see if the values object is a class or xml
                    final Element valuesElement = prop.element("values");
                    final String propertyName = prop.element("name").getTextTrim();
                    final int propertyType = getTypeInt(prop.element("type").getTextTrim());
                    if (valuesElement != null)
                    {
                        final Attribute valueGeneratorClassAttribute = valuesElement.attribute("class");
                        if (valueGeneratorClassAttribute != null)
                        {
                            final String generatorClass = valuesElement.attributeValue("class");
                            ObjectConfigurationProperty ocp = new ValuesGeneratorObjectConfigurationProperty(propertyName, description, defaultValue, propertyType, generatorClass, enabledConditionClassName, classLoader);
                            if (prop.element("i18n") != null)
                            {
                                ocp.setI18nValues(!"false".equals(prop.element("i18n").getTextTrim()));
                            }
                            if (prop.element("cascade-from") != null)
                            {
                                ocp.setCascadeFrom(prop.element("cascade-from").getTextTrim());
                            }
                            values.put(prop.element("key").getTextTrim(), ocp);
                        }
                        else
                        {
                            ObjectConfigurationProperty ocp = new XMLValuesObjectConfigurationProperty(propertyName, description, defaultValue, propertyType, valuesElement, enabledConditionClassName, classLoader);
                            if (prop.element("i18n") != null)
                            {
                                ocp.setI18nValues(!"false".equals(prop.element("i18n").getTextTrim()));
                            }
                            if (prop.element("cascade-from") != null)
                            {
                                ocp.setCascadeFrom(prop.element("cascade-from").getTextTrim());
                            }
                            values.put(prop.element("key").getTextTrim(), ocp);
                        }
                    }
                    else
                    {
                        values.put(prop.element("key").getTextTrim(), new ObjectConfigurationPropertyImpl(propertyName, description, defaultValue, propertyType, enabledConditionClassName, classLoader));
                    }
                }
            }
        }

        ObjectConfigurationHolder conf;
        // Only validate the properties if an ObjectDescriptor exists
        if (od != null)
        {
            // Validate the properties - e.g. project categories are enterprise only
            Map<String, ObjectConfigurationProperty> validatedValues = od.validateProperties(values);
            conf = new ObjectConfigurationHolder(id, validatedValues, od);
        }
        else
        {
            conf = new ObjectConfigurationHolder(id, values, od);
        }

        configs.put(id, conf);
    }

    /**
     * Returns a ObjectConfigurationProperty from a string representation of a number or a string representation of the
     * type.
     *
     * @param typeName the string type "select", etc.
     * @return the integer that represents the type
     * @see com.atlassian.configurable.ObjectConfigurationTypes
     */
    private int getTypeInt(String typeName)
    {
        try
        {
            return Integer.parseInt(typeName);
        }
        catch (NumberFormatException e)
        {
            return ObjectConfigurationTypes.getType(typeName);
        }
    }

    private class ObjectConfigurationHolder
    {
        String id;
        Map<String, ObjectConfigurationProperty> configProperties;
        ObjectDescriptor od;

        ObjectConfigurationHolder(String id, Map<String, ObjectConfigurationProperty> configProperties, ObjectDescriptor od)
        {
            this.id = id;
            this.configProperties = configProperties;
            this.od = od;
        }

        ObjectConfiguration getObjectConfiguration(Map userParams)
        {
            ObjectConfiguration oc = new ObjectConfigurationImpl(configProperties, od);
            oc.init(userParams);
            return oc;
        }
    }
}
