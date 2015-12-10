package com.atlassian.jira.admin;

import com.atlassian.jira.config.properties.ApplicationProperties;

/**
 * Persister that uses the JIRA application properties as a storage back end.
 *
 * @since v5.0.7
 */
public class ApplicationPropertiesPersister implements PropertyPersister
{
    private final ApplicationProperties applicationProperties;
    private final String propertyName;

    public ApplicationPropertiesPersister(ApplicationProperties applicationProperties, String propertyName)
    {
        this.applicationProperties = applicationProperties;
        this.propertyName = propertyName;
    }

    @Override
    public String load()
    {
        return applicationProperties.getText(propertyName);
    }

    @Override
    public void save(String value)
    {
        applicationProperties.setText(propertyName, value);
    }
}
