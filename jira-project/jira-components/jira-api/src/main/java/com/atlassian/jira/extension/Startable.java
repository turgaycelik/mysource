package com.atlassian.jira.extension;

/**
 * <p/>
 * Implementing this interface allows Components to be notified of when the JIRA application has started.
 *
 * <p>
 * NOTE: this is an internal JIRA mechanism that is no longer intended to be used by plugins. It is not part of
 * the official JIRA API and its support for plugin components is subject to removal at any point of time.
 *
 * <p/>
 * Plugin developers should implement {@link com.atlassian.sal.api.lifecycle.LifecycleAware} from the Shared
 * Access Layer for the equivalent and officially supported functionality.
 *
 */
public interface Startable
{
    /**
     * This method wil be called after the plugin system is fully initialised and all components added to the
     * dependency injection framework.
     *
     * @throws Exception Allows implementations to throw an Exception.
     */
    public void start() throws Exception;
}
