package com.atlassian.jira.issue.fields.config.persistence;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.fields.ConfigurableField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.map.CacheObject;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * A simple caching wrapper
 * <p/>
 * NOTE : you may be wondering about the cache invalidation strategy on this cache.  Will the top level classes
 * that use this cache such as {@link com.atlassian.jira.issue.CustomFieldManager@refreshCache} call {@link #init()}
 * and this clears the cache.
 * <p/>
 * TODO: This probably should be rewritten so that the upper lays of code are not responsible for clearing the lower level caches
 * and also the "cache inheritance" pattern should be removed.
 */
@EventComponent
public class CachedFieldConfigSchemePersister extends FieldConfigSchemePersisterImpl
{
    private final FieldConfigManager fieldConfigManager;

    private final Cache<Long, CacheObject<FieldConfigScheme>> cacheById;
    private final Cache<String, CacheObject<List<FieldConfigScheme>>> cacheByCustomField;
    private final Cache<Long, FieldConfigScheme> cacheByFieldConfig;

    public CachedFieldConfigSchemePersister(OfBizDelegator delegator, ConstantsManager constantsManager,
            final FieldConfigPersister fieldConfigPersister, final FieldConfigContextPersister fieldContextPersister,
            final CacheManager cacheManager, final FieldConfigManager fieldConfigManager)
    {
        super(delegator, constantsManager, fieldConfigPersister, fieldContextPersister);
        this.fieldConfigManager = fieldConfigManager;

        this.cacheById = cacheManager.getCache(CachedFieldConfigSchemePersister.class.getName() + ".cacheById",
                new FiledConfigByIdCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        this.cacheByCustomField = cacheManager.getCache(CachedFieldConfigSchemePersister.class.getName() + ".cacheByCustomField",
                new FieldConfigSchemeByFieldCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        this.cacheByFieldConfig = cacheManager.getCache(CachedFieldConfigSchemePersister.class.getName() + ".cacheByFieldConfig",
                new FieldConfigSchemeBySchemeCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @Override
    public void init()
    {
        super.init();
        cacheById.removeAll();
        cacheByCustomField.removeAll();
        cacheByFieldConfig.removeAll();
    }


    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cacheById.removeAll();
        cacheByCustomField.removeAll();
        cacheByFieldConfig.removeAll();
    }

    @Nullable
    @Override
    public FieldConfigScheme getFieldConfigScheme(Long configSchemeId)
    {
        if (configSchemeId == null)
        {
            return null;
        }
        return cacheById.get(configSchemeId).getValue();
    }

    @Nullable
    @Override
    public List<FieldConfigScheme> getConfigSchemesForCustomField(final ConfigurableField field)
    {
        if (field == null)
        {
            return null;
        }
        return cacheByCustomField.get(field.getId()).getValue();
    }

    @Nullable
    @Override
    public FieldConfigScheme getConfigSchemeForFieldConfig(final FieldConfig fieldConfig)
    {
        if (fieldConfig == null)
        {
            return null;
        }
        return cacheByFieldConfig.get(fieldConfig.getId());
    }

    @Override
    public FieldConfigScheme update(final FieldConfigScheme configScheme)
    {
        FieldConfigScheme fieldConfigScheme = null;
        try
        {
            fieldConfigScheme = super.update(configScheme);
        }
        finally
        {
            if (fieldConfigScheme != null)
            {
                cacheById.remove(fieldConfigScheme.getId());
                final ConfigurableField field = fieldConfigScheme.getField();
                if (field != null)
                {
                    cacheByCustomField.remove(field.getId());
                }
                // Note: Old configs will get invalidated by removeRelatedConfigsForUpdate.  This round may not
                // even be necessary because the update deletes and recreates these, pretty much guaranteeing
                // that the new configs have new IDs.  Deliberately being paranoid for now.
                Map<String, FieldConfig> configs = fieldConfigScheme.getConfigs();
                for (FieldConfig config : configs.values())
                {
                    cacheByFieldConfig.remove(config.getId());
                }
            }
        }
        return fieldConfigScheme;
    }

    @Override
    protected void removeRelatedConfigsForUpdate(@Nonnull final FieldConfigScheme configScheme, @Nonnull final GenericValue gv)
            throws GenericEntityException
    {
        try
        {
            super.removeRelatedConfigsForUpdate(configScheme, gv);
        }
        finally
        {
            final Map<String, FieldConfig> configs = configScheme.getConfigs();
            for (FieldConfig config : configs.values())
            {
                cacheByFieldConfig.remove(config.getId());
            }
        }
    }

    @Override
    public void remove(Long fieldConfigSchemeId)
    {
        FieldConfigScheme fieldConfigScheme = null;
        try
        {
            fieldConfigScheme = super.removeIfExist(fieldConfigSchemeId);
        }
        finally
        {
            cacheById.remove(fieldConfigSchemeId);
            if (fieldConfigScheme != null)
            {
                final ConfigurableField field = fieldConfigScheme.getField();
                if (field != null)
                {
                    cacheByCustomField.remove(field.getId());
                }
                Map<String, FieldConfig> configs = fieldConfigScheme.getConfigs();
                for (FieldConfig config : configs.values())
                {
                    cacheByFieldConfig.remove(config.getId());
                }
            }
        }
    }

    private class FiledConfigByIdCacheLoader implements CacheLoader<Long, CacheObject<FieldConfigScheme>>
    {
        @Override
        public CacheObject<FieldConfigScheme> load(@Nonnull final Long configSchemeId)
        {
            FieldConfigScheme scheme =  CachedFieldConfigSchemePersister.super.getFieldConfigScheme(configSchemeId);
            return CacheObject.wrap(scheme);
        }
    }

    private class FieldConfigSchemeByFieldCacheLoader implements CacheLoader<String, CacheObject<List<FieldConfigScheme>>>
    {
        @Override
        public CacheObject<List<FieldConfigScheme>> load(@Nonnull final String fieldId)
        {
            ConfigurableField field = (ConfigurableField) ComponentAccessor.getFieldManager().getField(fieldId);
            List<FieldConfigScheme> schemes = CollectionUtil.copyAsImmutableList(CachedFieldConfigSchemePersister.super.getConfigSchemesForCustomField(field));
            return  CacheObject.wrap(schemes);
        }
    }

    private class FieldConfigSchemeBySchemeCacheLoader implements CacheLoader<Long, FieldConfigScheme>
    {
        @Override
        public FieldConfigScheme load(@Nonnull final Long configId)
        {
            FieldConfig fieldConfig = fieldConfigManager.getFieldConfig(configId);
            return  CachedFieldConfigSchemePersister.super.getConfigSchemeForFieldConfig(fieldConfig);
        }
    }
}
