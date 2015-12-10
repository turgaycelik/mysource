package com.atlassian.jira.dashboard;

import java.lang.Iterable;
import java.lang.Object;
import java.lang.String;
import java.net.URI;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.atlassian.gadgets.directory.spi.SubscribedGadgetFeed;
import com.atlassian.gadgets.directory.spi.SubscribedGadgetFeedStore;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * @since v6.0
 */
public class JiraSubscribedGadgetFeedStore implements SubscribedGadgetFeedStore {
    static final String KEY = "com.atlassian.gadgets.directory.SubscribedGadgetFeedStore";

    private final PluginSettingsFactory factory;

    public JiraSubscribedGadgetFeedStore(PluginSettingsFactory factory)
    {
        this.factory = Preconditions.checkNotNull(factory, "factory");
    }

    public SubscribedGadgetFeed addFeed(URI feedUri)
    {
        final SubscribedGadgetFeed feed = new SubscribedGadgetFeed(UUID.randomUUID().toString(), feedUri);
        put(ImmutableMap.<String, SubscribedGadgetFeed>builder().putAll(feeds()).put(feed.getId(), feed).build());

        return feed;
    }

    public boolean containsFeed(String feedId)
    {
        return feeds().containsKey(feedId);
    }

    public SubscribedGadgetFeed getFeed(String feedId)
    {
        return feeds().get(feedId);
    }

    public Iterable<SubscribedGadgetFeed> getAllFeeds()
    {
        return feeds().values();
    }

    public void removeFeed(String feedId)
    {
        put(Maps.filterKeys(feeds(), Predicates.not(Predicates.equalTo(feedId))));
    }

    private void put(Map<String, SubscribedGadgetFeed> feeds)
    {
        PluginSettings settings = factory.createGlobalSettings();
        Properties properties = new Properties();
        properties.putAll(Maps.transformValues(feeds, serialize()));
        settings.put(KEY, properties);
    }

    private Map<String, SubscribedGadgetFeed> feeds()
    {
        PluginSettings settings = factory.createGlobalSettings();
        Properties serializedFeeds = (Properties) settings.get(KEY);
        if (serializedFeeds == null)
        {
            return ImmutableMap.of();
        }

        ImmutableMap.Builder<String, SubscribedGadgetFeed> feeds = ImmutableMap.builder();
        for (Map.Entry<Object, Object> entry : serializedFeeds.entrySet())
        {
            String id = (String) entry.getKey();
            feeds.put(id, new SubscribedGadgetFeed(id, URI.create((String) entry.getValue())));
        }
        return feeds.build();
    }

    private Function<SubscribedGadgetFeed, String> serialize()
    {
        return Serializer.INSTANCE;
    }

    private static enum Serializer implements Function<SubscribedGadgetFeed, String>
    {
        INSTANCE;

        public String apply(SubscribedGadgetFeed feed)
        {
            return feed.getUri().toASCIIString();
        }
    }
}
