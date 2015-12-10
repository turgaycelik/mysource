package com.atlassian.jira.startup;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.tenancy.TenantImpl;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.tenancy.api.event.TenantArrivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher that sends an event announcing that a tenant is present.
 *
 * Note: This launcher is temporary, and a way to start changing core services to make them tenant-aware without breaking
 * everything. In the future, this event will be sent when a tenant is actually assigned to an instance, instead of
 * during startup (at least in OnDemand).
 *
 * @since v6.3
 */
public class TenancyLauncher implements JiraLauncher
{
    private static final Logger log = LoggerFactory.getLogger(TenancyLauncher.class);

    @Override
    public void start()
    {
        if (JiraUtils.isSetup())
        {
            log.debug("Tenanting JIRA...");
            // In a lot of cases, clearing the cache will be sufficient, so we send a ClearCacheEvent now. Services who only
            // need to reset their caches can just depend on ClearCacheEvent instead of TenantArrivedEvent. The fewer services
            // depend on TenantArrivedEvent, the better.
            ComponentAccessor.getComponent(EventPublisher.class).publish(ClearCacheEvent.INSTANCE);
            final String baseUrl = ComponentAccessor.getComponent(ApplicationProperties.class).getDefaultBackedString(APKeys.JIRA_BASEURL);
            ComponentAccessor.getComponent(EventPublisher.class).publish(new TenantArrivedEvent(new TenantImpl(baseUrl)));
        }
    }

    @Override
    public void stop()
    {
        // no-op
    }
}
