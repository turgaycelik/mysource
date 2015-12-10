package com.atlassian.jira.event;

import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.internal.AnnotatedMethodsListenerHandler;
import com.atlassian.event.spi.ListenerHandler;
import com.atlassian.jira.event.issue.IssueEventListenerHandler;
import com.atlassian.jira.event.user.UserEventListenerHandler;
import com.atlassian.plugin.event.PluginEventListener;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Provides the listener handlers for atlassian-events that JIRA requires.
 *
 * @since v4.1
 */
public class JiraListenerHandlerConfigurationImpl implements ListenerHandlersConfiguration
{
    private final List<ListenerHandler> handlers = ImmutableList.of( new IssueEventListenerHandler(),
            new UserEventListenerHandler(), new AnnotatedMethodsListenerHandler(PluginEventListener.class),
            new AnnotatedMethodsListenerHandler());

    public List<ListenerHandler> getListenerHandlers()
    {
        return handlers;
    }
}
