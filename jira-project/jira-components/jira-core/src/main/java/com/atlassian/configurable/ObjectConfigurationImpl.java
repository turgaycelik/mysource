package com.atlassian.configurable;

import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ObjectConfigurationImpl implements ObjectConfiguration
{
    private final ObjectDescriptor od;
    private final Map<String, ObjectConfigurationProperty> configProperties = Collections.synchronizedMap(new ListOrderedMap());
    private final Map params = Collections.synchronizedMap(new ListOrderedMap());

    /**
     * Creates a new ObjectConfigurationImpl object.
     *
     * @param configProperties A Map of properties that can be configured for this object e.g. name, description.
     * @param od               A Class the retrieves the Description of this object based the currently configured properties.
     */
    public ObjectConfigurationImpl(Map<String, ObjectConfigurationProperty> configProperties, ObjectDescriptor od)
    {
        this.configProperties.putAll(configProperties);
        this.od = od;
    }

    /**
     * Initialises the object with some parameters
     *
     * @param params Map of initialisation params
     */
    public void init(Map params)
    {
        this.params.clear();
        if (params != null)
        {
            this.params.putAll(params);
        }
    }

    /**
     * Retrieves the name of a property with the specified key
     *
     * @param key Key of the property
     * @return Name of the specified property
     * @throws ObjectConfigurationException
     */
    public String getFieldName(String key) throws ObjectConfigurationException
    {
        if (configProperties.containsKey(key))
        {
            return configProperties.get(key).getName();
        }
        else
        {
            throw new ObjectConfigurationException("Field with key: " + key + " does not exist.");
        }
    }

    /**
     * Retrieves the description of a property with the specified key
     *
     * @param key Key of the property
     * @return Description of the specified property
     * @throws ObjectConfigurationException
     */
    public String getFieldDescription(String key) throws ObjectConfigurationException
    {
        if (configProperties.containsKey(key))
        {
            return configProperties.get(key).getDescription();
        }
        else
        {
            throw new ObjectConfigurationException("Field with key: " + key + " does not exist.");
        }
    }

    /**
     * Retrieves the default value for property with specified key
     *
     * @param key Key of the property
     * @return Default value of the specified property
     * @throws ObjectConfigurationException
     */
    public String getFieldDefault(String key) throws ObjectConfigurationException
    {
        if (configProperties.containsKey(key))
        {
            return configProperties.get(key).getDefault();
        }
        else
        {
            throw new ObjectConfigurationException("Field with key: " + key + " does not exist.");
        }
    }

    /**
     * Retrieves the type of the property with the specified key
     *
     * @param key Key of the property
     * @return Type of the specified property
     * @throws ObjectConfigurationException
     */
    public int getFieldType(String key) throws ObjectConfigurationException
    {
        if (configProperties.containsKey(key))
        {
            return new Integer(configProperties.get(key).getType()).intValue();
        }
        else
        {
            throw new ObjectConfigurationException("Field with key: " + key + " does not exist.");
        }
    }

    /**
     * Retrieves a map of available values for property with the specified key. e.g select list values
     *
     * @param key Key of the property
     * @return List valid name/value pairs for the specified property
     * @throws ObjectConfigurationException
     */
    public Map getFieldValues(String key) throws ObjectConfigurationException
    {
        if (configProperties.containsKey(key))
        {
            final ObjectConfigurationProperty objectConfigurationProperty = configProperties.get(key);
            objectConfigurationProperty.init(params);
            return objectConfigurationProperty;
        }
        else
        {
            throw new ObjectConfigurationException("Field with key: " + key + " does not exist.");
        }
    }

    /**
     * Retrieves a map of available values for property with the specified key. e.g select list values.
     * However, keys and values are html encoded in the returned map.
     *
     * @param key
     * @return List valid name/value pairs for the specified property - html encoded
     * @throws ObjectConfigurationException
     */
    public Map getFieldValuesHtmlEncoded(String key) throws ObjectConfigurationException
    {
        Map<?,?> fieldValues = getFieldValues(key);
        Map htmlEncodedFieldValues = new ListOrderedMap();

        for (Map.Entry<?,?> entry : fieldValues.entrySet())
        {
            String htmlEncodedKey = TextUtils.htmlEncode(entry.getKey().toString());
            String htmlEncodedValue =  TextUtils.htmlEncode(entry.getValue().toString());
            htmlEncodedFieldValues.put(htmlEncodedKey, htmlEncodedValue);
        }
        return htmlEncodedFieldValues;
    }

    /**
     * All the property keys for this configuration
     *
     * @return Property keys
     */
    public String[] getFieldKeys()
    {
        String[] returnValue = new String[configProperties.size()];
        int counter = 0;

        for (String key : configProperties.keySet())
        {
            returnValue[counter] = key;
            counter++;
        }

        return returnValue;
    }

    public String[] getEnabledFieldKeys()
    {
        List<String> returnValue = new ArrayList<String>();
        for (final Entry<String, ObjectConfigurationProperty> entry : configProperties.entrySet())
        {

            if ((entry.getValue()).isEnabled())
            {
                returnValue.add(entry.getKey());
            }
        }

        return returnValue.toArray(new String[returnValue.size()]);
    }


    public boolean isEnabled(String key)
    {
        ObjectConfigurationProperty property = configProperties.get(key);
        return property != null && property.isEnabled();
    }

    /**
     * The Description of this instance of an Object Configuration
     *
     * @param params Params used to derive
     * @return Description
     */
    public String getDescription(Map params)
    {
        return od.getDescription(configProperties, params);
    }

    public boolean allFieldsHidden()
    {
        if (configProperties != null)
        {
            for (ObjectConfigurationProperty ocp : configProperties.values())
            {
                if (ocp.getType() != ObjectConfigurationTypes.HIDDEN)
                {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isI18NValues(String key)
    {
        return configProperties.containsKey(key) && configProperties.get(key).isI18nValues();
    }
}
