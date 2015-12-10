package com.atlassian.jira.my_home;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeUpdateException;

import javax.annotation.Nonnull;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Applies validation before storing the new plugin module key.
 */
public class MyJiraHomeUpdateServiceImpl implements MyJiraHomeUpdateService
{
    private final MyJiraHomeValidator validator;
    private final MyJiraHomeStorage storage;

    public MyJiraHomeUpdateServiceImpl(@Nonnull final MyJiraHomeValidator validator, @Nonnull final MyJiraHomeStorage storage) {
        this.validator = validator;
        this.storage = storage;
    }

    @Override
    public void updateHome(@Nonnull final User user, @Nonnull final String completePluginModuleKey)
    {
        if (!isNullOrEmpty(completePluginModuleKey.trim()) && validator.isInvalid(completePluginModuleKey))
        {
            throw new MyJiraHomeUpdateException("The plugin module referenced by the plugin module key is not usable: " + completePluginModuleKey);
        }

        storage.store(user, completePluginModuleKey);
    }
}
