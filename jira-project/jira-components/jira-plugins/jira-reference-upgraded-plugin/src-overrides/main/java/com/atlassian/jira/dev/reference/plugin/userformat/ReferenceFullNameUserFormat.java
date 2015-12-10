package com.atlassian.jira.dev.reference.plugin.userformat;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Reference upgraded implementation that renders the users full name between parentheses instead of brackets and
 * appends the user's email address.
 *
 * @since v4.4
 */
public class ReferenceFullNameUserFormat implements UserFormat
{
    private UserUtil userUtil;

    public ReferenceFullNameUserFormat(final UserUtil userUtil)
    {
        this.userUtil = userUtil;
    }

    public String format(final String username, final String id)
    {
        if (StringUtils.isBlank(username))
        {
            return null;
        }
        final User user = userUtil.getUserObject(username);
        final String formattedUser = user == null ? username : userUtil.getDisplayableNameSafely(user);
        return "(" + TextUtils.htmlEncode(formattedUser) + ")" + " RELOADED!! " + "Contact:" + user.getEmailAddress();
    }

    public String format(final String username, final String id, final Map<String, Object> params)
    {
        return format(username, id);
    }
}
