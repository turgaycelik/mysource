package com.atlassian.jira.my_home;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomePreference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Applies validation after loading a value.
 */
public class MyJiraHomePreferenceImpl implements MyJiraHomePreference
{
    static final String DASHBOARD_PLUGIN_MODULE_KEY = "com.atlassian.jira.jira-my-home-plugin:set_my_jira_home_dashboard";

    private final MyJiraHomeValidator validator;
    private final MyJiraHomeStorage storage;

    public MyJiraHomePreferenceImpl(@Nonnull final MyJiraHomeValidator validator, @Nonnull final MyJiraHomeStorage storage) {
        this.validator = validator;
        this.storage = storage;
    }

    @Nonnull
    @Override
    public String findHome(@Nullable final User user)
    {
        if (user == null)
        {
            return "";
        }

        final String completePluginModuleKey = storage.load(user);
        if (isNullOrEmpty(completePluginModuleKey) || validator.isInvalid(completePluginModuleKey))
        {
            return DASHBOARD_PLUGIN_MODULE_KEY;
        }
        else
        {
            return completePluginModuleKey;
        }
    }

}
