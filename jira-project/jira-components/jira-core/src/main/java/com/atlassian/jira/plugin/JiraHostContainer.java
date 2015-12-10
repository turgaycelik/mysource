package com.atlassian.jira.plugin;

import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.hostcontainer.HostContainer;

/**
 * @since v4.0
 */
public class JiraHostContainer implements HostContainer
{
    public <T> T create(final Class<T> tClass) throws IllegalArgumentException
    {
        return JiraUtils.loadComponent(tClass);
    }
}
