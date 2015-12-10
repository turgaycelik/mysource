package com.atlassian.jira.notification;

import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestDefaultNotificationSchemeManager
{
    @Test
    public void testGetDefaultSchemeTranslationKeysExist() throws Exception
    {
        final I18nHelper i18n = mock(I18nHelper.class);

        when(i18n.getText("admin.schemes.notifications.default")).thenReturn("Default Notification Scheme");

        MockDefaultNotificationSchemeManager schemeManager = new MockDefaultNotificationSchemeManager(i18n);
        final GenericValue defaultScheme = schemeManager.createDefaultScheme();

        assertNotNull(defaultScheme);
        assertEquals("Default Notification Scheme", defaultScheme.getString("name"));
        assertNull(defaultScheme.getString("description"));
    }

    @Test
    public void testGetDefaultSchemeTranslationKeysDontExist() throws Exception
    {
        final I18nHelper i18n = mock(I18nHelper.class);

        when(i18n.getText("admin.schemes.notifications.default")).thenReturn("admin.schemes.notifications.default");

        MockDefaultNotificationSchemeManager schemeManager = new MockDefaultNotificationSchemeManager(i18n);
        final GenericValue defaultScheme = schemeManager.createDefaultScheme();

        assertNotNull(defaultScheme);
        assertEquals("Default Notification Scheme", defaultScheme.getString("name"));
        assertNull(defaultScheme.getString("description"));
    }

    static class MockDefaultNotificationSchemeManager extends DefaultNotificationSchemeManager
    {
        private final I18nHelper i18n;

        public MockDefaultNotificationSchemeManager(I18nHelper i18n)
        {
            super(null, null, null, null, null, null, null, null, null, null, new MemoryCacheManager());
            this.i18n = i18n;
        }

        @Override
        protected I18nHelper getApplicationI18n()
        {
            return i18n;
        }

        protected GenericValue createSchemeGenericValue(final Map<String,Object> values) throws GenericEntityException
        {
            return new MockGenericValue(getSchemeEntityName(), values);
        }

        public boolean schemeExists(final String name)
        {
            return false;
        }

        @Nullable
        @Override
        public GenericValue getDefaultScheme() throws GenericEntityException
        {
            return null;
        }

        protected void flushProjectSchemes()
        {

        }
    }
}
