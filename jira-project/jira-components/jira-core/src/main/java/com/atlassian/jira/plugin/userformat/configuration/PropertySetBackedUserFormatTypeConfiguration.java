package com.atlassian.jira.plugin.userformat.configuration;

import java.util.Collection;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * Responsible for storing the configured user format modules for each user format type in a property set.
 *
 * The underlying property set stores the name of a user format type as a key an its value is the plugin module key
 * of the user format descriptor currently configured for that type.
 *
 * @since v3.13
 */
@EventComponent
public class PropertySetBackedUserFormatTypeConfiguration implements UserFormatTypeConfiguration
{
    private final CachedReference<PropertySet> mappingPSRef;

    public PropertySetBackedUserFormatTypeConfiguration(
            final JiraPropertySetFactory jiraPropertySetFactory, final CacheManager cacheManager)
    {
        this.mappingPSRef = cacheManager.getCachedReference(getClass(),  "mappingPSRef",
                new UserFormatMappingSupplier(jiraPropertySetFactory));
    }

    @EventListener
    public void onClearCache(@SuppressWarnings("unused") final ClearCacheEvent event)
    {
        mappingPSRef.reset();
    }

    public void setUserFormatKeyForType(final String userFormatType, final String moduleKey)
    {
        mappingPSRef.get().setString(userFormatType, moduleKey);
        mappingPSRef.reset();
    }

    @Override
    public String getUserFormatKeyForType(final String userFormatType)
    {
        return mappingPSRef.get().getString(userFormatType);
    }

    @Override
    public boolean containsType(final String userFormatType)
    {
        return mappingPSRef.get().getString(userFormatType) != null;
    }

    @Override
    public void remove(final String userFormatType)
    {
        mappingPSRef.get().remove(userFormatType);
        mappingPSRef.reset();
    }

    @SuppressWarnings("unchecked")
    Collection<String> getConfiguredTypes()
    {
        return mappingPSRef.get().getKeys();
    }

    /**
     * Supplies a property set containing the user format mappings. Note that
     * because this is a caching property set, we throw it away after every
     * mutation so that other nodes in the cluster will do the same and
     * therefore see the change when they next read from the database using
     * this supplier.
     */
    static class UserFormatMappingSupplier implements Supplier<PropertySet>
    {
        static final String USER_FORMAT_CONFIGURATION_PROPERTY_SET_KEY = "user.format.mapping";

        private final JiraPropertySetFactory jiraPropertySetFactory;

        public UserFormatMappingSupplier(final JiraPropertySetFactory jiraPropertySetFactory)
        {
            this.jiraPropertySetFactory = jiraPropertySetFactory;
        }

        @Override
        public PropertySet get()
        {
            return jiraPropertySetFactory.buildCachingDefaultPropertySet(
                    USER_FORMAT_CONFIGURATION_PROPERTY_SET_KEY, true);
        }
    }
}
