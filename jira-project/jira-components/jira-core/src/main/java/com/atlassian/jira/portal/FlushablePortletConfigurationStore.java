package com.atlassian.jira.portal;

/**
 * @since v4.1
 */
public interface FlushablePortletConfigurationStore extends PortletConfigurationStore
{

    /**
     * Flush the cache by removing all entries.
     */
    public void flush();

}
