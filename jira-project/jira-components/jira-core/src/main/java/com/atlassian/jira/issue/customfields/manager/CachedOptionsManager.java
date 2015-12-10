package com.atlassian.jira.issue.customfields.manager;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.util.map.CacheObject;

import com.google.common.collect.ImmutableList;

/**
 * Cache for Options Manager. Not particularly neat More a problem with how the OptionsManager is used really
 */
@EventComponent
public class CachedOptionsManager extends DefaultOptionsManager
{
    private final CachedReference<List<Option>> allCache;
    private final Cache<Long, CacheObject<Options>> optionsCache;
    private final Cache<Long, CacheObject<Option>> optionCache;
    private final Cache<Long, List<Option>> parentCache;
    private final Cache<String, List<Option>> valueCache;

    public CachedOptionsManager(OfBizDelegator delegator, CollectionReorderer<Option> reorderer,
            FieldConfigManager fieldConfigManager, CacheManager cacheManager)
    {
        super(delegator, reorderer, fieldConfigManager);
        allCache = cacheManager.getCachedReference(CachedOptionsManager.class, "allCache", new AllOptionsSupplier());

        optionsCache = cacheManager.getCache(CachedOptionsManager.class.getName() + ".optionsCache",
                new OptionsCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        optionCache = cacheManager.getCache(CachedOptionsManager.class.getName() + ".optionCache",
                new OptionCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        parentCache = cacheManager.getCache(CachedOptionsManager.class.getName() + ".parentCache",
                new ParentCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        valueCache = cacheManager.getCache(CachedOptionsManager.class.getName() + ".valueCache",
                new ValueCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        init();
    }

    @EventListener
    public void onClearCache(@SuppressWarnings("unused") final ClearCacheEvent event)
    {
        init();
    }

    private void init()
    {
        optionsCache.removeAll();
        optionCache.removeAll();
        parentCache.removeAll();
        valueCache.removeAll();
        allCache.reset();
    }

    @Override
    public List<Option> getAllOptions()
    {
        return allCache.get();
    }

    @Override
    public List<Option> findByOptionValue(String value)
    {
        if (value == null)
        {
            return null;
        }

        // make the cache lookup case insensitive too
        value = CaseFolding.foldString(value);

        return valueCache.get(value);
    }

    public Options getOptions(FieldConfig fieldConfig)
    {
        final Long key = (fieldConfig == null) ? null : fieldConfig.getId();
        if (key == null)
        {
            // we can only cache something, not nothing
            return super.getOptions(fieldConfig);
        }
        return optionsCache.get(fieldConfig.getId()).getValue();
    }

    public void setRootOptions(FieldConfig fieldConfig, Options options)
    {
        super.setRootOptions(fieldConfig, options);

        init();
    }

    public void removeCustomFieldOptions(CustomField customField)
    {
        super.removeCustomFieldOptions(customField);

        init();
    }

    @Override
    public void removeCustomFieldConfigOptions(final FieldConfig fieldConfig)
    {
        super.removeCustomFieldConfigOptions(fieldConfig);

        // Nuke it all if a custom field is removed
        init();
    }

    public void updateOptions(Collection<Option> options)
    {
        super.updateOptions(options);
        for (Option option : options)
        {
            removeOptionFromCaches(option);
        }
        optionsCache.removeAll();
        allCache.reset();
    }

    public Option createOption(FieldConfig fieldConfig, Long parentOptionId, Long sequence, String value)
    {
        Option option = super.createOption(fieldConfig, parentOptionId, sequence, value);
        removeOptionFromCaches(option);
        optionsCache.removeAll();
        allCache.reset();
        return option;
    }

    public void deleteOptionAndChildren(Option option)
    {
        super.deleteOptionAndChildren(option);

        init();
    }

	public void setValue(Option option, String value) {
        String oldValue = option.getValue();
        super.setValue(option, value);
        removeOptionFromCaches(option);
        if (option.getValue() != null)
        {
            valueCache.remove(CaseFolding.foldString(oldValue));
        }
        if (option.getValue() != null)
        {
            valueCache.remove(CaseFolding.foldString(value));
        }
        optionsCache.removeAll();
        allCache.reset();
	}

	public void disableOption(Option option) {
		super.disableOption(option);
        removeOptionFromCaches(option);
        optionsCache.removeAll();
        allCache.reset();
	}

	public void enableOption(Option option) {
		super.enableOption(option);
        removeOptionFromCaches(option);
        optionsCache.removeAll();
        allCache.reset();
	}

    private void removeOptionFromCaches(final Option option)
    {
        optionCache.remove(option.getOptionId());
        if (option.getValue() != null)
        {
            valueCache.remove(CaseFolding.foldString(option.getValue()));
        }
        if (option.getParentOption() != null)
        {
            parentCache.remove(option.getParentOption().getOptionId());
        }
    }

    public Option findByOptionId(Long optionId)
    {
        if (optionId == null)
        {
            return null;
        }
        return optionCache.get(optionId).getValue();
    }


    public List<Option> findByParentId(Long parentOptionId)
    {
        return parentCache.get(parentOptionId);
    }

    private class OptionsCacheLoader implements CacheLoader<Long, CacheObject<Options>>
    {
        @Override
        public CacheObject<Options> load(@Nonnull final Long fieldConfigId)
        {
            final FieldConfig fieldConfig = fieldConfigManager.getFieldConfig(fieldConfigId);
            return CacheObject.wrap(CachedOptionsManager.super.getOptions(fieldConfig));
        }
    }

    private class AllOptionsSupplier implements Supplier<List<Option>>
    {
        @Override
        public List<Option> get()
        {
            return ImmutableList.copyOf(CachedOptionsManager.super.getAllOptions());
        }
    }

    private class OptionCacheLoader implements CacheLoader<Long, CacheObject<Option>>
    {
        @Override
        public CacheObject<Option> load(@Nonnull final Long id)
        {
            return CacheObject.wrap(CachedOptionsManager.super.findByOptionId(id));
        }
    }

    private class ParentCacheLoader implements CacheLoader<Long, List<Option>>
    {
        @Override
        public List<Option> load(@Nonnull final Long id)
        {
            return ImmutableList.copyOf(CachedOptionsManager.super.findByParentId(id));
        }
    }
    private class ValueCacheLoader implements CacheLoader<String, List<Option>>
    {
        @Override
        public List<Option> load(@Nonnull final String value)
        {
            return ImmutableList.copyOf(CachedOptionsManager.super.findByOptionValue(value));
        }
    }
}
