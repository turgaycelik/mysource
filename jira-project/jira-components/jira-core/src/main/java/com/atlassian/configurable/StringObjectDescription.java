package com.atlassian.configurable;

import java.util.Map;

/**
 * Simple implementation of {@link ObjectDescriptor} that simply returns the description that was passed
 * in when the object was created.
 */
public class StringObjectDescription implements ObjectDescriptor
{
    private final String description;

    /**
     * Creates a new StringObjectDescription object.
     *
     * @param description Description to use when called by the owning ObjectConfiguration
     */
    public StringObjectDescription(String description)
    {
        this.description = description;
    }

    /**
     * Returns the description used when creating the object.
     * The properties and values are not used in this class but are there to allow more complicated descriptions
     *
     * @return Description when object was created.
     */
    public String getDescription(Map properties, Map values)
    {
        return description;
    }

    // Validation of properties
    public Map validateProperties(Map values)
    {
        // Default - no validation required - return original values
        return values;
    }
}
