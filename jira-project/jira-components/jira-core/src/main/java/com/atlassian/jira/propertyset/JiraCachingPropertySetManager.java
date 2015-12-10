package com.atlassian.jira.propertyset;

/**
 * Manager for {@link JiraCachingPropertySet} instances.
 *
 * @since 6.1
 */
public interface JiraCachingPropertySetManager
{
    /**
     * Registers the given property set with this manager.
     *
     * @param propertySet the property set to register (ignored if null)
     */
    void register(JiraCachingPropertySet propertySet);
}
