package com.atlassian.jira.issue.fields.option;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.concurrent.TimeUnit.MINUTES;

@EventComponent
public class CachedOptionSetManager implements OptionSetManager
{
    private final Cache<Long, OptionSet> cache;
    private final OptionSetManagerImpl optionSetManager;

    public CachedOptionSetManager(OptionSetPersister optionSetPersister, ConstantsManager constantsManager,
            CacheManager cacheManager)
    {
        this.optionSetManager = new OptionSetManagerImpl(optionSetPersister, constantsManager);
        this.cache = cacheManager.getCache(CachedOptionSetManager.class.getName() + ".cache", new OptionSetLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, MINUTES).build());
    }

    @EventListener
    public void onClearCache(@SuppressWarnings("unused") final ClearCacheEvent event)
    {
        cache.removeAll();
    }

    public OptionSet getOptionsForConfig(@Nonnull FieldConfig config)
    {
        return cache.get(notNull("config", config).getId());
    }

    public OptionSet createOptionSet(@Nonnull FieldConfig config, Collection optionIds)
    {
        notNull("config", config);
        try
        {
            return optionSetManager.createOptionSet(config, optionIds);
        }
        finally
        {
            cache.remove(config.getId());
        }
    }

    public OptionSet updateOptionSet(@Nonnull FieldConfig config, Collection optionIds)
    {
        notNull("config", config);
        try
        {
            return optionSetManager.updateOptionSet(config, optionIds);
        }
        finally
        {
            cache.remove(config.getId());
        }
    }

    public void removeOptionSet(@Nonnull FieldConfig config)
    {
        notNull("config", config);
        try
        {
            optionSetManager.removeOptionSet(config);
        }
        finally
        {
            cache.remove(config.getId());
        }
    }

    class OptionSetLoader implements CacheLoader<Long, OptionSet>
    {
        public OptionSet load(final Long fieldConfigId)
        {
            // Injecting this component causes a circular dependency
            final FieldConfigManager fieldConfigManager = ComponentAccessor.getComponent(FieldConfigManager.class);
            final FieldConfig fieldConfig = fieldConfigManager.getFieldConfig(fieldConfigId);
            return optionSetManager.getOptionsForConfig(fieldConfig);
        }
    }
}
