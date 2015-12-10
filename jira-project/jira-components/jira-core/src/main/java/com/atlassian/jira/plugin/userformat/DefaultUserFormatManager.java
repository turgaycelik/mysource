package com.atlassian.jira.plugin.userformat;

import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.plugin.profile.UserFormatManager;

import java.util.Map;

/**
 * @since v3.13
 * @deprecated Substituted by {@link DefaultUserFormats}
 * @see DefaultUserFormats
 * @see UserFormats
 */
@Deprecated
public class DefaultUserFormatManager implements UserFormatManager
{
    private final UserFormats userFormats;

    public DefaultUserFormatManager(final UserFormats userFormats)
    {
        this.userFormats = userFormats;
    }

    public String formatUser(final String key, final String type, final String id)
    {
        final UserFormat userFormat = getUserFormat(type);
        if (userFormat != null)
        {
            return userFormat.format(key, id);
        }
        return null;
    }

    @Override
    public String formatUsername(String username, String type, String id)
    {
        final UserFormatter userFormatter = getUserFormatter(type);
        if (userFormatter != null)
        {
            return userFormatter.formatUsername(username, id);
        }
        return null;
    }

    @Override
    public String formatUserkey(String userkey, String type, String id)
    {
        final UserFormatter userFormatter = getUserFormatter(type);
        if (userFormatter != null)
        {
            return userFormatter.formatUserkey(userkey, id);
        }
        return null;
    }

    public String formatUser(final String key, final String type, final String id, final Map params)
    {
        final UserFormat userFormat = getUserFormat(type);
        if (userFormat != null)
        {
            return userFormat.format(key, id, params);
        }
        return null;
    }

    @Override
    public String formatUsername(String username, String type, String id, Map params)
    {
        final UserFormatter userFormatter = getUserFormatter(type);
        if (userFormatter != null)
        {
            return userFormatter.formatUsername(username, id, params);
        }
        return null;
    }

    @Override
    public String formatUserkey(String userkey, String type, String id, Map params)
    {
        final UserFormatter userFormatter = getUserFormatter(type);
        if (userFormatter != null)
        {
            return userFormatter.formatUserkey(userkey, id, params);
        }
        return null;
    }

    public UserFormat getUserFormat(final String type)
    {
        return userFormats.forType(type);
    }

    public UserFormatter getUserFormatter(final String type)
    {
        return userFormats.formatter(type);
    }
}
