package com.atlassian.jira.scheme;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.event.scheme.AbstractSchemeAddedToProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeCopiedEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeRemovedFromProjectEvent;
import com.atlassian.jira.event.scheme.AbstractSchemeUpdatedEvent;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Test;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @since v4.0
 */
public class TestAbstractSchemeManager
{

    @Test
    public void testGetDefaultSchemeTranslationKeysExist() throws Exception
    {
        final MockI18nHelper i18n = new MockI18nHelper();
        i18n.stubWith("default.name.key", "default scheme name");
        i18n.stubWith("default.desc.key", "this is the default scheme scheme");

        MockAbstractSchemeManager schemeManager = new MockAbstractSchemeManager(i18n);
        final GenericValue defaultScheme = schemeManager.createDefaultScheme();

        assertNotNull(defaultScheme);
        assertEquals("default scheme name", defaultScheme.getString("name"));
        assertEquals("this is the default scheme scheme", defaultScheme.getString("description"));

    }

    @Test
    public void testGetDefaultSchemeTranslationKeysDontExist() throws Exception
    {
        final MockI18nHelper i18n = new MockI18nHelper();

        MockAbstractSchemeManager schemeManager = new MockAbstractSchemeManager(i18n);
        final GenericValue defaultScheme = schemeManager.createDefaultScheme();

        assertNotNull(defaultScheme);
        assertEquals("Default desc Scheme", defaultScheme.getString("name"));
        assertEquals("This is the default desc Scheme. Any new projects that are created will be assigned this scheme", defaultScheme.getString("description"));

    }

    @Test
    public void testGetDefaultSchemeTranslationNullKeys() throws Exception
    {
        final I18nHelper i18n = Mockito.mock(I18nHelper.class);

        MockAbstractSchemeManager schemeManager = new MockAbstractSchemeManager(i18n)
        {
            @Override
            public String getDefaultDescriptionKey()
            {
                return null;
            }

            @Override
            public String getDefaultNameKey()
            {
                return null;
            }
        };

        final GenericValue defaultScheme = schemeManager.createDefaultScheme();

        assertNotNull(defaultScheme);
        assertEquals("Default desc Scheme", defaultScheme.getString("name"));
        assertEquals("This is the default desc Scheme. Any new projects that are created will be assigned this scheme", defaultScheme.getString("description"));

    }

    private static class MockAbstractSchemeManager extends AbstractSchemeManager
    {
        private I18nHelper i18nHelper;

        MockAbstractSchemeManager(I18nHelper i18nHelper)
        {
            super(null, null, null, null, null, null, null, null, new MemoryCacheManager());
            this.i18nHelper = i18nHelper;
        }

        protected GenericValue createSchemeGenericValue(final Map values) throws GenericEntityException
        {
            return new MockGenericValue(getSchemeEntityName(), values);
        }

        public GenericValue getDefaultScheme() throws GenericEntityException
        {
            return null;
        }

        @Nonnull
        @Override
        protected AbstractSchemeCopiedEvent createSchemeCopiedEvent(@Nonnull final Scheme oldScheme, @Nonnull final Scheme newScheme)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        protected I18nHelper getApplicationI18n()
        {
            return this.i18nHelper;
        }

        public String getSchemeEntityName()
        {
            return "scheme.entity";
        }

        public String getEntityName()
        {
            return "scheme";
        }

        public String getSchemeDesc()
        {
            return "desc";
        }

        public String getDefaultNameKey()
        {
            return "default.name.key";
        }

        public String getDefaultDescriptionKey()
        {
            return "default.desc.key";
        }

        @Override
        protected GenericValue createSchemeEntityNoEvent(final GenericValue scheme, final SchemeEntity schemeEntity)
                throws GenericEntityException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        protected GenericValue copySchemeEntity(final GenericValue scheme, final GenericValue entity)
                throws GenericEntityException
        {
            return null;
        }

        @Override
        protected AbstractSchemeEvent createSchemeCreatedEvent(final Scheme scheme)
        {
            return null;
        }

        @Override
        protected AbstractSchemeUpdatedEvent createSchemeUpdatedEvent(final Scheme scheme, final Scheme originalScheme)
        {
            return null;
        }

        @Override
        protected SchemeEntity makeSchemeEntity(final GenericValue schemeEntityGV)
        {
            return null;
        }

        @Override
        protected Object createSchemeEntityDeletedEvent(final GenericValue entity)
        {
            return null;
        }

        public List<GenericValue> getEntities(final GenericValue scheme, final Long entityTypeId) throws GenericEntityException
        {
            return null;
        }

        public List<GenericValue> getEntities(final GenericValue scheme, final Long entityTypeId, final String parameter)
                throws GenericEntityException
        {
            return null;
        }

        public List<GenericValue> getEntities(final GenericValue scheme, final String type, final Long entityTypeId)
                throws GenericEntityException
        {
            return null;
        }

        public List<GenericValue> getEntities(final GenericValue scheme, final String entityTypeId) throws GenericEntityException
        {
            return null;
        }

        public GenericValue createSchemeEntity(final GenericValue scheme, final SchemeEntity entity)
                throws GenericEntityException
        {
            return null;
        }

        public boolean hasSchemeAuthority(final Long entityType, final GenericValue entity)
        {
            return false;
        }

        public boolean hasSchemeAuthority(final Long entityType, final GenericValue entity, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation)
        {
            return false;
        }

        @Override
        protected AbstractSchemeRemovedFromProjectEvent createSchemeRemovedFromProjectEvent(final Scheme scheme, final Project project)
        {
            return null;
        }

        @Nonnull
        @Override
        protected AbstractSchemeAddedToProjectEvent createSchemeAddedToProjectEvent(final Scheme scheme, final Project project)
        {
            return null;
        }
    }
}
