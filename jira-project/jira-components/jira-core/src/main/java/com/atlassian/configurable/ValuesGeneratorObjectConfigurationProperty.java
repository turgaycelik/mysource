package com.atlassian.configurable;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.util.JiraUtils;

import com.google.common.annotations.VisibleForTesting;

import static com.atlassian.jira.util.JiraUtils.loadComponent;

/**
 * Represents a property which gets its values derived from a {@link com.atlassian.configurable.ValuesGenerator}
 * implementation.
 */
public class ValuesGeneratorObjectConfigurationProperty extends ObjectConfigurationPropertyImpl
{
    private final static Logger log = Logger.getLogger(ValuesGeneratorObjectConfigurationProperty.class);

    private final ValuesGenerator valuesGenerator;

    /**
     * Creates a new ValuesGeneratorObjectConfigurationProperty object.
     *
     * @param name Property name
     * @param description Property description
     * @param defaultValue Default value
     * @param type Field type, e.g. string, long ....
     * @param valueGeneratorClass Instance of ValuesGenerator used to retrieve a list of available choices
     */
    public ValuesGeneratorObjectConfigurationProperty(String name, String description, String defaultValue, int type, String valueGeneratorClass, String enabledConditionClass)
    {
        this(name, description, defaultValue, type, valueGeneratorClass, enabledConditionClass, ValuesGeneratorObjectConfigurationProperty.class.getClassLoader());
    }

    /**
     * Creates a new ValuesGeneratorObjectConfigurationProperty object.
     *
     * @param name Property name
     * @param description Property description
     * @param defaultValue Default value
     * @param type Field type, e.g. string, long ....
     * @param valueGeneratorClass Instance of ValuesGenerator used to retrieve a list of available choices
     * @param classLoader ClassLoader used to load the valueGeneratorClass
     */
    public ValuesGeneratorObjectConfigurationProperty(String name, String description, String defaultValue, int type, String valueGeneratorClass, String enabledConditionClass, ClassLoader classLoader)
    {
        super(name, description, defaultValue, type, enabledConditionClass, classLoader);

        //Load an instance of the class
        ValuesGenerator valuesGenerator = ValuesGenerator.NONE;
        try
        {
            @SuppressWarnings("unchecked")
            Class<ValuesGenerator> valuesGeneratorClass = (Class<ValuesGenerator>) classLoader.loadClass(valueGeneratorClass);
            valuesGenerator = createValuesGenerator(valuesGeneratorClass);
        }
        catch (Exception e)
        {
            log.warn("Could not create class: " + valueGeneratorClass, e);
        }
        this.valuesGenerator = valuesGenerator;
    }

    @VisibleForTesting
    ValuesGenerator createValuesGenerator(Class<ValuesGenerator> valuesGeneratorClass)
    {
        return loadComponent(valuesGeneratorClass);
    }

    /**
     * Overides method in super class that uses a map stored in the object to retrieve choices. This function uses a
     * {@link ValuesGenerator} class to retrieve the values.
     *
     * @param userParams used to retrieve a cut down list of choices from the {@link ValuesGenerator} class
     * @return Map of choices dependant on this users parameters. If null is returned from the ValuesGenerator an empty
     *         list is returned, which is consistent with the behaviour of {@link ObjectConfigurationPropertyImpl}
     */
    protected Map getInternalValues(Map userParams)
    {
        final Map values = valuesGenerator.getValues(userParams);
        if (values == null)
        {
            return Collections.EMPTY_MAP;
        }
        else
        {
            return values;
        }
    }
}
