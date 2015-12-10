package com.atlassian.jira.bc.admin;

/**
 * Represents an administrative admin setting's current value and maintains an association to its declared {@link
 * ApplicationPropertyMetadata} which stores its default value, key, type etc.
 *
 * @since v4.4
 */
public final class ApplicationProperty
{
    private final ApplicationPropertyMetadata metadata;
    private final String currentValue;

    public ApplicationProperty(ApplicationPropertyMetadata metadata, String currentValue)
    {
        this.metadata = metadata;
        this.currentValue = currentValue;
    }

    public ApplicationPropertyMetadata getMetadata()
    {
        return metadata;
    }

    public String getCurrentValue()
    {
        return currentValue;
    }

}
