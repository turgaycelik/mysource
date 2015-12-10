package com.atlassian.jira.studio.startup;

import javax.annotation.Nonnull;

/**
 * Strategy for finding some studio hooks.
 *
 * @since v4.4.1
 */
interface Locator
{
    StudioStartupHooks locate(@Nonnull ClassLoader loader);
}
