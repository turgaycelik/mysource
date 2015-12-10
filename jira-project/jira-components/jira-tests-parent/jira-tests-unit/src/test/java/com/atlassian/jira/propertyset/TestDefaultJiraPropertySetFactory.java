package com.atlassian.jira.propertyset;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationManager;
import com.atlassian.jira.config.database.Datasource;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.ClearStatics;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.ofbiz.FieldMap;

import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.AbstractPropertySet;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.ofbiz.OFBizPropertySet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Holds unit tests for {@link DefaultJiraPropertySetFactory}
 *
 * @since v3.12
 */
public class TestDefaultJiraPropertySetFactory
{
    private static final String ENTITY_NAME = "entityName";
    private static final String ENTITY_ID = "entityId";
    private static final Long DEFAULT_ID = 1L;

    @Rule public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);
    @Rule public final ClearStatics clearStatics = new ClearStatics();

    @Mock @AvailableInContainer private DatabaseConfigurationManager mockDatabaseConfigurationManager;
    @Mock @AvailableInContainer private OfBizPropertyEntryStore mockOfBizPropertyEntryStore;

    @Mock private Datasource dataSource;
    @Mock private JiraCachingPropertySetManager mockJiraCachingPropertySetManager;


    @Before
    public void setupMocks()
    {
        DatabaseConfig dbConfig = new DatabaseConfig("defaultDs", "default", "dbtype", "dbschema", dataSource );
        when(mockDatabaseConfigurationManager.getDatabaseConfiguration()).thenReturn(dbConfig);
    }

    @Test
    public void testGetPropertySetWithDefaultEntityId()
    {
        // Set up
        final PropertySet propertySet = mock(PropertySet.class);
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory(
                mockJiraCachingPropertySetManager,
                mockOfBizPropertyEntryStore)
        {
            @Nonnull
            @Override
            PropertySet createPropertySet(Class<? extends PropertySet> propertySetClass, Map<String,Object> args)
            {
                assertEquals(OFBizPropertySet.class, propertySetClass);
                assertNotNull(args);
                assertEquals("testEntity", args.get(ENTITY_NAME));
                assertEquals(DEFAULT_ID, args.get(ENTITY_ID));
                assertEquals("default", args.get("delegator.name"));
                return propertySet;
            }
        };

        // Invoke
        defaultJiraPropertySetManager.buildNoncachingPropertySet("testEntity");

        // Check
        verifyNoMoreInteractions(mockJiraCachingPropertySetManager);
    }

    @Test
    public void testGetPropertySetWithEntityId()
    {
        // Set up
        final PropertySet propertySet = mock(PropertySet.class);
        final Long id = 20L;
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory(
                mockJiraCachingPropertySetManager,
                mockOfBizPropertyEntryStore)
        {
            @Nonnull
            @Override
            PropertySet createPropertySet(Class<? extends PropertySet> propertySetClass, Map<String,Object> args)
            {
                assertEquals(OFBizPropertySet.class, propertySetClass);
                assertNotNull(args);
                assertEquals("testEntity", args.get(ENTITY_NAME));
                assertEquals(id, args.get(ENTITY_ID));
                assertEquals("default", args.get("delegator.name"));
                return propertySet;
            }
        };

        // Invoke
        defaultJiraPropertySetManager.buildNoncachingPropertySet("testEntity", id);

        // Check
        verifyNoMoreInteractions(mockJiraCachingPropertySetManager);
    }

    @Test
    public void testGetCachingPropertySet()
    {
        // Set up
        final PropertySet propertySet = mock(PropertySet.class);
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory(
                mockJiraCachingPropertySetManager,
                mockOfBizPropertyEntryStore)
        {
            int count = 0;

            @Nonnull
            @Override
            PropertySet createPropertySet(Class<? extends PropertySet> propertySetClass, Map<String,Object> args)
            {
                ++count;
                assertEquals(CachingOfBizPropertySet.class, propertySetClass);
                assertEquals(FieldMap.build(
                    ENTITY_NAME, "testEntity",
                    ENTITY_ID, DEFAULT_ID,
                    "delegator.name", "default"),
                        args);
                assertEquals(1, count);
                return propertySet;
            }
        };

        // Invoke
        defaultJiraPropertySetManager.buildCachingDefaultPropertySet("testEntity", true);

        // Check
        verifyZeroInteractions(mockJiraCachingPropertySetManager);
    }

    @Test
    public void testGetCachingPropertySetWithEntityId()
    {
        // Set up
        final PropertySet propertySet = mock(PropertySet.class);
        final Long entityId = 20L;
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory(
                mockJiraCachingPropertySetManager,
                mockOfBizPropertyEntryStore)
        {
            int count = 0;

            @Nonnull
            @Override
            PropertySet createPropertySet(Class<? extends PropertySet> propertySetClass, Map<String,Object> args)
            {
                ++count;
                assertEquals(CachingOfBizPropertySet.class, propertySetClass);
                assertEquals(FieldMap.build(
                        ENTITY_NAME, "testEntity",
                        ENTITY_ID, entityId,
                        "delegator.name", "default"),
                        args);
                assertEquals(1, count);
                return propertySet;
            }
        };

        // Invoke
        defaultJiraPropertySetManager.buildCachingPropertySet("testEntity", entityId, true);

        // Check
        verifyZeroInteractions(mockJiraCachingPropertySetManager);
    }

    @Test
    public void testCachingPropertySetWithNullParameter()
    {
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory(
                mockJiraCachingPropertySetManager,
                mockOfBizPropertyEntryStore);
        try
        {
            defaultJiraPropertySetManager.buildCachingPropertySet(null, false);
            fail("Should have thrown NullPointerException");
        }
        catch (IllegalArgumentException e)
        {
            // yay
            verifyZeroInteractions(mockJiraCachingPropertySetManager);
        }
    }

    @Test
    public void testCreatePropertySetInitialization()
    {
        final DefaultJiraPropertySetFactory defaultJiraPropertySetManager = new DefaultJiraPropertySetFactory(
                mockJiraCachingPropertySetManager,
                mockOfBizPropertyEntryStore);
        final Map<String,Object> args = ImmutableMap.<String,Object>of("Hello", 42, "World", true);

        final PropertySet propertySet = defaultJiraPropertySetManager.createPropertySet(MockPropertySet.class, args);
        assertThat(propertySet, instanceOf(MockPropertySet.class));

        //noinspection CastToConcreteClass
        ((MockPropertySet)propertySet).assertInitializedWith(args);
        verifyZeroInteractions(mockJiraCachingPropertySetManager);
    }



    public static class MockPropertySet extends AbstractPropertySet
    {
        private final AtomicReference<Map<?,?>> args = new AtomicReference<Map<?,?>>();

        void assertInitializedWith(Map<String,Object> args)
        {
            assertEquals(args, this.args.get());
        }

        @Override
        @SuppressWarnings("rawtypes")  // Mandated by PropertySet API
        public void init(final Map config, final Map args)
        {
            if (this.args.getAndSet(args) != null)
            {
                throw new IllegalStateException("Already initialized");
            }
            assertEquals(config, ImmutableMap.of());
        }

        @Override
        protected void setImpl(final int type, final String key, final Object value) throws PropertyException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        protected Object get(final int type, final String key) throws PropertyException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        @SuppressWarnings("rawtypes")  // Mandated by PropertySet API
        public Collection getKeys(final String prefix, final int type) throws PropertyException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public int getType(final String key) throws PropertyException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean exists(final String key) throws PropertyException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void remove(final String key) throws PropertyException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void remove() throws PropertyException
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}

