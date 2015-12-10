package com.atlassian.jira.plugin.userformat;

import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Very simple implementation that only renders the user's username.
 *
 * @since v5.0.3
 */
public class UserNameUserFormat implements UserFormat
{
    public static final String TYPE = "userName";

    private UserManager userManager;

    public UserNameUserFormat(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    @HtmlSafe
    public String format(final String key, final String id)
    {
        if (StringUtils.isBlank(key))
        {
            return null;
        }
        final ApplicationUser user = userManager.getUserByKeyEvenWhenUnknown(key);
        // user could only be null if key is null, but we already know that it is not blank.
        //noinspection ConstantConditions
        return TextUtils.htmlEncode(user.getUsername());
    }

    @Override
    @HtmlSafe
    public String format(final String key, final String id, final Map<String, Object> params)
    {
        return format(key, id);
    }
}
