package com.atlassian.jira.plugin.profile;

import com.atlassian.jira.plugin.userformat.UserFormatter;

import java.util.Map;

/**
 * Manages the {@link UserFormat} to type mapping.  UserFormat modules may be provided for various different types.
 *
 * @deprecated please use {@link com.atlassian.jira.plugin.userformat.UserFormats} instead.
 * @see com.atlassian.jira.plugin.userformat.UserFormats
 */
@Deprecated
public interface UserFormatManager
{
    /**
     * Returns the {@link UserFormat} for a specific type, or null if none exists
     *
     * @param type the specific type for which to find a UserFormat.
     * @return the {@link UserFormat} for a specific type, or null if none exists
     */
    UserFormat getUserFormat(String type);

    /**
     * Returns the {@link UserFormat} for a specific type, or null if none exists
     *
     * @param type the specific type for which to find a UserFormat.
     * @return the {@link UserFormat} for a specific type, or null if none exists
     *
     * @since 6.0
     */
    UserFormatter getUserFormatter(String type);

    /**
     * Convenience method to format a given user.  Method looks up the mapped user format for the type and
     * passes the username and id to this user format. Returns null if no mapped user format can be found.
     *
     * @param username The user to format
     * @param type     The user format type to use
     * @param id       A context sensitive id
     * @return formatted user or null.
     *
     * @deprecated Use {@link #formatUserkey(String, String, String)} or {@link #formatUsername(String, String, String)} instead. Since v6.0.
     */
    String formatUser(String username, String type, String id);

    /**
     * Convenience method to format a given user.  Method looks up the mapped user format for the type and
     * passes the username and id to this user format. Returns null if no mapped user format can be found.
     *
     * @param username      The key of the user to format
     * @param type     The user format type to use
     * @param id       A context sensitive id
     * @return formatted user or null.
     *
     * @since 6.0
     */
    String formatUsername(String username, String type, String id);

    /**
     * Convenience method to format a given user.  Method looks up the mapped user format for the type and
     * passes the user key and id to this user format. Returns null if no mapped user format can be found.
     *
     * @param userkey      The key of the user to format
     * @param type     The user format type to use
     * @param id       A context sensitive id
     * @return formatted user or null.
     *
     * @since 6.0
     */
    String formatUserkey(String userkey, String type, String id);

    /**
     * Convenience method to format a given user.  The params user allows a user to pass any information needed
     * for additional context to the underlying {@link UserFormat}.
     *
     * @param username The user to format
     * @param type     The user format type to use
     * @param id       A context sensitive id
     * @param params   Additional context to provide to the userformatter.
     * @return formatted user or null.
     *
     * @deprecated Use {@link #formatUserkey(String, String, String, java.util.Map)} or {@link #formatUsername(String, String, String, java.util.Map)} instead. Since v6.0.
     */
    String formatUser(String username, String type, String id, Map params);

    /**
     * Convenience method to format a given user.  The params user allows a user to pass any information needed
     * for additional context to the underlying {@link UserFormat}.
     *
     * @param username      The key of the user to format
     * @param type     The user format type to use
     * @param id       A context sensitive id
     * @param params   Additional context to provide to the userformatter.
     * @return formatted user or null.
     *
     * @since 6.0
     */
    String formatUsername(String username, String type, String id, Map params);

    /**
     * Convenience method to format a given user.  The params user allows a user to pass any information needed
     * for additional context to the underlying {@link UserFormat}.
     *
     * @param userkey      The key of the user to format
     * @param type     The user format type to use
     * @param id       A context sensitive id
     * @param params   Additional context to provide to the userformatter.
     * @return formatted user or null.
     *
     * @since 6.0
     */
    String formatUserkey(String userkey, String type, String id, Map params);
}
