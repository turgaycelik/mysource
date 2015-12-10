package com.atlassian.jira.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.cluster.ClusterMessageConsumer;
import com.atlassian.jira.cluster.ClusterMessagingService;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.cluster.Message;
import com.atlassian.jira.cluster.MessageHandlerService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build605;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.util.concurrent.ResettableLazyReference;

import com.google.common.collect.ImmutableSet;
import com.opensymphony.module.propertyset.PropertySet;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.cluster.ClusterManager.ALL_NODES;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultListenerManager implements ListenerManager, Startable
{
    public final static String REFRESH_LISTENERS = "Refresh Listeners";
    private static final Logger log = Logger.getLogger(DefaultListenerManager.class);
    /*
     * Registry of deleted listeners.  This prevents the ListenerManager from trying to load them and throwing an
     * exception before an upgrade task goes and deletes it.
     */
    private static final Collection<String> DELETED_LISTENERS = ImmutableSet.of(UpgradeTask_Build605.ISSUE_CACHE_LISTENER_CLASS);

    private final EventPublisher eventPublisher;
    @ClusterSafe("This class sends a cluster message when refresh is required")
    private final Listeners listeners = new Listeners();
    private final OfBizDelegator ofBizDelegator;
    private final MessageConsumer messageConsumer; // Needed to keep the consumer from being garbage collected.

    public DefaultListenerManager(final EventPublisher eventPublisher, final OfBizDelegator ofBizDelegator, final ClusterMessagingService messagingService)
    {
        this.eventPublisher = notNull("eventPublisher", eventPublisher);
        this.ofBizDelegator = notNull("ofBizDelegator", ofBizDelegator);
        messageConsumer = new MessageConsumer(listeners);
        messagingService.registerListener(REFRESH_LISTENERS, messageConsumer);
    }

    public void start() throws Exception
    {
        // Initialize the listeners and plonk them into the eventPublisher
        listeners.start();
        eventPublisher.register(this);
    }

    @SuppressWarnings ({ "UnusedParameters" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    @Override
    public Map<String, JiraListener> getListeners()
    {
        @SuppressWarnings("deprecation")
        final ComponentManager componentManager = ComponentManager.getInstance();
        if (!componentManager.getState().isComponentsRegistered())
        {
            throw new IllegalStateException(
                "Listeners are not available until all components have been registered with the ComponentManager. Components can implement the " + Startable.class + " to be notified when the system is ready.");
        }
        return listeners.get();
    }

    @Override
    public JiraListener createListener(String name, Class<? extends JiraListener> clazz)
    {
        ofBizDelegator.createValue("ListenerConfig", FieldMap.build("name", name).add("clazz", clazz.getName()));
        refresh();

        return getListeners().get(name);
    }

    @Override
    public void deleteListener(Class<? extends JiraListener> clazz)
    {
        final Collection<GenericValue> listenerConfigs = ofBizDelegator.findByAnd("ListenerConfig", FieldMap.build("clazz", clazz.getName()));

        if (listenerConfigs.size() > 0)
        {
            ofBizDelegator.removeAll(new ArrayList<GenericValue>(listenerConfigs));
            refresh();
        }
    }

    @Override
    public void refresh()
    {
        listeners.reset();
        // Can't constructor-inject the MessageHandlerService as it creates a circular dependency
        final MessageHandlerService messageHandlerService = ComponentAccessor.getComponent(MessageHandlerService.class);
        messageHandlerService.sendMessage(ALL_NODES, new Message(REFRESH_LISTENERS, null));
    }

    @Override
    public void onRefreshListeners()
    {
        listeners.reset();
    }

    /**
     * Load and hold the listeners map for us, guarantees singleton thread-safety and safe reference publication.
     */
    private final class Listeners extends ResettableLazyReference<Map<String, JiraListener>>
    {
        @Override
        protected Map<String, JiraListener> create() throws Exception
        {
            final Map<String, JiraListener> jiraListenerMap = loadListeners();
            registerDBListeners(jiraListenerMap.values());
            return jiraListenerMap;
        }

        @Override
        public void reset()
        {
            // Get rid of all the old listeners
            final Map<String, JiraListener> map = get();
            super.reset();
            for (final JiraListener listener : map.values())
            {
                eventPublisher.unregister(listener);
            }
            // Make sure we prime the listeners so they are ready to listen for events
            get();
        }

        void start()
        {
            // just call get to init the listeners
            get();
        }

        /**
         * Do the heavy lifting of creating the listeners.
         *
         * @return the map of listeners by name.
         */
        private Map<String, JiraListener> loadListeners()
        {
            final MapBuilder<String, JiraListener> listeners = MapBuilder.newBuilder();

            final Collection<GenericValue> listenerConfigs = ofBizDelegator.findAll("ListenerConfig");

            if (listenerConfigs == null)
            {
                log.info("No Listeners to Load");
                return Collections.emptyMap();
            }

            for (final GenericValue gv : listenerConfigs)
            {
                final String className = gv.getString("clazz");
                final String key = gv.getString("name");
                if (DELETED_LISTENERS.contains(className))
                {
                    log.debug("Not loading deleted listener: " + className);
                }
                else
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Creating listener. Class: " + className + ". Name: " + key);
                    }
                    try
                    {
                        listeners.add(key, ListenerFactory.getListener(className, getParameters(gv)));
                    }
                    catch (final Exception e)
                    {
                        log.error("Could not configure listener: " + key + " className:" + className, e);
                    }
                }
            }
            return listeners.toMap();
        }

        private Map<String, String> getParameters(final GenericValue gv)
        {
            final PropertySet ps = OFBizPropertyUtils.getPropertySet(gv);

            @SuppressWarnings("unchecked")
            final Collection<String> paramKeys = ps.getKeys(PropertySet.STRING);
            final MapBuilder<String, String> params = MapBuilder.newBuilder();
            for (final String key : paramKeys)
            {
                params.add(key, ps.getString(key));
            }
            return params.toMap();
        }

        private void registerDBListeners(final Collection<JiraListener> listeners)
        {
            for (final JiraListener listener : listeners)
            {
                eventPublisher.register(listener);
            }
        }
    }

    private static class MessageConsumer implements ClusterMessageConsumer
    {

        private final Listeners listeners;

        public MessageConsumer(final Listeners listeners)
        {
            this.listeners = listeners;
        }

        @Override
        public void receive(final String channel, final String message, final String senderId)
        {
            if (channel.equals(DefaultListenerManager.REFRESH_LISTENERS))
            {
                listeners.reset();
            }
        }
    }
}
