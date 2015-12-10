package com.atlassian.jira.configurableobjects;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationTypes;
import com.atlassian.jira.service.JiraServiceContainer;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigurableObjectUtil
{
    public static Map<String, String> getPropertyMap(final JiraServiceContainer serviceContainer) throws Exception
    {
        final Map<String, String> propertyMap = new LinkedHashMap<String, String>();
        if (serviceContainer.isUsable())
        {
            final PropertySet ps = serviceContainer.getProperties();

            // create a dummy service
            final ObjectConfiguration objectConfiguration = serviceContainer.getObjectConfiguration();

            // Test that we have an object configuration for the service
            if (objectConfiguration != null)
            {
                final String[] fieldKeys = objectConfiguration.getFieldKeys();

                // set all the possible accepted parameters - blank or missing params are set to null
                for (final String key : fieldKeys)
                {
                    String value = null;

                    // if the field type is not a select field, then go to the property set to get the value
                    final int fieldType = objectConfiguration.getFieldType(key);
                    if (fieldType == ObjectConfigurationTypes.SELECT)
                    {
                        // if the value is a select, the propertySet stores a key value pair where the:
                        // key = name of the field in the edit service form
                        // value = the id of the option in the select box
                        // to retrieve the value stored in the select box, we use the id (a value in PropertySet that is used AS A "key" in
                        // ObjectConfiguration to get the actual value.
                        final String psValue = ps.getString(key);
                        value = (String) objectConfiguration.getFieldValues(key).get(psValue);
                    }
                    else
                    {
                        value = ps.getString(key);
                    }

                    if (value != null)
                    {
                        propertyMap.put(key, value);
                    }
                }
            }
        }
        return propertyMap;
    }
}
