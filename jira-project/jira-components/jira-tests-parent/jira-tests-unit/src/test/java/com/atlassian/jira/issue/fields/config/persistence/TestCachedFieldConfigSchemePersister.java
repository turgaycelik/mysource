package com.atlassian.jira.issue.fields.config.persistence;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestCachedFieldConfigSchemePersister
{
    private CacheManager cacheManager = new MemoryCacheManager();

    @After
    public void tearDown()
    {
        cacheManager = null;
    }

    @Test
    public void testGetConfigSchemeForFieldConfig() throws Exception
    {
        final Long fieldConfigId = 10L;
        final Long schemeId = 200L;

        final OfBizDelegator delegator = mock(OfBizDelegator.class);
        when(delegator.findByAnd("FieldConfigSchemeIssueType", FieldMap.build("fieldconfiguration", fieldConfigId)))
                .thenReturn(values(new MockGenericValue("FieldConfigSchemeIssueType", FieldMap.build("fieldconfigscheme", schemeId))));

        final FieldConfig fieldConfig = mock(FieldConfig.class);
        when(fieldConfig.getId()).thenReturn(fieldConfigId);

        final FieldConfigPersister fieldConfigPersister = mock(FieldConfigPersister.class);
        final ConstantsManager constantsManager = mock(ConstantsManager.class);
        final FieldConfigScheme configScheme = mock(FieldConfigScheme.class);
        final FieldConfigManager fieldConfigManager = mock(FieldConfigManager.class);
        when(fieldConfigManager.getFieldConfig(10L)).thenReturn(fieldConfig);

        final AtomicInteger called = new AtomicInteger(0);
        final CachedFieldConfigSchemePersister persister = new CachedFieldConfigSchemePersister(delegator, constantsManager,
                fieldConfigPersister, null, cacheManager, fieldConfigManager)
        {
            @Override
            public FieldConfigScheme getFieldConfigScheme(final Long configSchemeId)
            {
                called.incrementAndGet();
                return configScheme;
            }
        };

        persister.getConfigSchemeForFieldConfig(fieldConfig);
        persister.getConfigSchemeForFieldConfig(fieldConfig);
        
        assertEquals(1, called.get());
    }

    static List<GenericValue> values(GenericValue... genericValues)
    {
        return ImmutableList.copyOf(genericValues);
    }
}
