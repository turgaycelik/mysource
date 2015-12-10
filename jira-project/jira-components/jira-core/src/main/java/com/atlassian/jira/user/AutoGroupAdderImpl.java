package com.atlassian.jira.user;

import com.atlassian.crowd.core.event.listener.AutoGroupAdderListener;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.util.ComponentLocator;

/**
 * Registers an {@link AutoGroupAdderListener}.
 *
 * @since v4.4
 */
public class AutoGroupAdderImpl implements AutoGroupAdder, Startable
{
    private final EventPublisher eventPublisher;
    private final ComponentLocator componentLocator;

    public AutoGroupAdderImpl(EventPublisher eventPublisher, ComponentLocator componentLocator)
    {
        this.eventPublisher = eventPublisher;
        this.componentLocator = componentLocator;
    }

    @Override
    public void start() throws Exception
    {
        DirectoryInstanceLoader directoryInstanceLoader = componentLocator.getComponentInstanceOfType(DirectoryInstanceLoader.class);
        DirectoryManager directoryManager = componentLocator.getComponentInstanceOfType(DirectoryManager.class);
        AutoGroupAdderListener autoGroupAdderListener = new AutoGroupAdderListener(directoryManager);
        eventPublisher.register(autoGroupAdderListener);
    }
}
