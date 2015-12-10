package com.atlassian.jira.my_home;

import javax.annotation.Nonnull;

/**
 * Checks whether the current My JIRA Home (the plugin module key) is actual valid and usable.
 */
public interface MyJiraHomeValidator
{
    boolean isValid(@Nonnull String completePluginModuleKey);

    boolean isInvalid(@Nonnull String completePluginModuleKey);
}
