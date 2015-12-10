package com.atlassian.jira.plugin.userformat.descriptors;

/**
 * Represents the user format types that have been defined in the user format module descriptors that are enabled in the
 * plugins system.
 *
 * @since v4.4
 */
public interface UserFormatTypes
{
    /**
     * Gets all the types which have an enabled user format module descriptor from the plugins system.
     *
     * @return An Iterable of all the types which have an enabled user format module descriptor.
     */
    Iterable<String> get();
}
