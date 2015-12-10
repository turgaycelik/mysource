package com.atlassian.jira.dev.reference.plugin.userformat;

import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Reference implementation that renders the users full name between brackets.
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

    @Override
    @HtmlSafe
    public String format(final String key, final String id)
    {
        if (StringUtils.isBlank(key))
        {
            return null;
        }
        final ApplicationUser user = userUtil.getUserByKey(key);
        final String formattedUser = user == null ? key : userUtil.getDisplayableNameSafely(user);
        return "[" + TextUtils.htmlEncode(formattedUser) + "]";
    }

    @Override
    @HtmlSafe
    public String format(final String key, final String id, final Map<String, Object> params)
    {
        return format(key, id);
    }
}
