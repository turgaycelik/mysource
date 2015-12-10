package com.atlassian.configurable;

import org.dom4j.Element;

import java.util.Map;


/**
 * This class retrieves an Object Configuration with a particular id.
 * The Object Configuration must be unique as a user will set there own parameters on the object
 * during its lifetime.
 */
public interface ObjectConfigurationFactory
{
    /**
     * Does this factory know about an ObjectConfiguration with the specified id
     *
     * @param id Identifier for the object configuration to retrieve.
     * @return object configuration is available.
     */
    boolean hasObjectConfiguration(String id);

    /**
     * This function retrieves an ObjectConfiguration based on a specified identifier,
     * N.B. must be muttable
     *
     * @param id Identifier for the object configuration to retrieve.
     * @return a ObjectConfiguration
     * @throws ObjectConfigurationException
     */
    ObjectConfiguration getObjectConfiguration(String id, Map userParams) throws ObjectConfigurationException;

    void loadObjectConfiguration(String xmlFile, String id, ClassLoader classLoader) throws ObjectConfigurationException;

    void loadObjectConfigurationFromElement(Element element, ObjectDescriptor od, String id, ClassLoader classLoader);
}
