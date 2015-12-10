/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.user.UserUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility code originally written to support various importers.  Contains methods for bypassing security schemes,
 * reindexing issues, creating new issue types, resolutions, custom fields, and various other useful bits of code.
 *
 * It's used by JIM (JIRA Importers Plugin)
 */
public class ImportUtils
{
    private static final ThreadLocal<Boolean> subvert = new ThreadLocal<Boolean>()
    {
        @Override
        protected Boolean initialValue()
        {
            return Boolean.FALSE;
        }
    };

    private static final ThreadLocal<Boolean> indexIssues = new ThreadLocal<Boolean>()
    {
        @Override
        protected Boolean initialValue()
        {
            return Boolean.TRUE;
        }
    };

    private static final ThreadLocal<Boolean> enableNotifications = new ThreadLocal<Boolean>()
    {
        @Override
        protected Boolean initialValue()
        {
            return Boolean.TRUE;
        }
    };

    /**
     * Whether to subvert the security scheme or not.  This is done on a thread local basis, so the subversion
     * only occurs for this thread / request.
     * <p/>
     * Other classes may alter their behaviour based on the value you pass.
     *
     * @param subvert Whether to subvert the scheme or not.
     * @see com.atlassian.jira.ManagerFactory#getPermissionManager()
     */
    public static void setSubvertSecurityScheme(final boolean subvert)
    {
        ImportUtils.subvert.set(Boolean.valueOf(subvert));
    }

    public static boolean isSubvertSecurityScheme()
    {
        final Boolean isSubverted = ImportUtils.subvert.get();

        //isSubverted will never be null, as it has an initial value of false
        return isSubverted.booleanValue();
    }

    public static void setIndexIssues(final boolean index)
    {
        ImportUtils.indexIssues.set(Boolean.valueOf(index));
    }

    public static void setEnableNotifications(final boolean enable)
    {
        ImportUtils.enableNotifications.set(Boolean.valueOf(enable));
    }

    public static boolean isEnableNotifications()
    {
        final Boolean isEnableNotifications = ImportUtils.enableNotifications.get();

        // isEnableNotifications will never be null, as it has an initial value of true
        return isEnableNotifications.booleanValue();
    }

    public static boolean isIndexIssues()
    {
        final Boolean isIndexing = ImportUtils.indexIssues.get();

        //isIndexing will never be null, as it has an initial value of false
        return isIndexing.booleanValue();
    }

    /**
     * Remove any HTML from text contents.
     *
     * @param str Text with HTML (eg &lt;a href="...">..&lt;/a> links).
     * @return str without HTML
     */
    public static String stripHTMLStrings(final String str)
    {
        String newString = RegexpUtils.replaceAll(str, "<a (?:target=\"_new\" )?href=['\"](?:mailto:)?(.*?)['\"](?: target=\"_new\")?>.*</a>", "$1");

        newString = RegexpUtils.replaceAll(newString, "&quot;", "\"");
        newString = RegexpUtils.replaceAll(newString, "&lt;", "<");
        newString = RegexpUtils.replaceAll(newString, "&gt;", ">");
        newString = RegexpUtils.replaceAll(newString, "&amp;", "&");
        newString = RegexpUtils.replaceAll(newString, "&nbsp;", " ");
        return newString;
    }

    /**
     * This method will determine all the users that will need to exist in JIRA to successfully import the
     * specified projects and will return the users that do not yet exist.
     *
     * @param users a Set <ExternalUser> of users that we need to check whether they exist in JIRA
     * @return Set <ExternalUser> all the users that will need to exist in JIRA but do not yet.
     */
    static Set<ExternalUser> getNonExistentUsers(final Set<ExternalUser> users)
    {
        final Set<ExternalUser> unknownUsers = new HashSet<ExternalUser>();

        for (final ExternalUser externalUser : users)
        {
            User user = UserUtils.getUser(externalUser.getName());
            if (user == null)
            {
                user = UserUtils.getUserByEmail(externalUser.getEmail());
                if (user == null)
                {
                    unknownUsers.add(externalUser);
                }
            }
        }
        return unknownUsers;
    }

    /**
     * Generate SQL-friendly comma-separated list of ?'s.
     */
    static String getSQLTokens(final String[] names)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++)
        {
            sb.append(" ? ");
            if (i != names.length - 1)
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    static void close(final PreparedStatement ps, final ResultSet rs)
    {
        closePS(ps);
        closeRS(rs);
    }

    static void closePS(final PreparedStatement ps)
    {
        try
        {
            if (ps != null)
            {
                ps.close();
            }
        }
        catch (final SQLException ignore)
        {
            // ignored
        }
    }

    static void closeRS(final ResultSet rs)
    {
        try
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        catch (final SQLException ignore)
        {
            // ignored
        }
    }

    private ImportUtils()
    {}
}