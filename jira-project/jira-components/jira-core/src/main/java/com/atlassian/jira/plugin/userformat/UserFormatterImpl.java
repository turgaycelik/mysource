package com.atlassian.jira.plugin.userformat;

import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v6.0
 */
public class UserFormatterImpl implements UserFormatter
{
    private final UserFormat userFormat;
    private final UserKeyService userKeyService;

    public UserFormatterImpl(UserFormat userFormat, UserKeyService userKeyService)
    {
        this.userFormat = notNull("userFormat", userFormat);
        this.userKeyService = notNull("userKeyService", userKeyService);
    }

    @Override
    @HtmlSafe
    public String formatUserkey(String userkey, String id)
    {
        return userFormat.format(userkey, id);
    }

    @Override
    @HtmlSafe
    public String formatUserkey(String userkey, String id, Map<String, Object> params)
    {
        return userFormat.format(userkey, id, params);
    }

    @Override
    @HtmlSafe
    public String formatUsername(String username, String id)
    {
        return userFormat.format(userKeyService.getKeyForUsername(username), id);
    }

    @Override
    @HtmlSafe
    public String formatUsername(String username, String id, Map<String, Object> params)
    {
        return userFormat.format(userKeyService.getKeyForUsername(username), id, params);
    }
}
