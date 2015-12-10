package com.atlassian.jira.jelly.tag.util;

import java.sql.Timestamp;

/**
 * Utility class for jelly tags
 *
 * @since v3.12
 */
public class JellyTagUtils
{
    /**
     * Converts a <code>String</code> object in JDBC timestamp escape format to a
     * <code>Timestamp</code> value.
     *
     * @param timestamp timestamp in format <code>yyyy-mm-dd hh:mm:ss.fffffffff</code>
     * @return corresponding <code>Timestamp</code> value
     * @exception java.lang.IllegalArgumentException if the given argument
     * does not have the format <code>yyyy-mm-dd hh:mm:ss.fffffffff</code>
     * @see java.sql.Timestamp#valueOf(String)
     */
    public static Timestamp parseDate(String timestamp)
    {
        return Timestamp.valueOf(timestamp);
    }
}
