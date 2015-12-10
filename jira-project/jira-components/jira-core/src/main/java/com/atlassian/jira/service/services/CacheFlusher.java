package com.atlassian.jira.service.services;

import java.util.Map;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.util.collect.MapBuilder;

import org.apache.log4j.Logger;

/**
 * Flushes caches in JIRA to save memory. Do not activate this unless you have independently determined it will not make
 * your performance worse.
 *
 * @since v6.1
 */
@SuppressWarnings ("UnusedDeclaration")
public class CacheFlusher extends AbstractService
{
    private static final Logger log = Logger.getLogger(CacheFlusher.class);

    @Override
    public void run()
    {
        boolean resultingFromCacheClearEvent = false;
        if (!resultingFromCacheClearEvent) {
            log.debug("Flushing JIRA memory caches");
            final Map<String, Boolean> props = MapBuilder.build(SERVICE_EVENT, true);
            ComponentAccessor.getComponent(EventPublisher.class).publish(new ClearCacheEvent(props));
        }
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("CACHEFLUSHER", "services/com/atlassian/jira/service/services/cacheflusher.xml", null);
    }
}
