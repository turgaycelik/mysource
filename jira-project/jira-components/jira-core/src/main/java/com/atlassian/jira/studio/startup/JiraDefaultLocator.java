package com.atlassian.jira.studio.startup;

import javax.annotation.Nonnull;

/**
 * Locator that finds the {@link VanillaJiraStartupHooks}.
 *
 * @since v4.4.1
 */
class JiraDefaultLocator implements Locator
{
    @Nonnull
    @Override
    public StudioStartupHooks locate(@Nonnull ClassLoader loader)
    {
        return new VanillaJiraStartupHooks();
    }
}
