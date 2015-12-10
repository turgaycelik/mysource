package com.atlassian.jira.plugin.userformat.descriptors;

import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;

/**
 * Represents the enabled user format module descriptors in the plugins system.
 *
 * @since v4.4
 */
public interface UserFormatModuleDescriptors
{
    /**
     * Gets a snapshot of all the configured {@link UserFormatModuleDescriptor user format module descriptors} from the
     * plugins system.
     *
     * @return An Iterable of all user format module descriptors.
     */
    Iterable<UserFormatModuleDescriptor> get();

    /**
     * Gets a snapshot of the configured {@link UserFormatModuleDescriptor user format module descriptors} for a
     * specific type from the plugins system.
     *
     * @param type The type of the user format module descriptors to find.
     * @return An Iterable of user format module descriptors for the specified type.
     */
    Iterable<UserFormatModuleDescriptor> forType(String type);

    /**
     * Gets the default {@link UserFormatModuleDescriptor user format module descriptors} for the specified type.
     *
     * @param type The type to find a default user format descriptor for.
     * @return A default user format module descriptor for the specified type or <code>null</code>
     * if no default was found.
     */
    UserFormatModuleDescriptor defaultFor(final String type);

    /**
     * Gets the {@link UserFormatModuleDescriptor user format module descriptor} for the specified key.
     *
     * @param completeKey The complete key for the module descriptor to find.
     * @return A {@link UserFormatModuleDescriptor user format module descriptor} for the specified key or null
     * if no module descriptor could be found for that key.
     */
    UserFormatModuleDescriptor withKey(String completeKey);
}
