package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Checks if there are any user directories with 'write' permission.
 *
 * @since v5.0
 */
public class HasWritableDirectoryCondition implements Condition
{
    private final UserManager userManager;

    public HasWritableDirectoryCondition(UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return userManager.hasWritableDirectory();
    }
}
