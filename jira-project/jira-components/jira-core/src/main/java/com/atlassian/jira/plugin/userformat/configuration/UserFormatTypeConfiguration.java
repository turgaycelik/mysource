package com.atlassian.jira.plugin.userformat.configuration;

/**
 * <p>Stores configuration entries for the available user format types.</p>
 *
 * <p>A user format type is configured against the module key of a user format module descriptor that references the user
 * format which will be used to render a user for that user format type.</p>
 *
 * @see com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor
 * @see com.atlassian.jira.plugin.profile.UserFormat *
 * @since v4.4
 */
public interface UserFormatTypeConfiguration
{
    /**
     * Stores a configuration entry for the specified type agains a user format module descriptor key.
     *
     * @param userFormatType the user format type to configure.
     * @param moduleKey The complete module key of the .
     */
    void setUserFormatKeyForType(String userFormatType, String moduleKey);

    /**
     * Retrieves the module key of the user format descriptor configured to render the specified type.
     *
     * @param userFormatType The type to retrieve a module key for.
     * @return the module key of the user format descriptor configured to render the specified type,
     * or null if there is no module key configured for that type.
     */
    String getUserFormatKeyForType(String userFormatType);

    /**
     * Whether there is a configuration entry stored for the specified type.
     *
     * @param userFormatType The type find a confiuration entry for.
     * @return true if there is configuration entry stored for the specified type; otherwise false.
     */
    boolean containsType(String userFormatType);

    /**
     * Removes the configuration entry for the specified type if one exists.
     *
     * @param userFormatType The type to remove from the configuration.
     */
    void remove(String userFormatType);

}
