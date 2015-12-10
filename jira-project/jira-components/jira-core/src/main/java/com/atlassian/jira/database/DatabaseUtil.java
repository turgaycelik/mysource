package com.atlassian.jira.database;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil
{
    /**
     * Closes the given connection if any.
     * If null is passed, it does nothing and if SQLExceptions happen on close, they are swallowed.
     *
     * @param con a Connection to be closed, potentially null
     */
    public static void closeQuietly(@Nullable Connection con)
    {
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (SQLException ignored)
            {
                // ignored
            }
        }
    }

    /**
     * Closes the given Statement if any.
     * If null is passed, it does nothing and if SQLExceptions happen on close, they are swallowed.
     *
     * @param stmt a Statement to be closed, potentially null
     */
    public static void closeQuietly(@Nullable Statement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException ignored)
            {
                // ignored
            }
        }
    }

    /**
     * Closes the given ResultSet if any.
     * If null is passed, it does nothing and if SQLExceptions happen on close, they are swallowed.
     *
     * @param rs a ResultSet to be closed, potentially null
     */
    public static void closeQuietly(@Nullable ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (SQLException ignored)
            {
                // ignored
            }
        }
    }
}
