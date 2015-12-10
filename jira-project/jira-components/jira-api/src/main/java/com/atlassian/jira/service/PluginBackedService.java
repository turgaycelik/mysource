package com.atlassian.jira.service;

import com.atlassian.annotations.PublicSpi;

/**
 * Optional interface for JIRA services that are backed by plugins. Due to the nature of the plugin system, a service
 * that is backed by a plugin may become unavailable at any time if the plugin is disabled or uninstalled.
 *
 * @since v5.2
 */
@PublicSpi
public interface PluginBackedService
{
    /**
     * Returns true if this service is usable. By default a service is always usable, but services that are backed by
     * plugin jobs, for example, may become unusable when the plugin is uninstalled or disabled.
     *
     * @return a boolean indicating whether the underlying service is usable
     * @since v5.2
     */
    boolean isAvailable();
}
