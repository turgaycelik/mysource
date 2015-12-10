package com.atlassian.jira.plugin.userformat;

import com.atlassian.jira.plugin.profile.UserFormat;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Very simple implementation that only renders the users full name.
 *
 * @since v3.13
 */
public class FullNameUserFormat implements UserFormat
{
    public static final String TYPE = "fullName";

    private final UserManager userManager;
    private final UserUtil userUtil;

    public FullNameUserFormat(final UserManager userManager, final UserUtil userUtil)
    {
        this.userManager = userManager;
        this.userUtil = userUtil;
    }

    @Override
    @HtmlSafe
    public String format(final String key, final String id)
    {
        return format(key, id, null);
    }

    @Override
    @HtmlSafe
    public String format(final String key, final String id, final Map<String, Object> params)
    {
        if (StringUtils.isBlank(key))
        {
            return null;
        }

        final ApplicationUser user = userManager.getUserByKeyEvenWhenUnknown(key);
        final String formattedUser = userUtil.getDisplayableNameSafely(user);

        Boolean escape = params != null ? (Boolean) params.get("escape") : null;
        if (escape != null && !escape)
        {
            return formattedUser;
        }
        else
        {
            return TextUtils.htmlEncode(formattedUser);
        }
    }
}
