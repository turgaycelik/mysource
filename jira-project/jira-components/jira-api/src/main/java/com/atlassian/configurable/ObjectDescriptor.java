package com.atlassian.configurable;

import java.util.Map;

/**
 * Interface that allows a ObjectConfiguration to retrieve a dynamic description based on the
 * values set for its properties
 */
public interface ObjectDescriptor
{
    String getDescription(Map properties, Map propertyValues);

    Map<String, ObjectConfigurationProperty> validateProperties(Map<String, ObjectConfigurationProperty> values);
}
