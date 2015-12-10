package com.atlassian.jira.plugin.userformat;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.plugin.profile.UserFormat;

/**
 * Builds {@link UserFormat user formats} for a specific user format type.
 * User Formats are used to output user information from JIRA.
 *
 * @see UserFormatModuleDescriptor
 * @see UserFormat
 *
 * @since v4.4
 */
@PublicApi
public interface UserFormats
{
    /**
     * Builds a user format for the specified type.
     *
     * @param type the type of user format to build.
     * @return A user format for the specified type or null if no user format could be found for the specfied type.
     *
     * @deprecated Use {@link #formatter(String)} instead. Since v6.0.
     */
    UserFormat forType(String type);

    /**
     * Builds a user formatter for the specified type.
     *
     * @param type the type of user formatter to build.
     * @return A user formatter for the specified type or null if no user formatter could be found for the specfied type.
     *
     * @since 6.0
     */
    UserFormatter formatter(String type);
}
